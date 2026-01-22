package com.chocotweak.handler;

import com.chocolate.chocolateQuest.ChocolateQuest;
import com.chocotweak.compat.BaublesCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 云靴五连跳处理器
 * 
 * 穿戴云靴时可以在空中进行最多5次额外跳跃
 */
@Mod.EventBusSubscriber(modid = "chocotweak")
public class CloudBootsMultiJumpHandler {

    // 最大额外跳跃次数
    private static final int MAX_EXTRA_JUMPS = 5;

    // 存储每个玩家的剩余额外跳跃次数
    private static Map<UUID, Integer> remainingJumps = new HashMap<>();

    // 存储跳跃按键是否被释放（用于防止长按连续跳）
    private static Map<UUID, Boolean> jumpReleased = new HashMap<>();

    // 存储上一tick是否在地面
    private static Map<UUID, Boolean> wasOnGround = new HashMap<>();

    /**
     * 检查玩家是否穿戴云靴（脚部装备或Baubles槽位）
     */
    public static boolean hasCloudBoots(EntityPlayer player) {
        // 检查脚部装备
        ItemStack boots = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
        if (!boots.isEmpty() && boots.getItem() == ChocolateQuest.cloudBoots) {
            return true;
        }

        // 检查Baubles槽位
        return BaublesCompat.hasCloudBoots(player);
    }

    /**
     * 客户端：监听跳跃按键
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;

        if (player == null)
            return;
        if (!hasCloudBoots(player))
            return;

        UUID playerId = player.getUniqueID();
        KeyBinding jumpKey = mc.gameSettings.keyBindJump;

        // 跳跃键处理
        if (jumpKey.isKeyDown()) {
            // 检查是否已释放过跳跃键（防止长按连续跳）
            Boolean released = jumpReleased.getOrDefault(playerId, true);

            if (released && !player.onGround && !player.isInWater() && !player.isOnLadder()) {
                // 在空中按下跳跃键
                int jumps = remainingJumps.getOrDefault(playerId, MAX_EXTRA_JUMPS);

                if (jumps > 0) {
                    // 执行额外跳跃
                    performExtraJump(player);
                    remainingJumps.put(playerId, jumps - 1);
                    jumpReleased.put(playerId, false);
                }
            }
        } else {
            // 跳跃键释放
            jumpReleased.put(playerId, true);
        }
    }

    /**
     * 玩家Tick：重置跳跃次数
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        EntityPlayer player = event.player;
        UUID playerId = player.getUniqueID();

        // 检查是否穿戴云靴
        if (!hasCloudBoots(player)) {
            remainingJumps.remove(playerId);
            jumpReleased.remove(playerId);
            wasOnGround.remove(playerId);
            return;
        }

        boolean wasGrounded = wasOnGround.getOrDefault(playerId, true);

        // 落地时重置跳跃次数
        if (player.onGround && !wasGrounded) {
            remainingJumps.put(playerId, MAX_EXTRA_JUMPS);
        }
        // 从地面起跳也重置
        else if (player.onGround) {
            remainingJumps.put(playerId, MAX_EXTRA_JUMPS);
        }

        wasOnGround.put(playerId, player.onGround);
    }

    /**
     * 执行额外跳跃
     */
    private static void performExtraJump(EntityPlayer player) {
        // 设置向上速度
        player.motionY = 0.42D; // 标准跳跃速度

        // 添加移动惯性（允许方向控制）
        float moveForward = player.moveForward;
        float moveStrafing = player.moveStrafing;

        if (moveForward != 0 || moveStrafing != 0) {
            float f = (float) Math.toRadians(player.rotationYaw);
            float sin = (float) Math.sin(f);
            float cos = (float) Math.cos(f);

            // 前后移动
            player.motionX += (-sin * moveForward + cos * moveStrafing) * 0.15F;
            player.motionZ += (cos * moveForward + sin * moveStrafing) * 0.15F;
        }

        // 标记速度已改变
        player.velocityChanged = true;

        // 触发跳跃音效（客户端）
        if (player.world.isRemote) {
            // 粒子效果
            for (int i = 0; i < 5; i++) {
                player.world.spawnParticle(
                        net.minecraft.util.EnumParticleTypes.CLOUD,
                        player.posX + (player.world.rand.nextDouble() - 0.5) * 0.5,
                        player.posY,
                        player.posZ + (player.world.rand.nextDouble() - 0.5) * 0.5,
                        0, -0.1, 0);
            }
        }
    }
}
