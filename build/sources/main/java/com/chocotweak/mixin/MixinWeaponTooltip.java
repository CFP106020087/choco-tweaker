package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.ChocolateQuest;
import com.chocolate.chocolateQuest.items.swords.ItemCQBlade;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 武器Tooltip增强Mixin
 * 为所有CQ剑盾添加详细特效描述
 */
@Mixin(value = ItemCQBlade.class, remap = false)
public class MixinWeaponTooltip {

    @Inject(method = "addInformation", at = @At("TAIL"))
    private void addWeaponTooltip(ItemStack is, World worldIn, List<String> list, ITooltipFlag flagIn,
            CallbackInfo ci) {
        Item item = is.getItem();

        // === 铁剑盾 ===
        if (item == ChocolateQuest.ironSwordAndShield) {
            list.add("");
            list.add(TextFormatting.GOLD + "" + TextFormatting.BOLD
                    + I18n.format("chocotweak.weapon.iron_shield.title"));
            list.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.iron_shield.desc1"));
        }
        // === 钻石剑盾 ===
        else if (item == ChocolateQuest.diamondSwordAndShield) {
            list.add("");
            list.add(TextFormatting.AQUA + "" + TextFormatting.BOLD
                    + I18n.format("chocotweak.weapon.diamond_shield.title"));
            list.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.diamond_shield.desc1"));
        }
        // === 锈剑盾 ===
        else if (item == ChocolateQuest.rustedSwordAndShied) {
            list.add("");
            list.add(
                    TextFormatting.DARK_RED + "" + TextFormatting.BOLD + I18n.format("chocotweak.weapon.rusted.title"));
            list.add(TextFormatting.RED + I18n.format("chocotweak.weapon.rusted.desc1"));
            list.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.rusted.desc2"));
            list.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.rusted.desc3"));
        }
        // === 龟盾 ===
        else if (item == ChocolateQuest.swordTurtle) {
            list.add("");
            list.add(TextFormatting.GREEN + "" + TextFormatting.BOLD + I18n.format("chocotweak.weapon.turtle.title"));
            list.add(TextFormatting.RED + I18n.format("chocotweak.weapon.turtle.desc1"));
            list.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.turtle.desc2"));
            list.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.turtle.desc3"));
            list.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.turtle.desc4"));
        }
        // === 蜘蛛剑盾 ===
        else if (item == ChocolateQuest.swordSpider) {
            list.add("");
            list.add(TextFormatting.DARK_GREEN + "" + TextFormatting.BOLD
                    + I18n.format("chocotweak.weapon.spider.title"));
            list.add(TextFormatting.GREEN + I18n.format("chocotweak.weapon.spider.desc1"));
            list.add(TextFormatting.RED + I18n.format("chocotweak.weapon.spider.desc2"));
            list.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.spider.desc3"));
            list.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.spider.desc4"));
        }
        // === 阳光剑 ===
        else if (item == ChocolateQuest.swordSunLight) {
            list.add("");
            list.add(TextFormatting.YELLOW + "" + TextFormatting.BOLD + I18n.format("chocotweak.weapon.sun.title"));
            list.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.sun.desc1"));
            list.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.sun.desc2"));
            list.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.sun.desc3"));
        }
        // === 月光剑 ===
        else if (item == ChocolateQuest.swordMoonLight) {
            list.add("");
            list.add(TextFormatting.DARK_PURPLE + "" + TextFormatting.BOLD
                    + I18n.format("chocotweak.weapon.moon.title"));
            list.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.moon.desc1"));
            list.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.moon.desc2"));
            list.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.moon.desc3"));
        }
        // === 行者之剑 ===
        else if (item == ChocolateQuest.endSword) {
            list.add("");
            list.add(TextFormatting.DARK_GRAY + "" + TextFormatting.BOLD
                    + I18n.format("chocotweak.weapon.walker.title"));
            list.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.walker.desc1"));
            list.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.walker.desc2"));
            list.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.walker.desc3"));
            list.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.walker.desc4"));
        }
    }
}
