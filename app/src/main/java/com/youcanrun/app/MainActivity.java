package com.youcanrun.app;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.youcanrun.ar.ARSessionManager;
import com.youcanrun.audio.AudioManager;
import com.youcanrun.core.CoreLogicManager;
import com.youcanrun.core.GameEventListener;
import com.youcanrun.ui.UIManager;
import com.youcanrun.utils.Vector3;

/**
 * The MainActivity serves as the orchestrator between all of the other modules
 * :core, :sensors, :ui, :audio, :ar
 * The role of the MainActivity is to initialize modules, implement listeners, pass data,
 * and handle app lifecycles. Other than this, the MainActivity has NO other
 * functional use.
 *
 * @date 11-07-2025
 */
public class MainActivity extends AppCompatActivity implements GameEventListener {
    private static final String TAG = "MainActivity";

    private CoreLogicManager coreLogicManager;
    private UIManager uiManager;
    private AudioManager audioManager;
    private ARSessionManager arManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        coreLogicManager = new CoreLogicManager(this);
        coreLogicManager.setGameEventListener(this);

        uiManager = new UIManager(this);
        audioManager = new AudioManager(this);
        arManager = new ARSessionManager(this);

        Log.d(TAG, "All modules initialized");
    }

    @Override
    protected void onResume() {
        super.onResume();
        coreLogicManager.resumeGame();
    }

    @Override
    protected void onPause() {
        super.onPause();
        coreLogicManager.pauseGame();
    }

    @Override
    public void onSpeedChanged(float speed) {
        uiManager.updateDevHudSpeed(speed);
    }

    @Override
    public void onPlayerDirectionChanged(Vector3 direction) { uiManager.updateDevHudDirection(direction);}

    @Override
    public void onMapPositionsChanged(Vector3 monsterPos, Vector3 playerOri, float monsterDistanceToPlayer){
        uiManager.updateDevHudMapView(monsterPos, playerOri);
        uiManager.updateDevHudDistance(monsterDistanceToPlayer);
    }
}
