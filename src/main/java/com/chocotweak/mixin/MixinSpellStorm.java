package com.chocotweak.mixin;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Storm法术增强
 * - 冷却: 1000 → 150
 * - 消耗: 200 → 40
 * - 伤害: 0.66x → 在全局增强中处理
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.magic.SpellStorm", remap = false)
public class MixinSpellStorm {

    @Inject(method = "getCoolDown", at = @At("RETURN"), cancellable = true)
    private void reduceCooldown(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(150); // 原1000
    }

    @Inject(method = "getCost", at = @At("RETURN"), cancellable = true)
    private void reduceCost(ItemStack is, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(40.0f); // 原200
    }
}
