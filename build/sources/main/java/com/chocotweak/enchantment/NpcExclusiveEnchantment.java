package com.chocotweak.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

/**
 * NPC 专属附魔基类
 * 这些附魔只能通过 CQ NPC 铁匠获得，无法从以下途径获取：
 * - 附魔台
 * - 村民交易
 * - 附魔书
 * - 战利品箱子
 */
public abstract class NpcExclusiveEnchantment extends Enchantment {

    protected NpcExclusiveEnchantment(Rarity rarity, EnumEnchantmentType type, EntityEquipmentSlot[] slots) {
        super(rarity, type, slots);
    }

    /**
     * 始终允许应用 - 让 Awakement GUI 显示这些附魔
     * 之后可以细化物品类型检查
     */
    @Override
    public boolean canApply(ItemStack stack) {
        // 暂时对所有非空物品返回 true，确保能在 GUI 中看到
        return !stack.isEmpty();
    }

    /**
     * 不能在附魔台获得
     */
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }

    /**
     * 是宝藏附魔 - 不会在普通战利品中出现
     */
    @Override
    public boolean isTreasureEnchantment() {
        return true;
    }

    /**
     * 可以附到书上 - NPC 可以给附魔书
     * 村民不会交易是因为 isTreasureEnchantment = true
     */
    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }
}
