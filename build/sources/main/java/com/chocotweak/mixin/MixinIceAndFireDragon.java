package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.entity.EntityHumanBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin 让 Ice and Fire 龙识别 CQ 佣兵为合法骑手
 * 
 * 问题：龙的 getControllingPassenger() 只识别：
 * 1. 龙的主人（EntityPlayer）
 * 2. DragonUtils.isDragonRider() 返回 true 的 EntityLiving
 * 
 * CQ 佣兵不满足这两个条件，会被当作猎物处理
 * 
 * 解决方案：注入 getControllingPassenger() 让它也返回 CQ 佣兵
 */
@Pseudo
@Mixin(targets = "com.github.alexthe666.iceandfire.entity.EntityDragonBase", remap = false)
public abstract class MixinIceAndFireDragon {

    /**
     * 修改 getControllingPassenger 让 CQ 佣兵被识别为控制者
     */
    @Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true)
    private void onGetControllingPassenger(CallbackInfoReturnable<Entity> cir) {
        EntityLiving self = (EntityLiving) (Object) this;

        // 检查所有乘客
        for (Entity passenger : self.getPassengers()) {
            // 如果乘客是 CQ 佣兵
            if (passenger instanceof EntityHumanBase) {
                EntityHumanBase mercenary = (EntityHumanBase) passenger;

                // 检查龙是否已驯服且佣兵的主人是龙的主人
                try {
                    // 获取龙的 isTamed() 和 getOwnerId()
                    java.lang.reflect.Method isTamed = self.getClass().getMethod("isTamed");
                    java.lang.reflect.Method getOwnerId = self.getClass().getMethod("getOwnerId");

                    boolean dragonTamed = (Boolean) isTamed.invoke(self);
                    Object dragonOwnerId = getOwnerId.invoke(self);

                    if (dragonTamed && dragonOwnerId != null) {
                        // 获取佣兵的主人
                        if (mercenary.getOwnerId() != null && mercenary.getOwnerId().equals(dragonOwnerId)) {
                            // 佣兵和龙有同一个主人，允许控制
                            cir.setReturnValue(passenger);
                            return;
                        }
                    }

                    // 即使没有同主人，也允许有主人的佣兵控制已驯服的龙
                    if (dragonTamed && mercenary.getOwnerId() != null) {
                        cir.setReturnValue(passenger);
                        return;
                    }
                } catch (Exception e) {
                    // 反射失败，回退：允许任何 CQ 佣兵控制
                    cir.setReturnValue(passenger);
                    return;
                }
            }
        }
        // 不干预，让原方法继续
    }
}
