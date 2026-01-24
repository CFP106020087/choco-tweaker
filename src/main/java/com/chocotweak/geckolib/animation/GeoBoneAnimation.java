package com.chocotweak.geckolib.animation;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents animation data for a single bone.
 * Contains rotation, position, and scale keyframes.
 * Backport from YSM GeckoLib 1.20.1.
 */
public class GeoBoneAnimation {
    public final String boneName;
    public final List<GeoKeyframe> rotationKeyframes = new ArrayList<>();
    public final List<GeoKeyframe> positionKeyframes = new ArrayList<>();
    public final List<GeoKeyframe> scaleKeyframes = new ArrayList<>();
    
    public GeoBoneAnimation(String boneName) {
        this.boneName = boneName;
    }
    
    public boolean hasRotation() {
        return !rotationKeyframes.isEmpty();
    }
    
    public boolean hasPosition() {
        return !positionKeyframes.isEmpty();
    }
    
    public boolean hasScale() {
        return !scaleKeyframes.isEmpty();
    }
    
    /**
     * Get the keyframe pair (before and after) for a given time.
     * Returns [beforeKeyframe, afterKeyframe] or null if no keyframes.
     */
    public GeoKeyframe[] getKeyframePair(List<GeoKeyframe> keyframes, float time) {
        if (keyframes.isEmpty()) return null;
        
        // If only one keyframe or before first keyframe
        if (keyframes.size() == 1 || time <= keyframes.get(0).time) {
            return new GeoKeyframe[]{keyframes.get(0), keyframes.get(0)};
        }
        
        // If after last keyframe
        GeoKeyframe last = keyframes.get(keyframes.size() - 1);
        if (time >= last.time) {
            return new GeoKeyframe[]{last, last};
        }
        
        // Find surrounding keyframes
        for (int i = 0; i < keyframes.size() - 1; i++) {
            GeoKeyframe current = keyframes.get(i);
            GeoKeyframe next = keyframes.get(i + 1);
            if (time >= current.time && time < next.time) {
                return new GeoKeyframe[]{current, next};
            }
        }
        
        return null;
    }
}
