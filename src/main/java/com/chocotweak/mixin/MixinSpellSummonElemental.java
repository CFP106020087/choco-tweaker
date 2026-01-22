package com.chocotweak.mixin;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 召唤元素法术增强
 * - 冷却: 600 → 80
 * - 消耗: 50/100 → 20/30
 * - 召唤物属性在 MixinSummonedElemental 中增强
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.magic.SpellSummonElemental", remap = false)
public class MixinSpellSummonElemental {

    @Inject(method = "getCoolDown", at = @At("RETURN"), cancellable = true)
    private void reduceCooldown(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(80); // 原600
    }

    @Inject(method = "getCost", at = @At("RETURN"), cancellable = true)
    private void reduceCost(ItemStack is, CallbackInfoReturnable<Float> cir) {
        float original = cir.getReturnValue();
        // 50 → 20, 100 → 30
        cir.setReturnValue(original <= 50 ? 20.0f : 30.0f);
    }
}
