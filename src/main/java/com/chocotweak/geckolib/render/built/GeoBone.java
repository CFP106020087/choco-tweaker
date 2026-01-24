package com.chocotweak.geckolib.render.built;

import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bone in the model hierarchy.
 * Backport from YSM GeckoLib 1.20.1.
 */
public class GeoBone {
    public GeoBone parent;
    public List<GeoBone> childBones = new ArrayList<>();
    public List<GeoCube> childCubes = new ArrayList<>();

    public String name;

    // Rotation point (pivot)
    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;

    // Animated position offset
    private float positionX;
    private float positionY;
    private float positionZ;

    // Animated rotation (radians)
    private float rotationX;
    private float rotationY;
    private float rotationZ;

    // Animated scale
    private float scaleX = 1;
    private float scaleY = 1;
    private float scaleZ = 1;

    private boolean hidden = false;
    private boolean childCubesHidden = false;
    private boolean childBonesHiddenToo = false;

    public GeoBone(String name, float pivotX, float pivotY, float pivotZ,
            float rotX, float rotY, float rotZ) {
        this.name = name;
        this.rotationPointX = pivotX;
        this.rotationPointY = pivotY;
        this.rotationPointZ = pivotZ;
        this.rotationX = rotX;
        this.rotationY = rotY;
        this.rotationZ = rotZ;
    }

    // Position getters/setters
    public float getPositionX() {
        return positionX;
    }

    public void setPositionX(float value) {
        this.positionX = value;
    }

    public float getPositionY() {
        return positionY;
    }

    public void setPositionY(float value) {
        this.positionY = value;
    }

    public float getPositionZ() {
        return positionZ;
    }

    public void setPositionZ(float value) {
        this.positionZ = value;
    }

    // Rotation getters/setters
    public float getRotationX() {
        return rotationX;
    }

    public void setRotationX(float value) {
        this.rotationX = value;
    }

    public float getRotationY() {
        return rotationY;
    }

    public void setRotationY(float value) {
        this.rotationY = value;
    }

    public float getRotationZ() {
        return rotationZ;
    }

    public void setRotationZ(float value) {
        this.rotationZ = value;
    }

    // Scale getters/setters
    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float value) {
        this.scaleX = value;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float value) {
        this.scaleY = value;
    }

    public float getScaleZ() {
        return scaleZ;
    }

    public void setScaleZ(float value) {
        this.scaleZ = value;
    }

    // Visibility
    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean cubesAreHidden() {
        return childCubesHidden;
    }

    public void setCubesHidden(boolean hidden) {
        this.childCubesHidden = hidden;
    }

    public boolean childBonesAreHiddenToo() {
        return childBonesHiddenToo;
    }

    public void setChildBonesHiddenToo(boolean hidden) {
        this.childBonesHiddenToo = hidden;
    }

    public String getName() {
        return name;
    }

    /**
     * Set rotation from degrees.
     */
    public void setRotationDegrees(float rotX, float rotY, float rotZ) {
        this.rotationX = (float) Math.toRadians(rotX);
        this.rotationY = (float) Math.toRadians(rotY);
        this.rotationZ = (float) Math.toRadians(rotZ);
    }

    /**
     * Reset animated transforms to defaults.
     */
    public void resetTransforms() {
        this.positionX = 0;
        this.positionY = 0;
        this.positionZ = 0;
        this.scaleX = 1;
        this.scaleY = 1;
        this.scaleZ = 1;
    }

    // Initial rotation values (set during model load)
    private float initialRotationX;
    private float initialRotationY;
    private float initialRotationZ;

    /**
     * Save initial rotation values for animation reset.
     */
    public void saveInitialRotation() {
        this.initialRotationX = this.rotationX;
        this.initialRotationY = this.rotationY;
        this.initialRotationZ = this.rotationZ;
    }

    public float getInitialRotationX() {
        return initialRotationX;
    }

    public float getInitialRotationY() {
        return initialRotationY;
    }

    public float getInitialRotationZ() {
        return initialRotationZ;
    }

    /**
     * Find a bone by name in this bone's hierarchy.
     */
    public GeoBone findBone(String boneName) {
        if (this.name != null && this.name.equals(boneName)) {
            return this;
        }
        for (GeoBone child : childBones) {
            GeoBone found = child.findBone(boneName);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
