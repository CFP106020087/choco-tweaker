package com.chocotweak.bedrock.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Represents a single cube in a Bedrock model.
 * Cubes are the basic building blocks that make up bones.
 */
public class BedrockCube {
    
    public final float[] origin;   // [x, y, z] - corner position
    public final float[] size;     // [width, height, depth]
    public final float[] pivot;    // [x, y, z] - rotation pivot (optional)
    public final float[] rotation; // [x, y, z] - rotation in degrees (optional)
    public final float[] uv;       // [u, v] - texture coordinates
    public final float inflate;    // size inflation
    public final boolean mirror;   // mirror texture
    
    public BedrockCube(JsonObject json, int textureWidth, int textureHeight) {
        this.origin = parseVec3(json, "origin", new float[]{0, 0, 0});
        this.size = parseVec3(json, "size", new float[]{1, 1, 1});
        this.pivot = parseVec3(json, "pivot", null);
        this.rotation = parseVec3(json, "rotation", null);
        this.uv = parseVec2(json, "uv", new float[]{0, 0});
        this.inflate = json.has("inflate") ? json.get("inflate").getAsFloat() : 0f;
        this.mirror = json.has("mirror") && json.get("mirror").getAsBoolean();
    }
    
    private float[] parseVec3(JsonObject json, String key, float[] defaultValue) {
        if (!json.has(key)) {
            return defaultValue;
        }
        JsonElement element = json.get(key);
        if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();
            return new float[]{
                arr.get(0).getAsFloat(),
                arr.get(1).getAsFloat(),
                arr.get(2).getAsFloat()
            };
        }
        return defaultValue;
    }
    
    private float[] parseVec2(JsonObject json, String key, float[] defaultValue) {
        if (!json.has(key)) {
            return defaultValue;
        }
        JsonElement element = json.get(key);
        if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();
            return new float[]{
                arr.get(0).getAsFloat(),
                arr.get(1).getAsFloat()
            };
        }
        return defaultValue;
    }
    
    /**
     * Get the actual origin with inflation applied
     */
    public float[] getInflatedOrigin() {
        return new float[]{
            origin[0] - inflate,
            origin[1] - inflate,
            origin[2] - inflate
        };
    }
    
    /**
     * Get the actual size with inflation applied
     */
    public float[] getInflatedSize() {
        return new float[]{
            size[0] + inflate * 2,
            size[1] + inflate * 2,
            size[2] + inflate * 2
        };
    }
}
