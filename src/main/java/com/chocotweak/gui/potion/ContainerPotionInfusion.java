package com.chocotweak.gui.potion;

import com.chocolate.chocolateQuest.items.swords.ItemCQBlade;
import com.chocolate.chocolateQuest.magic.Awakements;
import com.chocotweak.magic.AwakementPotionCapacity;
import com.chocotweak.core.AwakementsInitializer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;

import java.util.List;

/**
 * 药水灌注容器 - 处理服务端逻辑
 */
public class ContainerPotionInfusion extends Container {

    private InventoryPotionInfusion infusionInventory;
    private EntityPlayer player;

    /** 每次灌注的XP消耗 */
    public static final int XP_COST = 5;

    public ContainerPotionInfusion(InventoryPlayer playerInventory, InventoryPotionInfusion infusionInventory) {
        this.infusionInventory = infusionInventory;
        this.player = playerInventory.player;

        // 武器槽 (slot 0) - 位置 (26, 35)
        this.addSlotToContainer(new Slot(infusionInventory, 0, 26, 35) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof ItemCQBlade;
            }
        });

        // 药水槽 (slot 1) - 位置 (80, 35)
        this.addSlotToContainer(new Slot(infusionInventory, 1, 80, 35) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return InventoryPotionInfusion.isPotionItem(stack);
            }
        });

        // 玩家背包
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // 玩家快捷栏
        for (int col = 0; col < 9; ++col) {
            this.addSlotToContainer(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return infusionInventory.isUsableByPlayer(playerIn);
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        
        // 将物品返还给玩家
        if (!playerIn.world.isRemote) {
            ItemStack weapon = infusionInventory.removeStackFromSlot(0);
            if (!weapon.isEmpty()) {
                playerIn.dropItem(weapon, false);
            }
            
            ItemStack potion = infusionInventory.removeStackFromSlot(1);
            if (!potion.isEmpty()) {
                playerIn.dropItem(potion, false);
            }
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            itemstack = slotStack.copy();

            if (index < 2) {
                // 从灌注槽转移到玩家背包
                if (!this.mergeItemStack(slotStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 从玩家背包转移到灌注槽
                if (slotStack.getItem() instanceof ItemCQBlade) {
                    if (!this.mergeItemStack(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (InventoryPotionInfusion.isPotionItem(slotStack)) {
                    if (!this.mergeItemStack(slotStack, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (slotStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    /**
     * 检查是否可以执行灌注
     */
    public boolean canInfuse() {
        ItemStack weapon = infusionInventory.getStackInSlot(0);
        ItemStack potion = infusionInventory.getStackInSlot(1);

        if (weapon.isEmpty() || potion.isEmpty()) {
            return false;
        }

        if (!(weapon.getItem() instanceof ItemCQBlade)) {
            return false;
        }

        if (!InventoryPotionInfusion.isPotionItem(potion)) {
            return false;
        }

        // 检查药水槽数量
        int maxSlots = AwakementPotionCapacity.getPotionSlotsForWeapon(weapon);
        int currentEffects = getCurrentPotionCount(weapon);
        if (currentEffects >= maxSlots) {
            return false;
        }

        // 检查XP
        if (!player.capabilities.isCreativeMode && player.experienceLevel < XP_COST) {
            return false;
        }

        return true;
    }

    /**
     * 执行药水灌注
     */
    public boolean infusePotion() {
        if (!canInfuse()) {
            return false;
        }

        ItemStack weapon = infusionInventory.getStackInSlot(0);
        ItemStack potion = infusionInventory.getStackInSlot(1);

        // 获取药水效果
        List<PotionEffect> effects = PotionUtils.getEffectsFromStack(potion);
        if (effects.isEmpty()) {
            // 尝试从CustomPotionEffects读取
            if (potion.hasTagCompound() && potion.getTagCompound().hasKey("CustomPotionEffects")) {
                NBTTagList nbtList = potion.getTagCompound().getTagList("CustomPotionEffects", 10);
                for (int i = 0; i < nbtList.tagCount(); i++) {
                    PotionEffect effect = PotionEffect.readCustomPotionEffectFromNBT(nbtList.getCompoundTagAt(i));
                    if (effect != null) {
                        effects.add(effect);
                    }
                }
            }
        }

        if (effects.isEmpty()) {
            return false;
        }

        // 获取药水增幅倍率
        float bonus = 1.0f;
        if (AwakementsInitializer.potionCapacity != null) {
            int level = Awakements.getEnchantLevel(weapon, AwakementsInitializer.potionCapacity);
            bonus = AwakementPotionCapacity.getPotionBonus(level);
        }

        // 添加药水效果到武器
        addPotionEffectsToWeapon(weapon, effects, bonus);

        // 消耗XP
        if (!player.capabilities.isCreativeMode) {
            player.experienceLevel -= XP_COST;
        }

        // 消耗药水
        potion.shrink(1);
        if (potion.isEmpty()) {
            infusionInventory.setInventorySlotContents(1, ItemStack.EMPTY);
        }

        return true;
    }

    /**
     * 将药水效果添加到武器的NBT
     */
    private void addPotionEffectsToWeapon(ItemStack weapon, List<PotionEffect> newEffects, float bonus) {
        if (!weapon.hasTagCompound()) {
            weapon.setTagCompound(new NBTTagCompound());
        }

        NBTTagList existingList;
        if (weapon.getTagCompound().hasKey("CustomPotionEffects", 9)) {
            existingList = weapon.getTagCompound().getTagList("CustomPotionEffects", 10);
        } else {
            existingList = new NBTTagList();
        }

        for (PotionEffect newEffect : newEffects) {
            // 检查是否已有同类效果（叠加）
            boolean found = false;
            for (int i = 0; i < existingList.tagCount(); i++) {
                NBTTagCompound existingNBT = existingList.getCompoundTagAt(i);
                PotionEffect existingEffect = PotionEffect.readCustomPotionEffectFromNBT(existingNBT);
                if (existingEffect != null && existingEffect.getPotion() == newEffect.getPotion()) {
                    // 叠加效果：增加持续时间和等级
                    int newDuration = existingEffect.getDuration() + (int)(newEffect.getDuration() * bonus);
                    int newAmplifier = Math.max(existingEffect.getAmplifier(), newEffect.getAmplifier());
                    
                    PotionEffect merged = new PotionEffect(
                        newEffect.getPotion(), 
                        newDuration, 
                        newAmplifier
                    );
                    
                    NBTTagCompound mergedNBT = new NBTTagCompound();
                    merged.writeCustomPotionEffectToNBT(mergedNBT);
                    existingList.set(i, mergedNBT);
                    found = true;
                    break;
                }
            }

            if (!found) {
                // 添加新效果
                int boostedDuration = (int)(newEffect.getDuration() * bonus);
                PotionEffect boosted = new PotionEffect(
                    newEffect.getPotion(),
                    boostedDuration,
                    newEffect.getAmplifier()
                );
                
                NBTTagCompound effectNBT = new NBTTagCompound();
                boosted.writeCustomPotionEffectToNBT(effectNBT);
                existingList.appendTag(effectNBT);
            }
        }

        weapon.getTagCompound().setTag("CustomPotionEffects", existingList);
    }

    /**
     * 获取武器当前的药水效果数量
     */
    private int getCurrentPotionCount(ItemStack weapon) {
        if (!weapon.hasTagCompound()) return 0;
        if (!weapon.getTagCompound().hasKey("CustomPotionEffects", 9)) return 0;
        return weapon.getTagCompound().getTagList("CustomPotionEffects", 10).tagCount();
    }

    public InventoryPotionInfusion getInfusionInventory() {
        return infusionInventory;
    }

    public EntityPlayer getPlayer() {
        return player;
    }
}
