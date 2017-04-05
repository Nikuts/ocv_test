package com.nikita.opencvjavatest;

import android.app.Activity;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.WindowManager;

import com.nikita.opencvjavatest.opticalflow.OpticalFlowManager;
import com.nikita.opencvjavatest.sensors.SensorProvider;

import org.opencv.android.CameraBridgeNoView;
import org.opencv.android.JavaCameraNoView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;


public class MainActivity extends Activity implements CameraBridgeNoView.CvCameraViewListener2 {
    private CameraBridgeNoView mOpenCvCameraView = null;
    private OpticalFlowManager mOpticalFlowManager = null;
    private SensorProvider mSensorProvider = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        setContentView(R.layout.activity_main);
//        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.view);
        mOpenCvCameraView = new JavaCameraNoView(this, CameraBridgeNoView.CAMERA_ID_ANY);
        mOpenCvCameraView.enableFpsMeter();
        mOpenCvCameraView.SetCaptureFormat(CameraBridgeNoView.GRAY);
//        mOpenCvCameraView.setMaxFrameSize(800,600);
        mOpenCvCameraView.setMaxFrameSize(600,400);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mSensorProvider = new SensorProvider((SensorManager)getSystemService(SENSOR_SERVICE));
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        if (mSensorProvider != null)
            mSensorProvider.stop();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initDebug();
        mOpenCvCameraView.enableView();

        mSensorProvider.resume();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        if (mSensorProvider != null)
            mSensorProvider.stop();
    }

    public void onCameraViewStarted(int width, int height) {
        mOpticalFlowManager = new OpticalFlowManager(mSensorProvider);
    }

    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeNoView.CvCameraViewFrame inputFrame) {
        Mat newRgba = mOpticalFlowManager.processOpticalFlowLK(inputFrame);
        return newRgba;
    }
}