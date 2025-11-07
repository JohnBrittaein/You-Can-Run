package com.youcanrun.core;

import android.util.Log;

import com.youcanrun.utils.Vector3;

/**
 *
 */
public class GameMap {
    private static final String TAG = "GameMap";
    private Monster monster;
    private Vector3 bounds;

    public GameMap(Vector3 bounds, Monster monster){
        this.bounds = bounds;
        this.monster = monster;
        Log.d(TAG,"GameMap created");
    }

    public void update(float playerSpeed, Vector3 playerDelta, float dt){
        if(monster != null){
            Vector3 mPos = monster.getPosition();
            if (mPos.x > bounds.x || mPos.y > bounds.y || mPos.z > bounds.z){
                clampPosition(monster);
            }
            monster.update(playerSpeed, playerDelta, dt);
        }
    }

    /**
     * Keeps everything within the game maps boundaries
     * @param monster the monster
     */
    public void clampPosition(Monster monster){
        Vector3 pos = monster.getPosition();
        float x = Math.max(0, Math.min(pos.x, bounds.x));
        float y = Math.max(0, Math.min(pos.y, bounds.y));
        float z = Math.max(0, Math.min(pos.z, bounds.z));
        monster.setPosition(new Vector3(x, y, z));
    }

    public Monster getMonster() {
        return monster;
    }
}
