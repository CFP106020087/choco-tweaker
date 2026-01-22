package com.chocotweak.handler;

import com.chocolate.chocolateQuest.ChocolateQuest;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

/**
 * 骗术师匕首特殊效果：被攻击时有概率闪避并传送到攻击者背后
 */
@Mod.EventBusSubscriber(modid = "chocotweak")
public class TricksterDaggerHandler {

    private static final Random RANDOM = new Random();
    private static final double DODGE_CHANCE = 0.25; // 25% 闪避概率

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        EntityLivingBase victim = event.getEntityLiving();

        // 只对玩家生效
        if (!(victim instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) victim;

        // 检查是否持有骗术师匕首
        ItemStack mainHand = player.getHeldItemMainhand();
        ItemStack offHand = player.getHeldItemOffhand();

        boolean hasTricksterDagger = (!mainHand.isEmpty() && mainHand.getItem() == ChocolateQuest.tricksterDagger) ||
                (!offHand.isEmpty() && offHand.getItem() == ChocolateQuest.tricksterDagger);

        if (!hasTricksterDagger) {
            return;
        }

        // 获取攻击者
        if (!(event.getSource().getTrueSource() instanceof EntityLivingBase)) {
            return;
        }

        EntityLivingBase attacker = (EntityLivingBase) event.getSource().getTrueSource();

        // 闪避概率检查
        if (RANDOM.nextDouble() >= DODGE_CHANCE) {
            return;
        }

        // 取消伤害
        event.setCanceled(true);

        // 传送到攻击者背后
        if (!player.world.isRemote) {
            teleportBehindTarget(player, attacker);
        }

        // 播放音效
        player.world.playSound(null, player.posX, player.posY, player.posZ,
                SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.5f);

        // 生成粒子效果（原位置）
        spawnTeleportParticles(player);
    }

    /**
     * 传送到目标背后
     */
    private static void teleportBehindTarget(EntityPlayer player, EntityLivingBase target) {
        // 计算目标背后的位置
        float yaw = target.rotationYaw;
        double rad = Math.toRadians(yaw);

        // 目标背后 1.5 格位置
        double offsetX = Math.sin(rad) * 1.5;
        double offsetZ = -Math.cos(rad) * 1.5;

        double newX = target.posX + offsetX;
        double newY = target.posY;
        double newZ = target.posZ + offsetZ;

        // 检查目标位置是否安全（不在方块内）
        if (isSafeToTeleport(player, newX, newY, newZ)) {
            player.setPositionAndUpdate(newX, newY, newZ);

            // 面向目标
            double dx = target.posX - newX;
            double dz = target.posZ - newZ;
            float newYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
            player.rotationYaw = newYaw;
            player.rotationYawHead = newYaw;
        }
    }

    /**
     * 检查传送位置是否安全
     */
    private static boolean isSafeToTeleport(EntityPlayer player, double x, double y, double z) {
        // 简单检查：位置不在固体方块内
        net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(x, y, z);
        net.minecraft.util.math.BlockPos posAbove = pos.up();

        return !player.world.getBlockState(pos).isFullBlock() &&
                !player.world.getBlockState(posAbove).isFullBlock();
    }

    /**
     * 生成传送粒子效果
     */
    private static void spawnTeleportParticles(EntityPlayer player) {
        if (!player.world.isRemote) {
            // 服务端发送粒子到客户端
            for (int i = 0; i < 32; i++) {
                double offsetX = (RANDOM.nextDouble() - 0.5) * player.width * 2;
                double offsetY = RANDOM.nextDouble() * player.height;
                double offsetZ = (RANDOM.nextDouble() - 0.5) * player.width * 2;

                ((net.minecraft.world.WorldServer) player.world).spawnParticle(
                        EnumParticleTypes.PORTAL,
                        player.posX + offsetX,
                        player.posY + offsetY,
                        player.posZ + offsetZ,
                        1, 0, 0, 0, 0.1);
            }
        }
    }
}
