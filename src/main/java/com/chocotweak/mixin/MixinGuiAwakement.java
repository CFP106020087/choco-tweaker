package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.magic.Awakements;
import com.chocotweak.ChocoTweak;
import com.chocotweak.dialog.DialogActionEnchantItem;
import com.chocotweak.dialog.DialogActionSelectAwakening;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

/**
 * Mixin to filter awakenings in GuiAwakement
 * - 当有选择的特定觉醒时，只显示该觉醒
 * - 否则显示所有可用觉醒
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.gui.guinpc.GuiAwakement", remap = false)
public abstract class MixinGuiAwakement {

    /**
     * 在 updateButtonsAwakement 开始时，如果有选择的特定觉醒，
     * 临时修改 Awakements.awekements 数组只包含该觉醒
     */
    @Inject(method = "updateButtonsAwakement", at = @At("HEAD"))
    private void onUpdateButtonsAwakementHead(ItemStack is, CallbackInfo ci) {
        // 检查是否有选择的觉醒
        if (DialogActionSelectAwakening.SelectedAwakeningHolder.hasSelection()) {
            Object selectedAwakening = DialogActionSelectAwakening.SelectedAwakeningHolder.getSelectedAwakening();
            String awakeName = DialogActionSelectAwakening.SelectedAwakeningHolder.getSelectedAwakeningName();

            if (selectedAwakening != null) {
                ChocoTweak.LOGGER.info("[ChocoTweak] Filtering awakenings to only show: {}", awakeName);

                try {
                    // 临时保存原始数组
                    Field awakementsField = Awakements.class.getField("awekements");
                    Awakements[] originalArray = (Awakements[]) awakementsField.get(null);

                    // 创建只包含选中觉醒的新数组
                    Awakements[] filteredArray = new Awakements[] { (Awakements) selectedAwakening };

                    // 替换数组
                    awakementsField.set(null, filteredArray);

                    // 存储原始数组供恢复使用
                    OriginalAwakementsHolder.setOriginalArray(originalArray);

                } catch (Exception e) {
                    ChocoTweak.LOGGER.error("[ChocoTweak] Failed to filter awakenings: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 在 updateButtonsAwakement 结束时恢复原始数组
     */
    @Inject(method = "updateButtonsAwakement", at = @At("RETURN"))
    private void onUpdateButtonsAwakementReturn(ItemStack is, CallbackInfo ci) {
        // 恢复原始数组
        Awakements[] original = OriginalAwakementsHolder.getAndClearOriginalArray();
        if (original != null) {
            try {
                Field awakementsField = Awakements.class.getField("awekements");
                awakementsField.set(null, original);

                // 清除选择（一次性使用）
                DialogActionSelectAwakening.SelectedAwakeningHolder.getAndClearSelectedAwakening();

                ChocoTweak.LOGGER.info("[ChocoTweak] Restored original awakenings array");
            } catch (Exception e) {
                ChocoTweak.LOGGER.error("[ChocoTweak] Failed to restore awakenings: {}", e.getMessage());
            }
        }
    }

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
    }

    /**
     * 保存原始觉醒数组的 holder
     */
    public static class OriginalAwakementsHolder {
        private static Awakements[] originalArray = null;

        public static void setOriginalArray(Awakements[] array) {
            originalArray = array;
        }

        public static Awakements[] getAndClearOriginalArray() {
            Awakements[] result = originalArray;
            originalArray = null;
            return result;
        }
    }
}
