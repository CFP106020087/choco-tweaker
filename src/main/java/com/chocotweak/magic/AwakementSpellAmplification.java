package com.chocotweak.magic;

import com.chocolate.chocolateQuest.items.ItemStaffBase;
import com.chocolate.chocolateQuest.magic.Awakements;
import com.chocolate.chocolateQuest.misc.EnumEnchantType;
import net.minecraft.item.ItemStack;

/**
 * 咒文增幅 - 法术伤害提升觉醒
 * 
 * 等级效果：
 * - Lv1: +50% 伤害
 * - Lv2: +100% 伤害 (2倍)
 * - Lv3: +200% 伤害 (3倍)
 * - Lv4: +400% 伤害 (5倍)
 * - Lv5: +900% 伤害 (10倍)
 */
public class AwakementSpellAmplification extends Awakements {

    private static final float[] DAMAGE_MULTIPLIER = {
        1.0f,   // Lv0 (无效果)
        1.5f,   // Lv1 = +50%
        2.0f,   // Lv2 = +100%
        3.0f,   // Lv3 = +200%
        5.0f,   // Lv4 = +400%
        10.0f   // Lv5 = +900%
    };

    public AwakementSpellAmplification(String name, int icon) {
        super(name, icon);
    }

    @Override
    public boolean canBeUsedOnItem(ItemStack is) {
        return is.getItem() instanceof ItemStaffBase;
    }

    @Override
    public int getLevelCost() {
        return 4; // 高成本
    }

    @Override
    public boolean canBeAddedByNPC(int type) {
        return type == EnumEnchantType.STAVES.ordinal();
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    public static float getDamageMultiplier(int level) {
        if (level <= 0) return 1.0f;
        if (level >= DAMAGE_MULTIPLIER.length) return DAMAGE_MULTIPLIER[DAMAGE_MULTIPLIER.length - 1];
        return DAMAGE_MULTIPLIER[level];
    }
}
