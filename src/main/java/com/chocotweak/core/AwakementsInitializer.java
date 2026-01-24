package com.chocotweak.core;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * 由ASM在Awakements.<clinit>末尾调用
 * 注册所有自定义觉醒到CQ系统
 * 
 * 注意：本类不能直接 import CQ 的类，否则会触发早期类加载
 * 必须使用反射操作
 */
public class AwakementsInitializer {

    // 使用 Object 存储觉醒实例，避免类型引用触发加载
    public static Object highSpeedChant;
    public static Object spellAmplification;
    public static Object instantCast;
    public static Object manaSurge;
    public static Object longRange;
    public static Object echoingVoice;
    public static Object potionCapacity;
    public static Object potionInfusion;

    private static boolean initialized = false;

    /**
     * 由ASM在Awakements类静态初始化时调用
     */
    public static synchronized void initCustomAwakenings() {
        if (initialized) {
            return;
        }
        initialized = true;

        try {
            System.out.println("[ChocoTweak] initCustomAwakenings() called");

            // 通过反射获取 Awakements.awekements 数组
            Class<?> awakementsClass = Class.forName("com.chocolate.chocolateQuest.magic.Awakements");
            Field awakementsField = awakementsClass.getField("awekements");
            Object[] awekements = (Object[]) awakementsField.get(null);

            if (awekements == null) {
                System.err.println("[ChocoTweak] ERROR: Awakements.awekements is null!");
                return;
            }

            System.out.println("[ChocoTweak] Current awekements length: " + awekements.length);

            // 创建觉醒实例
            highSpeedChant = createAwakement("com.chocotweak.magic.AwakementHighSpeedChant", "highSpeedChant", 0);
            spellAmplification = createAwakement("com.chocotweak.magic.AwakementSpellAmplification",
                    "spellAmplification", 0);
            instantCast = createAwakement("com.chocotweak.magic.AwakementInstantCast", "instantCast", 0);
            manaSurge = createAwakement("com.chocotweak.magic.AwakementManaSurge", "manaSurge", 0);
            longRange = createAwakement("com.chocotweak.magic.AwakementLongRange", "longRange", 0);
            echoingVoice = createAwakement("com.chocotweak.magic.AwakementEchoingVoice", "echoingVoice", 0);
            potionCapacity = createAwakement("com.chocotweak.magic.AwakementPotionCapacity", "potionCapacity", 0);
            potionInfusion = createAwakement("com.chocotweak.magic.AwakementPotionInfusion", "potionInfusion", 0);

            // 扩展数组
            int oldLength = awekements.length;
            Object[] newArray = (Object[]) Array.newInstance(awakementsClass, oldLength + 8);
            System.arraycopy(awekements, 0, newArray, 0, oldLength);
            newArray[oldLength] = highSpeedChant;
            newArray[oldLength + 1] = spellAmplification;
            newArray[oldLength + 2] = instantCast;
            newArray[oldLength + 3] = manaSurge;
            newArray[oldLength + 4] = longRange;
            newArray[oldLength + 5] = echoingVoice;
            newArray[oldLength + 6] = potionCapacity;
            newArray[oldLength + 7] = potionInfusion;

            // 替换原数组
            awakementsField.set(null, newArray);

            System.out.println(
                    "[ChocoTweak] SUCCESS: Registered 8 custom awakenings! (new count: " + newArray.length + ")");

        } catch (Exception e) {
            System.err.println("[ChocoTweak] ERROR in initCustomAwakenings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Object createAwakement(String className, String name, int level) {
        try {
            Class<?> clazz = Class.forName(className);
            return clazz.getConstructor(String.class, int.class).newInstance(name, level);
        } catch (Exception e) {
            System.err.println("[ChocoTweak] Failed to create awakement: " + className + " - " + e.getMessage());
            return null;
        }
    }
}
