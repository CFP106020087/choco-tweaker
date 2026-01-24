package com.chocotweak.network;

import com.chocolate.chocolateQuest.entity.EntityHumanBase;
import com.chocotweak.bedrock.entity.IEasterEggCapable;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * 彩蛋NPC同步包 - 从客户端发送到服务端
 * 
 * 当玩家在NPC编辑GUI中修改彩蛋设置时，通过此包同步到服务端，
 * 确保NBT正确保存，避免重登后状态丢失。
 */
public class PacketEasterEggSync implements IMessage {

    private int entityId;
    private boolean isEasterEgg;
    private int variant;

    public PacketEasterEggSync() {
    }

    public PacketEasterEggSync(int entityId, boolean isEasterEgg, int variant) {
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

    public static class Handler implements IMessageHandler<PacketEasterEggSync, IMessage> {
        @Override
        public IMessage onMessage(PacketEasterEggSync message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;

            // 必须在主线程执行
            player.getServerWorld().addScheduledTask(() -> {
                // 验证玩家权限 (OP)
                if (!player.canUseCommand(2, "")) {
                    return;
                }

                // 查找目标实体
                Entity entity = player.world.getEntityByID(message.entityId);
                if (!(entity instanceof EntityHumanBase)) {
                    return;
                }

                // 验证距离 (防止远程修改)
                if (entity.getDistanceSq(player) > 64 * 64) {
                    return;
                }

                // 应用彩蛋设置
                if (entity instanceof IEasterEggCapable) {
                    IEasterEggCapable easterEgg = (IEasterEggCapable) entity;
                    easterEgg.setEasterEggNpc(message.isEasterEgg);
                    easterEgg.setModelVariant(message.variant);
                }
            });

            return null;
        }
    }
}
