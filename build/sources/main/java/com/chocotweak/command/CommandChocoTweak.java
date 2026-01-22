package com.chocotweak.command;

import com.chocotweak.ChocoTweak;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

/**
 * /chocotweak 命令
 * 
 * 用法:
 * /chocotweak spawn <结构名> - 在玩家位置生成结构
 * /chocotweak save <名称> - 保存选区到 ChocoTweak 目录
 * /chocotweak list - 列出可用结构
 * /chocotweak pos1 - 设置选区点1
 * /chocotweak pos2 - 设置选区点2
 */
public class CommandChocoTweak extends CommandBase {

    // 玩家选区存储
    private static Map<UUID, BlockPos> pos1Map = new HashMap<>();
    private static Map<UUID, BlockPos> pos2Map = new HashMap<>();

    @Override
    public String getName() {
        return "chocotweak";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/chocotweak <spawn|save|list|pos1|pos2> [参数]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // OP 权限
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sendHelp(sender);
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "spawn":
                handleSpawn(server, sender, args);
                break;
            case "save":
                handleSave(server, sender, args);
                break;
            case "list":
                handleList(sender);
                break;
            case "pos1":
                handlePos1(sender);
                break;
            case "pos2":
                handlePos2(sender);
                break;
            case "creature":
                handleCreature(sender);
                break;
            case "enchant":
                handleEnchant(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }
    }

    private void sendHelp(ICommandSender sender) {
        sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "=== Choco Tweak 命令 ==="));
        sender.sendMessage(new TextComponentString(
                TextFormatting.YELLOW + "/chocotweak spawn <结构名>" + TextFormatting.WHITE + " - 在脚下生成结构"));
        sender.sendMessage(new TextComponentString(
                TextFormatting.YELLOW + "/chocotweak save <名称>" + TextFormatting.WHITE + " - 保存选区为 schematic"));
        sender.sendMessage(new TextComponentString(
                TextFormatting.YELLOW + "/chocotweak list" + TextFormatting.WHITE + " - 列出可用的 schematic"));
        sender.sendMessage(new TextComponentString(
                TextFormatting.YELLOW + "/chocotweak pos1" + TextFormatting.WHITE + " - 设置选区点1 (脚下)"));
        sender.sendMessage(new TextComponentString(
                TextFormatting.YELLOW + "/chocotweak pos2" + TextFormatting.WHITE + " - 设置选区点2 (脚下)"));
        sender.sendMessage(new TextComponentString(
                TextFormatting.YELLOW + "/chocotweak creature" + TextFormatting.WHITE + " - [创造] 添加购买驯服生物"));
        sender.sendMessage(new TextComponentString(
                TextFormatting.YELLOW + "/chocotweak enchant" + TextFormatting.WHITE + " - [创造] 添加附魔服务"));
    }

    private void handleCreature(ICommandSender sender) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            throw new CommandException("必须由玩家执行此命令");
        }
        EntityPlayer player = (EntityPlayer) sender;
        if (!player.isCreative()) {
            throw new CommandException("此命令只能在创造模式下使用");
        }
        // 提示用户 - GUI 在客户端通过 ClientChatEvent 拦截打开
        sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "生物选择器已打开 (请在创造模式下直接输入命令)"));
    }

    private void handleEnchant(ICommandSender sender) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            throw new CommandException("必须由玩家执行此命令");
        }
        EntityPlayer player = (EntityPlayer) sender;
        if (!player.isCreative()) {
            throw new CommandException("此命令只能在创造模式下使用");
        }
        // 提示用户 - GUI 在客户端通过 ClientChatEvent 拦截打开
        sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "附魔选择器已打开 (请在创造模式下直接输入命令)"));
    }

    private void handleSpawn(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            throw new CommandException("必须由玩家执行此命令");
        }

        if (args.length < 2) {
            throw new CommandException("用法: /chocotweak spawn <结构名>");
        }

        EntityPlayer player = (EntityPlayer) sender;

        // 支持带空格的名称 - 连接所有参数
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1)
                nameBuilder.append(" ");
            nameBuilder.append(args[i]);
        }
        String structureName = nameBuilder.toString();

        try {
            boolean success = StructureUtils.spawnStructure(player.world, player.getPosition(), structureName);
            if (success) {
                sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "成功生成结构: " + structureName));
            } else {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "找不到结构: " + structureName));
            }
        } catch (Exception e) {
            ChocoTweak.LOGGER.error("生成结构失败", e);
            throw new CommandException("生成结构失败: " + e.getMessage());
        }
    }

    private void handleSave(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            throw new CommandException("必须由玩家执行此命令");
        }

        if (args.length < 2) {
            throw new CommandException("用法: /chocotweak save <名称>");
        }

        EntityPlayer player = (EntityPlayer) sender;
        UUID playerId = player.getUniqueID();
        String name = args[1];

        BlockPos p1 = pos1Map.get(playerId);
        BlockPos p2 = pos2Map.get(playerId);

        if (p1 == null || p2 == null) {
            throw new CommandException("请先用 /chocotweak pos1 和 pos2 设置选区!");
        }

        try {
            File savedFile = StructureUtils.saveStructure(player.world, p1, p2, name);
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "结构已保存到: " + savedFile.getPath()));
        } catch (Exception e) {
            ChocoTweak.LOGGER.error("保存结构失败", e);
            throw new CommandException("保存结构失败: " + e.getMessage());
        }
    }

    private void handleList(ICommandSender sender) {
        List<String> structures = StructureUtils.listAvailableStructures();

        sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "=== 可用结构 (" + structures.size() + ") ==="));
        for (String name : structures) {
            sender.sendMessage(new TextComponentString(TextFormatting.WHITE + "  - " + name));
        }
    }

    private void handlePos1(ICommandSender sender) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            throw new CommandException("必须由玩家执行此命令");
        }

        EntityPlayer player = (EntityPlayer) sender;
        BlockPos pos = player.getPosition();
        pos1Map.put(player.getUniqueID(), pos);

        sender.sendMessage(new TextComponentString(
                TextFormatting.GREEN + "选区点1 设置为: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
    }

    private void handlePos2(ICommandSender sender) throws CommandException {
        if (!(sender instanceof EntityPlayer)) {
            throw new CommandException("必须由玩家执行此命令");
        }

        EntityPlayer player = (EntityPlayer) sender;
        BlockPos pos = player.getPosition();
        pos2Map.put(player.getUniqueID(), pos);

        sender.sendMessage(new TextComponentString(
                TextFormatting.GREEN + "选区点2 设置为: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));

        // 显示选区大小
        BlockPos p1 = pos1Map.get(player.getUniqueID());
        if (p1 != null) {
            int sizeX = Math.abs(pos.getX() - p1.getX()) + 1;
            int sizeY = Math.abs(pos.getY() - p1.getY()) + 1;
            int sizeZ = Math.abs(pos.getZ() - p1.getZ()) + 1;
            sender.sendMessage(
                    new TextComponentString(TextFormatting.AQUA + "选区大小: " + sizeX + " x " + sizeY + " x " + sizeZ));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
            @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "spawn", "save", "list", "pos1", "pos2");
        }
        if (args.length == 2 && "spawn".equals(args[0])) {
            return getListOfStringsMatchingLastWord(args, StructureUtils.listAvailableStructures());
        }
        return Collections.emptyList();
    }

    public static BlockPos getPos1(EntityPlayer player) {
        return pos1Map.get(player.getUniqueID());
    }

    public static BlockPos getPos2(EntityPlayer player) {
        return pos2Map.get(player.getUniqueID());
    }
}
