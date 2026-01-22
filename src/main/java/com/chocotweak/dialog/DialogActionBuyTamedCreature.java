package com.chocotweak.dialog;

import com.chocotweak.ChocoTweak;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 购买驯服生物对话动作
 * 生成一个已驯服的生物，主人设为购买玩家
 */
public class DialogActionBuyTamedCreature {

    /**
     * 执行购买驯服生物
     * 
     * @param player   购买的玩家
     * @param entityId 生物的注册名 (如 "iceandfire:firedragon")
     * @param world    世界
     * @return 是否成功
     */
    public static boolean execute(EntityPlayer player, String entityId, World world) {
        try {
            ResourceLocation location = new ResourceLocation(entityId);
            Entity entity = EntityList.createEntityByIDFromName(location, world);

            if (entity == null) {
                ChocoTweak.LOGGER.error("Failed to create entity: {}", entityId);
                return false;
            }

            // 设置位置到玩家附近
            entity.setPosition(player.posX + 2, player.posY, player.posZ + 2);

            // 尝试驯服生物
            boolean tamed = tryTame(entity, player);

            if (!tamed) {
                ChocoTweak.LOGGER.warn("Could not tame entity: {}", entityId);
            }

            // 生成实体
            world.spawnEntity(entity);

            ChocoTweak.LOGGER.info("Spawned tamed creature {} for player {}", entityId, player.getName());
            return true;

        } catch (Exception e) {
            ChocoTweak.LOGGER.error("Error spawning creature: " + entityId, e);
            return false;
        }
    }

    /**
     * 尝试驯服生物
     */
    private static boolean tryTame(Entity entity, EntityPlayer player) {
        UUID playerUUID = player.getUniqueID();

        // 方法1: 尝试 setTamed + setOwnerId (适用于原版 EntityTameable)
        try {
            Method setTamed = entity.getClass().getMethod("setTamed", boolean.class);
            Method setOwnerId = entity.getClass().getMethod("setOwnerId", UUID.class);
            setTamed.invoke(entity, true);
            setOwnerId.invoke(entity, playerUUID);
            return true;
        } catch (Exception ignored) {
        }

        // 方法2: 尝试 setTamedBy (适用于某些模组)
        try {
            Method setTamedBy = entity.getClass().getMethod("setTamedBy", EntityPlayer.class);
            setTamedBy.invoke(entity, player);
            return true;
        } catch (Exception ignored) {
        }

        // 方法3: 尝试 Ice and Fire 龙
        try {
            Method setOwner = entity.getClass().getMethod("setOwner", EntityPlayer.class);
            setOwner.invoke(entity, player);
            return true;
        } catch (Exception ignored) {
        }

        // 方法4: 尝试直接设置 NBT
        try {
            if (entity instanceof EntityLiving) {
                NBTTagCompound nbt = new NBTTagCompound();
                entity.writeToNBT(nbt);
                nbt.setBoolean("Tame", true);
                nbt.setString("Owner", player.getName());
                nbt.setString("OwnerUUID", playerUUID.toString());
                entity.readFromNBT(nbt);
                return true;
            }
        } catch (Exception ignored) {
        }

        return false;
    }

    /**
     * 从 NBT 创建驯服的生物
     */
    public static Entity createFromNBT(NBTTagCompound tag, World world, EntityPlayer owner) {
        try {
            String entityId = tag.getString("EntityId");
            ResourceLocation location = new ResourceLocation(entityId);
            Entity entity = EntityList.createEntityByIDFromName(location, world);

            if (entity == null)
                return null;

            // 读取自定义 NBT
            if (tag.hasKey("CustomNBT")) {
                entity.readFromNBT(tag.getCompoundTag("CustomNBT"));
            }

            // 驯服
            tryTame(entity, owner);

            return entity;
        } catch (Exception e) {
            ChocoTweak.LOGGER.error("Error creating creature from NBT", e);
            return null;
        }
    }
}
