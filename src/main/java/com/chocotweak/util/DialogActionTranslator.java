package com.chocotweak.util;

import net.minecraft.client.resources.I18n;

import java.util.HashMap;
import java.util.Map;

/**
 * 对话动作名称翻译工具类
 * 从MixinDialogActionList提取出来的翻译逻辑
 */
public class DialogActionTranslator {

    // 原始英文名称 -> 翻译键 的映射
    private static final Map<String, String> NAME_TO_KEY = new HashMap<>();

    // 翻译后名称 -> 原始名称 的反向映射
    private static volatile Map<String, String> translatedToOriginal = null;
    private static volatile boolean mapBuildAttempted = false;

    static {
        // CQ 原版动作
        NAME_TO_KEY.put("Open shop", "chocotweak.dialog.open_shop");
        NAME_TO_KEY.put("Open item upgrade", "chocotweak.dialog.open_item_upgrade");
        NAME_TO_KEY.put("Open inventory", "chocotweak.dialog.open_inventory");
        NAME_TO_KEY.put("Join team", "chocotweak.dialog.join_team");
        NAME_TO_KEY.put("NPC variable", "chocotweak.dialog.npc_variable");
        NAME_TO_KEY.put("Global variable", "chocotweak.dialog.global_variable");
        NAME_TO_KEY.put("Reputation", "chocotweak.dialog.reputation");
        NAME_TO_KEY.put("Give item", "chocotweak.dialog.give_item");
        NAME_TO_KEY.put("Consume item", "chocotweak.dialog.consume_item");
        NAME_TO_KEY.put("Command", "chocotweak.dialog.command");
        NAME_TO_KEY.put("Set owner", "chocotweak.dialog.set_owner");
        NAME_TO_KEY.put("Set equipement", "chocotweak.dialog.set_equipement");
        NAME_TO_KEY.put("Set AI", "chocotweak.dialog.set_ai");
        NAME_TO_KEY.put("Load data from NBT", "chocotweak.dialog.load_data_from_nbt");
        NAME_TO_KEY.put("Spawn monster", "chocotweak.dialog.spawn_monster");
        NAME_TO_KEY.put("Kill counter", "chocotweak.dialog.kill_counter");
        NAME_TO_KEY.put("Timer", "chocotweak.dialog.timer");
        NAME_TO_KEY.put("Set home", "chocotweak.dialog.set_home");
        NAME_TO_KEY.put("Build schematic", "chocotweak.dialog.build_schematic");
        NAME_TO_KEY.put("Put in a bottle", "chocotweak.dialog.put_in_a_bottle");

        // ChocoTweak 自定义动作
        NAME_TO_KEY.put("Buy tamed creature", "chocotweak.dialog.buy_tamed_creature");
        NAME_TO_KEY.put("Enchant item", "chocotweak.dialog.enchant_item");
    }

    /**
     * 翻译名称
     */
    public static String translateName(String originalName) {
        if (originalName == null)
            return null;

        try {
            String key = NAME_TO_KEY.get(originalName);
            if (key != null) {
                String translated = I18n.format(key);
                if (translated != null && !translated.equals(key)) {
                    return translated;
                }
            }
        } catch (Exception ignored) {
        }
        return originalName;
    }

    /**
     * 延迟构建反向映射
     */
    private static synchronized void ensureReverseMapBuilt() {
        if (mapBuildAttempted)
            return;
        mapBuildAttempted = true;

        try {
            Map<String, String> newMap = new HashMap<>();
            for (Map.Entry<String, String> entry : NAME_TO_KEY.entrySet()) {
                String originalName = entry.getKey();
                String key = entry.getValue();
                try {
                    String translated = I18n.format(key);
                    if (translated != null && !translated.equals(key) && !translated.isEmpty()) {
                        newMap.put(translated, originalName);
                    }
                } catch (Exception ignored) {
                }
            }
            translatedToOriginal = newMap;
        } catch (Exception ignored) {
            translatedToOriginal = new HashMap<>();
        }
    }

    /**
     * 获取原始名称（从翻译后名称反查）
     */
    public static String getOriginalName(String translatedName) {
        if (translatedName == null)
            return null;

        try {
            ensureReverseMapBuilt();
            if (translatedToOriginal != null) {
                return translatedToOriginal.getOrDefault(translatedName, translatedName);
            }
        } catch (Exception ignored) {
        }
        return translatedName;
    }
}
