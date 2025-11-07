package com.youcanrun.core;

import android.util.Log;

import com.youcanrun.utils.Vector3;

public class Monster {
    private static final String TAG = "Monster";
    private Vector3 position;
    private Vector3 velocity;
    private float baseSpeed;
    private float momentum;

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
        this.baseSpeed = 1.0f;
        this.momentum = 1.0f; // higher has snappier movement
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
                toOrigin.x * baseSpeed,
                toOrigin.y * baseSpeed,
                toOrigin.z * baseSpeed
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
    };

    public Vector3 getPosition(){return position;}
    public Vector3 getVelocity(){return velocity;}

    public float getDistanceToPlayer(){return distanceToPlayer;}
    public void setPosition(Vector3 nPosition){this.position = nPosition;}

}
