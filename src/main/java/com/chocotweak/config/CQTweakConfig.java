package com.chocotweak.config;

import com.chocotweak.ChocoTweak;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * CQ Tweak 配置文件
 * 用于调整 Chocolate Quest 的各种参数
 */
@Config(modid = ChocoTweak.MODID, name = "chocotweak/cq_tweak")
@Config.LangKey("chocotweak.config.title")
public class CQTweakConfig {

    @Config.Comment("Monster stat multipliers - 怪物属性倍率")
    @Config.LangKey("chocotweak.config.monsters")
    public static MonsterStats monsters = new MonsterStats();

    @Config.Comment("Drop and loot settings - 掉落物设置")
    @Config.LangKey("chocotweak.config.drops")
    public static DropSettings drops = new DropSettings();

    @Config.Comment("Dungeon generation settings - 地牢生成设置")
    @Config.LangKey("chocotweak.config.dungeons")
    public static DungeonSettings dungeons = new DungeonSettings();

    @Config.Comment("Entity spawn settings - 实体生成设置")
    @Config.LangKey("chocotweak.config.spawns")
    public static SpawnSettings spawns = new SpawnSettings();

    public static class MonsterStats {
        @Config.Comment("Health multiplier for all CQ monsters (1.0 = default)")
        @Config.LangKey("chocotweak.config.monsters.health_multiplier")
        @Config.RangeDouble(min = 0.1, max = 10.0)
        public double healthMultiplier = 1.0;

        @Config.Comment("Damage multiplier for all CQ monsters (1.0 = default)")
        @Config.LangKey("chocotweak.config.monsters.damage_multiplier")
        @Config.RangeDouble(min = 0.1, max = 10.0)
        public double damageMultiplier = 1.0;

        @Config.Comment("Attack range multiplier for CQ monsters (1.0 = default)")
        @Config.LangKey("chocotweak.config.monsters.attack_range_multiplier")
        @Config.RangeDouble(min = 0.1, max = 5.0)
        public double attackRangeMultiplier = 1.0;

        @Config.Comment("Movement speed multiplier for CQ monsters (1.0 = default)")
        @Config.LangKey("chocotweak.config.monsters.speed_multiplier")
        @Config.RangeDouble(min = 0.1, max = 5.0)
        public double speedMultiplier = 1.0;
    }

    public static class DropSettings {
        @Config.Comment("XP drop multiplier for CQ monsters (1.0 = default)")
        @Config.LangKey("chocotweak.config.drops.xp_multiplier")
        @Config.RangeDouble(min = 0.0, max = 10.0)
        public double xpMultiplier = 1.0;

        @Config.Comment("Loot drop multiplier (affects drop chance, 1.0 = default)")
        @Config.LangKey("chocotweak.config.drops.loot_multiplier")
        @Config.RangeDouble(min = 0.0, max = 10.0)
        public double lootMultiplier = 1.0;

        @Config.Comment("Enable custom loot tables from config folder")
        @Config.LangKey("chocotweak.config.drops.custom_loot_enabled")
        public boolean customLootEnabled = false;

        @Config.Comment("Use vanilla loot tables for non-ExporterChest dungeon chests")
        @Config.LangKey("chocotweak.config.drops.use_vanilla_loot")
        public boolean useVanillaLootTables = false;

        @Config.Comment("Path to custom loot table JSON file (relative to config folder)")
        @Config.LangKey("chocotweak.config.drops.custom_loot_path")
        public String customLootPath = "chocotweak/loot_tables.json";
    }

    public static class DungeonSettings {
        @Config.Comment("Dungeon spawn chance multiplier (1.0 = default, 0.5 = half, 2.0 = double)")
        @Config.LangKey("chocotweak.config.dungeons.spawn_chance")
        @Config.RangeDouble(min = 0.0, max = 5.0)
        public double spawnChanceMultiplier = 1.0;

        @Config.Comment("Minimum distance between dungeons in chunks (default is 10)")
        @Config.LangKey("chocotweak.config.dungeons.min_distance")
        @Config.RangeInt(min = 1, max = 100)
        public int minDistanceChunks = 10;

        @Config.Comment("Enable/disable dungeon generation completely")
        @Config.LangKey("chocotweak.config.dungeons.enabled")
        public boolean enabled = true;
    }

    public static class SpawnSettings {
        @Config.Comment("NPC spawn rate multiplier in dungeons (1.0 = default)")
        @Config.LangKey("chocotweak.config.spawns.npc_multiplier")
        @Config.RangeDouble(min = 0.0, max = 5.0)
        public double npcMultiplier = 1.0;

        @Config.Comment("Faction presence weight multiplier (1.0 = default)")
        @Config.LangKey("chocotweak.config.spawns.faction_multiplier")
        @Config.RangeDouble(min = 0.0, max = 5.0)
        public double factionMultiplier = 1.0;

        @Config.Comment("Monster count multiplier in dungeons (1.0 = default)")
        @Config.LangKey("chocotweak.config.spawns.monster_count_multiplier")
        @Config.RangeDouble(min = 0.1, max = 5.0)
        public double monsterCountMultiplier = 1.0;
    }

    @Config.Comment("Weapon settings - 武器设置")
    @Config.LangKey("chocotweak.config.weapons")
    public static WeaponSettings weapons = new WeaponSettings();

    public static class WeaponSettings {
        @Config.Comment("Enable weapon special effects (custom attack behaviors)")
        @Config.LangKey("chocotweak.config.weapons.special_effects_enabled")
        public boolean specialEffectsEnabled = true;

        @Config.Comment("Enable custom weapon damage scaling")
        @Config.LangKey("chocotweak.config.weapons.custom_damage_enabled")
        public boolean customDamageEnabled = true;

        @Config.Comment("Enable custom weapon attack speed modifications")
        @Config.LangKey("chocotweak.config.weapons.custom_speed_enabled")
        public boolean customSpeedEnabled = true;
    }

    @Config.Comment("Element Stone settings - 元素石设置")
    @Config.LangKey("chocotweak.config.elementstone")
    public static ElementStoneSettings elementStone = new ElementStoneSettings();

    public static class ElementStoneSettings {
        @Config.Comment("Allow element stones to be applied to ANY weapon/armor from any mod (not just CQ items)")
        @Config.LangKey("chocotweak.config.elementstone.universal_compatibility")
        public boolean universalCompatibility = false;

        @Config.Comment("Base XP cost for applying element stone to non-CQ weapons")
        @Config.LangKey("chocotweak.config.elementstone.mod_weapon_base_cost")
        @Config.RangeInt(min = 1, max = 500)
        public int modWeaponBaseCost = 50;

        @Config.Comment("Base XP cost for applying element stone to non-CQ armor")
        @Config.LangKey("chocotweak.config.elementstone.mod_armor_base_cost")
        @Config.RangeInt(min = 1, max = 500)
        public int modArmorBaseCost = 25;

        @Config.Comment("Max element level for non-CQ weapons (stone's max level is used if lower)")
        @Config.LangKey("chocotweak.config.elementstone.mod_weapon_max_level")
        @Config.RangeInt(min = 1, max = 10)
        public int modWeaponMaxLevel = 6;

        @Config.Comment("Max element level for non-CQ armor")
        @Config.LangKey("chocotweak.config.elementstone.mod_armor_max_level")
        @Config.RangeInt(min = 1, max = 10)
        public int modArmorMaxLevel = 4;

        @Config.Comment("XP cost multiplier per existing level (cost = baseCost + level * this value)")
        @Config.LangKey("chocotweak.config.elementstone.cost_per_level")
        @Config.RangeInt(min = 0, max = 100)
        public int costPerLevel = 10;
    }

    @Config.Comment("Element Enhancement settings - 元素效果增强设置")
    @Config.LangKey("chocotweak.config.elementenhance")
    public static ElementEnhanceSettings elementEnhance = new ElementEnhanceSettings();

    public static class ElementEnhanceSettings {
        @Config.Comment("Enable enhanced element effects")
        @Config.LangKey("chocotweak.config.elementenhance.enabled")
        public boolean enabled = true;

        @Config.Comment("Fire: Base damage bonus when target is burning (0.1 = 10%)")
        @Config.LangKey("chocotweak.config.elementenhance.fire_burn_base")
        @Config.RangeDouble(min = 0.0, max = 2.0)
        public double fireBurnBonusBase = 0.1;

        @Config.Comment("Fire: Damage bonus per element level (0.05 = 5% per level)")
        @Config.LangKey("chocotweak.config.elementenhance.fire_burn_per_level")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double fireBurnBonusPerLevel = 0.05;

        @Config.Comment("Blast: Chance to apply STUN effect (0.15 = 15%)")
        @Config.LangKey("chocotweak.config.elementenhance.blast_stun_chance")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double blastStunChance = 0.15;

        @Config.Comment("Blast: STUN duration in ticks (20 = 1 second)")
        @Config.LangKey("chocotweak.config.elementenhance.blast_stun_duration")
        @Config.RangeInt(min = 1, max = 200)
        public int blastStunDuration = 20;

        @Config.Comment("Water: Slow amount (0.5 = 50% speed reduction)")
        @Config.LangKey("chocotweak.config.elementenhance.water_slow_amount")
        @Config.RangeDouble(min = 0.0, max = 0.95)
        public double waterSlowAmount = 0.5;

        @Config.Comment("Water: Drown damage multiplier per level (10.0 = 10 damage per level)")
        @Config.LangKey("chocotweak.config.elementenhance.water_drown_damage")
        @Config.RangeDouble(min = 0.0, max = 100.0)
        public double waterDrownDamage = 10.0;

        @Config.Comment("Darkness: Extra damage per wither level (0.1 = 10% per level)")
        @Config.LangKey("chocotweak.config.elementenhance.darkness_wither_bonus")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double darknessWitherDamageBonus = 0.1;

        @Config.Comment("Light: Base undead damage multiplier (1.5 = 1.5x)")
        @Config.LangKey("chocotweak.config.elementenhance.light_undead_base")
        @Config.RangeDouble(min = 1.0, max = 10.0)
        public double lightUndeadBase = 1.5;

        @Config.Comment("Light: Undead damage multiplier per level (0.2 = +0.2x per level)")
        @Config.LangKey("chocotweak.config.elementenhance.light_undead_per_level")
        @Config.RangeDouble(min = 0.0, max = 2.0)
        public double lightUndeadPerLevel = 0.2;

        @Config.Comment("Light: Attack range increase per element level")
        @Config.LangKey("chocotweak.config.elementenhance.light_range_per_level")
        @Config.RangeDouble(min = 0.0, max = 2.0)
        public double lightRangePerLevel = 0.5;

        @Config.Comment("Magic: Max stacks of Magic Vulnerability per level (2 = 2 stacks per level)")
        @Config.LangKey("chocotweak.config.elementenhance.magic_stacks_per_level")
        @Config.RangeInt(min = 1, max = 10)
        public int magicStacksPerLevel = 2;

        @Config.Comment("Magic: Damage bonus per stack (0.02 = 2% bonus magic damage received)")
        @Config.LangKey("chocotweak.config.elementenhance.magic_bonus_per_stack")
        @Config.RangeDouble(min = 0.0, max = 0.5)
        public double magicBonusPerStack = 0.02;

        @Config.Comment("Physical: Base damage multiplier (1.0 = no bonus)")
        @Config.LangKey("chocotweak.config.elementenhance.physical_base")
        @Config.RangeDouble(min = 1.0, max = 5.0)
        public double physicalBase = 1.0;

        @Config.Comment("Physical: Damage multiplier per level (0.1 = +10% per level)")
        @Config.LangKey("chocotweak.config.elementenhance.physical_per_level")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double physicalPerLevel = 0.1;
    }

    /**
     * 配置变更事件处理
     */
    @Mod.EventBusSubscriber(modid = ChocoTweak.MODID)
    public static class ConfigSyncHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(ChocoTweak.MODID)) {
                ConfigManager.sync(ChocoTweak.MODID, Config.Type.INSTANCE);
                ChocoTweak.LOGGER.info("ChocoTweak config reloaded!");
            }
        }
    }
}
