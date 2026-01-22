package com.chocotweak.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * 客户端 -> 服务端：通知左键按下状态
 */
public class PacketShieldAttack implements IMessage {

    public boolean leftClickDown;

    public PacketShieldAttack() {
    }

    public PacketShieldAttack(boolean leftClickDown) {
        this.leftClickDown = leftClickDown;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        leftClickDown = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(leftClickDown);
    }
}
