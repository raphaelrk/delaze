package net.qwuke.unblyopia;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;


import android.content.Context;
import android.os.Vibrator;
import android.view.MotionEvent;

import com.google.vrtoolkit.cardboard.*;
import com.google.vrtoolkit.cardboard.sensors.*;

import static java.util.logging.Logger.global;


public class MainActivity extends CardboardActivity {
    public static final String TAG = "net.qwuke.unblyopia";

    // Views
    private TetrisView mTetrisView;

    //private Sensor mAccelerometer;
    private HeadTracker mHeadTracker;
    private MotionSensorModule mMotionSensorModule;

    private Boolean isHeadTrackingEnabled;
    private Boolean isBackgroundMusicEnabled;

    int[] globalColours = new int[3];

    //private double[] velocity = new double[3];
    //private double[] accel_offset = new double[3];
    //private boolean accel_offset_set = false;

    private BackgroundSound mBackgroundSound;

    public class BackgroundSound extends AsyncTask<Void, Void, Void> {
        final MediaPlayer player;

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
        public void startPlayer() {
            player.start();
            player.setLooping(true);
        }
        public void pausePlayer() {
            player.pause();
        }
    }

    /******   OVERRIDDEN ACTIVITY METHODS   ******/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(getPackageName(), MODE_PRIVATE);

        isHeadTrackingEnabled = prefs.getBoolean("HeadTracking", true);
        isBackgroundMusicEnabled = prefs.getBoolean("BackgroundMusic", true);

        globalColours[0] = prefs.getInt("activeEyeBlockColour", Color.CYAN);
        globalColours[1] = prefs.getInt("bgColour", Color.LTGRAY);
        globalColours[2] = prefs.getInt("fallenColour", Color.BLUE);

        if(isHeadTrackingEnabled) {
            mHeadTracker = new HeadTracker(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //setContentView(R.layout.activity_main);

        if(isBackgroundMusicEnabled) {
            mBackgroundSound = new BackgroundSound();
            mBackgroundSound.doInBackground();
        }
        if(isHeadTrackingEnabled) {
            mHeadTracker.startTracking();
            SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mMotionSensorModule = new MotionSensorModule(mSensorManager, mHeadTracker);
        }
        Vibrator mVibrator = ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE));

        mTetrisView = new TetrisView(this, mMotionSensorModule, mVibrator, isHeadTrackingEnabled, globalColours);
        mTetrisView.setBackgroundColor(Color.BLACK);
        setContentView(mTetrisView);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mHeadTracker != null) {
            mHeadTracker.stopTracking();
            mMotionSensorModule.unregister();
        }
        if (mBackgroundSound != null) {
            mBackgroundSound.pausePlayer();
        }

    }

    /******   INTERFACING: Methods for triggers, buttons, sensors   ******/

    /**
     * Handles the user swiping the Google Cardboard magnet
     */
    @Override
    public void onCardboardTrigger() {
        mTetrisView.tm.actionButton();
        mTetrisView.tm.isOn();
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
        return (keyCode == KeyEvent.KEYCODE_VOLUME_UP) || (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) || super.onKeyUp(keyCode, event);
    }

}
