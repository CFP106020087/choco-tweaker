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
 * 自定义晕眩药水效果
 * - 禁止实体移动
 * - 头顶渲染亮色粒子
 */
public class PotionStun extends Potion {

    public static final ResourceLocation TEXTURE = new ResourceLocation("chocotweak", "textures/gui/potion_stun.png");

    public PotionStun() {
        super(true, 0xFFFF00); // 负面效果，黄色
        this.setPotionName("effect.chocotweak.stun");
        this.setRegistryName(new ResourceLocation("chocotweak", "stun"));
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        // 每 tick 都生效
        return true;
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        // 禁止移动：将速度归零
        entity.motionX = 0;
        entity.motionZ = 0;

        // 如果在空中，允许下落但不能水平移动
        if (!entity.onGround) {
            // 保持正常重力
        } else {
            entity.motionY = 0;
        }

        // 阻止跳跃
        entity.isAirBorne = false;

        // 渲染头顶粒子（客户端）
        if (entity.world.isRemote) {
            spawnStunParticles(entity);
        }
    }

    private void spawnStunParticles(EntityLivingBase entity) {
        // 在头顶生成亮色粒子（END_ROD 类型，白色发光）
        double x = entity.posX + (entity.world.rand.nextDouble() - 0.5) * 0.5;
        double y = entity.posY + entity.height + 0.3 + entity.world.rand.nextDouble() * 0.2;
        double z = entity.posZ + (entity.world.rand.nextDouble() - 0.5) * 0.5;

        // 旋转效果
        double angle = (entity.ticksExisted % 20) * Math.PI / 10;
        double radius = 0.3;
        double offsetX = Math.cos(angle) * radius;
        double offsetZ = Math.sin(angle) * radius;

        entity.world.spawnParticle(EnumParticleTypes.END_ROD,
                x + offsetX, y, z + offsetZ,
                0, 0.02, 0);

        // 第二个粒子，相对位置
        entity.world.spawnParticle(EnumParticleTypes.END_ROD,
                x - offsetX, y, z - offsetZ,
                0, 0.02, 0);
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
