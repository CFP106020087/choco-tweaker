package com.chocotweak.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Beam法术增强
 * - 射程: 6-8 → 20+等级*2
 * - 伤害: 在MixinSpellStats中已全局增强
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.magic.SpellBeam", remap = false)
public class MixinSpellBeam {

    /**
     * 增强射程: 原本6+等级 → 20+等级*2
     */
    @Inject(method = "getMaxRange", at = @At("RETURN"), cancellable = true)
    private void enhanceRange(CallbackInfoReturnable<Integer> cir) {
        // 原始: 6 + level
        // 增强: 20 + level * 2 (最低20格，满级可达30格)
        int original = cir.getReturnValue();
        int level = original - 6; // 反推等级
        int enhanced = 20 + level * 2;
        cir.setReturnValue(enhanced);
    }

    /**
     * 减少冷却: 80 → 20
     */
    @Inject(method = "getCoolDown", at = @At("RETURN"), cancellable = true)
    private void reduceCooldown(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(20);
    }
}
