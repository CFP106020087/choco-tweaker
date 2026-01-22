package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.ChocolateQuest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 猴王剑盾：边格挡边攻击
 * 
 * 原理：在客户端攻击实体时，如果玩家正在用猴王剑盾格挡，
 * 临时取消格挡状态，执行攻击，然后恢复格挡
 */
@Mixin(value = PlayerControllerMP.class, remap = true)
public abstract class MixinPlayerControllerAttack {

    @Shadow
    private Minecraft mc;

    // 标记是否需要在攻击后恢复格挡
    private boolean shouldRestoreBlock = false;

    /**
     * 在攻击实体前检查：如果是猴王剑盾格挡中，临时取消格挡
     * func_78764_a = attackEntity (SRG name)
     */
    @Inject(method = "func_78764_a", at = @At("HEAD"), remap = false)
    private void beforeAttackEntity(EntityPlayer player, Entity target, CallbackInfo ci) {
        ItemStack mainHand = player.getHeldItemMainhand();

        // 检查是否持有猴王剑盾且在格挡
        if (mainHand.getItem() == ChocolateQuest.monkingSwordAndShield) {
            if (player.isActiveItemStackBlocking()) {
                // 记录需要恢复格挡
                shouldRestoreBlock = true;
                // 临时取消格挡
                player.resetActiveHand();
            }
        }
    }

    /**
     * 在攻击实体后：如果需要恢复格挡，则重新激活
     * func_78764_a = attackEntity (SRG name)
     */
    @Inject(method = "func_78764_a", at = @At("RETURN"), remap = false)
    private void afterAttackEntity(EntityPlayer player, Entity target, CallbackInfo ci) {
        if (shouldRestoreBlock) {
            shouldRestoreBlock = false;

            ItemStack mainHand = player.getHeldItemMainhand();
            // 确认仍持有猴王剑盾
            if (mainHand.getItem() == ChocolateQuest.monkingSwordAndShield) {
                // 检查玩家是否仍按住右键
                if (mc.gameSettings.keyBindUseItem.isKeyDown()) {
                    // 恢复格挡
                    player.setActiveHand(EnumHand.MAIN_HAND);
                }
            }
        }
    }
}
