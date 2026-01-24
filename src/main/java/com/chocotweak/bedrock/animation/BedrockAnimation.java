package com.chocotweak.bedrock.animation;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a complete Bedrock animation.
 * Contains bone animations and loop/length properties.
 */
public class BedrockAnimation {
    
    public final String name;
    public final boolean loop;
    public final float length;
    public final Map<String, BedrockBoneAnimation> boneAnimations;
    
    public BedrockAnimation(String name, boolean loop, float length) {
        this.name = name;
        this.loop = loop;
        this.length = length;
        this.boneAnimations = new HashMap<>();
    }
    
    public void addBoneAnimation(BedrockBoneAnimation boneAnim) {
        boneAnimations.put(boneAnim.boneName, boneAnim);
    }
    
    public BedrockBoneAnimation getBoneAnimation(String boneName) {
        return boneAnimations.get(boneName);
    }
    
    /**
     * Get all bone transforms at a given time.
     * 
     * @param time Current animation time
     * @return Map of bone name to transforms [rotX, rotY, rotZ, posX, posY, posZ, scaleX, scaleY, scaleZ]
     */
    public Map<String, float[]> getAllTransformsAtTime(float time) {
        // Handle looping
        float animTime = time;
        if (loop && length > 0) {
            animTime = time % length;
        } else if (length > 0) {
            animTime = Math.min(time, length);
        }
        
        Map<String, float[]> result = new HashMap<>();
        for (Map.Entry<String, BedrockBoneAnimation> entry : boneAnimations.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getTransformAtTime(animTime));
        }
        return result;
    }
    
    /**
     * Check if the animation has finished (only for non-looping animations)
     */
    public boolean isFinished(float time) {
        return !loop && time >= length;
    }
}
