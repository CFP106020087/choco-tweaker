package com.chocotweak.dialog;

import com.chocotweak.ChocoTweak;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 注册自定义对话动作到 CQ 的 DialogAction.actions 数组
 * 使用反射避免直接类引用导致的早期类加载
 */
public class DialogActionRegistry {

    private static boolean registered = false;

    /**
     * 在 Mod 初始化时调用，注入自定义对话动作
     */
    public static void registerCustomActions() {
        if (registered) {
            return;
        }
        registered = true;

        try {
            // 使用反射获取 DialogAction 类
            Class<?> dialogActionClass = Class.forName("com.chocolate.chocolateQuest.quest.DialogAction");
            Class<?> dialogActionListClass = Class.forName("com.chocolate.chocolateQuest.quest.DialogActionList");

            // 获取 actions 字段
            Field actionsField = dialogActionClass.getDeclaredField("actions");
            actionsField.setAccessible(true);
            Object originalActions = actionsField.get(null);

            if (originalActions == null) {
                ChocoTweak.LOGGER.error("DialogAction.actions is null, cannot inject custom actions");
                return;
            }

            // 获取原数组长度
            int originalLength = Array.getLength(originalActions);

            // 创建新数组 (长度+2)
            Object newActions = Array.newInstance(dialogActionListClass, originalLength + 2);

            // 复制原有元素
            for (int i = 0; i < originalLength; i++) {
                Array.set(newActions, i, Array.get(originalActions, i));
            }

            // 创建新的 DialogActionList 实例并添加
            java.lang.reflect.Constructor<?> constructor = dialogActionListClass.getConstructor(Class.class,
                    String.class);

            Object buyCreatureAction = constructor.newInstance(DialogActionBuyCreature.class, "Buy tamed creature");
            Array.set(newActions, originalLength, buyCreatureAction);
            ChocoTweak.LOGGER.info("Added dialog action: Buy tamed creature");

            Object enchantItemAction = constructor.newInstance(DialogActionEnchantItem.class, "Enchant item");
            Array.set(newActions, originalLength + 1, enchantItemAction);
            ChocoTweak.LOGGER.info("Added dialog action: Enchant item");

            // 替换 actions 数组
            actionsField.set(null, newActions);

            ChocoTweak.LOGGER.info("Successfully injected {} custom dialog actions. Total: {}",
                    2, Array.getLength(newActions));

        } catch (ClassNotFoundException e) {
            ChocoTweak.LOGGER.warn("CQ DialogAction classes not found, skipping custom actions: {}", e.getMessage());
        } catch (NoClassDefFoundError e) {
            ChocoTweak.LOGGER.warn("CQ classes not available, skipping custom actions: {}", e.getMessage());
        } catch (Throwable t) {
            ChocoTweak.LOGGER.error("Failed to inject dialog actions", t);
        }
    }
}
