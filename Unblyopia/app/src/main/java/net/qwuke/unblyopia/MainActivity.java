package net.qwuke.unblyopia;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;


import android.content.Context;
import android.os.Vibrator;
import android.view.MotionEvent;

import com.google.vrtoolkit.cardboard.*;
import com.google.vrtoolkit.cardboard.sensors.*;


public class MainActivity extends CardboardActivity {
    public static final String TAG = "net.qwuke.unblyopia";

    // Views
    private TetrisView mTetrisView;

    // Interfacing- sensors, buttons, etc.
    private Vibrator mVibrator;
    private SensorManager mSensorManager;
    //private Sensor mAccelerometer;
    private HeadTracker mHeadTracker;
    private MotionSensorModule mMotionSensorModule;

    //private double[] velocity = new double[3];
    //private double[] accel_offset = new double[3];
    //private boolean accel_offset_set = false;

    BackgroundSound mBackgroundSound;

    public class BackgroundSound extends AsyncTask<Void, Void, Void> {
        protected MediaPlayer player;

        public BackgroundSound() {
            player = MediaPlayer.create(MainActivity.this, R.raw.tetris);
        }

        @Override
        protected Void doInBackground(Void... params) {
            if(player.isPlaying()){
                player.pause();
                return null;
            } else {
                player.setLooping(true); // Set looping
                // player.start(); // commented out because testing at night and family sleeping
                return null;
            }
        }

        protected void pause() {
            player.pause();
        }
    }

    /******   OVERRIDDEN ACTIVITY METHODS   ******/

    @Override
    public void onResume() {
        super.onResume();
        //setContentView(R.layout.activity_main);

        mBackgroundSound = new BackgroundSound();
        mBackgroundSound.doInBackground();
        mHeadTracker.startTracking();
        mVibrator = ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE));
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mMotionSensorModule = new MotionSensorModule(mSensorManager, mHeadTracker, this);

        mTetrisView = new TetrisView(this, mMotionSensorModule, mVibrator);
        mTetrisView.setBackgroundColor(Color.BLACK);
        setContentView(mTetrisView);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHeadTracker.stopTracking();
        mBackgroundSound.pause();
        mMotionSensorModule.unregister();
    }

    /******   INTERFACING: Methods for triggers, buttons, sensors   ******/

    /**
     * Handles the user swiping the Google Cardboard magnet
     */
    @Override
    public void onCardboardTrigger() {
        mTetrisView.tm.actionButton();
    }

    /**
     * Handles the user touching the screen
     */
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        mTetrisView.tm.actionButton();
        return true;
    }

    /**
     * Handles the user pressing the volume buttons
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        if(action == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    mTetrisView.tm.keyPressed(TetrisModel.Input.LEFT);
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    mTetrisView.tm.keyPressed(TetrisModel.Input.RIGHT);
                    return true;
                default:
                    return super.dispatchKeyEvent(event);
            }
        }

        return super.dispatchKeyEvent(event);
    }

    /**
     * This method prevents the volume buttons from making a noise
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) || (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    /******   UNCHANGED INTERFACE METHODS   ******/


    @Override
    public void onStart() { super.onStart(); }
    public void onStop() { super.onStop(); }
    public void onDestroy() { super.onDestroy(); }
    protected void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); mHeadTracker = new HeadTracker(this);}
}
