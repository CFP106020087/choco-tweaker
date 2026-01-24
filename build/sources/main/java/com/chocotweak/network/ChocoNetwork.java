package com.chocotweak.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 网络包注册
 * 
 * 在 mod 初始化时调用 ChocoNetwork.init()
 */
public class ChocoNetwork {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("chocotweak");

    private static int packetId = 0;

    public static void init() {
        // 客户端 -> 服务端：左键攻击
        INSTANCE.registerMessage(
                PacketShieldAttackHandler.class,
                PacketShieldAttack.class,
                packetId++,
                Side.SERVER);

        // 客户端 -> 服务端：法术槽位切换
        INSTANCE.registerMessage(
                PacketSwapSpellSlotHandler.class,
                PacketSwapSpellSlot.class,
                packetId++,
                Side.SERVER);

        // 客户端 -> 服务端：清除法术CD（连续施法）
        INSTANCE.registerMessage(
                PacketClearSpellCDHandler.class,
                PacketClearSpellCD.class,
                packetId++,
                Side.SERVER);

        // 客户端 -> 服务端：彩蛋NPC设置同步
        INSTANCE.registerMessage(
                        PacketEasterEggSync.Handler.class,
                        PacketEasterEggSync.class,
                        packetId++,
                        Side.SERVER);

        // 服务端 -> 客户端：彩蛋NPC状态同步（实体追踪时）
        INSTANCE.registerMessage(
                        PacketEasterEggSyncToClient.Handler.class,
                        PacketEasterEggSyncToClient.class,
                        packetId++,
                        Side.CLIENT);
    }
}
