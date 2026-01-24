package com.chocotweak.mixin;

import com.chocotweak.config.CQTweakConfig;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

/**
 * Mixin to apply XP multiplier to EntityHumanBase (parent of all CQ entities)
 * 应用经验值倍率到 CQ 实体
 *
 * 注意: experienceValue 字段在父类 EntityLiving 中，需要用反射访问
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.entity.EntityHumanBase", remap = false)
public abstract class MixinEntityHumanBaseXP {

    @Unique
    private static Field chocotweak$experienceValueField;

    @Unique
    private static boolean chocotweak$fieldInitialized = false;

    /**
     * 使用反射获取 experienceValue 字段（在 EntityLiving 父类中）
     */
    @Unique
    private static Field chocotweak$getExperienceValueField(Object entity) {
        if (!chocotweak$fieldInitialized) {
            chocotweak$fieldInitialized = true;
            try {
                // experienceValue 在 EntityLiving 中
                Class<?> clazz = entity.getClass();
                while (clazz != null && clazz != Object.class) {
                    try {
                        chocotweak$experienceValueField = clazz.getDeclaredField("experienceValue");
                        chocotweak$experienceValueField.setAccessible(true);
                        break;
                    } catch (NoSuchFieldException e) {
                        clazz = clazz.getSuperclass();
                    }
                }
            } catch (Exception e) {
                System.err.println("[ChocoTweak] Failed to get experienceValue field: " + e.getMessage());
            }
        }
        return chocotweak$experienceValueField;
    }

    /**
     * 在 onDeath 结束后应用 XP 倍率
     */
    @Inject(method = "onDeath", at = @At("TAIL"))
    private void chocotweak$onDeathApplyXpMultiplier(DamageSource damageSource, CallbackInfo ci) {
        try {
            if (CQTweakConfig.drops.xpMultiplier != 1.0) {
                Field field = chocotweak$getExperienceValueField(this);
                if (field != null) {
                    int currentXp = field.getInt(this);
                    int newXp = (int) (currentXp * CQTweakConfig.drops.xpMultiplier);
                    field.setInt(this, newXp);
                }
            }
        } catch (Exception ignored) {
        }
    }
}
