package com.youcanrun.core;

/**
 * Class to hold game statistics for display on end game screen
 */
public class GameStats {
    public final float currentDistance;
    public final float currentTopSpeed;
    public final float highscoreDistance;
    public final float totalDistance;
    public final float highscoreTopSpeed;

    public GameStats(float currentDistance, float currentTopSpeed,
                    float highscoreDistance, float totalDistance, float highscoreTopSpeed) {
        this.currentDistance = currentDistance;
        this.currentTopSpeed = currentTopSpeed;
        this.highscoreDistance = highscoreDistance;
        this.totalDistance = totalDistance;
        this.highscoreTopSpeed = highscoreTopSpeed;
    }
}
