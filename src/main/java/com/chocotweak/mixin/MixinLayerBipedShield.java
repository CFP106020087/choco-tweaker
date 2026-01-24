package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.entity.EntityHumanBase;
import com.chocotweak.bedrock.entity.IEasterEggCapable;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to disable LayerBipedShield rendering for Easter Egg NPCs when sitting
 * or sleeping.
 * 
 * 彩蛋 NPC 使用 YSM 模型渲染本体，CQ 渲染装备（包括盾牌）。
 * 只在坐下/睡觉时禁用盾牌渲染（因为 YSM 动画位置与 CQ 不匹配）。
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.utils.LayerBipedShield", remap = false)
public class MixinLayerBipedShield {

    @Inject(method = "doRenderLayer", at = @At("HEAD"), cancellable = true)
    private void chocotweak$skipForEasterEgg(EntityLivingBase entity, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {

        if (entity instanceof IEasterEggCapable) {
            IEasterEggCapable easterEgg = (IEasterEggCapable) entity;
            if (easterEgg.isEasterEggNpc()) {
                // 只在坐下或睡觉时禁用盾牌渲染
                if (entity instanceof EntityHumanBase) {
                    EntityHumanBase human = (EntityHumanBase) entity;
                    if (human.isSitting() || human.isSleeping()) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}
