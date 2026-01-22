package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.items.gun.ItemGun;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 枪械伤害翻倍
 */
@Mixin(value = ItemGun.class, remap = false)
public class MixinGunDamage {

    @Inject(method = "getPower", at = @At("RETURN"), cancellable = true)
    private void doubleDamage(ItemStack is, CallbackInfoReturnable<Float> cir) {
        // 伤害翻倍
        cir.setReturnValue(cir.getReturnValue() * 2.0f);
    }
}
