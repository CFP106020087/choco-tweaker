package com.chocotweak.mixin;

import com.chocotweak.util.DialogActionTranslator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;

/**
 * Mixin to fix getIDByName to handle translated names safely
 * 安全地修复 getIDByName 以处理翻译后的名称
 *
 * 注意: actions 字段类型是 DialogActionList[]，使用反射访问避免类型问题
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.quest.DialogAction", remap = false)
public class MixinDialogAction {

    @Unique
    private static Field chocotweak$actionsField;

    @Unique
    private static boolean chocotweak$fieldInitialized = false;

    /**
     * 使用反射获取 actions 字段
     */
    @Unique
    private static Object[] chocotweak$getActions() {
        if (!chocotweak$fieldInitialized) {
            chocotweak$fieldInitialized = true;
            try {
                Class<?> dialogActionClass = Class.forName("com.chocolate.chocolateQuest.quest.DialogAction");
                chocotweak$actionsField = dialogActionClass.getField("actions");
                chocotweak$actionsField.setAccessible(true);
            } catch (Exception e) {
                System.err.println("[ChocoTweak] Failed to get actions field: " + e.getMessage());
            }
        }
        if (chocotweak$actionsField != null) {
            try {
                return (Object[]) chocotweak$actionsField.get(null);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 通过反射获取 DialogActionList 的 name 字段
     */
    @Unique
    private static String chocotweak$getActionName(Object action) {
        try {
            Field nameField = action.getClass().getField("name");
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
        if (name == null) {
            return 0;
        }

        Object[] actions = chocotweak$getActions();
        if (actions == null) {
            return 0;
        }

        try {
            // 1. 先直接匹配原始名称（快速路径）
            for (int i = 0; i < actions.length; i++) {
                if (actions[i] == null)
                    continue;

                // 使用反射获取 name 字段
                String actionName = chocotweak$getActionName(actions[i]);
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

                    String actionName = chocotweak$getActionName(actions[i]);
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
