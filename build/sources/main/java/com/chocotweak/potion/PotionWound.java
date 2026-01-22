package com.chocotweak.potion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 自定义挫伤药水效果
 * - 减少无敌帧
 * - 持续流血粒子
 */
public class PotionWound extends Potion {

    public static final ResourceLocation TEXTURE = new ResourceLocation("chocotweak", "textures/gui/potion_wound.png");

    public PotionWound() {
        super(true, 0x8B0000); // 负面效果，深红色
        this.setPotionName("effect.chocotweak.wound");
        this.setRegistryName(new ResourceLocation("chocotweak", "wound"));
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        // 每 tick 都生效
        return true;
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        // 减少无敌帧 (每级减 2 帧)
        int reduction = (amplifier + 1) * 2;
        entity.hurtResistantTime = Math.max(0, entity.hurtResistantTime - reduction);

        // 流血粒子效果（客户端）
        if (entity.world.isRemote && entity.ticksExisted % 5 == 0) {
            spawnBleedParticles(entity);
        }
    }

    private void spawnBleedParticles(EntityLivingBase entity) {
        // 生成红色伤害粒子
        double x = entity.posX + (entity.world.rand.nextDouble() - 0.5) * entity.width;
        double y = entity.posY + entity.world.rand.nextDouble() * entity.height;
        double z = entity.posZ + (entity.world.rand.nextDouble() - 0.5) * entity.width;

        entity.world.spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR,
                x, y, z,
                0, -0.1, 0);
    }

    @Override
    public void applyAttributesModifiersToEntity(EntityLivingBase entity, AbstractAttributeMap attributeMap,
            int amplifier) {
        super.applyAttributesModifiersToEntity(entity, attributeMap, amplifier);
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entity, AbstractAttributeMap attributeMap,
            int amplifier) {
        super.removeAttributesModifiersFromEntity(entity, attributeMap, amplifier);
    }

    @Override
    public boolean hasStatusIcon() {
        return false; // 使用自定义图标
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
        mc.getTextureManager().bindTexture(TEXTURE);
        Gui.drawModalRectWithCustomSizedTexture(x + 3, y + 3, 0, 0, 18, 18, 18, 18);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        mc.getTextureManager().bindTexture(TEXTURE);
        Gui.drawModalRectWithCustomSizedTexture(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
    }
}
