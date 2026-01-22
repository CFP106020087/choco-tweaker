package com.chocotweak.api;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

/**
 * 实现此接口的物品可以在格挡时左键攻击
 */
public interface IShieldAttackItem {

    /**
     * 服务端左键攻击回调
     * 
     * @param stack  物品
     * @param player 玩家
     */
    void onShieldAttack(ItemStack stack, EntityPlayerMP player);
}
