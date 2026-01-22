package com.chocotweak.handler;

import com.chocolate.chocolateQuest.ChocolateQuest;
import com.chocolate.chocolateQuest.entity.projectile.EntityHookShoot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 钩剑特殊效果：钩住方块时免疫摔落伤害
 * 注意：只有钩住方块时生效，钩住实体时不生效
 */
@Mod.EventBusSubscriber(modid = "chocotweak")
public class HookSwordFallHandler {

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        // 检查主手和副手是否持有钩剑且钩住方块
        if (isHookSwordHookedToBlock(player, player.getHeldItemMainhand()) ||
                isHookSwordHookedToBlock(player, player.getHeldItemOffhand())) {
            // 钩住方块时免疫摔落伤害
            event.setCanceled(true);
        }
    }

    /**
     * 检查钩剑是否钩住了方块
     */
    private static boolean isHookSwordHookedToBlock(EntityPlayer player, ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != ChocolateQuest.hookSword) {
            return false;
        }

        // 检查钩爪是否激活
        if (!stack.hasTagCompound()) {
            return false;
        }

        int hookId = stack.getTagCompound().getInteger("hook");
        if (hookId == 0) {
            return false;
        }

        // 找到钩爪实体，检查是否钩住方块
        Entity hookEntity = player.world.getEntityByID(hookId);
        if (hookEntity instanceof EntityHookShoot) {
            EntityHookShoot hook = (EntityHookShoot) hookEntity;
            // 只有 blockHit 不为 null 且 hookedEntity 为 null 时，才表示钩住了方块
            return hook.blockHit != null && hook.hookedEntity == null;
        }

        return false;
    }
}
