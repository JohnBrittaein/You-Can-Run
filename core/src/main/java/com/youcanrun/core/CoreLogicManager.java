package com.youcanrun.core;

import android.content.Context;
import android.util.Log;

import androidx.graphics.shapes.Utils;

import com.youcanrun.sensors.MotionTracker;
import com.youcanrun.sensors.MotionListener;
import com.youcanrun.utils.Vector3;
import com.youcanrun.utils.DataManager;

/**
 * CoreLogicManager interprets data passed to it from other modules
 * and handles all game logic, but does not contain audio/visual or
 * tactile components.
 *
 * @date 11-07-2025
 */
public class CoreLogicManager implements MotionListener {
    private static final String TAG = "CoreLogicManager";

    // TODO: Implement GameEventListener
    private MotionTracker motionTracker;
    private GameEventListener mGameEventListener;


    // Game State Constants

    // Game State Variables
    private GameMap gameMap;
    private Vector3 playerDirection;
    private DataManager dataManager;
    private float playerSpeed;

    private float playerDistance;

    private float playerTopSpeed;
    private long lastUpdatedTime;

    public CoreLogicManager(Context context) {
        // Initialize motion tracker and related listeners
        motionTracker = new MotionTracker(context);
        motionTracker.setMotionListener(this);

        // Initialize monster,map other game related components
        Vector3 bounds = new Vector3(100,100,100);
        Vector3 spawnPos = bounds; //TODO: maybe implement a randomized edge spawning function
        Monster monster = new Monster(spawnPos);
        gameMap = new GameMap(spawnPos, monster);

        dataManager = new DataManager();
        dataManager.setContext(context);
        lastUpdatedTime = System.currentTimeMillis();

        Log.d(TAG,"CoreLogicManager initialized");
    }

    public void setGameEventListener(GameEventListener listener) {
        mGameEventListener = listener;
    }

    @Override
    public void onSpeedUpdated(float speed){
        Log.d(TAG, "Speed updated: " + speed);

        playerSpeed = speed;
        if (playerTopSpeed<speed){
            playerTopSpeed = speed;
        }

        // This notifies the MainActivity that the speed has changed
        if (mGameEventListener != null){
            mGameEventListener.onSpeedChanged(speed);
        }
    }

    @Override
    public void onPlayerDistanceUpdated(float distance){
        Log.d(TAG, "Distance Updated: " + distance);

        playerDistance = distance;
        // This notifies the MainActivity that the distance has changed
        if (mGameEventListener != null){
            mGameEventListener.onPlayerDistanceChanged(distance);
        }
    }

    @Override
    public void onPlayerDirectionUpdated(Vector3 direction) {
        Log.d(TAG, "Player Delta updated: " + direction.x + " " + direction.y + " " + direction.z);

        playerDirection = direction;

        // This notifies the MainActivity that player delta (orientation and speed) has changed
        if (mGameEventListener != null){
            mGameEventListener.onPlayerDirectionChanged(direction);
        }
    }

    // TODO: Implement methods for core game logic
    public void updateGame(){
        long currentTime = System.currentTimeMillis();
        float dt = (currentTime - lastUpdatedTime) / 1000f;
        lastUpdatedTime = currentTime;

        if(gameMap != null && playerDirection != null){
            gameMap.update(playerSpeed, playerDirection, dt);

            Vector3 monsterPos = gameMap.getMonster().getPosition();
            Vector3 playerOri = playerDirection;
            float monsterDistanceToPlayer = gameMap.getMonster().getDistanceToPlayer();
            if (monsterPos != null && mGameEventListener != null){
                mGameEventListener.onMapPositionsChanged(monsterPos, playerOri, monsterDistanceToPlayer);
            }
        }
    }

    // Game start/stop and lifecycle controls
    private volatile boolean running = false;
    private Thread gameThread;
    public void startGameLoop(){
        running = true;
        gameThread = new Thread(() -> {
            while (running) {

                updateGame();

                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        gameThread.start();
        Log.d(TAG,"Started Game Loop");
    }
    public void stopGameLoop(){
        running = false;
        if (gameThread != null) gameThread.interrupt();
        Log.d(TAG,"Stopped Game Loop");
    }
    public void startGame(){
        motionTracker.startTracking();
        startGameLoop();
    }

    public void stopGame(){
        motionTracker.stopTracking();
        stopGameLoop();
    }

    public void pauseGame(){
        motionTracker.stopTracking();
        stopGameLoop();
    }

    public void resumeGame() {
        motionTracker.startTracking();
        startGameLoop();
    }

    /**
     * player has either quit or has lost the game
     */
    public void endGame(){
        //updates new highscores
        if(dataManager.loadData("HighScores", "Distance") < playerDistance){
            //update new highscore
            dataManager.saveData("HighScore", "Distance", playerDistance);
        }
        if(playerTopSpeed > dataManager.loadData("HighScore", "TopSpeed")){
            dataManager.saveData("HighScore", "TopSpeed", playerTopSpeed);
        }

        float highscoreDistance = dataManager.loadData("HighScores", "Distance");
        float currentDistance = playerDistance;
        float totalDistanceRan = dataManager.loadData("HighScore", "TotalDistance") + playerDistance;
        float highscoreTopSpeed = dataManager.loadData("HighScore", "TopSpeed");
        //player top speed

        dataManager.saveData("HighScore", "TotalDistance", totalDistanceRan);

        //end game logic
    }
}

