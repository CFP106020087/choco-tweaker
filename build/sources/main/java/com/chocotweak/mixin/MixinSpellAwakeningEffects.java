package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.items.ItemStaffBase;
import com.chocolate.chocolateQuest.magic.Awakements;
import com.chocotweak.core.AwakementsInitializer;
import com.chocotweak.magic.*;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
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
     * 使用 @Overwrite 完全替换方法
     * 
     * 同时应用：
     * - 法袍增伤 (x2 基础伤害) - 来自 MixinSpellStats
     * - 咒文增幅觉醒 (额外伤害乘数)
     * 
     * @author ChocoTweak
     * @reason 应用法袍增伤和咒文增幅觉醒效果
     */
    @Overwrite
    public float getDamage(ItemStack itemStack) {
        // 原始伤害计算
        float baseDamage = ItemStaffBase.getMagicDamage(itemStack);

        // 应用法袍增伤 (x10) - 来自 MixinSpellStats
        float damage = baseDamage * 10.0f;

        // 应用咒文增幅觉醒
        if (AwakementsInitializer.spellAmplification != null && itemStack != null) {
            int level = Awakements.getEnchantLevel(itemStack, (Awakements) AwakementsInitializer.spellAmplification);
            if (level > 0) {
                float multiplier = AwakementSpellAmplification.getDamageMultiplier(level);
                damage = damage * multiplier;
            }
        }

        return damage;
    }

    /**
     * 魔力洪流 - 降低消耗
     * getCost(ItemStack itemstack) -> float
     */
    @Inject(method = "getCost", at = @At("RETURN"), cancellable = true)
    private void applyManaSurge(ItemStack itemstack, CallbackInfoReturnable<Float> cir) {
        if (AwakementsInitializer.manaSurge != null && itemstack != null) {
            int level = Awakements.getEnchantLevel(itemstack, (Awakements) AwakementsInitializer.manaSurge);
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
        if (AwakementsInitializer.longRange != null && itemstack != null) {
            int level = Awakements.getEnchantLevel(itemstack, (Awakements) AwakementsInitializer.longRange);
            if (level > 0) {
                float multiplier = AwakementLongRange.getRangeMultiplier(level);
                cir.setReturnValue((int) (cir.getReturnValue() * multiplier));
            }
        }
    }
}
