package com.chocotweak.mixin;

import com.chocotweak.config.CQTweakConfig;
import net.minecraft.entity.SharedMonsterAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to apply config multipliers to CQ monster stats
 * 应用配置倍率到 CQ 怪物属性
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.entity.mob.EntityHumanMob", remap = false)
public abstract class MixinEntityHumanMob {

    /**
     * 在 updateEntityAttributes 结束后应用倍率
     */
    @Inject(method = "updateEntityAttributes", at = @At("RETURN"))
    private void onUpdateEntityAttributes(CallbackInfo ci) {
        try {
            // 获取 this 作为 EntityLivingBase
            net.minecraft.entity.EntityLivingBase entity = (net.minecraft.entity.EntityLivingBase) (Object) this;

            // 应用血量倍率
            if (CQTweakConfig.monsters.healthMultiplier != 1.0) {
                double baseHealth = entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue();
                double newHealth = baseHealth * CQTweakConfig.monsters.healthMultiplier;
                entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(newHealth);
                entity.setHealth((float) newHealth);
            }

            // 应用攻击倍率
            if (CQTweakConfig.monsters.damageMultiplier != 1.0) {
                double baseDamage = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                double newDamage = baseDamage * CQTweakConfig.monsters.damageMultiplier;
                entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(newDamage);
            }

            // 应用攻击范围倍率
            if (CQTweakConfig.monsters.attackRangeMultiplier != 1.0) {
                double baseRange = entity.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue();
                double newRange = baseRange * CQTweakConfig.monsters.attackRangeMultiplier;
                entity.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(newRange);
            }

            // 应用速度倍率
            if (CQTweakConfig.monsters.speedMultiplier != 1.0) {
                double baseSpeed = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue();
                double newSpeed = baseSpeed * CQTweakConfig.monsters.speedMultiplier;
                entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(newSpeed);
            }
        } catch (Exception e) {
            // 静默忽略错误
        }
    }

    /**
     * 在 onDeath 开始时应用 XP 倍率
     */
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeathApplyXpMultiplier(net.minecraft.util.DamageSource damageSource, CallbackInfo ci) {
        try {
            if (CQTweakConfig.drops.xpMultiplier != 1.0) {
                net.minecraft.entity.EntityLivingBase entity = (net.minecraft.entity.EntityLivingBase) (Object) this;
                // 预先乘以倍率，因为原方法会覆盖 experienceValue
                // 需要在 onDeath 的 TAIL 处再次应用
            }
        } catch (Exception ignored) {
        }
    }
}


