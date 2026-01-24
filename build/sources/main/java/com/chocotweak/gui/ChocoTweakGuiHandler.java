package com.chocotweak.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

/**
 * ChocoTweak GUI处理器
 */
public class ChocoTweakGuiHandler implements IGuiHandler {

    public static final int GUI_BACKPACK = 100;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == GUI_BACKPACK) {
            ItemStack backpack = findBackpack(player);
            if (!backpack.isEmpty()) {
                return new ContainerBackpack(player.inventory, backpack);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == GUI_BACKPACK) {
            ItemStack backpack = findBackpack(player);
            if (!backpack.isEmpty()) {
                return new GuiBackpack(player.inventory, backpack);
            }
        }
        return null;
    }

    /**
     * 查找玩家装备的背包
     * 优先检查胸甲槽，然后检查Baubles槽
     */
    public static ItemStack findBackpack(EntityPlayer player) {
        // 1. 检查胸甲槽
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (isBackpack(chest)) {
            return chest;
        }

        // 2. 检查Baubles槽（如果Baubles已安装）
        try {
            Class<?> baublesApiClass = Class.forName("baubles.api.BaublesApi");
            Object baubles = baublesApiClass.getMethod("getBaublesHandler", EntityPlayer.class).invoke(null, player);
            if (baubles != null) {
                int slots = (int) baubles.getClass().getMethod("getSlots").invoke(baubles);
                for (int i = 0; i < slots; i++) {
                    ItemStack stack = (ItemStack) baubles.getClass().getMethod("getStackInSlot", int.class)
                            .invoke(baubles, i);
                    if (isBackpack(stack)) {
                        return stack;
                    }
                }
            }
        } catch (Exception ignored) {
            // Baubles未安装或API不可用
        }

        return ItemStack.EMPTY;
    }

    private static boolean isBackpack(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        return stack.getItem().getRegistryName() != null &&
                stack.getItem().getRegistryName().toString().equals("chocolatequest:backpack");
    }
}
