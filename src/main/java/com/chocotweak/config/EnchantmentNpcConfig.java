package com.chocotweak.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.chocotweak.ChocoTweak;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * NPC 附魔规则配置
 * 控制哪些 NPC 类型可以提供哪些附魔
 */
public class EnchantmentNpcConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ConfigData configData = null;
    private static File configFile;

    /**
     * 检查指定 NPC 是否可以提供指定附魔
     * 
     * @param enchantment 附魔
     * @param npcName     NPC 名称/类型
     * @return 是否可以提供
     */
    public static boolean canNpcProvideEnchantment(Enchantment enchantment, String npcName) {
        if (configData == null || enchantment == null)
            return true;

        ResourceLocation regName = enchantment.getRegistryName();
        if (regName == null)
            return true;

        String enchantId = regName.toString();
        String npcLower = npcName.toLowerCase();

        // 查找规则
        for (EnchantmentRule rule : configData.rules) {
            if (rule.enchantment.equals(enchantId)) {
                // 找到规则，检查 NPC 类型
                if (rule.allowedNpcTypes.contains("*")) {
                    return true; // 通配符，允许所有
                }
                for (String allowed : rule.allowedNpcTypes) {
                    if (npcLower.contains(allowed.toLowerCase())) {
                        return true;
                    }
                }
                return false; // 有规则但不匹配
            }
        }

        // 没有规则，使用默认行为
        return "show_all".equals(configData.defaultBehavior);
    }

    /**
     * 加载配置文件
     */
    public static void loadConfig(File configDir) {
        File chocoTweakDir = new File(configDir, "chocotweak");
        if (!chocoTweakDir.exists()) {
            chocoTweakDir.mkdirs();
        }

        configFile = new File(chocoTweakDir, "enchantment_npc_rules.json");

        if (!configFile.exists()) {
            createDefaultConfig();
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
            configData = GSON.fromJson(reader, ConfigData.class);
            ChocoTweak.LOGGER.info("Loaded {} enchantment NPC rules",
                    configData.rules != null ? configData.rules.size() : 0);
        } catch (Exception e) {
            ChocoTweak.LOGGER.error("Failed to load enchantment NPC rules", e);
            configData = new ConfigData();
            configData.rules = new ArrayList<>();
            configData.defaultBehavior = "show_all";
        }
    }

    private static void createDefaultConfig() {
        try (InputStream is = EnchantmentNpcConfig.class.getResourceAsStream(
                "/assets/chocotweak/default_enchantment_npc_rules.json")) {
            if (is != null) {
                try (OutputStream os = new FileOutputStream(configFile)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        os.write(buffer, 0, len);
                    }
                }
                ChocoTweak.LOGGER.info("Created default enchantment NPC rules config");
            }
        } catch (Exception e) {
            ChocoTweak.LOGGER.error("Failed to create default config", e);
        }
    }

    // 配置数据类
    public static class ConfigData {
        public List<EnchantmentRule> rules;
        public String defaultBehavior;
    }

    public static class EnchantmentRule {
        public String enchantment;
        public List<String> allowedNpcTypes;
        public String description;
    }
}
