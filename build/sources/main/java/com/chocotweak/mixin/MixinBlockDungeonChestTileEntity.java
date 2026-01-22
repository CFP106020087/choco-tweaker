package com.chocotweak.mixin;

import com.chocotweak.config.CQTweakConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Random;

/**
 * Mixin to add vanilla loot table support to BlockDungeonChestTileEntity
 * 为 BlockDungeonChestTileEntity 添加原版战利品表支持
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.block.BlockDungeonChestTileEntity", remap = false)
public abstract class MixinBlockDungeonChestTileEntity extends TileEntity implements IInventory {

    @Shadow
    NonNullList<ItemStack> container;

    @Unique
    private boolean chocotweak$lootGenerated = false;

    @Unique
    private ResourceLocation chocotweak$lootTable = null;

    @Unique
    private long chocotweak$lootTableSeed = 0L;

    /**
     * 当玩家打开箱子时检查是否需要生成战利品
     */
    @Inject(method = "openInventory", at = @At("HEAD"))
    private void onOpenInventory(EntityPlayer player, CallbackInfo ci) {
        if (!this.world.isRemote && !this.chocotweak$lootGenerated) {
            this.chocotweak$tryGenerateLoot(player);
        }
    }

    /**
     * 尝试从配置的战利品表生成物品
     */
    @Unique
    private void chocotweak$tryGenerateLoot(EntityPlayer player) {
        // 检查配置是否启用
        if (!CQTweakConfig.drops.useVanillaLootTables) {
            return;
        }

        // 检查路径是否为空（为空则使用原本战利品）
        String lootPath = CQTweakConfig.drops.customLootPath;
        if (lootPath == null || lootPath.isEmpty()) {
            return;
        }

        // 检查箱子是否已经有物品（如果有则不覆盖）
        if (!this.chocotweak$isChestEmpty()) {
            this.chocotweak$lootGenerated = true;
            return;
        }

        try {
            // 解析战利品表路径
            ResourceLocation lootTableLocation;
            if (lootPath.contains(":")) {
                lootTableLocation = new ResourceLocation(lootPath);
            } else {
                // 默认使用 minecraft 命名空间
                lootTableLocation = new ResourceLocation("minecraft", lootPath);
            }

            if (this.world instanceof WorldServer) {
                WorldServer worldServer = (WorldServer) this.world;
                LootTable lootTable = worldServer.getLootTableManager().getLootTableFromLocation(lootTableLocation);

                if (lootTable != LootTable.EMPTY_LOOT_TABLE) {
                    Random random = new Random();
                    if (this.chocotweak$lootTableSeed != 0L) {
                        random.setSeed(this.chocotweak$lootTableSeed);
                    }

                    LootContext.Builder contextBuilder = new LootContext.Builder(worldServer);
                    if (player != null) {
                        contextBuilder.withPlayer(player).withLuck(player.getLuck());
                    }

                    // 清空并填充箱子
                    this.clear();
                    List<ItemStack> lootItems = lootTable.generateLootForPools(random, contextBuilder.build());

                    // 将物品放入箱子
                    int slot = 0;
                    for (ItemStack stack : lootItems) {
                        if (slot >= this.getSizeInventory())
                            break;
                        this.setInventorySlotContents(slot++, stack);
                    }

                    System.out.println("[ChocoTweak] Generated loot from table: " + lootTableLocation);
                } else {
                    System.out.println(
                            "[ChocoTweak] Loot table not found: " + lootTableLocation + ", using original loot");
                }
            }
        } catch (Exception e) {
            System.err.println("[ChocoTweak] Error generating loot: " + e.getMessage());
            // 生成失败时保留原本战利品
        }

        this.chocotweak$lootGenerated = true;
    }

    /**
     * 检查箱子是否为空
     */
    @Unique
    private boolean chocotweak$isChestEmpty() {
        if (this.container == null)
            return true;
        for (ItemStack stack : this.container) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}


