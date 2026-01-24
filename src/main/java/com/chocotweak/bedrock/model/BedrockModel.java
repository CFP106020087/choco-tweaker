package com.chocotweak.bedrock.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a complete Bedrock geometry model.
 * Contains the bone hierarchy and texture dimensions.
 */
public class BedrockModel {
    
    public final String identifier;
    public final int textureWidth;
    public final int textureHeight;
    public final List<BedrockBone> rootBones;
    public final Map<String, BedrockBone> boneMap;
    
    public BedrockModel(String identifier, int textureWidth, int textureHeight) {
        this.identifier = identifier;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.rootBones = new ArrayList<>();
        this.boneMap = new HashMap<>();
    }
    
    public void addBone(BedrockBone bone) {
        boneMap.put(bone.name, bone);
    }
    
    /**
     * Build the bone hierarchy after all bones have been added.
     * Must be called after all addBone() calls.
     */
    public void buildHierarchy() {
        for (BedrockBone bone : boneMap.values()) {
            if (bone.parentName != null) {
                BedrockBone parent = boneMap.get(bone.parentName);
                if (parent != null) {
                    bone.setParent(parent);
                } else {
                    // Parent not found, treat as root
                    rootBones.add(bone);
                }
            } else {
                rootBones.add(bone);
            }
        }
    }
    
    public BedrockBone getBone(String name) {
        return boneMap.get(name);
    }
    
    public List<BedrockBone> getAllBones() {
        return new ArrayList<>(boneMap.values());
    }
}
