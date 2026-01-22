package com.chocotweak.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Miner法术增强
 * - 挖掘范围: 1x1 → 5x5
 * - 冷却: 20 → 5
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.magic.SpellMiner", remap = false)
public class MixinSpellMiner {

    @Inject(method = "getCoolDown", at = @At("RETURN"), cancellable = true)
    private void reduceCooldown(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(5); // 原20
    }

    // 挖掘范围在onShoot中处理，需要特殊Mixin
}
