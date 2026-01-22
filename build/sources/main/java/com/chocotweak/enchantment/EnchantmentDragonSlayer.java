package com.chocotweak.enchantment;

import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;

/**
 * 屠龙者 (Dragon Slayer)
 * NPC 专属附魔 - 对龙类生物造成额外伤害
 * 等级 I-V: 2.5/5/7.5/10/12.5 额外伤害
 * 
 * 支持的龙类:
 * - 原版末影龙
 * - Ice and Fire 龙 (火龙、冰龙、雷龙)
 * - 其他模组龙类
 */
public class EnchantmentDragonSlayer extends NpcExclusiveEnchantment {

    // 龙类判定的类名关键字
    private static final String[] DRAGON_KEYWORDS = {
            "dragon", "Dragon", "DRAGON",
            "EntityDragon", "EntityFireDragon", "EntityIceDragon", "EntityLightningDragon",
            "Wyvern", "wyvern"
    };

    public EnchantmentDragonSlayer() {
        super(Rarity.RARE, EnumEnchantmentType.WEAPON,
                new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
        this.setName("chocotweak.dragon_slayer");
        this.setRegistryName("chocotweak", "dragon_slayer");
    }

    @Override
    public int getMinEnchantability(int level) {
        return 12 + (level - 1) * 8;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return getMinEnchantability(level) + 25;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    /**
     * 计算对龙类的额外伤害
     * 
     * @param level 附魔等级
     * @return 额外伤害值
     */
    public static float getBonusDamage(int level) {
        return level * 2.5f;
    }

    /**
     * 判断实体是否为龙类
     * 
     * @param entity 目标实体
     * @return 是否为龙类
     */
    public static boolean isDragon(Entity entity) {
        if (entity == null)
            return false;

        String className = entity.getClass().getSimpleName();
        String fullClassName = entity.getClass().getName();

        // 检查类名是否包含龙类关键字
        for (String keyword : DRAGON_KEYWORDS) {
            if (className.contains(keyword) || fullClassName.contains(keyword)) {
                return true;
            }
        }

        // 检查是否为原版末影龙
        if (entity instanceof net.minecraft.entity.boss.EntityDragon) {
            return true;
        }

        return false;
    }
}
