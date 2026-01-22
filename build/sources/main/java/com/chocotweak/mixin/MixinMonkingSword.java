package com.chocotweak.mixin;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import com.chocotweak.potion.PotionRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * 猴王剑特殊效果 Mixin
 * - 范围攻击 (3格范围)
 * - 范围晕眩 3 秒
 * - 中等击退
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.items.swords.ItemBaseBroadSword", remap = false)
public class MixinMonkingSword {

    private static final double ATTACK_RANGE = 5.0; // 5格范围
    private static final int STUN_DURATION = 100; // 5 秒 = 100 ticks
    private static final float KNOCKBACK_STRENGTH = 1.5f; // 击退强度

    private static boolean isItem(Item item, String registryName) {
        ResourceLocation loc = item.getRegistryName();
        return loc != null && loc.toString().equals(registryName);
    }

    @Inject(method = "hitEntity", at = @At("HEAD"))
    private void onHitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker,
            CallbackInfoReturnable<Boolean> cir) {

        // 只对猴王剑生效
        if (!isItem(stack.getItem(), "chocolatequest:swordmonking")) {
            return;
        }

        // 服务端执行范围攻击
        if (!attacker.world.isRemote) {
            performAreaAttack(stack, target, attacker);
        }
    }

    private void performAreaAttack(ItemStack stack, EntityLivingBase mainTarget, EntityLivingBase attacker) {
        // 获取范围内所有实体
        AxisAlignedBB areaBox = attacker.getEntityBoundingBox().grow(ATTACK_RANGE);
        List<Entity> entities = attacker.world.getEntitiesWithinAABBExcludingEntity(attacker, areaBox);

        float baseDamage = 5.0f; // 范围伤害（比主目标低）

        for (Entity entity : entities) {
            // 跳过主目标（已经被正常攻击逻辑处理）
            if (entity == mainTarget) {
                continue;
            }

            // 只对生物生效
            if (!(entity instanceof EntityLivingBase)) {
                continue;
            }

            EntityLivingBase livingEntity = (EntityLivingBase) entity;

            // 不攻击同队友（如果攻击者是玩家）
            if (attacker instanceof EntityPlayer && livingEntity instanceof EntityPlayer) {
                EntityPlayer attackerPlayer = (EntityPlayer) attacker;
                EntityPlayer targetPlayer = (EntityPlayer) livingEntity;
                if (attackerPlayer.isOnSameTeam(targetPlayer)) {
                    continue;
                }
            }

            // 施加范围伤害
            livingEntity.attackEntityFrom(DamageSource.causeMobDamage(attacker), baseDamage);

            // 施加晕眩效果
            if (PotionRegistry.STUN != null) {
                livingEntity.addPotionEffect(new PotionEffect(PotionRegistry.STUN, STUN_DURATION, 0));
            }

            // 施加击退
            applyKnockback(attacker, livingEntity);
        }

        // 主目标也施加晕眩和击退
        if (PotionRegistry.STUN != null) {
            mainTarget.addPotionEffect(new PotionEffect(PotionRegistry.STUN, STUN_DURATION, 0));
        }
        applyKnockback(attacker, mainTarget);
    }

    private void applyKnockback(EntityLivingBase attacker, EntityLivingBase target) {
        // 计算击退方向
        double dx = target.posX - attacker.posX;
        double dz = target.posZ - attacker.posZ;
        double length = Math.sqrt(dx * dx + dz * dz);

        if (length > 0) {
            dx /= length;
            dz /= length;

            // 应用击退
            target.motionX += dx * KNOCKBACK_STRENGTH;
            target.motionY += 0.2; // 轻微上抬
            target.motionZ += dz * KNOCKBACK_STRENGTH;
            target.velocityChanged = true;
        }
    }
}


