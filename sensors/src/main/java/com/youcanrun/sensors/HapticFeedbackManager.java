package com.youcanrun.sensors;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;

/**
 * HapticFeedbackManager handles vibration and camera flash based on monster proximity
 */
public class HapticFeedbackManager {
    private static final String TAG = "HapticFeedbackManager";
    private final Context context;
    private Vibrator vibrator;
    private CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashOn = false;
    private static final float FLASH_DISTANCE = 5.0f; // Flash activates at 5m or less
    private static final float MAX_DISTANCE = 200.0f;
    private static final float MIN_DISTANCE = 0.5f;

    private long lastVibrationTime = 0;
    private long lastFlashToggleTime = 0;
    private boolean flashState = false;

    public HapticFeedbackManager(Context context) {
        this.context = context;
        initVibrator();
        initCameraFlash();
    }

    private void initVibrator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            if (vibratorManager != null) {
                vibrator = vibratorManager.getDefaultVibrator();
            }
        } else {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }

    }

    private void initCameraFlash() {
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (cameraManager != null) {
            try {
                cameraId = cameraManager.getCameraIdList()[0];
            } catch (CameraAccessException e) {
                Log.e(TAG, "Failed to access camera for flash", e);
            }
        }
    }

    public void updateHaptics(float distance, float signalStrength) {
        if (vibrator == null || !vibrator.hasVibrator()) return;

        long currentTime = System.currentTimeMillis();

        float normalizedDistance = Math.max(0f, Math.min(1f, (distance - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE)));
        float baseIntensity = 1.0f - normalizedDistance;
        float intensity = baseIntensity * signalStrength;

        long baseInterval = (long) (2000 - (1800 * baseIntensity));
        long vibrationInterval = (long) (baseInterval * (1.5f - (signalStrength * 0.5f)));

        if (currentTime - lastVibrationTime >= vibrationInterval) {
            triggerVibration(intensity, distance, signalStrength);
            lastVibrationTime = currentTime;
        }

        if (distance <= FLASH_DISTANCE && signalStrength > 0.3f) {
            long flashInterval = 200;
            if (currentTime - lastFlashToggleTime >= flashInterval) {
                toggleFlash();
                lastFlashToggleTime = currentTime;
            }
        } else {
            if (isFlashOn) {
                setFlash(false);
            }
        }
    }

    private void triggerVibration(float intensity, float distance, float signalStrength) {
        if (vibrator == null) return;

        long duration;
        int amplitude;

        if (distance <= FLASH_DISTANCE && signalStrength > 0.5f) {
            duration = 2000;
            amplitude = (int) (255 * signalStrength);
        } else {
            duration = (long) (50 + (150 * intensity));
            amplitude = (int) (30 + (225 * intensity));
        }

        amplitude = Math.max(30, Math.min(255, amplitude));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect effect = VibrationEffect.createOneShot(duration, amplitude);
            vibrator.vibrate(effect);
        } else {
            vibrator.vibrate(duration);
        }
    }

    private void toggleFlash() {
        setFlash(!flashState);
    }

    private void setFlash(boolean on) {
        if (cameraManager == null || cameraId == null) return;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, on);
                flashState = on;
                isFlashOn = on;
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to control camera flash", e);
        }
    }

    public void stop() {
        if (vibrator != null) {
            vibrator.cancel();
        }
        if (isFlashOn) {
            setFlash(false);
        }
    }

    public void release() {
        stop();
    }
}
