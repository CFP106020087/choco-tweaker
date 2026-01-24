package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.magic.Awakements;
import com.chocotweak.core.ContainerAwakementHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * 修复ContainerAwakement中的觉醒ID查找问题
 *
 * 原版CQ的问题：在觉醒模式下GUI传入null的Enchantment，
 * 导致getEnchantmentId返回-1，触发ArrayIndexOutOfBoundsException
 *
 * 此Mixin完全重写enchantItem方法，正确处理觉醒模式
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.gui.guinpc.ContainerAwakement", remap = false)
public abstract class MixinContainerAwakement {

    @Shadow
    public boolean mode;

    @Shadow
    public abstract int getXPRequiredToEnchantItem();

    @Shadow
    public abstract int getXPRequiredForEnchantment(Enchantment enchant, int level);

    @Shadow
    public abstract int getCatalystRebate(int expRequired);

    // 使用反射获取父类的player字段
    private EntityPlayer getPlayer() {
        try {
            Field playerField = this.getClass().getSuperclass().getDeclaredField("player");
            playerField.setAccessible(true);
            return (EntityPlayer) playerField.get(this);
        } catch (Exception e) {
            System.err.println("[ChocoTweak] Failed to get player: " + e.getMessage());
            return null;
        }
    }

    // 使用反射获取inventory字段
    private IInventory getInventory() {
        try {
            Field inventoryField = this.getClass().getDeclaredField("inventory");
            inventoryField.setAccessible(true);
            return (IInventory) inventoryField.get(this);
        } catch (Exception e) {
            System.err.println("[ChocoTweak] Failed to get inventory: " + e.getMessage());
            return null;
        }
    }

    /**
     * @author ChocoTweak
     * @reason Fix ArrayIndexOutOfBoundsException for custom awakenings
     */
    @Overwrite
    public void enchantItem(Enchantment enchantment) {
        IInventory inventory = getInventory();
        EntityPlayer player = getPlayer();

        if (inventory == null || player == null) {
            System.err.println("[ChocoTweak] Could not access inventory or player");
            return;
        }

        ItemStack is = inventory.getStackInSlot(0);
        int lvl;
        int expRequired;

        if (this.mode) {
            // ========== 附魔模式 (原版逻辑) ==========
            if (enchantment == null) {
                return;
            }
            lvl = EnchantmentHelper.getEnchantmentLevel(enchantment, is);
            expRequired = getXPRequiredToEnchantItem() + getXPRequiredForEnchantment(enchantment, lvl);
            expRequired -= getCatalystRebate(expRequired);

            if (!player.capabilities.isCreativeMode) {
                player.experienceLevel -= expRequired;
            }

            inventory.setInventorySlotContents(1, ItemStack.EMPTY);

            Map<Enchantment, Integer> aw = EnchantmentHelper.getEnchantments(is);
            if (aw.containsKey(enchantment)) {
                aw.put(enchantment, aw.get(enchantment) + 1);
            } else {
                aw.put(enchantment, 1);
            }
            EnchantmentHelper.setEnchantments(aw, is);

        } else {
            // ========== 觉醒模式 (修复后的逻辑) ==========

            // 获取觉醒ID（从ContainerAwakementHelper或enchantment）
            int awakementId = ContainerAwakementHelper.pendingAwakementId;
            ContainerAwakementHelper.pendingAwakementId = -1; // 重置

            if (awakementId < 0 && enchantment != null) {
                // 如果没有设置pendingId，尝试从enchantment获取
                awakementId = Enchantment.getEnchantmentID(enchantment);
            }

            if (awakementId < 0) {
                System.err.println("[ChocoTweak] No valid awakement ID");
                return;
            }

            // 在awekements数组中按ID查找觉醒
            Awakements targetAwakement = null;
            for (Awakements aw : Awakements.awekements) {
                if (aw != null && aw.id == awakementId) {
                    targetAwakement = aw;
                    break;
                }
            }

            if (targetAwakement == null) {
                System.err.println("[ChocoTweak] Could not find awakement with ID: " + awakementId);
                return;
            }

            lvl = Awakements.getEnchantLevel(is, targetAwakement);

            // ========== 特殊处理：药水灌注觉醒 ==========
            if (targetAwakement == com.chocotweak.core.AwakementsInitializer.potionInfusion) {
                // 药水灌注使用特殊逻辑：从背包消耗药水
                boolean success = com.chocotweak.magic.AwakementPotionInfusion.infusePotion(is, player);
                if (success) {
                    System.out.println("[ChocoTweak] Potion infusion successful!");
                } else {
                    System.out.println("[ChocoTweak] Potion infusion failed - check inventory for potions");
                }
                return; // 不执行标准的觉醒升级逻辑
            }

            // ========== 标准觉醒处理 ==========
            expRequired = getXPRequiredToEnchantItem() + getXPRequiredForEnchantment(null, lvl);
            expRequired -= getCatalystRebate(expRequired);

            if (!player.capabilities.isCreativeMode) {
                player.experienceLevel -= expRequired;
            }

            inventory.setInventorySlotContents(1, ItemStack.EMPTY);
            Awakements.addEnchant(is, targetAwakement, lvl + 1);

            System.out.println("[ChocoTweak] Applied awakement: " + targetAwakement.getName() +
                    " level " + (lvl + 1));
        }
    }
}
