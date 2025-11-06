package com.youcanrun.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Uses the onboard sensors to track the player's motion
 *
 * @author John Brittain
 * @version 0.1
 * @since 2025-11-05
 */
public class MotionTracker implements SensorEventListener {
    private static final String TAG = "MotionTracker";
    private Context context;

    // TODO: Tune Accelerometer and include GPS for hybrid motion tracking
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
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
            float dt = (event.timestamp - lastTimeStamp) * 1.0e-9f; // ns -> s
            intervalTimeAccum += dt;

            // Integrate over the interval
            vX += event.values[0] * dt;
            vY += event.values[1] * dt;
            vZ += event.values[2] * dt;

            // Check interval
            if(intervalTimeAccum >= intervalSeconds){
                double rawSpeed = Math.sqrt(vX*vX + vY*vY + vZ*vZ);

                // Reset if small
                if(rawSpeed < speedThreshold){
                    rawSpeed = 0f;
                    currentSpeed = 0f;
                } else {
                    // Apply smoothing
                    currentSpeed = (float)(alpha * currentSpeed + (1 - alpha) * rawSpeed);
                }

                // Reset for next interval
                vX = vY = vZ = 0f;
                intervalTimeAccum = 0f;
            }
        }
        lastTimeStamp = event.timestamp;

        // Notify listener
        notifySpeed(currentSpeed);

       // Log.d(TAG,"Accelerometer - TimeStamp: " + event.timestamp
       //         + " vX:" + vX + " vY:" + vY + " vZ:" + vZ + " Speed: " + currentSpeed);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i){}

    // TODO: Add methods to get player speed, position, etc.

    /**
     * Starts the motion tracker
     */
    public void startTracking(){
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
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
    private SpeedListener speedListener;

    /**
     * Sets the speed listener
     * @param listener The listener to set
     */
    public void setSpeedListener(SpeedListener listener) {
        this.speedListener = listener;
    }

    /**
     * Notifies the speed listener of a new speed
     * @param speed
     */
    private void notifySpeed(float speed) {
        if (speedListener != null) speedListener.onSpeedUpdated(speed);
    }

}
