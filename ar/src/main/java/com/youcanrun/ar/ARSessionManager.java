package com.youcanrun.ar;

import android.content.Context;
import android.util.Log;

/**
 *
 */
public class ARSessionManager {
    private static final String TAG = "ARSessionManager";
    private Context context;

    // TODO: Initialize ARCore session, manage anchors, render monsters

    public ARSessionManager(Context context) {
        this.context = context;
        Log.d(TAG, "ARSessionManager initialized");
    }

    // TODO: Add methods to manage and render the AR environment
}
