package com.chocotweak.magic;

import com.chocolate.chocolateQuest.items.ItemStaffBase;
import com.chocolate.chocolateQuest.magic.Awakements;
import com.chocolate.chocolateQuest.misc.EnumEnchantType;
import net.minecraft.item.ItemStack;

/**
 * 瞬发咏唱 - 减少施法时间和冷却觉醒
 * 
 * 等级效果：
 * - Lv1: -30% 冷却/施法时间
 * - Lv2: -50% 冷却/施法时间
 * - Lv3: -70% 冷却/施法时间
 * - Lv4: -85% 冷却/施法时间
 * - Lv5: -95% 冷却/施法时间 (几乎瞬发)
 */
public class AwakementInstantCast extends Awakements {

    private static final float[] COOLDOWN_MULTIPLIER = {
        1.0f,   // Lv0 (无效果)
        0.7f,   // Lv1 = -30%
        0.5f,   // Lv2 = -50%
        0.3f,   // Lv3 = -70%
        0.15f,  // Lv4 = -85%
        0.05f   // Lv5 = -95%
    };

    public AwakementInstantCast(String name, int icon) {
        super(name, icon);
    }

    @Override
    public boolean canBeUsedOnItem(ItemStack is) {
        return is.getItem() instanceof ItemStaffBase;
    }

    @Override
    public int getLevelCost() {
        return 3;
    }

    @Override
    public boolean canBeAddedByNPC(int type) {
        return type == EnumEnchantType.STAVES.ordinal();
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    public static float getCooldownMultiplier(int level) {
        if (level <= 0) return 1.0f;
        if (level >= COOLDOWN_MULTIPLIER.length) return COOLDOWN_MULTIPLIER[COOLDOWN_MULTIPLIER.length - 1];
        return COOLDOWN_MULTIPLIER[level];
    }
}
