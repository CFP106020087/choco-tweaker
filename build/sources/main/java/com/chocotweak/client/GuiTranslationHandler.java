package com.chocotweak.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Forge 事件处理器：翻译 NPC 编辑器界面
 * 使用 Forge 事件代替 Mixin，更可靠
 */
public class GuiTranslationHandler {

    // 英文按钮文本 -> 翻译键
    private static final Map<String, String> BUTTON_TRANSLATIONS = new HashMap<>();

    static {
        // ========== 第一层: NPC 主菜单 ==========
        BUTTON_TRANSLATIONS.put("Edit dialogs", "chocotweak.npc.edit_dialogs");
        BUTTON_TRANSLATIONS.put("Edit stats", "chocotweak.npc.edit_stats");
        BUTTON_TRANSLATIONS.put("Inventory", "chocotweak.npc.inventory");
        BUTTON_TRANSLATIONS.put("Import/Export", "chocotweak.npc.import_export");
        BUTTON_TRANSLATIONS.put("Edit AI", "chocotweak.npc.edit_ai");

        // ========== 第二层: NPC 属性编辑器 (GuiEditNpc) ==========
        BUTTON_TRANSLATIONS.put("Name (Identifier)", "chocotweak.npc.name_identifier");
        BUTTON_TRANSLATIONS.put("DisplayName", "chocotweak.npc.display_name");
        BUTTON_TRANSLATIONS.put("Texture", "chocotweak.npc.texture");
        BUTTON_TRANSLATIONS.put("Texture type", "chocotweak.npc.texture_type");
        BUTTON_TRANSLATIONS.put("Gender", "chocotweak.npc.gender");
        BUTTON_TRANSLATIONS.put("Color", "chocotweak.npc.color");
        BUTTON_TRANSLATIONS.put("Race", "chocotweak.npc.race");
        BUTTON_TRANSLATIONS.put("Size", "chocotweak.npc.size");
        BUTTON_TRANSLATIONS.put("Faction", "chocotweak.npc.faction");
        BUTTON_TRANSLATIONS.put("Reputation friendly", "chocotweak.npc.reputation_friendly");
        BUTTON_TRANSLATIONS.put("Reputation on kill", "chocotweak.npc.reputation_on_kill");
        BUTTON_TRANSLATIONS.put("Is invincible", "chocotweak.npc.is_invincible");
        BUTTON_TRANSLATIONS.put("Visible name", "chocotweak.npc.visible_name");
        BUTTON_TRANSLATIONS.put("Can pick loot", "chocotweak.npc.can_pick_loot");
        BUTTON_TRANSLATIONS.put("Target monsters", "chocotweak.npc.target_monsters");
        BUTTON_TRANSLATIONS.put("Voice", "chocotweak.npc.voice");
        BUTTON_TRANSLATIONS.put("Can teleport", "chocotweak.npc.can_teleport");
        BUTTON_TRANSLATIONS.put("Health", "chocotweak.npc.health");
        BUTTON_TRANSLATIONS.put("Speed", "chocotweak.npc.speed");
        BUTTON_TRANSLATIONS.put("Home X", "chocotweak.npc.home_x");
        BUTTON_TRANSLATIONS.put("Home Y", "chocotweak.npc.home_y");
        BUTTON_TRANSLATIONS.put("Home Z", "chocotweak.npc.home_z");
        BUTTON_TRANSLATIONS.put("Home radio", "chocotweak.npc.home_radius");

        // ========== 第二层: 对话编辑器 (GuiEditDialog) ==========
        BUTTON_TRANSLATIONS.put("Actions", "chocotweak.dialog.actions");
        BUTTON_TRANSLATIONS.put("Conditions", "chocotweak.dialog.conditions");
        BUTTON_TRANSLATIONS.put("Answers", "chocotweak.dialog.answers");
        BUTTON_TRANSLATIONS.put("Display name", "chocotweak.dialog.display_name");
        BUTTON_TRANSLATIONS.put("File", "chocotweak.dialog.file");
        BUTTON_TRANSLATIONS.put("Option Name", "chocotweak.dialog.option_name");
        BUTTON_TRANSLATIONS.put("Add", "chocotweak.dialog.add");
        BUTTON_TRANSLATIONS.put("Edit", "chocotweak.dialog.edit");
        BUTTON_TRANSLATIONS.put("Remove", "chocotweak.dialog.remove");
        BUTTON_TRANSLATIONS.put("Save Text", "chocotweak.dialog.save_text");

        // ========== 第三层: 动作编辑器 (GuiEditAction) ==========
        // 这些动态生成，需要特殊处理

        // ========== 通用选项 ==========
        BUTTON_TRANSLATIONS.put("Yes", "chocotweak.npc.yes");
        BUTTON_TRANSLATIONS.put("No", "chocotweak.npc.no");
        BUTTON_TRANSLATIONS.put("Player", "chocotweak.npc.player");
        BUTTON_TRANSLATIONS.put("Entity", "chocotweak.npc.entity");
        BUTTON_TRANSLATIONS.put("Male", "chocotweak.npc.male");
        BUTTON_TRANSLATIONS.put("Female", "chocotweak.npc.female");

        // ========== 对话动作名称 ==========
        BUTTON_TRANSLATIONS.put("Open shop", "chocotweak.dialog.open_shop");
        BUTTON_TRANSLATIONS.put("Open item upgrade", "chocotweak.dialog.open_item_upgrade");
        BUTTON_TRANSLATIONS.put("Open inventory", "chocotweak.dialog.open_inventory");
        BUTTON_TRANSLATIONS.put("Join team", "chocotweak.dialog.join_team");
        BUTTON_TRANSLATIONS.put("NPC variable", "chocotweak.dialog.npc_variable");
        BUTTON_TRANSLATIONS.put("Global variable", "chocotweak.dialog.global_variable");
        BUTTON_TRANSLATIONS.put("Reputation", "chocotweak.dialog.reputation");
        BUTTON_TRANSLATIONS.put("Give item", "chocotweak.dialog.give_item");
        BUTTON_TRANSLATIONS.put("Consume item", "chocotweak.dialog.consume_item");
        BUTTON_TRANSLATIONS.put("Command", "chocotweak.dialog.command");
        BUTTON_TRANSLATIONS.put("Set owner", "chocotweak.dialog.set_owner");
        BUTTON_TRANSLATIONS.put("Set equipement", "chocotweak.dialog.set_equipement");
        BUTTON_TRANSLATIONS.put("Set AI", "chocotweak.dialog.set_ai");
        BUTTON_TRANSLATIONS.put("Load data from NBT", "chocotweak.dialog.load_data_from_nbt");
        BUTTON_TRANSLATIONS.put("Spawn monster", "chocotweak.dialog.spawn_monster");
        BUTTON_TRANSLATIONS.put("Kill counter", "chocotweak.dialog.kill_counter");
        BUTTON_TRANSLATIONS.put("Timer", "chocotweak.dialog.timer");
        BUTTON_TRANSLATIONS.put("Set home", "chocotweak.dialog.set_home");
        BUTTON_TRANSLATIONS.put("Build schematic", "chocotweak.dialog.build_schematic");
        BUTTON_TRANSLATIONS.put("Put in a bottle", "chocotweak.dialog.put_in_a_bottle");
        BUTTON_TRANSLATIONS.put("Buy tamed creature", "chocotweak.dialog.buy_tamed_creature");
        BUTTON_TRANSLATIONS.put("Enchant item", "chocotweak.dialog.enchant_item");

        // ========== 对话条件名称 ==========
        BUTTON_TRANSLATIONS.put("Item", "chocotweak.condition.item");
        BUTTON_TRANSLATIONS.put("Variable", "chocotweak.condition.variable");
        BUTTON_TRANSLATIONS.put("Faction reputation", "chocotweak.condition.faction_reputation");
        BUTTON_TRANSLATIONS.put("Player reputation", "chocotweak.condition.player_reputation");
        BUTTON_TRANSLATIONS.put("Time", "chocotweak.condition.time");
        BUTTON_TRANSLATIONS.put("Quest", "chocotweak.condition.quest");

        // ========== AI 编辑器 ==========
        BUTTON_TRANSLATIONS.put("Melee", "chocotweak.ai.melee");
        BUTTON_TRANSLATIONS.put("Ranged", "chocotweak.ai.ranged");
        BUTTON_TRANSLATIONS.put("Passive", "chocotweak.ai.passive");
        BUTTON_TRANSLATIONS.put("Guard", "chocotweak.ai.guard");
        BUTTON_TRANSLATIONS.put("Follow", "chocotweak.ai.follow");
        BUTTON_TRANSLATIONS.put("Wander", "chocotweak.ai.wander");

        // ========== 第四层: 商店编辑器 (GuiShop) ==========
        BUTTON_TRANSLATIONS.put("Add trade", "chocotweak.shop.add_trade");
        BUTTON_TRANSLATIONS.put("Load", "chocotweak.shop.load");
        BUTTON_TRANSLATIONS.put("Save", "chocotweak.shop.save");

        // ========== 第四层: 导入/导出 (GuiImportNPC) ==========
        // Load 和 Save 已在上面定义

        // ========== 第四层: NPC 属性编辑器详细 (GuiEditNpc) ==========
        BUTTON_TRANSLATIONS.put("Red", "chocotweak.npc.red");
        BUTTON_TRANSLATIONS.put("Green", "chocotweak.npc.green");
        BUTTON_TRANSLATIONS.put("Blue", "chocotweak.npc.blue");
        BUTTON_TRANSLATIONS.put("Blocks movement", "chocotweak.npc.blocks_movement");
        BUTTON_TRANSLATIONS.put("Attack damage", "chocotweak.npc.attack_damage");
        BUTTON_TRANSLATIONS.put("Armor", "chocotweak.npc.armor");
        BUTTON_TRANSLATIONS.put("Knockback resistance", "chocotweak.npc.knockback_resistance");

        // ========== 操作符 ==========
        BUTTON_TRANSLATIONS.put("equals", "chocotweak.operator.equals");
        BUTTON_TRANSLATIONS.put("not equals", "chocotweak.operator.not_equals");
        BUTTON_TRANSLATIONS.put("greater", "chocotweak.operator.greater");
        BUTTON_TRANSLATIONS.put("less", "chocotweak.operator.less");
        BUTTON_TRANSLATIONS.put("add", "chocotweak.operator.add");
        BUTTON_TRANSLATIONS.put("subtract", "chocotweak.operator.subtract");
        BUTTON_TRANSLATIONS.put("set", "chocotweak.operator.set");

        // ========== 第五层: AI 位置编辑器 (GuiAIPositions) ==========
        BUTTON_TRANSLATIONS.put("Delete", "chocotweak.ai.delete");
        BUTTON_TRANSLATIONS.put("Get Item", "chocotweak.ai.get_item");
        BUTTON_TRANSLATIONS.put("Add AI", "chocotweak.ai.add_ai");
        BUTTON_TRANSLATIONS.put("Remove AI", "chocotweak.ai.remove_ai");
        BUTTON_TRANSLATIONS.put("Position", "chocotweak.ai.position");
        BUTTON_TRANSLATIONS.put("Target", "chocotweak.ai.target");
        BUTTON_TRANSLATIONS.put("Priority", "chocotweak.ai.priority");

        // ========== 觉醒界面 (GuiAwakement) ==========
        BUTTON_TRANSLATIONS.put("Awaken", "chocotweak.awakement.awaken");
        BUTTON_TRANSLATIONS.put("Cancel", "chocotweak.awakement.cancel");
        BUTTON_TRANSLATIONS.put("Select Enchantment", "chocotweak.awakement.select_enchantment");

        // ========== 更多通用按钮 ==========
        BUTTON_TRANSLATIONS.put("Back", "chocotweak.common.back");
        BUTTON_TRANSLATIONS.put("Next", "chocotweak.common.next");
        BUTTON_TRANSLATIONS.put("Previous", "chocotweak.common.previous");
        BUTTON_TRANSLATIONS.put("Apply", "chocotweak.common.apply");
        BUTTON_TRANSLATIONS.put("Reset", "chocotweak.common.reset");
        BUTTON_TRANSLATIONS.put("Copy", "chocotweak.common.copy");
        BUTTON_TRANSLATIONS.put("Paste", "chocotweak.common.paste");
        BUTTON_TRANSLATIONS.put("Clear", "chocotweak.common.clear");
        BUTTON_TRANSLATIONS.put("Select", "chocotweak.common.select");
        BUTTON_TRANSLATIONS.put("Empty", "chocotweak.common.empty");
    }

    /**
     * 在 GUI 初始化后翻译按钮
     */
    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        GuiScreen gui = event.getGui();

        // 检查是否是 CQ 的 GUI
        String className = gui.getClass().getName();
        if (!className.contains("chocolateQuest") && !className.contains("chocolate")) {
            return;
        }

        System.out.println("[ChocoTweak] GuiTranslationHandler: Processing " + className);

        // 遍历所有按钮并翻译
        for (GuiButton button : event.getButtonList()) {
            translateButton(button);

            // 特殊处理: 翻译 GuiScrollOptions 中的 modeNames
            if (button.getClass().getSimpleName().equals("GuiScrollOptions")) {
                translateScrollOptions(button);
            }
        }
    }

    /**
     * 翻译 GuiScrollOptions 中的选项名称
     */
    private void translateScrollOptions(GuiButton scrollOptions) {
        try {
            // 通过反射获取 modeNames 字段
            java.lang.reflect.Field modeNamesField = scrollOptions.getClass().getDeclaredField("modeNames");
            modeNamesField.setAccessible(true);
            String[] modeNames = (String[]) modeNamesField.get(scrollOptions);

            if (modeNames != null && modeNames.length > 0) {
                // 创建新数组避免 ConcurrentModificationException
                String[] translatedNames = new String[modeNames.length];
                boolean hasChanges = false;

                for (int i = 0; i < modeNames.length; i++) {
                    String original = modeNames[i];
                    String key = BUTTON_TRANSLATIONS.get(original);
                    if (key != null) {
                        String translated = net.minecraft.client.resources.I18n.format(key);
                        if (!translated.equals(key)) {
                            translatedNames[i] = translated;
                            hasChanges = true;
                            System.out.println(
                                    "[ChocoTweak] Translated scroll option: " + original + " -> " + translated);
                        } else {
                            translatedNames[i] = original;
                        }
                    } else {
                        translatedNames[i] = original;
                    }
                }

                // 只有有变化时才替换整个数组
                if (hasChanges) {
                    modeNamesField.set(scrollOptions, translatedNames);
                }
            }
        } catch (Exception e) {
            // 忽略反射错误
        }
    }

    /**
     * 翻译单个按钮
     */
    private void translateButton(GuiButton button) {
        if (button == null || button.displayString == null)
            return;

        String original = button.displayString;

        // 先尝试直接匹配
        String key = BUTTON_TRANSLATIONS.get(original);
        if (key != null) {
            String translated = I18n.format(key);
            if (!translated.equals(key)) {
                System.out.println("[ChocoTweak] Translated: " + original + " -> " + translated);
                button.displayString = translated;
                return;
            }
        }

        // 尝试处理带前缀的文本 (如 "§lAction: §rOpen shop")
        if (original.contains(":")) {
            String[] parts = original.split(":", 2);
            if (parts.length == 2) {
                String prefix = parts[0].replaceAll("§.", "").trim();
                String value = parts[1].replaceAll("§.", "").trim();

                // 翻译前缀 (Action, Condition)
                String prefixKey = null;
                if ("Action".equals(prefix)) {
                    prefixKey = "chocotweak.label.action";
                } else if ("Condition".equals(prefix)) {
                    prefixKey = "chocotweak.label.condition";
                }

                // 翻译值
                String valueKey = BUTTON_TRANSLATIONS.get(value);
                if (valueKey != null) {
                    String translatedValue = I18n.format(valueKey);
                    if (!translatedValue.equals(valueKey)) {
                        // 重新组合
                        String newPrefix = prefixKey != null ? I18n.format(prefixKey) : prefix;
                        button.displayString = "§l" + newPrefix + ": §r" + translatedValue;
                        System.out.println(
                                "[ChocoTweak] Translated composite: " + original + " -> " + button.displayString);
                    }
                }
            }
        }
    }
}
