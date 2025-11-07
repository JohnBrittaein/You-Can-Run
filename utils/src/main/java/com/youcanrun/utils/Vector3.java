package com.youcanrun.utils;

/**
 * Custom Vector3 implementation which allows the creation of 3D vectors
 * and performing mathematical operations to them.
 * This is a custom version of the deprecated Vector3 class from googles ARCore
 *
 * @author John Brittain
 * @date 2025-11-05
 */
public class Vector3 {
    public float x, y, z;

    public Vector3(float x, float y, float z) {
        this.x = x; this.y = y; this.z = z;
    }

    public Vector3 add(Vector3 v) { return new Vector3(x + v.x, y + v.y, z + v.z); }
    public Vector3 subtract(Vector3 v) { return new Vector3(x - v.x, y - v.y, z - v.z); }
    public Vector3 scale(float s) { return new Vector3(x * s, y * s, z * s); }
    public float length() { return (float)Math.sqrt(x*x + y*y + z*z); }
    public Vector3 normalize() {
        float len = length();
        return len > 0 ? scale(1f / len) : new Vector3(0, 0, 0);
    }
    public Vector3 lerp(Vector3 target, float t) {
        return new Vector3(
                x + (target.x - x) * t,
                y + (target.y - y) * t,
                z + (target.z - z) * t
        );
    }
}
