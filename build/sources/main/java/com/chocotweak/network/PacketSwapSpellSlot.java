package com.chocotweak.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * 法术槽位交换网络包
 */
public class PacketSwapSpellSlot implements IMessage {

    private int targetSlot;

    public PacketSwapSpellSlot() {
    }

    public PacketSwapSpellSlot(int targetSlot) {
        this.targetSlot = targetSlot;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.targetSlot = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.targetSlot);
    }

    public int getTargetSlot() {
        return targetSlot;
    }
}
