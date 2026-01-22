package com.chocotweak.mixin;

import com.chocotweak.config.CQTweakConfig;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to apply XP multiplier to EntityHumanBase (parent of all CQ entities)
 * 应用经验值倍率到 CQ 实体
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.entity.EntityHumanBase", remap = false)
public abstract class MixinEntityHumanBaseXP {

    @Shadow
    protected int experienceValue;

    /**
     * 在 onDeath 结束后应用 XP 倍率
     */
    @Inject(method = "onDeath", at = @At("TAIL"))
    private void onDeathApplyXpMultiplier(DamageSource damageSource, CallbackInfo ci) {
        try {
            if (CQTweakConfig.drops.xpMultiplier != 1.0) {
                this.experienceValue = (int) (this.experienceValue * CQTweakConfig.drops.xpMultiplier);
            }
        } catch (Exception ignored) {
        }
    }
}


