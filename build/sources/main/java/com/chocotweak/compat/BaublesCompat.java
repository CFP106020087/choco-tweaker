package com.chocotweak.compat;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.chocolate.chocolateQuest.ChocolateQuest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Baubles 兼容性工具 (直接 API)
 * 检查玩家 Baubles 槽位中的特殊物品
 */
public class BaublesCompat {

    /**
     * 检查玩家 Baubles 槽位是否有指定物品
     */
    public static boolean hasBaubleItem(EntityPlayer player, Item item) {
        try {
            IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
            if (handler == null) {
                return false;
            }

            int slots = handler.getSlots();
            for (int i = 0; i < slots; i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() == item) {
                    return true;
                }
            }
        } catch (Exception e) {
            // Baubles 未加载或其他错误
        }

        return false;
    }

    /**
     * 检查 Baubles 槽位是否有云靴
     */
    public static boolean hasCloudBoots(EntityPlayer player) {
        return hasBaubleItem(player, ChocolateQuest.cloudBoots);
    }

    /**
     * 检查 Baubles 槽位是否有龙头盔
     */
    public static boolean hasDragonHelmet(EntityPlayer player) {
        return hasBaubleItem(player, ChocolateQuest.dragonHelmet);
    }

    /**
     * 检查 Baubles 槽位是否有侦察器
     */
    public static boolean hasScouter(EntityPlayer player) {
        return hasBaubleItem(player, ChocolateQuest.scouter);
    }

    /**
     * 检查 Baubles 槽位是否有女巫帽
     */
    public static boolean hasWitchHat(EntityPlayer player) {
        return hasBaubleItem(player, ChocolateQuest.witchHat);
    }

    /**
     * 检查 Baubles 槽位是否有背包
     */
    public static boolean hasBackpack(EntityPlayer player) {
        return hasBaubleItem(player, ChocolateQuest.backpack);
    }
}
