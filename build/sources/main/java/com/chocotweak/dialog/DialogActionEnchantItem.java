package com.chocotweak.dialog;

import com.chocolate.chocolateQuest.entity.npc.EntityHumanNPC;
import com.chocolate.chocolateQuest.quest.DialogAction;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * 对话动作 - 打开附魔界面（Awakement GUI）
 * 可以选择一个附魔，确保该附魔出现在 GUI 中
 * 
 * 使用方法:
 * - operator: 选择的附魔索引
 * - value: 最大等级
 */
public class DialogActionEnchantItem extends DialogAction {

    /**
     * 获取所有已注册的附魔（不使用缓存）
     */
    private static Enchantment[] getAllEnchantments() {
        List<Enchantment> list = new ArrayList<>();
        for (Enchantment enchant : Enchantment.REGISTRY) {
            if (enchant != null) {
                list.add(enchant);
            }
        }
        return list.toArray(new Enchantment[0]);
    }

    /**
     * 获取附魔显示名称列表（用于下拉菜单）
     */
    private static String[] getAllEnchantmentNames() {
        Enchantment[] enchants = getAllEnchantments();
        String[] names = new String[enchants.length];
        for (int i = 0; i < enchants.length; i++) {
            names[i] = enchants[i].getTranslatedName(1);
        }
        return names;
    }

    @Override
    public void execute(EntityPlayer player, EntityHumanNPC npc) {
        // 存储选择的附魔到 ThreadLocal，供 MixinGuiAwakement 使用
        Enchantment[] enchants = getAllEnchantments();
        if (this.operator >= 0 && this.operator < enchants.length) {
            Enchantment selectedEnchant = enchants[this.operator];
            SelectedEnchantmentHolder.setSelectedEnchantment(selectedEnchant);
        }

        // 打开 Awakement GUI (type=0 是 ENCHANT 类型)
        // value 是最大等级
        npc.openEnchantment(player, 0, this.value);
    }

    @Override
    public boolean hasName() {
        return false;
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public String getNameForValue() {
        return net.minecraft.client.resources.I18n.format("chocotweak.dialog.max_level");
    }

    @Override
    public String[] getOptionsForOperator() {
        // 返回所有附魔名称作为下拉选项
        return getAllEnchantmentNames();
    }

    @Override
    public String getNameForOperator() {
        return net.minecraft.client.resources.I18n.format("chocotweak.dialog.select_enchantment");
    }

    @Override
    public boolean hasOperator() {
        return true;
    }

    @Override
    public void getSuggestions(List list) {
        list.add("Opens the Awakement GUI with the selected enchantment available");
    }

    /**
     * 用于存储选择的附魔，供 MixinGuiAwakement 使用
     */
    public static class SelectedEnchantmentHolder {
        private static Enchantment selectedEnchantment = null;

        public static void setSelectedEnchantment(Enchantment enchant) {
            selectedEnchantment = enchant;
        }

        public static Enchantment getSelectedEnchantment() {
            return selectedEnchantment;
        }

        public static Enchantment getAndClearSelectedEnchantment() {
            Enchantment result = selectedEnchantment;
            selectedEnchantment = null;
            return result;
        }
    }
}
