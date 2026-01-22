package com.chocotweak.core;

import com.chocolate.chocolateQuest.magic.Awakements;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;

/**
 * 由ASM注入到ContainerAwakement.enchantItem方法开头
 * 处理自定义觉醒的ID查找问题
 */
public class ContainerAwakementHelper {

    /** 静态holder，由MixinGuiAwakementAction设置按钮ID */
    public static int pendingAwakementId = -1;

    /**
     * 检查并处理觉醒模式
     * 
     * @param containerObj ContainerAwakement实例
     * @param enchantment 传入的附魔（觉醒模式下可能为null）
     * @return true表示已处理完成，应该return；false表示继续执行原方法
     */
    public static boolean handleEnchantItem(Object containerObj, Enchantment enchantment) {
        try {
            // 获取mode字段
            Field modeField = containerObj.getClass().getField("mode");
            boolean mode = modeField.getBoolean(containerObj);

            // 如果是附魔模式，让原方法处理
            if (mode) {
                return false;
            }

            // 觉醒模式 - 检查是否需要我们处理
            int awakementId = pendingAwakementId;
            pendingAwakementId = -1; // 重置

            if (awakementId < 0 && enchantment != null) {
                // 没有设置pendingId，尝试从enchantment获取
                awakementId = Enchantment.getEnchantmentID(enchantment);
            }

            // 如果ID有效（>= 0），让原方法尝试处理
            // 只有在ID为-1时（null enchantment）才需要我们介入
            if (awakementId >= 0 && awakementId < Awakements.awekements.length) {
                // ID在原始数组范围内，让原方法处理
                return false;
            }

            // ID超出原始范围或为-1，需要我们处理
            if (awakementId < 0) {
                System.err.println("[ChocoTweak] No valid awakement ID");
                return true; // 返回，不执行原方法
            }

            System.out.println("[ChocoTweak] Handling custom awakement ID: " + awakementId);

            // 在awekements数组中按ID查找觉醒
            Awakements targetAwakement = null;
            for (Awakements aw : Awakements.awekements) {
                if (aw != null && aw.id == awakementId) {
                    targetAwakement = aw;
                    break;
                }
            }

            if (targetAwakement == null) {
                System.err.println("[ChocoTweak] Could not find awakement with ID: " + awakementId);
                return true;
            }

            // 获取必要的字段
            Field inventoryField = containerObj.getClass().getDeclaredField("inventory");
            inventoryField.setAccessible(true);
            IInventory inventory = (IInventory) inventoryField.get(containerObj);

            Field playerField = containerObj.getClass().getSuperclass().getDeclaredField("player");
            playerField.setAccessible(true);
            EntityPlayer player = (EntityPlayer) playerField.get(containerObj);

            // 获取物品
            ItemStack is = inventory.getStackInSlot(0);

            // 计算经验消耗
            int lvl = Awakements.getEnchantLevel(is, targetAwakement);
            
            // 调用getXPRequiredToEnchantItem和getXPRequiredForEnchantment
            java.lang.reflect.Method getXPMethod = containerObj.getClass().getMethod("getXPRequiredToEnchantItem");
            int expRequired = (Integer) getXPMethod.invoke(containerObj);
            
            java.lang.reflect.Method getXPForEnchMethod = containerObj.getClass().getMethod(
                "getXPRequiredForEnchantment", Enchantment.class, int.class);
            expRequired += (Integer) getXPForEnchMethod.invoke(containerObj, null, lvl);
            
            java.lang.reflect.Method getCatalystMethod = containerObj.getClass().getMethod("getCatalystRebate", int.class);
            expRequired -= (Integer) getCatalystMethod.invoke(containerObj, expRequired);

            // 扣除经验
            if (!player.capabilities.isCreativeMode) {
                player.experienceLevel -= expRequired;
            }

            // 清空催化剂槽
            inventory.setInventorySlotContents(1, ItemStack.EMPTY);

            // 添加觉醒
            Awakements.addEnchant(is, targetAwakement, lvl + 1);

            System.out.println("[ChocoTweak] Applied awakement: " + targetAwakement.getName() + 
                " level " + (lvl + 1));

            return true; // 已处理，不执行原方法

        } catch (Exception e) {
            System.err.println("[ChocoTweak] Error in handleEnchantItem: " + e.getMessage());
            e.printStackTrace();
            return false; // 出错时让原方法尝试处理
        }
    }
}
