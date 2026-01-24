package com.chocotweak.bedrock.handler;

import com.chocolate.chocolateQuest.entity.EntityHumanBase;
import com.chocotweak.bedrock.entity.IEasterEggCapable;
import com.chocotweak.network.ChocoNetwork;
import com.chocotweak.network.PacketEasterEggSyncToClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 处理彩蛋 NPC 的实体追踪同步
 * 
 * 当玩家开始追踪一个实体时，如果是彩蛋 NPC，
 * 服务端会发送同步包到客户端确保正确渲染。
 */
public class EasterEggTrackingHandler {

    @SubscribeEvent
    public void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        // 仅在服务端执行
        if (event.getEntityPlayer().world.isRemote) {
            return;
        }

        Entity target = event.getTarget();

        // 检查是否是 EntityHumanBase（CQ NPC）
        if (!(target instanceof EntityHumanBase)) {
            return;
        }

        // 检查是否实现 IEasterEggCapable
        if (!(target instanceof IEasterEggCapable)) {
            return;
        }

        IEasterEggCapable easterEgg = (IEasterEggCapable) target;

        // 如果是彩蛋 NPC，发送同步包
        if (easterEgg.isEasterEggNpc()) {
            EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();
            
            PacketEasterEggSyncToClient packet = new PacketEasterEggSyncToClient(
                    target.getEntityId(),
                    true,
                    easterEgg.getModelVariant());
            
            ChocoNetwork.INSTANCE.sendTo(packet, player);
            
            System.out.println("[ChocoTweak] Sent Easter Egg sync to " + player.getName() 
                    + " for entity " + target.getEntityId() 
                    + " variant=" + easterEgg.getModelVariant());
        }
    }
}
