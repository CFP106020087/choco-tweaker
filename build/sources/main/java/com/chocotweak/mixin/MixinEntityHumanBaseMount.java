package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.entity.EntityHumanBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin 扩展 CQ NPC 骑乘系统
 * 
 * 功能：
 * 1. 扩展 isSuitableMount() 支持更多生物类型
 * 2. 每 tick 同步佣兵的仇恨目标到坐骑
 * 3. 无目标时坐骑停止移动
 * 
 * 这种方法利用坐骑自身的 AI 系统，兼容性极佳
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.entity.EntityHumanBase", remap = false)
public abstract class MixinEntityHumanBaseMount {

    /**
     * 扩展坐骑类型判断 - 支持大部分可骑乘生物
     */
    @Inject(method = "isSuitableMount", at = @At("HEAD"), cancellable = true)
    private void onIsSuitableMount(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (chocotweak$isUniversallyMountable(entity)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * 每 tick 同步仇恨目标到坐骑
     */
    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void onUpdateSyncMountTarget(CallbackInfo ci) {
        EntityHumanBase self = (EntityHumanBase) (Object) this;
        Entity riding = self.getRidingEntity();

        if (riding == null || !(riding instanceof EntityLiving)) {
            return;
        }

        EntityLiving mount = (EntityLiving) riding;
        EntityLivingBase myTarget = self.getAttackTarget();

        if (myTarget != null && myTarget.isEntityAlive()) {
            // 同步仇恨目标到坐骑
            if (mount.getAttackTarget() != myTarget) {
                mount.setAttackTarget(myTarget);
            }
        } else {
            // 无目标时停止坐骑移动
            if (mount.getAttackTarget() != null) {
                mount.setAttackTarget(null);
            }
            mount.getNavigator().clearPath();
        }
    }

    /**
     * 下马时清除坐骑的仇恨目标
     */
    @Inject(method = "dismountRidingEntity", at = @At("HEAD"))
    private void onDismount(CallbackInfo ci) {
        EntityHumanBase self = (EntityHumanBase) (Object) this;
        Entity riding = self.getRidingEntity();

        if (riding instanceof EntityLiving) {
            EntityLiving mount = (EntityLiving) riding;
            mount.setAttackTarget(null);
            mount.getNavigator().clearPath();
        }
    }

    /**
     * 阻止佣兵攻击自己的坐骑
     */
    @Inject(method = "setAttackTarget", at = @At("HEAD"), cancellable = true)
    private void onSetAttackTarget(EntityLivingBase target, CallbackInfo ci) {
        EntityHumanBase self = (EntityHumanBase) (Object) this;

        // 不能攻击自己正在骑的坐骑
        if (target != null && target == self.getRidingEntity()) {
            ci.cancel();
            return;
        }

        // 不能攻击当前骑在自己身上的骑手的坐骑
        if (target != null) {
            for (Entity passenger : target.getPassengers()) {
                if (passenger == self) {
                    ci.cancel();
                    return;
                }
            }
        }
    }

    /**
     * 阻止佣兵把坐骑设为复仇目标
     */
    @Inject(method = "setRevengeTarget", at = @At("HEAD"), cancellable = true)
    private void onSetRevengeTarget(EntityLivingBase target, CallbackInfo ci) {
        EntityHumanBase self = (EntityHumanBase) (Object) this;

        // 不能把坐骑设为复仇目标
        if (target != null && target == self.getRidingEntity()) {
            ci.cancel();
        }
    }

    /**
     * 判断实体是否可以作为通用坐骑
     * 
     * 规则：
     * 1. 必须是 EntityLiving
     * 2. 没有其他骑手
     * 3. 马匹类 - 不需要驯服检查（CQ 在 setMountAI 中处理驯服）
     * 4. 其他生物 - 必须已驯服（有主人）
     * 5. 支持：马、Lycanites Mobs、Ice and Fire、其他模组坐骑
     */
    @Unique
    private static boolean chocotweak$isUniversallyMountable(Entity entity) {
        if (entity == null)
            return false;
        if (!(entity instanceof EntityLiving))
            return false;

        // 已经有骑手的不能骑
        if (!entity.getPassengers().isEmpty())
            return false;

        // 马匹类 - 不需要驯服检查，CQ 原生会在 setMountAI 中处理驯服
        if (entity instanceof AbstractHorse) {
            return true;
        }

        // 其他生物必须已驯服
        if (!chocotweak$isTamed(entity)) {
            return false;
        }

        // 动物/生物类（模组坐骑）
        if (entity instanceof EntityAnimal || entity instanceof EntityCreature) {
            return true;
        }

        // 检查类名包含常见坐骑关键词
        String className = entity.getClass().getName().toLowerCase();
        if (className.contains("mount") ||
                className.contains("dragon") ||
                className.contains("wyvern") ||
                className.contains("griffon") ||
                className.contains("rideable") ||
                className.contains("steed") ||
                className.contains("lycanites")) { // Lycanites Mobs
            return true;
        }

        return false;
    }

    /**
     * 检查实体是否已驯服
     * 支持多种驯服系统：Vanilla, Ice and Fire, Lycanites Mobs 等
     */
    @Unique
    private static boolean chocotweak$isTamed(Entity entity) {
        // 方法1: 检查 isTamed() (Vanilla EntityTameable)
        try {
            java.lang.reflect.Method isTamed = entity.getClass().getMethod("isTamed");
            Object result = isTamed.invoke(entity);
            if (result instanceof Boolean && (Boolean) result) {
                return true;
            }
        } catch (Exception ignored) {
        }

        // 方法2: 检查 getOwner() != null (通用)
        try {
            java.lang.reflect.Method getOwner = entity.getClass().getMethod("getOwner");
            Object owner = getOwner.invoke(entity);
            if (owner != null) {
                return true;
            }
        } catch (Exception ignored) {
        }

        // 方法3: 检查 getOwnerId() (Vanilla)
        try {
            java.lang.reflect.Method getOwnerId = entity.getClass().getMethod("getOwnerId");
            Object ownerId = getOwnerId.invoke(entity);
            if (ownerId != null) {
                return true;
            }
        } catch (Exception ignored) {
        }

        // 方法4: Lycanites Mobs - 检查 getPetEntry() 或 hasOwner()
        try {
            java.lang.reflect.Method hasOwner = entity.getClass().getMethod("hasOwner");
            Object result = hasOwner.invoke(entity);
            if (result instanceof Boolean && (Boolean) result) {
                return true;
            }
        } catch (Exception ignored) {
        }

        // 方法5: Ice and Fire - 检查 getOwner()
        try {
            java.lang.reflect.Method getOwnerPlayer = entity.getClass().getMethod("getOwnerPlayer");
            Object owner = getOwnerPlayer.invoke(entity);
            if (owner != null) {
                return true;
            }
        } catch (Exception ignored) {
        }

        // 方法6: 检查 NBT 的 Owner 或 Tame 字段
        try {
            net.minecraft.nbt.NBTTagCompound nbt = new net.minecraft.nbt.NBTTagCompound();
            entity.writeToNBT(nbt);
            if (nbt.hasKey("Owner") || nbt.hasKey("OwnerUUID") ||
                    (nbt.hasKey("Tame") && nbt.getBoolean("Tame"))) {
                return true;
            }
        } catch (Exception ignored) {
        }

        // 原生马匹已驯服检查
        if (entity instanceof AbstractHorse) {
            return ((AbstractHorse) entity).isTame();
        }

        return false;
    }
}
