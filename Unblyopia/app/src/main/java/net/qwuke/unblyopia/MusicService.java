package net.qwuke.unblyopia;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;


public class MusicService extends Service {

    // service binder
    private final IBinder mBinder = new LocalBinder();

    // music player controling game music
    private static CarefulMediaPlayer mPlayer = null;

    @Override
    public void onCreate() {
        // load music file and create player
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.tetris);
        mediaPlayer.setLooping(true);
        mPlayer = new CarefulMediaPlayer(mediaPlayer, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // =========================
    // Player methods
    // =========================
    public void musicStart() {
        mPlayer.start();
    }

    public void musicStop() {
        mPlayer.stop();
    }

    public void musicPause() {
        mPlayer.pause();
    }

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

}