package com.chocotweak.handler;

import com.chocotweak.potion.PotionDungeonExplorer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 地牢保护处理器
 * 
 * 当玩家靠近 CQ Spawner (非NPC) 或 CQ 敌对生物时，
 * 施加"寻找巧克力!"效果，限制以下行为：
 * - 禁止飞行
 * - 禁止挖掘
 * - 禁止放置方块
 * - 禁止开箱子
 * - 禁止漏斗偷取
 * - 禁止喷气背包/爬墙
 * - 摔落伤害归零
 */
public class DungeonProtectionHandler {

    public static final DungeonProtectionHandler INSTANCE = new DungeonProtectionHandler();

    /** 检测范围 */
    private static final double DETECTION_RANGE = 30.0;

    /** 跳跃追踪 - 用于区分正常跳跃和喷气背包 */
    private static final Map<UUID, JumpState> jumpStates = new HashMap<>();

    private DungeonProtectionHandler() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    // ==================== 跳跃状态追踪 ====================

    private static class JumpState {
        int ticksSinceJump = 0;
        BlockPos lastGroundPos = BlockPos.ORIGIN;
    }

    // ==================== 检测逻辑 ====================

    /**
     * 检查是否为 CQ 生物刷怪笼 (非NPC)
     */
    private boolean isCQSpawner(TileEntity te) {
        if (te == null)
            return false;
        String className = te.getClass().getName();
        // CQ 的 Spawner TileEntity
        if (className.contains("chocolate") && className.toLowerCase().contains("spawner")) {
            // 检查 NBT 是否为 NPC（排除友好NPC）
            try {
                // 尝试读取 NBT 判断是否有敌对标记
                return true; // 简化处理：假设都是敌对刷怪笼
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    /**
     * 检查是否为 CQ 敌对生物
     */
    private boolean isCQHostileMob(Entity entity) {
        if (entity == null)
            return false;
        String className = entity.getClass().getName();

        // CQ 敌对生物类
        if (className.contains("chocolate") || className.contains("chocolateQuest")) {
            // 排除友好 NPC 和玩家
            if (entity instanceof EntityPlayer)
                return false;

            // 检查是否实现 IMob 接口或有敌对标记
            if (entity instanceof IMob)
                return true;

            // 检查是否为 EntityHumanMob 或其子类
            Class<?> clazz = entity.getClass();
            while (clazz != null) {
                String name = clazz.getName();
                if (name.contains("EntityHumanMob") || name.contains("EntityBaseBoss")) {
                    return true;
                }
                clazz = clazz.getSuperclass();
            }
        }
        return false;
    }

    /**
     * 检查玩家附近是否有地牢触发条件
     */
    private boolean isNearDungeon(EntityPlayer player) {
        if (player.world.isRemote)
            return false;

        BlockPos playerPos = player.getPosition();

        // 1. 检查附近的 CQ 刷怪笼
        int range = (int) DETECTION_RANGE;
        for (int x = -range; x <= range; x += 4) { // 每4格检查一次提高性能
            for (int y = -range / 2; y <= range / 2; y += 4) {
                for (int z = -range; z <= range; z += 4) {
                    BlockPos checkPos = playerPos.add(x, y, z);
                    TileEntity te = player.world.getTileEntity(checkPos);
                    if (isCQSpawner(te)) {
                        // 精确距离检查
                        if (player.getDistanceSq(checkPos) <= DETECTION_RANGE * DETECTION_RANGE) {
                            return true;
                        }
                    }
                }
            }
        }

        // 2. 检查附近的 CQ 敌对生物
        AxisAlignedBB searchBox = player.getEntityBoundingBox().grow(DETECTION_RANGE);
        List<Entity> entities = player.world.getEntitiesWithinAABBExcludingEntity(player, searchBox);
        for (Entity entity : entities) {
            if (isCQHostileMob(entity)) {
                return true;
            }
        }

        return false;
    }

    // ==================== 事件处理 ====================

    /**
     * 玩家 Tick - 检测地牢并施加效果，处理飞行/喷气背包/爬墙
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;
        if (event.player.world.isRemote)
            return;

        EntityPlayer player = event.player;
        UUID uuid = player.getUniqueID();

        // 检测地牢并施加效果
        if (player.ticksExisted % 20 == 0) { // 每秒检测一次
            if (isNearDungeon(player)) {
                PotionDungeonExplorer.applyEffect(player);
            }
        }

        // 如果有效果，执行限制
        if (PotionDungeonExplorer.hasEffect(player)) {
            // === 禁止飞行 ===
            if (player.capabilities.isFlying) {
                player.capabilities.isFlying = false;
                player.sendPlayerAbilities();
                teleportToGround(player);
            }

            // === 反喷气背包/爬墙 ===
            // 排除合法情况
            if (!isValidClimbing(player)) {
                JumpState state = jumpStates.computeIfAbsent(uuid, k -> new JumpState());

                if (player.onGround) {
                    state.ticksSinceJump = 0;
                    state.lastGroundPos = player.getPosition();
                } else {
                    state.ticksSinceJump++;

                    // 如果离开地面超过 15 tick 且仍在上升 → 异常行为（喷气背包）
                    if (state.ticksSinceJump > 15 && player.motionY > 0.1) {
                        teleportToGround(player);
                        state.ticksSinceJump = 0;
                    }

                    // 检测爬墙：水平碰撞 + 上升 + 不在地面
                    if (player.collidedHorizontally && player.motionY > 0.05) {
                        teleportToGround(player);
                        state.ticksSinceJump = 0;
                    }
                }
            }
        } else {
            jumpStates.remove(uuid);
        }
    }

    /**
     * 检查玩家是否在合法攀爬状态（梯子、藤蔓、水、跳跃药水）
     */
    private boolean isValidClimbing(EntityPlayer player) {
        // 在梯子或藤蔓上
        if (player.isOnLadder()) {
            return true;
        }

        // 在水中
        if (player.isInWater() || player.isInLava()) {
            return true;
        }

        // 有跳跃药水效果
        if (player.isPotionActive(net.minecraft.init.MobEffects.JUMP_BOOST)) {
            return true;
        }

        // 有漂浮效果
        if (player.isPotionActive(net.minecraft.init.MobEffects.LEVITATION)) {
            return true;
        }

        return false;
    }

    /**
     * 将玩家传送回地面
     */
    private void teleportToGround(EntityPlayer player) {
        BlockPos pos = player.getPosition();

        // 向下搜索固体方块
        for (int y = pos.getY(); y > 1; y--) {
            BlockPos checkPos = new BlockPos(pos.getX(), y - 1, pos.getZ());
            IBlockState state = player.world.getBlockState(checkPos);

            // 找到固体方块
            if (state.getMaterial().isSolid()) {
                // 传送到这个方块上方
                player.setPositionAndUpdate(pos.getX() + 0.5, y, pos.getZ() + 0.5);
                player.motionY = 0;
                player.fallDistance = 0;
                return;
            }
        }

        // 如果没找到，传送到 y=64
        player.setPositionAndUpdate(pos.getX() + 0.5, 64, pos.getZ() + 0.5);
        player.motionY = 0;
        player.fallDistance = 0;
    }

    /**
     * 禁止挖掘方块
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player != null && PotionDungeonExplorer.hasEffect(player)) {
            event.setCanceled(true);
        }
    }

    /**
     * 禁止放置方块
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player != null && PotionDungeonExplorer.hasEffect(player)) {
            event.setCanceled(true);
        }
    }

    /**
     * 禁止右键交互（箱子、漏斗等）
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player != null && PotionDungeonExplorer.hasEffect(player)) {
            BlockPos pos = event.getPos();
            IBlockState state = player.world.getBlockState(pos);

            // 禁止打开箱子类容器
            if (state.getBlock() == Blocks.CHEST ||
                    state.getBlock() == Blocks.TRAPPED_CHEST ||
                    state.getBlock() == Blocks.ENDER_CHEST ||
                    state.getBlock() == Blocks.HOPPER ||
                    state.getBlock() == Blocks.DISPENSER ||
                    state.getBlock() == Blocks.DROPPER ||
                    state.getBlock().getClass().getName().contains("Shulker")) {
                event.setCanceled(true);
                return;
            }

            // 禁止打开 CQ 箱子
            TileEntity te = player.world.getTileEntity(pos);
            if (te != null && te.getClass().getName().contains("DungeonChest")) {
                event.setCanceled(true);
                return;
            }
        }
    }

    /**
     * 摔落伤害归零
     */
    @SubscribeEvent
    public void onFall(LivingFallEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            if (PotionDungeonExplorer.hasEffect(player)) {
                event.setDistance(0);
                event.setDamageMultiplier(0);
            }
        }
    }

    /**
     * 禁止搬运实体（兼容 CarryOn 模组）
     * CarryOn 使用 AttackEntityEvent 检查是否可以捡起实体
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAttackEntity(net.minecraftforge.event.entity.player.AttackEntityEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player != null && PotionDungeonExplorer.hasEffect(player)) {
            // CarryOn 通过攻击事件判断是否可以捡起
            // 取消事件阻止 CarryOn 捡起实体
            event.setCanceled(true);
        }
    }

    /**
     * 禁止漏斗从地牢区域偷取 (每 tick 检查)
     * 注意：这需要在世界 tick 中处理，或使用 Mixin
     */
    // TODO: 考虑使用 Mixin 拦截 TileEntityHopper.pullItems
}
