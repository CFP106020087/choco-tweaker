package com.chocotweak.geckolib.animation;

/**
 * Represents a single animation keyframe.
 * Backport from YSM GeckoLib 1.20.1.
 */
public class GeoKeyframe {
    public final float time;           // Time in seconds
    public final float[] values;       // [x, y, z] or [value] depending on channel
    public final String lerp;          // "linear", "catmullrom", or "step"
    
    public GeoKeyframe(float time, float[] values, String lerp) {
        this.time = time;
        this.values = values != null ? values : new float[]{0, 0, 0};
        this.lerp = lerp != null ? lerp : "linear";
    }
    
    public GeoKeyframe(float time, float[] values) {
        this(time, values, "linear");
    }
    
    public float getX() {
        return values.length > 0 ? values[0] : 0;
    }
    
    public float getY() {
        return values.length > 1 ? values[1] : 0;
    }
    
    public float getZ() {
        return values.length > 2 ? values[2] : 0;
    }
}
