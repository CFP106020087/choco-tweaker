package com.chocotweak.client;

import com.chocolate.chocolateQuest.ChocolateQuest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 侦察器准心瞄准系统
 * - 在视野内的怪物身上随机显示准心
 * - 被准心标记的怪物受到 100% 额外伤害
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = "chocotweak", value = Side.CLIENT)
public class ScouterTargetingSystem {

    private static final ResourceLocation CROSSHAIR_TEXTURE = new ResourceLocation("chocotweak",
            "textures/gui/scouter_crosshair.png");
    private static final Random RANDOM = new Random();

    // 当前标记的目标
    private static EntityLivingBase markedTarget = null;
    private static int markTimer = 0;
    private static final int MARK_DURATION = 60; // 3秒 = 60 ticks
    private static final int MARK_COOLDOWN = 20; // 1秒冷却

    // 标记位置 (0=头部, 1=身体, 2=腿部)
    private static int markPosition = 1;
    private static final float HEADSHOT_KILL_CHANCE = 0.35f; // 35%概率秒杀
    private static final float HEADSHOT_INSTANT_DAMAGE = 99999999.0f;

    // Gamma 值相关
    private static float originalGamma = -1.0f;
    private static boolean gammaModified = false;
    private static final float SCOUTER_GAMMA = 10.0f; // 高 gamma = 夜视效果

    // ===== 每 Tick 更新 =====
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null || mc.world == null) {
            // 恢复 gamma
            if (gammaModified) {
                mc.gameSettings.gammaSetting = originalGamma;
                gammaModified = false;
            }
            return;
        }

        // 检查是否有侦察器
        boolean hasScouter = hasScouter(player);

        // Gamma 夜视效果
        if (hasScouter) {
            if (!gammaModified) {
                originalGamma = mc.gameSettings.gammaSetting;
                gammaModified = true;
            }
            mc.gameSettings.gammaSetting = SCOUTER_GAMMA;
        } else {
            if (gammaModified) {
                mc.gameSettings.gammaSetting = originalGamma;
                gammaModified = false;
            }
            markedTarget = null;
            return;
        }

        // 更新标记计时器
        if (markTimer > 0) {
            markTimer--;
        }

        // 清理无效目标
        if (markedTarget != null && (markedTarget.isDead || !markedTarget.isEntityAlive())) {
            markedTarget = null;
        }

        // 寻找新目标
        if (markTimer <= 0) {
            findAndMarkTarget(player, mc);
            markTimer = MARK_COOLDOWN;
        }

        // 同步标记状态到服务端（用于伤害加成）
        if (markedTarget != null) {
            markedTarget.getEntityData().setBoolean("chocotweak_scouter_marked", true);
        }
    }

    // ===== 寻找并标记目标 =====
    private static void findAndMarkTarget(EntityPlayer player, Minecraft mc) {
        // 获取视野内的敌对生物
        List<EntityLivingBase> visibleMobs = new ArrayList<>();

        double range = 32.0;
        AxisAlignedBB searchBox = player.getEntityBoundingBox().grow(range);
        List<Entity> entities = mc.world.getEntitiesWithinAABB(EntityLivingBase.class, searchBox);

        for (Entity entity : entities) {
            if (!(entity instanceof EntityLivingBase))
                continue;
            if (entity == player)
                continue;

            EntityLivingBase living = (EntityLivingBase) entity;

            // 只标记敌对生物
            if (!(living instanceof EntityMob))
                continue;

            // 检查是否在视野内
            if (isInPlayerView(player, living)) {
                visibleMobs.add(living);
            }
        }

        // 选择最近的敌人
        if (!visibleMobs.isEmpty()) {
            // 按距离排序，选择最近的
            EntityLivingBase closest = null;
            double closestDist = Double.MAX_VALUE;
            for (EntityLivingBase mob : visibleMobs) {
                double dist = player.getDistanceSq(mob);
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = mob;
                }
            }

            // 如果目标改变，重新选择标记位置
            if (closest != null && closest != markedTarget) {
                markedTarget = closest;
                // 随机选择标记位置: 0=头部(35%), 1=身体(40%), 2=腿部(25%)
                float posRoll = RANDOM.nextFloat();
                if (posRoll < 0.35f) {
                    markPosition = 0; // 头部 - 秒杀机会
                } else if (posRoll < 0.75f) {
                    markPosition = 1; // 身体
                } else {
                    markPosition = 2; // 腿部
                }
                // 同步到实体数据
                markedTarget.getEntityData().setInteger("chocotweak_mark_position", markPosition);
            }
        }
    }

    // ===== 检查是否在玩家视野内 =====
    private static boolean isInPlayerView(EntityPlayer player, Entity target) {
        Vec3d look = player.getLookVec();
        Vec3d toTarget = new Vec3d(
                target.posX - player.posX,
                target.posY + target.height / 2 - (player.posY + player.getEyeHeight()),
                target.posZ - player.posZ).normalize();

        double dot = look.dotProduct(toTarget);
        // 约60度视角内
        return dot > 0.5;
    }

    // ===== 渲染 HUD 准心 =====
    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL)
            return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null)
            return;

        if (!hasScouter(player))
            return;

        // 如果有标记目标，显示信息
        if (markedTarget != null && markedTarget.isEntityAlive()) {
            ScaledResolution sr = new ScaledResolution(mc);
            int width = sr.getScaledWidth();
            int height = sr.getScaledHeight();

            // 显示目标信息
            String targetName = markedTarget.getName();
            int health = (int) markedTarget.getHealth();
            int maxHealth = (int) markedTarget.getMaxHealth();

            // 位置标记
            String posName = markPosition == 0 ? "§c§l[HEAD]" : (markPosition == 1 ? "[BODY]" : "[LEGS]");
            String bonusText = markPosition == 0 ? "§c§l秒杀!" : "+100% DMG";

            String info = posName + " " + targetName + " [" + health + "/" + maxHealth + "] " + bonusText;

            GlStateManager.pushMatrix();
            mc.fontRenderer.drawStringWithShadow(info, width / 2 - mc.fontRenderer.getStringWidth(info) / 2,
                    height / 2 + 30, markPosition == 0 ? 0xFF0000 : 0xFF5555);
            GlStateManager.popMatrix();
        }
    }

    // ===== 在世界中渲染准心 =====
    @SubscribeEvent
    public static void onRenderWorld(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null)
            return;

        if (!hasScouter(player))
            return;
        if (markedTarget == null || !markedTarget.isEntityAlive())
            return;

        // 渲染 3D 准心在目标身上
        renderTargetCrosshair(markedTarget, event.getPartialTicks());
    }

    // ===== 渲染目标准心 =====
    private static void renderTargetCrosshair(EntityLivingBase target, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        RenderManager renderManager = mc.getRenderManager();

        // 根据标记位置计算Y偏移
        double yOffset;
        switch (markPosition) {
            case 0: // 头部
                yOffset = target.height + 0.3;
                break;
            case 2: // 腿部
                yOffset = target.height * 0.25;
                break;
            default: // 身体
                yOffset = target.height * 0.6;
                break;
        }

        double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * partialTicks - renderManager.viewerPosX;
        double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * partialTicks - renderManager.viewerPosY
                + yOffset;
        double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * partialTicks - renderManager.viewerPosZ;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        // 始终面向玩家
        GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(renderManager.playerViewX, 1.0f, 0.0f, 0.0f);

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D(); // 必须禁用纹理才能看到 GL_LINES
        GL11.glLineWidth(3.0f); // 增加线宽

        // 绘制准心
        drawCrosshair();

        GL11.glLineWidth(1.0f);
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    // ===== 绘制准心图形 =====
    private static void drawCrosshair() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        float size = 0.5f;
        float pulse = (float) Math.sin(System.currentTimeMillis() / 100.0) * 0.1f + 1.0f;
        size *= pulse;

        // 红色准心
        GlStateManager.color(1.0f, 0.2f, 0.2f, 0.9f);

        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        // 水平线
        buffer.pos(-size, 0, 0).endVertex();
        buffer.pos(-size * 0.3, 0, 0).endVertex();
        buffer.pos(size * 0.3, 0, 0).endVertex();
        buffer.pos(size, 0, 0).endVertex();

        // 垂直线
        buffer.pos(0, -size, 0).endVertex();
        buffer.pos(0, -size * 0.3, 0).endVertex();
        buffer.pos(0, size * 0.3, 0).endVertex();
        buffer.pos(0, size, 0).endVertex();

        tessellator.draw();

        // 外圈
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        int segments = 16;
        for (int i = 0; i < segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            buffer.pos(Math.cos(angle) * size * 0.8, Math.sin(angle) * size * 0.8, 0).endVertex();
        }
        tessellator.draw();

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    // ===== 检查是否在 Baubles 槽位有侦察器 =====
    private static boolean hasScouter(EntityPlayer player) {
        // 只检查 Baubles 槽位
        return com.chocotweak.compat.BaublesCompat.hasBaubleItem(player, ChocolateQuest.scouter);
    }

    // ===== 获取当前标记目标（供其他类使用）=====
    public static EntityLivingBase getMarkedTarget() {
        return markedTarget;
    }
}
