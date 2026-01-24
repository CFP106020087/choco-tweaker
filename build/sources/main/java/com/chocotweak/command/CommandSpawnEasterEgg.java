package com.chocotweak.command;

import com.chocolate.chocolateQuest.entity.npc.EntityHumanNPC;
import com.chocotweak.bedrock.entity.IEasterEggCapable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Command to spawn an Easter Egg NPC for testing.
 * Usage: /easteregg [variant]
 * variant: 0-14 for different wine_fox skins
 */
public class CommandSpawnEasterEgg extends CommandBase {
    
    @Override
    public String getName() {
        return "easteregg";
    }
    
    @Override
    public String getUsage(ICommandSender sender) {
        return "/easteregg [variant 0-14]";
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 2;  // OP level
    }
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            sender.sendMessage(new TextComponentString("Must be run by a player"));
            return;
        }
        
        EntityPlayer player = (EntityPlayer) sender;
        World world = player.world;
        
        // Parse variant
        int variant = 0;
        if (args.length > 0) {
            try {
                variant = Integer.parseInt(args[0]);
                variant = Math.max(0, Math.min(14, variant));
            } catch (NumberFormatException e) {
                // Use random variant
                variant = world.rand.nextInt(15);
            }
        } else {
            // Random variant
            variant = world.rand.nextInt(15);
        }
        
        // Spawn CQ NPC
        EntityHumanNPC npc = new EntityHumanNPC(world);
        npc.setPosition(player.posX + 2, player.posY, player.posZ);
        
        // Set as Easter Egg
        if (npc instanceof IEasterEggCapable) {
            IEasterEggCapable easterEgg = (IEasterEggCapable) npc;
            easterEgg.setEasterEggNpc(true);
            easterEgg.setModelVariant(variant);
            
            // Set NPC properties
            npc.setCustomNameTag("§d酒狐 §7[彩蛋]");
            npc.setAlwaysRenderNameTag(true);
            
            world.spawnEntity(npc);
            
            String[] variantNames = {
                "大正女仆", "新年", "宇航员", "功夫", "魔法少女",
                "汉服", "JK", "STA", "海螺", "值班",
                "推销员", "小", "成熟", "桃桃", "克鲁诺亚"
            };
            
            sender.sendMessage(new TextComponentString(
                "§a已生成酒狐彩蛋 NPC §6[" + variantNames[variant] + "]§a (变体 " + variant + ")"
            ));
        } else {
            sender.sendMessage(new TextComponentString("§cMixin 未生效，无法设置为彩蛋 NPC"));
            world.spawnEntity(npc);
        }
    }
    
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, 
            String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, 
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14");
        }
        return Collections.emptyList();
    }
}
