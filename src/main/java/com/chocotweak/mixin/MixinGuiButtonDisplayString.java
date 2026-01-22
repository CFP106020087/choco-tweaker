package com.chocotweak.mixin;

import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Mixin to translate GuiButtonDisplayString labels in NPC editor
 * 翻译 NPC 编辑器中的标签
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.gui.GuiButtonDisplayString", remap = false)
public class MixinGuiButtonDisplayString {

    // 英文标签 -> 翻译键 的映射
    private static final Map<String, String> LABEL_TO_KEY = new HashMap<>();

    static {
        // NPC 编辑器标签
        LABEL_TO_KEY.put("Name (Identifier)", "chocotweak.npc.name_identifier");
        LABEL_TO_KEY.put("DisplayName", "chocotweak.npc.display_name");
        LABEL_TO_KEY.put("Texture", "chocotweak.npc.texture");
        LABEL_TO_KEY.put("Texture type", "chocotweak.npc.texture_type");
        LABEL_TO_KEY.put("Gender", "chocotweak.npc.gender");
        LABEL_TO_KEY.put("Color", "chocotweak.npc.color");
        LABEL_TO_KEY.put("Race", "chocotweak.npc.race");
        LABEL_TO_KEY.put("Size", "chocotweak.npc.size");
        LABEL_TO_KEY.put("Faction", "chocotweak.npc.faction");
        LABEL_TO_KEY.put("Reputation friendly", "chocotweak.npc.reputation_friendly");
        LABEL_TO_KEY.put("Reputation on kill", "chocotweak.npc.reputation_on_kill");
        LABEL_TO_KEY.put("Is invincible", "chocotweak.npc.is_invincible");
        LABEL_TO_KEY.put("Visible name", "chocotweak.npc.visible_name");
        LABEL_TO_KEY.put("Can pick loot", "chocotweak.npc.can_pick_loot");
        LABEL_TO_KEY.put("Target monsters", "chocotweak.npc.target_monsters");
        LABEL_TO_KEY.put("Voice", "chocotweak.npc.voice");
        LABEL_TO_KEY.put("Can teleport", "chocotweak.npc.can_teleport");
        LABEL_TO_KEY.put("Health", "chocotweak.npc.health");
        LABEL_TO_KEY.put("Speed", "chocotweak.npc.speed");
        LABEL_TO_KEY.put("Home X", "chocotweak.npc.home_x");
        LABEL_TO_KEY.put("Home Y", "chocotweak.npc.home_y");
        LABEL_TO_KEY.put("Home Z", "chocotweak.npc.home_z");
        LABEL_TO_KEY.put("Home radio", "chocotweak.npc.home_radius");

        // 对话编辑器标签
        LABEL_TO_KEY.put("Actions", "chocotweak.dialog.actions");
        LABEL_TO_KEY.put("Conditions", "chocotweak.dialog.conditions");
        LABEL_TO_KEY.put("Answers", "chocotweak.dialog.answers");
        LABEL_TO_KEY.put("Display name", "chocotweak.dialog.display_name");
        LABEL_TO_KEY.put("File", "chocotweak.dialog.file");
        LABEL_TO_KEY.put("Option Name", "chocotweak.dialog.option_name");
    }

    /**
     * 在构造函数结束时翻译 displayString
     */
    @Inject(method = "<init>(IIIIILjava/lang/String;)V", at = @At("RETURN"))
    private void onInit(int id, int posX, int posY, int width, int height, String text, CallbackInfo ci) {
        translateDisplayString(text);
    }

    @Inject(method = "<init>(IIIIILjava/lang/String;I)V", at = @At("RETURN"))
    private void onInitWithColor(int id, int posX, int posY, int width, int height, String text, int color,
            CallbackInfo ci) {
        translateDisplayString(text);
    }

    private void translateDisplayString(String originalText) {
        if (originalText == null)
            return;

        try {
            String key = LABEL_TO_KEY.get(originalText);
            if (key != null) {
                String translated = I18n.format(key);
                if (translated != null && !translated.equals(key)) {
                    // 使用反射设置 displayString 字段
                    // 因为我们不能直接访问父类的 protected 字段
                    try {
                        java.lang.reflect.Field field = net.minecraft.client.gui.GuiButton.class
                                .getDeclaredField("displayString");
                        field.setAccessible(true);
                        field.set(this, translated);
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}


