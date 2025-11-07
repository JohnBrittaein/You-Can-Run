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
    private Context context;

    // TODO: Tune Accelerometer and include GPS for hybrid motion tracking
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mRotationVector;
    private float[] rotMatrix = new float[9];
    private float currentSpeed;

    /**
     * Initializes the motion tracker object
     * Contains a reference to the application context
     * And gets the default accelerometer sensor(s)
     * @param context
     */
    public MotionTracker(Context context) {
        this.context = context;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        Log.d(TAG, "MotionTracker initialized");
    }

    private float vX, vY, vZ;
    private long lastTimeStamp = -1;
    private final float alpha = 0.8f; // Smoothing factor, lowering this will make the speed more responsive
    private final float speedThreshold = 0.01f;
    private final float intervalSeconds = 0.2f; // increasing this will improve consistency but decrease responsiveness
    private float intervalTimeAccum = 0f; // accumulates dt

    @Override
    public void onSensorChanged(SensorEvent event){
        // Because the sensor type is declared as TYPE_LINEAR_ACCELERATION, there is no
        // need to account for gravity to calculate speed.

        // Speed is calculates as M/S

        // This calculates speed by integrating acceleration over a set interval of time
        if(lastTimeStamp != -1){
            // Use the RotationVector sensor to update the rotation matrix
            if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
                SensorManager.getRotationMatrixFromVector(rotMatrix, event.values);
            }

            if (event.sensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION) {
                return;
            }

            float dt = (event.timestamp - lastTimeStamp) * 1.0e-9f; // ns -> s
            intervalTimeAccum += dt;

            // Integrate over the interval
            vX += event.values[0] * dt;
            vY += event.values[1] * dt;
            vZ += event.values[2] * dt;

            // Check interval
            if(intervalTimeAccum >= intervalSeconds){
                // Computing absolute speed
                double rawSpeed = Math.sqrt(vX*vX + vY*vY + vZ*vZ);

                // Reset if small
                if(rawSpeed < speedThreshold){
                    rawSpeed = 0f;
                    currentSpeed = 0f;
                } else {
                    // Apply smoothing
                    currentSpeed = (float)(alpha * currentSpeed + (1 - alpha) * rawSpeed);
                }

                // Compute world-space velocity using the rotation matrix
                float wX = rotMatrix[0] * vX + rotMatrix[1] * vY + rotMatrix[2] * vZ;
                float wY = rotMatrix[3] * vX + rotMatrix[4] * vY + rotMatrix[5] * vZ;
                float wZ = rotMatrix[6] * vX + rotMatrix[7] * vY + rotMatrix[8] * vZ;

                // Scale by interval to get displacement in meters
                Vector3 playerDelta = new Vector3(wX, wY, wZ).scale(intervalTimeAccum);

                // Log.d(TAG,"Accelerometer - TimeStamp: " + event.timestamp
                //         + " vX:" + vX + " vY:" + vY + " vZ:" + vZ + " Speed: " + currentSpeed);

                //Log.d(TAG, "Rotation Vector - wX: " + wX + "wY: " + wY + "wZ: " + wZ);
                //Log.d(TAG, "Rotation Vector - Player Delta: " + playerDelta.x + " " + playerDelta.y + " " + playerDelta.z);
                notifyListener(currentSpeed, playerDelta);

                // Reset for next interval
                vX = vY = vZ = 0f;
                intervalTimeAccum = 0f;
            }
        }
        lastTimeStamp = event.timestamp;

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

    /**
     * Gets the current speed of the player
     * @return The current speed in meters/second
     */
    public double getCurrentSpeed(){
        Log.d(TAG, "Current speed: " + currentSpeed);
        return currentSpeed;
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

    /**
     * Notifies the speed listener of a new speed
     * @param speed
     */
    private void notifyListener(float speed, Vector3 playerDelta) {
        if (motionListener != null) {
            motionListener.onSpeedUpdated(speed);
            motionListener.onPlayerDeltaUpdated(playerDelta);
        }
    }

}
