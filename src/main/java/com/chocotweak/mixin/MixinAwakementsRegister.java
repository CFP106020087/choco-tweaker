package com.chocotweak.mixin;

import com.chocotweak.core.AwakementsInitializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 通过Mixin注册自定义觉醒到CQ系统
 *
 * 关键：在Awakements类静态初始化完成后立即添加新觉醒
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.magic.Awakements", remap = false)
public class MixinAwakementsRegister {

    /**
     * 注入到静态初始化块结束时 - 这确保在awekements数组初始化后立即执行
     */
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onClinit(CallbackInfo ci) {
        AwakementsInitializer.initCustomAwakenings();
    }
}
