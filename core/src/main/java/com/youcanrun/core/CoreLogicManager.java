package com.youcanrun.core;

import android.content.Context;
import android.util.Log;

import com.youcanrun.sensors.MotionTracker;
import com.youcanrun.sensors.SpeedListener;

/**
 *
 */
public class CoreLogicManager implements SpeedListener {
    private static final String TAG = "CoreLogicManager";

    // TODO: Implement GameEventListener
    private MotionTracker motionTracker;
    private GameEventListener mGameEventListener;


    // Game State Constants

    // Game State Variables

    public CoreLogicManager(Context context) {
        // Initialize motion tracker and related listeners
        motionTracker = new MotionTracker(context);
        motionTracker.setSpeedListener(this);
        Log.d(TAG,"CoreLogicManager initialized");
    }

    public void setGameEventListener(GameEventListener listener) {
        mGameEventListener = listener;
    }

    @Override
    public void onSpeedUpdated(float speed){
        Log.d(TAG, "Speed updated: " + speed);

        // This notifies the MainActivity that the speed has changed
        if (mGameEventListener != null){
            mGameEventListener.onSpeedChanged(speed);
        }
    }

    // TODO: Implement methods for core game logic
    public void startGame(){
        motionTracker.startTracking();
    }

    public void stopGame(){
        motionTracker.stopTracking();
    }

    public void pauseGame(){
        motionTracker.stopTracking();
    }

    public void resumeGame() {
        motionTracker.startTracking();
    }
}
