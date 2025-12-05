package com.youcanrun.core;

import android.util.Log;

import com.youcanrun.utils.Vector3;

/**
 * Monster class, which determines the position,speed and in general
 * behavior of the monster chasing the player.
 *
 * @date 11-07-2025
 * @author John Brittain
 */
public class Monster {
    private static final String TAG = "Monster";
    private Vector3 position;
    private final Vector3 velocity;
    private final float baseSpeed;
    private float enragement;

    private float distanceToPlayer;
    //TODO: consider implementing dynamic momentum instead of fixed
    // Makes the monster feel more alive and immersive

    public Monster(Vector3 startPos){
        this.position = startPos;
        this.velocity = new Vector3(0,0,0);
        this.distanceToPlayer = (float) Math.sqrt(
                position.x * position.x +
                        position.y * position.y +
                        position.z * position.z
        );
        this.baseSpeed = 0.75f;
        this.enragement = 1.0f;
        Log.d(TAG,"Monster created");
    }

    public void update(float playerSpeed, Vector3 playerDirection, float dt){
        // Vector from monster to origin
        Vector3 toOrigin = new Vector3(-position.x, -position.y, -position.z);

        // Normalize direction
        float length = (float) Math.sqrt(toOrigin.x*toOrigin.x + toOrigin.y*toOrigin.y + toOrigin.z*toOrigin.z);
        if (length > 0.01f) {
            toOrigin.x /= length;
            toOrigin.y /= length;
            toOrigin.z /= length;
        }

        // Monster velocity toward origin(player)
        Vector3 monsterVel = new Vector3(
                toOrigin.x * baseSpeed * enragement,
                toOrigin.y * baseSpeed * enragement,
                toOrigin.z * baseSpeed * enragement
        );

        // Player velocity
        Vector3 playerVel = new Vector3(
                playerDirection.x * playerSpeed,
                playerDirection.y * playerSpeed,
                playerDirection.z * playerSpeed
        );

        // Subtract player's velocity
        monsterVel.x -= playerVel.x;
        monsterVel.y -= playerVel.y;
        monsterVel.z -= playerVel.z;

        // Update monster position
        position.x += monsterVel.x * dt;
        position.y += monsterVel.y * dt;
        position.z += monsterVel.z * dt;

        distanceToPlayer = (float) Math.sqrt(position.x*position.x + position.y*position.y + position.z*position.z);

        //Logging
        //Log.d(TAG,"Distance To player: " + distance + " Player Speed: " + playerSpeed);
        //Log.d(TAG,"Position X: " + position.x + " Y: " + position.y + " Z: " + position.z);
        //Log.d(TAG,"Velocity X: " + velocity.x + " Y: " + velocity.y + " Z: " + velocity.z);
    }

    public Vector3 getPosition(){return position;}
    public float getEnragement(){return enragement;}
    public void setEnragement(float enragement){this.enragement = enragement;}
    public float getDistanceToPlayer(){return distanceToPlayer;}
    public void setPosition(Vector3 nPosition){this.position = nPosition;}

}
