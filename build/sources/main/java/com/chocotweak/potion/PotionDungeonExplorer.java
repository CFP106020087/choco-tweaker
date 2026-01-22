package com.chocotweak.potion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * "寻找巧克力!" 药水效果
 * 
 * 在地牢区域内施加此效果，限制玩家行为：
 * - 禁止飞行
 * - 禁止挖掘
 * - 禁止放置方块
 * - 禁止开箱子
 * - 禁止漏斗偷取
 * - 禁止喷气背包等motion操控
 * - 摔落伤害归零
 */
public class PotionDungeonExplorer extends Potion {

    public static PotionDungeonExplorer INSTANCE;
    public static final String REGISTRY_NAME = "chocotweak:dungeon_explorer";

    private static final ResourceLocation ICON_TEXTURE = new ResourceLocation("chocotweak",
            "textures/gui/dungeon_explorer.png");

    public PotionDungeonExplorer() {
        super(false, 0xD4A017); // 金黄色，正面效果
        this.setPotionName("effect.chocotweak.dungeon_explorer");
        this.setRegistryName(new ResourceLocation("chocotweak", "dungeon_explorer"));
        this.setBeneficial(); // 标记为正面效果
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true; // 每tick都生效
    }

    @Override
    public boolean isInstant() {
        return false;
    }

    @Override
    public boolean isBadEffect() {
        return false; // 正面效果
    }

    @Override
    public boolean shouldRender(PotionEffect effect) {
        return true;
    }

    @Override
    public boolean shouldRenderInvText(PotionEffect effect) {
        return true;
    }

    @Override
    public boolean shouldRenderHUD(PotionEffect effect) {
        return true;
    }

    /**
     * 渲染自定义图标
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        mc.getTextureManager().bindTexture(ICON_TEXTURE);
        GlStateManager.enableBlend();
        Gui.drawModalRectWithCustomSizedTexture(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
        GlStateManager.disableBlend();
    }

    /**
     * 渲染 HUD 图标
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
        mc.getTextureManager().bindTexture(ICON_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        Gui.drawModalRectWithCustomSizedTexture(x + 3, y + 3, 0, 0, 18, 18, 18, 18);
        GlStateManager.disableBlend();
    }

    /**
     * 检查玩家是否有此效果
     */
    public static boolean hasEffect(net.minecraft.entity.EntityLivingBase entity) {
        return INSTANCE != null && entity.isPotionActive(INSTANCE);
    }

    /**
     * 给玩家施加效果 (30秒 = 600 ticks)
     */
    public static void applyEffect(net.minecraft.entity.EntityLivingBase entity) {
        if (INSTANCE != null) {
            entity.addPotionEffect(new PotionEffect(INSTANCE, 600, 0, false, true));
        }
    }
}
