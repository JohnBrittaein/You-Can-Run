package com.youcanrun.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

import com.youcanrun.utils.Vector3;

import java.util.Random;

/**
 * AudioManager handles all audio playback for the game including:
 * - UI click sounds (random selection from 7 variants)
 * - Ambient white noise with dynamic volume and stereo panning based on monster position
 *
 * @date 12-04-2025
 */
public class AudioManager {
    private static final String TAG = "AudioManager";
    private final Context context;

    // SoundPool for short UI click sounds
    private SoundPool soundPool;
    private int[] uiClickSounds;
    private final Random random;
    private boolean soundsLoaded = false;
    private int loadedCount = 0;

    // MediaPlayer for looping white noise
    private MediaPlayer whiteNoisePlayer;
    private boolean isWhiteNoisePlaying = false;

    // Audio parameters
    private static final float MAX_DISTANCE = 200f;
    private static final float MIN_VOLUME = 0.001f;
    private static final float MAX_VOLUME = 1.0f;

    // Current audio level (for level meter display)
    private float currentAudioLevel = 0f;

    public AudioManager(Context context) {
        this.context = context;
        this.random = new Random();

        initSoundPool();
        initWhiteNoise();

        Log.d(TAG, "AudioManager initialized");
    }

    /**
     * Initialize SoundPool and load all UI click sounds
     */
    private void initSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        // Set load complete listener
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                loadedCount++;
                Log.d(TAG, "Sound loaded successfully: " + sampleId + " (" + loadedCount + "/7)");
                if (loadedCount == 7) {
                    soundsLoaded = true;
                    Log.d(TAG, "All UI sounds loaded!");
                }
            } else {
                Log.e(TAG, "Failed to load sound: " + sampleId);
            }
        });

        // Load all 7 UI click sounds
        uiClickSounds = new int[7];
        String packageName = context.getPackageName();
        Log.d(TAG, "Loading sounds from package: " + packageName);

        for (int i = 0; i < 7; i++) {
            String soundName = "uiclick_" + (i + 1);

            int resId = context.getResources().getIdentifier(
                soundName,
                "raw",
                packageName
            );

            if (resId != 0) {
                uiClickSounds[i] = soundPool.load(context, resId, 1);
                Log.d(TAG, "Loading " + soundName + " with resId: " + resId);
            } else {
                Log.e(TAG, "Failed to find resource: " + soundName);
            }
        }

        Log.d(TAG, "SoundPool initialization started");
    }

    /**
     * Initialize MediaPlayer for white noise
     */
    private void initWhiteNoise() {
        String packageName = context.getPackageName();
        int resId = context.getResources().getIdentifier(
            "scary_white_noise",
            "raw",
            packageName
        );

        Log.d(TAG, "Looking for scary_white_noise in package: " + packageName);

        if (resId != 0) {
            Log.d(TAG, "Found scary_white_noise with resId: " + resId);
            whiteNoisePlayer = MediaPlayer.create(context, resId);
            if (whiteNoisePlayer != null) {
                whiteNoisePlayer.setLooping(true);
                whiteNoisePlayer.setVolume(0f, 0f);
                Log.d(TAG, "White noise MediaPlayer initialized successfully");
            } else {
                Log.e(TAG, "Failed to create white noise MediaPlayer");
            }
        } else {
            Log.e(TAG, "Failed to find scary_white_noise resource");
        }
    }

    /**
     * Play a random UI click sound
     */
    public void playUIClick() {
        Log.d(TAG, "playUIClick called - soundsLoaded: " + soundsLoaded);

        if (soundPool == null) {
            Log.e(TAG, "Cannot play click - soundPool is null");
            return;
        }

        if (uiClickSounds == null) {
            Log.e(TAG, "Cannot play click - uiClickSounds array is null");
            return;
        }

        if (!soundsLoaded) {
            Log.w(TAG, "Sounds not fully loaded yet (" + loadedCount + "/7), attempting to play anyway");
        }

        int randomIndex = random.nextInt(7);
        int soundId = uiClickSounds[randomIndex];

        if (soundId == 0) {
            Log.e(TAG, "Sound ID is 0 for index " + randomIndex);
            return;
        }

        int streamId = soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
        Log.d(TAG, "Playing UI click sound " + (randomIndex + 1) + " (soundId: " + soundId + ", streamId: " + streamId + ")");
    }

    /**
     * Start playing the white noise loop
     */
    public void startWhiteNoise() {
        if (whiteNoisePlayer != null && !isWhiteNoisePlaying) {
            whiteNoisePlayer.start();
            isWhiteNoisePlaying = true;
            Log.d(TAG, "White noise started");
        }
    }

    /**
     * Stop the white noise
     */
    public void stopWhiteNoise() {
        if (whiteNoisePlayer != null && isWhiteNoisePlaying) {
            whiteNoisePlayer.pause();
            whiteNoisePlayer.seekTo(0);
            isWhiteNoisePlaying = false;
            Log.d(TAG, "White noise stopped");
        }
    }

    /**
     * Update white noise volume and stereo panning based on monster position
     *
     * @param monsterPos Monster position in 3D space
     * @param playerDir Player's facing direction
     * @param distance Distance from monster to player
     * @param signalStrength Signal strength from UI slider (0.0 to 1.0)
     */
    public void updateWhiteNoise(Vector3 monsterPos, Vector3 playerDir, float distance, float signalStrength) {
        if (whiteNoisePlayer == null) return;

        // Calculate volume based on distance (closer = louder)
        float distanceFactor = Math.max(0f, 1f - (distance / MAX_DISTANCE));
        float baseVolume = MIN_VOLUME + (distanceFactor * (MAX_VOLUME - MIN_VOLUME));

        // Calculate directional factor (louder when looking at monster, quieter when looking away)
        float directionalFactor = calculateDirectionalFactor(monsterPos, playerDir);

        // Apply signal strength and directional multipliers
        float volume = baseVolume * signalStrength * directionalFactor;
        volume = Math.max(MIN_VOLUME, Math.min(MAX_VOLUME, volume));

        // Calculate stereo panning based on monster's relative position
        float pan = calculateStereoPan(monsterPos, playerDir);

        float leftVolume = volume * (1.0f - Math.max(0f, pan));
        float rightVolume = volume * (1.0f + Math.min(0f, pan));

        whiteNoisePlayer.setVolume(leftVolume, rightVolume);

        // Store current audio level (average of left and right for meter display)
        currentAudioLevel = (leftVolume + rightVolume) / 2.0f;

        // Log.d(TAG, String.format("White noise - Distance: %.2f, Dir: %.2f, Vol: %.2f, Pan: %.2f", distance, directionalFactor, volume, pan));
    }

    /**
     * Get the current audio level (0.0 to 1.0) for display on level meter
     */
    public float getCurrentAudioLevel() {
        return currentAudioLevel;
    }

    /**
     * Calculate directional factor based on how directly the player is facing the monster
     * Returns 0.1 (looking away) to 1.0 (looking directly at monster)
     */
    private float calculateDirectionalFactor(Vector3 monsterPos, Vector3 playerDir) {
        // Normalize monster position (direction to monster)
        float monsterLength = (float) Math.sqrt(
            monsterPos.x * monsterPos.x +
            monsterPos.y * monsterPos.y +
            monsterPos.z * monsterPos.z
        );

        if (monsterLength < 0.01f) return 1.0f;

        Vector3 toMonster = new Vector3(
            monsterPos.x / monsterLength,
            monsterPos.y / monsterLength,
            monsterPos.z / monsterLength
        );

        // Normalize player direction
        float playerLength = (float) Math.sqrt(
            playerDir.x * playerDir.x +
            playerDir.y * playerDir.y +
            playerDir.z * playerDir.z
        );

        if (playerLength < 0.01f) return 1.0f;

        Vector3 forward = new Vector3(
            playerDir.x / playerLength,
            playerDir.y / playerLength,
            playerDir.z / playerLength
        );

        // Dot product gives cosine of angle between directions
        // 1.0 = looking directly at monster, -1.0 = looking directly away
        float dot = toMonster.x * forward.x + toMonster.y * forward.y + toMonster.z * forward.z;

        // Looking directly at monster 100% volume
        // Looking perpendicular 55% volume
        // Looking away 10% volume
        float directionalFactor = 0.01f + (dot + 1.0f) * 0.45f;

        return Math.max(0.01f, Math.min(1.0f, directionalFactor));
    }

    /**
     * Calculate stereo panning based on monster position relative to player direction
     * Returns -1.0 (left) to 1.0 (right)
     */
    private float calculateStereoPan(Vector3 monsterPos, Vector3 playerDir) {
        float monsterLength = (float) Math.sqrt(
            monsterPos.x * monsterPos.x +
            monsterPos.y * monsterPos.y +
            monsterPos.z * monsterPos.z
        );

        if (monsterLength < 0.01f) return 0f;

        Vector3 toMonster = new Vector3(
            monsterPos.x / monsterLength,
            monsterPos.y / monsterLength,
            monsterPos.z / monsterLength
        );

        // Normalize player direction
        float playerLength = (float) Math.sqrt(
            playerDir.x * playerDir.x +
            playerDir.y * playerDir.y +
            playerDir.z * playerDir.z
        );

        if (playerLength < 0.01f) return 0f;

        Vector3 forward = new Vector3(
            playerDir.x / playerLength,
            playerDir.y / playerLength,
            playerDir.z / playerLength
        );

        // Calculate right vector (perpendicular to forward in XY plane)
        // Using cross product: forward × up = right
        // For forward = (fx, fy, fz) and up = (0, 0, 1): right = (fy, -fx, 0)
        Vector3 right = new Vector3(forward.y, -forward.x, 0);
        float rightLength = (float) Math.sqrt(right.x * right.x + right.y * right.y);

        if (rightLength > 0.01f) {
            right.x /= rightLength;
            right.y /= rightLength;
        }

        float pan = toMonster.x * right.x + toMonster.y * right.y;

        return Math.max(-1f, Math.min(1f, pan));
    }

    /**
     * Release all audio resources
     */
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
            Log.d(TAG, "SoundPool released");
        }

        if (whiteNoisePlayer != null) {
            if (isWhiteNoisePlaying) {
                whiteNoisePlayer.stop();
            }
            whiteNoisePlayer.release();
            whiteNoisePlayer = null;
            isWhiteNoisePlaying = false;
            Log.d(TAG, "White noise MediaPlayer released");
        }
    }
}
