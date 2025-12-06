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

    private final SensorManager sensorManager;
    private final Sensor rotationSensor;
    private final float[] deviceOrientation = new float[3];
    private final float[] rotMatrix = new float[9];

    public ARRenderer(CameraFragment fragment) {
        this.fragment = fragment;
        sensorManager = (SensorManager) fragment.requireContext().getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void setSession(Session session) {
        this.arSession = session;
        setupCameraTexture();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        backgroundRenderer = new BackgroundRenderer();
        backgroundRenderer.createOnGlThread();

        monsterRenderer = new MonsterRenderer();
        monsterRenderer.createOnGlThread();

        setupCameraTexture();
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
                WindowManager windowManager = (WindowManager) fragment.requireContext()
                        .getSystemService(Context.WINDOW_SERVICE);
                Display display = windowManager.getDefaultDisplay();
                int rotation = display.getRotation();
                arSession.setDisplayGeometry(rotation, width, height);
            } catch (Exception e) {
                Log.e(TAG, "Display configuration error", e);
            }
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (arSession == null || surfaceTexture == null) {
            if (surfaceTexture == null) {
                setupCameraTexture();
            }
            return;
        }

        try {
            surfaceTexture.updateTexImage();
            Frame frame = arSession.update();
            Camera camera = frame.getCamera();

            if (backgroundRenderer != null) {
                backgroundRenderer.draw(frame);
            }

            if (monsterRenderer != null) {
                monsterRenderer.update(0.016f);
                monsterRenderer.draw(camera, deviceOrientation);
            }
        } catch (Exception e) {
            Log.e(TAG, "Render error", e);
        }
    }

    public void setFilter(CameraFilter filter) {
        if (backgroundRenderer != null) {
            backgroundRenderer.setFilter(filter);
        }
        if (monsterRenderer != null) {
            monsterRenderer.setFilter(filter);
        }
    }

    public void setMonsterPosition(float x, float y, float z) {
        if (monsterRenderer != null) {
            monsterRenderer.setPosition(x, y, z);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotMatrix, event.values);
            float[] remapped = new float[9];
            SensorManager.remapCoordinateSystem(rotMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remapped);
            SensorManager.getOrientation(remapped, deviceOrientation);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void cleanup() {
        if (sensorManager != null && rotationSensor != null) {
            sensorManager.unregisterListener(this);
        }
    }
}
