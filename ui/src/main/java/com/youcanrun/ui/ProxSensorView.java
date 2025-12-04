package com.youcanrun.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.youcanrun.utils.Vector3;

public class ProxSensorView extends View {

    Paint paint = new Paint();
    private float delta;
    private float playerDirection;
    private float distance;
    private float animationTime = 0f;
    private boolean isAnimating = false;
    private float signalStrength = 0.5f;
    private float enragement = 1.0f;

    public ProxSensorView(Context context) {
        super(context);
        init();
    }

    public ProxSensorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProxSensorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }
    public void updateProxSensor(Vector3 monsterPosition, Vector3 playerPosition){
        float monsterDirection = POStoAngle(monsterPosition);
        playerDirection = POStoAngle(playerPosition);

        // Calculate distance between player and monster
        float dx = monsterPosition.x - playerPosition.x;
        float dz = monsterPosition.z - playerPosition.z;
        distance = (float) Math.sqrt(dx * dx + dz * dz);

        float diff = 0f;

        if ((Math.max(playerDirection,monsterDirection) - Math.min(playerDirection,monsterDirection)) > 180){
           diff = 360 - (Math.max(playerDirection,monsterDirection) - Math.min(playerDirection,monsterDirection));
        }
        else{
            diff = Math.max(playerDirection,monsterDirection) - Math.min(playerDirection,monsterDirection);
        }

        // Delta represents how much the player is looking at the monster, from 0 - 1.
        delta = (180 - diff) / 180f;

        // Start animation
        if (!isAnimating) {
            isAnimating = true;
            startAnimation();
        }

        Log.i("ProxSensor", "Direction diff: " + diff + ", delta: " + delta + ", distance: " + distance);
    }

    public float POStoAngle(Vector3 POS){
        float angle = 0f;
        if (POS.x < 0){//Quadrant 2 && 3
            angle = (float) (180f+(90f * (Math.atan(POS.z / POS.x)/1.57079632679)));
        }
        else if (POS.x > 0 && POS.z > 0){//Quadrant 1
            angle = (float) (90f * (Math.atan(POS.z / POS.x)/1.57079632679));
        }
        else if (POS.x > 0 && POS.z < 0){//Quadrant 4
            angle = (float) (360f+(90*(Math.atan(POS.z / POS.x)/1.57079632679)));
        }
        else if(POS.x == 1){
            angle = 0;
        }
        else if(POS.z == 1){
            angle = 90;
        }
        else if(POS.z == -1){
            angle = 270;
        }

        return angle;
    }

    public float getDelta(){
        return delta;
    }

    public void setSignalStrength(float strength) {
        this.signalStrength = Math.max(0f, Math.min(strength, 1f));
    }

    public void setEnragement(float enragement) {
        this.enragement = enragement;
    }

    private void startAnimation() {
        post(new Runnable() {
            @Override
            public void run() {
                if (isAnimating) {
                    animationTime += 0.15f; // Increased from 0.05f for faster animation
                    invalidate();
                    postDelayed(this, 16);
                }
            }
        });
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Vibrant orange neon color
        paint.setColor(Color.rgb(255, 149, 0));
        paint.setStrokeWidth(2f);

        float centerY = getHeight() / 2f;
        float maxAmplitude = getHeight() / 2.5f;

        // Calculate proximity factor (closer = higher value)
        float normalizedDistance = Math.min(1f, distance / 160f);
        float proximityFactor = (float) Math.pow(1f - normalizedDistance, 3.0);

        // Combined signal intensity: direction * proximity * slider strength * enragement
        float baseIntensity = delta * proximityFactor;

        // Signal strength from slider boosts magnitude
        float magnitudeBoost = 1f + (signalStrength * 2f); // 1x to 3x multiplier

        // Enragement increases chaos and speed
        float chaosMultiplier = enragement;

        // When very close (< 10 meters), extreme boost
        if (distance < 10f) {
            baseIntensity *= (1f + (10f - distance) / 2f);
        }

        float totalIntensity = baseIntensity * magnitudeBoost;

        // Draw the waveform
        float prevX = 0;
        float prevY = centerY;

        for (int i = 0; i < getWidth(); i++) {
            float normalizedX = (i - (getWidth() / 2f)) / (getWidth() / 10f);

            // Pseudo-random seed based on position for irregular patterns
            float seed1 = (float) Math.sin(normalizedX * 17.3f) * 1000f;
            float seed2 = (float) Math.sin(normalizedX * 31.7f) * 1000f;

            // Base carrier with enragement-affected speed
            float baseWave = (float) Math.sin(normalizedX * 0.8f + animationTime * 2f * chaosMultiplier);

            // Irregular noise components - mix sine, triangle-like, and pseudo-random
            float noise1 = (float) Math.sin(normalizedX * 5.0f + animationTime * 4.0f * chaosMultiplier);
            float noise2 = (float) Math.sin(normalizedX * 12.0f - animationTime * 3.5f * chaosMultiplier);

            // Triangle/sawtooth-like waves for sharper transitions
            float noise3 = (float) (Math.asin(Math.sin(normalizedX * 20.0f + animationTime * 5.0f * chaosMultiplier)) / 1.57);
            float noise4 = (float) (Math.asin(Math.sin(normalizedX * 35.0f - animationTime * 4.5f * chaosMultiplier)) / 1.57);

            // Pseudo-random bumps using nested sine functions
            float chaotic1 = (float) Math.sin(seed1 + animationTime * 7.0f * chaosMultiplier);
            float chaotic2 = (float) Math.sin(seed2 - animationTime * 5.5f * chaosMultiplier);
            float chaotic3 = (float) Math.sin(normalizedX * 50.0f + chaotic1 * 3f + animationTime * 6.0f * chaosMultiplier);

            // Sharp spikes using absolute values and powers
            float spike1 = (float) Math.pow(Math.abs(Math.sin(normalizedX * 8.0f + animationTime * 3.0f * chaosMultiplier)), 0.3);
            float spike2 = (float) Math.pow(Math.abs(Math.sin(normalizedX * 15.0f - animationTime * 4.0f * chaosMultiplier)), 0.5);

            // Combine all noise sources with varying amplitudes
            float smoothNoise = (noise1 * 0.4f + noise2 * 0.35f);
            float sharpNoise = (noise3 * 0.25f + noise4 * 0.2f);
            float chaoticNoise = (chaotic1 * 0.15f + chaotic2 * 0.15f + chaotic3 * 0.25f);
            float spikeNoise = ((spike1 - 0.5f) * 0.3f + (spike2 - 0.5f) * 0.2f);

            // Weight different noise types based on intensity and enragement
            float enragementWeight = Math.min(enragement, 3f) / 3f; // 0-1 range
            float combinedNoise = smoothNoise * (1f - enragementWeight * 0.5f)
                                + sharpNoise * 0.8f
                                + chaoticNoise * enragementWeight
                                + spikeNoise * enragementWeight;

            // Apply total intensity
            float noiseFactor = (float) Math.pow(totalIntensity, 2.5);
            float noiseAmplitude = combinedNoise * noiseFactor;

            // Base amplitude
            float baseAmplitude = totalIntensity * 0.6f;

            // Final wave calculation
            float wave = (baseWave * baseAmplitude + noiseAmplitude) * maxAmplitude;

            float y = centerY + wave;

            // Draw line connecting points
            if (i > 0) {
                canvas.drawLine(prevX, prevY, i, y, paint);
            }

            prevX = i;
            prevY = y;
        }

        // Draw centerline for reference
        paint.setColor(Color.argb(50, 255, 149, 0));
        paint.setStrokeWidth(1f);
        canvas.drawLine(0, centerY, getWidth(), centerY, paint);
    }
}
