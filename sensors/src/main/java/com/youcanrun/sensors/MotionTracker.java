package com.youcanrun.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 *
 */
public class MotionTracker implements SensorEventListener {
    private static final String TAG = "MotionTracker";
    private Context context;

    // TODO: Implement accelerometer and GPS tracking
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float currentSpeed;

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
                }

                // Apply smoothing
                currentSpeed = (float)(alpha * currentSpeed + (1 - alpha) * rawSpeed);

                // Reset for next interval
                vX = vY = vZ = 0f;
                intervalTimeAccum = 0f;
            }
        }
        lastTimeStamp = event.timestamp;


        Log.d(TAG,"Accelerometer - TimeStamp: " + event.timestamp
                + " vX:" + vX + " vY:" + vY + " vZ:" + vZ + " Speed: " + currentSpeed);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i){}

    // TODO: Add methods to get player speed, position, etc.
    public void startTracking(){
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        Log.d(TAG, "MotionTracker started");
    }

    public void stopTracking(){
        mSensorManager.unregisterListener(this);
        Log.d(TAG, "MotionTracker stopped");
    }

    public double getCurrentSpeed(){
        Log.d(TAG, "Current speed: " + currentSpeed);
        return currentSpeed;
    }

}
