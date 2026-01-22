package com.chocotweak.command;

import com.chocotweak.ChocoTweak;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.io.*;
import java.util.*;

/**
 * 结构工具类
 * 处理 schematic 的加载、保存和生成
 */
public class StructureUtils {

    private static File structuresDir;
    private static File cqBuildingDir;

    /**
     * 初始化目录路径
     */
    public static void init(File configDir) {
        // ChocoTweak 保存目录
        structuresDir = new File(configDir, "chocotweak/structures");
        if (!structuresDir.exists()) {
            structuresDir.mkdirs();
        }

        // CQ Building 目录
        cqBuildingDir = new File(configDir, "Chocolate/Building");

        ChocoTweak.LOGGER.info("Structure save directory: {}", structuresDir.getAbsolutePath());
    }

    /**
     * 列出所有可用的 schematic 文件
     */
    public static List<String> listAvailableStructures() {
        List<String> structures = new ArrayList<>();

        // 扫描 CQ Building 目录
        if (cqBuildingDir != null && cqBuildingDir.exists()) {
            scanForSchematics(cqBuildingDir, "", structures);
        }

        // 扫描 ChocoTweak 目录
        if (structuresDir != null && structuresDir.exists()) {
            for (File file : structuresDir.listFiles()) {
                if (file.getName().endsWith(".schematic")) {
                    structures.add("[CT] " + file.getName().replace(".schematic", ""));
                }
            }
        }

        return structures;
    }

    private static void scanForSchematics(File dir, String prefix, List<String> results) {
        File[] files = dir.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanForSchematics(file, prefix + file.getName() + "/", results);
            } else if (file.getName().endsWith(".schematic")) {
                results.add(prefix + file.getName().replace(".schematic", ""));
            }
        }
    }

    /**
     * 生成结构
     */
    public static boolean spawnStructure(World world, BlockPos pos, String name) {
        File schematicFile = findSchematicFile(name);
        if (schematicFile == null || !schematicFile.exists()) {
            return false;
        }

        try {
            NBTTagCompound nbt = CompressedStreamTools.readCompressed(new FileInputStream(schematicFile));

            short width = nbt.getShort("Width");
            short height = nbt.getShort("Height");
            short length = nbt.getShort("Length");

            byte[] blockIds = nbt.getByteArray("Blocks");
            byte[] blockData = nbt.getByteArray("Data");
            NBTTagList tileEntities = nbt.getTagList("TileEntities", Constants.NBT.TAG_COMPOUND);
            NBTTagList entities = nbt.getTagList("Entities", Constants.NBT.TAG_COMPOUND);

            // 放置方块
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    for (int x = 0; x < width; x++) {
                        int index = y * width * length + z * width + x;
                        if (index < blockIds.length) {
                            int blockId = blockIds[index] & 0xFF;
                            int meta = blockData[index] & 0xF;

                            Block block = Block.getBlockById(blockId);
                            if (block != null) {
                                BlockPos targetPos = pos.add(x, y, z);
                                IBlockState state = block.getStateFromMeta(meta);
                                world.setBlockState(targetPos, state, 2);
                            }
                        }
                    }
                }
            }

            // 放置 TileEntity
            for (int i = 0; i < tileEntities.tagCount(); i++) {
                NBTTagCompound teTag = tileEntities.getCompoundTagAt(i);
                int x = teTag.getInteger("x");
                int y = teTag.getInteger("y");
                int z = teTag.getInteger("z");

                BlockPos tePos = pos.add(x, y, z);
                teTag.setInteger("x", tePos.getX());
                teTag.setInteger("y", tePos.getY());
                teTag.setInteger("z", tePos.getZ());

                TileEntity te = world.getTileEntity(tePos);
                if (te != null) {
                    te.readFromNBT(teTag);
                }
            }

            // 生成实体
            for (int i = 0; i < entities.tagCount(); i++) {
                NBTTagCompound entityTag = entities.getCompoundTagAt(i);
                NBTTagList posList = entityTag.getTagList("Pos", Constants.NBT.TAG_DOUBLE);

                double ex = posList.getDoubleAt(0) + pos.getX();
                double ey = posList.getDoubleAt(1) + pos.getY();
                double ez = posList.getDoubleAt(2) + pos.getZ();

                Entity entity = EntityList.createEntityFromNBT(entityTag, world);
                if (entity != null) {
                    entity.setPosition(ex, ey, ez);
                    world.spawnEntity(entity);
                }
            }

            ChocoTweak.LOGGER.info("Spawned structure {} at {}", name, pos);
            return true;

        } catch (Exception e) {
            ChocoTweak.LOGGER.error("Failed to spawn structure: " + name, e);
            return false;
        }
    }

    private static File findSchematicFile(String name) {
        // 检查 ChocoTweak 目录
        if (name.startsWith("[CT] ")) {
            name = name.substring(5);
            return new File(structuresDir, name + ".schematic");
        }

        // 检查 CQ Building 目录
        if (cqBuildingDir != null) {
            File file = new File(cqBuildingDir, name + ".schematic");
            if (file.exists())
                return file;

            // 递归搜索
            return searchFile(cqBuildingDir, name + ".schematic");
        }

        return null;
    }

    private static File searchFile(File dir, String fileName) {
        File[] files = dir.listFiles();
        if (files == null)
            return null;

        for (File file : files) {
            if (file.isDirectory()) {
                File found = searchFile(file, fileName);
                if (found != null)
                    return found;
            } else if (file.getName().equals(fileName)) {
                return file;
            }
        }
        return null;
    }

    /**
     * 保存结构到 ChocoTweak 目录
     */
    public static File saveStructure(World world, BlockPos pos1, BlockPos pos2, String name) throws IOException {
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        short width = (short) (maxX - minX + 1);
        short height = (short) (maxY - minY + 1);
        short length = (short) (maxZ - minZ + 1);

        byte[] blocks = new byte[width * height * length];
        byte[] data = new byte[width * height * length];
        NBTTagList tileEntitiesList = new NBTTagList();
        NBTTagList entitiesList = new NBTTagList();

        // 收集方块
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    BlockPos worldPos = new BlockPos(minX + x, minY + y, minZ + z);
                    IBlockState state = world.getBlockState(worldPos);
                    int index = y * width * length + z * width + x;

                    blocks[index] = (byte) Block.getIdFromBlock(state.getBlock());
                    data[index] = (byte) state.getBlock().getMetaFromState(state);

                    // 收集 TileEntity
                    TileEntity te = world.getTileEntity(worldPos);
                    if (te != null) {
                        NBTTagCompound teTag = new NBTTagCompound();
                        te.writeToNBT(teTag);
                        teTag.setInteger("x", x);
                        teTag.setInteger("y", y);
                        teTag.setInteger("z", z);
                        tileEntitiesList.appendTag(teTag);
                    }
                }
            }
        }

        // 收集实体（跳过玩家）
        AxisAlignedBB area = new AxisAlignedBB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, area);
        for (Entity entity : entities) {
            // 跳过玩家
            if (entity instanceof net.minecraft.entity.player.EntityPlayer) {
                continue;
            }

            try {
                NBTTagCompound entityTag = new NBTTagCompound();
                // 使用 writeToNBTAtomically 确保完整保存（包括 id 标签）
                entity.writeToNBTAtomically(entityTag);

                // 确保有实体 ID
                if (!entityTag.hasKey("id")) {
                    net.minecraft.util.ResourceLocation id = EntityList.getKey(entity);
                    if (id != null) {
                        entityTag.setString("id", id.toString());
                    } else {
                        continue; // 没有 ID 的实体跳过
                    }
                }

                // 转换为相对坐标
                NBTTagList posList = new NBTTagList();
                posList.appendTag(new net.minecraft.nbt.NBTTagDouble(entity.posX - minX));
                posList.appendTag(new net.minecraft.nbt.NBTTagDouble(entity.posY - minY));
                posList.appendTag(new net.minecraft.nbt.NBTTagDouble(entity.posZ - minZ));
                entityTag.setTag("Pos", posList);

                // 清除 UUID 避免重复
                entityTag.removeTag("UUIDMost");
                entityTag.removeTag("UUIDLeast");
                entityTag.removeTag("UUID");

                entitiesList.appendTag(entityTag);
                ChocoTweak.LOGGER.info("Saved entity: {}", entityTag.getString("id"));
            } catch (Exception e) {
                ChocoTweak.LOGGER.warn("Failed to save entity: {}", entity.getClass().getName(), e);
            }
        }

        // 构建 NBT
        NBTTagCompound schematic = new NBTTagCompound();
        schematic.setShort("Width", width);
        schematic.setShort("Height", height);
        schematic.setShort("Length", length);
        schematic.setByteArray("Blocks", blocks);
        schematic.setByteArray("Data", data);
        schematic.setTag("TileEntities", tileEntitiesList);
        schematic.setTag("Entities", entitiesList);
        schematic.setString("Materials", "Alpha");

        // 保存文件
        File outputFile = new File(structuresDir, name + ".schematic");
        CompressedStreamTools.writeCompressed(schematic, new FileOutputStream(outputFile));

        ChocoTweak.LOGGER.info("Saved structure {} ({} x {} x {})", name, width, height, length);
        return outputFile;
    }
}
