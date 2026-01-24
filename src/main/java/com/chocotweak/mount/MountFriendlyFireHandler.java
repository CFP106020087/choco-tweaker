package com.chocotweak.mount;

import com.chocolate.chocolateQuest.entity.EntityHumanBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 防止坐骑攻击骑在它身上的 NPC
 * 
 * 监控 LivingSetAttackTargetEvent 事件，
 * 当坐骑尝试攻击它的骑手时，清除攻击目标
 */
public class MountFriendlyFireHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSetAttackTarget(LivingSetAttackTargetEvent event) {
        EntityLivingBase attacker = event.getEntityLiving();
        EntityLivingBase target = event.getTarget();
        
        if (target == null) return;
        
        // 检查攻击者是否有骑手
        if (!attacker.getPassengers().isEmpty()) {
            for (Entity passenger : attacker.getPassengers()) {
                // 如果目标是骑在攻击者身上的人
                if (passenger == target) {
                    // 清除攻击目标
                    if (attacker instanceof EntityLiving) {
                        ((EntityLiving) attacker).setAttackTarget(null);
                    }
                    return;
                }
                
                // 如果目标是同一个队伍的 CQ NPC
                if (passenger instanceof EntityHumanBase && target instanceof EntityHumanBase) {
                    EntityHumanBase riderNpc = (EntityHumanBase) passenger;
                    EntityHumanBase targetNpc = (EntityHumanBase) target;
                    if (riderNpc.isOnSameTeam(targetNpc)) {
                        if (attacker instanceof EntityLiving) {
                            ((EntityLiving) attacker).setAttackTarget(null);
                        }
                        return;
                    }
                }
            }
        }
        
        // 检查目标是否是攻击者的坐骑
        Entity attackerMount = attacker.getRidingEntity();
        if (attackerMount != null && attackerMount == target) {
            if (attacker instanceof EntityLiving) {
                ((EntityLiving) attacker).setAttackTarget(null);
            }
        }
    }
}
