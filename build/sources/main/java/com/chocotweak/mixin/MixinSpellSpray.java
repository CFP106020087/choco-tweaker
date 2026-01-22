package com.chocotweak.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Spray法术增强
 * - 伤害: 0.2x → 1.5x (在onShoot中处理)
 * - 冷却: 100 → 30
 * - 消耗: 45 → 15
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.magic.SpellSpray", remap = false)
public class MixinSpellSpray {

    @Inject(method = "getCoolDown", at = @At("RETURN"), cancellable = true)
    private void reduceCooldown(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(30); // 原100
    }

    @Inject(method = "getCastingTime", at = @At("RETURN"), cancellable = true)
    private void reduceCastTime(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(15); // 原45
    }
}
