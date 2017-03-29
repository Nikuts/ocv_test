package com.nikita.opencvjavatest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


public class SensorProvider implements SensorEventListener {
    private static final String TAG = SensorProvider.class.getSimpleName();
    private static final float DEFAULT_GYROSCOPE_SCALE_X = 8.7f;
    private static final float DEFAULT_GYROSCOPE_SCALE_Y = DEFAULT_GYROSCOPE_SCALE_X + 0.3f;
    private static final float ACCELEROMETER_SCALE = 10.0f;
    private static final String mSynchronizeSensor = "mSynchronizeSensor";

    private float [] mDeltaRotationVector = {0, 0};
    private float [] mDeltaTranslationVector = {0, 0};

    private float mGyroscopeScaleX = DEFAULT_GYROSCOPE_SCALE_X;
    private float mGyroscopeScaleY = DEFAULT_GYROSCOPE_SCALE_Y;

    private SensorManager mSensorManager = null;

    public SensorProvider(SensorManager systemService){
        mSensorManager = systemService;
    }

    public void resume(){
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), mSensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), mSensorManager.SENSOR_DELAY_GAME);
    }

    public void stop(){
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (mSynchronizeSensor) {
            switch (event.sensor.getType()){
                case Sensor.TYPE_GYROSCOPE:
                    mDeltaRotationVector[0] += event.values[0];
                    mDeltaRotationVector[1] += event.values[1];
                    break;
                case Sensor.TYPE_ACCELEROMETER:
                    mDeltaTranslationVector[0] += event.values[0];
                    mDeltaTranslationVector[1] += event.values[1];
                    break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.v(TAG,"onAccuracyChanged: " + sensor + ", accuracy: " + accuracy);
    }

    public float[] getDeltaRotationVector(){ //ox: same direction, oy: opposite direction
        float[] deltaRotationVector = {0, 0};
        synchronized (mSynchronizeSensor) {
            deltaRotationVector[0] = (mDeltaRotationVector[0] * mGyroscopeScaleX);
            deltaRotationVector[1] = (mDeltaRotationVector[1] * mGyroscopeScaleX);
            mDeltaRotationVector[0] = 0;
            mDeltaRotationVector[1] = 0;
        }
//      Log.v(TAG, "gyro: " + deltaRotationVector[0] + ", " + deltaRotationVector[1]);
        return deltaRotationVector;
    }

    public float[] getDeltaTranslationVector(){
        float [] deltaTranslationVector = {0, 0};
        synchronized (mSynchronizeSensor) {
            deltaTranslationVector[0] = (mDeltaTranslationVector[0] * ACCELEROMETER_SCALE);
            deltaTranslationVector[1] = (mDeltaTranslationVector[1] * ACCELEROMETER_SCALE);
            mDeltaTranslationVector[0] = 0;
            mDeltaTranslationVector[1] = 0;
        }
//        Log.v(TAG, "accelerometer: " + deltaTranslationVector[0] + ", " + deltaTranslationVector[1]);
        return deltaTranslationVector;
    }

    public void setGyroscopeScale(float scaleX, float scaleY) {
        mGyroscopeScaleX = DEFAULT_GYROSCOPE_SCALE_X * scaleX;
        mGyroscopeScaleY = DEFAULT_GYROSCOPE_SCALE_Y * scaleY;
    }
}