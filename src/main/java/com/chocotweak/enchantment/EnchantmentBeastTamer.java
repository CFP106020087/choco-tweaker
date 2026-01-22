package com.chocotweak.enchantment;

import com.chocotweak.config.TamingConfig;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 驯兽师 (Beast Tamer)
 * NPC 专属附魔 - 攻击未驯服生物时有几率直接驯服
 * 等级 I-II: 3%/6% 几率
 * 
 * 支持原版可驯服生物和配置文件中的模组生物
 */
public class EnchantmentBeastTamer extends NpcExclusiveEnchantment {

    public EnchantmentBeastTamer() {
        super(Rarity.VERY_RARE, EnumEnchantmentType.WEAPON,
                new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
        this.setName("chocotweak.beast_tamer");
        this.setRegistryName("chocotweak", "beast_tamer");
    }

    @Override
    public int getMinEnchantability(int level) {
        return 20 + (level - 1) * 15;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return getMinEnchantability(level) + 40;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    /**
     * 计算驯服几率
     * 
     * @param level 附魔等级
     * @return 几率 (0.0-1.0)
     */
    public static float getTameChance(int level) {
        return level * 0.03f; // 3% 每级
    }

    /**
     * 尝试驯服实体
     * 
     * @param entity 目标实体
     * @param player 玩家
     * @return 是否成功驯服
     */
    public static boolean tryTame(Entity entity, EntityPlayer player) {
        if (entity == null || player == null)
            return false;

        // 先尝试原版 EntityTameable
        if (entity instanceof EntityTameable) {
            EntityTameable tameable = (EntityTameable) entity;
            if (!tameable.isTamed()) {
                tameable.setTamed(true);
                tameable.setOwnerId(player.getUniqueID());
                return true;
            }
            return false;
        }

        // 尝试使用配置文件中的规则（反射）
        TamingConfig.TamingRule rule = TamingConfig.findRuleForEntity(entity);
        if (rule != null) {
            try {
                return applyTamingReflection(entity, player, rule);
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    /**
     * 使用反射进行驯服
     */
    private static boolean applyTamingReflection(Entity entity, EntityPlayer player,
            TamingConfig.TamingRule rule) throws Exception {

        Class<?> entityClass = entity.getClass();

        // 检查是否已驯服（尝试调用 isTamed 或类似方法）
        try {
            Method isTamedMethod = findMethod(entityClass, "isTamed");
            if (isTamedMethod != null) {
                isTamedMethod.setAccessible(true);
                Boolean isTamed = (Boolean) isTamedMethod.invoke(entity);
                if (isTamed != null && isTamed) {
                    return false; // 已经驯服
                }
            }
        } catch (Exception ignored) {
        }

        // 调用驯服方法
        if (rule.tameMethod != null && !rule.tameMethod.isEmpty()) {
            Method tameMethod = findMethod(entityClass, rule.tameMethod, boolean.class);
            if (tameMethod != null) {
                tameMethod.setAccessible(true);
                tameMethod.invoke(entity, true);
            }
        }

        // 设置主人
        if (rule.ownerMethod != null && !rule.ownerMethod.isEmpty()) {
            if ("UUID".equals(rule.ownerType)) {
                Method ownerMethod = findMethod(entityClass, rule.ownerMethod, UUID.class);
                if (ownerMethod != null) {
                    ownerMethod.setAccessible(true);
                    ownerMethod.invoke(entity, player.getUniqueID());
                }
            } else if ("EntityPlayer".equals(rule.ownerType)) {
                Method ownerMethod = findMethod(entityClass, rule.ownerMethod, EntityPlayer.class);
                if (ownerMethod != null) {
                    ownerMethod.setAccessible(true);
                    ownerMethod.invoke(entity, player);
                }
            }
        }

        return true;
    }

    private static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredMethod(name, paramTypes);
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
