package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.client.rendererHuman.RenderHuman;
import com.chocolate.chocolateQuest.entity.EntityHumanBase;
import com.chocotweak.bedrock.entity.IEasterEggCapable;
import com.chocotweak.geckolib.animation.GeoAnimation;
import com.chocotweak.geckolib.animation.GeoAnimationLoader;
import com.chocotweak.geckolib.animation.GeoAnimationPlayer;
import com.chocotweak.geckolib.animation.YsmAnimationMapper;
import com.chocotweak.geckolib.loader.GeoModelLoader;
import com.chocotweak.geckolib.render.EquipmentLocatorRenderer;
import com.chocotweak.geckolib.render.GeoModelRenderer;
import com.chocotweak.geckolib.render.built.GeoModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Mixin to intercept RenderHuman.renderModel and use GeckoLib renderer for
 * Easter Egg NPCs.
 * Uses faithful YSM GeckoLib backport for 1.12.2.
 */
@Mixin(value = RenderHuman.class, remap = false)
public class MixinRenderHumanEasterEgg {

    @Unique
    private static final String[] WINE_FOX_VARIANTS = {
            "01_taisho_maid", "02_new_year", "03_astronaut", "04_kongfu", "05_magical",
            "06_hanfu", "07_jk", "08_sta", "09_hailuo", "10_zhiban",
            "11_salesperson", "12_little", "13_matured", "14_momo", "15_kluonoa"
    };

    // Each variant's main texture filename (different from skin.png for some)
    @Unique
    private static final String[] WINE_FOX_TEXTURES = {
            "skin.png", // 01_taisho_maid
            "skin.png", // 02_new_year
            "skin.png", // 03_astronaut
            "default.png", // 04_kongfu
            "winefox.png", // 05_magical
            "default.png", // 06_hanfu
            "skin.png", // 07_jk
            "texture.png", // 08_sta
            "texture.png", // 09_hailuo
            "default.png", // 10_zhiban
            "default.png", // 11_salesperson
            "skin.png", // 12_little
            "default.png", // 13_matured
            "skin_pink.png", // 14_momo
            "texture.png" // 15_kluonoa
    };

    @Unique
    private static final Map<Integer, GeoModel> chocotweak$modelCache = new HashMap<>();

    @Unique
    private static final Map<Integer, ResourceLocation> chocotweak$textureCache = new HashMap<>();

    @Unique
    private static final Map<Integer, Map<String, GeoAnimation>> chocotweak$animationCache = new HashMap<>();

    /**
     * Override getEntityTexture for Easter Egg NPCs to return a valid texture.
     * This prevents NPE in RenderNPC.bindTexture when the entity has special
     * texture handling.
     * We return Steve texture as fallback since the actual texture is bound
     * in renderModel when using GeckoLib renderer.
     */
    @Inject(method = "getEntityTexture", at = @At("HEAD"), cancellable = true)
    private void chocotweak$overrideTexture(EntityHumanBase entity, CallbackInfoReturnable<ResourceLocation> cir) {
        if (entity instanceof IEasterEggCapable) {
            IEasterEggCapable easterEgg = (IEasterEggCapable) entity;
            if (easterEgg.isEasterEggNpc()) {
                // Return Steve texture as fallback - actual YSM texture is bound in renderModel
                cir.setReturnValue(new ResourceLocation("textures/entity/steve.png"));
            }
        }
    }

    /**
     * Intercept renderModel to use GeckoLib rendering for Easter Egg NPCs.
     */
    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
    private void chocotweak$renderBedrockModel(EntityHumanBase entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {

        if (!(entity instanceof IEasterEggCapable)) {
            return;
        }

        IEasterEggCapable easterEgg = (IEasterEggCapable) entity;
        if (!easterEgg.isEasterEggNpc()) {
            return;
        }

        // Cancel vanilla rendering
        ci.cancel();

        int variant = easterEgg.getModelVariant();

        // Get or load model and animation
        GeoModel model = chocotweak$getModel(variant);
        ResourceLocation texture = chocotweak$getTexture(variant);
        Map<String, GeoAnimation> animations = chocotweak$getAnimations(variant);

        if (model == null) {
            return; // Model not loaded, skip rendering
        }

        // Apply animation using YsmAnimationMapper for AI-driven selection
        if (animations != null && !animations.isEmpty()) {
            float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();

            // === 动画分层系统 ===
            // 1. 先获取基础动画（行走/奔跑/待机等）- 不考虑攻击状态
            String baseAnimName = YsmAnimationMapper.getBaseAnimationForState(entity, limbSwingAmount,
                    entity.ticksExisted);
            float baseAnimTime = YsmAnimationMapper.getAnimationTime(entity, baseAnimName, partialTicks);

            GeoAnimation baseAnim = animations.get(baseAnimName);
            if (baseAnim == null && baseAnimName.startsWith("new_idle_")) {
                baseAnim = animations.get("idle");
            }
            if (baseAnim == null) {
                baseAnim = animations.get("idle");
            }

            // 2. 播放基础动画（整个身体）
            if (baseAnim != null) {
                GeoAnimationPlayer.animate(model, baseAnim, baseAnimTime);
            }

            // 3. 如果正在攻击，叠加手臂攻击动画
            if (entity.swingProgress > 0 || entity.isSwingInProgress) {
                GeoAnimation swingAnim = animations.get(YsmAnimationMapper.SWING_HAND);
                if (swingAnim != null) {
                    // 攻击动画使用 swingProgress 作为时间
                    float swingTime = entity.swingProgress * swingAnim.length;
                    // 叠加攻击动画（只影响手臂骨骼）
                    GeoAnimationPlayer.animateAdditive(model, swingAnim, swingTime);
                }
            }

            // DEBUG: Log animation status every 100 ticks
            if (entity.ticksExisted % 100 == 0) {
                System.out.println("[YSM-DEBUG] Entity: " + entity.getName() + " | BaseAnim: " + baseAnimName
                        + " | Swing: " + entity.swingProgress);
            }
        } else {
            // DEBUG: No animations loaded
            if (entity.ticksExisted % 100 == 0) {
                System.out.println("[YSM-DEBUG] NO ANIMATIONS! animations=" + (animations == null ? "NULL" : "EMPTY"));
            }
        }

        // Bind texture
        if (texture != null) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        }

        // Enable GL states for proper rendering
        GlStateManager.pushMatrix();

        // Enable texturing and blending
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // Disable lighting for cleaner texture display
        GlStateManager.disableLighting();

        // Disable culling for double-sided faces
        GlStateManager.disableCull();

        // Enable alpha test for transparent textures
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);

        // Flip and offset to match CQ coordinate system
        // YSM models are designed for Y-up, MC uses Y-down for entity rendering
        // Also mirror X axis to correct left/right hand swap
        GlStateManager.scale(-1.0f, -1.0f, 1.0f); // Flip Y and mirror X
        GlStateManager.translate(0, -1.5f, 0); // Move up (24 blocks = 1.5 entity units)

        // Apply sitting/sleeping offset (matching CQ RenderHuman.renderModel)
        if (entity.isSitting()) {
            // CQ sit offset: translate(0, 0.6, 0) but we're Y-flipped so negate
            GlStateManager.translate(0.0f, -0.6f, 0.0f);
        } else if (entity.isSleeping()) {
            // CQ sleeping: translate(0, 1.35, 0) + rotate -90 on X
            // For YSM we keep model upright but offset down
            GlStateManager.translate(0.0f, -1.35f, 0.0f);
        }

        // Since Y is flipped, front face winding is reversed - set to CW
        GL11.glFrontFace(GL11.GL_CW);

        // Render using GeckoLib renderer
        GeoModelRenderer.render(model);

        // Equipment rendered by CQ's native LayerHeldItem - no custom rendering needed
        // YSM animations will be modified to match CQ armature poses

        // Restore GL states
        GL11.glFrontFace(GL11.GL_CCW); // Restore default CCW
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Unique
    private static GeoModel chocotweak$getModel(int variant) {
        if (chocotweak$modelCache.containsKey(variant)) {
            return chocotweak$modelCache.get(variant);
        }

        String variantName = WINE_FOX_VARIANTS[Math.min(variant, WINE_FOX_VARIANTS.length - 1)];
        ResourceLocation modelLoc = new ResourceLocation("yes_steve_model",
                "builtin/wine_fox/" + variantName + "/models/main.json");

        GeoModel model = GeoModelLoader.load(modelLoc);
        if (model != null) {
            // Save initial rotations for animation
            model.saveInitialRotations();
        }
        chocotweak$modelCache.put(variant, model);
        return model;
    }

    @Unique
    private static ResourceLocation chocotweak$getTexture(int variant) {
        if (chocotweak$textureCache.containsKey(variant)) {
            return chocotweak$textureCache.get(variant);
        }

        int idx = Math.min(variant, WINE_FOX_VARIANTS.length - 1);
        String variantName = WINE_FOX_VARIANTS[idx];
        String textureName = WINE_FOX_TEXTURES[idx];
        // Path: assets/yes_steve_model/builtin/wine_fox/{variant}/textures/{texture}
        ResourceLocation texture = new ResourceLocation("yes_steve_model",
                "builtin/wine_fox/" + variantName + "/textures/" + textureName);

        chocotweak$textureCache.put(variant, texture);
        return texture;
    }

    @Unique
    private static Map<String, GeoAnimation> chocotweak$getAnimations(int variant) {
        if (chocotweak$animationCache.containsKey(variant)) {
            return chocotweak$animationCache.get(variant);
        }

        String variantName = WINE_FOX_VARIANTS[Math.min(variant, WINE_FOX_VARIANTS.length - 1)];
        ResourceLocation animLoc = new ResourceLocation("yes_steve_model",
                "builtin/wine_fox/" + variantName + "/animations/main.animation.json");

        Map<String, GeoAnimation> animations = GeoAnimationLoader.load(animLoc);
        chocotweak$animationCache.put(variant, animations);
        return animations;
    }
}
