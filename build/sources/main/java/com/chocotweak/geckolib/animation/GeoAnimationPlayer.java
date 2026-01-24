package com.chocotweak.geckolib.animation;

import com.chocotweak.geckolib.render.built.GeoBone;
import com.chocotweak.geckolib.render.built.GeoModel;

import java.util.List;

/**
 * Plays GeoAnimation on a GeoModel by interpolating keyframes.
 * Backport from YSM GeckoLib 1.20.1.
 */
public class GeoAnimationPlayer {

    /**
     * Apply animation to model at given time.
     * 
     * @param model     The model to animate
     * @param animation The animation to play
     * @param time      Current animation time in seconds
     */
    public static void animate(GeoModel model, GeoAnimation animation, float time) {
        if (model == null || animation == null)
            return;

        // Reset all bones to initial state first
        resetBones(model);

        // Get looped time
        float animTime = animation.getAnimationTime(time);

        // Apply animation to each bone
        for (GeoBoneAnimation boneAnim : animation.boneAnimations.values()) {
            GeoBone bone = model.findBone(boneAnim.boneName);
            if (bone == null)
                continue;

            // Apply rotation animation
            if (boneAnim.hasRotation()) {
                float[] rotation = interpolateChannel(boneAnim.rotationKeyframes, animTime);
                // Rotation values in animation are in degrees, need to convert and add to
                // initial
                bone.setRotationX(bone.getInitialRotationX() + (float) Math.toRadians(rotation[0]));
                bone.setRotationY(bone.getInitialRotationY() + (float) Math.toRadians(rotation[1]));
                bone.setRotationZ(bone.getInitialRotationZ() + (float) Math.toRadians(rotation[2]));
            }

            // Apply position animation
            if (boneAnim.hasPosition()) {
                float[] position = interpolateChannel(boneAnim.positionKeyframes, animTime);
                bone.setPositionX(position[0]);
                bone.setPositionY(position[1]);
                bone.setPositionZ(position[2]);
            }

            // Apply scale animation
            if (boneAnim.hasScale()) {
                float[] scale = interpolateChannel(boneAnim.scaleKeyframes, animTime);
                bone.setScaleX(scale[0]);
                bone.setScaleY(scale[1]);
                bone.setScaleZ(scale[2]);
            }
        }
    }

    /**
     * Apply animation additively on top of existing bone transforms.
     * Does NOT reset bones first - used for animation layering (e.g., arm attack
     * over walk).
     * 
     * @param model     The model to animate
     * @param animation The animation to apply additively
     * @param time      Current animation time in seconds
     */
    public static void animateAdditive(GeoModel model, GeoAnimation animation, float time) {
        if (model == null || animation == null)
            return;

        // Get looped time
        float animTime = animation.getAnimationTime(time);

        // Apply animation additively to each bone (add to current rotation, not
        // initial)
        for (GeoBoneAnimation boneAnim : animation.boneAnimations.values()) {
            GeoBone bone = model.findBone(boneAnim.boneName);
            if (bone == null)
                continue;

            // Apply rotation animation additively
            if (boneAnim.hasRotation()) {
                float[] rotation = interpolateChannel(boneAnim.rotationKeyframes, animTime);
                // Add rotation to current bone rotation (not initial!)
                bone.setRotationX(bone.getRotationX() + (float) Math.toRadians(rotation[0]));
                bone.setRotationY(bone.getRotationY() + (float) Math.toRadians(rotation[1]));
                bone.setRotationZ(bone.getRotationZ() + (float) Math.toRadians(rotation[2]));
            }

            // Skip position and scale for additive - usually only want rotation on overlay
            // animations
        }
    }

    /**
     * Apply CQ-style swing overlay to the swinging arm based on entity's
     * swingProgress.
     * This makes YSM arm movement closer to vanilla CQ animations.
     * 
     * CQ formula: swing = sin(swingProgress * PI)
     * - arm.rotateAngleX -= swing * 1.2
     * - arm.rotateAngleZ = sin(swingProgress * PI) * -0.4 (mirrored for left)
     * 
     * @param model         The YSM model
     * @param swingProgress Entity's swing progress (0-1)
     * @param isLeftHand    True if swinging left hand, false for right hand
     */
    public static void applyCQSwingOverlay(GeoModel model, float swingProgress, boolean isLeftHand) {
        if (model == null || swingProgress <= 0) {
            return;
        }

        GeoBone arm;
        GeoBone foreArm;

        if (isLeftHand) {
            // Find left arm bone (YSM naming)
            arm = model.findBone("LeftArm");
            if (arm == null) {
                arm = model.findBone("Arm_Left");
            }
            foreArm = model.findBone("LeftForeArm");
            if (foreArm == null) {
                foreArm = model.findBone("ForeArm_Left");
            }
        } else {
            // Find right arm bone (YSM naming)
            arm = model.findBone("RightArm");
            if (arm == null) {
                arm = model.findBone("Arm_Right");
            }
            foreArm = model.findBone("RightForeArm");
            if (foreArm == null) {
                foreArm = model.findBone("ForeArm_Right");
            }
        }

        if (arm == null) {
            return;
        }

        // CQ swing formula
        float swing = (float) Math.sin(swingProgress * Math.PI);
        float swingX = -swing * 1.2f; // Forward swing (same for both hands)
        // Side swing - mirrored for left hand
        float swingZ = isLeftHand ? (swing * 0.4f) : (swing * -0.4f);

        // Apply additional rotation (additive to animation)
        arm.setRotationX(arm.getRotationX() + swingX);
        arm.setRotationZ(arm.getRotationZ() + swingZ);

        // Also apply to forearm for more natural look
        if (foreArm != null) {
            foreArm.setRotationX(foreArm.getRotationX() + swingX * 0.5f);
        }
    }

    /**
     * Legacy overload for backward compatibility - defaults to right hand
     */
    public static void applyCQSwingOverlay(GeoModel model, float swingProgress) {
        applyCQSwingOverlay(model, swingProgress, false);
    }

    /**
     * Interpolate keyframes at given time.
     * 
     * @param keyframes List of keyframes (sorted by time)
     * @param time      Current animation time
     * @return Interpolated [x, y, z] values
     */
    private static float[] interpolateChannel(List<GeoKeyframe> keyframes, float time) {
        if (keyframes.isEmpty()) {
            return new float[] { 0, 0, 0 };
        }

        // Single keyframe or before first keyframe
        if (keyframes.size() == 1 || time <= keyframes.get(0).time) {
            GeoKeyframe kf = keyframes.get(0);
            return new float[] { kf.getX(), kf.getY(), kf.getZ() };
        }

        // After last keyframe
        GeoKeyframe last = keyframes.get(keyframes.size() - 1);
        if (time >= last.time) {
            return new float[] { last.getX(), last.getY(), last.getZ() };
        }

        // Find surrounding keyframes
        GeoKeyframe before = keyframes.get(0);
        GeoKeyframe after = keyframes.get(0);

        for (int i = 0; i < keyframes.size() - 1; i++) {
            GeoKeyframe current = keyframes.get(i);
            GeoKeyframe next = keyframes.get(i + 1);
            if (time >= current.time && time < next.time) {
                before = current;
                after = next;
                break;
            }
        }

        // Calculate interpolation factor (0-1)
        float duration = after.time - before.time;
        float progress = duration > 0 ? (time - before.time) / duration : 0;

        // Linear interpolation
        return new float[] {
                lerp(before.getX(), after.getX(), progress),
                lerp(before.getY(), after.getY(), progress),
                lerp(before.getZ(), after.getZ(), progress)
        };
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    /**
     * Reset all bone transforms in a model to their initial values.
     */
    public static void resetBones(GeoModel model) {
        if (model == null)
            return;
        for (GeoBone bone : model.topLevelBones) {
            resetBoneRecursive(bone);
        }
    }

    private static void resetBoneRecursive(GeoBone bone) {
        bone.setRotationX(bone.getInitialRotationX());
        bone.setRotationY(bone.getInitialRotationY());
        bone.setRotationZ(bone.getInitialRotationZ());
        bone.resetTransforms();

        for (GeoBone child : bone.childBones) {
            resetBoneRecursive(child);
        }
    }
}
