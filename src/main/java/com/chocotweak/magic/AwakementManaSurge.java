package com.chocotweak.magic;

import com.chocolate.chocolateQuest.items.ItemStaffBase;
import com.chocolate.chocolateQuest.magic.Awakements;
import com.chocolate.chocolateQuest.misc.EnumEnchantType;
import net.minecraft.item.ItemStack;

/**
 * 魔力洪流 - 降低法术消耗觉醒
 * 
 * 等级效果：
 * - Lv1: -30% 消耗
 * - Lv2: -50% 消耗
 * - Lv3: -70% 消耗
 * - Lv4: -85% 消耗
 * - Lv5: -95% 消耗 (几乎免费)
 */
public class AwakementManaSurge extends Awakements {

    private static final float[] COST_MULTIPLIER = {
        1.0f,   // Lv0 (无效果)
        0.7f,   // Lv1 = -30%
        0.5f,   // Lv2 = -50%
        0.3f,   // Lv3 = -70%
        0.15f,  // Lv4 = -85%
        0.05f   // Lv5 = -95%
    };

    public AwakementManaSurge(String name, int icon) {
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

    public static float getCostMultiplier(int level) {
        if (level <= 0) return 1.0f;
        if (level >= COST_MULTIPLIER.length) return COST_MULTIPLIER[COST_MULTIPLIER.length - 1];
        return COST_MULTIPLIER[level];
    }
}
