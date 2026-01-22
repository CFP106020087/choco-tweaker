package com.chocotweak.mixin;

import com.chocotweak.ChocoTweak;
import com.chocotweak.config.TamingConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Mixin to extend DialogActionSpawnMonster with modded pet taming support.
 * 
 * This injects AFTER the vanilla EntityTameable check but BEFORE
 * world.spawnEntity(),
 * allowing us to tame modded entities (Ice and Fire dragons, Lycanites Mobs,
 * etc.)
 * using reflection.
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.quest.DialogActionSpawnMonster", remap = false)
public abstract class MixinDialogActionSpawnMonster {

    /**
     * Temporary storage for the player who triggered the spawn.
     * Set by the inject at HEAD, used by inject before spawnEntity.
     */
    private static ThreadLocal<EntityPlayer> pendingPlayer = new ThreadLocal<>();

    /**
     * Capture the player at the start of execute()
     */
    @Inject(method = "execute", at = @At("HEAD"))
    private void capturePlayer(EntityPlayer player, Object npc, CallbackInfo ci) {
        pendingPlayer.set(player);
    }

    /**
     * Clear the player after execute() completes
     */
    @Inject(method = "execute", at = @At("RETURN"))
    private void clearPlayer(EntityPlayer player, Object npc, CallbackInfo ci) {
        pendingPlayer.remove();
    }

    /**
     * Inject before world.spawnEntity() to tame modded entities.
     * 
     * We hook into the INVOKE of World.spawnEntity and try to tame the entity
     * if it matches any of our configured taming rules.
     */
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), locals = LocalCapture.CAPTURE_FAILHARD, require = 0 // Don't
                                                                                                                                                                                                   // crash
                                                                                                                                                                                                   // if
                                                                                                                                                                                                   // injection
                                                                                                                                                                                                   // fails
    )
    private void onBeforeSpawnEntity(EntityPlayer player, Object npc, CallbackInfo ci,
            Object tag, Object entitytag, Object coords, Entity entity) {

        if (entity == null)
            return;

        // Check if entity matches any taming rule
        TamingConfig.TamingRule rule = TamingConfig.findRuleForEntity(entity);

        if (rule != null) {
            ChocoTweak.LOGGER.info("Found taming rule for {} - attempting to tame",
                    entity.getClass().getSimpleName());

            try {
                applyTaming(entity, player, rule);
                ChocoTweak.LOGGER.info("Successfully tamed {} for player {}",
                        entity.getClass().getSimpleName(), player.getName());
            } catch (Exception e) {
                ChocoTweak.LOGGER.warn("Failed to tame {}: {}",
                        entity.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    /**
     * Apply taming using reflection based on the rule configuration.
     */
    private void applyTaming(Entity entity, EntityPlayer player, TamingConfig.TamingRule rule)
            throws Exception {

        Class<?> entityClass = entity.getClass();

        // Call tame method (usually setTamed(true))
        if (rule.tameMethod != null && !rule.tameMethod.isEmpty()) {
            try {
                Method tameMethod = findMethod(entityClass, rule.tameMethod, boolean.class);
                if (tameMethod != null) {
                    tameMethod.setAccessible(true);
                    tameMethod.invoke(entity, true);
                    ChocoTweak.LOGGER.debug("Called {}(true)", rule.tameMethod);
                }
            } catch (NoSuchMethodException e) {
                ChocoTweak.LOGGER.debug("Tame method {} not found, skipping", rule.tameMethod);
            }
        }

        // Set owner based on configured type
        if (rule.ownerMethod != null && !rule.ownerMethod.isEmpty()) {
            try {
                if ("UUID".equals(rule.ownerType)) {
                    Method ownerMethod = findMethod(entityClass, rule.ownerMethod, UUID.class);
                    if (ownerMethod != null) {
                        ownerMethod.setAccessible(true);
                        ownerMethod.invoke(entity, player.getUniqueID());
                        ChocoTweak.LOGGER.debug("Called {}(UUID)", rule.ownerMethod);
                    }
                } else if ("EntityPlayer".equals(rule.ownerType)) {
                    Method ownerMethod = findMethod(entityClass, rule.ownerMethod, EntityPlayer.class);
                    if (ownerMethod != null) {
                        ownerMethod.setAccessible(true);
                        ownerMethod.invoke(entity, player);
                        ChocoTweak.LOGGER.debug("Called {}(EntityPlayer)", rule.ownerMethod);
                    }
                }
            } catch (NoSuchMethodException e) {
                ChocoTweak.LOGGER.debug("Owner method {} not found, skipping", rule.ownerMethod);
            }
        }

        // Call post-tame method if configured
        if (rule.postTameMethod != null && !rule.postTameMethod.isEmpty()) {
            try {
                Method postMethod = findMethod(entityClass, rule.postTameMethod);
                if (postMethod != null) {
                    postMethod.setAccessible(true);
                    postMethod.invoke(entity);
                    ChocoTweak.LOGGER.debug("Called {}()", rule.postTameMethod);
                }
            } catch (NoSuchMethodException e) {
                ChocoTweak.LOGGER.debug("Post-tame method {} not found, skipping", rule.postTameMethod);
            }
        }
    }

    /**
     * Find a method in the class hierarchy, checking superclasses.
     */
    private Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes)
            throws NoSuchMethodException {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredMethod(name, paramTypes);
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchMethodException(name);
    }
}


