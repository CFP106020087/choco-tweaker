package com.chocotweak.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.chocotweak.ChocoTweak;

/**
 * 附魔注册表
 * 在 Forge 注册表中注册所有自定义附魔
 */
@Mod.EventBusSubscriber(modid = ChocoTweak.MODID)
public class ModEnchantments {

    // 武器附魔
    public static EnchantmentSoulHarvest SOUL_HARVEST;
    public static EnchantmentVampiric VAMPIRIC;
    public static EnchantmentDragonSlayer DRAGON_SLAYER;
    public static EnchantmentBeastTamer BEAST_TAMER;

    // 护甲附魔
    public static EnchantmentPermafrost PERMAFROST;
    public static EnchantmentIntimidation INTIMIDATION;

    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
        ChocoTweak.LOGGER.info("Registering Choco Tweak enchantments...");

        SOUL_HARVEST = new EnchantmentSoulHarvest();
        VAMPIRIC = new EnchantmentVampiric();
        DRAGON_SLAYER = new EnchantmentDragonSlayer();
        BEAST_TAMER = new EnchantmentBeastTamer();
        PERMAFROST = new EnchantmentPermafrost();
        INTIMIDATION = new EnchantmentIntimidation();

        event.getRegistry().registerAll(
                SOUL_HARVEST,
                VAMPIRIC,
                DRAGON_SLAYER,
                BEAST_TAMER,
                PERMAFROST,
                INTIMIDATION);

        ChocoTweak.LOGGER.info("Registered 6 Choco Tweak enchantments");
    }
}
