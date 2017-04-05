package com.nikita.ocvgvrfapp;

import android.hardware.SensorManager;
import android.os.Bundle;

import com.nikita.ocvgvrfapp.graphics.VrMain;
import com.nikita.ocvgvrfapp.opencv.opticalflow.OpticalFlowManager;
import com.nikita.ocvgvrfapp.opencv.sensors.SensorProvider;

import org.gearvrf.GVRActivity;
import org.opencv.android.CameraBridgeNoView;
import org.opencv.android.JavaCameraNoView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;

public class MainActivity extends GVRActivity implements CameraBridgeNoView.CvCameraViewListener2 {
    private CameraBridgeNoView mOpenCvCameraNoView = null;
    private OpticalFlowManager mOpticalFlowManager = null;
    private SensorProvider mSensorProvider = null;
    private VrMain mVrMain;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mVrMain = new VrMain();
        setMain(mVrMain);
        mOpenCvCameraNoView = new JavaCameraNoView(this, CameraBridgeNoView.CAMERA_ID_ANY);
        mOpenCvCameraNoView.enableFpsMeter();
        mOpenCvCameraNoView.SetCaptureFormat(CameraBridgeNoView.GRAY);
//        mOpenCvCameraNoView.setMaxFrameSize(800,600);
        mOpenCvCameraNoView.setMaxFrameSize(600,400);
        mOpenCvCameraNoView.setCvCameraViewListener(this);

        mSensorProvider = new SensorProvider((SensorManager)getSystemService(SENSOR_SERVICE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraNoView != null)
            mOpenCvCameraNoView.disableView();
        if (mSensorProvider != null)
            mSensorProvider.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initDebug();
        mOpenCvCameraNoView.enableView();
        mSensorProvider.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraNoView != null)
            mOpenCvCameraNoView.disableView();
        if (mSensorProvider != null)
            mSensorProvider.stop();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mOpticalFlowManager = new OpticalFlowManager(mSensorProvider);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeNoView.CvCameraViewFrame inputFrame) {
        Point rotationVector = mOpticalFlowManager.processOpticalFlowLK(inputFrame);
        mVrMain.setRotationVector(rotationVector);
        return null;
    }
}
