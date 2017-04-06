package com.nikita.opencvjavatest.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


public class SensorProvider implements SensorEventListener {
    private static final String TAG = SensorProvider.class.getSimpleName();
    private static final float DEFAULT_GYROSCOPE_SCALE_X = 13.5f; //8.2f;
    private static final float DEFAULT_GYROSCOPE_SCALE_Y = DEFAULT_GYROSCOPE_SCALE_X; //+ 0.3f;
    private static final float ACCELEROMETER_NOISE = 0.2f;
    private static final String mSynchronizeSensor = "mSynchronizeSensor";

    private int mRotationVectorCount = 0;

    private float [] mDeltaRotationVector = {0, 0};
    private float [] mPrevDeltaRotationVector = {0,0};
    private float [] mLastTranslationVector = {0, 0};
    private float [] mDeltaTranslationVector = {0, 0};
    private float mTranslationSpeed = 0;

    private boolean mLinearAccelerationInitialized = false;

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
                    mRotationVectorCount++;
//                    Log.v(TAG, "rotation x,y: " + event.values[0] + ", " + event.values[1]);
                    break;
                case Sensor.TYPE_ACCELEROMETER:
                    float x = event.values[0];
                    float y = event.values[1];
                    if (!mLinearAccelerationInitialized) {
                        mLastTranslationVector[0] = x;
                        mLastTranslationVector[1] = y;
                        mLinearAccelerationInitialized = true;
                    }
                    else {
                        mDeltaTranslationVector[0] = Math.abs(mLastTranslationVector[0] - x);
                        mDeltaTranslationVector[1] = Math.abs(mLastTranslationVector[1] - y);

                        if (mDeltaTranslationVector[0] < ACCELEROMETER_NOISE) mDeltaTranslationVector[0] = 0;
                        if (mDeltaTranslationVector[1] < ACCELEROMETER_NOISE) mDeltaTranslationVector[1] = 0;

                        mLastTranslationVector[0] = x;
                        mLastTranslationVector[1] = y;
                        mTranslationSpeed = (float) Math.sqrt(Math.pow(mDeltaTranslationVector[0],2) + Math.pow(mDeltaTranslationVector[1],2));
                    }
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
            deltaRotationVector[0] = (( -mDeltaRotationVector[0]/mRotationVectorCount + mPrevDeltaRotationVector[0]) * mGyroscopeScaleX);
            deltaRotationVector[1] = (( -mDeltaRotationVector[1]/mRotationVectorCount + mPrevDeltaRotationVector[1]) * mGyroscopeScaleY);
            mPrevDeltaRotationVector[0] = mDeltaRotationVector[0];
            mPrevDeltaRotationVector[1] = mDeltaRotationVector[1];
            mDeltaRotationVector[0] = 0;
            mDeltaRotationVector[1] = 0;
            mRotationVectorCount = 0;
        }
//      Log.v(TAG, "gyro: " + deltaRotationVector[0] + ", " + deltaRotationVector[1]);
        return deltaRotationVector;
    }

    public float getTranslationSpeed(){
        float speed;
        synchronized (mSynchronizeSensor) {
            speed = mTranslationSpeed;
        }
        return speed;
//        Log.v(TAG, "accelerometer: " + deltaTranslationVector[0] + ", " + deltaTranslationVector[1]);
    }

    public void setGyroscopeScale(float scaleX, float scaleY) {
        mGyroscopeScaleX = DEFAULT_GYROSCOPE_SCALE_X * scaleX;
        mGyroscopeScaleY = DEFAULT_GYROSCOPE_SCALE_Y * scaleY;
    }
}