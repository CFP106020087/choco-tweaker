package com.chocotweak;

import com.chocotweak.config.TamingConfig;
import com.chocotweak.config.EnchantmentNpcConfig;
import com.chocotweak.config.NpcDialogConfig;
import com.chocotweak.command.CommandChocoTweak;
import com.chocotweak.command.StructureUtils;
import com.chocotweak.handler.ShieldAttackHandler;
import com.chocotweak.network.ChocoNetwork;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Choco Tweak - Chocolate Quest Extension Mod
 * 
 * This addon provides Mixin-based extensions for Chocolate Quest:
 * - Modded pet taming support (Ice and Fire, Lycanites Mobs)
 * - Custom enchantments with NPC-specific availability
 * - Structure spawn/save commands for modpack creation
 * - NPC creature/enchantment customization
 * - Configurable via JSON
 */
@Mod(modid = ChocoTweak.MODID, name = ChocoTweak.NAME, version = ChocoTweak.VERSION, dependencies = "required-after:chocolatequest;after:baubles")
public class ChocoTweak {

    public static final String MODID = "chocotweak";
    public static final String NAME = "Choco Tweak";
    public static final String VERSION = "1.0.0";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.Instance(MODID)
    public static ChocoTweak instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Choco Tweak Pre-Init");
        TamingConfig.loadConfig(event.getModConfigurationDirectory());
        EnchantmentNpcConfig.loadConfig(event.getModConfigurationDirectory());
        NpcDialogConfig.loadConfig(event.getModConfigurationDirectory());
        StructureUtils.init(event.getModConfigurationDirectory());

        // 注册网络包
        ChocoNetwork.init();
        LOGGER.info("Choco Tweak network registered");

        // 注册GUI处理器
        net.minecraftforge.fml.common.network.NetworkRegistry.INSTANCE.registerGuiHandler(
                instance, new com.chocotweak.gui.ChocoTweakGuiHandler());
        LOGGER.info("Choco Tweak GUI handler registered");

        // 注册盾牌攻击处理器
        ShieldAttackHandler.INSTANCE.register();

        // 注册地牢保护处理器
        com.chocotweak.handler.DungeonProtectionHandler.INSTANCE.register();
        LOGGER.info("Choco Tweak dungeon protection registered");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("Choco Tweak Initialized - Loaded {} taming rules",
                TamingConfig.getRules().size());

        // 注入自定义对话动作到 CQ
        com.chocotweak.dialog.DialogActionRegistry.registerCustomActions();

        // 注册武器特效事件处理器
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new com.chocotweak.weapon.WeaponEventHandler());
        LOGGER.info("Choco Tweak weapon effects registered");

        // 猴王武器系统日志
        LOGGER.info("Choco Tweak Monking weapon system active - Stun potion, armor drop, hook fall immunity");

        // 应用自定义武器白值
        com.chocotweak.weapon.WeaponStatModifier.applyCustomStats();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new com.chocotweak.weapon.WeaponStatModifier());

        // Register client-side key bindings and tooltip handler
        if (net.minecraftforge.fml.common.FMLCommonHandler.instance().getSide().isClient()) {
            ClientProxy.registerKeyBindings();
            net.minecraftforge.common.MinecraftForge.EVENT_BUS
                    .register(new com.chocotweak.client.ArmorTooltipHandler());
            LOGGER.info("Choco Tweak armor tooltips registered");
        }

        // 深渊漫步者之王胸甲效果
        net.minecraftforge.common.MinecraftForge.EVENT_BUS
                .register(new com.chocotweak.handler.AbyssWalkerArmorHandler());
        LOGGER.info("Abyss Walker King Armor effects registered");
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandChocoTweak());
        event.registerServerCommand(new com.chocotweak.command.CommandSpellAwaken());
        event.registerServerCommand(new com.chocotweak.command.CommandPotionInfuse());
        LOGGER.info("Choco Tweak commands registered");
    }
}
