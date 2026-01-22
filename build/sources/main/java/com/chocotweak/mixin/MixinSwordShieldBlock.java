package com.chocotweak.mixin;

import com.chocotweak.weapon.WeaponEventHandler;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 剑盾格挡特效Mixin
 * 拦截onBlock方法以添加格挡时的特殊效果
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.items.swords.ItemSwordAndShieldBase", remap = false)
public class MixinSwordShieldBlock {

    private static final String SWORD_SPIDER = "chocolatequest:swordspider";
    private static final String SWORD_SUNLIGHT = "chocolatequest:defensesword";
    private static final String SWORD_MOONLIGHT = "chocolatequest:moonsword";
    private static final String SWORD_WALKER = "chocolatequest:walkersword";

    @Inject(method = "onBlock", at = @At("HEAD"))
    private void onBlockEnhanced(EntityLivingBase blockingEntity, EntityLivingBase attackerEntity, DamageSource ds,
            CallbackInfo ci) {
        if (!(blockingEntity instanceof EntityPlayer))
            return;

        EntityPlayer player = (EntityPlayer) blockingEntity;
        ItemStack mainHand = player.getHeldItemMainhand();
        if (mainHand.isEmpty())
            return;

        ResourceLocation regName = mainHand.getItem().getRegistryName();
        if (regName == null)
            return;

        String itemId = regName.toString();

        if (itemId.equals(SWORD_SPIDER)) {
            WeaponEventHandler.applyRandomDebuff(attackerEntity);
        } else if (itemId.equals(SWORD_SUNLIGHT)) {
            long time = player.world.getWorldTime() % 24000;
            if (time < 12000) {
                attackerEntity.setFire(30);
            }
        } else if (itemId.equals(SWORD_MOONLIGHT)) {
            // 月光剑：夜晚时使敌人陷入混乱，攻击周围生物
            long time = player.world.getWorldTime() % 24000;
            if (time >= 12000) {
                applyChaosEffect(player, attackerEntity);
            }
        } else if (itemId.equals(SWORD_WALKER)) {
            // 行者剑：冻结一定范围内所有生物的移动
            applyAbyssFreezeEffect(player, attackerEntity);
        }
    }

    /**
     * 月光剑效果：使目标陷入混乱，攻击周围任意生物（包括同类）
     */
    private void applyChaosEffect(EntityPlayer player, EntityLivingBase target) {
        if (player.world.isRemote)
            return;

        // 施加负面效果
        target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 1));
        target.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 100, 0));

        // 标记混乱状态
        target.getEntityData().setLong("chocotweak_chaos_end", System.currentTimeMillis() + 8000);

        // 如果是生物，尝试修改其攻击目标
        if (target instanceof EntityCreature) {
            EntityCreature creature = (EntityCreature) target;

            // 寻找附近的其他生物作为新目标
            List<EntityLivingBase> nearbyMobs = player.world.getEntitiesWithinAABB(
                    EntityLivingBase.class,
                    target.getEntityBoundingBox().grow(10.0),
                    entity -> entity != target && entity != player && entity.isEntityAlive());

            if (!nearbyMobs.isEmpty()) {
                // 随机选择一个目标
                EntityLivingBase newTarget = nearbyMobs.get(player.world.rand.nextInt(nearbyMobs.size()));
                creature.setAttackTarget(newTarget);
                creature.setRevengeTarget(newTarget);
            }
        }
    }

    /**
     * 行者剑效果：冻结一定范围内所有生物的移动（motion=0）
     */
    private void applyAbyssFreezeEffect(EntityPlayer player, EntityLivingBase attacker) {
        if (player.world.isRemote)
            return;

        double range = 8.0;
        AxisAlignedBB area = player.getEntityBoundingBox().grow(range);

        List<EntityLivingBase> nearbyEntities = player.world.getEntitiesWithinAABB(
                EntityLivingBase.class,
                area,
                entity -> entity != player && entity.isEntityAlive());

        for (EntityLivingBase entity : nearbyEntities) {
            // 冻结移动
            entity.motionX = 0;
            entity.motionY = 0;
            entity.motionZ = 0;
            entity.velocityChanged = true;

            // 标记深渊冻结状态
            entity.getEntityData().setLong("chocotweak_abyss_freeze", System.currentTimeMillis() + 3000);

            // 施加缓慢效果
            entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 127)); // 127级缓慢 = 无法移动
        }
    }
}