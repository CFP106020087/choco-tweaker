package com.chocotweak.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Mixin to prevent CQ NPCs from attacking tamed creatures
 * 让 CQ 佣兵不攻击被驯服的生物（与 MixinDialogActionSpawnMonster 配合使用）
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.entity.mob.EntityHumanMob", remap = false)
public abstract class MixinNpcTargetFilter {

    // Lycanites Mobs 基类（懒加载）
    private static Class<?> lycanitesTameable = null;
    private static boolean lycanitesChecked = false;

    /**
     * 在 canAttackEntity 方法中检查目标是否为友方驯服生物
     */
    @Inject(method = "canAttackEntity", at = @At("HEAD"), cancellable = true, require = 0)
    private void onCanAttackEntity(EntityLivingBase target, CallbackInfoReturnable<Boolean> cir) {
        if (target == null)
            return;

        try {
            // 获取 this 的主人
            EntityPlayer myOwner = getOwnerPlayer((Entity) (Object) this);
            if (myOwner == null)
                return;

            // 检查目标是否属于同一主人
            EntityPlayer targetOwner = getOwnerPlayer(target);
            if (targetOwner != null && targetOwner.equals(myOwner)) {
                // 目标属于同一主人，不攻击
                cir.setReturnValue(false);
                return;
            }

            // 检查目标是否为主人本人
            if (target instanceof EntityPlayer && target.equals(myOwner)) {
                cir.setReturnValue(false);
                return;
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 在 setAttackTarget 中过滤掉友方目标
     */
    @Inject(method = "setAttackTarget", at = @At("HEAD"), cancellable = true, require = 0)
    private void onSetAttackTarget(EntityLivingBase target,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (target == null)
            return;

        try {
            EntityPlayer myOwner = getOwnerPlayer((Entity) (Object) this);
            if (myOwner == null)
                return;

            EntityPlayer targetOwner = getOwnerPlayer(target);
            if (targetOwner != null && targetOwner.equals(myOwner)) {
                // 取消设置攻击目标
                ci.cancel();
                return;
            }

            if (target instanceof EntityPlayer && target.equals(myOwner)) {
                ci.cancel();
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 获取实体的主人玩家
     */
    private EntityPlayer getOwnerPlayer(Entity entity) {
        // 1. 检查 IEntityOwnable 接口
        if (entity instanceof IEntityOwnable) {
            Entity owner = ((IEntityOwnable) entity).getOwner();
            if (owner instanceof EntityPlayer) {
                return (EntityPlayer) owner;
            }
        }

        // 2. 检查 Lycanites Mobs
        if (!lycanitesChecked) {
            lycanitesChecked = true;
            try {
                lycanitesTameable = Class.forName("com.lycanitesmobs.core.entity.TameableCreatureEntity");
            } catch (ClassNotFoundException ignored) {
            }
        }

        if (lycanitesTameable != null && lycanitesTameable.isInstance(entity)) {
            try {
                Method getOwnerMethod = findMethod(entity.getClass(), "getPlayerOwner");
                if (getOwnerMethod != null) {
                    Object owner = getOwnerMethod.invoke(entity);
                    if (owner instanceof EntityPlayer) {
                        return (EntityPlayer) owner;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // 3. 通用反射检测
        try {
            // 尝试 getOwner()
            Method m = findMethod(entity.getClass(), "getOwner");
            if (m != null) {
                Object owner = m.invoke(entity);
                if (owner instanceof EntityPlayer) {
                    return (EntityPlayer) owner;
                }
            }

            // 尝试 getOwnerId()
            m = findMethod(entity.getClass(), "getOwnerId");
            if (m != null) {
                Object ownerId = m.invoke(entity);
                if (ownerId instanceof UUID) {
                    // 在世界中查找玩家
                    for (EntityPlayer player : entity.world.playerEntities) {
                        if (player.getUniqueID().equals(ownerId)) {
                            return player;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private Method findMethod(Class<?> clazz, String name, Class<?>... params) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                Method m = current.getDeclaredMethod(name, params);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
