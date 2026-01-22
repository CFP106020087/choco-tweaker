package com.chocotweak.mixin;

import com.chocotweak.config.CQTweakConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to modify dungeon territory separation
 * 修改地牢间距
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.quest.worldManager.TerrainManager", remap = false)
public abstract class MixinTerrainManager {

    /**
     * 修改地牢间距
     */
    @Inject(method = "getTerritorySeparation", at = @At("RETURN"), cancellable = true)
    private static void onGetTerritorySeparation(CallbackInfoReturnable<Integer> cir) {
        try {
            // 如果配置值不同于原始值 10，则使用配置值
            if (CQTweakConfig.dungeons.minDistanceChunks != 10) {
                cir.setReturnValue(CQTweakConfig.dungeons.minDistanceChunks);
            }
        } catch (Exception ignored) {
        }
    }
}


