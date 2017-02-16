package com.bakkenbaeck.token.util;


import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.support.annotation.IntDef;
import android.util.SparseIntArray;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.BaseApplication;

public class SoundManager {

    @IntDef({
            ADD_CONTACT,
            RECEIVE_MESSAGE,
            SEND_MESSAGE,
            SCAN,
            SCAN_RESULT,
            TAB_BUTTON})
    public @interface Sound {}
    public static final int ADD_CONTACT = 0;
    public static final int RECEIVE_MESSAGE = 1;
    public static final int SEND_MESSAGE = 2;
    public static final int SCAN = 3;
    public static final int SCAN_RESULT = 4;
    public static final int TAB_BUTTON = 5;
    private static final int MAX_STREAMS = 5;
    private static SoundManager instance;

    public static SoundManager get() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private final SoundPool soundPool;
    private final SparseIntArray soundPoolMap;
    private final AudioManager audioManager;

    private SoundManager() {
        final Context context = BaseApplication.get();
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        soundPool = buildSoundPool();

        soundPoolMap = new SparseIntArray();
        soundPoolMap.put(ADD_CONTACT, soundPool.load(context, R.raw.addcontact, 1));
        soundPoolMap.put(RECEIVE_MESSAGE, soundPool.load(context, R.raw.messagereceive, 1));
        soundPoolMap.put(SEND_MESSAGE, soundPool.load(context, R.raw.messagesend, 1));
        soundPoolMap.put(SCAN, soundPool.load(context, R.raw.scan, 1));
        soundPoolMap.put(SCAN_RESULT, soundPool.load(context, R.raw.scanresult, 1));
        soundPoolMap.put(TAB_BUTTON, soundPool.load(context, R.raw.tabbutton, 1));
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private SoundPool buildSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            return new SoundPool.Builder()
                    .setMaxStreams(MAX_STREAMS)
                    .setAudioAttributes(audioAttributes)
                    .build();
        }

        return new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
    }

    public void playSound(final @Sound int index) {
        final float currentVolumeIndex = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        final float maxVolumeIndex = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        final float streamVolume = currentVolumeIndex / maxVolumeIndex;
        soundPool.play(soundPoolMap.get(index), streamVolume, streamVolume, 1, 0, 1f);
    }
}