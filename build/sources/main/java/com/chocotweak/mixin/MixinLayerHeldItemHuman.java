package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.entity.EntityHumanBase;
import com.chocotweak.bedrock.entity.IEasterEggCapable;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to suppress held item rendering for Easter Egg NPCs when sitting or sleeping.
 * This prevents ghost equipment from appearing when the NPC is in these states.
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.client.rendererHuman.LayerHeldItemHuman", remap = false)
public class MixinLayerHeldItemHuman {

    /**
     * Cancel held item rendering for Easter Egg NPCs when sitting or sleeping.
     */
    @Inject(method = "doRenderLayer", at = @At("HEAD"), cancellable = true)
    private void chocotweak$suppressEasterEggEquipment(EntityLivingBase entity,
            float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch, float scale,
            CallbackInfo ci) {
        
        if (!(entity instanceof EntityHumanBase)) {
            return;
        }
        
        EntityHumanBase human = (EntityHumanBase) entity;
        
        // Check if this is an Easter Egg NPC
        if (entity instanceof IEasterEggCapable) {
            IEasterEggCapable easterEgg = (IEasterEggCapable) entity;
            if (easterEgg.isEasterEggNpc()) {
                // Suppress equipment rendering when sitting or sleeping
                if (human.isSitting() || human.isSleeping()) {
                    ci.cancel();
                    return;
                }
            }
        }
    }
}
