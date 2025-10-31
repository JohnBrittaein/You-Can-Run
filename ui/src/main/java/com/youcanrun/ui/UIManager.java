package com.youcanrun.ui;

import android.content.Context;
import android.util.Log;

/**
 *
 */
public class UIManager {
    private static final String TAG = "UIManager";
    private Context context;

    // TODO: Implement HUD, glitch effects, and other UI elements

    public UIManager(Context context) {
        this.context = context;
        Log.d(TAG, "UIManager initialized");
    }

    // TODO: Add methods to handle HUD, effects, etc.
}
