package com.chocotweak.command;

import com.chocotweak.ChocoTweak;
import com.chocotweak.gui.ChocoTweakGuiHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

/**
 * 药水灌注命令
 * 
 * /potioninfuse - 打开药水灌注GUI
 */
public class CommandPotionInfuse extends CommandBase {

    @Override
    public String getName() {
        return "potioninfuse";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/potioninfuse - Open potion infusion GUI";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // 所有玩家可用
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Only players can use this command"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;
        
        // 打开药水灌注GUI
        player.openGui(ChocoTweak.instance, ChocoTweakGuiHandler.GUI_POTION_INFUSION, 
                player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
    }
}
