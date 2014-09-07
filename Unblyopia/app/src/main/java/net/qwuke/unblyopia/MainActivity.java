package net.qwuke.unblyopia;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.EventLog;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.google.vrtoolkit.cardboard.*;

import net.qwuke.unblyopia.R;


public class MainActivity extends CardboardActivity {

    private Vibrator mVibrator;
    private TetrisView mTetrisView;
    BackgroundSound mBackgroundSound = new BackgroundSound();


    public class BackgroundSound extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            MediaPlayer player = MediaPlayer.create(MainActivity.this, R.raw.tetris);
            player.setLooping(true); // Set looping
            player.start();
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        //mBackgroundSound.doInBackground();
        mTetrisView = new TetrisView(this);
        mTetrisView.setBackgroundColor(Color.BLACK);
        setContentView(mTetrisView);
        mVibrator = ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE));
    }
    @Override
    public void onCardboardTrigger() {

        // Always give user feedback
        mVibrator.vibrate(100);
        mTetrisView.keyPressed(TetrisView.UP);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    mVibrator.vibrate(100); // Move left
                    mTetrisView.keyPressed(TetrisView.LEFT);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    mVibrator.vibrate(100); // Move right
                    mTetrisView.keyPressed(TetrisView.RIGHT);
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        mVibrator.vibrate(100);
        mTetrisView.keyPressed(TetrisView.UP);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    public void onResume() {
        super.onResume();
        mBackgroundSound.execute((Void[]) null);
    }
    public void onPause() {
        super.onPause();
        mBackgroundSound.cancel(true);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
