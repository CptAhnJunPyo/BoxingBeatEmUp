package com.map.boxingbeatemup;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private SoundPool soundPool;
    private Map<String, Integer> soundMap;
    private Context context;

    public SoundManager(Context context) {
        this.context = context;
        initializeSoundPool();
        loadSounds();
    }

    private void initializeSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(attributes)
                .build();

        soundMap = new HashMap<>();
    }

    private void loadSounds() {
        // Load your sound effects here
        soundMap.put("punch", soundPool.load(context, R.raw.punch, 1));
        soundMap.put("kick", soundPool.load(context, R.raw.kick, 1));
        //soundMap.put("hit", soundPool.load(context, R.raw.hit, 1));
    }

    public void playSound(String soundName) {
        Integer soundId = soundMap.get(soundName);
        if (soundId != null) {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }
}