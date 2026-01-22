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
 * 为猴王武器添加特殊 Tooltip
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.items.swords.ItemCQBlade", remap = false)
public class MixinMonkingTooltip {

    private static boolean isItem(Item item, String registryName) {
        ResourceLocation loc = item.getRegistryName();
        return loc != null && loc.toString().equals(registryName);
    }

    @Inject(method = "addInformation", at = @At("RETURN"))
    private void addSpecialTooltip(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn,
            CallbackInfo ci) {
        Item item = stack.getItem();

        // 猴王剑盾 - 巨力
        if (isItem(item, "chocolatequest:swordshiedmonking")) {
            tooltip.add("");
            tooltip.add(TextFormatting.GOLD + "" + TextFormatting.BOLD + "巨力");
            tooltip.add(TextFormatting.GRAY + "可边格挡边攻击");
            tooltip.add(TextFormatting.GRAY + "命中时使目标" + TextFormatting.YELLOW + "晕眩5秒");
            tooltip.add(TextFormatting.GRAY + "15%概率" + TextFormatting.RED + "打落敌人盔甲");
        }

        // 猴王剑 - 巨力
        else if (isItem(item, "chocolatequest:swordmonking")) {
            tooltip.add("");
            tooltip.add(TextFormatting.GOLD + "" + TextFormatting.BOLD + "巨力");
            tooltip.add(TextFormatting.GRAY + "范围攻击(5格)");
            tooltip.add(TextFormatting.GRAY + "范围内敌人" + TextFormatting.YELLOW + "晕眩5秒");
            tooltip.add(TextFormatting.GRAY + "附带中等击退");
        }

        // 钩剑 - 飞爪
        else if (isItem(item, "chocolatequest:hooksword")) {
            tooltip.add("");
            tooltip.add(TextFormatting.AQUA + "" + TextFormatting.BOLD + "飞爪");
            tooltip.add(TextFormatting.GRAY + "右键发射钩爪");
            tooltip.add(TextFormatting.GRAY + "钩住方块时" + TextFormatting.GREEN + "免疫摔落伤害");
        }
    }
}


