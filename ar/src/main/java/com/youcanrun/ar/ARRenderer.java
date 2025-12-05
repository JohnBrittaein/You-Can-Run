package com.youcanrun.ar;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * ARRenderer - Renders ARCore camera feed using BackgroundRenderer.
 */
public class ARRenderer implements GLSurfaceView.Renderer, SensorEventListener {
    private static final String TAG = "ARRenderer";
    private final CameraFragment fragment;
    private Session arSession;
    private BackgroundRenderer backgroundRenderer;
    private MonsterRenderer monsterRenderer;
    private SurfaceTexture surfaceTexture;
    private int frameCount = 0;

    // Device orientation tracking (yaw, pitch, roll)
    private final SensorManager sensorManager;
    private final Sensor rotationSensor;
    private final float[] deviceOrientation = new float[3]; // [yaw, pitch, roll]
    private final float[] rotMatrix = new float[9];

    public ARRenderer(CameraFragment fragment) {
        this.fragment = fragment;

        // Initialize sensor for device orientation tracking
        sensorManager = (SensorManager) fragment.requireContext().getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
            Log.i(TAG, "ARRenderer created with orientation tracking");
        } else {
            Log.w(TAG, "ARRenderer created but rotation sensor not available");
        }
    }

    public void setSession(Session session) {
        this.arSession = session;
        // Try to set up camera texture if renderer is already initialized
        setupCameraTexture();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        // Create background renderer
        backgroundRenderer = new BackgroundRenderer();
        backgroundRenderer.createOnGlThread();

        // Create monster renderer
        monsterRenderer = new MonsterRenderer();
        monsterRenderer.createOnGlThread();

        // Set up texture for ARCore
        setupCameraTexture();

        Log.i(TAG, "GL surface created with monster renderer");
    }

    private void setupCameraTexture() {
        if (arSession != null && backgroundRenderer != null && surfaceTexture == null) {
            try {
                int textureId = backgroundRenderer.getTextureId();
                arSession.setCameraTextureName(textureId);
                surfaceTexture = new SurfaceTexture(textureId);
                surfaceTexture.setDefaultBufferSize(1920, 1080);
                Log.d(TAG, "Camera texture configured: " + textureId);
            } catch (Exception e) {
                Log.e(TAG, "Failed to set camera texture: " + e.getMessage());
            }
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        if (arSession != null && fragment != null) {
            try {
                // Get actual display rotation from WindowManager
                WindowManager windowManager = (WindowManager) fragment.requireContext()
                        .getSystemService(Context.WINDOW_SERVICE);
                Display display = windowManager.getDefaultDisplay();
                int rotation = display.getRotation();

                arSession.setDisplayGeometry(rotation, width, height);
                Log.i(TAG, "Display configured: " + width + "x" + height + ", rotation: " + rotation);
            } catch (Exception e) {
                Log.e(TAG, "Display configuration error: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        frameCount++;
        if (frameCount % 60 == 1) { // Log every 60 frames (once per second at 60fps)
            Log.d(TAG, "onDrawFrame called (frame " + frameCount + ")");
        }

        if (arSession == null) {
            if (frameCount % 60 == 1) {
                Log.w(TAG, "arSession is null");
            }
            return;
        }

        // Try to setup camera texture if not done yet
        if (surfaceTexture == null) {
            setupCameraTexture();
            if (surfaceTexture == null) {
                if (frameCount % 60 == 1) {
                    Log.w(TAG, "surfaceTexture is still null");
                }
                return;
            }
        }

        try {
            // Update surface texture with latest camera frame
            surfaceTexture.updateTexImage();

            // Get ARCore frame
            Frame frame = arSession.update();
            Camera camera = frame.getCamera();

            if (frameCount % 60 == 1) {
                Log.d(TAG, "Rendering frame, tracking state: " + camera.getTrackingState());
            }

            // Render camera background
            if (backgroundRenderer != null) {
                backgroundRenderer.draw(frame);
            }

            // Update and render monster
            if (monsterRenderer != null) {
                monsterRenderer.update(0.016f); // ~60fps
                monsterRenderer.draw(camera, deviceOrientation);
            }

        } catch (Exception e) {
            Log.e(TAG, "Render error: " + e.getMessage(), e);
        }
    }

    /**
     * Set the camera filter mode
     */
    public void setFilter(CameraFilter filter) {
        if (backgroundRenderer != null) {
            backgroundRenderer.setFilter(filter);
        }
    }

    /**
     * Set monster position in world coordinates
     */
    public void setMonsterPosition(float x, float y, float z) {
        if (monsterRenderer != null) {
            monsterRenderer.setPosition(x, y, z);
        }
    }

    // SensorEventListener implementation for device orientation tracking

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            // Convert rotation vector to rotation matrix
            SensorManager.getRotationMatrixFromVector(rotMatrix, event.values);

            // Remap coordinate system (same as MotionTracker)
            float[] remapped = new float[9];
            SensorManager.remapCoordinateSystem(rotMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remapped);

            // Get orientation angles: [yaw, pitch, roll]
            SensorManager.getOrientation(remapped, deviceOrientation);

            // deviceOrientation now contains:
            // [0] = yaw (azimuth) - rotation around Y axis
            // [1] = pitch - rotation around X axis
            // [2] = roll - rotation around Z axis
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used, but required by interface
    }

    /**
     * Cleanup method - call when renderer is destroyed
     */
    public void cleanup() {
        if (sensorManager != null && rotationSensor != null) {
            sensorManager.unregisterListener(this);
            Log.i(TAG, "Sensor listener unregistered");
        }
    }
}
