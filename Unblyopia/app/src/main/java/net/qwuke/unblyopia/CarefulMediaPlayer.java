package net.qwuke.unblyopia;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;

class CarefulMediaPlayer {
    private final SharedPreferences sp;
    private final MediaPlayer mp;
    private boolean isPlaying = false;

    public CarefulMediaPlayer(final MediaPlayer mp, final MusicService ms) {
        sp = PreferenceManager.getDefaultSharedPreferences(ms.getApplicationContext());
        this.mp = mp;
    }

    public void start() {
        if (sp.getBoolean("com.embed.candy.music", true) && !isPlaying) {
            mp.start();
            isPlaying = true;
        }
    }

    public void pause() {
        if (isPlaying) {
            mp.pause();
            isPlaying = false;
        }
    }

    public void stop() {
        isPlaying = false;
        try {
            mp.stop();
            mp.release();
        } catch (final Exception e) {}
    }
}