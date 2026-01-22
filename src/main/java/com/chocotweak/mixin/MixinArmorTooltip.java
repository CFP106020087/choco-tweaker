package com.chocotweak.mixin;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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
 * 护甲套装和特殊物品 Tooltip
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.items.ItemArmorBase", remap = false)
public class MixinArmorTooltip {

    private static boolean isItem(Item item, String registryName) {
        ResourceLocation loc = item.getRegistryName();
        return loc != null && loc.toString().equals(registryName);
    }

    @Inject(method = "addInformation", at = @At("TAIL"))
    private void addChocoTweakTooltip(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag,
            CallbackInfo ci) {
        Item item = stack.getItem();

        // ===== 龟甲套 =====
        if (isItem(item, "chocolatequest:turtlehelmet") || isItem(item, "chocolatequest:turtlepants")
                || isItem(item, "chocolatequest:turtleboots")) {
            tooltip.add("");
            tooltip.add(TextFormatting.GREEN + "ChocoTweak:");
            tooltip.add(TextFormatting.GRAY + "每10秒回复 " + TextFormatting.RED + "0.5心");
        }
        if (isItem(item, "chocolatequest:turtleplate")) {
            tooltip.add("");
            tooltip.add(TextFormatting.GREEN + "ChocoTweak:");
            tooltip.add(TextFormatting.GRAY + "每10秒回复 " + TextFormatting.RED + "0.5心");
            tooltip.add(TextFormatting.GRAY + "背后受伤 " + TextFormatting.YELLOW + "减半");
            tooltip.add(TextFormatting.GOLD + "[全套] " + TextFormatting.LIGHT_PURPLE + "死亡保护 (10分钟冷却)");
        }

        // ===== 公牛套 =====
        if (isItem(item, "chocolatequest:bullhelmet") || isItem(item, "chocolatequest:bullplate") ||
                isItem(item, "chocolatequest:bullpants") || isItem(item, "chocolatequest:bullboots")) {
            tooltip.add("");
            tooltip.add(TextFormatting.GREEN + "ChocoTweak:");
            tooltip.add(TextFormatting.GRAY + "每件 " + TextFormatting.RED + "+5攻击力");
            tooltip.add(TextFormatting.GOLD + "[全套] " + TextFormatting.AQUA + "持续恢复耐力");
            tooltip.add(TextFormatting.GOLD + "[全套] " + TextFormatting.YELLOW + "冲刺时速度II + 碰撞伤害");
        }

        // ===== 蜘蛛套 =====
        if (isItem(item, "chocolatequest:spiderhelmet") || isItem(item, "chocolatequest:spiderplate") ||
                isItem(item, "chocolatequest:spiderpants") || isItem(item, "chocolatequest:spiderboots")) {
            tooltip.add("");
            tooltip.add(TextFormatting.GREEN + "ChocoTweak:");
            tooltip.add(TextFormatting.GRAY + "每件 " + TextFormatting.AQUA + "+10%移速");
            tooltip.add(TextFormatting.GOLD + "[全套] " + TextFormatting.DARK_PURPLE + "爬墙能力");
            tooltip.add(TextFormatting.GOLD + "[全套] " + TextFormatting.GREEN + "免疫摔落伤害");
            tooltip.add(TextFormatting.GOLD + "[全套] " + TextFormatting.YELLOW + "负面效果上限1级");
        }

        // ===== 史莱姆套 =====
        if (isItem(item, "chocolatequest:slimehelmet") || isItem(item, "chocolatequest:slimeplate") ||
                isItem(item, "chocolatequest:slimepants") || isItem(item, "chocolatequest:slimeboots")) {
            tooltip.add("");
            tooltip.add(TextFormatting.GREEN + "ChocoTweak:");
            tooltip.add(TextFormatting.GRAY + "每件 " + TextFormatting.RED + "+10生命");
            tooltip.add(TextFormatting.GRAY + "每件 " + TextFormatting.YELLOW + "+25%击退抗性");
            tooltip.add(TextFormatting.GOLD + "[全套] " + TextFormatting.GREEN + "受伤生成史莱姆战斗");
        }

        // ===== 云靴 (Baubles CHARM) =====
        if (isItem(item, "chocolatequest:cloudboots")) {
            tooltip.add("");
            tooltip.add(TextFormatting.GREEN + "ChocoTweak:");
            tooltip.add(TextFormatting.GRAY + "免疫摔落伤害");
            tooltip.add(TextFormatting.AQUA + "+15%移速");
            tooltip.add(TextFormatting.LIGHT_PURPLE + "五段跳");
            tooltip.add(TextFormatting.DARK_AQUA + "空中移动增强");
            tooltip.add(TextFormatting.GOLD + "[Baubles CHARM 槽位生效]");
        }

        // ===== 龙头盔 (Baubles HEAD) =====
        if (isItem(item, "chocolatequest:dragonhelmet")) {
            tooltip.add("");
            tooltip.add(TextFormatting.GREEN + "ChocoTweak:");
            tooltip.add(TextFormatting.RED + "+200%攻击力");
            tooltip.add(TextFormatting.RED + "+20生命");
            tooltip.add(TextFormatting.GOLD + "[Baubles HEAD 槽位生效]");
        }

        // ===== 侦察器 (Baubles AMULET) =====
        if (isItem(item, "chocolatequest:scouter")) {
            tooltip.add("");
            tooltip.add(TextFormatting.GREEN + "ChocoTweak:");
            tooltip.add(TextFormatting.GOLD + "永久夜视 (Gamma)");
            tooltip.add(TextFormatting.RED + "自动标记视野内怪物");
            tooltip.add(TextFormatting.GRAY + "  [BODY/LEGS] " + TextFormatting.YELLOW + "+100%伤害");
            tooltip.add(TextFormatting.DARK_RED + "  [HEAD] " + TextFormatting.RED + "35%概率秒杀!");
            tooltip.add(TextFormatting.GOLD + "[Baubles AMULET 槽位生效]");
        }

        // ===== 女巫帽 (Baubles HEAD) =====
        if (isItem(item, "chocolatequest:witchhat")) {
            tooltip.add("");
            tooltip.add(TextFormatting.GREEN + "ChocoTweak:");
            tooltip.add(TextFormatting.LIGHT_PURPLE + "+20%施法速度");
            tooltip.add(TextFormatting.AQUA + "+30%魔法消耗减少");
            tooltip.add(TextFormatting.GOLD + "[Baubles HEAD 槽位生效]");
        }

        // ===== 背包 (Baubles BODY) =====
        if (isItem(item, "chocolatequest:backpack")) {
            tooltip.add("");
            tooltip.add(TextFormatting.GREEN + "ChocoTweak:");
            tooltip.add(TextFormatting.AQUA + "容量: " + TextFormatting.WHITE + "108格 (12行x9列)");
            tooltip.add(TextFormatting.GRAY + "相当于两个大箱子");
            tooltip.add(TextFormatting.GOLD + "[Baubles BODY 槽位生效]");
        }
    }
}
