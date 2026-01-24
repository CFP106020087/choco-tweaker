package com.chocotweak.bedrock.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Configuration for Bedrock NPC system.
 */
@Config(modid = "chocotweak", name = "chocotweak/bedrock_npc", category = "bedrock_npc")
public class BedrockNpcConfig {
    
    @Config.Comment("Enable Easter Egg NPC system")
    public static boolean enableEasterEggNpc = true;
    
    @Config.Comment("Base path for YSM models (relative to assets)")
    public static String modelBasePath = "yes_steve_model:builtin/wine_fox";
    
    @Config.Comment("Default model variant index (0-14 for wine_fox)")
    @Config.RangeInt(min = 0, max = 14)
    public static int defaultVariant = 0;
    
    @Config.Comment("Animation speed multiplier")
    @Config.RangeDouble(min = 0.1, max = 3.0)
    public static double animationSpeed = 1.0;
    
    @Config.Comment("Enable reaction animations after dialog/trade")
    public static boolean enableReactionAnimations = true;
    
    @Config.Comment("List of reaction animations to use (from YSM extra animations)")
    public static String[] reactionAnimations = {
        "extra1",  // 打招呼
        "extra2",  // 鼓掌
        "extra5"   // 卖萌
    };
    
    @Mod.EventBusSubscriber(modid = "chocotweak")
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals("chocotweak")) {
                ConfigManager.sync("chocotweak", Config.Type.INSTANCE);
            }
        }
    }
}
