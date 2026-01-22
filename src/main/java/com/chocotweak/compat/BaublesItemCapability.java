package com.chocotweak.compat;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import com.chocolate.chocolateQuest.ChocolateQuest;
import com.chocolate.chocolateQuest.items.ItemArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Baubles 物品兼容性 - 使物品可以放入饰品槽
 * 使用直接 Baubles API
 */
@Mod.EventBusSubscriber(modid = "chocotweak")
public class BaublesItemCapability {

    /**
     * 检查物品是否应该成为 Baubles 物品
     */
    public static boolean shouldBeBauble(Item item) {
        return item == ChocolateQuest.cloudBoots
                || item == ChocolateQuest.dragonHelmet
                || item == ChocolateQuest.scouter
                || item == ChocolateQuest.witchHat
                || item == ChocolateQuest.backpack;
    }

    /**
     * 获取物品对应的 Bauble 类型
     */
    public static BaubleType getBaubleType(Item item) {
        // 云靴 - CHARM (通用饰品槽)
        if (item == ChocolateQuest.cloudBoots)
            return BaubleType.CHARM;
        // 龙头盔 - HEAD (头部槽)
        if (item == ChocolateQuest.dragonHelmet)
            return BaubleType.HEAD;
        // 侦察器 - AMULET (项链槽)
        if (item == ChocolateQuest.scouter)
            return BaubleType.AMULET;
        // 女巫帽 - HEAD (头部槽)
        if (item == ChocolateQuest.witchHat)
            return BaubleType.HEAD;
        // 背包 - BODY (身体槽)
        if (item == ChocolateQuest.backpack)
            return BaubleType.BODY;

        return BaubleType.CHARM; // 默认
    }

    /**
     * 为 ItemStack 附加 Baubles Capability
     */
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack.isEmpty())
            return;

        Item item = stack.getItem();

        // 检查是否是需要添加 Baubles 支持的物品
        if (shouldBeBauble(item)) {
            event.addCapability(
                    new ResourceLocation("chocotweak", "bauble"),
                    new BaubleCapabilityProvider(item));
        }
    }

    /**
     * IBauble 实现类
     */
    private static class BaubleImpl implements IBauble {
        private final Item item;

        public BaubleImpl(Item item) {
            this.item = item;
        }

        @Override
        public BaubleType getBaubleType(ItemStack itemStack) {
            return BaublesItemCapability.getBaubleType(item);
        }

        @Override
        public void onWornTick(ItemStack stack, EntityLivingBase entity) {
            // 调用原始盔甲物品的 onUpdateEquiped 方法
            Item wornItem = stack.getItem();
            if (wornItem instanceof ItemArmorBase) {
                ((ItemArmorBase) wornItem).onUpdateEquiped(stack, entity.world, entity);
            }

            // === 云靴特殊效果 ===
            if (wornItem == ChocolateQuest.cloudBoots) {
                // 取消摔落伤害
                if (entity.fallDistance >= 3.0F) {
                    entity.fallDistance = 0.0F;
                    if (entity.world.isRemote) {
                        for (int i = 0; i < 3; ++i) {
                            entity.world.spawnParticle(EnumParticleTypes.CLOUD,
                                    entity.posX, entity.posY - 2.0D, entity.posZ,
                                    (entity.world.rand.nextFloat() - 0.5F) / 2.0F, -0.5D,
                                    (entity.world.rand.nextFloat() - 0.5F) / 2.0F);
                        }
                    }
                }
                // 冲刺时云粒子
                if (entity.isSprinting() && entity.world.isRemote) {
                    entity.world.spawnParticle(EnumParticleTypes.CLOUD,
                            entity.posX, entity.posY - 1.5D, entity.posZ,
                            (entity.world.rand.nextFloat() - 0.5F) / 2.0F, 0.1D,
                            (entity.world.rand.nextFloat() - 0.5F) / 2.0F);
                }
                // 空中移动增强
                if (!entity.onGround && entity instanceof EntityPlayer) {
                    entity.jumpMovementFactor += 0.03F;
                }
                // 碰撞时增加 stepHeight
                if (entity.collidedHorizontally) {
                    entity.stepHeight = 1.0F;
                } else {
                    entity.stepHeight = 0.5F;
                }
            }

            // === 侦察器效果 ===
            if (wornItem == ChocolateQuest.scouter && entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                // 夜视效果 (220 ticks, 不显示粒子)
                player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 220, 0, true, false));
            }
        }

        @Override
        public boolean canEquip(ItemStack itemStack, EntityLivingBase player) {
            return true;
        }

        @Override
        public boolean canUnequip(ItemStack itemStack, EntityLivingBase player) {
            return true;
        }

        @Override
        public boolean willAutoSync(ItemStack itemStack, EntityLivingBase player) {
            return true;
        }
    }

    /**
     * Capability Provider
     */
    private static class BaubleCapabilityProvider implements ICapabilityProvider {
        private final IBauble baubleImpl;

        public BaubleCapabilityProvider(Item item) {
            this.baubleImpl = new BaubleImpl(item);
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
        }

        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE) {
                return (T) baubleImpl;
            }
            return null;
        }
    }
}
