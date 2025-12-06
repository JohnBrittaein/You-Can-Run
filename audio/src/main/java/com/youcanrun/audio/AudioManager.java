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
 */
public class AudioManager {
    private static final String TAG = "AudioManager";
    private static final float MAX_DISTANCE = 200f;
    private static final float MIN_VOLUME = 0.001f;
    private static final float MAX_VOLUME = 1.0f;

    private final Context context;
    private SoundPool soundPool;
    private int[] uiClickSounds;
    private final Random random;
    private boolean soundsLoaded = false;
    private int loadedCount = 0;
    private MediaPlayer whiteNoisePlayer;
    private boolean isWhiteNoisePlaying = false;
    private float currentAudioLevel = 0f;

    public AudioManager(Context context) {
        this.context = context;
        this.random = new Random();
        initSoundPool();
        initWhiteNoise();
    }

    private void initSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                loadedCount++;
                if (loadedCount == 7) {
                    soundsLoaded = true;
                }
            }
        });

        uiClickSounds = new int[7];
        String packageName = context.getPackageName();

        for (int i = 0; i < 7; i++) {
            String soundName = "uiclick_" + (i + 1);
            int resId = context.getResources().getIdentifier(soundName, "raw", packageName);
            if (resId != 0) {
                uiClickSounds[i] = soundPool.load(context, resId, 1);
            }
        }
    }

    private void initWhiteNoise() {
        String packageName = context.getPackageName();
        int resId = context.getResources().getIdentifier("scary_white_noise", "raw", packageName);

        if (resId != 0) {
            whiteNoisePlayer = MediaPlayer.create(context, resId);
            if (whiteNoisePlayer != null) {
                whiteNoisePlayer.setLooping(true);
                whiteNoisePlayer.setVolume(0f, 0f);
            }
        }
    }

    public void playUIClick() {
        if (soundPool == null || uiClickSounds == null) return;

        int randomIndex = random.nextInt(7);
        int soundId = uiClickSounds[randomIndex];
        if (soundId != 0) {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    public void startWhiteNoise() {
        if (whiteNoisePlayer != null && !isWhiteNoisePlaying) {
            whiteNoisePlayer.start();
            isWhiteNoisePlaying = true;
        }
    }

    public void stopWhiteNoise() {
        if (whiteNoisePlayer != null && isWhiteNoisePlaying) {
            whiteNoisePlayer.pause();
            whiteNoisePlayer.seekTo(0);
            isWhiteNoisePlaying = false;
        }
    }

    public void updateWhiteNoise(Vector3 monsterPos, Vector3 playerDir, float distance, float signalStrength) {
        if (whiteNoisePlayer == null) return;

        float distanceFactor = Math.max(0f, 1f - (distance / MAX_DISTANCE));
        float baseVolume = MIN_VOLUME + (distanceFactor * (MAX_VOLUME - MIN_VOLUME));
        float directionalFactor = calculateDirectionalFactor(monsterPos, playerDir);
        float volume = baseVolume * signalStrength * directionalFactor;
        volume = Math.max(MIN_VOLUME, Math.min(MAX_VOLUME, volume));

        float pan = calculateStereoPan(monsterPos, playerDir);
        float leftVolume = volume * (1.0f - Math.max(0f, pan));
        float rightVolume = volume * (1.0f + Math.min(0f, pan));

        whiteNoisePlayer.setVolume(leftVolume, rightVolume);
        currentAudioLevel = (leftVolume + rightVolume) / 2.0f;
    }

    public float getCurrentAudioLevel() {
        return currentAudioLevel;
    }

    private Vector3 normalizeVector(Vector3 v) {
        float length = v.length();
        return length < 0.01f ? v : v.scale(1f / length);
    }

    private float calculateDirectionalFactor(Vector3 monsterPos, Vector3 playerDir) {
        Vector3 toMonster = normalizeVector(monsterPos);
        Vector3 forward = normalizeVector(playerDir);
        float dot = toMonster.x * forward.x + toMonster.y * forward.y + toMonster.z * forward.z;
        float directionalFactor = 0.01f + (dot + 1.0f) * 0.45f;
        return Math.max(0.01f, Math.min(1.0f, directionalFactor));
    }

    private float calculateStereoPan(Vector3 monsterPos, Vector3 playerDir) {
        Vector3 toMonster = normalizeVector(monsterPos);
        Vector3 forward = normalizeVector(playerDir);

        Vector3 right = new Vector3(forward.y, -forward.x, 0);
        float rightLength = (float) Math.sqrt(right.x * right.x + right.y * right.y);

        if (rightLength > 0.01f) {
            right.x /= rightLength;
            right.y /= rightLength;
        }

        float pan = toMonster.x * right.x + toMonster.y * right.y;
        return Math.max(-1f, Math.min(1f, pan));
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }

        if (whiteNoisePlayer != null) {
            if (isWhiteNoisePlaying) {
                whiteNoisePlayer.stop();
            }
            whiteNoisePlayer.release();
            whiteNoisePlayer = null;
            isWhiteNoisePlaying = false;
        }
    }
}
