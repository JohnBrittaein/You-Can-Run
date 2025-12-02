package com.youcanrun.ar;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.google.ar.core.Camera;
import com.google.ar.core.Pose;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * MonsterRenderer - Renders a simple 3D monster (cube) in AR space
 */
public class MonsterRenderer {
    private static final String TAG = "MonsterRenderer";

    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;
    private int shaderProgram;
    private int positionHandle;
    private int colorHandle;
    private int mvpMatrixHandle;

    private float[] modelMatrix = new float[16];
    private float[] mvpMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];

    // Monster position in game world space (relative to player at origin)
    private float monsterWorldX = 0.0f;
    private float monsterWorldY = 0.0f;
    private float monsterWorldZ = -2.0f; // 2 meters behind player initially

    // Monster color (red/orange for heat signature)
    private float[] monsterColor = new float[]{1.0f, 0.3f, 0.0f, 0.8f}; // Orange with some transparency
    private float pulseTime = 0.0f;
    private int frameCount = 0;

    // Cube vertices (position only) - 10x10x10 meter cube (large enough to see at distance)
    private static final float[] CUBE_VERTICES = {
            // Front face
            -5.0f, -5.0f,  5.0f,
             5.0f, -5.0f,  5.0f,
             5.0f,  5.0f,  5.0f,
            -5.0f,  5.0f,  5.0f,
            // Back face
            -5.0f, -5.0f, -5.0f,
             5.0f, -5.0f, -5.0f,
             5.0f,  5.0f, -5.0f,
            -5.0f,  5.0f, -5.0f
    };

    // Cube indices
    private static final short[] CUBE_INDICES = {
            // Front
            0, 1, 2,  0, 2, 3,
            // Back
            4, 6, 5,  4, 7, 6,
            // Left
            4, 0, 3,  4, 3, 7,
            // Right
            1, 5, 6,  1, 6, 2,
            // Top
            3, 2, 6,  3, 6, 7,
            // Bottom
            4, 5, 1,  4, 1, 0
    };

    public void createOnGlThread() {
        // Create vertex buffer
        ByteBuffer vbb = ByteBuffer.allocateDirect(CUBE_VERTICES.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(CUBE_VERTICES);
        vertexBuffer.position(0);

        // Create index buffer
        ByteBuffer ibb = ByteBuffer.allocateDirect(CUBE_INDICES.length * 2);
        ibb.order(ByteOrder.nativeOrder());
        indexBuffer = ibb.asShortBuffer();
        indexBuffer.put(CUBE_INDICES);
        indexBuffer.position(0);

        // Create shader program
        String vertexShader = "#version 100\n" +
                "attribute vec4 a_Position;\n" +
                "uniform mat4 u_MVPMatrix;\n" +
                "void main() {\n" +
                "  gl_Position = u_MVPMatrix * a_Position;\n" +
                "}\n";

        String fragmentShader = "#version 100\n" +
                "precision mediump float;\n" +
                "uniform vec4 u_Color;\n" +
                "void main() {\n" +
                "  gl_FragColor = u_Color;\n" +
                "}\n";

        int vsh = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        int fsh = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vsh);
        GLES20.glAttachShader(shaderProgram, fsh);
        GLES20.glLinkProgram(shaderProgram);

        // Check linking
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            Log.e(TAG, "Shader link error: " + GLES20.glGetProgramInfoLog(shaderProgram));
            return;
        }

        // Get handles
        positionHandle = GLES20.glGetAttribLocation(shaderProgram, "a_Position");
        colorHandle = GLES20.glGetUniformLocation(shaderProgram, "u_Color");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "u_MVPMatrix");

        GLES20.glDeleteShader(vsh);
        GLES20.glDeleteShader(fsh);

        Log.i(TAG, "MonsterRenderer initialized");
    }

    private int compileShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            Log.e(TAG, "Shader compile error: " + GLES20.glGetShaderInfoLog(shader));
            return 0;
        }
        return shader;
    }

    public void draw(Camera camera, float[] deviceOrientation) {
        if (shaderProgram == 0) return;

        // Get camera matrices
        camera.getViewMatrix(viewMatrix, 0);
        camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 200.0f); // Increased far plane to 200m

        // Transform monster from world-space to camera-space using device orientation
        // This accounts for full 3D rotation (yaw, pitch, roll) from game world

        if (deviceOrientation != null && deviceOrientation.length >= 3) {
            float yaw = deviceOrientation[0];   // Rotation around Y-axis (compass heading)
            float pitch = deviceOrientation[1]; // Rotation around X-axis (tilt up/down)
            float roll = deviceOrientation[2];  // Rotation around Z-axis (tilt left/right)

            // Build rotation matrix from Euler angles (ZXY order matches Android orientation)
            float[] rotMatrix = new float[16];
            Matrix.setIdentityM(rotMatrix, 0);

            // Apply rotations in order: yaw (Y), pitch (X), roll (Z)
            // This transforms from world-space (compass-aligned) to device-space
            Matrix.rotateM(rotMatrix, 0, (float)Math.toDegrees(-yaw), 0, 1, 0);    // Yaw around Y
            Matrix.rotateM(rotMatrix, 0, (float)Math.toDegrees(-pitch), 1, 0, 0);  // Pitch around X
            Matrix.rotateM(rotMatrix, 0, (float)Math.toDegrees(-roll), 0, 0, 1);   // Roll around Z

            // Transform monster world position to camera-space
            float[] worldPos = new float[]{monsterWorldX, monsterWorldY, monsterWorldZ, 1.0f};
            float[] cameraPos = new float[4];
            Matrix.multiplyMV(cameraPos, 0, rotMatrix, 0, worldPos, 0);

            // Extract camera-space position
            float monsterCameraX = cameraPos[0];
            float monsterCameraY = cameraPos[1];
            float monsterCameraZ = -cameraPos[2]; // ARCore uses -Z as forward

            // Set up model matrix (position the monster in camera-space)
            Matrix.setIdentityM(modelMatrix, 0);
            Matrix.translateM(modelMatrix, 0, monsterCameraX, monsterCameraY, monsterCameraZ);

            // Log what we're actually drawing (every 60 frames)
            if (frameCount % 60 == 0) {
                Log.d(TAG, String.format("Monster - World: (%.1f, %.1f, %.1f) Orientation: (Y:%.1f° P:%.1f° R:%.1f°) → Camera: (%.1f, %.1f, %.1f)",
                    monsterWorldX, monsterWorldY, monsterWorldZ,
                    Math.toDegrees(yaw), Math.toDegrees(pitch), Math.toDegrees(roll),
                    monsterCameraX, monsterCameraY, monsterCameraZ));
            }
        } else {
            // Fallback: no rotation if orientation not available
            Matrix.setIdentityM(modelMatrix, 0);
            Matrix.translateM(modelMatrix, 0, monsterWorldX, monsterWorldY, -monsterWorldZ);

            if (frameCount % 60 == 0) {
                Log.w(TAG, "Device orientation not available, using fallback positioning");
            }
        }
        frameCount++;

        // Add subtle rotation for visual interest
        Matrix.rotateM(modelMatrix, 0, pulseTime * 20.0f, 0, 1, 0);

        // Calculate MVP matrix
        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0);

        // Enable depth test and blending for 3D rendering
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // Use shader program
        GLES20.glUseProgram(shaderProgram);

        // Add pulsing effect to color
        float pulse = 0.5f + 0.5f * (float)Math.sin(pulseTime * 3.0);
        float[] animatedColor = new float[]{
            monsterColor[0] * (0.7f + 0.3f * pulse),
            monsterColor[1] * (0.7f + 0.3f * pulse),
            monsterColor[2] * (0.7f + 0.3f * pulse),
            monsterColor[3]
        };

        // Set uniforms
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20.glUniform4fv(colorHandle, 1, animatedColor, 0);

        // Set vertex data
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer);

        // Draw the cube
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, CUBE_INDICES.length,
                              GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        // Cleanup
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    /**
     * Set monster position in world coordinates (relative to player at origin)
     */
    public void setPosition(float x, float y, float z) {
        Log.d(TAG, String.format("setPosition called (world-space): (%.1f, %.1f, %.1f)", x, y, z));
        this.monsterWorldX = x;
        this.monsterWorldY = y;
        this.monsterWorldZ = z;
    }

    /**
     * Set monster color (RGBA)
     */
    public void setColor(float r, float g, float b, float a) {
        this.monsterColor[0] = r;
        this.monsterColor[1] = g;
        this.monsterColor[2] = b;
        this.monsterColor[3] = a;
    }

    /**
     * Update monster position based on game logic
     */
    public void update(float deltaTime) {
        pulseTime += deltaTime;
        // Simple floating animation (disabled when using real position)
        // monsterY offset can be added here if needed
    }

    /**
     * Get current distance from camera/player
     */
    public float getDistance() {
        return (float)Math.sqrt(monsterWorldX * monsterWorldX + monsterWorldY * monsterWorldY + monsterWorldZ * monsterWorldZ);
    }
}
