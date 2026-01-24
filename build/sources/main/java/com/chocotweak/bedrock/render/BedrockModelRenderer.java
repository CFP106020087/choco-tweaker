package com.chocotweak.bedrock.render;

import com.chocotweak.bedrock.animation.BedrockAnimation;
import com.chocotweak.bedrock.model.BedrockBone;
import com.chocotweak.bedrock.model.BedrockCube;
import com.chocotweak.bedrock.model.BedrockModel;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.Map;

/**
 * Renders Bedrock models using OpenGL 1.12.2 compatible calls.
 * Handles bone hierarchy transformations and cube rendering.
 */
public class BedrockModelRenderer {
    
    // Conversion factor: Bedrock uses 1/16 block units, MC uses 1 block
    private static final float SCALE_FACTOR = 1f / 16f;
    
    /**
     * Render a Bedrock model with animation.
     * 
     * @param model The model to render
     * @param animation Current animation (may be null)
     * @param animTime Animation time in seconds
     * @param textureWidth Texture width for UV calculation
     * @param textureHeight Texture height for UV calculation
     */
    public static void render(BedrockModel model, BedrockAnimation animation, 
            float animTime, int textureWidth, int textureHeight) {
        if (model == null) return;
        
        // Get animation transforms
        Map<String, float[]> animTransforms = null;
        if (animation != null) {
            animTransforms = animation.getAllTransformsAtTime(animTime);
        }
        
        GlStateManager.pushMatrix();
        
        // Apply global scale (Bedrock uses different coordinate system)
        GlStateManager.scale(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);
        
        // Flip Y axis (Bedrock Y is up, pivot at feet)
        GlStateManager.translate(0, 24, 0);  // Move up by typical humanoid height
        
        // Render all root bones (they will recursively render children)
        for (BedrockBone rootBone : model.rootBones) {
            renderBone(rootBone, animTransforms, textureWidth, textureHeight);
        }
        
        GlStateManager.popMatrix();
    }
    
    private static void renderBone(BedrockBone bone, Map<String, float[]> animTransforms,
            int textureWidth, int textureHeight) {
        GlStateManager.pushMatrix();
        
        // Translate to pivot point
        GlStateManager.translate(bone.pivot[0], bone.pivot[1], bone.pivot[2]);
        
        // Apply animation transforms
        float[] transforms = animTransforms != null ? animTransforms.get(bone.name) : null;
        if (transforms != null) {
            // Apply animated rotation (ZYX order for Bedrock)
            GlStateManager.rotate(transforms[2], 0, 0, 1);  // Z
            GlStateManager.rotate(transforms[1], 0, 1, 0);  // Y
            GlStateManager.rotate(transforms[0], 1, 0, 0);  // X
            
            // Apply animated position offset
            GlStateManager.translate(transforms[3], transforms[4], transforms[5]);
            
            // Apply animated scale
            GlStateManager.scale(transforms[6], transforms[7], transforms[8]);
        } else {
            // Apply default rotation
            GlStateManager.rotate(bone.rotation[2], 0, 0, 1);
            GlStateManager.rotate(bone.rotation[1], 0, 1, 0);
            GlStateManager.rotate(bone.rotation[0], 1, 0, 0);
        }
        
        // Translate back from pivot for cube rendering
        GlStateManager.translate(-bone.pivot[0], -bone.pivot[1], -bone.pivot[2]);
        
        // Render cubes
        for (BedrockCube cube : bone.cubes) {
            renderCube(cube, bone.pivot, textureWidth, textureHeight);
        }
        
        // Render children
        for (BedrockBone child : bone.children) {
            renderBone(child, animTransforms, textureWidth, textureHeight);
        }
        
        GlStateManager.popMatrix();
    }
    
    private static void renderCube(BedrockCube cube, float[] bonePivot, 
            int textureWidth, int textureHeight) {
        GlStateManager.pushMatrix();
        
        // Apply cube-local pivot and rotation if present
        if (cube.pivot != null && cube.rotation != null) {
            GlStateManager.translate(cube.pivot[0], cube.pivot[1], cube.pivot[2]);
            GlStateManager.rotate(cube.rotation[2], 0, 0, 1);
            GlStateManager.rotate(cube.rotation[1], 0, 1, 0);
            GlStateManager.rotate(cube.rotation[0], 1, 0, 0);
            GlStateManager.translate(-cube.pivot[0], -cube.pivot[1], -cube.pivot[2]);
        }
        
        float[] origin = cube.getInflatedOrigin();
        float[] size = cube.getInflatedSize();
        
        float x1 = origin[0];
        float y1 = origin[1];
        float z1 = origin[2];
        float x2 = origin[0] + size[0];
        float y2 = origin[1] + size[1];
        float z2 = origin[2] + size[2];
        
        // Calculate UV coordinates
        float u = cube.uv[0];
        float v = cube.uv[1];
        float w = size[0];
        float h = size[1];
        float d = size[2];
        
        // Normalize UV to 0-1 range
        float texU = u / textureWidth;
        float texV = v / textureHeight;
        float texW = w / textureWidth;
        float texH = h / textureHeight;
        float texD = d / textureWidth;
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
        
        // Front face (Z+)
        addVertex(buffer, x1, y1, z2, texU + texD, texV + texD + texH, 0, 0, 1);
        addVertex(buffer, x2, y1, z2, texU + texD + texW, texV + texD + texH, 0, 0, 1);
        addVertex(buffer, x2, y2, z2, texU + texD + texW, texV + texD, 0, 0, 1);
        addVertex(buffer, x1, y2, z2, texU + texD, texV + texD, 0, 0, 1);
        
        // Back face (Z-)
        addVertex(buffer, x2, y1, z1, texU + texD + texW + texD, texV + texD + texH, 0, 0, -1);
        addVertex(buffer, x1, y1, z1, texU + texD + texW + texD + texW, texV + texD + texH, 0, 0, -1);
        addVertex(buffer, x1, y2, z1, texU + texD + texW + texD + texW, texV + texD, 0, 0, -1);
        addVertex(buffer, x2, y2, z1, texU + texD + texW + texD, texV + texD, 0, 0, -1);
        
        // Top face (Y+)
        addVertex(buffer, x1, y2, z1, texU + texD, texV, 0, 1, 0);
        addVertex(buffer, x1, y2, z2, texU + texD, texV + texD, 0, 1, 0);
        addVertex(buffer, x2, y2, z2, texU + texD + texW, texV + texD, 0, 1, 0);
        addVertex(buffer, x2, y2, z1, texU + texD + texW, texV, 0, 1, 0);
        
        // Bottom face (Y-)
        addVertex(buffer, x1, y1, z2, texU + texD + texW, texV, 0, -1, 0);
        addVertex(buffer, x1, y1, z1, texU + texD + texW, texV + texD, 0, -1, 0);
        addVertex(buffer, x2, y1, z1, texU + texD + texW + texW, texV + texD, 0, -1, 0);
        addVertex(buffer, x2, y1, z2, texU + texD + texW + texW, texV, 0, -1, 0);
        
        // Right face (X+)
        addVertex(buffer, x2, y1, z2, texU, texV + texD + texH, 1, 0, 0);
        addVertex(buffer, x2, y1, z1, texU + texD, texV + texD + texH, 1, 0, 0);
        addVertex(buffer, x2, y2, z1, texU + texD, texV + texD, 1, 0, 0);
        addVertex(buffer, x2, y2, z2, texU, texV + texD, 1, 0, 0);
        
        // Left face (X-)
        addVertex(buffer, x1, y1, z1, texU + texD + texW + texD + texW, texV + texD + texH, -1, 0, 0);
        addVertex(buffer, x1, y1, z2, texU + texD + texW + texD + texW + texD, texV + texD + texH, -1, 0, 0);
        addVertex(buffer, x1, y2, z2, texU + texD + texW + texD + texW + texD, texV + texD, -1, 0, 0);
        addVertex(buffer, x1, y2, z1, texU + texD + texW + texD + texW, texV + texD, -1, 0, 0);
        
        tessellator.draw();
        
        GlStateManager.popMatrix();
    }
    
    private static void addVertex(BufferBuilder buffer, float x, float y, float z, 
            float u, float v, float nx, float ny, float nz) {
        buffer.pos(x, y, z).tex(u, v).normal(nx, ny, nz).endVertex();
    }
}
