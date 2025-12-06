package com.youcanrun.core;

import android.util.Log;

import com.youcanrun.utils.Vector3;

/**
 * Monster class, which determines the position, speed and in general
 * behavior of the monster chasing the player.
 */
public class Monster {
    private static final String TAG = "Monster";
    private Vector3 position;
    private final float baseSpeed;
    private float enragement;
    private float distanceToPlayer;

    public Monster(Vector3 startPos) {
        this.position = startPos;
        this.distanceToPlayer = position.length();
        this.baseSpeed = 0.75f;
        this.enragement = 1.0f;
        Log.d(TAG, "Monster created");
    }

    public void update(float playerSpeed, Vector3 playerDirection, float dt) {
        Vector3 toOrigin = new Vector3(-position.x, -position.y, -position.z);
        Vector3 normalizedDirection = toOrigin.normalize();

        Vector3 monsterVel = normalizedDirection.scale(baseSpeed * enragement);
        Vector3 playerVel = playerDirection.scale(playerSpeed);
        Vector3 relativeVel = monsterVel.subtract(playerVel);

        position = position.add(relativeVel.scale(dt));
        distanceToPlayer = position.length();
    }

    public Vector3 getPosition() {
        return position;
    }

    public float getEnragement() {
        return enragement;
    }

    public void setEnragement(float enragement) {
        this.enragement = enragement;
    }

    public float getDistanceToPlayer() {
        return distanceToPlayer;
    }

    public void setPosition(Vector3 nPosition) {
        this.position = nPosition;
    }

}
