package com.chocotweak.mixin;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 深渊漫步者之王胸甲 Tooltip
 * 
 * 显示特殊效果：
 * - +100%伤害
 * - +30%易伤(负面)
 * - 手持Walker剑时飞行+沙尘暴
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.items.ItemArmorKing", remap = false)
public class MixinKingArmorTooltip {

    @Inject(method = "addInformation", at = @At("TAIL"))
    private void addKingArmorTooltip(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag,
            CallbackInfo ci) {
        
        tooltip.add("");
        tooltip.add(TextFormatting.DARK_PURPLE + "§l深渊漫步者之王套装效果:");
        tooltip.add("");
        
        // 增益效果
        tooltip.add(TextFormatting.GREEN + "▶ 增益:");
        tooltip.add(TextFormatting.RED + "  +100% " + TextFormatting.GRAY + "伤害输出");
        
        // 负面效果
        tooltip.add(TextFormatting.DARK_RED + "▶ 代价:");
        tooltip.add(TextFormatting.YELLOW + "  +30% " + TextFormatting.GRAY + "受到伤害");
        
        // 特殊效果
        tooltip.add("");
        tooltip.add(TextFormatting.LIGHT_PURPLE + "▶ 手持深渊行者之剑时:");
        tooltip.add(TextFormatting.AQUA + "  ✦ " + TextFormatting.WHITE + "获得飞行能力");
        tooltip.add(TextFormatting.DARK_GRAY + "  ✦ " + TextFormatting.GRAY + "脚下环绕黑色沙尘暴");
    }
}
