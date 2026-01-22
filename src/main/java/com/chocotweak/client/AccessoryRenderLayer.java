package com.chocotweak.client;

import com.chocolate.chocolateQuest.ChocolateQuest;
import com.chocolate.chocolateQuest.client.ClientProxy;
import com.chocotweak.compat.BaublesCompat;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 饰品渲染层
 * 只渲染 Baubles 槽位中的特殊装备（云靴、龙头盔、侦察器、女巫帽、背包）
 */
@SideOnly(Side.CLIENT)
public class AccessoryRenderLayer implements LayerRenderer<EntityPlayer> {

    private final RenderPlayer renderer;

    // 贴图资源
    private static final ResourceLocation CLOUD_TEXTURE = new ResourceLocation(
            "chocolatequest:textures/armor/cloud_1.png");
    private static final ResourceLocation DRAGON_TEXTURE = new ResourceLocation(
            "chocolatequest:textures/entity/dragonbd.png");
    private static final ResourceLocation SCOUTER_TEXTURE = new ResourceLocation(
            "chocolatequest:textures/armor/scouter_1.png");
    private static final ResourceLocation WITCH_TEXTURE = new ResourceLocation("textures/entity/witch.png");
    private static final ResourceLocation BACKPACK_TEXTURE = new ResourceLocation(
            "chocolatequest:textures/armor/bag.png");

    public AccessoryRenderLayer(RenderPlayer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityPlayer player, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        // 只检查 Baubles 槽位中的饰品并渲染

        // === 龙头盔 ===
        if (hasAccessoryInBaubles(player, ChocolateQuest.dragonHelmet)) {
            renderDragonHelmet(player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch,
                    scale);
        }

        // === 云靴 ===
        if (hasAccessoryInBaubles(player, ChocolateQuest.cloudBoots)) {
            renderCloudBoots(player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch,
                    scale);
        }

        // === 侦察器 ===
        if (hasAccessoryInBaubles(player, ChocolateQuest.scouter)) {
            renderScouter(player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
        }

        // === 女巫帽 ===
        if (hasAccessoryInBaubles(player, ChocolateQuest.witchHat)) {
            renderWitchHat(player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
        }

        // === 背包 ===
        if (hasAccessoryInBaubles(player, ChocolateQuest.backpack)) {
            renderBackpack(player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    /**
     * 检查物品是否在 Baubles 槽位中（只有 Baubles 槽位才渲染）
     */
    private boolean hasAccessoryInBaubles(EntityPlayer player, Item item) {
        // 只检查 Baubles 槽位
        return BaublesCompat.hasBaubleItem(player, item);
    }

    private void renderDragonHelmet(EntityPlayer player, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        this.renderer.bindTexture(DRAGON_TEXTURE);

        ModelBiped model = ClientProxy.dragonHead;
        if (model != null) {
            model.setModelAttributes(this.renderer.getMainModel());
            model.setLivingAnimations(player, limbSwing, limbSwingAmount, partialTicks);

            GlStateManager.pushMatrix();
            model.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.popMatrix();
        }
    }

    private void renderCloudBoots(EntityPlayer player, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        this.renderer.bindTexture(CLOUD_TEXTURE);

        // 使用默认护甲模型渲染靴子
        ModelBiped model = this.renderer.getMainModel();

        GlStateManager.pushMatrix();
        // 只渲染腿部
        model.bipedLeftLeg.render(scale);
        model.bipedRightLeg.render(scale);
        GlStateManager.popMatrix();
    }

    private void renderScouter(EntityPlayer player, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        // 获取侦察器物品
        Item scouterItem = ChocolateQuest.scouter;
        if (!(scouterItem instanceof net.minecraft.item.ItemArmor)) {
            return;
        }

        // 使用 ForgeHooksClient 获取盔甲模型
        ItemStack scouterStack = new ItemStack(scouterItem);
        ModelBiped model = net.minecraftforge.client.ForgeHooksClient.getArmorModel(
                player, scouterStack, EntityEquipmentSlot.HEAD, null);

        // 如果没有自定义模型，使用默认盔甲模型
        if (model == null) {
            model = new net.minecraft.client.model.ModelBiped(1.0F);
        }

        // 绑定盔甲贴图
        String texture = ((net.minecraft.item.ItemArmor) scouterItem).getArmorTexture(
                scouterStack, player, EntityEquipmentSlot.HEAD, null);
        if (texture != null) {
            this.renderer.bindTexture(new ResourceLocation(texture));
        } else {
            this.renderer.bindTexture(SCOUTER_TEXTURE);
        }

        model.setModelAttributes(this.renderer.getMainModel());
        model.setLivingAnimations(player, limbSwing, limbSwingAmount, partialTicks);

        GlStateManager.pushMatrix();
        // 渲染头盔
        model.bipedHead.render(scale);
        model.bipedHeadwear.render(scale);
        GlStateManager.popMatrix();
    }

    private void renderWitchHat(EntityPlayer player, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        this.renderer.bindTexture(WITCH_TEXTURE);

        ModelBiped model = ClientProxy.witchHat;
        if (model != null) {
            model.setModelAttributes(this.renderer.getMainModel());
            model.setLivingAnimations(player, limbSwing, limbSwingAmount, partialTicks);

            GlStateManager.pushMatrix();
            model.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }

    /**
     * 渲染背包
     */
    private void renderBackpack(EntityPlayer player, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        this.renderer.bindTexture(BACKPACK_TEXTURE);

        ModelBiped model = ClientProxy.armorBag;
        if (model != null) {
            model.setModelAttributes(this.renderer.getMainModel());
            model.setLivingAnimations(player, limbSwing, limbSwingAmount, partialTicks);

            GlStateManager.pushMatrix();
            model.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.popMatrix();
        }
    }

}
