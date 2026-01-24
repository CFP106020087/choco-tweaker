package com.chocotweak.geckolib.render.built;

import net.minecraft.util.EnumFacing;
import org.lwjgl.util.vector.Vector3f;

/**
 * Represents a quad face with 4 vertices and a normal.
 * Backport from YSM GeckoLib 1.20.1.
 */
public class GeoQuad {
    public final Vector3f normal;
    public GeoVertex[] vertices;
    public EnumFacing direction;

    public GeoQuad(GeoVertex[] verticesIn, float u1, float v1, float uSize, float vSize,
            float texWidth, float texHeight, Boolean mirrorIn, EnumFacing directionIn) {
        this.direction = directionIn;
        this.vertices = verticesIn;

        float u2 = u1 + uSize;
        float v2 = v1 + vSize;

        // Normalize UV to 0-1
        u1 /= texWidth;
        u2 /= texWidth;
        v1 /= texHeight;
        v2 /= texHeight;

        if (mirrorIn != null && mirrorIn) {
            vertices[0] = verticesIn[0].setTextureUV(u1, v1);
            vertices[1] = verticesIn[1].setTextureUV(u2, v1);
            vertices[2] = verticesIn[2].setTextureUV(u2, v2);
            vertices[3] = verticesIn[3].setTextureUV(u1, v2);
        } else {
            vertices[0] = verticesIn[0].setTextureUV(u2, v1);
            vertices[1] = verticesIn[1].setTextureUV(u1, v1);
            vertices[2] = verticesIn[2].setTextureUV(u1, v2);
            vertices[3] = verticesIn[3].setTextureUV(u2, v2);
        }

        this.normal = getDirectionNormal(directionIn);
        if (mirrorIn != null && mirrorIn) {
            this.normal.x *= -1.0F;
        }
    }

    public GeoQuad(GeoVertex[] verticesIn, double[] uvCoords, double[] uvSize, float texWidth,
            float texHeight, Boolean mirrorIn, EnumFacing directionIn) {
        this(verticesIn, (float) uvCoords[0], (float) uvCoords[1], (float) uvSize[0], (float) uvSize[1],
                texWidth, texHeight, mirrorIn, directionIn);
    }

    private static Vector3f getDirectionNormal(EnumFacing facing) {
        switch (facing) {
            case DOWN:
                return new Vector3f(0, -1, 0);
            case UP:
                return new Vector3f(0, 1, 0);
            case NORTH:
                return new Vector3f(0, 0, -1);
            case SOUTH:
                return new Vector3f(0, 0, 1);
            case WEST:
                return new Vector3f(-1, 0, 0);
            case EAST:
                return new Vector3f(1, 0, 0);
            default:
                return new Vector3f(0, 1, 0);
        }
    }
}
