package com.chocotweak.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 剑盾自动格挡处理器
 * 
 * 功能：受到攻击时自动格挡
 * 注意：使用类名检查代替直接类引用，避免触发CQ类加载失败
 */
@Mod.EventBusSubscriber(modid = "chocotweak")
public class MonkingSwordShieldHandler {

    /**
     * 安全检测物品是否为 ItemSwordAndShieldBase 子类
     */
    private static boolean isSwordAndShield(Item item) {
        Class<?> clazz = item.getClass();
        while (clazz != null) {
            if (clazz.getName().equals("com.chocolate.chocolateQuest.items.swords.ItemSwordAndShieldBase")) {
                return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    /**
     * 受到攻击时自动格挡
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        ItemStack mainHand = player.getHeldItemMainhand();

        // 检查是否持有 CQ 剑盾
        if (isSwordAndShield(mainHand.getItem())) {
            // 如果没有在格挡，自动开始格挡
            if (!player.isActiveItemStackBlocking()) {
                player.setActiveHand(EnumHand.MAIN_HAND);
            }
        }
    }

    /**
     * 检查玩家是否持有剑盾并正在格挡
     */
    public static boolean isMonkingBlocking(EntityPlayer player) {
        ItemStack mainHand = player.getHeldItemMainhand();
        return isSwordAndShield(mainHand.getItem())
                && player.isActiveItemStackBlocking();
    }
}
