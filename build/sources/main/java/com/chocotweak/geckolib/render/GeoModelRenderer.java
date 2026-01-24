package com.chocotweak.geckolib.render;

import com.chocotweak.geckolib.render.built.GeoBone;
import com.chocotweak.geckolib.render.built.GeoCube;
import com.chocotweak.geckolib.render.built.GeoModel;
import com.chocotweak.geckolib.render.built.GeoQuad;
import com.chocotweak.geckolib.render.built.GeoVertex;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Main GeckoLib model renderer for 1.12.2.
 * Faithful backport from YSM GeckoLib 1.20.1.
 */
public class GeoModelRenderer {

    // Bones to hide during rendering
    private static final Set<String> hiddenBones = new HashSet<>();

    // Locator bone names to track for equipment rendering
    private static final Set<String> LOCATOR_BONES = new HashSet<>();

    // Stored modelview matrices for locator bones (per-frame, cleared before
    // render)
    private static final Map<String, float[]> locatorMatrices = new HashMap<>();

    // Buffer for reading GL matrix
    private static final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    // Toggle to hide fox companion
    private static boolean hideFox = true;

    static {
        // Locator bone names used for equipment attachment
        LOCATOR_BONES.add("RightHandLocator");
        LOCATOR_BONES.add("LeftHandLocator");
        LOCATOR_BONES.add("RightHand");
        LOCATOR_BONES.add("LeftHand");
        LOCATOR_BONES.add("Head");

        // Default: hide fox companion bones
        if (hideFox) {
            hiddenBones.add("FOX");
            hiddenBones.add("FoxEye");
            hiddenBones.add("FoxEyeHold");
        }
        // Hide GUI/background/debug bones (appear as small cubes)
        hiddenBones.add("Backgrounds");
        hiddenBones.add("Background1");
        hiddenBones.add("gui");
        hiddenBones.add("curtain");
        hiddenBones.add("white_curtain");
        hiddenBones.add("molang");
        hiddenBones.add("molang2");
        hiddenBones.add("molang3");
        // Note: Do NOT hide GuiRoot - it's the root bone in some model variants!
    }

    /**
     * Get stored modelview matrix for a locator bone.
     * Returns null if bone was not found during last render.
     */
    public static float[] getLocatorMatrix(String locatorName) {
        return locatorMatrices.get(locatorName);
    }

    /**
     * Clear all stored locator matrices (call before each model render).
     */
    public static void clearLocatorMatrices() {
        locatorMatrices.clear();
    }

    public static void setHideFox(boolean hide) {
        hideFox = hide;
        if (hide) {
            hiddenBones.add("FOX");
            hiddenBones.add("FoxEye");
            hiddenBones.add("FoxEyeHold");
        } else {
            hiddenBones.remove("FOX");
            hiddenBones.remove("FoxEye");
            hiddenBones.remove("FoxEyeHold");
        }
    }

    /**
     * Render a GeoModel with default white color.
     */
    public static void render(GeoModel model) {
        render(model, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * Render a GeoModel with animation support.
     */
    public static void render(GeoModel model, float red, float green, float blue, float alpha) {
        if (model == null)
            return;

        // Clear stored locator matrices from previous frame
        clearLocatorMatrices();

        GlStateManager.pushMatrix();

        // Render all root bones
        for (GeoBone bone : model.topLevelBones) {
            renderRecursively(bone, red, green, blue, alpha);
        }

        GlStateManager.popMatrix();
    }

    /**
     * Recursively render a bone and its children.
     */
    private static void renderRecursively(GeoBone bone, float red, float green, float blue, float alpha) {
        // Skip hidden bones and their children
        if (hiddenBones.contains(bone.name)) {
            return;
        }

        GlStateManager.pushMatrix();

        // Apply bone transforms (matching YSM prepMatrixForBone)
        GeoRenderUtils.prepMatrixForBone(bone);

        // Save matrix for locator bones (for equipment rendering)
        if (LOCATOR_BONES.contains(bone.name)) {
            saveCurrentMatrix(bone.name);
        }

        // Render cubes of this bone
        renderCubesOfBone(bone, red, green, blue, alpha);

        // Render child bones
        renderChildBones(bone, red, green, blue, alpha);

        GlStateManager.popMatrix();
    }

    /**
     * Save current GL modelview matrix for a locator bone.
     */
    private static void saveCurrentMatrix(String boneName) {
        matrixBuffer.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, matrixBuffer);
        float[] matrix = new float[16];
        matrixBuffer.get(matrix);
        locatorMatrices.put(boneName, matrix);
    }

    private static void renderCubesOfBone(GeoBone bone, float red, float green, float blue, float alpha) {
        if (bone.isHidden())
            return;

        for (GeoCube cube : bone.childCubes) {
            if (!bone.cubesAreHidden()) {
                GlStateManager.pushMatrix();
                renderCube(cube, red, green, blue, alpha);
                GlStateManager.popMatrix();
            }
        }
    }

    private static void renderChildBones(GeoBone bone, float red, float green, float blue, float alpha) {
        if (bone.childBonesAreHiddenToo())
            return;

        for (GeoBone childBone : bone.childBones) {
            renderRecursively(childBone, red, green, blue, alpha);
        }
    }

    /**
     * Render a single cube (6 faces).
     * Matching YSM renderCube: pivot → rotate → -pivot → quads
     */
    private static void renderCube(GeoCube cube, float red, float green, float blue, float alpha) {
        // Apply cube transforms
        GeoRenderUtils.translateToPivotPoint(cube);
        GeoRenderUtils.rotateMatrixAroundCube(cube);
        GeoRenderUtils.translateAwayFromPivotPoint(cube);

        // Render all 6 quads in a single batch
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Convert float RGBA to int (0-255)
        int r = (int) (red * 255);
        int g = (int) (green * 255);
        int b = (int) (blue * 255);
        int a = (int) (alpha * 255);

        // Begin once for all quads of this cube
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

        for (GeoQuad quad : cube.quads) {
            if (quad == null)
                continue;

            // Get normal with correction for zero-size faces (matching YSM)
            Vector3f normal = new Vector3f(quad.normal);
            if ((cube.size.y == 0 || cube.size.z == 0) && normal.x < 0) {
                normal.x *= -1;
            }
            if ((cube.size.x == 0 || cube.size.z == 0) && normal.y < 0) {
                normal.y *= -1;
            }
            if ((cube.size.x == 0 || cube.size.y == 0) && normal.z < 0) {
                normal.z *= -1;
            }

            for (GeoVertex vertex : quad.vertices) {
                Vector3f pos = vertex.position;
                buffer.pos(pos.x, pos.y, pos.z)
                        .tex(vertex.textureU, vertex.textureV)
                        .color(r, g, b, a)
                        .normal(normal.x, normal.y, normal.z)
                        .endVertex();
            }
        }

        // Draw all quads at once
        tessellator.draw();
    }
}
