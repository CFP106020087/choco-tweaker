package com.chocotweak.enchantment;

import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;

/**
 * 吸血 (Vampiric)
 * NPC 专属附魔 - 攻击时回复造成伤害的一定比例生命值
 * 等级 I-III: 5%/10%/15% 伤害值
 */
public class EnchantmentVampiric extends NpcExclusiveEnchantment {

    public EnchantmentVampiric() {
        super(Rarity.RARE, EnumEnchantmentType.WEAPON,
                new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
        this.setName("chocotweak.vampiric");
        this.setRegistryName("chocotweak", "vampiric");
    }

    @Override
    public int getMinEnchantability(int level) {
        return 15 + (level - 1) * 12;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return getMinEnchantability(level) + 35;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    /**
     * 计算生命吸取比例
     * 
     * @param level 附魔等级
     * @return 比例 (0.0-1.0)
     */
    public static float getLifestealPercent(int level) {
        return level * 0.05f; // 5% 每级
    }
}
