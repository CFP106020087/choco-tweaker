package com.chocotweak.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * 客户端 -> 服务端：请求清除法杖法术CD
 */
public class PacketClearSpellCD implements IMessage {

    public PacketClearSpellCD() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // 无需数据
    }

    @Override
    public void toBytes(ByteBuf buf) {
        // 无需数据
    }
}
