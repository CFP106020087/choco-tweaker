package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.gui.InventoryBag;
import com.chocolate.chocolateQuest.magic.SpellBase;
import com.chocotweak.network.ChocoNetwork;
import com.chocotweak.network.PacketClearSpellCD;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 法杖持续施法 - 机关枪模式
 * 
 * 方案：客户端右键时发包给服务端清除CD
 * 同时客户端本地也清除，确保两端同步
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.items.ItemStaffBase", remap = false)
public abstract class MixinStaffContinuousCast {

    @Shadow
    public abstract SpellBase getSpell(ItemStack is);

    /**
     * 在右键使用法杖时，清除法术CD
     * 客户端：本地清除 + 发包给服务端
     * 服务端：直接清除
     */
    @Inject(method = "onItemRightClick", at = @At("HEAD"))
    private void clearCDOnRightClick(World world, EntityPlayer player, EnumHand hand,
            CallbackInfoReturnable<ActionResult<ItemStack>> cir) {

        ItemStack itemstack = player.getHeldItem(hand);
        if (itemstack.isEmpty()) {
            return;
        }

        // 蹲下打开GUI，不处理
        if (player.isSneaking()) {
            return;
        }

        // 获取法术槽位并清除CD
        ItemStack[] cargo = InventoryBag.getCargo(itemstack);
        if (cargo[0] != null && !cargo[0].isEmpty()) {
            // 清除TagCompound让hasTagCompound()返回false
            cargo[0].setTagCompound(null);
            InventoryBag.saveCargo(itemstack, cargo);
            itemstack.setItemDamage(-1);
        }

        // 客户端发包给服务端同步
        if (world.isRemote) {
            ChocoNetwork.INSTANCE.sendToServer(new PacketClearSpellCD());
        }
    }
}
