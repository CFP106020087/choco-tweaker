package com.chocotweak.geckolib.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

/**
 * 装备渲染器 - 使用 GeoModelRenderer 保存的 Locator 骨骼矩阵渲染物品
 * 
 * 在模型渲染后调用，会从 GeoModelRenderer 获取预先保存的变换矩阵
 */
public class EquipmentLocatorRenderer {

    // Buffer for loading saved matrix
    private static final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    /**
     * 渲染实体手持物品到模型的 Locator 骨骼位置
     * 使用 GeoModelRenderer 在渲染时保存的矩阵
     * 
     * @param entity 实体 (用于获取装备)
     */
    public static void renderEquipmentOnLocators(EntityLivingBase entity) {
        if (entity == null) {
            return;
        }

        // 获取手持物品
        ItemStack mainHand = entity.getHeldItemMainhand();
        ItemStack offHand = entity.getHeldItemOffhand();

        // 渲染主手 - 优先使用 RightHandLocator, 回退到 RightHand
        if (mainHand != null && !mainHand.isEmpty()) {
            float[] matrix = GeoModelRenderer.getLocatorMatrix("RightHandLocator");
            if (matrix == null) {
                matrix = GeoModelRenderer.getLocatorMatrix("RightHand");
            }
            if (matrix != null) {
                renderItemWithMatrix(matrix, mainHand, entity, false);
            }
        }

        // 渲染副手 - 优先使用 LeftHandLocator, 回退到 LeftHand
        if (offHand != null && !offHand.isEmpty()) {
            float[] matrix = GeoModelRenderer.getLocatorMatrix("LeftHandLocator");
            if (matrix == null) {
                matrix = GeoModelRenderer.getLocatorMatrix("LeftHand");
            }
            if (matrix != null) {
                renderItemWithMatrix(matrix, offHand, entity, true);
            }
        }
    }

    /**
     * 使用保存的矩阵渲染物品
     */
    private static void renderItemWithMatrix(float[] matrix, ItemStack itemStack,
            EntityLivingBase entity, boolean isLeftHand) {
        if (itemStack == null || itemStack.isEmpty()) {
            return;
        }

        GlStateManager.pushMatrix();

        // 加载保存的骨骼变换矩阵
        loadMatrix(matrix);

        // 物品渲染调整
        GlStateManager.scale(0.5f, 0.5f, 0.5f); // 缩小物品到合适大小

        if (isLeftHand) {
            GlStateManager.scale(-1, 1, 1); // 镜像左手
        }

        // 旋转调整让物品朝向正确
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.rotate(180, 1, 0, 0);

        // 渲染物品
        renderItem(itemStack, entity, isLeftHand);

        GlStateManager.popMatrix();
    }

    /**
     * 加载 4x4 矩阵到 GL modelview
     */
    private static void loadMatrix(float[] matrix) {
        matrixBuffer.clear();
        matrixBuffer.put(matrix);
        matrixBuffer.flip();
        GL11.glLoadMatrix(matrixBuffer);
    }

    /**
     * 渲染物品
     */
    private static void renderItem(ItemStack itemStack, EntityLivingBase entity, boolean isLeftHand) {
        if (itemStack == null || itemStack.isEmpty()) {
            return;
        }

        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

        GlStateManager.pushMatrix();

        // 启用灯光以便物品正确显示
        GlStateManager.enableLighting();
        GlStateManager.enableRescaleNormal();

        // 使用第三人称变换
        ItemCameraTransforms.TransformType transformType = isLeftHand
                ? ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND
                : ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;

        // 渲染物品模型
        renderItem.renderItem(itemStack, entity, transformType, isLeftHand);

        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();

        GlStateManager.popMatrix();
    }
}
