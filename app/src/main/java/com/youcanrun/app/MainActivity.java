package com.youcanrun.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.youcanrun.ar.ARActivity;
import com.youcanrun.audio.AudioManager;
import com.youcanrun.core.CoreLogicManager;
import com.youcanrun.core.GameEventListener;
import com.youcanrun.core.GameStats;
import com.youcanrun.sensors.HapticFeedbackManager;
import com.youcanrun.ui.UIManager;
import com.youcanrun.utils.Vector3;

import java.util.Objects;

/**
 * The MainActivity serves as the orchestrator between all of the other modules
 * :core, :sensors, :ui, :audio
 * The role of the MainActivity is to initialize modules, implement listeners, pass data,
 * and handle app lifecycles. Other than this, the MainActivity has NO other
 * functional use.
 *
 * MainActivity displays the main HUD with sensors (compass, speedometer, etc.)
 * AR viewing is optional and triggered via the camera button, which launches ARActivity.
 *
 * @date 12-04-2025
 */
public class MainActivity extends AppCompatActivity implements GameEventListener {
    private static final String TAG = "MainActivity";

    private CoreLogicManager coreLogicManager;
    private UIManager uiManager;
    private AudioManager audioManager;
    private HapticFeedbackManager hapticFeedbackManager;
    private boolean gameStarted = false;
    private boolean gameEnded = false;

    // Game over threshold - monster catches player at this distance
    private static final float GAME_OVER_DISTANCE = 1.0f; // 1 meter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if device supports AR (needed for optional camera button)
        if (!checkSystemSupport(this)) {
            return;
        }

        // Initialize UI first (handles all UI including camera button)
        uiManager = new UIManager(this);

        // Initialize core game modules
        coreLogicManager = new CoreLogicManager(this);
        coreLogicManager.setGameEventListener(this);

        // Initialize audio manager and connect it to UI
        audioManager = new AudioManager(this);
        uiManager.setAudioManager(audioManager);

        // Initialize haptic feedback manager
        hapticFeedbackManager = new HapticFeedbackManager(this);

        // Set game start listener - game will start when start button is clicked
        uiManager.setOnGameStartListener(this::startGame);

        // Set game end listener - ends game when quit button is clicked
        uiManager.setOnGameEndListener(() -> endGame(false));

        // Start white noise loop (will be controlled by monster position)
        audioManager.startWhiteNoise();

        Log.d(TAG, "All modules initialized (AR is optional via camera button)");
    }

    /**
     * Start the game (called when start button is clicked)
     */
    public void startGame() {
        if (!gameStarted && coreLogicManager != null) {
            // Reset game ended flag if starting a new game
            gameEnded = false;

            // Reset all game state before starting
            coreLogicManager.resetGame();

            // Reset UI displays
            if (uiManager != null) {
                uiManager.resetUIDisplays();
            }

            // Restart white noise audio
            if (audioManager != null) {
                audioManager.startWhiteNoise();
            }

            // Start the game
            coreLogicManager.startGame();
            gameStarted = true;
            Log.d(TAG, "Game started");
        }
    }

    /**
     * End the game - either caught by monster or user quit
     * @param caughtByMonster true if monster caught player, false if user quit
     */
    public void endGame(boolean caughtByMonster) {
        if (gameEnded) return; // Already ended

        gameEnded = true;
        gameStarted = false;

        // Stop all systems
        if (coreLogicManager != null) {
            coreLogicManager.stopGame();
        }
        if (hapticFeedbackManager != null) {
            hapticFeedbackManager.stop();
        }
        if (audioManager != null) {
            audioManager.stopWhiteNoise();
        }

        // Get game stats
        GameStats stats = null;
        if (coreLogicManager != null) {
            stats = coreLogicManager.getGameStats();
        }

        // Show game over screen
        if (uiManager != null) {
            uiManager.showGameOverScreen(caughtByMonster, stats);
        }

        Log.d(TAG, "Game ended - " + (caughtByMonster ? "Caught by monster" : "User quit"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only resume if game was already started
        if (gameStarted && coreLogicManager != null) {
            coreLogicManager.resumeGame();
            Log.d(TAG, "Game resumed");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioManager != null) {
            audioManager.release();
        }
        if (hapticFeedbackManager != null) {
            hapticFeedbackManager.release();
        }
    }

    @Override
    public void onSpeedChanged(float speed) {
        if (uiManager != null) {
            uiManager.updateDevHudSpeed(speed);
            uiManager.updateSpeedometer(speed);
        }
    }

    @Override
    public void onPlayerDistanceChanged(float distance) {
        if (uiManager != null) {
            uiManager.updateDevHudPlayerDistance(distance);
            uiManager.updateOdometer(distance);
        }
    }

    @Override
    public void onPlayerDirectionChanged(Vector3 direction) {
        if (uiManager != null) {
            uiManager.updateDevHudDirection(direction);
            uiManager.updateCompass(direction);

        }
    }

    @Override
    public void onMapPositionsChanged(Vector3 monsterPos, Vector3 playerOri, float monsterDistanceToPlayer) {
        // Check if game is over
        if (gameEnded) return;

        // Check if monster caught the player
        if (monsterDistanceToPlayer <= GAME_OVER_DISTANCE) {
            endGame(true); // Caught by monster
            return;
        }

        if (uiManager != null) {
            uiManager.updateDevHudMapView(monsterPos, playerOri);
            uiManager.updateDevHudDistance(monsterDistanceToPlayer);

            float signalStrength = uiManager.getSignalStrength();
            if (coreLogicManager != null) {
                coreLogicManager.setSignalStrength(signalStrength);

                // Get monster enragement and pass to ProxSensor
                if (coreLogicManager.getMonster() != null) {
                    float enragement = coreLogicManager.getMonster().getEnragement();
                    uiManager.updateProxSensorEnragement(enragement);
                }
            }

            uiManager.updateProxSensor(monsterPos, playerOri);

            // Update white noise audio based on monster position
            if (audioManager != null) {
                audioManager.updateWhiteNoise(monsterPos, playerOri, monsterDistanceToPlayer, signalStrength);

                // Update audio level meter with current audio level
                float audioLevel = audioManager.getCurrentAudioLevel();
                uiManager.updateAudioLevelMeter(audioLevel);
            }

            // Update haptic feedback based on monster distance and signal strength
            if (hapticFeedbackManager != null) {
                hapticFeedbackManager.updateHaptics(monsterDistanceToPlayer, signalStrength);
            }
        }

        ARActivity.updateMonster(monsterPos, playerOri);
    }

    public boolean checkSystemSupport(Activity activity) {

        // checking whether the API version of the running Android >= 24
        // that means Android Nougat 7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            String openGlVersion = ((ActivityManager) Objects.requireNonNull(activity.getSystemService(Context.ACTIVITY_SERVICE))).getDeviceConfigurationInfo().getGlEsVersion();

            // checking whether the OpenGL version >= 3.0
            if (Double.parseDouble(openGlVersion) >= 3.0) {
                return true;
            } else {
                Toast.makeText(activity, "App needs OpenGl Version 3.0 or later", Toast.LENGTH_SHORT).show();
                activity.finish();
                return false;
            }
        } else {
            Toast.makeText(activity, "App does not support required Build Version", Toast.LENGTH_SHORT).show();
            activity.finish();
            return false;
        }
    }
}

