package com.chocotweak.mixin;

import com.chocotweak.util.DialogActionTranslator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to fix getIDByName to handle translated names
 * 使用 @Inject 代替 @Overwrite 避免类初始化问题
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.quest.DialogAction", remap = false)
public class MixinDialogAction {

    /**
     * 在 getIDByName 方法开始时注入
     * 如果输入是翻译后的名称，转换为原始名称后重新调用
     */
    @Inject(method = "getIDByName", at = @At("HEAD"), cancellable = true)
    private static void chocotweak$onGetIDByName(String name, CallbackInfoReturnable<Integer> cir) {
        if (name == null) {
            return;
        }

        // 防止递归
        if (DialogActionTranslator.isProcessing(name)) {
            return;
        }

        try {
            // 尝试获取原始名称
            String originalName = DialogActionTranslator.getOriginalName(name);

            // 如果找到了翻译映射，且与输入不同，则使用原始名称重新查找
            if (originalName != null && !originalName.equals(name)) {
                // 使用反射调用原始方法，传入原始名称
                Class<?> dialogActionClass = Class.forName("com.chocolate.chocolateQuest.quest.DialogAction");
                java.lang.reflect.Method method = dialogActionClass.getMethod("getIDByName", String.class);

                // 临时标记，防止递归
                DialogActionTranslator.markProcessing(name);
                try {
                    Integer result = (Integer) method.invoke(null, originalName);
                    cir.setReturnValue(result);
                } finally {
                    DialogActionTranslator.unmarkProcessing(name);
                }
            }
        } catch (Exception ignored) {
            // 出错时让原方法继续执行
        }
    }
}
