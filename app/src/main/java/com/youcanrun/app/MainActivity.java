package com.youcanrun.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.youcanrun.ar.ARSessionManager;
import com.youcanrun.audio.AudioManager;
import com.youcanrun.core.CoreLogicManager;
import com.youcanrun.core.GameEventListener;
import com.youcanrun.ui.UIManager;
import com.youcanrun.utils.Vector3;

import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.Objects;

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
        if(checkSystemSupport(this)) {
            arManager = new ARSessionManager(this);
            ArFragment arcam = (ArFragment) getSupportFragmentManager().findFragmentById(com.youcanrun.ui.R.id.arCameraArea);
            Anchor anchor = arcam.;
        }
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

    public static boolean checkSystemSupport(Activity activity) {

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
