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

    @Override
    public void onSensorChanged(SensorEvent event){
        // Because the sensor type is declared as TYPE_LINEAR_ACCELERATION, there is no
        // need to account for gravity to get speed.

        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];

        // Calculate currentSpeed(magnitude) of acceleration
        currentSpeed = (float) Math.sqrt(x*x + y*y + z*z);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i){}

    // TODO: Add methods to get player speed, position, etc.
    private void StartTracking(){
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    private void StopTracking(){
        mSensorManager.unregisterListener(this);
    }

    public double getCurrentSpeed(){return currentSpeed;}

}
