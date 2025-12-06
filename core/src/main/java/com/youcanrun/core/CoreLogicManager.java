package com.youcanrun.core;

import android.content.Context;
import android.util.Log;

import com.youcanrun.sensors.MotionTracker;
import com.youcanrun.sensors.MotionListener;
import com.youcanrun.utils.Vector3;
import com.youcanrun.utils.DataManager;

/**
 * CoreLogicManager interprets data passed to it from other modules
 * and handles all game logic, but does not contain audio/visual or
 * tactile components.
 */
public class CoreLogicManager implements MotionListener {
    private static final String TAG = "CoreLogicManager";
    private final MotionTracker motionTracker;
    private GameEventListener mGameEventListener;
    private GameMap gameMap;
    private Vector3 playerDirection;
    private DataManager dataManager;
    private float playerSpeed;
    private float signalStrength = 0.5f;
    private float playerDistance;
    private float playerTopSpeed;
    private long lastUpdatedTime;

    public CoreLogicManager(Context context) {
        motionTracker = new MotionTracker(context);
        motionTracker.setMotionListener(this);

        // Initialize monster,map other game related components
        Vector3 spawnPos = genSpawnPosition();
        Monster monster = new Monster(spawnPos);
        gameMap = new GameMap(spawnPos, monster);

        dataManager = new DataManager();
        dataManager.setContext(context);
        lastUpdatedTime = System.currentTimeMillis();

        Log.d(TAG, "CoreLogicManager initialized");
    }

    /**
     * Generate a random spawn position in one of the four corners of the map
     * @return Vector3 spawn position in world coordinates
     */
    private Vector3 genSpawnPosition(){
        // Map boundaries - spawn at corners with distance from origin
        float spawnDistance = 100.0f; // 100 meters from origin
        float yHeight = 100.0f; // Fixed height

        // Randomly select one of four corners (0-3)
        int corner = (int)(Math.random() * 4);

        float x, z;
        switch(corner) {
            case 0: // Northeast corner
                x = spawnDistance;
                z = spawnDistance;
                Log.d(TAG, "Monster spawning at NORTHEAST corner");
                break;
            case 1: // Northwest corner
                x = -spawnDistance;
                z = spawnDistance;
                Log.d(TAG, "Monster spawning at NORTHWEST corner");
                break;
            case 2: // Southeast corner
                x = spawnDistance;
                z = -spawnDistance;
                Log.d(TAG, "Monster spawning at SOUTHEAST corner");
                break;
            case 3: // Southwest corner
                x = -spawnDistance;
                z = -spawnDistance;
                Log.d(TAG, "Monster spawning at SOUTHWEST corner");
                break;
            default:
                x = spawnDistance;
                z = spawnDistance;
                break;
        }

        return new Vector3(x, yHeight, z);
    }

    public void setGameEventListener(GameEventListener listener) {
        mGameEventListener = listener;
    }

    @Override
    public void onSpeedUpdated(float speed) {
        playerSpeed = speed;
        if (playerTopSpeed < speed) {
            playerTopSpeed = speed;
        }

        if (mGameEventListener != null) {
            mGameEventListener.onSpeedChanged(speed);
        }
    }

    @Override
    public void onPlayerDistanceUpdated(float distance) {
        playerDistance = distance;
        if (mGameEventListener != null) {
            mGameEventListener.onPlayerDistanceChanged(distance);
        }
    }

    @Override
    public void onPlayerDirectionUpdated(Vector3 direction) {
        playerDirection = direction;
        if (mGameEventListener != null) {
            mGameEventListener.onPlayerDirectionChanged(direction);
        }
    }

    public void updateGame() {
        long currentTime = System.currentTimeMillis();
        float dt = (currentTime - lastUpdatedTime) / 1000f;
        lastUpdatedTime = currentTime;

        if (gameMap != null && playerDirection != null) {
            float enragement = 1.0f + (signalStrength * 4.0f);
            gameMap.getMonster().setEnragement(enragement);

            gameMap.update(playerSpeed, playerDirection, dt);

            Vector3 monsterPos = gameMap.getMonster().getPosition();
            Vector3 playerOri = playerDirection;
            float monsterDistanceToPlayer = gameMap.getMonster().getDistanceToPlayer();

            if (monsterPos != null && mGameEventListener != null) {
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

    private volatile boolean running = false;
    private Thread gameThread;

    public void startGameLoop() {
        running = true;
        gameThread = new Thread(() -> {
            while (running) {
                updateGame();
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Game loop interrupted", e);
                    Thread.currentThread().interrupt();
                }
            }
        });
        gameThread.start();
        Log.d(TAG, "Started game loop");
    }

    public void stopGameLoop() {
        running = false;
        if (gameThread != null) gameThread.interrupt();
        Log.d(TAG, "Stopped game loop");
    }

    public void startGame() {
        motionTracker.startTracking();
        startGameLoop();
    }

    public void stopGame() {
        motionTracker.stopTracking();
        stopGameLoop();
    }

    public void resumeGame() {
        motionTracker.startTracking();
        startGameLoop();
    }

    public void resetGame() {
        playerDirection = null;
        playerSpeed = 0f;
        playerDistance = 0f;
        playerTopSpeed = 0f;

        // Reset motion tracker - clears accumulated distance and speed
        motionTracker.reset();

        // Reinitialize monster and map with random spawn position
        Vector3 spawnPos = genSpawnPosition();
        Monster monster = new Monster(spawnPos);
        gameMap = new GameMap(spawnPos, monster);

        lastUpdatedTime = System.currentTimeMillis();

        Log.d(TAG, "Game state reset");
    }

    /**
     * player has either quit or has lost the game
     */
    public void endGame(){
        // Save current game distance before resetting
        float currentGameDistance = playerDistance;
        float currentGameTopSpeed = playerTopSpeed;

        //updates new highscores
        if(dataManager.loadData("HighScore", "Distance") < currentGameDistance){
            dataManager.saveData("HighScore", "Distance", currentGameDistance);
        }
        if(currentGameTopSpeed > dataManager.loadData("HighScore", "TopSpeed")){
            dataManager.saveData("HighScore", "TopSpeed", currentGameTopSpeed);
        }

        // Update total distance across all games
        float totalDistanceRan = dataManager.loadData("HighScore", "TotalDistance") + currentGameDistance;
        dataManager.saveData("HighScore", "TotalDistance", totalDistanceRan);

        Log.d(TAG, String.format("Game ended - Distance: %.2fm, Total: %.2fm", currentGameDistance, totalDistanceRan));

        //end game logic
        stopGame();
    }

    public GameStats getGameStats() {
        float highscoreDistance = dataManager.loadData("HighScore", "Distance");
        float totalDistance = dataManager.loadData("HighScore", "TotalDistance");
        float highscoreTopSpeed = dataManager.loadData("HighScore", "TopSpeed");

        return new GameStats(
            playerDistance,
            playerTopSpeed,
            highscoreDistance,
            totalDistance,
            highscoreTopSpeed
        );
    }
}

