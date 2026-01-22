package com.chocotweak.enchantment;

import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;

/**
 * 灵魂收割 (Soul Harvest)
 * NPC 专属附魔 - 击杀生物时有几率获得双倍经验
 * 等级 I-III: 15%/30%/45% 几率
 */
public class EnchantmentSoulHarvest extends NpcExclusiveEnchantment {

    public EnchantmentSoulHarvest() {
        super(Rarity.UNCOMMON, EnumEnchantmentType.WEAPON,
                new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
        this.setName("chocotweak.soul_harvest");
        this.setRegistryName("chocotweak", "soul_harvest");
    }

    @Override
    public int getMinEnchantability(int level) {
        return 10 + (level - 1) * 10;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return getMinEnchantability(level) + 30;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    public static float getDoubleXpChance(int level) {
        return level * 0.15f;
    }
}
