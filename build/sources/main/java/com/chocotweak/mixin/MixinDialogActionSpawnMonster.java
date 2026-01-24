package com.chocotweak.mixin;

import com.chocotweak.ChocoTweak;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Mixin to extend DialogActionSpawnMonster with modded pet taming support.
 * 检查 instanceof Tameable 接口来驯服模组生物
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.quest.DialogActionSpawnMonster", remap = false)
public abstract class MixinDialogActionSpawnMonster {

    @Unique
    private static boolean chocotweak$eventRegistered = false;

    @Inject(method = "execute", at = @At("HEAD"), require = 0)
    private void chocotweak$onExecuteHead(EntityPlayer player, @Coerce Object npc, CallbackInfo ci) {
        if (!chocotweak$eventRegistered) {
            MinecraftForge.EVENT_BUS.register(new TameableHandler());
            chocotweak$eventRegistered = true;
        }
        TameableHandler.pendingPlayer = player;
    }

    @Inject(method = "execute", at = @At("RETURN"), require = 0)
    private void chocotweak$onExecuteReturn(EntityPlayer player, @Coerce Object npc, CallbackInfo ci) {
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            TameableHandler.pendingPlayer = null;
        }).start();
    }

    public static class TameableHandler {
        static EntityPlayer pendingPlayer = null;

        // 模组常用的 Tameable 接口类名
        private static Class<?> iceAndFireTameable = null;
        private static Class<?> lycanitesTameable = null;
        private static boolean interfacesLoaded = false;

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void onEntityJoinWorld(EntityJoinWorldEvent event) {
            Entity entity = event.getEntity();
            EntityPlayer player = pendingPlayer;
            if (player == null || entity == null || entity.world.isRemote)
                return;

            // EntityTameable 由 CQ 原版处理
            if (entity instanceof EntityTameable)
                return;

            // AbstractHorse 需要特殊处理驯服
            if (entity instanceof AbstractHorse) {
                tryTameHorse((AbstractHorse) entity, player);
                return;
            }

            loadModInterfaces();
            tryTameModdedEntity(entity, player);
        }

        private void tryTameHorse(AbstractHorse horse, EntityPlayer player) {
            try {
                // 驯服马匹
                horse.setTamedBy(player);
                horse.setHorseTamed(true);

                ChocoTweak.LOGGER.info("[ChocoTweak] Tamed horse {} for {}",
                        horse.getClass().getSimpleName(), player.getName());
            } catch (Exception e) {
                ChocoTweak.LOGGER.warn("[ChocoTweak] Failed to tame horse: {}", e.getMessage());
            }
        }

        private void loadModInterfaces() {
            if (interfacesLoaded)
                return;
            interfacesLoaded = true;

            // Ice and Fire
            try {
                iceAndFireTameable = Class
                        .forName("com.github.alexthe666.iceandfire.entity.util.IBlacklistedFromStatues");
                // 实际上 Ice and Fire 的龙等继承 EntityTameable，所以用父类检测
            } catch (ClassNotFoundException ignored) {
            }

            // Lycanites Mobs
            try {
                lycanitesTameable = Class.forName("com.lycanitesmobs.core.entity.TameableCreatureEntity");
            } catch (ClassNotFoundException ignored) {
            }
        }

        private void tryTameModdedEntity(Entity entity, EntityPlayer player) {
            Class<?> entityClass = entity.getClass();
            boolean tamed = false;

            // 1. 检查是否实现 IEntityOwnable 接口（原版接口）
            if (entity instanceof IEntityOwnable) {
                tamed = trySetOwner(entity, entityClass, player);
            }

            // 2. 检查 Lycanites Mobs
            if (!tamed && lycanitesTameable != null && lycanitesTameable.isInstance(entity)) {
                tamed = tryLycanitesTaming(entity, player);
            }

            // 3. 通用反射检测 - 查找 setTamed 方法
            if (!tamed) {
                tamed = tryGenericTaming(entity, entityClass, player);
            }

            if (tamed) {
                ChocoTweak.LOGGER.info("[ChocoTweak] Tamed {} for {}",
                        entityClass.getSimpleName(), player.getName());
            }
        }

        private boolean trySetOwner(Entity entity, Class<?> clazz, EntityPlayer player) {
            // 尝试 setOwnerId(UUID)
            try {
                Method m = findMethod(clazz, "setOwnerId", UUID.class);
                if (m != null) {
                    m.invoke(entity, player.getUniqueID());
                    return true;
                }
            } catch (Exception ignored) {
            }

            // 尝试 setOwnerUniqueId(UUID)
            try {
                Method m = findMethod(clazz, "setOwnerUniqueId", UUID.class);
                if (m != null) {
                    m.invoke(entity, player.getUniqueID());
                    return true;
                }
            } catch (Exception ignored) {
            }

            return false;
        }

        private boolean tryLycanitesTaming(Entity entity, EntityPlayer player) {
            try {
                Method m = findMethod(entity.getClass(), "setPlayerOwner", EntityPlayer.class);
                if (m != null) {
                    m.invoke(entity, player);
                    return true;
                }
            } catch (Exception ignored) {
            }
            return false;
        }

        private boolean tryGenericTaming(Entity entity, Class<?> clazz, EntityPlayer player) {
            boolean tamed = false;

            // 尝试 setTamed(true)
            try {
                Method m = findMethod(clazz, "setTamed", boolean.class);
                if (m != null) {
                    m.invoke(entity, true);
                    tamed = true;
                }
            } catch (Exception ignored) {
            }

            // 尝试设置主人
            if (tamed) {
                trySetOwner(entity, clazz, player);
            }

            return tamed;
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
}
