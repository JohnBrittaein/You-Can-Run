package com.youcanrun.sensors;

import com.youcanrun.utils.Vector3;

public interface MotionListener {
    void onSpeedUpdated(float speed);
    void onPlayerDeltaUpdated(Vector3 delta);
}