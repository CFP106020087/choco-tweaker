package com.chocotweak.magic;

import com.chocolate.chocolateQuest.items.ItemStaffBase;
import com.chocolate.chocolateQuest.magic.Awakements;
import com.chocolate.chocolateQuest.misc.EnumEnchantType;
import net.minecraft.item.ItemStack;

/**
 * 远距咏唱 - 增加法术射程觉醒
 * 
 * 等级效果：
 * - Lv1: +50% 射程
 * - Lv2: +100% 射程 (2倍)
 * - Lv3: +200% 射程 (3倍)
 * - Lv4: +300% 射程 (4倍)
 * - Lv5: +500% 射程 (6倍)
 */
public class AwakementLongRange extends Awakements {

    private static final float[] RANGE_MULTIPLIER = {
        1.0f,   // Lv0 (无效果)
        1.5f,   // Lv1 = +50%
        2.0f,   // Lv2 = +100%
        3.0f,   // Lv3 = +200%
        4.0f,   // Lv4 = +300%
        6.0f    // Lv5 = +500%
    };

    public AwakementLongRange(String name, int icon) {
        super(name, icon);
    }

    @Override
    public boolean canBeUsedOnItem(ItemStack is) {
        return is.getItem() instanceof ItemStaffBase;
    }

    @Override
    public int getLevelCost() {
        return 2;
    }

    @Override
    public boolean canBeAddedByNPC(int type) {
        return type == EnumEnchantType.STAVES.ordinal();
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    public static float getRangeMultiplier(int level) {
        if (level <= 0) return 1.0f;
        if (level >= RANGE_MULTIPLIER.length) return RANGE_MULTIPLIER[RANGE_MULTIPLIER.length - 1];
        return RANGE_MULTIPLIER[level];
    }
}
