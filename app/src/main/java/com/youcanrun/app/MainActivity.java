package com.youcanrun.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.youcanrun.audio.AudioManager;
import com.youcanrun.core.CoreLogicManager;
import com.youcanrun.core.GameEventListener;
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
 * @date 11-07-2025
 */
public class MainActivity extends AppCompatActivity implements GameEventListener {
    private static final String TAG = "MainActivity";

    private CoreLogicManager coreLogicManager;
    private UIManager uiManager;
    private AudioManager audioManager;

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

        audioManager = new AudioManager(this);

        Log.d(TAG, "All modules initialized (AR is optional via camera button)");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (coreLogicManager != null) {
            coreLogicManager.resumeGame();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Don't pause the game - it needs to keep running when AR view is open
        // to continue tracking movement and updating monster position
        // if (coreLogicManager != null) {
        //     coreLogicManager.pauseGame();
        // }
    }

    @Override
    public void onSpeedChanged(float speed) {
        if (uiManager != null) {
            uiManager.updateDevHudSpeed(speed);
        }
    }

    @Override
    public void onPlayerDistanceChanged(float distance) {
        if (uiManager != null) {
            uiManager.updateDevHudPlayerDistance(distance);

        }
    }

    @Override
    public void onPlayerDirectionChanged(Vector3 direction) {
        if (uiManager != null) {
            uiManager.updateDevHudDirection(direction);
        }
    }

    @Override
    public void onMapPositionsChanged(Vector3 monsterPos, Vector3 playerOri, float monsterDistanceToPlayer) {
        if (uiManager != null) {
            uiManager.updateDevHudMapView(monsterPos, playerOri);
            uiManager.updateDevHudDistance(monsterDistanceToPlayer);
        }

        // Update AR view if it's active (pass both monster position and player orientation)
        com.youcanrun.ar.ARActivity.updateMonster(monsterPos, playerOri);
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

