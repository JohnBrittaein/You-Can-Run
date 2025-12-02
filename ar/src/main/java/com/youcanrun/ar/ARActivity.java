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

    // Static reference for MainActivity to update monster position
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

        Log.i(TAG, "ARActivity started with filter controls");
    }

    private void setupUI() {
        currentFilterText = findViewById(R.id.txtCurrentFilter);

        // Setup filter buttons
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

        // Setup back button
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked, finishing ARActivity");
            finish();
        });
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

    /**
     * Update monster position from game logic (called by MainActivity)
     * @param monsterPos Monster position in world-space (relative to player at origin)
     * @param playerDir Player direction vector (not used - camera pose handles rotation)
     */
    public void updateMonsterPosition(com.youcanrun.utils.Vector3 monsterPos, com.youcanrun.utils.Vector3 playerDir) {
        Log.d(TAG, "updateMonsterPosition called, cameraFragment=" + (cameraFragment != null) +
            ", renderer=" + (cameraFragment != null ? (cameraFragment.getRenderer() != null) : "N/A"));

        if (cameraFragment != null && cameraFragment.getRenderer() != null) {
            // Pass world-space coordinates directly to renderer
            // The MonsterRenderer will use ARCore camera pose to transform to camera-space
            // This accounts for full 3D rotation (yaw, pitch, roll) automatically

            Log.d(TAG, String.format("Monster world position: (%.1f, %.1f, %.1f)",
                monsterPos.x, monsterPos.y, monsterPos.z));

            cameraFragment.getRenderer().setMonsterPosition(monsterPos.x, monsterPos.y, monsterPos.z);
        } else {
            Log.w(TAG, "Cannot update monster position - fragment or renderer is null");
        }
    }

    /**
     * Static method for MainActivity to update monster position
     * Safe to call even when ARActivity is not active
     */
    public static void updateMonster(com.youcanrun.utils.Vector3 monsterPos, com.youcanrun.utils.Vector3 playerDir) {
        // Capture instance in local variable to avoid race condition
        ARActivity activity = instance;
        if (activity != null) {
            activity.runOnUiThread(() -> activity.updateMonsterPosition(monsterPos, playerDir));
        }
        // If instance is null, AR view is not open - silently ignore
    }
}
