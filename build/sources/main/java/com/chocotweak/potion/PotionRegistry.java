
package com.chocotweak.potion;

import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 药水效果注册
 */
@Mod.EventBusSubscriber(modid = "chocotweak")
public class PotionRegistry {

    public static PotionStun STUN;
    public static PotionWound WOUND;
    public static PotionMagicVulnerability MAGIC_VULNERABILITY;

    @SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        STUN = new PotionStun();
        WOUND = new PotionWound();
        PotionDungeonExplorer.INSTANCE = new PotionDungeonExplorer();
        MAGIC_VULNERABILITY = new PotionMagicVulnerability();

        event.getRegistry().register(STUN);
        event.getRegistry().register(WOUND);
        event.getRegistry().register(PotionDungeonExplorer.INSTANCE);
        event.getRegistry().register(MAGIC_VULNERABILITY);

        System.out.println("[ChocoTweak] Registered custom potions: Stun, Wound, DungeonExplorer, MagicVulnerability");
    }
}
