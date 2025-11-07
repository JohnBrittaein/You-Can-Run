package com.youcanrun.ui;

import android.content.Context;
import android.util.Log;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.youcanrun.utils.Vector3;


/**
 *
 */
public class UIManager {
    private static final String TAG = "UIManager";
    private Context context;

    // TODO: Implement HUD, glitch effects, and other UI elements

    public UIManager(Context context) {
        this.context = context;
        initDevHud();
        Log.d(TAG, "UIManager initialized");
    }

    // TODO: Add methods to handle HUD, effects, etc.
    public void initHud(){}
    public void updateHud(){}

    /** ------------------ DEV HUD FUNCTIONS HERE ------------------**/
    private View devHud;
    private TextView devSpeedTextView;
    private TextView devDeltaTextView;

    private void initDevHud(){
        if (!(context instanceof Activity)) return;
        Activity activity = (Activity) context;

        ViewGroup root = activity.findViewById(android.R.id.content);
        devHud = LayoutInflater.from(context).inflate(R.layout.dev_hud, root, false);

        // Add it on top of existing layout
        root.addView(devHud);

        // Get references to TextViews
        devSpeedTextView = devHud.findViewById(R.id.devSpeedTextView);
        devDeltaTextView = devHud.findViewById(R.id.devDeltaTextView);
        Log.d(TAG,"Dev Hud Initialized");
    }

    public void updateDevHudSpeed(float speed) {
        // Update speed
        if (devSpeedTextView != null) {
            devSpeedTextView.setText(String.format("Speed: %.2f m/s", speed));
        }
    }

    public void updateDevHudDelta(Vector3 delta){
        // Update delta
        if(delta == null) return;

        if(devDeltaTextView != null){
            String deltaText = String.format(
                    "Delta: X: %.3f, Y: %.3f, Z: %.3f",
                    delta.x, delta.y, delta.z
            );
            devDeltaTextView.setText(deltaText);
        }
    }

    public void setDevHudVisible(boolean visible) {
        if (devHud != null) {
            devHud.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }


}
