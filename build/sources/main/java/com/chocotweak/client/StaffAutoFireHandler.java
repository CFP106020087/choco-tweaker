package com.chocotweak.client;

import com.chocolate.chocolateQuest.gui.InventoryBag;
import com.chocolate.chocolateQuest.items.ItemStaffBase;
import com.chocolate.chocolateQuest.magic.Awakements;
import com.chocotweak.ChocoTweak;
import com.chocotweak.magic.AwakementHighSpeedChant;
import com.chocotweak.mixin.MixinAwakementsRegister;
import com.chocotweak.network.ChocoNetwork;
import com.chocotweak.network.PacketClearSpellCD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 法杖连续施法处理器 - 高速神言觉醒效果
 * 
 * 根据觉醒等级决定连续施法持续时间：
 * - 无觉醒: 不触发连续施法
 * - Lv1-4: 有限时间连续施法
 * - Lv5: 无限制连续施法
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = ChocoTweak.MODID, value = Side.CLIENT)
public class StaffAutoFireHandler {

    private static int fireCooldown = 0;
    private static int continuousCastingTimer = 0; // 累计连续施法时间
    private static boolean wasCasting = false; // 上一帧是否在施法

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null || mc.currentScreen != null) {
            return;
        }

        // 检查是否按住右键
        boolean isRightClickHeld = mc.gameSettings.keyBindUseItem.isKeyDown();

        if (!isRightClickHeld) {
            // 松开右键，重置计时器
            fireCooldown = 0;
            continuousCastingTimer = 0;
            wasCasting = false;
            return;
        }

        EntityPlayerSP player = mc.player;

        // 检查主手是否是法杖
        ItemStack mainHand = player.getHeldItemMainhand();
        if (mainHand.isEmpty() || !(mainHand.getItem() instanceof ItemStaffBase)) {
            continuousCastingTimer = 0;
            wasCasting = false;
            return;
        }

        // 蹲下是打开GUI
        if (player.isSneaking()) {
            return;
        }

        // 检查高速神言觉醒等级
        int awakementLevel = 0;
        if (MixinAwakementsRegister.highSpeedChant != null) {
            awakementLevel = Awakements.getEnchantLevel(mainHand, MixinAwakementsRegister.highSpeedChant);
        }

        // 无觉醒则不触发连续施法
        if (awakementLevel <= 0) {
            return;
        }

        // 获取该等级允许的最大持续时间
        int maxDuration = AwakementHighSpeedChant.getDurationForLevel(awakementLevel);

        // 检查是否超过持续时间限制
        if (continuousCastingTimer >= maxDuration && maxDuration != Integer.MAX_VALUE) {
            // 超时，停止连续施法
            return;
        }

        // 累计连续施法时间
        continuousCastingTimer++;

        // 发包冷却检查
        if (fireCooldown > 0) {
            fireCooldown--;
            return;
        }

        // 如果玩家正在使用物品，先停止
        if (player.isHandActive()) {
            mc.playerController.onStoppedUsingItem(player);
        }

        // 清除本地CD
        ItemStack[] cargo = InventoryBag.getCargo(mainHand);
        if (cargo[0] != null && !cargo[0].isEmpty()) {
            cargo[0].setTagCompound(null);
            InventoryBag.saveCargo(mainHand, cargo);
            mainHand.setItemDamage(-1);
        }

        // 发包给服务端清除CD
        ChocoNetwork.INSTANCE.sendToServer(new PacketClearSpellCD());

        // 使用 playerController 触发右键使用
        mc.playerController.processRightClick(player, mc.world, EnumHand.MAIN_HAND);

        // 设置发包冷却（3tick = 0.15秒）
        fireCooldown = 3;
        wasCasting = true;
    }
}
