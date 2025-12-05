package com.youcanrun.core;

import android.content.Context;
import android.util.Log;

import com.youcanrun.sensors.MotionTracker;
import com.youcanrun.sensors.MotionListener;
import com.youcanrun.utils.Vector3;

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
    private final MotionTracker motionTracker;
    private GameEventListener mGameEventListener;


    // Game State Constants

    // Game State Variables
    private GameMap gameMap;
    private Vector3 playerDirection;
    private float playerSpeed;
    private float signalStrength = 0.5f;

    private long lastUpdatedTime;

    public CoreLogicManager(Context context) {
        // Initialize motion tracker and related listeners
        motionTracker = new MotionTracker(context);
        motionTracker.setMotionListener(this);

        // Initialize monster,map other game related components
        Vector3 spawnPos = new Vector3(100,100,100); //TODO: maybe implement a randomized edge spawning function
        Monster monster = new Monster(spawnPos);
        gameMap = new GameMap(spawnPos, monster);

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

        // This notifies the MainActivity that the speed has changed
        if (mGameEventListener != null){
            mGameEventListener.onSpeedChanged(speed);
        }
    }

    @Override
    public void onPlayerDistanceUpdated(float distance){
        Log.d(TAG, "Distance Updated: " + distance);

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
            float enragement = 1.0f + (signalStrength * 4.0f);
            gameMap.getMonster().setEnragement(enragement);

            gameMap.update(playerSpeed, playerDirection, dt);

            Vector3 monsterPos = gameMap.getMonster().getPosition();
            Vector3 playerOri = playerDirection;
            float monsterDistanceToPlayer = gameMap.getMonster().getDistanceToPlayer();

            if (monsterDistanceToPlayer < 0){
                endGame();
            }
            if (monsterPos != null && mGameEventListener != null){
                mGameEventListener.onMapPositionsChanged(monsterPos, playerOri, monsterDistanceToPlayer);
            }
        }
    }

    public void setSignalStrength(float strength) {
        this.signalStrength = Math.max(0f, Math.min(strength, 1f));
    }

    public Monster getMonster() {
        if (gameMap != null) {
            return gameMap.getMonster();
        }
        return null;
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

    public void resumeGame() {
        motionTracker.startTracking();
        startGameLoop();
    }

    /**
     * Reset all game state variables for a new game
     */
    public void resetGame() {
        // Reset player state
        playerDirection = null;
        playerSpeed = 0f;
        signalStrength = 0.5f;

        // Reinitialize monster and map
        Vector3 spawnPos = new Vector3(100, 100, 100);
        Monster monster = new Monster(spawnPos);
        gameMap = new GameMap(spawnPos, monster);

        // Reset timing
        lastUpdatedTime = System.currentTimeMillis();

        Log.d(TAG, "Game state reset - ready for new game");
    }

    /**
     * player has either quit or has lost the game
     */
    public void endGame(){
        // Show/saves scores
        //end game logic
        stopGame();

        // Save score
    }
}
