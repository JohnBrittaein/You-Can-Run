package com.youcanrun.ar;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/**
 * ARActivity - AR camera view with filter controls
 */
public class ARActivity extends AppCompatActivity {
    private static final String TAG = "ARActivity";
    private CameraFragment cameraFragment;
    private TextView currentFilterText;
    private static ARActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        instance = this;

        // Get the camera fragment from layout
        Fragment fragment = getSupportFragmentManager()
                .findFragmentById(R.id.arCameraFragment);

        if (!(fragment instanceof CameraFragment)) {
            Log.e(TAG, "CameraFragment not found in layout");
            finish();
            return;
        }

        cameraFragment = (CameraFragment) fragment;
        setupUI();
    }

    private void setupUI() {
        currentFilterText = findViewById(R.id.txtCurrentFilter);

        findViewById(R.id.btnFilterNormal).setOnClickListener(v -> {
            cameraFragment.setFilter(CameraFilter.NORMAL);
            currentFilterText.setText("[ OPTICAL ]");
        });

        findViewById(R.id.btnFilterPredator).setOnClickListener(v -> {
            cameraFragment.setFilter(CameraFilter.PREDATOR);
            currentFilterText.setText("[ HEAT ]");
        });

        findViewById(R.id.btnFilterEdge).setOnClickListener(v -> {
            cameraFragment.setFilter(CameraFilter.EDGE_DETECT);
            currentFilterText.setText("[ MOTION ]");
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        instance = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    public void updateMonsterPosition(com.youcanrun.utils.Vector3 monsterPos) {
        if (cameraFragment != null && cameraFragment.getRenderer() != null) {
            cameraFragment.getRenderer().setMonsterPosition(monsterPos.x, monsterPos.y, monsterPos.z);
        }
    }

    public static void updateMonster(com.youcanrun.utils.Vector3 monsterPos, com.youcanrun.utils.Vector3 playerDir) {
        ARActivity activity = instance;
        if (activity != null) {
            activity.runOnUiThread(() -> activity.updateMonsterPosition(monsterPos));
        }
    }
}
