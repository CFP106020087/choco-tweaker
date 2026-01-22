package com.chocotweak.core;

import com.chocolate.chocolateQuest.magic.Awakements;
import com.chocotweak.magic.*;

import java.util.Arrays;

/**
 * 由ASM在Awakements.<clinit>末尾调用
 * 注册所有自定义觉醒到CQ系统
 */
public class AwakementsInitializer {

    // ======== 法术觉醒实例 ========
    public static Awakements highSpeedChant;
    public static Awakements spellAmplification;
    public static Awakements instantCast;
    public static Awakements manaSurge;
    public static Awakements longRange;
    public static Awakements echoingVoice;

    // ======== 武器觉醒实例 ========
    public static Awakements potionCapacity;
    public static Awakements potionInfusion;

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
            System.out.println("[ChocoTweak] initCustomAwakenings() called - awekements length: " + 
                (Awakements.awekements != null ? Awakements.awekements.length : "null"));

            if (Awakements.awekements == null) {
                System.err.println("[ChocoTweak] ERROR: Awakements.awekements is null!");
                return;
            }

            // 创建法术觉醒实例
            highSpeedChant = new AwakementHighSpeedChant("highSpeedChant", 0);
            spellAmplification = new AwakementSpellAmplification("spellAmplification", 0);
            instantCast = new AwakementInstantCast("instantCast", 0);
            manaSurge = new AwakementManaSurge("manaSurge", 0);
            longRange = new AwakementLongRange("longRange", 0);
            echoingVoice = new AwakementEchoingVoice("echoingVoice", 0);

            // 创建武器觉醒实例
            potionCapacity = new AwakementPotionCapacity("potionCapacity", 0);
            potionInfusion = new AwakementPotionInfusion("potionInfusion", 0);

            // 将新觉醒添加到CQ的awekements数组
            int oldLength = Awakements.awekements.length;
            Awakements[] newArray = Arrays.copyOf(Awakements.awekements, oldLength + 8);
            newArray[oldLength] = highSpeedChant;
            newArray[oldLength + 1] = spellAmplification;
            newArray[oldLength + 2] = instantCast;
            newArray[oldLength + 3] = manaSurge;
            newArray[oldLength + 4] = longRange;
            newArray[oldLength + 5] = echoingVoice;
            newArray[oldLength + 6] = potionCapacity;
            newArray[oldLength + 7] = potionInfusion;

            // 替换原数组
            Awakements.awekements = newArray;

            System.out.println("[ChocoTweak] SUCCESS: Registered 8 custom awakenings! (new count: " + 
                Awakements.awekements.length + ")");

        } catch (Exception e) {
            System.err.println("[ChocoTweak] ERROR in initCustomAwakenings: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
