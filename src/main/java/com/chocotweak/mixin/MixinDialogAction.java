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
 * 使用纯反射访问 actions 字段，避免 @Shadow 类型不匹配问题
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.quest.DialogAction", remap = false)
public class MixinDialogAction {

    @Unique
    private static Field chocotweak$actionsField;

    @Unique
    private static Field chocotweak$nameField;

    @Unique
    private static volatile boolean chocotweak$initialized = false;

    @Unique
    private static void chocotweak$ensureInit(Class<?> selfClass) {
        if (!chocotweak$initialized) {
            synchronized (MixinDialogAction.class) {
                if (!chocotweak$initialized) {
                    try {
                        chocotweak$actionsField = selfClass.getField("actions");
                    } catch (Exception ignored) {}
                    chocotweak$initialized = true;
                }
            }
        }
    }

    @Unique
    private static Object[] chocotweak$getActions(Class<?> selfClass) {
        chocotweak$ensureInit(selfClass);
        if (chocotweak$actionsField != null) {
            try {
                return (Object[]) chocotweak$actionsField.get(null);
            } catch (Exception ignored) {}
        }
        return null;
    }

    @Unique
    private static String chocotweak$getActionName(Object action) {
        if (action == null) return null;
        if (chocotweak$nameField == null) {
            try {
                chocotweak$nameField = action.getClass().getField("name");
            } catch (Exception ignored) {
                return null;
            }
        }
        try {
            return (String) chocotweak$nameField.get(action);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 重写 getIDByName 方法 - 安全版本
     *
     * @author ChocoTweak
     * @reason Fix action lookup for translated names
     */
    @Overwrite
    public static int getIDByName(String name) {
        if (name == null) {
            return 0;
        }

        try {
            // 获取当前类（被mixin合并后的DialogAction类）
            Class<?> selfClass = Class.forName("com.chocolate.chocolateQuest.quest.DialogAction", false,
                    Thread.currentThread().getContextClassLoader());

            Object[] actions = chocotweak$getActions(selfClass);
            if (actions == null) {
                return 0;
            }

            // 1. 先直接匹配原始名称（快速路径）
            for (int i = 0; i < actions.length; i++) {
                String actionName = chocotweak$getActionName(actions[i]);
                if (name.equals(actionName)) {
                    return i;
                }
            }

            // 2. 如果没有匹配，尝试从翻译映射获取原始名称
            String originalName = DialogActionTranslator.getOriginalName(name);
            if (originalName != null && !originalName.equals(name)) {
                for (int i = 0; i < actions.length; i++) {
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
