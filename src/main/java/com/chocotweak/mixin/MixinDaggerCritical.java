package com.chocotweak.mixin;

import net.minecraft.util.ResourceLocation;
import com.chocotweak.potion.PotionRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 匕首特殊效果 Mixin
 * 
 * 所有匕首：
 * - 背刺 (120度) 伤害 3 倍
 * - 各有不同的暴击倍率和特殊效果
 * 
 * 铁匕首: 1/5 概率 2x 伤害
 * 钻石匕首: 1/5 概率 4x 伤害
 * 锈匕首: 1/5 概率 6x 伤害 + 剑盾效果(晕眩+打落盔甲)
 * tricksterDagger: 1/5 概率 6x 伤害 (防御效果在另一个处理器)
 * ninjaDagger: 1/5 概率 10x 伤害
 * monkingDagger: 1/5 概率 6x 伤害 + 晕眩
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.items.swords.ItemBaseDagger", remap = false)
public class MixinDaggerCritical {

    private static final Random RANDOM = new Random();
    private static final double CRIT_CHANCE = 0.2; // 1/5 = 20%
    private static final double WOUND_CHANCE = 0.35; // 35% 挫伤概率
    private static final double BACKSTAB_ANGLE = 120.0; // 背刺角度
    private static final float BACKSTAB_MULTIPLIER = 3.0f; // 背刺伤害倍率

    @Inject(method = "hitEntity", at = @At("HEAD"))
    private void onHitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker,
            CallbackInfoReturnable<Boolean> cir) {

        Item item = stack.getItem();

        // 计算是否背刺 (120度)
        boolean isBackstab = isBackstab(attacker, target);

        // 计算是否暴击 (1/5 概率)
        boolean isCrit = RANDOM.nextDouble() < CRIT_CHANCE;

        float critMultiplier = getCritMultiplier(item);
        float totalMultiplier = 1.0f;

        // 背刺 3 倍
        if (isBackstab) {
            totalMultiplier *= BACKSTAB_MULTIPLIER;
            spawnBackstabParticles(target);
        }

        // 暴击倍率
        if (isCrit && critMultiplier > 1.0f) {
            totalMultiplier *= critMultiplier;
            spawnCritParticles(target);
        }

        // 应用额外伤害（通过直接攻击）
        if (totalMultiplier > 1.0f && !target.world.isRemote) {
            // 计算基础攻击力
            float baseDamage = (float) attacker.getEntityAttribute(
                    net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();

            // 额外伤害 = 基础伤害 * (倍率 - 1)，因为原版会造成 1 倍伤害
            float extraDamage = baseDamage * (totalMultiplier - 1.0f);

            if (extraDamage > 0) {
                // 造成额外伤害
                if (attacker instanceof EntityPlayer) {
                    target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) attacker), extraDamage);
                } else {
                    target.attackEntityFrom(DamageSource.causeMobDamage(attacker), extraDamage);
                }
            }
        }

        // 锈匕首：每次攻击有35%概率施加挫伤（无上限）
        if (isItem(item, "chocolatequest:rusteddagger") && !target.world.isRemote) {
            if (RANDOM.nextDouble() < WOUND_CHANCE) {
                applyWoundEffect(target);
            }
        }

        // 暴击特殊效果
        if (isCrit && !target.world.isRemote) {
            applySpecialEffect(item, target, attacker);
        }
    }

    /**
     * 检查是否为背刺（攻击者在目标背后 120 度内）
     */
    private boolean isBackstab(EntityLivingBase attacker, EntityLivingBase target) {
        double angle = attacker.rotationYaw - target.rotationYaw;

        // 归一化角度到 0-360
        while (angle > 360.0)
            angle -= 360.0;
        while (angle < 0.0)
            angle += 360.0;

        // 计算与正后方的差距
        angle = Math.abs(angle - 180.0);

        // 120 度背刺范围 = 左右各 60 度
        return angle > (180.0 - BACKSTAB_ANGLE / 2);
    }

    private static boolean isItem(Item item, String registryName) {
        ResourceLocation loc = item.getRegistryName();
        return loc != null && loc.toString().equals(registryName);
    }

    /**
     * 获取各匕首的暴击倍率
     */
    private float getCritMultiplier(Item item) {
        if (isItem(item, "chocolatequest:irondagger"))
            return 2.0f;
        if (isItem(item, "chocolatequest:diamonddagger"))
            return 4.0f;
        if (isItem(item, "chocolatequest:rusteddagger"))
            return 6.0f;
        if (isItem(item, "chocolatequest:tricksterdagger"))
            return 6.0f;
        if (isItem(item, "chocolatequest:ninjadagger"))
            return 10.0f;
        if (isItem(item, "chocolatequest:monkingdagger"))
            return 6.0f;
        return 1.0f;
    }

    /**
     * 应用匕首特殊效果（暴击时触发）
     */
    private void applySpecialEffect(Item item, EntityLivingBase target, EntityLivingBase attacker) {
        // 锈匕首的挫伤已移到每次攻击触发，这里不再处理

        // 猴王匕首：晕眩
        if (isItem(item, "chocolatequest:monkingdagger")) {
            if (PotionRegistry.STUN != null) {
                target.addPotionEffect(new PotionEffect(PotionRegistry.STUN, 60, 0)); // 3秒
            }
        }

        // tricksterDagger 的闪避效果在 TricksterDaggerHandler 中处理
    }

    /**
     * 施加挫伤效果（无上限叠加）
     */
    private void applyWoundEffect(EntityLivingBase target) {
        if (PotionRegistry.WOUND != null) {
            int currentLevel = 0;
            if (target.isPotionActive(PotionRegistry.WOUND)) {
                currentLevel = target.getActivePotionEffect(PotionRegistry.WOUND).getAmplifier();
            }
            // 无上限叠加
            int newLevel = currentLevel + 1;
            target.addPotionEffect(new PotionEffect(PotionRegistry.WOUND, 200, newLevel)); // 10秒
        }
    }

    /**
     * 随机打落目标的一件盔甲
     */
    private void dropRandomArmor(EntityLivingBase target) {
        EntityEquipmentSlot[] armorSlots = {
                EntityEquipmentSlot.HEAD,
                EntityEquipmentSlot.CHEST,
                EntityEquipmentSlot.LEGS,
                EntityEquipmentSlot.FEET
        };

        List<EntityEquipmentSlot> wornArmor = new ArrayList<>();
        for (EntityEquipmentSlot slot : armorSlots) {
            ItemStack armor = target.getItemStackFromSlot(slot);
            if (!armor.isEmpty()) {
                wornArmor.add(slot);
            }
        }

        if (!wornArmor.isEmpty()) {
            EntityEquipmentSlot slotToDrop = wornArmor.get(RANDOM.nextInt(wornArmor.size()));
            ItemStack armorToDrop = target.getItemStackFromSlot(slotToDrop).copy();

            target.setItemStackToSlot(slotToDrop, ItemStack.EMPTY);

            EntityItem entityItem = new EntityItem(target.world,
                    target.posX, target.posY + target.height, target.posZ,
                    armorToDrop);
            entityItem.setPickupDelay(10);
            entityItem.motionX = (RANDOM.nextDouble() - 0.5) * 0.3;
            entityItem.motionY = 0.2 + RANDOM.nextDouble() * 0.2;
            entityItem.motionZ = (RANDOM.nextDouble() - 0.5) * 0.3;

            target.world.spawnEntity(entityItem);
        }
    }

    private void spawnBackstabParticles(EntityLivingBase target) {
        if (target.world.isRemote) {
            for (int i = 0; i < 8; i++) {
                target.world.spawnParticle(EnumParticleTypes.CRIT_MAGIC,
                        target.posX + (RANDOM.nextDouble() - 0.5) * target.width,
                        target.posY + target.height * 0.5 + RANDOM.nextDouble() * target.height * 0.5,
                        target.posZ + (RANDOM.nextDouble() - 0.5) * target.width,
                        0, 0.1, 0);
            }
        }
    }

    private void spawnCritParticles(EntityLivingBase target) {
        if (target.world.isRemote) {
            for (int i = 0; i < 10; i++) {
                target.world.spawnParticle(EnumParticleTypes.CRIT,
                        target.posX + (RANDOM.nextDouble() - 0.5) * target.width,
                        target.posY + target.height * 0.5 + RANDOM.nextDouble() * target.height * 0.5,
                        target.posZ + (RANDOM.nextDouble() - 0.5) * target.width,
                        (RANDOM.nextDouble() - 0.5) * 0.5, 0.2, (RANDOM.nextDouble() - 0.5) * 0.5);
            }
        }
    }
}


