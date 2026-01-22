package com.chocotweak.magic;

import com.chocolate.chocolateQuest.items.ItemStaffBase;
import com.chocolate.chocolateQuest.magic.Awakements;
import com.chocolate.chocolateQuest.misc.EnumEnchantType;
import net.minecraft.item.ItemStack;

import java.util.Random;

/**
 * 回响之音 - 几率免费重放法术觉醒
 * 
 * 等级效果：
 * - Lv1: 15% 几率
 * - Lv2: 30% 几率
 * - Lv3: 45% 几率
 * - Lv4: 60% 几率
 * - Lv5: 80% 几率
 */
public class AwakementEchoingVoice extends Awakements {

    private static final float[] ECHO_CHANCE = {
        0.0f,   // Lv0 (无效果)
        0.15f,  // Lv1 = 15%
        0.30f,  // Lv2 = 30%
        0.45f,  // Lv3 = 45%
        0.60f,  // Lv4 = 60%
        0.80f   // Lv5 = 80%
    };

    private static final Random random = new Random();

    public AwakementEchoingVoice(String name, int icon) {
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

    public static float getEchoChance(int level) {
        if (level <= 0) return 0.0f;
        if (level >= ECHO_CHANCE.length) return ECHO_CHANCE[ECHO_CHANCE.length - 1];
        return ECHO_CHANCE[level];
    }

    /**
     * 检查是否触发回响
     */
    public static boolean shouldEcho(int level) {
        float chance = getEchoChance(level);
        return random.nextFloat() < chance;
    }
}
