package com.chocotweak.network;

import com.chocolate.chocolateQuest.gui.InventoryBag;
import com.chocolate.chocolateQuest.items.ItemStaffBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * 服务端处理法术槽位交换
 */
public class PacketSwapSpellSlotHandler implements IMessageHandler<PacketSwapSpellSlot, IMessage> {

    @Override
    public IMessage onMessage(PacketSwapSpellSlot message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        
        // 在主线程执行
        player.getServerWorld().addScheduledTask(() -> {
            ItemStack held = player.getHeldItemMainhand();
            
            if (held.isEmpty() || !(held.getItem() instanceof ItemStaffBase)) {
                return;
            }

            int targetSlot = message.getTargetSlot();
            ItemStack[] spells = InventoryBag.getCargo(held);
            
            if (targetSlot < 0 || targetSlot >= spells.length) {
                return;
            }

            // 执行交换: 槽位0 <-> 目标槽位
            ItemStack temp = spells[0];
            spells[0] = spells[targetSlot];
            spells[targetSlot] = temp;

            // 保存回NBT
            InventoryBag.saveCargo(held, spells);
            
            // 更新客户端显示 (可选:发送聊天消息)
            // player.sendMessage(new TextComponentString("§a法术已切换"));
        });

        return null;
    }
}
