package com.chocotweak.network;

import com.chocotweak.gui.potion.ContainerPotionInfusion;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * 药水灌注网络包
 * 
 * 客户端 → 服务端：请求执行灌注
 */
public class PacketPotionInfusion implements IMessage {

    public PacketPotionInfusion() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // 无数据
    }

    @Override
    public void toBytes(ByteBuf buf) {
        // 无数据
    }

    public static class Handler implements IMessageHandler<PacketPotionInfusion, IMessage> {
        @Override
        public IMessage onMessage(PacketPotionInfusion message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            
            // 必须在主线程执行
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof ContainerPotionInfusion) {
                    ContainerPotionInfusion container = (ContainerPotionInfusion) player.openContainer;
                    container.infusePotion();
                }
            });
            
            return null;
        }
    }
}
