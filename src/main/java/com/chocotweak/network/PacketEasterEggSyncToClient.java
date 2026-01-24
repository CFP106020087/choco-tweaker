package com.chocotweak.network;

import com.chocotweak.bedrock.entity.IEasterEggCapable;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 彩蛋 NPC 同步包 - 服务端 -> 客户端
 * 
 * 当玩家开始追踪实体时，服务端发送此包到客户端同步彩蛋状态，
 * 确保重新进入游戏后客户端能正确渲染彩蛋 NPC。
 */
public class PacketEasterEggSyncToClient implements IMessage {

    private int entityId;
    private boolean isEasterEgg;
    private int variant;

    public PacketEasterEggSyncToClient() {
    }

    public PacketEasterEggSyncToClient(int entityId, boolean isEasterEgg, int variant) {
        this.entityId = entityId;
        this.isEasterEgg = isEasterEgg;
        this.variant = variant;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.isEasterEgg = buf.readBoolean();
        this.variant = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(isEasterEgg);
        buf.writeByte(variant);
    }

    public static class Handler implements IMessageHandler<PacketEasterEggSyncToClient, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketEasterEggSyncToClient message, MessageContext ctx) {
            // 必须在主线程执行
            Minecraft.getMinecraft().addScheduledTask(() -> {
                // 查找目标实体
                Entity entity = Minecraft.getMinecraft().world.getEntityByID(message.entityId);
                if (entity == null) {
                    return;
                }

                // 应用彩蛋设置
                if (entity instanceof IEasterEggCapable) {
                    IEasterEggCapable easterEgg = (IEasterEggCapable) entity;
                    easterEgg.setEasterEggNpc(message.isEasterEgg);
                    easterEgg.setModelVariant(message.variant);
                    
                    System.out.println("[ChocoTweak] Synced Easter Egg NPC: entityId=" + message.entityId 
                        + ", isEasterEgg=" + message.isEasterEgg 
                        + ", variant=" + message.variant);
                }
            });

            return null;
        }
    }
}
