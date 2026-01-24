package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.magic.Awakements;
import com.chocolate.chocolateQuest.magic.SpellBase;
import com.chocotweak.ChocoTweak;
import com.chocotweak.core.AwakementsInitializer;
import com.chocotweak.magic.AwakementEchoingVoice;
import com.chocotweak.magic.AwakementInstantCast;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

/**
 * 法杖觉醒效果 Mixin
 * 
 * - 瞬发咏唱 (instantCast): 减少冷却/施法时间
 * - 回响之音 (echoingVoice): 法术重放几率 - 触发时下一次法术冷却大幅降低
 * - 高速神言 (highSpeedChant): 施法期间持续降低冷却
 * 
 * 这些效果对玩家和 NPC 都生效
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.items.ItemStaffBase", remap = false)
public class MixinItemStaffBaseAwakenings {

    // CoolDownTracker 的反射缓存
    private static Field cooldownsField = null;
    private static boolean cooldownsFieldSearched = false;
    private static boolean debugLogged = false;

    @Shadow
    Object cachedTracker;

    /**
     * 瞬发咏唱 - 减少冷却时间
     * 在 getStaffCastSpeed 返回值上应用乘数
     */
    @Inject(method = "getStaffCastSpeed", at = @At("RETURN"), cancellable = true)
    private static void applyInstantCast(ItemStack is, CallbackInfoReturnable<Float> cir) {
        if (AwakementsInitializer.instantCast != null && is != null) {
            int level = Awakements.getEnchantLevel(is, (Awakements) AwakementsInitializer.instantCast);
            if (level > 0) {
                float multiplier = AwakementInstantCast.getCooldownMultiplier(level);
                cir.setReturnValue(cir.getReturnValue() * multiplier);
                ChocoTweak.LOGGER.info("[InstantCast] Applied multiplier {} for level {}", multiplier, level);
            }
        }
    }

    /**
     * 施法后应用觉醒效果
     * 
     * - 瞬发咏唱：降低当前法术的冷却时间
     * - 回响之音：触发时立即重置当前法术冷却（免费重放）
     * - 高速神言：降低冷却时间加成
     */
    @Inject(method = "mobCastSpell", at = @At("RETURN"), cancellable = true)
    private void applyAwakeningEffects(SpellBase spell, int i, EntityLivingBase entity, ItemStack is,
            CallbackInfoReturnable<Integer> cir) {

        // 首次调用时记录日志
        if (!debugLogged) {
            debugLogged = true;
            ChocoTweak.LOGGER.info("[AwakeningsDebug] mobCastSpell mixin called! cachedTracker={}", cachedTracker);
        }

        if (is == null) {
            ChocoTweak.LOGGER.debug("[AwakeningsDebug] ItemStack is null, skipping");
            return;
        }

        if (cachedTracker == null) {
            ChocoTweak.LOGGER.debug("[AwakeningsDebug] cachedTracker is null, skipping");
            return;
        }

        // 获取当前法术的冷却时间数组
        int[] cooldowns = getCooldownsArray(cachedTracker);
        if (cooldowns == null) {
            ChocoTweak.LOGGER.warn("[AwakeningsDebug] Failed to get cooldowns array from {}",
                    cachedTracker.getClass().getName());
            return;
        }

        if (i < 0 || i >= cooldowns.length) {
            ChocoTweak.LOGGER.debug("[AwakeningsDebug] Spell index {} out of bounds (length={})", i, cooldowns.length);
            return;
        }

        int originalCooldown = cooldowns[i];
        float cooldownReduction = 1.0f;

        // 瞬发咏唱 - 减少冷却时间
        if (AwakementsInitializer.instantCast != null) {
            int instantLevel = Awakements.getEnchantLevel(is, (Awakements) AwakementsInitializer.instantCast);
            if (instantLevel > 0) {
                cooldownReduction *= AwakementInstantCast.getCooldownMultiplier(instantLevel);
                ChocoTweak.LOGGER.info("[InstantCast] NPC cast spell, applying cooldown reduction: {} -> {} (level {})",
                        originalCooldown, (int) (originalCooldown * cooldownReduction), instantLevel);
            }
        }

        // 高速神言 - 额外减少冷却
        if (AwakementsInitializer.highSpeedChant != null) {
            int highSpeedLevel = Awakements.getEnchantLevel(is, (Awakements) AwakementsInitializer.highSpeedChant);
            if (highSpeedLevel > 0) {
                // 高速神言每级减少20%冷却
                float highSpeedReduction = 1.0f - (highSpeedLevel * 0.20f);
                highSpeedReduction = Math.max(0.2f, highSpeedReduction); // 最多减少80%
                cooldownReduction *= highSpeedReduction;
                ChocoTweak.LOGGER.info(
                        "[HighSpeedChant] NPC cast spell, applying cooldown reduction: {} -> {} (level {})",
                        originalCooldown, (int) (originalCooldown * cooldownReduction), highSpeedLevel);
            }
        }

        // 应用冷却减少
        if (cooldownReduction < 1.0f) {
            int newCooldown = Math.max(1, (int) (originalCooldown * cooldownReduction));
            cooldowns[i] = newCooldown;
            ChocoTweak.LOGGER.info("[Awakenings] Reduced cooldown from {} to {}", originalCooldown, newCooldown);
        }

        // 回响之音 - 触发时重置冷却（免费重放）
        if (AwakementsInitializer.echoingVoice != null) {
            int echoLevel = Awakements.getEnchantLevel(is, (Awakements) AwakementsInitializer.echoingVoice);
            if (echoLevel > 0 && AwakementEchoingVoice.shouldEcho(echoLevel)) {
                // 触发回响：将冷却设为 0，让 NPC 可以立即再次施法
                cooldowns[i] = 0;
                // 同时减少返回的施法时间
                int originalTime = cir.getReturnValue();
                cir.setReturnValue(Math.max(1, originalTime / 3));
                ChocoTweak.LOGGER.info("[EchoingVoice] Echo triggered! Cooldown reset to 0, cast time: {} -> {}",
                        originalTime, cir.getReturnValue());
            }
        }
    }

    /**
     * 通过反射获取 CoolDownTracker 的 cooldowns 数组
     */
    private static int[] getCooldownsArray(Object tracker) {
        if (tracker == null) {
            return null;
        }

        if (!cooldownsFieldSearched) {
            cooldownsFieldSearched = true;
            try {
                Class<?> trackerClass = tracker.getClass();
                ChocoTweak.LOGGER.info("[AwakeningsDebug] Looking for cooldowns field in class: {}",
                        trackerClass.getName());
                cooldownsField = trackerClass.getDeclaredField("cooldowns");
                cooldownsField.setAccessible(true);
                ChocoTweak.LOGGER.info("[AwakeningsDebug] Found cooldowns field: {}", cooldownsField);
            } catch (NoSuchFieldException e) {
                ChocoTweak.LOGGER.error("[AwakeningsDebug] Could not find cooldowns field: {}", e.getMessage());
                // 尝试打印所有字段
                for (Field f : tracker.getClass().getDeclaredFields()) {
                    ChocoTweak.LOGGER.info("[AwakeningsDebug] Available field: {} ({})", f.getName(),
                            f.getType().getName());
                }
            }
        }

        if (cooldownsField != null) {
            try {
                return (int[]) cooldownsField.get(tracker);
            } catch (IllegalAccessException e) {
                ChocoTweak.LOGGER.error("[AwakeningsDebug] Could not access cooldowns field: {}", e.getMessage());
            }
        }

        return null;
    }
}
