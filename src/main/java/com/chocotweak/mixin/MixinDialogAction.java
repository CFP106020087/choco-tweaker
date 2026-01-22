package com.chocotweak.mixin;

import com.chocotweak.util.DialogActionTranslator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Mixin to fix getIDByName to handle translated names safely
 * 安全地修复 getIDByName 以处理翻译后的名称
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.quest.DialogAction", remap = false)
public class MixinDialogAction {

    @Shadow
    public static Object[] actions; // 使用 Object[] 避免直接引用 DialogActionList

    /**
     * 通过反射获取 name 字段
     */
    private static String getActionName(Object action) {
        try {
            java.lang.reflect.Field nameField = action.getClass().getField("name");
            return (String) nameField.get(action);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 重写 getIDByName 方法 - 安全版本
     * 不调用 toString() 避免循环触发 mixin
     * 
     * @author ChocoTweak
     * @reason Fix action lookup for translated names
     */
    @Overwrite
    public static int getIDByName(String name) {
        if (name == null || actions == null) {
            return 0;
        }

        try {
            // 1. 先直接匹配原始名称（快速路径）
            for (int i = 0; i < actions.length; i++) {
                if (actions[i] == null)
                    continue;

                // 使用反射获取 name 字段
                String actionName = getActionName(actions[i]);
                if (name.equals(actionName)) {
                    return i;
                }
            }

            // 2. 如果没有匹配，尝试从翻译映射获取原始名称
            String originalName = DialogActionTranslator.getOriginalName(name);
            if (originalName != null && !originalName.equals(name)) {
                for (int i = 0; i < actions.length; i++) {
                    if (actions[i] == null)
                        continue;

                    String actionName = getActionName(actions[i]);
                    if (originalName.equals(actionName)) {
                        return i;
                    }
                }
            }
        } catch (Exception ignored) {
            // 出错时返回默认值
        }

        return 0;
    }
}
