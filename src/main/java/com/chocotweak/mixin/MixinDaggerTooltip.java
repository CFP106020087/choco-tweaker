package com.chocotweak.mixin;

import net.minecraft.util.ResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 匕首特殊 Tooltip
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.items.swords.ItemBaseDagger", remap = false)
public class MixinDaggerTooltip {

    private static boolean isItem(Item item, String registryName) {
        ResourceLocation loc = item.getRegistryName();
        return loc != null && loc.toString().equals(registryName);
    }

    @Inject(method = "addInformation", at = @At("RETURN"))
    private void addDaggerTooltip(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn,
            CallbackInfo ci) {
        Item item = stack.getItem();

        // 通用：背刺效果
        tooltip.add("");
        tooltip.add(TextFormatting.DARK_PURPLE + "背刺(120°): " + TextFormatting.RED + "3倍伤害");

        // 铁匕首
        if (isItem(item, "chocolatequest:irondagger")) {
            tooltip.add(TextFormatting.GRAY + "20%概率 " + TextFormatting.YELLOW + "2倍暴击");
        }

        // 钻石匕首
        else if (isItem(item, "chocolatequest:diamonddagger")) {
            tooltip.add(TextFormatting.GRAY + "20%概率 " + TextFormatting.YELLOW + "4倍暴击");
        }

        // 锈匕首
        else if (isItem(item, "chocolatequest:rusteddagger")) {
            tooltip.add(TextFormatting.GRAY + "20%概率 " + TextFormatting.YELLOW + "6倍暴击");
            tooltip.add(TextFormatting.GRAY + "暴击时 " + TextFormatting.DARK_RED + "挫伤(可叠加)");
        }

        // 骗术师匕首
        else if (isItem(item, "chocolatequest:tricksterdagger")) {
            tooltip.add("");
            tooltip.add(TextFormatting.LIGHT_PURPLE + "" + TextFormatting.BOLD + "幻影");
            tooltip.add(TextFormatting.GRAY + "20%概率 " + TextFormatting.YELLOW + "6倍暴击");
            tooltip.add(TextFormatting.GRAY + "被攻击时25%概率");
            tooltip.add(TextFormatting.GREEN + "  免疫伤害并传送到敌人背后");
        }

        // 忍者匕首
        else if (isItem(item, "chocolatequest:ninjadagger")) {
            tooltip.add("");
            tooltip.add(TextFormatting.DARK_GRAY + "" + TextFormatting.BOLD + "暗杀");
            tooltip.add(TextFormatting.GRAY + "20%概率 " + TextFormatting.RED + "10倍暴击");
        }

        // 猴王匕首
        else if (isItem(item, "chocolatequest:monkingdagger")) {
            tooltip.add("");
            tooltip.add(TextFormatting.GOLD + "" + TextFormatting.BOLD + "巨力");
            tooltip.add(TextFormatting.GRAY + "20%概率 " + TextFormatting.YELLOW + "6倍暴击");
            tooltip.add(TextFormatting.GRAY + "暴击时 " + TextFormatting.YELLOW + "晕眩3秒");
        }
    }
}


