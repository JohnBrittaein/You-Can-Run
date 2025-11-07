package com.youcanrun.core;

import android.content.Context;
import android.util.Log;

import com.youcanrun.sensors.MotionTracker;
import com.youcanrun.sensors.MotionListener;
import com.youcanrun.utils.Vector3;

/**
 *
 */
public class CoreLogicManager implements MotionListener {
    private static final String TAG = "CoreLogicManager";

    // TODO: Implement GameEventListener
    private MotionTracker motionTracker;
    private GameEventListener mGameEventListener;


    // Game State Constants

    // Game State Variables

    public CoreLogicManager(Context context) {
        // Initialize motion tracker and related listeners
        motionTracker = new MotionTracker(context);
        motionTracker.setMotionListener(this);
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

    @Override
    public void onPlayerDeltaUpdated(Vector3 delta) {
        Log.d(TAG, "Player Delta updated: " + delta.x + " " + delta.y + " " + delta.z);

        // This notifies the MainActivity that player delta (orientation and speed) has changed
        if (mGameEventListener != null){
            mGameEventListener.onPlayerDeltaChanged(delta);
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
