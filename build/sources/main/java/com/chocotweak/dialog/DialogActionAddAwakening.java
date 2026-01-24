package com.chocotweak.dialog;

import com.chocolate.chocolateQuest.entity.npc.EntityHumanNPC;
import com.chocolate.chocolateQuest.quest.DialogAction;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

/**
 * 对话动作 - 打开觉醒界面并添加自定义觉醒选项
 * 
 * 类型（operator）:
 * - 0 = STAFF (法杖觉醒)
 * - 1 = BLACKSMITH (锻造觉醒)
 * - 2 = ENCHANTER (附魔觉醒)
 * 
 * value = 最大等级
 */
public class DialogActionAddAwakening extends DialogAction {

    // 觉醒类型映射（与 CQ 的 EnumEnchantType 对应）
    private static final String[] TYPE_NAMES = {
        "STAFF",        // 0 - 法杖
        "BLACKSMITH",   // 1 - 锻造
        "ENCHANTER"     // 2 - 附魔
    };

    @Override
    public void execute(EntityPlayer player, EntityHumanNPC npc) {
        // operator 是觉醒类型 (0=STAFF, 1=BLACKSMITH, 2=ENCHANTER)
        // value 是最大等级
        int type = Math.max(0, Math.min(this.operator, 2));
        int maxLevel = this.value > 0 ? this.value : 30;
        
        // 打开 Awakement GUI
        // type 对应 CQ 的 EnumEnchantType
        npc.openEnchantment(player, type, maxLevel);
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
        // 返回觉醒类型名称作为下拉选项
        return TYPE_NAMES;
    }

    @Override
    public String getNameForOperator() {
        return net.minecraft.client.resources.I18n.format("chocotweak.dialog.awakening_type");
    }

    @Override
    public boolean hasOperator() {
        return true;
    }

    @Override
    public void getSuggestions(List list) {
        list.add("Opens the Awakement GUI with the selected awakening type (STAFF/BLACKSMITH/ENCHANTER)");
    }
}
