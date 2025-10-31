package com.youcanrun.audio;

import android.content.Context;
import android.util.Log;

/**
 *
 */
public class AudioManager {
    private static final String TAG = "AudioManager";
    private Context context;

    // TODO: Implement sound playback, volume control, audio effects

    public AudioManager(Context context) {
        this.context = context;
        Log.d(TAG, "AudioManager initialized");
    }

    // TODO: Add methods to play sounds
}
