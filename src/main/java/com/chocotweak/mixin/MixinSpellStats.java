package com.chocotweak.mixin;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 法术：施法速度砍半（冷却翻倍），伤害翻倍
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.magic.SpellBase", remap = false)
public class MixinSpellStats {

    /**
     * 施法时间翻倍（施法速度砍半）
     */
    @Inject(method = "getCastingTime", at = @At("RETURN"), cancellable = true)
    private void doubleCastingTime(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValue() * 2);
    }

    /**
     * 冷却翻倍（施法速度砍半的一部分）
     */
    @Inject(method = "getCoolDown", at = @At("RETURN"), cancellable = true)
    private void doubleCooldown(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValue() * 2);
    }

    /**
     * 伤害翻倍
     */
    @Inject(method = "getDamage", at = @At("RETURN"), cancellable = true)
    private void doubleDamage(ItemStack itemStack, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValue() * 2.0f);
    }
}


