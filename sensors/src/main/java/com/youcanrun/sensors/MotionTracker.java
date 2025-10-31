package com.youcanrun.sensors;

import android.content.Context;
import android.util.Log;

/**
 *
 */
public class MotionTracker {
    private static final String TAG = "MotionTracker";
    private Context context;

    // TODO: Implement accelerometer and GPS tracking

    public MotionTracker(Context context) {
        this.context = context;
        Log.d(TAG, "MotionTracker initialized");
    }

    // TODO: Add methods to get player speed, position, etc.
}
