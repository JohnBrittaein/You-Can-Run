package com.youcanrun.core;

import com.youcanrun.utils.Vector3;

public interface GameEventListener {
    void onSpeedChanged(float speed);
    void onPlayerDistanceChanged(float distance);
    void onPlayerDirectionChanged(Vector3 direction);
    void onMapPositionsChanged(Vector3 monsterDelta, Vector3 playerOri, float monsterDistanceToPlayer);
}
