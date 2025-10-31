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
import com.youcanrun.sensors.MotionTracker;
import com.youcanrun.ui.UIManager;
import com.youcanrun.ui.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private CoreLogicManager coreLogicManager;
    private MotionTracker motionTracker;
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

        coreLogicManager = new CoreLogicManager();
        motionTracker = new MotionTracker(this);
        uiManager = new UIManager(this);
        audioManager = new AudioManager(this);
        arManager = new ARSessionManager(this);

        Log.d(TAG, "All modules initialized");
    }

}
