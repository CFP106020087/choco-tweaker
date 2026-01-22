package com.chocotweak.enchantment;

import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.potion.PotionEffect;

/**
 * 冰霜行者 (Permafrost)
 * NPC 专属附魔 - 受到伤害时有几率冻结攻击者
 * 等级 I-III: 10%/20%/30% 几率
 * 冻结时间: 1+等级 秒 (缓慢 IV + 虚弱)
 */
public class EnchantmentPermafrost extends NpcExclusiveEnchantment {

    public EnchantmentPermafrost() {
        super(Rarity.RARE, EnumEnchantmentType.ARMOR_CHEST,
                new EntityEquipmentSlot[] { EntityEquipmentSlot.CHEST });
        this.setName("chocotweak.permafrost");
        this.setRegistryName("chocotweak", "permafrost");
    }

    @Override
    public int getMinEnchantability(int level) {
        return 15 + (level - 1) * 10;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return getMinEnchantability(level) + 30;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    /**
     * 计算冻结几率
     * 
     * @param level 附魔等级
     * @return 几率 (0.0-1.0)
     */
    public static float getFreezeChance(int level) {
        return level * 0.10f; // 10% 每级
    }

    /**
     * 计算冻结持续时间（tick）
     * 
     * @param level 附魔等级
     * @return 持续时间 (ticks)
     */
    public static int getFreezeDuration(int level) {
        return (1 + level) * 20; // (1+等级) 秒
    }

    /**
     * 对攻击者施加冰冻效果
     * 
     * @param attacker 攻击者
     * @param level    附魔等级
     */
    public static void applyFreeze(EntityLivingBase attacker, int level) {
        if (attacker == null)
            return;

        int duration = getFreezeDuration(level);

        // 缓慢 IV - 几乎无法移动
        attacker.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, duration, 3));

        // 虚弱 - 降低攻击力
        attacker.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, duration, 1));

        // 挖掘疲劳 - 无法快速破坏方块
        attacker.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, duration, 2));
    }
}
