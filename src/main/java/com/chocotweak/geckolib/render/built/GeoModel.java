package com.chocotweak.geckolib.render.built;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a complete GeckoLib model.
 * Backport from YSM GeckoLib 1.20.1.
 */
public class GeoModel {
    public List<GeoBone> topLevelBones = new ArrayList<>();
    public Map<String, GeoBone> boneMap = new HashMap<>();

    public String identifier;
    public int textureWidth;
    public int textureHeight;

    public GeoModel(String identifier, int textureWidth, int textureHeight) {
        this.identifier = identifier;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    public void addBone(GeoBone bone) {
        boneMap.put(bone.name, bone);
    }

    public void addTopLevelBone(GeoBone bone) {
        topLevelBones.add(bone);
    }

    public GeoBone getBone(String name) {
        return boneMap.get(name);
    }

    /**
     * Reset all bone transforms to initial state.
     */
    public void resetBoneTransforms() {
        for (GeoBone bone : boneMap.values()) {
            bone.resetTransforms();
        }
    }

    /**
     * Find a bone by name. First checks boneMap, then searches recursively.
     */
    public GeoBone findBone(String name) {
        // Fast path: check boneMap
        GeoBone bone = boneMap.get(name);
        if (bone != null)
            return bone;

        // Slow path: search recursively
        for (GeoBone topBone : topLevelBones) {
            bone = topBone.findBone(name);
            if (bone != null) {
                // Cache for future lookups
                boneMap.put(name, bone);
                return bone;
            }
        }
        return null;
    }

    /**
     * Save initial rotation values for all bones (call after model load).
     */
    public void saveInitialRotations() {
        for (GeoBone bone : boneMap.values()) {
            bone.saveInitialRotation();
        }
    }
}
