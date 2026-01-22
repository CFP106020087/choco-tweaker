package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.ChocolateQuest;
import com.chocotweak.potion.PotionRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

/**
 * 猴王剑盾特殊效果 Mixin
 * - 命中时施加晕眩 5 秒
 * - 15% 概率打落敌人盔甲
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.items.swords.ItemSwordAndShieldBase", remap = false)
public class MixinMonkingSwordShield {

    private static final Random RANDOM = new Random();
    private static final double ARMOR_DROP_CHANCE = 0.15; // 15% 概率
    private static final int STUN_DURATION = 100; // 5 秒 = 100 ticks

    @Inject(method = "hitEntity", at = @At("HEAD"))
    private void onHitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker,
            CallbackInfoReturnable<Boolean> cir) {

        // 只对猴王剑盾生效
        if (stack.getItem() != ChocolateQuest.monkingSwordAndShield) {
            return;
        }

        // 施加晕眩效果 5 秒
        if (PotionRegistry.STUN != null) {
            target.addPotionEffect(new PotionEffect(PotionRegistry.STUN, STUN_DURATION, 0));
        }

        // 15% 概率打落盔甲
        if (RANDOM.nextDouble() < ARMOR_DROP_CHANCE) {
            dropRandomArmor(target);
        }
    }

    /**
     * 随机打落目标的一件盔甲
     */
    private void dropRandomArmor(EntityLivingBase target) {
        EntityEquipmentSlot[] armorSlots = {
                EntityEquipmentSlot.HEAD,
                EntityEquipmentSlot.CHEST,
                EntityEquipmentSlot.LEGS,
                EntityEquipmentSlot.FEET
        };

        // 收集目标穿戴的盔甲
        java.util.List<EntityEquipmentSlot> wornArmor = new java.util.ArrayList<>();
        for (EntityEquipmentSlot slot : armorSlots) {
            ItemStack armor = target.getItemStackFromSlot(slot);
            if (!armor.isEmpty()) {
                wornArmor.add(slot);
            }
        }

        // 如果有盔甲，随机打落一件
        if (!wornArmor.isEmpty()) {
            EntityEquipmentSlot slotToDrop = wornArmor.get(RANDOM.nextInt(wornArmor.size()));
            ItemStack armorToDrop = target.getItemStackFromSlot(slotToDrop).copy();

            // 从目标身上移除
            target.setItemStackToSlot(slotToDrop, ItemStack.EMPTY);

            // 在世界中生成掉落物
            if (!target.world.isRemote) {
                EntityItem entityItem = new EntityItem(target.world,
                        target.posX, target.posY + target.height, target.posZ,
                        armorToDrop);
                entityItem.setPickupDelay(10); // 短暂延迟防止立即捡起

                // 给掉落物一些随机速度
                entityItem.motionX = (RANDOM.nextDouble() - 0.5) * 0.3;
                entityItem.motionY = 0.2 + RANDOM.nextDouble() * 0.2;
                entityItem.motionZ = (RANDOM.nextDouble() - 0.5) * 0.3;

                target.world.spawnEntity(entityItem);
            }
        }
    }
}
