package com.chocotweak.mixin;

import com.chocotweak.ChocoTweak;
import com.chocotweak.config.TamingConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Mixin to extend DialogActionSpawnMonster with modded pet taming support.
 * 使用 @Redirect 拦截 spawnEntity 调用，避免 LocalCapture 问题
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.quest.DialogActionSpawnMonster", remap = false)
public abstract class MixinDialogActionSpawnMonster {

    @Unique
    private static final ThreadLocal<EntityPlayer> chocotweak$pendingPlayer = new ThreadLocal<>();

    /**
     * Capture the player at the start of execute()
     */
    @Inject(method = "execute", at = @At("HEAD"), require = 0)
    private void chocotweak$capturePlayer(EntityPlayer player, @Coerce Object npc, CallbackInfo ci) {
        chocotweak$pendingPlayer.set(player);
    }

    /**
     * Clear the player after execute() completes
     */
    @Inject(method = "execute", at = @At("RETURN"), require = 0)
    private void chocotweak$clearPlayer(EntityPlayer player, @Coerce Object npc, CallbackInfo ci) {
        chocotweak$pendingPlayer.remove();
    }

    /**
     * Redirect spawnEntity call to apply taming before spawning
     */
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), require = 0)
    private boolean chocotweak$onSpawnEntity(World world, Entity entity) {
        if (entity != null) {
            EntityPlayer player = chocotweak$pendingPlayer.get();
            if (player != null) {
                try {
                    TamingConfig.TamingRule rule = TamingConfig.findRuleForEntity(entity);
                    if (rule != null) {
                        ChocoTweak.LOGGER.info("Found taming rule for {} - attempting to tame",
                                entity.getClass().getSimpleName());
                        chocotweak$applyTaming(entity, player, rule);
                        ChocoTweak.LOGGER.info("Successfully tamed {} for player {}",
                                entity.getClass().getSimpleName(), player.getName());
                    }
                } catch (Exception e) {
                    ChocoTweak.LOGGER.warn("Failed to tame {}: {}",
                            entity.getClass().getSimpleName(), e.getMessage());
                }
            }
        }
        // 调用原始方法
        return world.spawnEntity(entity);
    }

    @Unique
    private void chocotweak$applyTaming(Entity entity, EntityPlayer player, TamingConfig.TamingRule rule) {
        Class<?> entityClass = entity.getClass();

        // Call tame method
        if (rule.tameMethod != null && !rule.tameMethod.isEmpty()) {
            try {
                Method tameMethod = chocotweak$findMethod(entityClass, rule.tameMethod, boolean.class);
                if (tameMethod != null) {
                    tameMethod.setAccessible(true);
                    tameMethod.invoke(entity, true);
                }
            } catch (Exception e) {
                ChocoTweak.LOGGER.debug("Tame method {} failed: {}", rule.tameMethod, e.getMessage());
            }
        }

        // Set owner
        if (rule.ownerMethod != null && !rule.ownerMethod.isEmpty()) {
            try {
                if ("UUID".equals(rule.ownerType)) {
                    Method ownerMethod = chocotweak$findMethod(entityClass, rule.ownerMethod, UUID.class);
                    if (ownerMethod != null) {
                        ownerMethod.setAccessible(true);
                        ownerMethod.invoke(entity, player.getUniqueID());
                    }
                } else if ("EntityPlayer".equals(rule.ownerType)) {
                    Method ownerMethod = chocotweak$findMethod(entityClass, rule.ownerMethod, EntityPlayer.class);
                    if (ownerMethod != null) {
                        ownerMethod.setAccessible(true);
                        ownerMethod.invoke(entity, player);
                    }
                }
            } catch (Exception e) {
                ChocoTweak.LOGGER.debug("Owner method {} failed: {}", rule.ownerMethod, e.getMessage());
            }
        }

        // Post-tame method
        if (rule.postTameMethod != null && !rule.postTameMethod.isEmpty()) {
            try {
                Method postMethod = chocotweak$findMethod(entityClass, rule.postTameMethod);
                if (postMethod != null) {
                    postMethod.setAccessible(true);
                    postMethod.invoke(entity);
                }
            } catch (Exception e) {
                ChocoTweak.LOGGER.debug("Post-tame method {} failed: {}", rule.postTameMethod, e.getMessage());
            }
        }
    }

    @Unique
    private Method chocotweak$findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredMethod(name, paramTypes);
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
