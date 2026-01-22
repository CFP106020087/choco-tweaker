package com.chocotweak.handler;

import com.chocolate.chocolateQuest.items.ItemArmorKing;
import com.chocolate.chocolateQuest.items.swords.ItemSwordWalker;
import com.chocolate.chocolateQuest.particles.EffectManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 深渊漫步者之王胸甲效果处理器
 * 
 * 效果：
 * - 穿戴胸甲：+50%伤害输出, +30%易伤
 * - 手持Walker剑/剑盾：脚下黑色沙尘暴 + 飞行能力
 */
public class AbyssWalkerArmorHandler {

    /** 增伤倍率 (+100%) */
    private static final float DAMAGE_BOOST = 2.0f;

    /** 易伤倍率 */
    private static final float VULNERABILITY = 1.3f;

    /** 沙尘暴粒子数量 */
    private static final int STORM_PARTICLES = 8;

    /** 跟踪哪些玩家正在使用飞行能力 */
    private static final Set<UUID> flyingPlayers = new HashSet<>();

    /**
     * 伤害处理：增伤 + 易伤
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onLivingHurt(LivingHurtEvent event) {
        // 增伤：如果攻击者穿戴King胸甲
        if (event.getSource().getTrueSource() instanceof EntityPlayer) {
            EntityPlayer attacker = (EntityPlayer) event.getSource().getTrueSource();
            if (isWearingKingArmor(attacker)) {
                event.setAmount(event.getAmount() * DAMAGE_BOOST);
            }
        }

        // 易伤：如果受害者穿戴King胸甲
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer victim = (EntityPlayer) event.getEntityLiving();
            if (isWearingKingArmor(victim)) {
                event.setAmount(event.getAmount() * VULNERABILITY);
            }
        }
    }

    /**
     * 玩家tick：飞行能力 + 沙尘暴粒子
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        EntityPlayer player = event.player;
        boolean shouldFly = isWearingKingArmor(player) && isHoldingWalkerSword(player);

        if (shouldFly) {
            // 启用飞行
            if (!player.capabilities.allowFlying && !player.isCreative() && !player.isSpectator()) {
                player.capabilities.allowFlying = true;
                flyingPlayers.add(player.getUniqueID());
                player.sendPlayerAbilities();
            }

            // 客户端渲染沙尘暴粒子
            if (player.world.isRemote) {
                spawnBlackStormParticles(player);
            }
        } else {
            // 关闭飞行（如果是我们给的）
            if (flyingPlayers.contains(player.getUniqueID())) {
                if (!player.isCreative() && !player.isSpectator()) {
                    player.capabilities.allowFlying = false;
                    player.capabilities.isFlying = false;
                    flyingPlayers.remove(player.getUniqueID());
                    player.sendPlayerAbilities();
                }
            }
        }
    }

    /**
     * 检查是否穿戴King胸甲
     */
    private boolean isWearingKingArmor(EntityPlayer player) {
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chest.isEmpty())
            return false;

        // 检查是否是ItemArmorKing
        return chest.getItem() instanceof ItemArmorKing;
    }

    /**
     * 检查是否手持Walker剑
     */
    private boolean isHoldingWalkerSword(EntityPlayer player) {
        ItemStack mainHand = player.getHeldItemMainhand();
        ItemStack offHand = player.getHeldItemOffhand();

        return isWalkerSword(mainHand) || isWalkerSword(offHand);
    }

    /**
     * 检查物品是否是Walker剑
     */
    private boolean isWalkerSword(ItemStack stack) {
        if (stack.isEmpty())
            return false;

        // 检查是否是ItemSwordWalker
        return stack.getItem() instanceof ItemSwordWalker;
    }

    /**
     * 渲染黑色沙尘暴粒子
     */
    @SideOnly(Side.CLIENT)
    private void spawnBlackStormParticles(EntityPlayer player) {
        if (!player.world.isRemote)
            return;

        for (int i = 0; i < STORM_PARTICLES; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = 0.5 + Math.random() * 1.5;
            double x = player.posX + Math.cos(angle) * radius;
            double y = player.posY + Math.random() * 0.5;
            double z = player.posZ + Math.sin(angle) * radius;

            // 向上旋转的运动
            double motionX = Math.cos(angle + Math.PI / 2) * 0.1;
            double motionY = 0.1 + Math.random() * 0.2;
            double motionZ = Math.sin(angle + Math.PI / 2) * 0.1;

            // 使用 dust_walker 粒子类型 (黑色沙尘)
            EffectManager.spawnParticle(EffectManager.dust_walker,
                    player.world, x, y, z, motionX, motionY, motionZ);
        }
    }
}
