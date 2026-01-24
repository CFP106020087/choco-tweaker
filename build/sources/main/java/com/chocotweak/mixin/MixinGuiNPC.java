package com.chocotweak.mixin;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Mixin to translate GuiNPC main menu buttons
 * 使用 @Inject 在 initGui 结束后修改按钮文本
 * 通过继承 GuiScreen 来访问 buttonList 字段
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.gui.guinpc.GuiNPC", remap = false)
public abstract class MixinGuiNPC extends GuiScreen {

        /**
         * 在 initGui 结束后修改按钮文本
         */
        @Inject(method = "initGui", at = @At("RETURN"))
        private void onInitGuiReturn(CallbackInfo ci) {
            List<GuiButton> buttons = this.buttonList;

        if (buttons == null)
                return;

    for (Object obj : buttons) {
            if (obj instanceof GuiButton) {
                    GuiButton button = (GuiButton) obj;
                    String original = button.displayString;

                // 直接替换按钮文本
                if ("Edit dialogs".equals(original)) {
                        button.displayString = I18n.format("chocotweak.npc.edit_dialogs");
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
