package com.chocotweak.quest;

import com.chocolate.chocolateQuest.ChocolateQuest;
import com.chocolate.chocolateQuest.entity.npc.EntityHumanNPC;
import com.chocolate.chocolateQuest.quest.DialogAction;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

/**
 * 允许生存模式玩家通过对话编辑NPC/佣兵装备
 * 使用 GUI ID 11 绕过创造模式检查
 */
public class DialogActionEditEquipment extends DialogAction {

    @Override
    public void execute(EntityPlayer player, EntityHumanNPC npc) {
        // GUI ID 11 = 生存模式装备编辑 (由 MixinCommonProxyGui 处理)
        player.openGui(ChocolateQuest.instance, 11, player.world, npc.getEntityId(), 0, 0);
    }

    @Override
    public boolean hasName() {
        return false;
    }

    @Override
    public boolean hasValue() {
        return false;
    }

    @Override
    public boolean hasOperator() {
        return false;
    }

    @Override
    public void getSuggestions(List list) {
    }
}
