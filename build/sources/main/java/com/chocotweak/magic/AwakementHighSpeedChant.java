package com.chocotweak.magic;

import com.chocolate.chocolateQuest.items.ItemStaffBase;
import com.chocolate.chocolateQuest.magic.Awakements;
import com.chocolate.chocolateQuest.misc.EnumEnchantType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 高速神言 - 法杖连续施法觉醒
 * 
 * 需要完成任务1解锁
 * 
 * 等级效果：
 * - Lv1: 连续施法持续 3秒 (60 ticks)
 * - Lv2: 连续施法持续 5秒 (100 ticks)
 * - Lv3: 连续施法持续 8秒 (160 ticks)
 * - Lv4: 连续施法持续 12秒 (240 ticks)
 * - Lv5: 无限制 (Integer.MAX_VALUE)
 */
public class AwakementHighSpeedChant extends Awakements {

    private static final int[] DURATION_PER_LEVEL = {
            0,
            60, // Lv1 = 3秒
            100, // Lv2 = 5秒
            160, // Lv3 = 8秒
            240, // Lv4 = 12秒
            Integer.MAX_VALUE // Lv5 = 无限
    };

    public AwakementHighSpeedChant(String name, int icon) {
        super(name, icon);
    }

    @Override
    public boolean canBeUsedOnItem(ItemStack is) {
        // 必须是法杖
        if (!(is.getItem() instanceof ItemStaffBase)) {
            return false;
        }

        // TODO: 解锁检查暂时禁用，待任务系统完成后启用
        // EntityPlayer player = getClientPlayer();
        // if (player != null && !SpellAwakeningUnlockTracker.isUnlocked(player,
        // SpellAwakeningUnlockTracker.AWAKENING_HIGH_SPEED_CHANT)) {
        // return false;
        // }

        return true;
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

    public static int getDurationForLevel(int level) {
        if (level <= 0)
            return 0;
        if (level >= DURATION_PER_LEVEL.length)
            return Integer.MAX_VALUE;
        return DURATION_PER_LEVEL[level];
    }

    public static boolean isUnlimited(int level) {
        return level >= 5;
    }

    @SideOnly(Side.CLIENT)
    private static EntityPlayer getClientPlayer() {
        try {
            return Minecraft.getMinecraft().player;
        } catch (Exception e) {
            return null;
        }
    }
}
