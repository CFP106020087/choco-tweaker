package com.chocotweak.mixin;

import com.chocotweak.core.ContainerAwakementHelper;
import net.minecraft.client.gui.GuiButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

/**
 * 捕获GuiAwakement中的按钮点击，在调用enchantItem之前设置pendingAwakementId
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.gui.guinpc.GuiAwakement", remap = false)
public abstract class MixinGuiAwakementAction {

    @Shadow
    public boolean mode;

    /**
     * 在actionPerformed开始时，如果是觉醒模式，保存按钮ID到holder
     */
    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void onActionPerformedHead(GuiButton button, CallbackInfo ci) throws IOException {
        // 只处理觉醒按钮 (id < 256)
        if (button.id < 256 && !this.mode) {
            // 觉醒模式 - 保存按钮ID（就是觉醒ID）到helper
            ContainerAwakementHelper.pendingAwakementId = button.id;
            System.out.println("[ChocoTweak] Set pendingAwakementId = " + button.id);
        }
    }
}
