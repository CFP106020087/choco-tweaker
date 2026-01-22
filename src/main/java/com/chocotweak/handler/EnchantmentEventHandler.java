package com.chocotweak.handler;

import com.chocotweak.ChocoTweak;
import com.chocotweak.enchantment.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Random;

/**
 * 附魔效果事件处理器
 * 使用 Forge 事件总线监听战斗事件并触发附魔效果
 */
@Mod.EventBusSubscriber(modid = ChocoTweak.MODID)
public class EnchantmentEventHandler {

    private static final Random RANDOM = new Random();

    /**
     * 处理攻击事件 - 武器附魔效果
     * - 吸血
     * - 屠龙者
     * - 驯兽师
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource() == null)
            return;

        Entity sourceEntity = event.getSource().getTrueSource();
        if (!(sourceEntity instanceof EntityPlayer))
            return;

        EntityPlayer player = (EntityPlayer) sourceEntity;
        EntityLivingBase target = event.getEntityLiving();
        ItemStack weapon = player.getHeldItemMainhand();

        if (weapon.isEmpty())
            return;

        float damage = event.getAmount();

        // === 吸血 (Vampiric) ===
        int vampiricLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.VAMPIRIC, weapon);
        if (vampiricLevel > 0) {
            float healAmount = damage * EnchantmentVampiric.getLifestealPercent(vampiricLevel);
            if (healAmount > 0) {
                player.heal(healAmount);
                ChocoTweak.LOGGER.debug("Vampiric healed {} for {} HP", player.getName(), healAmount);
            }
        }

        // === 屠龙者 (Dragon Slayer) ===
        int dragonSlayerLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.DRAGON_SLAYER, weapon);
        if (dragonSlayerLevel > 0 && EnchantmentDragonSlayer.isDragon(target)) {
            float bonusDamage = EnchantmentDragonSlayer.getBonusDamage(dragonSlayerLevel);
            event.setAmount(damage + bonusDamage);
            ChocoTweak.LOGGER.debug("Dragon Slayer added {} damage to {}", bonusDamage, target.getName());
        }

        // === 驯兽师 (Beast Tamer) ===
        int beastTamerLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.BEAST_TAMER, weapon);
        if (beastTamerLevel > 0) {
            float chance = EnchantmentBeastTamer.getTameChance(beastTamerLevel);
            if (RANDOM.nextFloat() < chance) {
                if (EnchantmentBeastTamer.tryTame(target, player)) {
                    // 驯服成功，取消伤害
                    event.setCanceled(true);
                    ChocoTweak.LOGGER.info("Beast Tamer tamed {} for {}", target.getName(), player.getName());
                }
            }
        }
    }

    /**
     * 处理受伤事件（被攻击方视角）- 护甲附魔效果
     * - 冰霜行者
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer))
            return;

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        Entity attacker = event.getSource().getTrueSource();

        if (!(attacker instanceof EntityLivingBase))
            return;
        EntityLivingBase livingAttacker = (EntityLivingBase) attacker;

        // === 冰霜行者 (Permafrost) ===
        ItemStack chestArmor = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!chestArmor.isEmpty()) {
            int permafrostLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.PERMAFROST, chestArmor);
            if (permafrostLevel > 0) {
                float chance = EnchantmentPermafrost.getFreezeChance(permafrostLevel);
                if (RANDOM.nextFloat() < chance) {
                    EnchantmentPermafrost.applyFreeze(livingAttacker, permafrostLevel);
                    ChocoTweak.LOGGER.debug("Permafrost froze {} for {} ticks",
                            livingAttacker.getName(), EnchantmentPermafrost.getFreezeDuration(permafrostLevel));
                }
            }
        }
    }

    /**
     * 处理经验掉落事件
     * - 灵魂收割
     */
    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        EntityPlayer player = event.getAttackingPlayer();
        if (player == null)
            return;

        ItemStack weapon = player.getHeldItemMainhand();
        if (weapon.isEmpty())
            return;

        int soulHarvestLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.SOUL_HARVEST, weapon);
        if (soulHarvestLevel > 0) {
            float chance = EnchantmentSoulHarvest.getDoubleXpChance(soulHarvestLevel);
            if (RANDOM.nextFloat() < chance) {
                int originalXp = event.getDroppedExperience();
                event.setDroppedExperience(originalXp * 2);
                ChocoTweak.LOGGER.debug("Soul Harvest doubled XP from {} to {}", originalXp, originalXp * 2);
            }
        }
    }

    /**
     * 玩家 tick 事件 - 持续效果
     * - 威压（每2秒检查一次）
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        if (event.player.world.isRemote)
            return;

        EntityPlayer player = event.player;

        // 每 40 tick (2秒) 检查一次威压效果
        if (player.ticksExisted % 40 != 0)
            return;

        // === 威压 (Intimidation) ===
        int intimidationLevel = EnchantmentIntimidation.getIntimidationLevel(player);
        if (intimidationLevel > 0) {
            EnchantmentIntimidation.applyIntimidation(player, intimidationLevel);
        }
    }
}
