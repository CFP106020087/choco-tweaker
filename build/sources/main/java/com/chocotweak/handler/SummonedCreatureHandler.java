package com.chocotweak.handler;

import com.chocotweak.ChocoTweak;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.UUID;

/**
 * 召唤物属性增强处理器
 * 
 * 增强CQ召唤的元素生物：
 * - HP +200%
 * - 伤害 +100%
 * - 移动速度 +100%
 * - 攻击速度 +50%
 */
@Mod.EventBusSubscriber(modid = ChocoTweak.MODID)
public class SummonedCreatureHandler {

    // 使用固定UUID确保不会重复添加
    private static final UUID HP_MODIFIER_UUID = UUID.fromString("e8b6c3a1-4d5f-6789-abcd-ef0123456789");
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("f9a7d4b2-5e6a-789b-cdef-012345678901");
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("a0b8e5c3-6f7b-890c-def0-123456789012");
    private static final UUID ATTACK_SPEED_MODIFIER_UUID = UUID.fromString("b1c9f6d4-7a8c-901d-ef01-234567890123");

    @SubscribeEvent
    public static void onEntitySpawn(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) {
            return;
        }

        if (!(event.getEntity() instanceof EntityLivingBase)) {
            return;
        }

        EntityLivingBase entity = (EntityLivingBase) event.getEntity();
        String className = entity.getClass().getName();

        // 检测CQ召唤物
        if (!isCQSummonedCreature(className)) {
            return;
        }

        // 延迟应用以确保实体完全初始化
        entity.getServer().addScheduledTask(() -> {
            applyEnhancements(entity);
        });
    }

    private static boolean isCQSummonedCreature(String className) {
        // CQ元素/召唤物类名模式
        return className.contains("chocolateQuest") && (
            className.contains("Elemental") ||
            className.contains("Summon") ||
            className.contains("Golem") ||
            className.contains("Hound")
        );
    }

    private static void applyEnhancements(EntityLivingBase entity) {
        // HP +200% (3倍)
        IAttributeInstance healthAttr = entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        if (healthAttr != null && healthAttr.getModifier(HP_MODIFIER_UUID) == null) {
            healthAttr.applyModifier(new AttributeModifier(
                HP_MODIFIER_UUID, "Summoned HP Boost", 2.0, 1 // 1 = 乘法
            ));
            entity.setHealth(entity.getMaxHealth()); // 刷新血量
        }

        // 伤害 +100% (2倍)
        IAttributeInstance damageAttr = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        if (damageAttr != null && damageAttr.getModifier(DAMAGE_MODIFIER_UUID) == null) {
            damageAttr.applyModifier(new AttributeModifier(
                DAMAGE_MODIFIER_UUID, "Summoned Damage Boost", 1.0, 1
            ));
        }

        // 移动速度 +100% (2倍)
        IAttributeInstance speedAttr = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (speedAttr != null && speedAttr.getModifier(SPEED_MODIFIER_UUID) == null) {
            speedAttr.applyModifier(new AttributeModifier(
                SPEED_MODIFIER_UUID, "Summoned Speed Boost", 1.0, 1
            ));
        }

        // 攻击速度 +50%
        IAttributeInstance attackSpeedAttr = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED);
        if (attackSpeedAttr != null && attackSpeedAttr.getModifier(ATTACK_SPEED_MODIFIER_UUID) == null) {
            attackSpeedAttr.applyModifier(new AttributeModifier(
                ATTACK_SPEED_MODIFIER_UUID, "Summoned Attack Speed Boost", 0.5, 1
            ));
        }

        ChocoTweak.LOGGER.debug("Enhanced summoned creature: {}", entity.getClass().getSimpleName());
    }
}
