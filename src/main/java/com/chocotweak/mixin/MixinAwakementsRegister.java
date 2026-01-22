package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.magic.Awakements;
import com.chocotweak.magic.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

/**
 * 通过Mixin注册自定义觉醒到CQ系统
 * 
 * 关键：在Awakements类静态初始化完成后立即添加新觉醒
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.magic.Awakements", remap = false)
public class MixinAwakementsRegister {

    @Shadow
    public static Awakements[] awekements;

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

    /**
     * 注入到静态初始化块结束时 - 这确保在awekements数组初始化后立即执行
     */
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onClinit(CallbackInfo ci) {
        registerAwakenings();
    }

    /**
     * 注册所有自定义觉醒
     */
    private static void registerAwakenings() {
        System.out.println("[ChocoTweak] Registering custom awakenings in <clinit>... (current count: " +
                (awekements != null ? awekements.length : "null") + ")");

        if (awekements == null) {
            System.err.println("[ChocoTweak] ERROR: awekements is null in clinit!");
            return;
        }

        try {
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
            int oldLength = awekements.length;
            Awakements[] newArray = Arrays.copyOf(awekements, oldLength + 8);
            newArray[oldLength] = highSpeedChant;
            newArray[oldLength + 1] = spellAmplification;
            newArray[oldLength + 2] = instantCast;
            newArray[oldLength + 3] = manaSurge;
            newArray[oldLength + 4] = longRange;
            newArray[oldLength + 5] = echoingVoice;
            newArray[oldLength + 6] = potionCapacity;
            newArray[oldLength + 7] = potionInfusion;

            // 替换原数组
            awekements = newArray;

            System.out.println(
                    "[ChocoTweak] SUCCESS: Registered 8 custom awakenings! (new count: " + awekements.length + ")");
        } catch (Exception e) {
            System.err.println("[ChocoTweak] ERROR registering awakenings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 强制初始化 - 触发Awakements类加载
     */
    public static void forceInit() {
        // 访问Awakements类的任意静态成员会触发类加载和clinit执行
        int dummy = awekements != null ? awekements.length : 0;
    }
}
