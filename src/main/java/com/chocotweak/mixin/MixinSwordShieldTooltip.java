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
 * 剑盾 Tooltip
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.items.swords.ItemSwordAndShieldBase", remap = false)
public class MixinSwordShieldTooltip {

    @Inject(method = "addInformation", at = @At("TAIL"), remap = false)
    private void addSwordShieldTooltip(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag,
            CallbackInfo ci) {
        tooltip.add("");
        tooltip.add(TextFormatting.GREEN + "ChocoTweak:");
        tooltip.add(TextFormatting.AQUA + "剑与盾的组合");
        tooltip.add(TextFormatting.YELLOW + "攻守一体");
    }
}
