package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.entity.npc.EntityHumanNPC;
import com.chocolate.chocolateQuest.gui.guinpc.GuiNPC;
import com.chocotweak.bedrock.entity.IEasterEggCapable;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to trigger Easter Egg NPC reaction animation after dialog selection.
 */
@Mixin(value = GuiNPC.class, remap = false)
public abstract class MixinGuiNPCReaction extends GuiScreen {

    @Shadow
    public EntityHumanNPC npc;

    /**
     * Trigger reaction animation after player selects a dialog option.
     */
    @Inject(method = "actionPerformed", at = @At("RETURN"))
    private void chocotweak$triggerReactionAfterDialog(GuiButton button, CallbackInfo ci) {
        if (npc == null) return;
        
        // Check if the NPC is an Easter Egg NPC
        if (npc instanceof IEasterEggCapable) {
            IEasterEggCapable easterEgg = (IEasterEggCapable) npc;
            if (easterEgg.isEasterEggNpc()) {
                // Trigger a random reaction animation
                easterEgg.triggerEasterEggReaction();
            }
        }
    }
}
