package com.chocotweak.geckolib.render.built;

/**
 * Represents per-face UV data for a single face.
 * Used when model uses per-face UV instead of box UV.
 * 
 * Backport from YSM GeckoLib 1.20.1 FaceUv.
 */
public class FaceUV {
    public final double[] uv;      // [u, v] origin
    public final double[] uvSize;  // [width, height]
    
    public FaceUV(double[] uv, double[] uvSize) {
        this.uv = uv != null ? uv : new double[]{0, 0};
        this.uvSize = uvSize != null ? uvSize : new double[]{0, 0};
    }
    
    public double[] getUv() {
        return uv;
    }
    
    public double[] getUvSize() {
        return uvSize;
    }
}
