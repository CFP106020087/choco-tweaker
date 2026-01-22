package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.magic.Awakements;
import com.chocotweak.magic.*;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 应用法术觉醒效果到SpellBase
 * 
 * - 咒文增幅: 提升伤害 (getDamage)
 * - 魔力洪流: 降低消耗 (getCost)
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.magic.SpellBase", remap = false)
public class MixinSpellAwakeningEffects {

    /**
     * 咒文增幅 - 提升伤害
     * getDamage(ItemStack itemStack) -> float
     */
    @Inject(method = "getDamage", at = @At("RETURN"), cancellable = true)
    private void applySpellAmplification(ItemStack itemStack, CallbackInfoReturnable<Float> cir) {
        if (MixinAwakementsRegister.spellAmplification != null) {
            int level = Awakements.getEnchantLevel(itemStack, MixinAwakementsRegister.spellAmplification);
            if (level > 0) {
                float multiplier = AwakementSpellAmplification.getDamageMultiplier(level);
                cir.setReturnValue(cir.getReturnValue() * multiplier);
            }
        }
    }

    /**
     * 魔力洪流 - 降低消耗
     * getCost(ItemStack itemstack) -> float
     */
    @Inject(method = "getCost", at = @At("RETURN"), cancellable = true)
    private void applyManaSurge(ItemStack itemstack, CallbackInfoReturnable<Float> cir) {
        if (MixinAwakementsRegister.manaSurge != null) {
            int level = Awakements.getEnchantLevel(itemstack, MixinAwakementsRegister.manaSurge);
            if (level > 0) {
                float multiplier = AwakementManaSurge.getCostMultiplier(level);
                cir.setReturnValue(cir.getReturnValue() * multiplier);
            }
        }
    }

    /**
     * 远距咏唱 - 增加射程
     * getRange(ItemStack itemstack) -> int
     */
    @Inject(method = "getRange", at = @At("RETURN"), cancellable = true)
    private void applyLongRange(ItemStack itemstack, CallbackInfoReturnable<Integer> cir) {
        if (MixinAwakementsRegister.longRange != null) {
            int level = Awakements.getEnchantLevel(itemstack, MixinAwakementsRegister.longRange);
            if (level > 0) {
                float multiplier = AwakementLongRange.getRangeMultiplier(level);
                cir.setReturnValue((int) (cir.getReturnValue() * multiplier));
            }
        }
    }

    /**
     * 瞬发咏唱 - 减少冷却
     * getCoolDown() -> int (无ItemStack参数，需要在ItemStaffBase处理)
     * getCastingTime() -> int (同上)
     * 
     * 注意：这两个方法没有ItemStack参数，无法直接获取觉醒等级
     * 需要在 MixinItemStaffBase 中处理
     */
}
