package com.chocotweak.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 * 背包存储容器
 * 支持108格超大容量（12行×9列）
 */
public class ContainerBackpack extends Container {

    private static final int BACKPACK_ROWS = 12;
    private static final int SLOTS_PER_ROW = 9;
    private static final int BACKPACK_SIZE = BACKPACK_ROWS * SLOTS_PER_ROW; // 108

    private final ItemStack backpackStack;
    private final ItemStack[] contents;
    private final int tempId;

    public ContainerBackpack(InventoryPlayer playerInventory, ItemStack backpackStack) {
        this.backpackStack = backpackStack;
        this.contents = new ItemStack[BACKPACK_SIZE];

        // 初始化
        for (int i = 0; i < BACKPACK_SIZE; i++) {
            contents[i] = ItemStack.EMPTY;
        }

        // 生成临时ID用于识别
        this.tempId = (int) (Math.random() * Integer.MAX_VALUE);
        ensureTag().setInteger("chocotweak_tempid", tempId);

        // 从NBT加载内容
        loadFromNBT();

        // 添加背包槽位（12行×9列）
        for (int row = 0; row < BACKPACK_ROWS; row++) {
            for (int col = 0; col < SLOTS_PER_ROW; col++) {
                int index = row * SLOTS_PER_ROW + col;
                // 槽位Y坐标：从18开始，每行18像素
                this.addSlotToContainer(new SlotBackpack(this, index, 8 + col * 18, 18 + row * 18));
            }
        }

        // 添加玩家物品栏（3行）- 在背包下方
        int playerInvY = 18 + BACKPACK_ROWS * 18 + 14;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlotToContainer(
                        new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }

        // 添加快捷栏
        int hotbarY = playerInvY + 58;
        for (int col = 0; col < 9; col++) {
            this.addSlotToContainer(new Slot(playerInventory, col, 8 + col * 18, hotbarY));
        }
    }

    private NBTTagCompound ensureTag() {
        if (!backpackStack.hasTagCompound()) {
            backpackStack.setTagCompound(new NBTTagCompound());
        }
        return backpackStack.getTagCompound();
    }

    private void loadFromNBT() {
        NBTTagCompound tag = ensureTag();
        if (tag.hasKey("BackpackItems")) {
            NBTTagList list = tag.getTagList("BackpackItems", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound slotTag = list.getCompoundTagAt(i);
                int slot = slotTag.getByte("Slot") & 255;
                if (slot >= 0 && slot < BACKPACK_SIZE) {
                    contents[slot] = new ItemStack(slotTag);
                }
            }
        }
    }

    public void saveToNBT() {
        NBTTagCompound tag = ensureTag();
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < BACKPACK_SIZE; i++) {
            if (!contents[i].isEmpty()) {
                NBTTagCompound slotTag = new NBTTagCompound();
                slotTag.setByte("Slot", (byte) i);
                contents[i].writeToNBT(slotTag);
                list.appendTag(slotTag);
            }
        }
        tag.setTag("BackpackItems", list);
    }

    public ItemStack getStackInSlot(int index) {
        if (index >= 0 && index < BACKPACK_SIZE) {
            return contents[index];
        }
        return ItemStack.EMPTY;
    }

    public void setStackInSlot(int index, ItemStack stack) {
        if (index >= 0 && index < BACKPACK_SIZE) {
            contents[index] = stack;
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        saveToNBT();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            result = slotStack.copy();

            if (index < BACKPACK_SIZE) {
                // 从背包移动到玩家物品栏
                if (!this.mergeItemStack(slotStack, BACKPACK_SIZE, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 从玩家物品栏移动到背包
                if (!this.mergeItemStack(slotStack, 0, BACKPACK_SIZE, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return result;
    }

    /**
     * 背包专用槽位
     */
    public static class SlotBackpack extends Slot {
        private final ContainerBackpack container;
        private final int slotIndex;

        public SlotBackpack(ContainerBackpack container, int index, int x, int y) {
            super(null, index, x, y);
            this.container = container;
            this.slotIndex = index;
        }

        @Override
        public ItemStack getStack() {
            return container.getStackInSlot(slotIndex);
        }

        @Override
        public void putStack(ItemStack stack) {
            container.setStackInSlot(slotIndex, stack);
            this.onSlotChanged();
        }

        @Override
        public ItemStack decrStackSize(int amount) {
            ItemStack current = getStack();
            if (current.isEmpty())
                return ItemStack.EMPTY;

            int toRemove = Math.min(amount, current.getCount());
            ItemStack result = current.splitStack(toRemove);
            if (current.isEmpty()) {
                putStack(ItemStack.EMPTY);
            }
            return result;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            // 不能放入背包本身
            return stack.getItem().getRegistryName() == null ||
                    !stack.getItem().getRegistryName().toString().equals("chocolatequest:backpack");
        }

        @Override
        public int getSlotStackLimit() {
            return 64;
        }
    }
}
