package com.chocotweak.handler;

import com.chocolate.chocolateQuest.ChocolateQuest;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

/**
 * 特殊物品效果处理器
 * 
 * 云靴: 免疫摔落 + 15%移速 + 空中移动增强 + 五段跳
 * 龙头盔: +200%攻击力 + 20生命
 * 侦察器: 永久夜视 + 准心瞄准加成
 * 女巫帽: +20魔力 +30最大魔力 (原版已有)
 */
@Mod.EventBusSubscriber(modid = "chocotweak")
public class SpecialItemHandler {

    // 属性修改器 UUID
    private static final UUID CLOUD_SPEED_UUID = UUID.fromString("1A2B3C4D-5678-9ABC-DEF0-123456789ABC");
    private static final UUID DRAGON_ATTACK_UUID = UUID.fromString("2B3C4D5E-6789-ABCD-EF01-23456789ABCD");
    private static final UUID DRAGON_HEALTH_UUID = UUID.fromString("3C4D5E6F-789A-BCDE-F012-3456789ABCDE");

    // 五段跳常量
    private static final int MAX_AIR_JUMPS = 5;
    private static final String AIR_JUMP_TAG = "chocotweak_air_jumps";

    // ===== 每 Tick 更新事件 =====
    @SubscribeEvent
    public static void onLivingUpdate(LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        // === 云靴效果 ===
        if (hasCloudBoots(player)) {
            // 摔落距离重置
            if (player.fallDistance >= 3.0f) {
                player.fallDistance = 0.0f;
                if (player.world.isRemote) {
                    for (int i = 0; i < 3; i++) {
                        player.world.spawnParticle(EnumParticleTypes.CLOUD,
                                player.posX, player.posY - 2.0, player.posZ,
                                (player.world.rand.nextFloat() - 0.5f) / 2.0f, -0.5,
                                (player.world.rand.nextFloat() - 0.5f) / 2.0f);
                    }
                }
            }

            // 冲刺时云粒子
            if (player.isSprinting() && player.world.isRemote) {
                player.world.spawnParticle(EnumParticleTypes.CLOUD,
                        player.posX, player.posY - 1.5, player.posZ,
                        (player.world.rand.nextFloat() - 0.5f) / 2.0f, 0.1,
                        (player.world.rand.nextFloat() - 0.5f) / 2.0f);
            }

            // 空中移动增强
            if (!player.onGround) {
                player.jumpMovementFactor += 0.03f;
            }

            // 落地时重置跳跃次数
            if (player.onGround) {
                player.getEntityData().setInteger(AIR_JUMP_TAG, 0);
            }
        }

        // === 侦察器效果 ===
        // 夜视效果在客户端 ScouterTargetingSystem 通过 gamma 实现

        // === 女巫帽效果 ===
        // 设置标记，用于法术系统检测（+20%施法速度 +30%魔法消耗减少）
        if (hasWitchHat(player)) {
            player.getEntityData().setBoolean("chocotweak_witch_hat", true);
        } else {
            player.getEntityData().removeTag("chocotweak_witch_hat");
        }

        // === 更新属性修改器 ===
        updateSpecialItemAttributes(player);
    }

    // ===== 跳跃事件 - 五段跳 =====
    @SubscribeEvent
    public static void onLivingJump(LivingJumpEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        // 云靴五段跳
        if (hasCloudBoots(player) && !player.onGround) {
            int airJumps = player.getEntityData().getInteger(AIR_JUMP_TAG);
            if (airJumps < MAX_AIR_JUMPS) {
                player.motionY = 0.42; // 标准跳跃高度
                player.getEntityData().setInteger(AIR_JUMP_TAG, airJumps + 1);

                // 跳跃粒子
                if (player.world.isRemote) {
                    for (int i = 0; i < 5; i++) {
                        player.world.spawnParticle(EnumParticleTypes.CLOUD,
                                player.posX + (player.world.rand.nextDouble() - 0.5) * player.width,
                                player.posY,
                                player.posZ + (player.world.rand.nextDouble() - 0.5) * player.width,
                                0, -0.1, 0);
                    }
                }
            }
        }
    }

    // ===== 摔落事件 - 云靴免疫 =====
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        // 云靴免疫摔落伤害
        if (hasCloudBoots(player)) {
            event.setCanceled(true);
        }
    }

    // ===== 攻击加成 - 侦察器准心 =====
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        EntityLivingBase target = event.getEntityLiving();

        // 侦察器：攻击被标记的目标
        if (hasScouter(player)) {
            if (target.getEntityData().getBoolean("chocotweak_scouter_marked")) {
                int markPos = target.getEntityData().getInteger("chocotweak_mark_position");

                if (markPos == 0) {
                    // 头部标记：35%概率秒杀
                    if (player.world.rand.nextFloat() < 0.35f) {
                        // 秒杀！真实伤害
                        event.setAmount(99999999.0f);
                        // 清除标记
                        target.getEntityData().removeTag("chocotweak_scouter_marked");
                        target.getEntityData().removeTag("chocotweak_mark_position");
                    } else {
                        // 未触发秒杀，100%加成
                        event.setAmount(event.getAmount() * 2.0f);
                    }
                } else {
                    // 非头部标记：100%伤害加成
                    event.setAmount(event.getAmount() * 2.0f);
                }
            }
        }
    }

    // ===== 属性更新 =====
    private static void updateSpecialItemAttributes(EntityPlayer player) {
        IAttributeInstance attackAttr = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        IAttributeInstance healthAttr = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        IAttributeInstance speedAttr = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);

        // 移除旧修改器
        attackAttr.removeModifier(DRAGON_ATTACK_UUID);
        healthAttr.removeModifier(DRAGON_HEALTH_UUID);
        speedAttr.removeModifier(CLOUD_SPEED_UUID);

        // 龙头盔: +200%攻击力 +20生命
        if (hasDragonHelmet(player)) {
            double baseAttack = attackAttr.getBaseValue();
            attackAttr.applyModifier(
                    new AttributeModifier(DRAGON_ATTACK_UUID, "Dragon Helmet Attack", baseAttack * 2.0, 0));
            healthAttr.applyModifier(new AttributeModifier(DRAGON_HEALTH_UUID, "Dragon Helmet Health", 20.0, 0));
        }

        // 云靴: +15%移速
        if (hasCloudBoots(player)) {
            speedAttr.applyModifier(new AttributeModifier(CLOUD_SPEED_UUID, "Cloud Boots Speed", 0.15, 2));
        }
    }

    // ===== 辅助方法 =====

    private static boolean hasCloudBoots(EntityPlayer player) {
        // 只检查 Baubles 槽位
        return checkBaubles(player, ChocolateQuest.cloudBoots);
    }

    private static boolean hasDragonHelmet(EntityPlayer player) {
        // 只检查 Baubles 槽位
        return checkBaubles(player, ChocolateQuest.dragonHelmet);
    }

    private static boolean hasScouter(EntityPlayer player) {
        // 只检查 Baubles 槽位
        return checkBaubles(player, ChocolateQuest.scouter);
    }

    private static boolean hasWitchHat(EntityPlayer player) {
        // 只检查 Baubles 槽位
        return checkBaubles(player, ChocolateQuest.witchHat);
    }

    /**
     * 检查物品栏是否有指定物品
     */
    private static boolean hasItemInInventory(EntityPlayer player, Item item) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查 Baubles 槽位是否有指定物品
     */
    private static boolean checkBaubles(EntityPlayer player, Item item) {
        try {
            return com.chocotweak.compat.BaublesCompat.hasBaubleItem(player, item);
        } catch (NoClassDefFoundError e) {
            return false; // Baubles 未加载
        }
    }
}
