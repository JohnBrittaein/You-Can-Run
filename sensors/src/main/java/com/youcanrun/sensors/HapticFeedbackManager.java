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
 * - Far away: Weak, infrequent pulses
 * - Getting closer: Stronger, more frequent vibration
 * - Very close (≤5m): Intense vibration + flashing camera light
 *
 * @date 12-04-2025
 */
public class HapticFeedbackManager {
    private static final String TAG = "HapticFeedbackManager";

    private Context context;
    private Vibrator vibrator;
    private CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashOn = false;
    private static final float FLASH_DISTANCE = 5.0f; // Flash activates at 5m or less
    private static final float MAX_DISTANCE = 200.0f; // Maximum tracking distance
    private static final float MIN_DISTANCE = 0.5f; // Minimum distance before max intensity

    // Vibration timing
    private long lastVibrationTime = 0;
    private long lastFlashToggleTime = 0;
    private boolean flashState = false;

    public HapticFeedbackManager(Context context) {
        this.context = context;
        initVibrator();
        initCameraFlash();
        Log.d(TAG, "HapticFeedbackManager initialized");
    }

    /**
     * Initialize vibrator service
     */
    private void initVibrator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibratorManager != null) {
                vibrator = vibratorManager.getDefaultVibrator();
            }
        } else {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }

        if (vibrator != null && vibrator.hasVibrator()) {
            Log.d(TAG, "Vibrator initialized successfully");
        } else {
            Log.w(TAG, "No vibrator available on this device");
        }
    }

    /**
     * Initialize camera flash
     */
    private void initCameraFlash() {
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (cameraManager != null) {
            try {
                cameraId = cameraManager.getCameraIdList()[0];
                Log.d(TAG, "Camera flash initialized with camera ID: " + cameraId);
            } catch (CameraAccessException e) {
                Log.e(TAG, "Failed to access camera for flash", e);
            }
        }
    }

    /**
     * Update haptic feedback based on monster distance and signal strength
     * Call this every frame from the game loop
     *
     * @param distance Distance to monster in meters
     * @param signalStrength Signal strength from slider (0.0 to 1.0)
     */
    public void updateHaptics(float distance, float signalStrength) {
        if (vibrator == null || !vibrator.hasVibrator()) return;

        long currentTime = System.currentTimeMillis();

        // Calculate intensity and frequency based on distance
        float normalizedDistance = Math.max(0f, Math.min(1f, (distance - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE)));
        float baseIntensity = 1.0f - normalizedDistance; // 1.0 = close, 0.0 = far

        // Apply signal strength multiplier to intensity
        float intensity = baseIntensity * signalStrength;

        // Calculate vibration interval (shorter = more frequent)
        // Signal strength affects frequency too
        long baseInterval = (long) (2000 - (1800 * baseIntensity)); // 2000ms (far) to 200ms (close)
        long vibrationInterval = (long) (baseInterval * (1.5f - (signalStrength * 0.5f))); // Higher signal = more frequent

        // Check if it's time to vibrate
        if (currentTime - lastVibrationTime >= vibrationInterval) {
            triggerVibration(intensity, distance, signalStrength);
            lastVibrationTime = currentTime;
        }

        // Handle flash for very close proximity (≤5m)
        // Flash only activates if signal strength is high enough
        if (distance <= FLASH_DISTANCE && signalStrength > 0.3f) {
            // Flash rapidly when monster is very close
            long flashInterval = 200; // Toggle every 200ms
            if (currentTime - lastFlashToggleTime >= flashInterval) {
                toggleFlash();
                lastFlashToggleTime = currentTime;
            }
        } else {
            // Turn off flash if monster moves away or signal is too weak
            if (isFlashOn) {
                setFlash(false);
            }
        }
    }

    /**
     * Trigger a vibration pulse based on intensity and signal strength
     */
    private void triggerVibration(float intensity, float distance, float signalStrength) {
        if (vibrator == null) return;

        // Calculate duration and amplitude based on intensity (which already includes signal strength)
        long duration;
        int amplitude;

        if (distance <= FLASH_DISTANCE && signalStrength > 0.5f) {
            // Very close with strong signal: continuous strong vibration
            duration = 2000;
            amplitude = (int) (255 * signalStrength); // Max scaled by signal
        } else {
            // Scale duration: 50ms (far) to 200ms (close)
            duration = (long) (50 + (150 * intensity));
            // Scale amplitude: 30 (far/weak signal) to 255 (close/strong signal)
            amplitude = (int) (30 + (225 * intensity));
        }

        // Ensure minimum amplitude threshold
        amplitude = Math.max(30, Math.min(255, amplitude));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect effect = VibrationEffect.createOneShot(duration, amplitude);
            vibrator.vibrate(effect);
        } else {
            vibrator.vibrate(duration);
        }

        // Log.d(TAG, String.format("Vibration: distance=%.2fm, intensity=%.2f, signal=%.2f, duration=%dms, amplitude=%d", distance, intensity, signalStrength, duration, amplitude));
    }

    /**
     * Toggle camera flash on/off
     */
    private void toggleFlash() {
        setFlash(!flashState);
    }

    /**
     * Set camera flash state
     */
    private void setFlash(boolean on) {
        if (cameraManager == null || cameraId == null) return;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, on);
                flashState = on;
                isFlashOn = on;
                // Log.d(TAG, "Flash " + (on ? "ON" : "OFF"));
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to control camera flash", e);
        }
    }

    /**
     * Stop all haptic feedback
     */
    public void stop() {
        if (vibrator != null) {
            vibrator.cancel();
        }
        if (isFlashOn) {
            setFlash(false);
        }
        Log.d(TAG, "Haptic feedback stopped");
    }

    /**
     * Release resources
     */
    public void release() {
        stop();
        Log.d(TAG, "HapticFeedbackManager released");
    }
}
