package com.youcanrun.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
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
 */
public class MainActivity extends AppCompatActivity implements GameEventListener {
    private static final String TAG = "MainActivity";
    private static final float GAME_OVER_DISTANCE = 0.0f;

    private CoreLogicManager coreLogicManager;
    private UIManager uiManager;
    private AudioManager audioManager;
    private HapticFeedbackManager hapticFeedbackManager;
    private boolean gameStarted = false;
    private boolean gameEnded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkSystemSupport(this)) {
            return;
        }

        uiManager = new UIManager(this);
        coreLogicManager = new CoreLogicManager(this);
        coreLogicManager.setGameEventListener(this);
        audioManager = new AudioManager(this);
        uiManager.setAudioManager(audioManager);
        hapticFeedbackManager = new HapticFeedbackManager(this);
        uiManager.setOnGameStartListener(this::startGame);
        uiManager.setOnGameEndListener(() -> endGame(false));
        audioManager.startWhiteNoise();

        Log.d(TAG, "All modules initialized");
    }

    public void startGame() {
        if (!gameStarted && coreLogicManager != null) {
            gameEnded = false;
            coreLogicManager.resetGame();

            if (uiManager != null) {
                uiManager.resetUIDisplays();
            }
            if (audioManager != null) {
                audioManager.startWhiteNoise();
            }

            coreLogicManager.startGame();
            gameStarted = true;
            Log.d(TAG, "Game started");
        }
    }

    public void endGame(boolean caughtByMonster) {
        if (gameEnded) return;

        gameEnded = true;
        gameStarted = false;

        if (hapticFeedbackManager != null) {
            hapticFeedbackManager.stop();
        }
        if (audioManager != null) {
            audioManager.stopWhiteNoise();
        }

        GameStats stats = null;
        if (coreLogicManager != null) {
            coreLogicManager.endGame();
            stats = coreLogicManager.getGameStats();
        }

        if (uiManager != null) {
            uiManager.showGameOverScreen(caughtByMonster, stats);
        }

        Log.d(TAG, "Game ended - " + (caughtByMonster ? "Caught by monster" : "User quit"));
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        if (gameEnded) return;

        if (monsterDistanceToPlayer <= GAME_OVER_DISTANCE) {
            endGame(true);
            return;
        }

        if (uiManager != null) {
            uiManager.updateDevHudMapView(monsterPos, playerOri);
            uiManager.updateDevHudDistance(monsterDistanceToPlayer);

            float signalStrength = uiManager.getSignalStrength();
            if (coreLogicManager != null) {
                coreLogicManager.setSignalStrength(signalStrength);

                if (coreLogicManager.getMonster() != null) {
                    float enragement = coreLogicManager.getMonster().getEnragement();
                    uiManager.updateProxSensorEnragement(enragement);
                }
            }

            uiManager.updateProxSensor(monsterPos, playerOri);

            if (audioManager != null) {
                audioManager.updateWhiteNoise(monsterPos, playerOri, monsterDistanceToPlayer, signalStrength);
                float audioLevel = audioManager.getCurrentAudioLevel();
                uiManager.updateAudioLevelMeter(audioLevel);
            }

            if (hapticFeedbackManager != null) {
                hapticFeedbackManager.updateHaptics(monsterDistanceToPlayer, signalStrength);
            }
        }

        ARActivity.updateMonster(monsterPos, playerOri);
    }

    public boolean checkSystemSupport(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Toast.makeText(activity, "App does not support required Build Version", Toast.LENGTH_SHORT).show();
            activity.finish();
            return false;
        }

        String openGlVersion = ((ActivityManager) Objects.requireNonNull(activity.getSystemService(Context.ACTIVITY_SERVICE)))
                .getDeviceConfigurationInfo().getGlEsVersion();

        if (Double.parseDouble(openGlVersion) < 3.0) {
            Toast.makeText(activity, "App needs OpenGL Version 3.0 or later", Toast.LENGTH_SHORT).show();
            activity.finish();
            return false;
        }

        return true;
    }
}

