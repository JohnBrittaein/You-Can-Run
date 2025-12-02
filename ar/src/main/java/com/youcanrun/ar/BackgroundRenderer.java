package com.youcanrun.ar;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.google.ar.core.Frame;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * BackgroundRenderer - Renders the camera feed as the background using ARCore frame data.
 */
public class BackgroundRenderer {
    private static final String TAG = "BackgroundRenderer";

    private int textureId = -1;
    private int currentShaderProgram = -1;

    // Shader programs for different filters
    private int normalShaderProgram = -1;
    private int predatorShaderProgram = -1;
    private int nightVisionShaderProgram = -1;
    private int flirShaderProgram = -1;
    private int edgeDetectShaderProgram = -1;

    private int positionHandle = -1;
    private int texCoordHandle = -1;
    private int textureUniform = -1;
    private int timeUniform = -1;

    private FloatBuffer vertexBuffer;
    private FloatBuffer texCoordBuffer;

    private CameraFilter currentFilter = CameraFilter.NORMAL;
    private float time = 0.0f;

    public void createOnGlThread() {
        // Create texture for camera
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureId = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // Create all shader programs
        normalShaderProgram = createNormalShader();
        predatorShaderProgram = createPredatorShader();
        nightVisionShaderProgram = createNightVisionShader();
        flirShaderProgram = createFLIRShader();
        edgeDetectShaderProgram = createEdgeDetectShader();

        currentShaderProgram = normalShaderProgram;
        Log.i(TAG, "All shaders compiled successfully");

        // Create fullscreen quad (NDC coordinates)
        float[] vertices = {
            -1.0f, -1.0f, 0.0f,  // bottom-left
             1.0f, -1.0f, 0.0f,  // bottom-right
            -1.0f,  1.0f, 0.0f,  // top-left
             1.0f,  1.0f, 0.0f   // top-right
        };

        // Initial texture coordinates (will be transformed by ARCore)
        float[] texCoords = {
            0.0f, 0.0f,  // bottom-left
            1.0f, 0.0f,  // bottom-right
            0.0f, 1.0f,  // top-left
            1.0f, 1.0f   // top-right
        };

        vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        texCoordBuffer.put(texCoords);
        texCoordBuffer.position(0);

        Log.i(TAG, "BackgroundRenderer initialized, textureId=" + textureId);
    }

    private int compileShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        if (shader == 0) {
            Log.e(TAG, "Failed to create shader of type: " + type);
            return 0;
        }

        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            String log = GLES20.glGetShaderInfoLog(shader);
            Log.e(TAG, "Shader compile error (" + (type == GLES20.GL_VERTEX_SHADER ? "vertex" : "fragment") + "): " + log);
            GLES20.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }

    private String getVertexShader() {
        return "#version 100\n" +
                "attribute vec4 a_Position;\n" +
                "attribute vec2 a_TexCoord;\n" +
                "varying vec2 v_TexCoord;\n" +
                "void main() {\n" +
                "  gl_Position = a_Position;\n" +
                "  v_TexCoord = a_TexCoord;\n" +
                "}\n";
    }

    private int createProgramFromShaders(String fragmentShader, String name) {
        int vsh = compileShader(GLES20.GL_VERTEX_SHADER, getVertexShader());
        int fsh = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

        if (vsh == 0 || fsh == 0) {
            Log.e(TAG, "Failed to compile " + name + " shaders!");
            return -1;
        }

        int program = GLES20.glCreateProgram();
        if (program == 0) {
            Log.e(TAG, "Failed to create " + name + " program!");
            return -1;
        }

        GLES20.glAttachShader(program, vsh);
        GLES20.glAttachShader(program, fsh);
        GLES20.glLinkProgram(program);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            Log.e(TAG, name + " link error: " + GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            return -1;
        }

        GLES20.glDeleteShader(vsh);
        GLES20.glDeleteShader(fsh);
        Log.d(TAG, name + " shader created successfully");
        return program;
    }

    private int createNormalShader() {
        String fragmentShader = "#version 100\n" +
                "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "uniform samplerExternalOES u_Texture;\n" +
                "varying vec2 v_TexCoord;\n" +
                "void main() {\n" +
                "  gl_FragColor = texture2D(u_Texture, v_TexCoord);\n" +
                "}\n";
        return createProgramFromShaders(fragmentShader, "Normal");
    }

    private int createPredatorShader() {
        String fragmentShader = "#version 100\n" +
                "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "uniform samplerExternalOES u_Texture;\n" +
                "uniform float u_Time;\n" +
                "varying vec2 v_TexCoord;\n" +
                "void main() {\n" +
                "  vec4 color = texture2D(u_Texture, v_TexCoord);\n" +
                "  // Calculate luminance for heat map\n" +
                "  float heat = (color.r + color.g + color.b) / 3.0;\n" +
                "  // Create thermal color gradient\n" +
                "  vec3 cold = vec3(0.0, 0.0, 0.3);  // dark blue\n" +
                "  vec3 warm = vec3(0.8, 0.3, 0.0);  // orange\n" +
                "  vec3 hot = vec3(1.0, 1.0, 0.0);   // yellow\n" +
                "  vec3 thermal;\n" +
                "  if (heat < 0.5) {\n" +
                "    thermal = mix(cold, warm, heat * 2.0);\n" +
                "  } else {\n" +
                "    thermal = mix(warm, hot, (heat - 0.5) * 2.0);\n" +
                "  }\n" +
                "  // Add scan lines\n" +
                "  float scanLine = sin(v_TexCoord.y * 200.0 + u_Time * 5.0) * 0.05;\n" +
                "  gl_FragColor = vec4(thermal + scanLine, 1.0);\n" +
                "}\n";
        return createProgramFromShaders(fragmentShader, "Predator");
    }

    private int createNightVisionShader() {
        String fragmentShader = "#version 100\n" +
                "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "uniform samplerExternalOES u_Texture;\n" +
                "uniform float u_Time;\n" +
                "varying vec2 v_TexCoord;\n" +
                "void main() {\n" +
                "  vec4 color = texture2D(u_Texture, v_TexCoord);\n" +
                "  // Convert to grayscale and boost brightness\n" +
                "  float luminance = (color.r + color.g + color.b) / 3.0;\n" +
                "  luminance = pow(luminance, 0.7) * 1.5; // Brighten\n" +
                "  // Apply green tint\n" +
                "  vec3 nightVision = vec3(luminance * 0.2, luminance, luminance * 0.2);\n" +
                "  // Add noise for realism\n" +
                "  float noise = fract(sin(dot(v_TexCoord + u_Time * 0.1, vec2(12.9898, 78.233))) * 43758.5453) * 0.1;\n" +
                "  // Add vignette\n" +
                "  float dist = distance(v_TexCoord, vec2(0.5, 0.5));\n" +
                "  float vignette = 1.0 - smoothstep(0.3, 0.8, dist);\n" +
                "  gl_FragColor = vec4(nightVision * vignette + noise, 1.0);\n" +
                "}\n";
        return createProgramFromShaders(fragmentShader, "NightVision");
    }

    private int createFLIRShader() {
        String fragmentShader = "#version 100\n" +
                "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "uniform samplerExternalOES u_Texture;\n" +
                "varying vec2 v_TexCoord;\n" +
                "void main() {\n" +
                "  vec4 color = texture2D(u_Texture, v_TexCoord);\n" +
                "  float temp = (color.r + color.g + color.b) / 3.0;\n" +
                "  // FLIR white-hot thermal\n" +
                "  vec3 flir;\n" +
                "  if (temp < 0.33) {\n" +
                "    flir = vec3(0.0, 0.0, temp * 3.0); // Black to blue\n" +
                "  } else if (temp < 0.66) {\n" +
                "    flir = vec3((temp - 0.33) * 3.0, 0.0, 1.0); // Blue to magenta\n" +
                "  } else {\n" +
                "    float t = (temp - 0.66) * 3.0;\n" +
                "    flir = vec3(1.0, t, 1.0 - t); // Magenta to white\n" +
                "  }\n" +
                "  gl_FragColor = vec4(flir, 1.0);\n" +
                "}\n";
        return createProgramFromShaders(fragmentShader, "FLIR");
    }

    private int createEdgeDetectShader() {
        String fragmentShader = "#version 100\n" +
                "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "uniform samplerExternalOES u_Texture;\n" +
                "varying vec2 v_TexCoord;\n" +
                "void main() {\n" +
                "  // Simple edge detection\n" +
                "  float offset = 0.003;\n" +
                "  vec4 center = texture2D(u_Texture, v_TexCoord);\n" +
                "  vec4 left = texture2D(u_Texture, v_TexCoord + vec2(-offset, 0.0));\n" +
                "  vec4 right = texture2D(u_Texture, v_TexCoord + vec2(offset, 0.0));\n" +
                "  vec4 up = texture2D(u_Texture, v_TexCoord + vec2(0.0, offset));\n" +
                "  vec4 down = texture2D(u_Texture, v_TexCoord + vec2(0.0, -offset));\n" +
                "  vec4 edges = abs(center - left) + abs(center - right) + abs(center - up) + abs(center - down);\n" +
                "  float edge = (edges.r + edges.g + edges.b) / 3.0;\n" +
                "  gl_FragColor = vec4(vec3(edge * 2.0), 1.0);\n" +
                "}\n";
        return createProgramFromShaders(fragmentShader, "EdgeDetect");
    }

    private int drawCount = 0;
    private FloatBuffer quadTexCoord;

    public void draw(Frame frame) {
        if (textureId < 0 || currentShaderProgram < 0) {
            Log.w(TAG, "Not ready: tex=" + textureId + " prog=" + currentShaderProgram);
            return;
        }

        drawCount++;
        time += 0.016f; // Approx 60fps

        // Create quad texture coordinate buffer if needed
        if (quadTexCoord == null) {
            float[] coords = {
                0.0f, 0.0f,  // bottom-left
                1.0f, 0.0f,  // bottom-right
                0.0f, 1.0f,  // top-left
                1.0f, 1.0f   // top-right
            };
            quadTexCoord = ByteBuffer.allocateDirect(coords.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            quadTexCoord.put(coords);
        }

        // Transform texture coordinates from screen space to texture space
        quadTexCoord.position(0);
        texCoordBuffer.position(0);
        try {
            frame.transformCoordinates2d(
                com.google.ar.core.Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
                quadTexCoord,
                com.google.ar.core.Coordinates2d.TEXTURE_NORMALIZED,
                texCoordBuffer
            );
        } catch (Exception e) {
            Log.e(TAG, "Error transforming coordinates", e);
            return;
        }
        texCoordBuffer.position(0);

        GLES20.glUseProgram(currentShaderProgram);
        checkGLError("glUseProgram");

        // Disable depth test and blending for background
        GLES20.glDepthMask(false);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_BLEND);

        // Get uniform locations for current program
        positionHandle = GLES20.glGetAttribLocation(currentShaderProgram, "a_Position");
        texCoordHandle = GLES20.glGetAttribLocation(currentShaderProgram, "a_TexCoord");
        textureUniform = GLES20.glGetUniformLocation(currentShaderProgram, "u_Texture");
        timeUniform = GLES20.glGetUniformLocation(currentShaderProgram, "u_Time");

        // Set texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(textureUniform, 0);

        // Set time uniform if available (for animated effects)
        if (timeUniform >= 0) {
            GLES20.glUniform1f(timeUniform, time);
        }

        checkGLError("Uniforms setup");

        // Set vertex attributes
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer);

        GLES20.glEnableVertexAttribArray(texCoordHandle);
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, texCoordBuffer);

        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGLError("glDrawArrays");

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);

        // Re-enable depth test for other rendering
        GLES20.glDepthMask(true);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        if (drawCount % 120 == 1) {
            Log.d(TAG, "Background drawn (filter: " + currentFilter + ")");
        }
    }

    private void checkGLError(String operation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, operation + ": glError " + error);
        }
    }

    public int getTextureId() {
        return textureId;
    }

    /**
     * Set the camera filter mode
     */
    public void setFilter(CameraFilter filter) {
        this.currentFilter = filter;
        switch (filter) {
            case NORMAL:
                currentShaderProgram = normalShaderProgram;
                break;
            case PREDATOR:
                currentShaderProgram = predatorShaderProgram;
                break;
            case EDGE_DETECT:
                currentShaderProgram = edgeDetectShaderProgram;
                break;
        }
        Log.i(TAG, "Filter changed to: " + filter);
    }

    public CameraFilter getCurrentFilter() {
        return currentFilter;
    }
}

