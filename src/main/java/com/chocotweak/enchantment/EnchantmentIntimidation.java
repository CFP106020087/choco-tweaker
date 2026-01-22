package com.chocotweak.enchantment;

import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

/**
 * 威压 (Intimidation)
 * NPC 专属附魔 - 附近敌对生物受到虚弱效果
 * 等级 I-II: 4/8 格范围
 * 每 2 秒刷新效果
 */
public class EnchantmentIntimidation extends NpcExclusiveEnchantment {

    public EnchantmentIntimidation() {
        super(Rarity.UNCOMMON, EnumEnchantmentType.ARMOR_HEAD,
                new EntityEquipmentSlot[] { EntityEquipmentSlot.HEAD });
        this.setName("chocotweak.intimidation");
        this.setRegistryName("chocotweak", "intimidation");
    }

    @Override
    public int getMinEnchantability(int level) {
        return 12 + (level - 1) * 10;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return getMinEnchantability(level) + 25;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    /**
     * 计算威压范围
     * 
     * @param level 附魔等级
     * @return 范围（格）
     */
    public static double getRange(int level) {
        return level * 4.0; // 4格 每级
    }

    /**
     * 对范围内的敌对生物施加虚弱效果
     * 
     * @param player 拥有附魔的玩家
     * @param level  附魔等级
     */
    public static void applyIntimidation(EntityPlayer player, int level) {
        if (player == null || player.world.isRemote)
            return;

        double range = getRange(level);
        AxisAlignedBB area = player.getEntityBoundingBox().grow(range);

        // 获取范围内的所有敌对生物
        List<EntityMob> mobs = player.world.getEntitiesWithinAABB(EntityMob.class, area,
                mob -> mob != null && mob.isEntityAlive() && !mob.isOnSameTeam(player));

        for (EntityMob mob : mobs) {
            // 施加虚弱效果 (3秒持续，每2秒刷新)
            mob.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 60, level - 1));
        }
    }

    /**
     * 检查玩家是否应该激活威压效果
     * 每 40 tick (2秒) 调用一次
     * 
     * @param player 玩家
     * @return 附魔等级，0表示没有附魔
     */
    public static int getIntimidationLevel(EntityPlayer player) {
        if (player == null)
            return 0;

        ItemStack helmet = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (helmet.isEmpty())
            return 0;

        return net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(
                ModEnchantments.INTIMIDATION, helmet);
    }
}
