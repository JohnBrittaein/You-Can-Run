package com.youcanrun.ui;

import android.content.Context;
import android.util.Log;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


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
    private TextView devSpeedTextView;

    private void initDevHud(){
        if (!(context instanceof Activity)) return;
        Activity activity = (Activity) context;

        ViewGroup root = activity.findViewById(android.R.id.content);
        View hud = LayoutInflater.from(context).inflate(R.layout.dev_hud, root, false);

        // Add it on top of existing layout
        root.addView(hud);

        // Get references to TextViews
        devSpeedTextView = hud.findViewById(R.id.devSpeedTextView);
        Log.d(TAG,"Dev Hud Initialized");
    }

    public void updateDevHud(float speed) {
        // Update speed
        if (devSpeedTextView != null) {
            devSpeedTextView.setText(String.format("Speed: %.2f m/s", speed));
        }
    }

    public void setDevHudVisible(boolean visible) {
        if (devSpeedTextView != null && devSpeedTextView.getParent() instanceof View) {
            ((View)devSpeedTextView.getParent()).setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }


}
