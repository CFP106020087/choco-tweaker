package com.chocotweak.gui.potion;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

/**
 * 药水灌注GUI的物品栏
 * 
 * 槽位：
 * - 0: 武器（待灌注）
 * - 1: 药水（消耗品）
 */
public class InventoryPotionInfusion implements IInventory {

    private ItemStack[] items = new ItemStack[2];
    private EntityPlayer player;

    public InventoryPotionInfusion(EntityPlayer player) {
        this.player = player;
        for (int i = 0; i < items.length; i++) {
            items[i] = ItemStack.EMPTY;
        }
    }

    @Override
    public int getSizeInventory() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < items.length ? items[index] : ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (index >= 0 && index < items.length && !items[index].isEmpty()) {
            ItemStack result;
            if (items[index].getCount() <= count) {
                result = items[index];
                items[index] = ItemStack.EMPTY;
            } else {
                result = items[index].splitStack(count);
            }
            markDirty();
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        if (index >= 0 && index < items.length) {
            ItemStack result = items[index];
            items[index] = ItemStack.EMPTY;
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index >= 0 && index < items.length) {
            items[index] = stack;
            markDirty();
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == 0) {
            // 武器槽：只接受CQ武器
            return stack.getItem() instanceof com.chocolate.chocolateQuest.items.swords.ItemCQBlade;
        } else if (index == 1) {
            // 药水槽：接受任何药水
            return isPotionItem(stack);
        }
        return false;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        for (int i = 0; i < items.length; i++) {
            items[i] = ItemStack.EMPTY;
        }
    }

    @Override
    public String getName() {
        return "Potion Infusion";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }

    /**
     * 检查是否是药水物品
     */
    public static boolean isPotionItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        
        // 检查是否是原版药水
        if (stack.getItem() instanceof net.minecraft.item.ItemPotion ||
            stack.getItem() instanceof net.minecraft.item.ItemSplashPotion ||
            stack.getItem() instanceof net.minecraft.item.ItemLingeringPotion) {
            return true;
        }
        
        // 检查是否有药水NBT (支持模组药水)
        if (stack.hasTagCompound()) {
            return stack.getTagCompound().hasKey("Potion") ||
                   stack.getTagCompound().hasKey("CustomPotionEffects");
        }
        
        return false;
    }

    public EntityPlayer getPlayer() {
        return player;
    }
}
