package com.chocotweak.mixin;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.chocolate.chocolateQuest.ChocolateQuest;
import com.chocotweak.bedrock.entity.IEasterEggCapable;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin 到 LayerArmorBase 以渲染 Baubles 槽位中的盔甲
 * 也用于禁用 Easter Egg NPC 的盔甲渲染
 */
@Mixin(LayerArmorBase.class)
public abstract class MixinLayerArmorBase<T extends ModelBiped> {

    @Shadow
    @Final
    protected RenderLivingBase<?> renderer;

    @Shadow
    protected abstract void setModelSlotVisible(T model, EntityEquipmentSlot slotIn);

    @Shadow
    protected abstract T getModelFromSlot(EntityEquipmentSlot slotIn);

    /**
     * 跳过 Easter Egg NPC 的盔甲渲染
     */
    @Inject(method = "doRenderLayer", at = @At("HEAD"), cancellable = true)
    private void chocotweak$skipArmorForEasterEgg(EntityLivingBase entity, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {
        if (entity instanceof IEasterEggCapable) {
            IEasterEggCapable easterEgg = (IEasterEggCapable) entity;
            if (easterEgg.isEasterEggNpc()) {
                ci.cancel();
            }
        }
    }

    /**
     * 在渲染盔甲层后，额外渲染 Baubles 槽位中的盔甲物品
     */
    @Inject(method = "doRenderLayer", at = @At("RETURN"))
    private void renderBaublesArmor(EntityLivingBase entity, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {

        if (!(entity instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) entity;

        try {
            IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
            if (handler == null) {
                return;
            }

            // 遍历 Baubles 槽位
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (stack.isEmpty()) {
                    continue;
                }

                Item item = stack.getItem();

                // 检查是否是我们的特殊盔甲物品
                if (item == ChocolateQuest.scouter || item == ChocolateQuest.dragonHelmet
                        || item == ChocolateQuest.witchHat || item == ChocolateQuest.cloudBoots
                        || item == ChocolateQuest.backpack) {

                    if (item instanceof ItemArmor) {
                        ItemArmor armor = (ItemArmor) item;
                        EntityEquipmentSlot slot = armor.armorType;

                        // 使用物品的模型渲染
                        T model = this.getModelFromSlot(slot);
                        if (model != null) {
                            model.setModelAttributes(this.renderer.getMainModel());
                            model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);

                            this.setModelSlotVisible(model, slot);

                            // 获取贴图
                            String texture = armor.getArmorTexture(stack, entity, slot, null);
                            if (texture != null) {
                                this.renderer.bindTexture(new ResourceLocation(texture));
                            }

                            // 渲染模型
                            GlStateManager.pushMatrix();
                            model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                            GlStateManager.popMatrix();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 忽略错误
        }
    }
}
