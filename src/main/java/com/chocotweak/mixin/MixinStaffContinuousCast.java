package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.gui.InventoryBag;
import com.chocolate.chocolateQuest.magic.Awakements;
import com.chocolate.chocolateQuest.magic.SpellBase;
import com.chocotweak.core.AwakementsInitializer;
import com.chocotweak.magic.AwakementHighSpeedChant;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * 法杖持续施法 - 高速神言觉醒实现
 * 
 * 高速神言等级决定持续施法时长：
 * - Lv0: 不触发持续施法（无觉醒）
 * - Lv1: 3秒 (60 ticks)
 * - Lv2: 5秒 (100 ticks)
 * - Lv3: 8秒 (160 ticks)
 * - Lv4: 12秒 (240 ticks)
 * - Lv5: 无限
 * 
 * 对玩家和 NPC 都生效
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.items.ItemStaffBase", remap = false)
public abstract class MixinStaffContinuousCast {

    @Shadow
    public abstract SpellBase getSpell(ItemStack is);

    // 追踪每个实体的连续施法开始时间
    @Unique
    private static Map<Integer, Integer> chocotweak$continuousCastStartTick = new HashMap<>();

    /**
     * 检查高速神言觉醒是否允许继续清除冷却
     */
    private static boolean debugLogOnce = false;

    @Inject(method = "onUpdate", at = @At("RETURN"))
    private void clearCDAfterUpdate(ItemStack itemStack, World world, Entity entity, int par4, boolean par5,
            CallbackInfo ci) {
        // 调试：确认 mixin 被加载
        if (!debugLogOnce && !world.isRemote) {
            debugLogOnce = true;
            com.chocotweak.ChocoTweak.LOGGER.info("[StaffMixin] onUpdate mixin is active!");
        }
        // 只在服务端执行，ItemStack 变化会自动同步到客户端
        if (world.isRemote) {
            return;
        }

        if (!(entity instanceof EntityLivingBase)) {
            return;
        }

        EntityLivingBase living = (EntityLivingBase) entity;

        // 检查是否是玩家主手或 NPC 持有法杖
        boolean isMainHand = false;
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            isMainHand = player.getHeldItemMainhand() == itemStack;
        } else {
            // NPC - 假设正在使用的法杖
            isMainHand = true;
        }

        if (!isMainHand) {
            return;
        }

        // 检查高速神言觉醒等级
        int level = 0;
        if (AwakementsInitializer.highSpeedChant != null) {
            level = Awakements.getEnchantLevel(itemStack, (Awakements) AwakementsInitializer.highSpeedChant);
        }

        // 没有觉醒则不触发持续施法
        if (level <= 0) {
            chocotweak$continuousCastStartTick.remove(entity.getEntityId());
            return;
        }

        // 获取允许的持续时长
        int maxDuration = AwakementHighSpeedChant.getDurationForLevel(level);

        // 检查是否正在施法（按住右键）
        boolean isActive = living.isHandActive();

        if (isActive) {
            // 记录开始时间
            if (!chocotweak$continuousCastStartTick.containsKey(entity.getEntityId())) {
                chocotweak$continuousCastStartTick.put(entity.getEntityId(), entity.ticksExisted);
            }

            // 计算已施法时长
            int startTick = chocotweak$continuousCastStartTick.get(entity.getEntityId());
            int duration = entity.ticksExisted - startTick;

            // 检查是否在允许时长内
            if (duration <= maxDuration) {
                // 清除 CD，允许持续施法
                ItemStack[] cargo = InventoryBag.getCargo(itemStack);
                if (cargo[0] != null && !cargo[0].isEmpty() && cargo[0].hasTagCompound()) {
                    cargo[0].setTagCompound(null);
                    InventoryBag.saveCargo(itemStack, cargo);
                    itemStack.setItemDamage(-1);
                }
            }
        } else {
            // 停止施法时重置计时
            chocotweak$continuousCastStartTick.remove(entity.getEntityId());
        }
    }
}
