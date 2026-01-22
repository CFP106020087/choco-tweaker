package com.chocotweak.handler;

import com.chocotweak.ChocoTweak;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 法术特效处理器
 * 
 * 流星雨/风暴命中时解除敌人无敌帧
 */
@Mod.EventBusSubscriber(modid = ChocoTweak.MODID)
public class SpellEffectHandler {

    /**
     * 在伤害事件中检测CQ法术伤害，清除目标无敌帧
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        EntityLivingBase target = event.getEntityLiving();

        // 检测是否是魔法伤害
        if (!source.isMagicDamage()) {
            return;
        }

        // 检测是否来自CQ法术特定类型 (通过伤害源类型名或造成者检测)
        String damageType = source.getDamageType();
        boolean isMeteorOrStorm = false;

        // 检测伤害源
        if (damageType != null && (
            damageType.contains("magic") ||
            damageType.contains("spell") ||
            damageType.contains("indirectMagic")
        )) {
            // CQ的魔法投射物伤害
            isMeteorOrStorm = true;
        }

        // 如果造成者是CQ的投射物实体
        if (source.getImmediateSource() != null) {
            String sourceClass = source.getImmediateSource().getClass().getName();
            if (sourceClass.contains("chocolateQuest") && (
                sourceClass.contains("Ball") ||
                sourceClass.contains("Projectile") ||
                sourceClass.contains("Meteor") ||
                sourceClass.contains("Storm")
            )) {
                isMeteorOrStorm = true;
            }
        }

        if (isMeteorOrStorm) {
            // 清除无敌帧
            target.hurtResistantTime = 0;
            
            // 可选：添加额外的脆弱效果
            // target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 40, 0));
        }
    }
}
