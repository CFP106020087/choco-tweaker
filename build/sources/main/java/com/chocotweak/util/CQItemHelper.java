package com.chocotweak.util;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

/**
 * 安全的 CQ 物品访问工具
 * 使用注册名而非直接类引用，避免触发 CQ 类加载
 */
public class CQItemHelper {

    private static final Map<String, Item> ITEM_CACHE = new HashMap<>();

    /**
     * 通过注册名获取物品
     */
    public static Item getItem(String registryName) {
        return ITEM_CACHE.computeIfAbsent(registryName,
                name -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(name)));
    }

    /**
     * 检查物品是否为指定注册名
     */
    public static boolean isItem(Item item, String registryName) {
        if (item == null)
            return false;
        ResourceLocation loc = item.getRegistryName();
        return loc != null && loc.toString().equals(registryName);
    }

    /**
     * 检查物品类是否匹配指定类名（通过类继承链）
     */
    public static boolean isInstanceOf(Item item, String className) {
        if (item == null)
            return false;
        Class<?> clazz = item.getClass();
        while (clazz != null) {
            if (clazz.getName().equals(className)) {
                return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    // ========== 常用注册名常量 ==========
    public static final String CLOUD_BOOTS = "chocolatequest:cloudboots";
    public static final String DRAGON_HELMET = "chocolatequest:dragonhelmet";
    public static final String SCOUTER = "chocolatequest:scouter";
    public static final String WITCH_HAT = "chocolatequest:witchhat";
    public static final String BACKPACK = "chocolatequest:backpack";

    public static final String TURTLE_HELMET = "chocolatequest:turtlehelmet";
    public static final String TURTLE_PLATE = "chocolatequest:turtleplate";
    public static final String TURTLE_PANTS = "chocolatequest:turtlepants";
    public static final String TURTLE_BOOTS = "chocolatequest:turtleboots";

    public static final String BULL_HELMET = "chocolatequest:bullhelmet";
    public static final String BULL_PLATE = "chocolatequest:bullplate";
    public static final String BULL_PANTS = "chocolatequest:bullpants";
    public static final String BULL_BOOTS = "chocolatequest:bullboots";

    public static final String SPIDER_HELMET = "chocolatequest:spiderhelmet";
    public static final String SPIDER_PLATE = "chocolatequest:spiderplate";
    public static final String SPIDER_PANTS = "chocolatequest:spiderpants";
    public static final String SPIDER_BOOTS = "chocolatequest:spiderboots";

    public static final String SLIME_HELMET = "chocolatequest:slimehelmet";
    public static final String SLIME_PLATE = "chocolatequest:slimeplate";
    public static final String SLIME_PANTS = "chocolatequest:slimepants";
    public static final String SLIME_BOOTS = "chocolatequest:slimeboots";

    public static final String HOOK_SWORD = "chocolatequest:swordhook";
    public static final String TRICKSTER_DAGGER = "chocolatequest:daggertrickster";

    public static final String SWORD_TURTLE = "chocolatequest:swordturtle";
    public static final String SWORD_SPIDER = "chocolatequest:swordspider";
    public static final String SWORD_SUNLIGHT = "chocolatequest:swordsunlight";
    public static final String SWORD_MOONLIGHT = "chocolatequest:moonsword";
    public static final String END_SWORD = "chocolatequest:endsword";
    public static final String RUSTED_SWORD = "chocolatequest:rustedswordandshied";
    public static final String IRON_SWORD_SHIELD = "chocolatequest:ironswordandshield";
    public static final String DIAMOND_SWORD_SHIELD = "chocolatequest:diamondswordandshield";
    public static final String MONKING_SWORD_SHIELD = "chocolatequest:swordshiedmonking";
    public static final String MONKING_SWORD = "chocolatequest:swordmonking";
}
