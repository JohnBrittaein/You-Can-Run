package com.youcanrun.core;

import com.youcanrun.utils.Vector3;

public interface GameEventListener {
    void onSpeedChanged(float speed);
    void onPlayerDeltaChanged(Vector3 delta);
}
