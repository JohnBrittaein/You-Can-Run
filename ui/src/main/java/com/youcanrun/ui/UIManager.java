package com.youcanrun.ui;

import android.content.Context;
import android.util.Log;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.youcanrun.utils.Vector3;


/**
 * UIManager is responsible for all UI/Visual effects functionality
 * within the app. It manages the HUD, sensor displays (compass, speedometer, etc.),
 * and handles user interactions like the camera button.
 *
 * @date 11-07-2025
 */
public class UIManager {
    private static final String TAG = "UIManager";
    private Context context;
    public OdometerView odometerView;
    public SpeedometerView speedometerView;
    public CompassView compassView;
    public ProxSensorView proxSensorView;
    private int proxSensorTolerance = 2;
    private Button startBtn;
    private ImageButton quitBtn;
    private ImageButton cameraBtn;
    private boolean scanInit = false;
    private OnCameraButtonClickListener cameraButtonListener;
    private float lastPlayerAngle = 0f;
    private SignalSliderView signalStrengthSlider;
    private SignalStrengthView signalStrengthView;
    private com.youcanrun.audio.AudioManager audioManager;
    private OnGameStartListener gameStartListener;


    public interface OnCameraButtonClickListener {
        void onCameraButtonClicked();
    }

    public interface OnGameStartListener {
        void onGameStarted();
    }

    public UIManager(Context context) {
        this.context = context;
        setStartMenuVisible();
        showDHudBtn = startMenuView.findViewById(R.id.show_dev_hud_button);
        showDHudBtn.setOnClickListener(v -> {
            playClickSound();
            if (scanInit) {
                if (devHud.getVisibility() != View.VISIBLE) {
                    setDevHudVisible(true);
                } else {
                    setDevHudVisible(false);
                }
            }
        });
        startBtn = startMenuView.findViewById(R.id.start_button);
        startBtn.setOnClickListener(v -> {
            playClickSound();
            initHud();
            initDevHud();
            scanInit = true;
            startBtn.setVisibility(View.GONE);

            // Start the game
            if (gameStartListener != null) {
                gameStartListener.onGameStarted();
                Log.d(TAG, "Game start triggered from start button");
            }
        });
        Log.d(TAG, "UIManager initialized");
    }

    /**
     * Set the AudioManager instance for playing UI sounds
     */
    public void setAudioManager(com.youcanrun.audio.AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    /**
     * Set the game start listener
     */
    public void setOnGameStartListener(OnGameStartListener listener) {
        this.gameStartListener = listener;
    }

    /**
     * Play UI click sound if AudioManager is available
     */
    private void playClickSound() {
        Log.d(TAG, "playClickSound called - audioManager is " + (audioManager != null ? "available" : "null"));
        if (audioManager != null) {
            audioManager.playUIClick();
        } else {
            Log.w(TAG, "AudioManager is null, cannot play click sound");
        }
    }

    /**
     * Set the camera button click listener
     * This allows MainActivity to handle AR activity launch
     */
    public void setOnCameraButtonClickListener(OnCameraButtonClickListener listener) {
        this.cameraButtonListener = listener;
    }

    /**
     * Launch AR Activity directly from UIManager when camera button is clicked
     */
    public void launchARActivity() {
        if (!(context instanceof Activity)) return;
        Activity activity = (Activity) context;
        
        android.content.Intent intent = new android.content.Intent(context, com.youcanrun.ar.ARActivity.class);
        activity.startActivity(intent);
    }

    // TODO: Add methods to handle HUD, effects, etc.
    private View hud;
    private void initHud(){
        if (!(context instanceof Activity)) return;
        Activity activity = (Activity) context;

        ViewGroup root = activity.findViewById(android.R.id.content);
        hud = LayoutInflater.from(context).inflate(R.layout.hud, root, false);

        // Add it on top of existing layout
        root.addView(hud);

        // Reference OdometerView
        odometerView = hud.findViewById(R.id.OdometerView);

        // Reference SpeedometerView
        speedometerView = hud.findViewById(R.id.SpeedometerView);

        // Reference SpeedometerView
        compassView = hud.findViewById(R.id.CompassView);

        //Reference proxSensorView
        proxSensorView = hud.findViewById(R.id.ProxSensorView);

        // Reference SignalSliderView
        signalStrengthSlider = hud.findViewById(R.id.SignalSliderView);

        // Reference SignalStrengthView (audio level meter)
        signalStrengthView = hud.findViewById(R.id.SignalStrengthView);

        cameraBtn = hud.findViewById(R.id.camera_button);
        cameraBtn.setOnClickListener(v -> {
            playClickSound();
            // Launch AR Activity directly from UIManager
            launchARActivity();
        });

        quitBtn = hud.findViewById(R.id.quit_button);
        quitBtn.setOnClickListener(v -> {
            playClickSound();
            // TODO: implement method when the game ends, or quits
            // Send message to Quit the game
        });

        Log.d(TAG,"Hud Initialized");
    }
    public void updateHud(){}
    private View startMenuView;
    private Button showDHudBtn;
    public void setStartMenuVisible() {
        if (!(context instanceof Activity)) return;
        Activity activity = (Activity) context;

        //ViewGroup root = activity.findViewById(android.R.id.content);
        //startMenuView = LayoutInflater.from(context).inflate(R.layout.start_menu, root, false);

        startMenuView = LayoutInflater.from(context).inflate(R.layout.start_menu, null);
        activity.setContentView(startMenuView);
    }

    public void hideStartMenu(Activity activity, int mainLayoutResId) {
        activity.setContentView(mainLayoutResId);
    }

    /** ------------------ DEV HUD FUNCTIONS HERE ------------------**/
    private View devHud;
    private TextView devSpeedTextView;
    private TextView devDirectionTextView;
    private DevMapView devMapView;
    private TextView devDistanceTextView;
    private TextView devDeltaTextView;
    private TextView devPlayerDistanceTextView;

    private void initDevHud(){
        if (!(context instanceof Activity)) return;
        Activity activity = (Activity) context;

        ViewGroup root = activity.findViewById(android.R.id.content);
        devHud = LayoutInflater.from(context).inflate(R.layout.dev_hud, root, false);

        // Add it on top of existing layout
        root.addView(devHud);

        // Get references to TextViews
        devSpeedTextView = devHud.findViewById(R.id.devSpeedTextView);
        devDirectionTextView = devHud.findViewById(R.id.devDirectionTextView);
        devMapView = devHud.findViewById(R.id.devMapView);
        devDistanceTextView = devHud.findViewById(R.id.devMonsterDistanceTextView);
        devPlayerDistanceTextView = devHud.findViewById(R.id.devPlayerDistanceTextView);
        devDeltaTextView = devHud.findViewById(R.id.devDeltaTextView);
        Log.d(TAG,"Dev Hud Initialized");
    }

    public void updateDevHudSpeed(float speed) {
        // Update speed
        if (devSpeedTextView != null) {
            ((Activity) context).runOnUiThread(() -> {
                devSpeedTextView.setText(String.format("Speed: %.2f m/s", speed));
            });
        }
    }

    public void updateDevHudPlayerDistance(float distance){
        Log.i("Distance", "updateDevHudPlayerDistance: " + distance);
        if (devPlayerDistanceTextView != null) {
            ((Activity) context).runOnUiThread(() -> {
                if(distance < 1000f) {
                    devPlayerDistanceTextView.setText(String.format("Player Distance: %.2f m", distance));
                }
                else{
                    devPlayerDistanceTextView.setText(String.format("Player Distance: %.2f km", distance/1000));
                }
            });
        }
    }

    public void updateOdometer(float distance){
        Log.i("Distance", "updateOdometer: " + distance);
        if (odometerView != null) {
            ((Activity) context).runOnUiThread(() -> {
                    odometerView.setDistance(distance);
            });
        }
    }

    public void updateSpeedometer(float speed){
        Log.i("Speed", "updateSpeedometer: " + speed);
        if (speedometerView != null) {
            ((Activity) context).runOnUiThread(() -> {
                speedometerView.setSpeed(speed);
            });
        }
    }

    public void updateCompass(Vector3 delta){
        // Update delta
        if(delta == null) return;

        if(compassView != null){
            ((Activity) context).runOnUiThread(() -> {
                String deltaText = String.format(
                        "Delta: X: %.3f, Y: %.3f, Z: %.3f",
                        delta.x, delta.y, delta.z
                );
                compassView.setCompassDirection(delta);
            });
        }
    }

    public void updateProxSensor(final Vector3 mPos, final Vector3 pDir){
        if (proxSensorView != null && mPos != null) {
            float pDiff = proxSensorView.POStoAngle(pDir);
            if( !( (pDiff < (lastPlayerAngle+proxSensorTolerance)) && (pDiff > (lastPlayerAngle-proxSensorTolerance))) ) {
                proxSensorView.updateProxSensor(mPos, pDir);

                if (signalStrengthSlider != null) {
                    proxSensorView.setSignalStrength(signalStrengthSlider.getFillFraction());
                }

                lastPlayerAngle = pDiff;
            }
        }
    }

    public void updateProxSensorEnragement(float enragement) {
        if (proxSensorView != null) {
            proxSensorView.setEnragement(enragement);
        }
    }

    public float getSignalStrength() {
        if (signalStrengthSlider != null) {
            return signalStrengthSlider.getFillFraction();
        }
        return 0.5f; // Default value
    }

    /**
     * Update the audio level meter with current audio level
     * @param audioLevel Audio level from 0.0 to 1.0
     */
    public void updateAudioLevelMeter(float audioLevel) {
        if (signalStrengthView != null) {
            ((Activity) context).runOnUiThread(() -> {
                // Convert 0.0-1.0 to 0-10 bars
                int bars = Math.round(audioLevel * 10);
                signalStrengthView.setSignalStrength(bars);
            });
        }
    }

    public void updateDevHudDirection(Vector3 delta){
        // Update delta
        if(delta == null) return;

        if(devDirectionTextView != null){
            ((Activity) context).runOnUiThread(() -> {
                String deltaText = String.format(
                        "Delta: X: %.3f, Y: %.3f, Z: %.3f",
                        delta.x, delta.y, delta.z
                );
                devDirectionTextView.setText(deltaText);
            });
        }
        if(devDeltaTextView != null){
            devDeltaTextView.setText("Delta: " + proxSensorView.getDelta());
        }
    }

    public void updateDevHudMapView(final Vector3 mPos, final Vector3 pDir){
        if (devMapView != null && mPos != null) {
            ((Activity) context).runOnUiThread(() -> {
                devMapView.setMonsterPosition(mPos);
                devMapView.setPlayerDirection(pDir);
            });
        }
    }

    public void updateDevHudDistance(final float distance){
        if(devDistanceTextView != null){
            ((Activity) context).runOnUiThread(() -> {
                devDistanceTextView.setText(String.format("Distance: %.2f m", distance));

            });
        }

    }

    public void setDevHudVisible(final boolean visible) {
        if (devHud != null) {
            ((Activity) context).runOnUiThread(() -> {
                devHud.setVisibility(visible ? View.VISIBLE : View.GONE);
            });
        }
    }
}
