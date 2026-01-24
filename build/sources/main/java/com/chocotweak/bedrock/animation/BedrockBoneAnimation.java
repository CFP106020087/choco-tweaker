package com.chocotweak.bedrock.animation;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the animation track for a single bone.
 * Contains keyframes for rotation, position, and scale.
 */
public class BedrockBoneAnimation {
    
    public final String boneName;
    public final List<BedrockKeyframe> keyframes;
    
    public BedrockBoneAnimation(String boneName) {
        this.boneName = boneName;
        this.keyframes = new ArrayList<>();
    }
    
    public void addKeyframe(BedrockKeyframe keyframe) {
        // Insert in sorted order by timestamp
        int insertIndex = 0;
        for (int i = 0; i < keyframes.size(); i++) {
            if (keyframes.get(i).timestamp > keyframe.timestamp) {
                break;
            }
            insertIndex = i + 1;
        }
        keyframes.add(insertIndex, keyframe);
    }
    
    /**
     * Get the interpolated transforms at a given time.
     * 
     * @param time Current animation time
     * @return Interpolated transforms [rotX, rotY, rotZ, posX, posY, posZ, scaleX, scaleY, scaleZ]
     */
    public float[] getTransformAtTime(float time) {
        if (keyframes.isEmpty()) {
            return new float[]{0, 0, 0, 0, 0, 0, 1, 1, 1};
        }
        
        if (keyframes.size() == 1) {
            BedrockKeyframe kf = keyframes.get(0);
            return new float[]{
                kf.rotation != null ? kf.rotation[0] : 0,
                kf.rotation != null ? kf.rotation[1] : 0,
                kf.rotation != null ? kf.rotation[2] : 0,
                kf.position != null ? kf.position[0] : 0,
                kf.position != null ? kf.position[1] : 0,
                kf.position != null ? kf.position[2] : 0,
                kf.scale != null ? kf.scale[0] : 1,
                kf.scale != null ? kf.scale[1] : 1,
                kf.scale != null ? kf.scale[2] : 1
            };
        }
        
        // Find surrounding keyframes
        BedrockKeyframe prev = keyframes.get(0);
        BedrockKeyframe next = keyframes.get(keyframes.size() - 1);
        
        for (int i = 0; i < keyframes.size() - 1; i++) {
            if (keyframes.get(i).timestamp <= time && keyframes.get(i + 1).timestamp >= time) {
                prev = keyframes.get(i);
                next = keyframes.get(i + 1);
                break;
            }
        }
        
        // Calculate interpolation factor
        float duration = next.timestamp - prev.timestamp;
        float t = duration > 0 ? (time - prev.timestamp) / duration : 0;
        t = Math.max(0, Math.min(1, t));
        
        return prev.lerp(next, t);
    }
}
