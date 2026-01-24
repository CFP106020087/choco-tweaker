package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.magic.Awakements;
import com.chocolate.chocolateQuest.magic.SpellBase;
import com.chocotweak.core.AwakementsInitializer;
import com.chocotweak.magic.AwakementInstantCast;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 应用法术觉醒效果到ItemStaffBase
 * 
 * - 瞬发咏唱: 减少冷却时间 (在设置CD时应用)
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.items.ItemStaffBase", remap = false)
public abstract class MixinStaffAwakeningEffects {

    @Shadow
    public abstract SpellBase getSpell(ItemStack is);

    /**
     * 瞬发咏唱 - 减少冷却
     * 在 onPlayerStoppedUsing 设置CD时修改
     */
    @Inject(method = "onPlayerStoppedUsing", at = @At("RETURN"))
    private void applyInstantCastCooldown(ItemStack itemstack, World world, EntityLivingBase entityLiving, 
            int timeLeft, CallbackInfo ci) {
        
        if (!(entityLiving instanceof EntityPlayer)) {
            return;
        }

        // 检查瞬发咏唱觉醒
        if (AwakementsInitializer.instantCast == null) {
            return;
        }

        int level = Awakements.getEnchantLevel(itemstack, (Awakements) AwakementsInitializer.instantCast);
        if (level <= 0) {
            return;
        }

        // 获取法术槽位并修改CD
        ItemStack[] cargo = com.chocolate.chocolateQuest.gui.InventoryBag.getCargo(itemstack);
        if (cargo[0] != null && !cargo[0].isEmpty() && cargo[0].hasTagCompound()) {
            int currentCD = cargo[0].getTagCompound().getInteger("cd");
            if (currentCD > 0) {
                // 应用瞬发咏唱效果
                float multiplier = AwakementInstantCast.getCooldownMultiplier(level);
                int newCD = Math.max(1, (int)(currentCD * multiplier));
                cargo[0].getTagCompound().setInteger("cd", newCD);
                com.chocolate.chocolateQuest.gui.InventoryBag.saveCargo(itemstack, cargo);
                itemstack.setItemDamage(newCD);
            }
        }
    }
}
