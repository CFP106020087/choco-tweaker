package com.chocotweak.handler;

import com.chocolate.chocolateQuest.magic.Elements;
import com.chocotweak.ChocoTweak;
import com.chocotweak.config.CQTweakConfig;
import com.chocotweak.potion.PotionRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.UUID;

/**
 * 元素效果增强处理器
 * 
 * 效果:
 * - Fire: 燃烧中目标+20%伤害
 * - Blast: 概率STUN
 * - Water: 减速 + 溺水伤害 + 水粒子
 * - Darkness: 叠加凋零 + 凋零越高伤害越高
 * - Light: 亡灵2.5x + 攻击距离增加
 */
@Mod.EventBusSubscriber(modid = ChocoTweak.MODID)
public class ElementEnhanceHandler {

    private static final UUID LIGHT_RANGE_UUID = UUID.fromString("b5e8d4a2-3c1f-4e9a-8b7d-6c2a1f0e3d5b");
    private static boolean logged = false;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!CQTweakConfig.elementEnhance.enabled) {
            return;
        }

        EntityLivingBase target = event.getEntityLiving();
        DamageSource source = event.getSource();

        // 获取攻击者
        if (!(source.getTrueSource() instanceof EntityLivingBase)) {
            return;
        }
        EntityLivingBase attacker = (EntityLivingBase) source.getTrueSource();

        // 获取攻击者武器
        ItemStack weapon = attacker.getHeldItemMainhand();
        if (weapon.isEmpty() || !Elements.hasElements(weapon)) {
            return;
        }

        if (!logged) {
            ChocoTweak.LOGGER.info("[ChocoTweak] Element enhancement active");
            logged = true;
        }

        float damage = event.getAmount();
        float bonusDamage = 0;

        // ===== FIRE: 燃烧中目标+20%伤害 (Scaling) =====
        int fireLevel = Elements.getElementValue(weapon, Elements.fire);
        if (fireLevel > 0 && target.isBurning()) {
            double bonusPct = CQTweakConfig.elementEnhance.fireBurnBonusBase
                    + (fireLevel * CQTweakConfig.elementEnhance.fireBurnBonusPerLevel);
            bonusDamage += damage * (float) bonusPct;
        }

        // ===== BLAST: 概率STUN =====
        int blastLevel = Elements.getElementValue(weapon, Elements.blast);
        if (blastLevel > 0) {
            // 概率随等级增加: baseChance + level * 0.05
            double stunChance = CQTweakConfig.elementEnhance.blastStunChance + blastLevel * 0.03;
            if (target.world.rand.nextDouble() < stunChance) {
                int duration = CQTweakConfig.elementEnhance.blastStunDuration + blastLevel * 5;
                target.addPotionEffect(new PotionEffect(PotionRegistry.STUN, duration, 0));
            }
        }

        // ===== WATER: 减速 + 溺水伤害 + 水粒子 =====
        int waterLevel = Elements.getElementValue(weapon, Elements.water);
        if (waterLevel > 0) {
            // 减速 (非药水方式)
            double slowAmount = CQTweakConfig.elementEnhance.waterSlowAmount * (1 + waterLevel * 0.1);
            slowAmount = Math.min(slowAmount, 0.95); // 最大95%减速
            target.motionX *= (1.0 - slowAmount);
            target.motionZ *= (1.0 - slowAmount);

            // 溺水伤害
            float drownDamage = (float) (CQTweakConfig.elementEnhance.waterDrownDamage * waterLevel * 0.5);
            if (drownDamage > 0 && !target.world.isRemote) {
                target.attackEntityFrom(DamageSource.DROWN, drownDamage);
            }

            // 水粒子效果
            if (target.world.isRemote) {
                spawnWaterParticles(target);
            }
        }

        // ===== DARKNESS: 叠加凋零 + 凋零越高伤害越高 =====
        int darknessLevel = Elements.getElementValue(weapon, Elements.darkness);
        if (darknessLevel > 0) {
            // 获取当前凋零等级
            PotionEffect currentWither = target.getActivePotionEffect(MobEffects.WITHER);
            int currentAmplifier = (currentWither != null) ? currentWither.getAmplifier() : -1;
            int newAmplifier = Math.min(currentAmplifier + 1, 4); // 最高凋零V

            // 应用叠加凋零
            int witherDuration = 60 + darknessLevel * 20; // 3秒~5秒
            target.addPotionEffect(new PotionEffect(MobEffects.WITHER, witherDuration, newAmplifier));

            // 凋零越高伤害越高
            if (currentAmplifier >= 0) {
                bonusDamage += damage
                        * (float) (CQTweakConfig.elementEnhance.darknessWitherDamageBonus * (currentAmplifier + 1));
            }
        }

        // ===== LIGHT: 亡灵伤害加成 (Scaling) =====
        int lightLevel = Elements.getElementValue(weapon, Elements.light);
        if (lightLevel > 0) {
            if (target.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD) {
                // 计算新倍率: Base + (Level * PerLevel)
                float originalMultiplier = 1.8f;
                double newMultiplier = CQTweakConfig.elementEnhance.lightUndeadBase
                        + (lightLevel * CQTweakConfig.elementEnhance.lightUndeadPerLevel);

                // 增量 = (新倍率 - 原倍率) * 原始伤害
                if (newMultiplier > originalMultiplier) {
                    bonusDamage += damage * (float) (newMultiplier - originalMultiplier) / originalMultiplier;
                }
            }
        }

        // ===== MAGIC: 施加魔法易伤 (Magic Vulnerability) =====
        int magicLevel = Elements.getElementValue(weapon, Elements.magic);
        if (magicLevel > 0) {
            PotionEffect currentVuln = target.getActivePotionEffect(PotionRegistry.MAGIC_VULNERABILITY);
            int currentStacks = (currentVuln != null) ? currentVuln.getAmplifier() + 1 : 0;
            int maxStacks = magicLevel * CQTweakConfig.elementEnhance.magicStacksPerLevel;

            // 叠加层数
            if (currentStacks < maxStacks) {
                target.addPotionEffect(new PotionEffect(PotionRegistry.MAGIC_VULNERABILITY, 100, currentStacks)); // amplifier
                                                                                                                  // =
                                                                                                                  // stacks
                                                                                                                  // - 1
            } else {
                // 刷新持续时间
                target.addPotionEffect(new PotionEffect(PotionRegistry.MAGIC_VULNERABILITY, 100, currentStacks - 1));
            }
        }

        // ===== PHYSICAL: 单纯的伤害倍率 =====
        int physicalLevel = Elements.getElementValue(weapon, Elements.physic);
        if (physicalLevel > 0) {
            double multiplier = CQTweakConfig.elementEnhance.physicalBase
                    + (physicalLevel * CQTweakConfig.elementEnhance.physicalPerLevel);
            // 增量 = (倍率 - 1) * 原始伤害
            if (multiplier > 1.0) {
                bonusDamage += damage * (float) (multiplier - 1.0);
            }
        }

        // 应用加成伤害
        if (bonusDamage > 0) {
            event.setAmount(damage + bonusDamage);
        }
    }

    /**
     * 额外监听: 处理魔法易伤的增伤效果 (Defender Side)
     * 分离出来是为了逻辑清晰，且不依赖攻击者是否持有武器
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onMagicDamageReceiver(LivingHurtEvent event) {
        if (!CQTweakConfig.elementEnhance.enabled) {
            return;
        }

        EntityLivingBase target = event.getEntityLiving();
        DamageSource source = event.getSource();

        // 检查是否为魔法伤害
        if (!source.isMagicDamage()) {
            return;
        }

        // 检查目标是否有魔法易伤效果
        PotionEffect vulnEffect = target.getActivePotionEffect(PotionRegistry.MAGIC_VULNERABILITY);
        if (vulnEffect != null) {
            int stacks = vulnEffect.getAmplifier() + 1;
            float bonusPercent = (float) (stacks * CQTweakConfig.elementEnhance.magicBonusPerStack);

            float damage = event.getAmount();
            float bonusDamage = damage * bonusPercent;

            event.setAmount(damage + bonusDamage);
        }
    }

    /**
     * 更新光明元素的攻击距离
     * 应在玩家切换武器或元素变化时调用
     */
    public static void updateLightRangeModifier(EntityPlayer player) {
        if (!CQTweakConfig.elementEnhance.enabled) {
            return;
        }

        IAttributeInstance reachAttr = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE);
        if (reachAttr == null) {
            return;
        }

        // 移除旧的修改器
        AttributeModifier oldModifier = reachAttr.getModifier(LIGHT_RANGE_UUID);
        if (oldModifier != null) {
            reachAttr.removeModifier(oldModifier);
        }

        // 检查当前武器的光明等级
        ItemStack weapon = player.getHeldItemMainhand();
        if (weapon.isEmpty() || !Elements.hasElements(weapon)) {
            return;
        }

        int lightLevel = Elements.getElementValue(weapon, Elements.light);
        if (lightLevel > 0) {
            double rangeBonus = lightLevel * CQTweakConfig.elementEnhance.lightRangePerLevel;
            AttributeModifier newModifier = new AttributeModifier(
                    LIGHT_RANGE_UUID,
                    "Light Element Range",
                    rangeBonus,
                    0 // 加算
            );
            reachAttr.applyModifier(newModifier);
        }
    }

    /**
     * 在目标周围生成水粒子
     */
    private static void spawnWaterParticles(EntityLivingBase target) {
        for (int i = 0; i < 8; i++) {
            double x = target.posX + (target.world.rand.nextDouble() - 0.5) * target.width;
            double y = target.posY + target.world.rand.nextDouble() * target.height;
            double z = target.posZ + (target.world.rand.nextDouble() - 0.5) * target.width;

            target.world.spawnParticle(EnumParticleTypes.WATER_SPLASH,
                    x, y, z,
                    0, 0.1, 0);
        }
    }
}
