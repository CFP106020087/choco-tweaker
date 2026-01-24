package com.chocotweak.mixin;

import com.chocotweak.util.DialogActionTranslator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Mixin to translate DialogActionList names
 * 在 toString() 中直接翻译动作名称，确保所有使用都能显示翻译后的名称
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.quest.DialogActionList", remap = false)
public class MixinDialogActionList {

    @Shadow
    @Final
    public String name;

    /**
     * 重写 toString 方法，使其返回翻译后的名称
     * 
     * @author ChocoTweak
     * @reason Translate dialog action names to current language
     */
    @Overwrite
    public String toString() {
        return DialogActionTranslator.translateName(this.name);
    }
}
