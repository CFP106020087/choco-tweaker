package com.chocotweak.bedrock.animation;

/**
 * Represents a single keyframe in a Bedrock animation.
 * Keyframes define bone transformations at specific timestamps.
 */
public class BedrockKeyframe {
    
    public final float timestamp;
    public final float[] rotation;   // [x, y, z] in degrees, may be null
    public final float[] position;   // [x, y, z], may be null
    public final float[] scale;      // [x, y, z], may be null
    
    public BedrockKeyframe(float timestamp, float[] rotation, float[] position, float[] scale) {
        this.timestamp = timestamp;
        this.rotation = rotation;
        this.position = position;
        this.scale = scale;
    }
    
    /**
     * Linearly interpolate between this keyframe and the next.
     * 
     * @param next The next keyframe
     * @param t Interpolation factor (0-1)
     * @return Interpolated transforms [rotX, rotY, rotZ, posX, posY, posZ, scaleX, scaleY, scaleZ]
     */
    public float[] lerp(BedrockKeyframe next, float t) {
        float[] result = new float[9];
        
        // Interpolate rotation
        if (rotation != null && next.rotation != null) {
            result[0] = lerpValue(rotation[0], next.rotation[0], t);
            result[1] = lerpValue(rotation[1], next.rotation[1], t);
            result[2] = lerpValue(rotation[2], next.rotation[2], t);
        } else if (rotation != null) {
            result[0] = rotation[0];
            result[1] = rotation[1];
            result[2] = rotation[2];
        } else if (next.rotation != null) {
            result[0] = next.rotation[0];
            result[1] = next.rotation[1];
            result[2] = next.rotation[2];
        }
        
        // Interpolate position
        if (position != null && next.position != null) {
            result[3] = lerpValue(position[0], next.position[0], t);
            result[4] = lerpValue(position[1], next.position[1], t);
            result[5] = lerpValue(position[2], next.position[2], t);
        } else if (position != null) {
            result[3] = position[0];
            result[4] = position[1];
            result[5] = position[2];
        } else if (next.position != null) {
            result[3] = next.position[0];
            result[4] = next.position[1];
            result[5] = next.position[2];
        }
        
        // Interpolate scale
        if (scale != null && next.scale != null) {
            result[6] = lerpValue(scale[0], next.scale[0], t);
            result[7] = lerpValue(scale[1], next.scale[1], t);
            result[8] = lerpValue(scale[2], next.scale[2], t);
        } else if (scale != null) {
            result[6] = scale[0];
            result[7] = scale[1];
            result[8] = scale[2];
        } else if (next.scale != null) {
            result[6] = next.scale[0];
            result[7] = next.scale[1];
            result[8] = next.scale[2];
        } else {
            result[6] = 1f;
            result[7] = 1f;
            result[8] = 1f;
        }
        
        return result;
    }
    
    private float lerpValue(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
