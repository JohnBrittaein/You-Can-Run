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
 */
public class MotionTracker implements SensorEventListener {
    private static final String TAG = "MotionTracker";
    private static final float DISTANCE_TOLERANCE = 0.001f;
    private static final float SMOOTHING_ALPHA = 0.8f;
    private static final float UPDATE_INTERVAL_SECONDS = 0.2f;

    private final SensorManager mSensorManager;
    private final Sensor mAccelerometer;
    private final Sensor mRotationVector;
    private final float[] rotMatrix = new float[9];
    private float currentSpeed;
    private float currentDistance;
    private float vX, vY, vZ;
    private long lastTimeStamp = -1;
    private float intervalTimeAccum = 0f;
    private MotionListener motionListener;

    public MotionTracker(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long timestamp = event.timestamp;

        if (lastTimeStamp < 0) {
            lastTimeStamp = timestamp;
            return;
        }

        float dt = (timestamp - lastTimeStamp) * 1.0e-9f;
        lastTimeStamp = timestamp;

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            vX += event.values[0] * dt;
            vY += event.values[1] * dt;
            vZ += event.values[2] * dt;

            intervalTimeAccum += dt;

            float rawSpeed = (float) Math.sqrt(vX * vX + vY * vY + vZ * vZ);
            float rawDistanceAtMoment = (float) Math.sqrt((vX * dt) * (vX * dt) + (vY * dt) * (vY * dt) + (vZ * dt) * (vZ * dt));

            if (rawDistanceAtMoment > DISTANCE_TOLERANCE && motionListener != null) {
                currentDistance = currentDistance + rawDistanceAtMoment;
                motionListener.onPlayerDistanceUpdated(currentDistance);
            }

            if (intervalTimeAccum >= UPDATE_INTERVAL_SECONDS) {
                currentSpeed = SMOOTHING_ALPHA * currentSpeed + (1 - SMOOTHING_ALPHA) * rawSpeed;

                if (motionListener != null) {
                    motionListener.onSpeedUpdated(currentSpeed);
                }

                vX = vY = vZ = 0f;
                intervalTimeAccum = 0f;
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotMatrix, event.values);

            float[] remapped = new float[9];
            SensorManager.remapCoordinateSystem(rotMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remapped);

            float[] orientation = new float[3];
            SensorManager.getOrientation(remapped, orientation);

            float yaw = orientation[0];
            Vector3 playerDirection = new Vector3((float) Math.sin(yaw), 0f, (float) Math.cos(yaw));

            if (motionListener != null) {
                motionListener.onPlayerDirectionUpdated(playerDirection);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void startTracking() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_GAME);
        Log.d(TAG, "MotionTracker started");
    }

    public void stopTracking() {
        mSensorManager.unregisterListener(this);
        Log.d(TAG, "MotionTracker stopped");
    }

    public void reset() {
        currentSpeed = 0f;
        currentDistance = 0f;
        vX = 0f;
        vY = 0f;
        vZ = 0f;
        lastTimeStamp = -1;
        intervalTimeAccum = 0f;
        Log.d(TAG, "MotionTracker reset");
    }

    public void setMotionListener(MotionListener listener) {
        this.motionListener = listener;
    }
}
