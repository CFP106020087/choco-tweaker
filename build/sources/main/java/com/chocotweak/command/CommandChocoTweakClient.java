package com.chocotweak.command;

import com.chocotweak.gui.GuiSelectCreature;
import com.chocotweak.gui.GuiSelectEnchantment;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

/**
 * 客户端命令 - 用于打开 GUI
 * 通过 ClientCommandHandler 注册，只在客户端运行
 */
@SideOnly(Side.CLIENT)
public class CommandChocoTweakClient extends CommandBase {

    @Override
    public String getName() {
        return "ctc"; // ChocoTweak Client
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/ctc <creature|enchant>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // 客户端命令不需要权限
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.player == null) {
            return;
        }

        if (!mc.player.isCreative()) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "此命令只能在创造模式下使用"));
            return;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "creature":
            case "c":
                mc.addScheduledTask(() -> {
                    mc.displayGuiScreen(new GuiSelectCreature(null));
                });
                break;
            case "enchant":
            case "e":
                mc.addScheduledTask(() -> {
                    mc.displayGuiScreen(new GuiSelectEnchantment(null));
                });
                break;
            default:
                sendHelp(sender);
                break;
        }
    }

    private void sendHelp(ICommandSender sender) {
        sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "=== Choco Tweak 客户端命令 ==="));
        sender.sendMessage(new TextComponentString(
                TextFormatting.YELLOW + "/ctc creature" + TextFormatting.WHITE + " - 打开生物选择器"));
        sender.sendMessage(new TextComponentString(
                TextFormatting.YELLOW + "/ctc enchant" + TextFormatting.WHITE + " - 打开附魔选择器"));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
            net.minecraft.util.math.BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "creature", "enchant");
        }
        return Collections.emptyList();
    }
}
