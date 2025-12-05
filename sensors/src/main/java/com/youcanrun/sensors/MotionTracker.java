package com.youcanrun.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.youcanrun.utils.Vector3;

/**
 * Uses the onboard sensors to track the player's motion
 *
 * @author John Brittain
 * @date 2025-11-05
 */
public class MotionTracker implements SensorEventListener {
    private static final String TAG = "MotionTracker";

    // TODO: Tune Accelerometer and include GPS for hybrid motion tracking
    private final SensorManager mSensorManager;
    private final Sensor mAccelerometer;
    private final Sensor mRotationVector;
    private final float[] rotMatrix = new float[9];
    private float currentSpeed;
    private float currentDistance;

    /**
     * Initializes the motion tracker object
     * Contains a reference to the application context
     * And gets the default accelerometer sensor(s)
     * @param context
     */
    public MotionTracker(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        Log.d(TAG, "MotionTracker initialized");
    }

    private float vX, vY, vZ;
    private long lastTimeStamp = -1;
    private float intervalTimeAccum = 0f; // accumulates dt

    @Override
    public void onSensorChanged(SensorEvent event) {

        long timestamp = event.timestamp;

        if (lastTimeStamp < 0) {
            lastTimeStamp = timestamp;
            return;
        }

        float dt = (timestamp - lastTimeStamp) * 1.0e-9f; // ns -> s
        lastTimeStamp = timestamp;

        // --- Speed computation (from linear acceleration) ---
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            vX += event.values[0] * dt;
            vY += event.values[1] * dt;
            vZ += event.values[2] * dt;

            intervalTimeAccum += dt;

            float rawSpeed = (float) Math.sqrt(vX*vX + vY*vY + vZ*vZ);

            float rawDistanceAtMoment = (float) Math.sqrt((vX*dt)*(vX*dt)+(vY*dt)*(vY*dt)+(vZ*dt)*(vZ*dt));

            float distanceTolarence = 0.001f;
            if(rawDistanceAtMoment > distanceTolarence && motionListener != null) {
                currentDistance = currentDistance + rawDistanceAtMoment;
                // Notify listener
                motionListener.onPlayerDistanceUpdated(currentDistance);
            }
            // increasing this will improve consistency but decrease responsiveness
            float intervalSeconds = 0.2f;
            if (intervalTimeAccum >= intervalSeconds) {
                // Smooth speed
                // Smoothing factor, lowering this will make the speed more responsive
                float alpha = 0.8f;
                currentSpeed = alpha * currentSpeed + (1 - alpha) * rawSpeed;

                // Notify listener
                if (motionListener != null) {
                    motionListener.onSpeedUpdated(currentSpeed);
                }

                // Reset integration
                vX = vY = vZ = 0f;
                intervalTimeAccum = 0f;
            }
        }

        // --- Player direction (rotation) ---
        if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotMatrix, event.values);

            // Optional: remap axes if needed
            float[] remapped = new float[9];
            SensorManager.remapCoordinateSystem(rotMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remapped);

            float[] orientation = new float[3];
            SensorManager.getOrientation(remapped, orientation);

            float yaw = orientation[0]; // rotation around Y axis
            // Forward vector in XZ plane
            Vector3 playerDirection = new Vector3((float) Math.sin(yaw), 0f, (float) Math.cos(yaw));

            if (motionListener != null) {
                motionListener.onPlayerDirectionUpdated(playerDirection);
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i){}

    // TODO: Add methods to get player speed, position, etc.

    /**
     * Starts the motion tracker
     */
    public void startTracking(){
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_GAME);
        Log.d(TAG, "MotionTracker started");
    }

    /**
     * Stops the motion tracker
     */
    public void stopTracking(){
        mSensorManager.unregisterListener(this);
        Log.d(TAG, "MotionTracker stopped");
    }

    // Create SpeedListener
    private MotionListener motionListener;

    /**
     * Sets the speed listener
     * @param listener The listener to set
     */
    public void setMotionListener(MotionListener listener) {
        this.motionListener = listener;
    }

}
