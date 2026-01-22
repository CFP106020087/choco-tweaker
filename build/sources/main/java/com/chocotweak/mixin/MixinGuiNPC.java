package com.chocotweak.mixin;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Mixin to translate GuiNPC main menu buttons
 * 使用 @Inject 在 initGui 结束后修改按钮文本
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.gui.guinpc.GuiNPC", remap = false)
public abstract class MixinGuiNPC {

        // Shadow buttonList from GuiScreen via inheritance
        @Shadow
        protected List<GuiButton> buttonList;

        /**
         * 在 initGui 结束后修改按钮文本
         */
        @Inject(method = "initGui", at = @At("RETURN"))
        private void onInitGuiReturn(CallbackInfo ci) {
                System.out.println("[ChocoTweak] MixinGuiNPC.onInitGuiReturn() - buttonList: " +
                                (buttonList != null ? buttonList.size() : "null"));

                if (buttonList == null)
                        return;

                for (Object obj : buttonList) {
                        if (obj instanceof GuiButton) {
                                GuiButton button = (GuiButton) obj;
                                String original = button.displayString;
                                System.out.println("[ChocoTweak] Button found: '" + original + "'");

                                // 直接替换按钮文本
                                if ("Edit dialogs".equals(original)) {
                                        button.displayString = I18n.format("chocotweak.npc.edit_dialogs");
                                        System.out.println("[ChocoTweak] -> " + button.displayString);
                                } else if ("Edit stats".equals(original)) {
                                        button.displayString = I18n.format("chocotweak.npc.edit_stats");
                                } else if ("Inventory".equals(original)) {
                                        button.displayString = I18n.format("chocotweak.npc.inventory");
                                } else if ("Import/Export".equals(original)) {
                                        button.displayString = I18n.format("chocotweak.npc.import_export");
                                } else if ("Edit AI".equals(original)) {
                                        button.displayString = I18n.format("chocotweak.npc.edit_ai");
                                }
                        }
                }
        }
}


