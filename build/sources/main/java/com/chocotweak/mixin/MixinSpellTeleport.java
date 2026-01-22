package com.chocotweak.mixin;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Teleport法术增强
 * - 冷却: 400 → 40
 * - 消耗: 100 → 15
 * - 射程: 2 → 32
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.magic.SpellTeleport", remap = false)
public class MixinSpellTeleport {

    @Inject(method = "getCoolDown", at = @At("RETURN"), cancellable = true)
    private void reduceCooldown(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(40); // 原400
    }

    @Inject(method = "getCost", at = @At("RETURN"), cancellable = true)
    private void reduceCost(ItemStack is, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(15.0f); // 原100
    }

    @Inject(method = "getMaxRange", at = @At("RETURN"), cancellable = true)
    private void enhanceRange(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(32); // 原2
    }
}
