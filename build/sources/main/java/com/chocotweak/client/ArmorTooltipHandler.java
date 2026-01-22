package com.chocotweak.client;

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
 * 护甲套装和特殊物品 Tooltip（事件版）
 * 使用注册名检查避免直接引用CQ类导致的类加载问题
 */
@SideOnly(Side.CLIENT)
public class ArmorTooltipHandler {

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

        // ========== 武器 Tooltip ==========

        // === 铁剑盾 ===
        if (isItem(item, "chocolatequest:ironswordandshield")) {
            tooltip.add("");
            tooltip.add(TextFormatting.GOLD + "" + TextFormatting.BOLD + "铁剑盾特效");
            tooltip.add(TextFormatting.GRAY + "基础剑盾");
        }
        // === 钻石剑盾 ===
        else if (isItem(item, "chocolatequest:diamondswordandshield")) {
            tooltip.add("");
            tooltip.add(TextFormatting.AQUA + "" + TextFormatting.BOLD + "钻石剑盾特效");
            tooltip.add(TextFormatting.GRAY + "高级剑盾");
        }
        // === 锈剑盾 ===
        else if (isItem(item, "chocolatequest:rustedswordandshied")) {
            tooltip.add("");
            tooltip.add(TextFormatting.DARK_RED + "" + TextFormatting.BOLD + "锈蚀诅咒");
            tooltip.add(TextFormatting.RED + "攻击附带凋零");
            tooltip.add(TextFormatting.GRAY + "受到攻击时有几率给予攻击者凋零");
            tooltip.add(TextFormatting.GRAY + "格挡时恢复生命");
        }
        // === 龟盾 ===
        else if (isItem(item, "chocolatequest:swordturtle")) {
            tooltip.add("");
            tooltip.add(TextFormatting.GREEN + "" + TextFormatting.BOLD + "龟盾特效");
            tooltip.add(TextFormatting.RED + "极高格挡减伤");
            tooltip.add(TextFormatting.GRAY + "格挡时恢复耐力");
            tooltip.add(TextFormatting.GRAY + "格挡时有几率反弹投射物");
            tooltip.add(TextFormatting.GRAY + "受伤时获得短暂抗性提升");
        }
        // === 蜘蛛剑盾 ===
        else if (isItem(item, "chocolatequest:swordspider")) {
            tooltip.add("");
            tooltip.add(TextFormatting.DARK_GREEN + "" + TextFormatting.BOLD + "蜘蛛剑盾特效");
            tooltip.add(TextFormatting.GREEN + "攻击附带中毒");
            tooltip.add(TextFormatting.RED + "暴击时附加额外毒伤");
            tooltip.add(TextFormatting.GRAY + "格挡时生成蛛网");
            tooltip.add(TextFormatting.GRAY + "对中毒目标伤害提升");
        }
        // === 阳光剑 ===
        else if (isItem(item, "chocolatequest:swordsunlight")) {
            tooltip.add("");
            tooltip.add(TextFormatting.YELLOW + "" + TextFormatting.BOLD + "阳光之剑");
            tooltip.add(TextFormatting.GRAY + "白天攻击力提升");
            tooltip.add(TextFormatting.GRAY + "攻击亡灵生物造成额外伤害");
            tooltip.add(TextFormatting.GRAY + "格挡时产生光芒致盲周围敌人");
        }
        // === 月光剑 ===
        else if (isItem(item, "chocolatequest:moonsword")) {
            tooltip.add("");
            tooltip.add(TextFormatting.DARK_PURPLE + "" + TextFormatting.BOLD + "月光之剑");
            tooltip.add(TextFormatting.GRAY + "夜晚攻击力提升");
            tooltip.add(TextFormatting.GRAY + "攻击时有几率偷取生命");
            tooltip.add(TextFormatting.GRAY + "格挡时隐身");
        }
        // === 行者之剑 ===
        else if (isItem(item, "chocolatequest:endsword")) {
            tooltip.add("");
            tooltip.add(TextFormatting.DARK_GRAY + "" + TextFormatting.BOLD + "末影行者之剑");
            tooltip.add(TextFormatting.GRAY + "攻击时随机传送敌人");
            tooltip.add(TextFormatting.GRAY + "格挡时传送躲避攻击");
            tooltip.add(TextFormatting.GRAY + "对末影人造成额外伤害");
            tooltip.add(TextFormatting.GRAY + "右键使用短距离传送");
        }
    }
}
