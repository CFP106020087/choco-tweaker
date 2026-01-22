package com.chocotweak.network;

import com.chocolate.chocolateQuest.gui.InventoryBag;
import com.chocolate.chocolateQuest.items.ItemStaffBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * 服务端处理：清除玩家手持法杖的法术CD
 */
public class PacketClearSpellCDHandler implements IMessageHandler<PacketClearSpellCD, IMessage> {

    @Override
    public IMessage onMessage(PacketClearSpellCD message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        
        // 在主线程执行
        player.getServerWorld().addScheduledTask(() -> {
            ItemStack held = player.getHeldItemMainhand();
            
            if (held.isEmpty() || !(held.getItem() instanceof ItemStaffBase)) {
                // 尝试副手
                held = player.getHeldItemOffhand();
                if (held.isEmpty() || !(held.getItem() instanceof ItemStaffBase)) {
                    return;
                }
            }

            // 清除法术CD
            ItemStack[] cargo = InventoryBag.getCargo(held);
            if (cargo[0] != null && !cargo[0].isEmpty()) {
                cargo[0].setTagCompound(null);
                InventoryBag.saveCargo(held, cargo);
                held.setItemDamage(-1);
            }
        });

        return null;
    }
}
