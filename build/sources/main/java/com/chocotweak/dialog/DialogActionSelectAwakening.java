package com.chocotweak.dialog;

import com.chocolate.chocolateQuest.entity.npc.EntityHumanNPC;
import com.chocolate.chocolateQuest.quest.DialogAction;
import com.chocotweak.core.AwakementsInitializer;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

/**
 * 对话动作 - 选择特定觉醒词条并打开觉醒界面
 * 
 * 功能：
 * - operator: 选择具体的觉醒词条（从所有注册的觉醒中）
 * - value: 觉醒类型 (0=STAFF, 1=BLACKSMITH, 2=ENCHANTER)
 * 
 * GUI 将只显示选中的那个觉醒词条。
 */
public class DialogActionSelectAwakening extends DialogAction {

    // 觉醒类型映射（与 CQ 的 EnumEnchantType 对应）
    private static final String[] TYPE_OPTIONS = {
            "STAFF", // 0 - 法杖
            "BLACKSMITH", // 1 - 锻造
            "ENCHANTER" // 2 - 附魔
    };

    // 自定义觉醒名称列表
    private static final String[] AWAKENING_NAMES = {
            "高速神言 (High Speed Chant)",
            "法术增幅 (Spell Amplification)",
            "瞬发 (Instant Cast)",
            "魔力涌动 (Mana Surge)",
            "远程咏唱 (Long Range)",
            "回响之声 (Echoing Voice)",
            "药水容量 (Potion Capacity)",
            "药水灌注 (Potion Infusion)"
    };

    // 对应 AwakementsInitializer 中的字段
    private static final String[] AWAKENING_FIELD_NAMES = {
            "highSpeedChant",
            "spellAmplification",
            "instantCast",
            "manaSurge",
            "longRange",
            "echoingVoice",
            "potionCapacity",
            "potionInfusion"
    };

    @Override
    public void execute(EntityPlayer player, EntityHumanNPC npc) {
        // operator 是选择的觉醒词条索引
        // value 是觉醒类型 (0=STAFF, 1=BLACKSMITH, 2=ENCHANTER)
        int awakeningIndex = Math.max(0, Math.min(this.operator, AWAKENING_FIELD_NAMES.length - 1));
        int type = Math.max(0, Math.min(this.value, 2));

        // 获取选中的觉醒对象
        Object selectedAwakening = getAwakeningByIndex(awakeningIndex);

        if (selectedAwakening != null) {
            // 存储选中的觉醒到 Holder，供 Mixin 使用
            SelectedAwakeningHolder.setSelectedAwakening(selectedAwakening);
            SelectedAwakeningHolder.setSelectedAwakeningName(AWAKENING_FIELD_NAMES[awakeningIndex]);
        }

        // 打开觉醒 GUI，最大等级设为 30
        npc.openEnchantment(player, type, 30);
    }

    /**
     * 根据索引获取觉醒对象
     */
    private Object getAwakeningByIndex(int index) {
        try {
            java.lang.reflect.Field field = AwakementsInitializer.class.getField(AWAKENING_FIELD_NAMES[index]);
            return field.get(null);
        } catch (Exception e) {
            System.err.println("[ChocoTweak] Failed to get awakening: " + AWAKENING_FIELD_NAMES[index]);
            return null;
        }
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
        // value 用于输入觉醒类型索引 (0=STAFF, 1=BLACKSMITH, 2=ENCHANTER)
        return net.minecraft.client.resources.I18n.format("chocotweak.dialog.awakening_type_index");
    }

    @Override
    public String[] getOptionsForOperator() {
        // 返回所有觉醒名称作为下拉选项
        return AWAKENING_NAMES;
    }

    @Override
    public String getNameForOperator() {
        return net.minecraft.client.resources.I18n.format("chocotweak.dialog.select_awakening_affix");
    }

    @Override
    public boolean hasOperator() {
        return true;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void getSuggestions(List list) {
        list.add("Opens the Awakement GUI showing ONLY the selected awakening affix");
    }

    /**
     * 用于存储选择的觉醒，供 MixinGuiAwakement 使用
     */
    public static class SelectedAwakeningHolder {
        private static Object selectedAwakening = null;
        private static String selectedAwakeningName = null;

        public static void setSelectedAwakening(Object awakening) {
            selectedAwakening = awakening;
        }

        public static Object getSelectedAwakening() {
            return selectedAwakening;
        }

        public static void setSelectedAwakeningName(String name) {
            selectedAwakeningName = name;
        }

        public static String getSelectedAwakeningName() {
            return selectedAwakeningName;
        }

        public static Object getAndClearSelectedAwakening() {
            Object result = selectedAwakening;
            selectedAwakening = null;
            selectedAwakeningName = null;
            return result;
        }

        public static boolean hasSelection() {
            return selectedAwakening != null;
        }
    }
}
