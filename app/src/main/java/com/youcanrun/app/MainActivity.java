package com.youcanrun.app;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.youcanrun.ar.ARSessionManager;
import com.youcanrun.audio.AudioManager;
import com.youcanrun.core.CoreLogicManager;
import com.youcanrun.core.GameEventListener;
import com.youcanrun.ui.UIManager;
import com.youcanrun.ui.R;

public class MainActivity extends AppCompatActivity implements GameEventListener {
    private static final String TAG = "MainActivity";

    private CoreLogicManager coreLogicManager;
    private UIManager uiManager;
    private AudioManager audioManager;
    private ARSessionManager arManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        coreLogicManager = new CoreLogicManager(this);
        coreLogicManager.setGameEventListener(this);

        uiManager = new UIManager(this);
        audioManager = new AudioManager(this);
        arManager = new ARSessionManager(this);

        // Toggle Dev HUD
        uiManager.setDevHudVisible(true);

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
        uiManager.updateDevHud(speed);
    }



}
