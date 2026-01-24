package com.chocotweak.geckolib.render.built;

import org.lwjgl.util.vector.Vector3f;

/**
 * Represents a vertex with position and UV coordinates.
 * Backport from YSM GeckoLib 1.20.1.
 */
public class GeoVertex {
    public final Vector3f position;
    public float textureU;
    public float textureV;

    public GeoVertex(float x, float y, float z) {
        this.position = new Vector3f(x, y, z);
    }

    public GeoVertex(double x, double y, double z) {
        this.position = new Vector3f((float) x, (float) y, (float) z);
    }

    public GeoVertex(Vector3f posIn, float texU, float texV) {
        this.position = posIn;
        this.textureU = texU;
        this.textureV = texV;
    }

    public GeoVertex setTextureUV(float texU, float texV) {
        return new GeoVertex(new Vector3f(position), texU, texV);
    }
}
