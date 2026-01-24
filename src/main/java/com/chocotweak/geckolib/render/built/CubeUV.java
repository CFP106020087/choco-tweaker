package com.chocotweak.geckolib.render.built;

/**
 * Holds UV data for a cube - either box UV or per-face UV.
 * Backport from YSM GeckoLib 1.20.1 UvUnion concept.
 */
public class CubeUV {
    public final boolean isBoxUV;
    public final double[] boxUVCoords;  // For box UV: [u, v]
    
    // For per-face UV:
    public final FaceUV north;
    public final FaceUV south;
    public final FaceUV east;
    public final FaceUV west;
    public final FaceUV up;
    public final FaceUV down;
    
    /**
     * Create box UV.
     */
    public CubeUV(double[] boxUVCoords) {
        this.isBoxUV = true;
        this.boxUVCoords = boxUVCoords != null ? boxUVCoords : new double[]{0, 0};
        this.north = null;
        this.south = null;
        this.east = null;
        this.west = null;
        this.up = null;
        this.down = null;
    }
    
    /**
     * Create per-face UV.
     */
    public CubeUV(FaceUV north, FaceUV south, FaceUV east, FaceUV west, FaceUV up, FaceUV down) {
        this.isBoxUV = false;
        this.boxUVCoords = null;
        this.north = north;
        this.south = south;
        this.east = east;
        this.west = west;
        this.up = up;
        this.down = down;
    }
}
