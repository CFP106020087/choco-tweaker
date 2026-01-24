package com.chocotweak.geckolib.render;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Software matrix stack to replace OpenGL's hardware matrix stack.
 * Mimics 1.20.1 PoseStack behavior for accurate vertex transformation.
 */
public class MatrixStack {
    private final Deque<Matrix4f> stack = new ArrayDeque<>();
    
    public MatrixStack() {
        // 初始化为单位矩阵
        Matrix4f identity = new Matrix4f();
        identity.setIdentity();
        stack.push(identity);
    }
    
    public void push() {
        Matrix4f current = stack.peek();
        Matrix4f copy = new Matrix4f();
        copy.load(current);
        stack.push(copy);
    }
    
    public void pop() {
        if (stack.size() > 1) {
            stack.pop();
        }
    }
    
    public Matrix4f last() {
        return stack.peek();
    }
    
    /**
     * 平移当前矩阵
     */
    public void translate(float x, float y, float z) {
        Matrix4f current = stack.peek();
        Matrix4f translation = new Matrix4f();
        translation.setIdentity();
        translation.m30 = x;
        translation.m31 = y;
        translation.m32 = z;
        Matrix4f.mul(current, translation, current);
    }
    
    /**
     * 缩放当前矩阵
     */
    public void scale(float x, float y, float z) {
        Matrix4f current = stack.peek();
        Matrix4f scale = new Matrix4f();
        scale.setIdentity();
        scale.m00 = x;
        scale.m11 = y;
        scale.m22 = z;
        Matrix4f.mul(current, scale, current);
    }
    
    /**
     * 绕 X 轴旋转 (弧度)
     */
    public void rotateX(float radians) {
        if (radians == 0) return;
        Matrix4f current = stack.peek();
        Matrix4f rotation = new Matrix4f();
        rotation.setIdentity();
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        rotation.m11 = cos;
        rotation.m12 = sin;
        rotation.m21 = -sin;
        rotation.m22 = cos;
        Matrix4f.mul(current, rotation, current);
    }
    
    /**
     * 绕 Y 轴旋转 (弧度)
     */
    public void rotateY(float radians) {
        if (radians == 0) return;
        Matrix4f current = stack.peek();
        Matrix4f rotation = new Matrix4f();
        rotation.setIdentity();
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        rotation.m00 = cos;
        rotation.m02 = -sin;
        rotation.m20 = sin;
        rotation.m22 = cos;
        Matrix4f.mul(current, rotation, current);
    }
    
    /**
     * 绕 Z 轴旋转 (弧度)
     */
    public void rotateZ(float radians) {
        if (radians == 0) return;
        Matrix4f current = stack.peek();
        Matrix4f rotation = new Matrix4f();
        rotation.setIdentity();
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        rotation.m00 = cos;
        rotation.m01 = sin;
        rotation.m10 = -sin;
        rotation.m11 = cos;
        Matrix4f.mul(current, rotation, current);
    }
    
    /**
     * 变换顶点位置
     */
    public Vector3f transformPosition(float x, float y, float z) {
        Matrix4f mat = stack.peek();
        Vector4f vec = new Vector4f(x, y, z, 1.0f);
        Matrix4f.transform(mat, vec, vec);
        return new Vector3f(vec.x, vec.y, vec.z);
    }
    
    /**
     * 变换法线向量（使用 3x3 部分，忽略平移）
     */
    public Vector3f transformNormal(float nx, float ny, float nz) {
        Matrix4f mat = stack.peek();
        // 法线变换使用矩阵的 3x3 部分
        float rx = mat.m00 * nx + mat.m10 * ny + mat.m20 * nz;
        float ry = mat.m01 * nx + mat.m11 * ny + mat.m21 * nz;
        float rz = mat.m02 * nx + mat.m12 * ny + mat.m22 * nz;
        // 归一化
        float len = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);
        if (len > 0.00001f) {
            return new Vector3f(rx / len, ry / len, rz / len);
        }
        return new Vector3f(nx, ny, nz);
    }
}
