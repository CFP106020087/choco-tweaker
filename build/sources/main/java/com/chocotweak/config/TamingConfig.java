package com.chocotweak.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.chocotweak.ChocoTweak;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Configuration manager for modded pet taming rules.
 * Loads from config/chocotweak/taming_rules.json
 */
public class TamingConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static List<TamingRule> rules = new ArrayList<>();

    /**
     * Represents a single taming rule for a modded entity.
     */
    public static class TamingRule {
        /**
         * Full class name of the entity (e.g.,
         * "com.github.alexthe666.iceandfire.entity.EntityFireDragon")
         */
        public String entityClass;

        /** Method name to call for taming (e.g., "setTamed") */
        public String tameMethod = "setTamed";

        /** Method name to set owner (e.g., "setOwnerUUID") */
        public String ownerMethod = "setOwnerUUID";

        /** Owner parameter type: "UUID" or "EntityPlayer" */
        public String ownerType = "UUID";

        /** Optional: Additional method to call after taming */
        public String postTameMethod = null;

        /** Description for documentation */
        public String description = "";

        public TamingRule() {
        }

        public TamingRule(String entityClass, String description) {
            this.entityClass = entityClass;
            this.description = description;
        }
    }

    /**
     * Configuration root object.
     */
    public static class ConfigRoot {
        public List<TamingRule> rules = new ArrayList<>();
    }

    /**
     * Load configuration from file.
     */
    public static void loadConfig(File configDir) {
        File modConfigDir = new File(configDir, "chocotweak");
        if (!modConfigDir.exists()) {
            modConfigDir.mkdirs();
        }

        File configFile = new File(modConfigDir, "taming_rules.json");

        if (!configFile.exists()) {
            // Create default config
            createDefaultConfig(configFile);
        }

        try (FileReader reader = new FileReader(configFile)) {
            ConfigRoot config = GSON.fromJson(reader, ConfigRoot.class);
            if (config != null && config.rules != null) {
                rules = config.rules;
                ChocoTweak.LOGGER.info("Loaded {} taming rules from config", rules.size());
            }
        } catch (IOException e) {
            ChocoTweak.LOGGER.error("Failed to load taming config", e);
        }
    }

    /**
     * Create default configuration file with Ice and Fire and Lycanites Mobs
     * support.
     */
    private static void createDefaultConfig(File configFile) {
        ConfigRoot config = new ConfigRoot();

        // Ice and Fire Dragons
        TamingRule fireDragon = new TamingRule(
                "com.github.alexthe666.iceandfire.entity.EntityFireDragon",
                "Ice and Fire - Fire Dragon");
        config.rules.add(fireDragon);

        TamingRule iceDragon = new TamingRule(
                "com.github.alexthe666.iceandfire.entity.EntityIceDragon",
                "Ice and Fire - Ice Dragon");
        config.rules.add(iceDragon);

        TamingRule lightningDragon = new TamingRule(
                "com.github.alexthe666.iceandfire.entity.EntityLightningDragon",
                "Ice and Fire - Lightning Dragon");
        config.rules.add(lightningDragon);

        // Ice and Fire other creatures
        TamingRule hippogryph = new TamingRule(
                "com.github.alexthe666.iceandfire.entity.EntityHippogryph",
                "Ice and Fire - Hippogryph");
        config.rules.add(hippogryph);

        TamingRule amphithere = new TamingRule(
                "com.github.alexthe666.iceandfire.entity.EntityAmphithere",
                "Ice and Fire - Amphithere");
        config.rules.add(amphithere);

        // Lycanites Mobs
        TamingRule lycanitesBase = new TamingRule(
                "com.lycanitesmobs.core.entity.BaseCreatureEntity",
                "Lycanites Mobs - Base Creature (covers all)");
        lycanitesBase.ownerMethod = "setPlayerOwner";
        lycanitesBase.ownerType = "EntityPlayer";
        config.rules.add(lycanitesBase);

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(config, writer);
            ChocoTweak.LOGGER.info("Created default taming_rules.json");
        } catch (IOException e) {
            ChocoTweak.LOGGER.error("Failed to create default config", e);
        }
    }

    /**
     * Get all loaded taming rules.
     */
    public static List<TamingRule> getRules() {
        return rules;
    }

    /**
     * Find a matching rule for an entity.
     */
    public static TamingRule findRuleForEntity(Object entity) {
        if (entity == null)
            return null;

        Class<?> entityClass = entity.getClass();

        for (TamingRule rule : rules) {
            try {
                Class<?> ruleClass = Class.forName(rule.entityClass);
                if (ruleClass.isAssignableFrom(entityClass)) {
                    return rule;
                }
            } catch (ClassNotFoundException e) {
                // Mod not loaded, skip this rule
            }
        }

        return null;
    }
}
