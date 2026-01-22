package com.chocotweak.network;

import com.chocotweak.handler.ShieldAttackHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * 服务端处理左键攻击包
 */
public class PacketShieldAttackHandler implements IMessageHandler<PacketShieldAttack, IMessage> {

    @Override
    public IMessage onMessage(PacketShieldAttack message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;

        // 在主线程处理
        player.getServerWorld().addScheduledTask(() -> {
            ShieldAttackHandler.setLeftClickState(player, message.leftClickDown);
        });

        return null;
    }
}
