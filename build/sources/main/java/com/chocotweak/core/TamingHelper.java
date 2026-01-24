package com.chocotweak.core;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 驯服助手类 - 被ASM注入的代码调用
 * 自动检测并驯服所有支持驯服的模组生物
 */
public class TamingHelper {

    // Lycanites Mobs 基类 (懒加载)
    private static Class<?> lycanitesTameable = null;
    private static boolean lycanitesChecked = false;

    /**
     * 在实体生成前尝试驯服
     * 由ASM从DialogActionSpawnMonster.execute注入调用
     */
    public static void tryTameBeforeSpawn(Entity entity, EntityPlayer player) {
        if (entity == null || player == null) return;
        if (entity.world != null && entity.world.isRemote) return;

        // 原版已处理的类型跳过
        if (entity instanceof EntityTameable || entity instanceof AbstractHorse) {
            return;
        }

        try {
            boolean tamed = tryTame(entity, player);
            if (tamed) {
                System.out.println("[ChocoTweak] Tamed " + entity.getClass().getSimpleName() + " for " + player.getName());
            }
        } catch (Exception e) {
            System.out.println("[ChocoTweak] Taming failed: " + e.getMessage());
        }
    }

    private static boolean tryTame(Entity entity, EntityPlayer player) {
        Class<?> entityClass = entity.getClass();
        boolean success = false;

        // 1. 检查 Lycanites Mobs
        if (!lycanitesChecked) {
            lycanitesChecked = true;
            try {
                lycanitesTameable = Class.forName("com.lycanitesmobs.core.entity.TameableCreatureEntity");
            } catch (ClassNotFoundException ignored) {}
        }

        if (lycanitesTameable != null && lycanitesTameable.isInstance(entity)) {
            success = invokeMethod(entity, entityClass, "setPlayerOwner", EntityPlayer.class, player);
            if (success) return true;
        }

        // 2. 尝试 setTamed(true)
        boolean tamedSuccess = invokeMethod(entity, entityClass, "setTamed", boolean.class, true);

        // 3. 尝试设置主人
        if (tamedSuccess || entity instanceof IEntityOwnable) {
            success = invokeMethod(entity, entityClass, "setOwnerId", UUID.class, player.getUniqueID());
            if (!success) {
                success = invokeMethod(entity, entityClass, "setOwnerUniqueId", UUID.class, player.getUniqueID());
            }
            if (!success) {
                success = invokeMethod(entity, entityClass, "setOwnerUUID", UUID.class, player.getUniqueID());
            }
        }

        return tamedSuccess || success;
    }

    private static boolean invokeMethod(Object target, Class<?> clazz, String methodName, Class<?> paramType, Object paramValue) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                Method method = current.getDeclaredMethod(methodName, paramType);
                method.setAccessible(true);
                method.invoke(target, paramValue);
                return true;
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}
