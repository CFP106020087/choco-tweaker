package com.chocotweak.geckolib.animation;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a complete animation with all bone animations.
 * Backport from YSM GeckoLib 1.20.1.
 */
public class GeoAnimation {
    public final String name;
    public final float length;          // Animation length in seconds
    public final boolean loop;
    public final Map<String, GeoBoneAnimation> boneAnimations = new HashMap<>();
    
    public GeoAnimation(String name, float length, boolean loop) {
        this.name = name;
        this.length = length;
        this.loop = loop;
    }
    
    /**
     * Get bone animation by name, or null if not found.
     */
    public GeoBoneAnimation getBoneAnimation(String boneName) {
        return boneAnimations.get(boneName);
    }
    
    /**
     * Add or get existing bone animation.
     */
    public GeoBoneAnimation getOrCreateBoneAnimation(String boneName) {
        return boneAnimations.computeIfAbsent(boneName, GeoBoneAnimation::new);
    }
    
    /**
     * Calculate animation time with looping.
     */
    public float getAnimationTime(float rawTime) {
        if (length <= 0) return 0;
        
        if (loop) {
            return rawTime % length;
        } else {
            return Math.min(rawTime, length);
        }
    }
}
