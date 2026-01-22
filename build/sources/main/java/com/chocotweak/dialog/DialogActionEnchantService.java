package com.chocotweak.dialog;

import com.chocotweak.ChocoTweak;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

/**
 * 附魔服务对话动作
 * NPC 为玩家物品提供附魔服务
 */
public class DialogActionEnchantService {

    /**
     * 为物品添加附魔
     * 
     * @param player        玩家
     * @param stack         要附魔的物品
     * @param enchantmentId 附魔ID (如 "chocotweak:dragon_slayer")
     * @param level         附魔等级
     * @return 是否成功
     */
    public static boolean applyEnchantment(EntityPlayer player, ItemStack stack, String enchantmentId, int level) {
        if (stack.isEmpty()) {
            return false;
        }

        try {
            Enchantment enchantment = Enchantment.REGISTRY.getObject(new ResourceLocation(enchantmentId));

            if (enchantment == null) {
                ChocoTweak.LOGGER.error("Enchantment not found: {}", enchantmentId);
                return false;
            }

            // 检查是否可以应用此附魔
            if (!enchantment.canApply(stack)) {
                ChocoTweak.LOGGER.warn("Cannot apply enchantment {} to item {}", enchantmentId, stack.getDisplayName());
                return false;
            }

            // 检查是否与现有附魔冲突
            Map<Enchantment, Integer> existingEnchants = EnchantmentHelper.getEnchantments(stack);
            for (Enchantment existing : existingEnchants.keySet()) {
                if (!enchantment.isCompatibleWith(existing) && enchantment != existing) {
                    ChocoTweak.LOGGER.warn("Enchantment {} conflicts with existing enchantment {}",
                            enchantmentId, existing.getRegistryName());
                    return false;
                }
            }

            // 应用附魔
            stack.addEnchantment(enchantment, level);

            ChocoTweak.LOGGER.info("Applied enchantment {} level {} to {} for player {}",
                    enchantmentId, level, stack.getDisplayName(), player.getName());
            return true;

        } catch (Exception e) {
            ChocoTweak.LOGGER.error("Error applying enchantment: " + enchantmentId, e);
            return false;
        }
    }

    /**
     * 获取附魔的本地化名称
     */
    public static String getEnchantmentDisplayName(String enchantmentId) {
        Enchantment enchantment = Enchantment.REGISTRY.getObject(new ResourceLocation(enchantmentId));
        if (enchantment != null) {
            return enchantment.getTranslatedName(1);
        }
        return enchantmentId;
    }

    /**
     * 检查物品是否可以接受指定附魔
     */
    public static boolean canApply(ItemStack stack, String enchantmentId) {
        if (stack.isEmpty())
            return false;

        Enchantment enchantment = Enchantment.REGISTRY.getObject(new ResourceLocation(enchantmentId));
        return enchantment != null && enchantment.canApply(stack);
    }
}
