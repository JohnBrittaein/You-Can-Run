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

    @Override
    public void onSensorChanged(SensorEvent event){
        // Because the sensor type is declared as TYPE_LINEAR_ACCELERATION, there is no
        // need to account for gravity to get speed.

        if (lastTimeStamp != -1) {
            float dt = (event.timestamp - lastTimeStamp)  * 1.0e-9f; // ns -> seconds
            vX = event.values[0] * dt;
            vY = event.values[1] * dt;
            vZ = event.values[2] * dt;
        }
        lastTimeStamp = event.timestamp;

        // Calculate currentSpeed(magnitude) of acceleration
        currentSpeed = (float) Math.sqrt(vX * vX + vY*vY + vZ*vZ);

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
