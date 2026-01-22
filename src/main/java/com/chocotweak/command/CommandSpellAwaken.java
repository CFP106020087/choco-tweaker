package com.chocotweak.command;

import com.chocotweak.magic.SpellAwakeningUnlockTracker;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

/**
 * 法术觉醒解锁命令
 * 
 * /spellawaken unlock - 解锁下一个觉醒
 * /spellawaken unlockall - 解锁所有觉醒
 * /spellawaken reset - 重置所有解锁
 * /spellawaken status - 查看解锁状态
 */
public class CommandSpellAwaken extends CommandBase {

    @Override
    public String getName() {
        return "spellawaken";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/spellawaken <unlock|unlockall|reset|status>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // 所有玩家可用
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "只有玩家可以使用此命令"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;

        if (args.length == 0) {
            showUsage(player);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "unlock":
                String unlocked = SpellAwakeningUnlockTracker.unlockNext(player);
                if (unlocked != null) {
                    int count = SpellAwakeningUnlockTracker.getUnlockedCount(player);
                    player.sendMessage(new TextComponentString(
                            TextFormatting.GREEN + "✓ 解锁觉醒: " + TextFormatting.GOLD + getAwakeningDisplayName(unlocked)
                                    +
                                    TextFormatting.GRAY + " (" + count + "/"
                                    + SpellAwakeningUnlockTracker.UNLOCK_ORDER.length + ")"));
                } else {
                    player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "所有觉醒已解锁！"));
                }
                break;

            case "unlockall":
                SpellAwakeningUnlockTracker.unlockAll(player);
                player.sendMessage(new TextComponentString(TextFormatting.GREEN + "✓ 已解锁所有法术觉醒！"));
                break;

            case "reset":
                SpellAwakeningUnlockTracker.resetAll(player);
                player.sendMessage(new TextComponentString(TextFormatting.YELLOW + "已重置所有法术觉醒解锁状态"));
                break;

            case "status":
                showStatus(player);
                break;

            default:
                showUsage(player);
        }
    }

    private void showUsage(EntityPlayer player) {
        player.sendMessage(new TextComponentString(TextFormatting.GOLD + "=== 法术觉醒命令 ==="));
        player.sendMessage(new TextComponentString("/spellawaken unlock - 解锁下一个觉醒"));
        player.sendMessage(new TextComponentString("/spellawaken unlockall - 解锁所有觉醒"));
        player.sendMessage(new TextComponentString("/spellawaken reset - 重置所有解锁"));
        player.sendMessage(new TextComponentString("/spellawaken status - 查看解锁状态"));
    }

    private void showStatus(EntityPlayer player) {
        player.sendMessage(new TextComponentString(TextFormatting.GOLD + "=== 法术觉醒解锁状态 ==="));

        for (int i = 0; i < SpellAwakeningUnlockTracker.UNLOCK_ORDER.length; i++) {
            String id = SpellAwakeningUnlockTracker.UNLOCK_ORDER[i];
            boolean unlocked = SpellAwakeningUnlockTracker.isUnlocked(player, id);
            String status = unlocked ? TextFormatting.GREEN + "✓ 已解锁"
                    : TextFormatting.RED + "✗ 未解锁 (任务" + (i + 1) + ")";

            player.sendMessage(new TextComponentString(
                    TextFormatting.WHITE.toString() + (i + 1) + ". " + getAwakeningDisplayName(id) + " - " + status));
        }
    }

    private String getAwakeningDisplayName(String id) {
        switch (id) {
            case SpellAwakeningUnlockTracker.AWAKENING_HIGH_SPEED_CHANT:
                return "高速神言";
            case SpellAwakeningUnlockTracker.AWAKENING_SPELL_AMPLIFICATION:
                return "咒文增幅";
            case SpellAwakeningUnlockTracker.AWAKENING_INSTANT_CAST:
                return "瞬发咏唱";
            case SpellAwakeningUnlockTracker.AWAKENING_MANA_SURGE:
                return "魔力洪流";
            case SpellAwakeningUnlockTracker.AWAKENING_LONG_RANGE:
                return "远距咏唱";
            case SpellAwakeningUnlockTracker.AWAKENING_ECHOING_VOICE:
                return "回响之音";
            default:
                return id;
        }
    }
}
