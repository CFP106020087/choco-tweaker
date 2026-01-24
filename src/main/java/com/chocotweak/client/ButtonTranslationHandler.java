package com.chocotweak.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * 综合翻译处理器 - 使用 Forge 事件替代多个 Mixin
 * 只翻译普通按钮文本，不处理 GuiScrollOptions
 * 
 * 避免 Mixin 导致的 DialogAction 早期类加载问题
 */
public class ButtonTranslationHandler {

    // 延迟翻译标志 - 等待游戏完全加载
    private static boolean isGameReady = false;
    private static int tickCounter = 0;
    private static final int DELAY_TICKS = 20; // Wait 1 second after first GUI open

    private static final Map<String, String> BUTTON_TRANSLATIONS = new HashMap<>();

    static {
        // === NPC 编辑器按钮 (MixinGuiNPC) ===
        BUTTON_TRANSLATIONS.put("Edit dialogs", "chocotweak.npc.edit_dialogs");
        BUTTON_TRANSLATIONS.put("Edit stats", "chocotweak.npc.edit_stats");
        BUTTON_TRANSLATIONS.put("Inventory", "chocotweak.npc.inventory");
        BUTTON_TRANSLATIONS.put("Import/Export", "chocotweak.npc.import_export");
        BUTTON_TRANSLATIONS.put("Edit AI", "chocotweak.npc.edit_ai");

        // === NPC 编辑器标签 (MixinGuiButtonDisplayString) ===
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

        // === 对话编辑器标签 ===
        BUTTON_TRANSLATIONS.put("Actions", "chocotweak.dialog.actions");
        BUTTON_TRANSLATIONS.put("Conditions", "chocotweak.dialog.conditions");
        BUTTON_TRANSLATIONS.put("Answers", "chocotweak.dialog.answers");
        BUTTON_TRANSLATIONS.put("Display name", "chocotweak.dialog.display_name");
        BUTTON_TRANSLATIONS.put("File", "chocotweak.dialog.file");
        BUTTON_TRANSLATIONS.put("Option Name", "chocotweak.dialog.option_name");

        // === 附魔类型 (MixinEnumEnchantType) ===
        BUTTON_TRANSLATIONS.put("ENCHANT", "chocotweak.enchanttype.enchant");
        BUTTON_TRANSLATIONS.put("BLACKSMITH", "chocotweak.enchanttype.blacksmith");
        BUTTON_TRANSLATIONS.put("GUNSMITH", "chocotweak.enchanttype.gunsmith");
        BUTTON_TRANSLATIONS.put("STAVES", "chocotweak.enchanttype.staves");
        BUTTON_TRANSLATIONS.put("TAILOR", "chocotweak.enchanttype.tailor");
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        GuiScreen gui = event.getGui();

        // 只处理 CQ 的 GUI
        String className = gui.getClass().getName();
        if (!className.contains("chocolate")) {
            return;
        }

        // 延迟翻译 - 累计 tick 直到游戏就绪
        if (!isGameReady) {
            tickCounter++;
            if (tickCounter >= DELAY_TICKS) {
                isGameReady = true;
            } else {
                return; // 还没准备好，跳过翻译
            }
        }

        // 翻译所有按钮 (只翻译普通按钮，不翻译 ScrollOptions)
        for (GuiButton button : event.getButtonList()) {
            translateButton(button);
        }
    }

    private void translateButton(GuiButton button) {
        if (button == null)
            return;

        String displayString = button.displayString;
        if (displayString == null)
            return;

        // 检查直接翻译
        String key = BUTTON_TRANSLATIONS.get(displayString);
        if (key != null) {
            String translated = I18n.format(key);
            if (!translated.equals(key)) {
                button.displayString = translated;
                return;
            }
        }

        // 不再翻译 DialogAction 名称 - 避免类加载问题
    }
}
