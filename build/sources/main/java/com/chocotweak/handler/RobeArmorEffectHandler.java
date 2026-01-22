package com.chocotweak.handler;

import com.chocotweak.ChocoTweak;
import com.chocotweak.mixin.MixinHelperDamageSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 法袍特殊效果处理器
 * 
 * 当玩家穿着CQ法袍套装时：
 * - 只允许 cq_spell 类型的伤害
 * - 其他所有伤害归0
 */
@Mod.EventBusSubscriber(modid = ChocoTweak.MODID)
public class RobeArmorEffectHandler {

    /** CQ法袍类名关键字 */
    private static final String[] ROBE_CLASS_NAMES = {
        "ItemArmorRobe",
        "ItemArmorRobeLiche",
        "ItemArmorMage"
    };

    /**
     * 检测玩家受到伤害时
     * 如果穿着法袍套装，非cq_spell伤害归0
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        DamageSource source = event.getSource();

        // 检查是否穿着法袍套装
        if (!isWearingRobeSet(player)) {
            return;
        }

        // 检查是否是CQ法术伤害
        if (MixinHelperDamageSource.CQ_SPELL_DAMAGE_TYPE.equals(source.getDamageType())) {
            // 允许CQ法术伤害
            return;
        }

        // 一些伤害类型应该被放行 (跌落、饥饿、虚空等)
        if (shouldBypassRobeProtection(source)) {
            return;
        }

        // 非CQ法术伤害归0
        event.setAmount(0);
        event.setCanceled(true);
    }

    /**
     * 检查是否穿着法袍套装 (至少3件)
     */
    private static boolean isWearingRobeSet(EntityPlayer player) {
        int robeCount = 0;

        // 检查所有护甲槽
        for (EntityEquipmentSlot slot : new EntityEquipmentSlot[]{
            EntityEquipmentSlot.HEAD,
            EntityEquipmentSlot.CHEST,
            EntityEquipmentSlot.LEGS,
            EntityEquipmentSlot.FEET
        }) {
            ItemStack armor = player.getItemStackFromSlot(slot);
            if (!armor.isEmpty() && isRobeArmor(armor)) {
                robeCount++;
            }
        }

        // 需要至少3件法袍
        return robeCount >= 3;
    }

    /**
     * 检查是否是法袍护甲
     */
    private static boolean isRobeArmor(ItemStack stack) {
        String className = stack.getItem().getClass().getName();
        for (String robeName : ROBE_CLASS_NAMES) {
            if (className.contains(robeName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 某些伤害类型不受法袍保护影响
     */
    private static boolean shouldBypassRobeProtection(DamageSource source) {
        String type = source.getDamageType();
        return source.isDamageAbsolute() ||       // 绝对伤害
               source.isUnblockable() ||          // 无法格挡
               "outOfWorld".equals(type) ||       // 虚空
               "starve".equals(type) ||           // 饥饿
               "drown".equals(type) ||            // 溺水
               "inWall".equals(type) ||           // 卡墙
               source.isFireDamage();             // 火焰伤害
    }
}
