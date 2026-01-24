package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.entity.EntityHumanBase;
import com.chocolate.chocolateQuest.gui.GuiDummy;
import com.chocolate.chocolateQuest.gui.ContainerHumanInventory;
import com.chocolate.chocolateQuest.gui.InventoryHuman;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 添加 GUI ID 11 用于生存模式装备编辑
 * 跳过创造模式检查，允许生存玩家编辑NPC装备
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.CommonProxy", remap = false)
public class MixinCommonProxyGui {

    /**
     * 拦截客户端 GUI 创建，添加 ID 11 支持
     */
    @Inject(method = "getClientGuiElement", at = @At("HEAD"), cancellable = true)
    private void onGetClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z,
            CallbackInfoReturnable<Object> cir) {
        if (ID == 11) {
            Entity e = world.getEntityByID(x);
            if (e instanceof EntityHumanBase) {
                // 无权限检查，直接使用 GuiDummy (完整编辑界面)
                cir.setReturnValue(new GuiDummy((EntityHumanBase) e, new InventoryHuman((EntityHumanBase) e), player));
            }
        }
    }

    /**
     * 拦截服务端容器创建，添加 ID 11 支持
     */
    @Inject(method = "getServerGuiElement", at = @At("HEAD"), cancellable = true)
    private void onGetServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z,
            CallbackInfoReturnable<Object> cir) {
        if (ID == 11) {
            Entity e = world.getEntityByID(x);
            if (e instanceof EntityHumanBase) {
                cir.setReturnValue(new ContainerHumanInventory(player.inventory, 
                        new InventoryHuman((EntityHumanBase) e)));
            }
        }
    }
}
