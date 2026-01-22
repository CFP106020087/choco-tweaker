package com.chocotweak.magic;

import com.chocolate.chocolateQuest.items.swords.ItemCQBlade;
import com.chocolate.chocolateQuest.magic.Awakements;
import net.minecraft.item.ItemStack;

/**
 * 药水瓶扩展 - 增加武器可附魔的药水数量
 * 
 * 等级效果：
 * - 无觉醒: 1个药水槽
 * - Lv1: 2个药水槽
 * - Lv2: 3个药水槽
 * - Lv3: 3个药水槽 + 药水效果增强25%
 */
public class AwakementPotionCapacity extends Awakements {

    public AwakementPotionCapacity(String name, int icon) {
        super(name, icon);
    }

    @Override
    public boolean canBeUsedOnItem(ItemStack is) {
        // 只能用于CQ武器（剑类）
        return is.getItem() instanceof ItemCQBlade;
    }

    @Override
    public int getLevelCost() {
        return 4; // 每级4点经验
    }

    @Override
    public boolean canBeAddedByNPC(int type) {
        // 铁匠NPC提供此服务
        return type == com.chocolate.chocolateQuest.misc.EnumEnchantType.BLACKSMITH.ordinal();
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    /**
     * 获取允许的药水槽数量
     */
    public static int getPotionSlots(int level) {
        if (level <= 0)
            return 1; // 无觉醒 = 1槽
        if (level == 1)
            return 2; // Lv1 = 2槽
        return 3; // Lv2+ = 3槽
    }

    /**
     * 获取药水效果增强倍率
     */
    public static float getPotionBonus(int level) {
        if (level >= 3)
            return 1.25f; // Lv3 = +25%效果
        return 1.0f;
    }

    /**
     * 获取武器的药水槽数量
     */
    public static int getPotionSlotsForWeapon(ItemStack weapon) {
        if (weapon.isEmpty())
            return 0;
        int level = Awakements.getEnchantLevel(weapon,
                com.chocotweak.mixin.MixinAwakementsRegister.potionCapacity);
        return getPotionSlots(level);
    }
}
