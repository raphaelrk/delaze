package net.qwuke.unblyopia;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.ViewParent;

import java.util.Date;

/**
 * Module for Accelerometer data
 *
 * Created by RAPHAEL on 9/21/2014.
 */
public class MotionSensorModule implements SensorEventListener {

    private final SensorManager mSensorManager;
    private final Sensor mSensor;

    /**
     * floats are used because the sensor returns a float[]
     */
    private float[] mValues;

    // these values control how receptive acceleration/velocity averages are to change
    private final float gravAlpha = 0.8f; // how much old gravity values stay
    private final float velAlpha = 0.8f; // how much old velocity average values stay

    // these arrays store the current acceleration/velocity readings
    private static float[] currentAcceleration = {0, 0, 0};
    private static float[] velocity = {0, 0, 0};

    // these arrays store averages of the acceleration/velocity readings
    private static float[] gravity = {0, 0, 0}; // "gravity" really means average acceleration
    private static float[] velocityAverage = {0, 0, 0};

    // used as the 't' in 'Vf = Vo * at'
    private Date lastUpdate;

    public MotionSensorModule(SensorManager sm, Activity mainActivity) {
        mSensorManager = sm;
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mValues = new float[3];
        register();

        lastUpdate = new Date(System.currentTimeMillis());
    }

    // called on its own every couple accelerometer readings (
    @Override
    public void onSensorChanged(SensorEvent event) {
        mValues = event.values;

        /*
        In this example, alpha is calculated as t / (t + dT),
        where t is the low-pass filter's time-constant and
        dT is the event delivery rate.
        */

        // Isolate the force of gravity with the low-pass filter.
        // all this does is calculate the average acceleration and remove it
        // accelaverage.x = 0.8 * accelaverage.x + 0.2 * newaccel.x
        for(int i = 0; i < 10; i++) {
            gravity[0] = gravAlpha * gravity[0] + (1 - gravAlpha) * mValues[0];
            gravity[1] = gravAlpha * gravity[1] + (1 - gravAlpha) * mValues[1];
            gravity[2] = gravAlpha * gravity[2] + (1 - gravAlpha) * mValues[2];
        }

        // Remove the gravity and miscalibration contribution with the high-pass filter.
        currentAcceleration[0] = mValues[0] - gravity[0];
        currentAcceleration[1] = mValues[1] - gravity[1];
        currentAcceleration[2] = mValues[2] - gravity[2];

        updateVelocity();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private float round(float a) {
        int round = 10;
        float b = (float) (Math.floor(Math.abs(a) * round)) / round;
        return b * Math.signum(a);
    }

    private void updateVelocity() {
        // Calculate how long this acceleration has been applied.
        Date timeNow = new Date(System.currentTimeMillis());
        long timeDelta = timeNow.getTime() - lastUpdate.getTime();

        lastUpdate.setTime(timeNow.getTime());

        // Calculate the change in velocity at the
        // current acceleration since the last update.
        float[] deltaVelocity = {0, 0, 0};
        float timeDeltaInSec = (float) (timeDelta) / 1000;
        deltaVelocity[0] = timeDelta * currentAcceleration[0];
        deltaVelocity[1] = timeDelta * currentAcceleration[1];
        deltaVelocity[2] = timeDelta * currentAcceleration[2];

        // Add the velocity change to the current velocity.
        velocity[0] += deltaVelocity[0];
        velocity[1] += deltaVelocity[1];
        velocity[2] += deltaVelocity[2];

        // Calibration based on averages
        for(int i = 0; i < 10; i++) {
            velocityAverage[0] = velAlpha * velocity[0] + (1 - velAlpha) * mValues[0]; // velAlpha * grav[0] could work better
            velocityAverage[1] = velAlpha * velocity[1] + (1 - velAlpha) * mValues[1];
            velocityAverage[2] = velAlpha * velocity[2] + (1 - velAlpha) * mValues[2];
        }
    }

    public void register() {
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Sensors don't unregister themselves - if not unregistered then the
     * accelerometer will drain the user's battery
     */
    public void unregister() {
        mSensorManager.unregisterListener(this);
    }

    public float[] getValues() {
        return mValues;
    }

    public float[] getVelocities() {
        return velocity;
    }

    public float[] getAverageVelocities() {
        return velocityAverage;
    }
}
