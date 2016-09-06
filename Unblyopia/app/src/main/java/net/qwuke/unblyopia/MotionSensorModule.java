package net.qwuke.unblyopia;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.FloatMath;
import android.view.ViewParent;

import com.google.vrtoolkit.cardboard.sensors.HeadTracker;

import java.util.Date;

/**
 * Module for Accelerometer data
 *
 * Created by RAPHAEL on 9/21/2014.
 */

public class MotionSensorModule implements SensorEventListener {

    private final SensorManager mSensorManager;
    private final HeadTracker mHeadTracker;
    private final Sensor mSensor;

    /**
     * floats are used because the sensor returns a float[]
     */
    private float[] mValues;
    private float[] mHeadMatrix;
    private float[] mHeadQuat;
    private float[] mHeadAngles;
    private float[] mQuatAngles;
    // these values control how receptive acceleration/velocity averages are to change
    private final float gravAlpha = 0.8f; // how much old gravity values stay
    private final float velAlpha = 0.8f; // how much old velocity average values stay

    // these arrays store the current acceleration/velocity readings
    private static float[] currentAcceleration = {0, 0, 0};
    private static float[] velocity = {0, 0, 0};
    private static float[] headAngles = {0, 0, 0};
    private static float[] quatAngles = {0, 0, 0};
    private static float[] headValues = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    // these arrays store averages of the acceleration/velocity readings
    private static float[] gravity = {0, 0, 0}; // "gravity" really means average acceleration
    private static float[] velocityAverage = {0, 0, 0};

    public float minYV = 0, maxYV = 0;

    // used as the 't' in 'Vf = Vo * at'
    private Date lastUpdate;

    public MotionSensorModule(SensorManager sm, HeadTracker ht, Activity mainActivity) {
        mSensorManager = sm;
        mHeadTracker = ht;
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mValues = new float[3];
        mHeadAngles = new float[3];
        mHeadMatrix = new float[16];
        mHeadQuat = new float[4];
        mQuatAngles = new float[3];
        register();

        lastUpdate = new Date(System.currentTimeMillis());
    }

    // called on its own every couple accelerometer readings (
    @Override
    public void onSensorChanged(SensorEvent event) {
        mValues = event.values;
        mHeadTracker.getLastHeadView(mHeadMatrix, 0);
        getQuaternion(mHeadMatrix, mHeadQuat, 0);
        setQuatAngles(mHeadQuat,mQuatAngles);
        getEulerAngles(mHeadMatrix, mHeadAngles, 0);
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
    public void getQuaternion(float[] headMatrix, float[] quaternion, int offset) {
        if (offset + 4 > quaternion.length) {
            throw new IllegalArgumentException(
                    "Not enough space to write the result");
        }
        float[] m = headMatrix;
        float t = m[0] + m[5] + m[10];
        float x;
        float y;
        float z;
        float w;
        float s;
        if (t >= 0.0F) {
            s = (float) Math.sqrt(t + 1.0F);
            w = 0.5F * s;
            s = 0.5F / s;
            x = (m[9] - m[6]) * s;
            y = (m[2] - m[8]) * s;
            z = (m[4] - m[1]) * s;
        } else {
            if ((m[0] > m[5]) && (m[0] > m[10])) {
                s = (float) Math.sqrt(1.0F + m[0] - m[5] - m[10]);
                x = s * 0.5F;
                s = 0.5F / s;
                y = (m[4] + m[1]) * s;
                z = (m[2] + m[8]) * s;
                w = (m[9] - m[6]) * s;
            } else {
                if (m[5] > m[10]) {
                    s = (float) Math.sqrt(1.0F + m[5] - m[0] - m[10]);
                    y = s * 0.5F;
                    s = 0.5F / s;
                    x = (m[4] + m[1]) * s;
                    z = (m[9] + m[6]) * s;
                    w = (m[2] - m[8]) * s;
                } else {
                    s = (float) Math.sqrt(1.0F + m[10] - m[0] - m[5]);
                    z = s * 0.5F;
                    s = 0.5F / s;
                    x = (m[2] + m[8]) * s;
                    y = (m[9] + m[6]) * s;
                    w = (m[4] - m[1]) * s;
                }
            }
        }
        quaternion[(offset + 0)] = x;
        quaternion[(offset + 1)] = y;
        quaternion[(offset + 2)] = z;
        quaternion[(offset + 3)] = w;
    }
    public void setQuatAngles(float[] quaternion, float[] quatangles) {
        float yaw;
        float pitch;
        float roll;
        float test = quaternion[0]*quaternion[1] + quaternion[2]*quaternion[3];
        if (test > 0.499) { // singularity at north pole
            yaw = (float) (2 * Math.atan2(quaternion[0],quaternion[3]));
            pitch = (float) (Math.PI/2);
            roll = 0;
            return;
        }
        if (test < -0.499) { // singularity at south pole
            yaw = (float) (-2 * Math.atan2(quaternion[0],quaternion[3]));
            pitch = (float) (- Math.PI/2);
            roll = 0;
            return;
        }
        double sqx = quaternion[0]*quaternion[0];
        double sqy = quaternion[1]*quaternion[1];
        double sqz = quaternion[2]*quaternion[2];
        yaw = (float) Math.atan2(2 * quaternion[1] * quaternion[3] - 2 * quaternion[0] * quaternion[2], 1 - 2 * sqy - 2 * sqz);
        pitch = (float) Math.asin(2 * test);
        roll = (float) Math.atan2(2 * quaternion[0] * quaternion[3] - 2 * quaternion[1] * quaternion[2], 1 - 2 * sqx - 2 * sqz);
        quatangles[0] = pitch;
        quatangles[1] = yaw;
        quatangles[2] = roll;
    }
    public void getEulerAngles(float[] headMatrix, float[] eulerAngles, int offset) {
        if (offset + 3 > eulerAngles.length) {
            throw new IllegalArgumentException(
                    "Not enough space to write the result");
        }
        float pitch = (float) Math.asin(headMatrix[6]);
        float roll;
        float yaw; //YAAAAAAAAAAAAWWWWWWWWWWWWWWWWWWWWWWWWWWSSSSSSSSSSSSSSSSSSSSSS
        if ((float) Math.sqrt(1.0F - headMatrix[6] * headMatrix[6]) >= 0.01F) {
            yaw = (float) Math.atan2(-headMatrix[2],
                    headMatrix[10]);
            roll = (float) Math.atan2(-headMatrix[4], headMatrix[5]);
        } else {
            yaw = 0.0F;
            roll = (float) Math.atan2(headMatrix[1], headMatrix[0]);
        }
        eulerAngles[(offset + 0)] = (-pitch);
        eulerAngles[(offset + 1)] = (-yaw);
        eulerAngles[(offset + 2)] = (-roll);
    }

    private void updateVelocity() {
        headValues = mHeadMatrix;
        headAngles = mHeadAngles;
        quatAngles = mQuatAngles;
        /* Former velocity code
        // Calculate how long this acceleration has been applied.
        Date timeNow = new Date(System.currentTimeMillis());
        long timeDelta = timeNow.getTime() - lastUpdate.getTime();
        headValues = mHeadMatrix;
        headAngles = mHeadAngles;
        quatAngles = mQuatAngles;

        // lastUpdate.setTime(timeNow.getTime());


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

        // update y velocity range
        if(Math.abs(velocity[2]) < 40) {
            if (velocity[1] < minYV) {
                minYV = velocity[1];
            }
            if (velocity[1] > maxYV) {
                maxYV = velocity[1];
            }
        } */
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

    public float[] getHeadValues() {
        return headValues;
    }

    public float[] getHeadAngles() {
        return headAngles;
    }

    public float[] getQuatAngles() { return quatAngles; }

    public float[] getVelocities() {
        return velocity;
    }

    public float[] getAverageVelocities() {
        return velocityAverage;
    }
}
