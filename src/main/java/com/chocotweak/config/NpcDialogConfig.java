package com.chocotweak.config;

import com.chocotweak.ChocoTweak;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * NPC 对话配置管理
 * 管理自定义的购买生物和附魔服务
 */
public class NpcDialogConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ConfigData configData = null;
    private static File configFile;

    /**
     * 获取所有可购买的生物列表
     */
    public static List<CreatureEntry> getAvailableCreatures() {
        if (configData != null && configData.creatures != null) {
            return configData.creatures;
        }
        return Collections.emptyList();
    }

    /**
     * 获取所有可用的附魔服务
     */
    public static List<EnchantmentEntry> getAvailableEnchantments() {
        if (configData != null && configData.enchantments != null) {
            return configData.enchantments;
        }
        return Collections.emptyList();
    }

    /**
     * 添加新的生物配置
     */
    public static void addCreature(String entityId, String displayName, List<CostItem> costItems) {
        if (configData == null) {
            configData = new ConfigData();
            configData.creatures = new ArrayList<>();
            configData.enchantments = new ArrayList<>();
        }

        CreatureEntry entry = new CreatureEntry();
        entry.entityId = entityId;
        entry.displayName = displayName;
        entry.costItems = costItems != null ? costItems : new ArrayList<>();
        configData.creatures.add(entry);

        saveConfig();
    }

    /**
     * 添加新的附魔配置
     */
    public static void addEnchantment(String enchantmentId, int level, List<CostItem> costItems) {
        if (configData == null) {
            configData = new ConfigData();
            configData.creatures = new ArrayList<>();
            configData.enchantments = new ArrayList<>();
        }

        EnchantmentEntry entry = new EnchantmentEntry();
        entry.enchantmentId = enchantmentId;
        entry.level = level;
        entry.costItems = costItems != null ? costItems : new ArrayList<>();
        configData.enchantments.add(entry);

        saveConfig();
    }

    /**
     * 加载配置
     */
    public static void loadConfig(File configDir) {
        File chocoTweakDir = new File(configDir, "chocotweak");
        if (!chocoTweakDir.exists()) {
            chocoTweakDir.mkdirs();
        }

        configFile = new File(chocoTweakDir, "npc_dialog.json");

        if (!configFile.exists()) {
            createDefaultConfig();
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
            configData = GSON.fromJson(reader, ConfigData.class);
            ChocoTweak.LOGGER.info("Loaded {} creature entries and {} enchantment entries",
                    configData.creatures != null ? configData.creatures.size() : 0,
                    configData.enchantments != null ? configData.enchantments.size() : 0);
        } catch (Exception e) {
            ChocoTweak.LOGGER.error("Failed to load NPC dialog config", e);
            configData = new ConfigData();
            configData.creatures = new ArrayList<>();
            configData.enchantments = new ArrayList<>();
        }
    }

    /**
     * 保存配置
     */
    public static void saveConfig() {
        if (configFile == null || configData == null)
            return;

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
            GSON.toJson(configData, writer);
            ChocoTweak.LOGGER.info("Saved NPC dialog config");
        } catch (Exception e) {
            ChocoTweak.LOGGER.error("Failed to save NPC dialog config", e);
        }
    }

    private static void createDefaultConfig() {
        configData = new ConfigData();
        configData.creatures = new ArrayList<>();
        configData.enchantments = new ArrayList<>();

        // 示例配置 - 购买狼 (需要 5 绿宝石)
        CreatureEntry wolf = new CreatureEntry();
        wolf.entityId = "minecraft:wolf";
        wolf.displayName = "Wolf";
        wolf.costItems = new ArrayList<>();
        CostItem wolfCost = new CostItem();
        wolfCost.itemId = "minecraft:emerald";
        wolfCost.count = 5;
        wolf.costItems.add(wolfCost);
        configData.creatures.add(wolf);

        // 示例配置 - 灵魂收割附魔 (需要 10 绿宝石 + 1 附魔书)
        EnchantmentEntry soulHarvest = new EnchantmentEntry();
        soulHarvest.enchantmentId = "chocotweak:soul_harvest";
        soulHarvest.level = 1;
        soulHarvest.costItems = new ArrayList<>();
        CostItem emeraldCost = new CostItem();
        emeraldCost.itemId = "minecraft:emerald";
        emeraldCost.count = 10;
        soulHarvest.costItems.add(emeraldCost);
        CostItem bookCost = new CostItem();
        bookCost.itemId = "minecraft:book";
        bookCost.count = 1;
        soulHarvest.costItems.add(bookCost);
        configData.enchantments.add(soulHarvest);

        saveConfig();
    }

    /**
     * 扫描并返回所有可驯服的实体
     */
    public static List<String> scanTameableEntities() {
        List<String> result = new ArrayList<>();

        for (ResourceLocation key : EntityList.getEntityNameList()) {
            String name = key.toString();
            // 添加常见可驯服生物模组
            if (name.contains("wolf") || name.contains("dragon") ||
                    name.contains("pet") || name.contains("tame") ||
                    name.startsWith("iceandfire:") || name.startsWith("lycanitesmobs:")) {
                result.add(name);
            }
        }

        return result;
    }

    /**
     * 扫描并返回所有附魔
     */
    public static List<String> scanAllEnchantments() {
        List<String> result = new ArrayList<>();

        for (Enchantment enchant : Enchantment.REGISTRY) {
            if (enchant.getRegistryName() != null) {
                result.add(enchant.getRegistryName().toString());
            }
        }

        return result;
    }

    // 配置数据类
    public static class ConfigData {
        public List<CreatureEntry> creatures;
        public List<EnchantmentEntry> enchantments;
    }

    public static class CreatureEntry {
        public String entityId;
        public String displayName;
        public List<CostItem> costItems; // 自定义购买所需物品
        public String nbtData; // 可选，自定义 NBT
    }

    public static class EnchantmentEntry {
        public String enchantmentId;
        public int level;
        public List<CostItem> costItems; // 自定义购买所需物品
    }

    public static class CostItem {
        public String itemId; // 物品ID，如 "minecraft:emerald"
        public int count; // 数量
        public int metadata; // 元数据/伤害值
        public String nbt; // 可选，物品 NBT
    }
}
