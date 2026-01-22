package com.chocotweak.dialog;

import com.chocolate.chocolateQuest.entity.npc.EntityHumanNPC;
import com.chocolate.chocolateQuest.quest.DialogAction;
import com.chocotweak.ChocoTweak;
import com.chocotweak.config.NpcDialogConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 对话动作 - 购买驯服的生物
 * 玩家支付物品后获得一只已驯服的生物
 * 
 * 使用方法:
 * - name: 生物ID (如 minecraft:wolf)
 * - surname: 价格配置，格式 "物品ID:数量,物品ID:数量" (如
 * minecraft:diamond:5,minecraft:emerald:10)
 * - 如果 surname 为空，则从 npc_dialog.json 读取配置
 */
public class DialogActionBuyCreature extends DialogAction {

    @Override
    public void execute(EntityPlayer player, EntityHumanNPC npc) {
        if (npc.world.isRemote) {
            return; // 只在服务端执行
        }

        String entityId = this.name;
        if (entityId == null || entityId.isEmpty()) {
            player.sendMessage(new TextComponentTranslation("chocotweak.action.no_entity_id")
                    .setStyle(new net.minecraft.util.text.Style().setColor(TextFormatting.RED)));
            return;
        }

        // 解析价格配置
        List<CostEntry> costs = parseCostItems();

        if (!costs.isEmpty()) {
            // 检查并消耗物品
            if (!consumeCostItems(player, costs)) {
                player.sendMessage(new TextComponentTranslation("chocotweak.action.not_enough_items")
                        .setStyle(new net.minecraft.util.text.Style().setColor(TextFormatting.RED)));
                return;
            }
        }

        // 生成驯服的生物
        spawnTamedCreature(player, npc, entityId);
    }

    /**
     * 解析价格配置
     * 优先使用 surname 字段，如果为空则从配置文件读取
     */
    private List<CostEntry> parseCostItems() {
        List<CostEntry> costs = new ArrayList<>();

        // 优先使用 surname 字段配置
        if (this.surname != null && !this.surname.isEmpty()) {
            // 格式: "minecraft:diamond:5,minecraft:emerald:10"
            String[] items = this.surname.split(",");
            for (String itemDef : items) {
                String[] parts = itemDef.trim().split(":");
                if (parts.length >= 2) {
                    String itemId;
                    int count = 1;
                    int meta = 0;

                    if (parts.length == 2) {
                        // 格式: itemId:count (无命名空间)
                        itemId = "minecraft:" + parts[0];
                        try {
                            count = Integer.parseInt(parts[1]);
                        } catch (Exception e) {
                        }
                    } else if (parts.length == 3) {
                        // 格式: modid:itemId:count
                        itemId = parts[0] + ":" + parts[1];
                        try {
                            count = Integer.parseInt(parts[2]);
                        } catch (Exception e) {
                        }
                    } else {
                        // 格式: modid:itemId:count:meta
                        itemId = parts[0] + ":" + parts[1];
                        try {
                            count = Integer.parseInt(parts[2]);
                        } catch (Exception e) {
                        }
                        try {
                            meta = Integer.parseInt(parts[3]);
                        } catch (Exception e) {
                        }
                    }

                    costs.add(new CostEntry(itemId, count, meta));
                }
            }
        }

        // 如果 surname 为空，从配置文件读取
        if (costs.isEmpty() && this.name != null) {
            NpcDialogConfig.CreatureEntry entry = findCreatureEntry(this.name);
            if (entry != null && entry.costItems != null) {
                for (NpcDialogConfig.CostItem cost : entry.costItems) {
                    costs.add(new CostEntry(cost.itemId, cost.count, cost.metadata));
                }
            }
        }

        return costs;
    }

    private NpcDialogConfig.CreatureEntry findCreatureEntry(String entityId) {
        List<NpcDialogConfig.CreatureEntry> creatures = NpcDialogConfig.getAvailableCreatures();
        if (creatures == null)
            return null;

        for (NpcDialogConfig.CreatureEntry entry : creatures) {
            if (entityId.equals(entry.entityId)) {
                return entry;
            }
        }
        return null;
    }

    private boolean consumeCostItems(EntityPlayer player, List<CostEntry> costs) {
        // 先检查玩家是否有足够物品
        for (CostEntry cost : costs) {
            Item item = Item.getByNameOrId(cost.itemId);
            if (item == null) {
                ChocoTweak.LOGGER.warn("Unknown item: {}", cost.itemId);
                continue;
            }

            int count = countItem(player, item, cost.metadata);
            if (count < cost.count) {
                return false;
            }
        }

        // 消耗物品
        for (CostEntry cost : costs) {
            Item item = Item.getByNameOrId(cost.itemId);
            if (item == null)
                continue;

            removeItem(player, item, cost.metadata, cost.count);
        }
        return true;
    }

    private int countItem(EntityPlayer player, Item item, int meta) {
        int count = 0;
        for (ItemStack stack : player.inventory.mainInventory) {
            if (!stack.isEmpty() && stack.getItem() == item &&
                    (meta == 0 || stack.getMetadata() == meta)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void removeItem(EntityPlayer player, Item item, int meta, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.inventory.mainInventory.size() && remaining > 0; i++) {
            ItemStack stack = player.inventory.mainInventory.get(i);
            if (!stack.isEmpty() && stack.getItem() == item &&
                    (meta == 0 || stack.getMetadata() == meta)) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.shrink(toRemove);
                remaining -= toRemove;
            }
        }
    }

    private void spawnTamedCreature(EntityPlayer player, EntityHumanNPC npc, String entityId) {
        Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(entityId), npc.world);
        if (entity == null) {
            player.sendMessage(new TextComponentTranslation("chocotweak.action.entity_not_found", entityId)
                    .setStyle(new net.minecraft.util.text.Style().setColor(TextFormatting.RED)));
            return;
        }

        // 设置位置
        entity.setPosition(player.posX + 1, player.posY, player.posZ);

        // 尝试驯服
        boolean tamed = tryTame(entity, player);

        npc.world.spawnEntity(entity);

        // 使用 entity.getName() 自动获取翻译后的名称
        if (tamed) {
            player.sendMessage(new TextComponentTranslation("chocotweak.action.tame_success", entity.getName())
                    .setStyle(new net.minecraft.util.text.Style().setColor(TextFormatting.GREEN)));
        } else {
            player.sendMessage(new TextComponentTranslation("chocotweak.action.spawn_success", entity.getName())
                    .setStyle(new net.minecraft.util.text.Style().setColor(TextFormatting.YELLOW)));
        }
    }

    private boolean tryTame(Entity entity, EntityPlayer player) {
        // 方法1: EntityTameable
        if (entity instanceof EntityTameable) {
            EntityTameable tameable = (EntityTameable) entity;
            tameable.setTamed(true);
            tameable.setOwnerId(player.getUniqueID());
            return true;
        }

        // 方法2: 反射尝试各种驯服方法
        try {
            Method setTamed = entity.getClass().getMethod("setTamed", boolean.class);
            setTamed.invoke(entity, true);

            try {
                Method setOwner = entity.getClass().getMethod("setOwnerId", java.util.UUID.class);
                setOwner.invoke(entity, player.getUniqueID());
                return true;
            } catch (Exception e) {
                try {
                    Method setTamedBy = entity.getClass().getMethod("setTamedBy", EntityPlayer.class);
                    setTamedBy.invoke(entity, player);
                    return true;
                } catch (Exception e2) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean hasName() {
        return true; // 生物ID
    }

    @Override
    public boolean hasSurname() {
        return true; // 价格配置
    }

    @Override
    public boolean hasValue() {
        return false;
    }

    @Override
    public void getSuggestions(java.util.List list) {
        list.add("minecraft:wolf");
        list.add("minecraft:ocelot");
        list.add("minecraft:parrot");
        list.add("minecraft:horse");
        list.add("minecraft:donkey");
        list.add("minecraft:llama");
    }

    /**
     * 价格条目
     */
    private static class CostEntry {
        String itemId;
        int count;
        int metadata;

        CostEntry(String itemId, int count, int metadata) {
            this.itemId = itemId;
            this.count = count;
            this.metadata = metadata;
        }
    }
}
