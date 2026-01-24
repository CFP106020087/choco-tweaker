package com.chocotweak.bedrock.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bone in the Bedrock model hierarchy.
 * Bones form a tree structure and can contain cubes and child bones.
 */
public class BedrockBone {
    
    public final String name;
    public final String parentName;  // null if root bone
    public final float[] pivot;      // transformation origin [x, y, z]
    public final float[] rotation;   // default rotation [x, y, z] in degrees
    public final List<BedrockCube> cubes;
    public final List<BedrockBone> children;
    
    private BedrockBone parent;
    
    public BedrockBone(JsonObject json, int textureWidth, int textureHeight) {
        this.name = json.has("name") ? json.get("name").getAsString() : "unnamed";
        this.parentName = json.has("parent") ? json.get("parent").getAsString() : null;
        this.pivot = parseVec3(json, "pivot", new float[]{0, 0, 0});
        this.rotation = parseVec3(json, "rotation", new float[]{0, 0, 0});
        this.cubes = new ArrayList<>();
        this.children = new ArrayList<>();
        
        // Parse cubes if present
        if (json.has("cubes")) {
            JsonArray cubesArray = json.getAsJsonArray("cubes");
            for (JsonElement cubeElement : cubesArray) {
                cubes.add(new BedrockCube(cubeElement.getAsJsonObject(), textureWidth, textureHeight));
            }
        }
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
    
    public void setParent(BedrockBone parent) {
        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
        }
    }
    
    public BedrockBone getParent() {
        return parent;
    }
    
    public boolean isRoot() {
        return parentName == null;
    }
    
    /**
     * Get the world-space pivot by accumulating parent transforms
     */
    public float[] getWorldPivot() {
        if (parent == null) {
            return pivot.clone();
        }
        float[] parentPivot = parent.getWorldPivot();
        return new float[]{
            parentPivot[0] + pivot[0],
            parentPivot[1] + pivot[1],
            parentPivot[2] + pivot[2]
        };
    }
}
