package com.chocotweak.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Mixin to DialogActionList - placeholder
 * 
 * 翻译逻辑已移至 DialogActionTranslator 工具类
 * 此Mixin仅作为占位符保留
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.quest.DialogActionList", remap = false)
public class MixinDialogActionList {

    @Shadow
    public String name;

    // 翻译方法已移至 com.chocotweak.util.DialogActionTranslator
}
