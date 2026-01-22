package com.chocotweak.mixin;

import com.chocotweak.ChocoTweak;
import com.chocotweak.dialog.DialogActionEnchantItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to ensure selected enchantments appear in the Awakement GUI
 * 确保选择的附魔出现在 Awakement GUI 中
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.gui.guinpc.GuiAwakement", remap = false)
public abstract class MixinGuiAwakement {

    /**
     * 在 updateButtonsEnchantment 开始时记录调试信息
     */
    @Inject(method = "updateButtonsEnchantment", at = @At("HEAD"))
    private void onUpdateButtonsEnchantmentHead(ItemStack is, CallbackInfo ci) {
        // 检查是否有选择的附魔
        Enchantment selectedEnchant = DialogActionEnchantItem.SelectedEnchantmentHolder.getSelectedEnchantment();
        if (selectedEnchant != null) {
            ChocoTweak.LOGGER.info("[ChocoTweak] Selected enchantment for GUI: {}",
                    selectedEnchant.getRegistryName());
        }

        // 记录调试信息
        int total = 0;
        int applicable = 0;
        int chocoTweakFound = 0;

        for (Enchantment enchant : Enchantment.REGISTRY) {
            if (enchant == null)
                continue;
            total++;

            boolean canApply = enchant.canApply(is);
            int currentLevel = EnchantmentHelper.getEnchantmentLevel(enchant, is);
            int maxLevel = enchant.getMaxLevel();

            // Log ChocoTweak enchantments specifically
            if (enchant.getRegistryName() != null
                    && enchant.getRegistryName().getNamespace().equals("chocotweak")) {
                chocoTweakFound++;
                ChocoTweak.LOGGER.info("[ChocoTweak]   {} - canApply={}, level={}/{}",
                        enchant.getRegistryName(), canApply, currentLevel, maxLevel);
            }

            if (canApply && currentLevel < maxLevel) {
                applicable++;
            }
        }

        ChocoTweak.LOGGER.info("[ChocoTweak] Total: {}, Applicable: {}, ChocoTweak: {}",
                total, applicable, chocoTweakFound);
    }
}


