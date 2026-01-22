package com.chocotweak.client;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * 武器Tooltip事件处理器
 * 使用Forge ItemTooltipEvent添加武器特效描述
 * 注意：使用注册名检查代替直接类引用，避免ChocolateQuest类加载失败
 */
@SideOnly(Side.CLIENT)
public class WeaponTooltipHandler {

    /**
     * 安全检测物品是否为 ItemSwordAndShieldBase 子类
     * 使用类名检查避免 NoClassDefFoundError
     */
    private static boolean isSwordAndShield(Item item) {
        Class<?> clazz = item.getClass();
        while (clazz != null) {
            if (clazz.getName().equals("com.chocolate.chocolateQuest.items.swords.ItemSwordAndShieldBase")) {
                return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    /**
     * 安全检查物品注册名
     */
    private static boolean isItem(Item item, String registryName) {
        ResourceLocation loc = item.getRegistryName();
        return loc != null && loc.toString().equals(registryName);
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty())
            return;

        Item item = stack.getItem();
        List<String> tooltip = event.getToolTip();

        // === 通用剑盾 Tooltip (替代 MixinSwordShieldTooltip) ===
        if (isSwordAndShield(item)) {
            tooltip.add("");
            tooltip.add(TextFormatting.GREEN + "ChocoTweak:");
            tooltip.add(TextFormatting.AQUA + "受到攻击时自动格挡");
            tooltip.add(TextFormatting.YELLOW + "格挡中可以攻击");
        }

        // === 铁剑盾 ===
        if (isItem(item, "chocolatequest:ironswordandshield")) {
            tooltip.add("");
            tooltip.add(TextFormatting.GOLD + "" + TextFormatting.BOLD
                    + I18n.format("chocotweak.weapon.iron_shield.title"));
            tooltip.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.iron_shield.desc1"));
        }
        // === 钻石剑盾 ===
        else if (isItem(item, "chocolatequest:diamondswordandshield")) {
            tooltip.add("");
            tooltip.add(TextFormatting.AQUA + "" + TextFormatting.BOLD
                    + I18n.format("chocotweak.weapon.diamond_shield.title"));
            tooltip.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.diamond_shield.desc1"));
        }
        // === 锈剑盾 ===
        else if (isItem(item, "chocolatequest:rustedswordandshied")) {
            tooltip.add("");
            tooltip.add(
                    TextFormatting.DARK_RED + "" + TextFormatting.BOLD + I18n.format("chocotweak.weapon.rusted.title"));
            tooltip.add(TextFormatting.RED + I18n.format("chocotweak.weapon.rusted.desc1"));
            tooltip.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.rusted.desc2"));
            tooltip.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.rusted.desc3"));
        }
        // === 龟盾 ===
        else if (isItem(item, "chocolatequest:swordturtle")) {
            tooltip.add("");
            tooltip.add(
                    TextFormatting.GREEN + "" + TextFormatting.BOLD + I18n.format("chocotweak.weapon.turtle.title"));
            tooltip.add(TextFormatting.RED + I18n.format("chocotweak.weapon.turtle.desc1"));
            tooltip.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.turtle.desc2"));
            tooltip.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.turtle.desc3"));
            tooltip.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.turtle.desc4"));
        }
        // === 蜘蛛剑盾 ===
        else if (isItem(item, "chocolatequest:swordspider")) {
            tooltip.add("");
            tooltip.add(TextFormatting.DARK_GREEN + "" + TextFormatting.BOLD
                    + I18n.format("chocotweak.weapon.spider.title"));
            tooltip.add(TextFormatting.GREEN + I18n.format("chocotweak.weapon.spider.desc1"));
            tooltip.add(TextFormatting.RED + I18n.format("chocotweak.weapon.spider.desc2"));
            tooltip.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.spider.desc3"));
            tooltip.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.spider.desc4"));
        }
        // === 阳光剑 ===
        else if (isItem(item, "chocolatequest:swordsunlight")) {
            tooltip.add("");
            tooltip.add(TextFormatting.YELLOW + "" + TextFormatting.BOLD + I18n.format("chocotweak.weapon.sun.title"));
            tooltip.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.sun.desc1"));
            tooltip.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.sun.desc2"));
            tooltip.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.sun.desc3"));
        }
        // === 月光剑 ===
        else if (isItem(item, "chocolatequest:moonsword")) {
            tooltip.add("");
            tooltip.add(TextFormatting.DARK_PURPLE + "" + TextFormatting.BOLD
                    + I18n.format("chocotweak.weapon.moon.title"));
            tooltip.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.moon.desc1"));
            tooltip.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.moon.desc2"));
            tooltip.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.moon.desc3"));
        }
        // === 行者之剑 ===
        else if (isItem(item, "chocolatequest:endsword")) {
            tooltip.add("");
            tooltip.add(TextFormatting.DARK_GRAY + "" + TextFormatting.BOLD
                    + I18n.format("chocotweak.weapon.walker.title"));
            tooltip.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.walker.desc1"));
            tooltip.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.walker.desc2"));
            tooltip.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.walker.desc3"));
            tooltip.add(TextFormatting.GRAY + I18n.format("chocotweak.weapon.walker.desc4"));
        }
    }
}
