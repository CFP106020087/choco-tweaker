package com.chocotweak.geckolib.render;

import com.chocotweak.geckolib.render.built.GeoBone;
import com.chocotweak.geckolib.render.built.GeoCube;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.util.vector.Vector3f;

/**
 * Render utility methods for GeckoLib 1.12.2 backport.
 * Replaces PoseStack with GlStateManager calls.
 */
public final class GeoRenderUtils {
    
    private static final float TO_BLOCK_UNITS = 1f / 16f;
    
    /**
     * Translate by bone's animated position offset.
     * YSM: -posX/16, posY/16, posZ/16
     */
    public static void translateMatrixToBone(GeoBone bone) {
        GlStateManager.translate(
            -bone.getPositionX() * TO_BLOCK_UNITS,
            bone.getPositionY() * TO_BLOCK_UNITS,
            bone.getPositionZ() * TO_BLOCK_UNITS
        );
    }
    
    /**
     * Rotate around bone's rotation point.
     * Order: Z → Y → X (YSM standard)
     */
    public static void rotateMatrixAroundBone(GeoBone bone) {
        if (bone.getRotationZ() != 0.0F) {
            GlStateManager.rotate((float) Math.toDegrees(bone.getRotationZ()), 0, 0, 1);
        }
        if (bone.getRotationY() != 0.0F) {
            GlStateManager.rotate((float) Math.toDegrees(bone.getRotationY()), 0, 1, 0);
        }
        if (bone.getRotationX() != 0.0F) {
            GlStateManager.rotate((float) Math.toDegrees(bone.getRotationX()), 1, 0, 0);
        }
    }
    
    /**
     * Rotate around cube's pivot point.
     * Order: Z → Y → X (cube rotation is already in radians)
     */
    public static void rotateMatrixAroundCube(GeoCube cube) {
        Vector3f rotation = cube.rotation;
        if (rotation != null) {
            if (rotation.z != 0.0F) {
                GlStateManager.rotate((float) Math.toDegrees(rotation.z), 0, 0, 1);
            }
            if (rotation.y != 0.0F) {
                GlStateManager.rotate((float) Math.toDegrees(rotation.y), 0, 1, 0);
            }
            if (rotation.x != 0.0F) {
                GlStateManager.rotate((float) Math.toDegrees(rotation.x), 1, 0, 0);
            }
        }
    }
    
    /**
     * Scale by bone's scale factors.
     */
    public static void scaleMatrixForBone(GeoBone bone) {
        GlStateManager.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
    }
    
    /**
     * Translate TO cube pivot point.
     */
    public static void translateToPivotPoint(GeoCube cube) {
        Vector3f pivot = cube.pivot;
        if (pivot != null) {
            GlStateManager.translate(
                pivot.x * TO_BLOCK_UNITS,
                pivot.y * TO_BLOCK_UNITS,
                pivot.z * TO_BLOCK_UNITS
            );
        }
    }
    
    /**
     * Translate TO bone rotation point.
     */
    public static void translateToPivotPoint(GeoBone bone) {
        GlStateManager.translate(
            bone.rotationPointX * TO_BLOCK_UNITS,
            bone.rotationPointY * TO_BLOCK_UNITS,
            bone.rotationPointZ * TO_BLOCK_UNITS
        );
    }
    
    /**
     * Translate AWAY from cube pivot point.
     */
    public static void translateAwayFromPivotPoint(GeoCube cube) {
        Vector3f pivot = cube.pivot;
        if (pivot != null) {
            GlStateManager.translate(
                -pivot.x * TO_BLOCK_UNITS,
                -pivot.y * TO_BLOCK_UNITS,
                -pivot.z * TO_BLOCK_UNITS
            );
        }
    }
    
    /**
     * Translate AWAY from bone rotation point.
     */
    public static void translateAwayFromPivotPoint(GeoBone bone) {
        GlStateManager.translate(
            -bone.rotationPointX * TO_BLOCK_UNITS,
            -bone.rotationPointY * TO_BLOCK_UNITS,
            -bone.rotationPointZ * TO_BLOCK_UNITS
        );
    }
    
    /**
     * Full bone matrix preparation (matching YSM prepMatrixForBone).
     * Order: translate → pivot → rotate → scale → -pivot
     */
    public static void prepMatrixForBone(GeoBone bone) {
        translateMatrixToBone(bone);
        translateToPivotPoint(bone);
        rotateMatrixAroundBone(bone);
        scaleMatrixForBone(bone);
        translateAwayFromPivotPoint(bone);
    }
}
