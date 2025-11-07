package com.youcanrun.core;

import com.youcanrun.utils.Vector3;

public class Monster {
    private Vector3 position;
    private Vector3 velocity;
    private float baseSpeed;
    private float momentum;
    //TODO: consider implementing dynamic momentum instead of fixed
    // Makes the monster feel more alive and immersive

    public Monster(Vector3 startPos){
        this.position = startPos;
        this.velocity = new Vector3(0,0,0);
        this.baseSpeed = 0.5f;
        this.momentum = 0.5f; // higher has snappier movement
    }

    public void update(float playerSpeed, Vector3 playerDelta, float dt){
        Vector3 direction = playerDelta.normalize();
        Vector3 desVel = direction.scale(baseSpeed + playerSpeed);
        velocity = velocity.lerp(desVel, momentum * dt);
        position = position.add(velocity.scale(dt));
    };

    public Vector3 getPosition(){return position;}
    public Vector3 getVelocity(){return velocity;}

}
