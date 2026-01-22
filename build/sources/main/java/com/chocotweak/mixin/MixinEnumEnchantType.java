package com.chocotweak.mixin;

import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Mixin to inject Chinese translations for EnumEnchantType
 * Also adds new ALCHEMIST type (ordinal 5) for potion-related awakenings
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.misc.EnumEnchantType", remap = false)
public class MixinEnumEnchantType {

    /** 新增的炼金术师类型的ordinal值 */
    public static final int ALCHEMIST_ORDINAL = 5;

    private static final String[] TRANSLATION_KEYS = {
            "chocotweak.enchanttype.enchant", // 0
            "chocotweak.enchanttype.blacksmith", // 1
            "chocotweak.enchanttype.gunsmith", // 2
            "chocotweak.enchanttype.staves", // 3
            "chocotweak.enchanttype.tailor", // 4
            "chocotweak.enchanttype.alchemist" // 5 - NEW
    };

    private static final String[] FALLBACKS = {
            "附魔",
            "铁匠",
            "枪匠",
            "法杖附魔",
            "护甲附魔",
            "炼金术师" // 5 - NEW
    };

    /**
     * @author ChocoTweak
     * @reason Inject Chinese translations for NPC enchantment options and add
     *         ALCHEMIST
     */
    @Overwrite
    public static String[] getNames() {
        String[] names = new String[TRANSLATION_KEYS.length];

        for (int i = 0; i < TRANSLATION_KEYS.length; i++) {
            try {
                String translated = I18n.format(TRANSLATION_KEYS[i]);
                // If translation key is returned as-is, use Chinese fallback
                names[i] = translated.equals(TRANSLATION_KEYS[i]) ? FALLBACKS[i] : translated;
            } catch (Exception e) {
                names[i] = FALLBACKS[i];
            }
        }

        return names;
    }
}
