package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.ChocolateQuest;
import com.chocolate.chocolateQuest.block.BlockMobSpawnerTileEntity;
import com.chocolate.chocolateQuest.entity.EntityHumanBase;
import com.chocolate.chocolateQuest.entity.npc.EntityGolemMecha;
import com.chocolate.chocolateQuest.items.mobControl.ItemMobToSpawner;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin 扩展 ItemMobToSpawner 支持按住 Shift 左键存入多个生物
 * 
 * 功能：
 * 1. Shift+左键：将生物添加到队列（不立即创建刷怪笼）
 * 2. 显示当前队列中的生物数量
 * 3. 普通左键：创建包含所有队列生物的刷怪笼（或单个生物）
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.items.mobControl.ItemMobToSpawner", remap = false)
public abstract class MixinItemMobToSpawnerMulti {

    @Unique
    private static final String NBT_MOB_QUEUE = "ChocoTweakMobQueue";

    /**
     * 拦截 onLeftClickEntity，Shift+左键时将生物加入队列
     */
    @Inject(method = "onLeftClickEntity", at = @At("HEAD"), cancellable = true)
    private void onLeftClickEntityMulti(ItemStack stack, EntityPlayer player, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity.world.isRemote) {
            return;
        }

        // Shift+左键：加入队列模式
        if (player.isSneaking()) {
            // 处理机甲骑手
            if (entity instanceof EntityGolemMecha && entity.isBeingRidden()) {
                Entity rider = entity.getPassengers().get(0);
                if (rider instanceof EntityHumanBase) {
                    entity = rider;
                }
            }

            // 获取或创建 NBT 队列
            NBTTagCompound stackTag = stack.getTagCompound();
            if (stackTag == null) {
                stackTag = new NBTTagCompound();
                stack.setTagCompound(stackTag);
            }

            NBTTagList mobQueue = stackTag.getTagList(NBT_MOB_QUEUE, 10);

            // 保存生物 NBT
            NBTTagCompound mobData = new NBTTagCompound();
            boolean wrote;
            
            if (entity instanceof EntityHumanBase) {
                EntityHumanBase human = (EntityHumanBase) entity;
                int x = MathHelper.floor(entity.posX);
                int y = MathHelper.floor(entity.posY);
                int z = MathHelper.floor(entity.posZ);
                human.writeToNBTOptional(mobData);
                human.writeEntityToSpawnerNBT(mobData, x, y, z);
                wrote = true;
            } else {
                wrote = entity.writeToNBTOptional(mobData);
            }

            if (wrote) {
                // 清理 UUID 等
                mobData.removeTag("Age");
                mobData.removeTag("UUIDMost");
                mobData.removeTag("UUIDLeast");
                mobData.removeTag("UUID");
                mobData.removeTag("Dimension");
                mobData.removeTag("Pos");

                // 添加到队列
                mobQueue.appendTag(mobData);
                stackTag.setTag(NBT_MOB_QUEUE, mobQueue);

                // 发送消息
                String entityName = entity.getName();
                player.sendStatusMessage(
                    new TextComponentString(TextFormatting.GREEN + "已添加 " + TextFormatting.WHITE + entityName 
                        + TextFormatting.GREEN + " 到队列 (" + TextFormatting.YELLOW + mobQueue.tagCount() 
                        + TextFormatting.GREEN + " 个生物)"), true);

                // 移除原实体
                if (entity instanceof EntityHumanBase) {
                    EntityHumanBase human = (EntityHumanBase) entity;
                    if (human.getRidingEntity() != null) {
                        human.getRidingEntity().setDead();
                    }
                }
                entity.setDead();
            } else {
                player.sendStatusMessage(
                    new TextComponentString(TextFormatting.RED + "无法保存此生物!"), true);
            }

            cir.setReturnValue(true);
            return;
        }

        // 非 Shift：检查是否有队列，如果有则创建多生物刷怪笼
        NBTTagCompound stackTag = stack.getTagCompound();
        if (stackTag != null && stackTag.hasKey(NBT_MOB_QUEUE)) {
            NBTTagList mobQueue = stackTag.getTagList(NBT_MOB_QUEUE, 10);
            if (mobQueue.tagCount() > 0) {
                // 先将当前点击的生物也添加到队列
                if (entity instanceof EntityGolemMecha && entity.isBeingRidden()) {
                    Entity rider = entity.getPassengers().get(0);
                    if (rider instanceof EntityHumanBase) {
                        entity = rider;
                    }
                }

                NBTTagCompound mobData = new NBTTagCompound();
                boolean wrote;
                
                if (entity instanceof EntityHumanBase) {
                    EntityHumanBase human = (EntityHumanBase) entity;
                    int x = MathHelper.floor(entity.posX);
                    int y = MathHelper.floor(entity.posY);
                    int z = MathHelper.floor(entity.posZ);
                    human.writeToNBTOptional(mobData);
                    human.writeEntityToSpawnerNBT(mobData, x, y, z);
                    wrote = true;
                } else {
                    wrote = entity.writeToNBTOptional(mobData);
                }

                if (wrote) {
                    mobData.removeTag("Age");
                    mobData.removeTag("UUIDMost");
                    mobData.removeTag("UUIDLeast");
                    mobData.removeTag("UUID");
                    mobData.removeTag("Dimension");
                    mobData.removeTag("Pos");
                    mobQueue.appendTag(mobData);
                }

                // 创建多生物刷怪笼
                int x = MathHelper.floor(entity.posX);
                int y = MathHelper.floor(entity.posY);
                int z = MathHelper.floor(entity.posZ);
                
                boolean success = chocotweak$createMultiMobSpawner(entity.world, x, y, z, mobQueue);
                
                if (success) {
                    // 清空队列
                    stackTag.removeTag(NBT_MOB_QUEUE);
                    
                    // 移除当前实体
                    if (entity instanceof EntityHumanBase) {
                        EntityHumanBase human = (EntityHumanBase) entity;
                        if (human.getRidingEntity() != null) {
                            human.getRidingEntity().setDead();
                        }
                    }
                    entity.setDead();
                    
                    player.sendStatusMessage(
                        new TextComponentString(TextFormatting.GREEN + "已创建包含 " + TextFormatting.YELLOW 
                            + mobQueue.tagCount() + TextFormatting.GREEN + " 个生物的刷怪笼!"), true);
                }

                cir.setReturnValue(true);
                return;
            }
        }

        // 没有队列，让原方法处理
    }

    /**
     * 创建包含多个生物的刷怪笼
     */
    @Unique
    private static boolean chocotweak$createMultiMobSpawner(World world, int x, int y, int z, NBTTagList mobList) {
        BlockPos pos = new BlockPos(x, y, z);
        if (!world.isAirBlock(pos)) {
            ++y;
            pos = new BlockPos(x, y, z);
        }

        world.setBlockState(pos, ChocolateQuest.spawner.getDefaultState());
        BlockMobSpawnerTileEntity te = new BlockMobSpawnerTileEntity();
        
        // 使用第一个生物作为主 mob
        if (mobList.tagCount() > 0) {
            te.mobNBT = mobList.getCompoundTagAt(0).copy();
            te.mob = -1;
            
            // 如果有多个生物，将剩余的存储在 Party 标签中
            if (mobList.tagCount() > 1) {
                NBTTagList partyList = new NBTTagList();
                for (int i = 1; i < mobList.tagCount(); i++) {
                    partyList.appendTag(mobList.getCompoundTagAt(i).copy());
                }
                te.mobNBT.setTag("Party", partyList);
            }
        }
        
        world.setTileEntity(pos, te);
        return true;
    }
}
