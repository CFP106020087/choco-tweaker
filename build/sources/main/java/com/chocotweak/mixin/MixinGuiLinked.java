package com.chocotweak.mixin;

import com.chocotweak.util.DialogActionTranslator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Mixin to translate dialog action names in GuiLinked
 * 在 GUI 渲染时翻译名称，避免在 toString() 中翻译导致的问题
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.gui.guinpc.GuiLinked", remap = false)
public class MixinGuiLinked {

    /**
     * 重写 getNames 方法，在获取名称时进行翻译
     * 
     * @author ChocoTweak
     * @reason Translate dialog action names to current language
     */
    @Overwrite
    public String[] getNames(Object[] array) {
        String[] dialogNames;
        if (array != null) {
            dialogNames = new String[array.length];

            for (int i = 0; i < dialogNames.length; ++i) {
                String originalName = array[i].toString();
                // 尝试翻译
                dialogNames[i] = DialogActionTranslator.translateName(originalName);
            }
        } else {
            dialogNames = new String[0];
        }

        return dialogNames;
    }
}
