package com.nikita.opencvjavatest;

import android.app.Activity;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.androidcv.R;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;


public class MainActivity extends Activity implements CvCameraViewListener2 {
    private CameraBridgeViewBase mOpenCvCameraView = null;
    private OpticalFlowManager mOpticalFlowManager = null;
    private SensorProvider mSensorProvider = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.enableFpsMeter();
        mOpenCvCameraView.SetCaptureFormat(CameraBridgeViewBase.GRAY);
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

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        //return inputFrame.rgba();
//        return processOpticalFlowFarneback(inputFrame);
        Mat newRgba = mOpticalFlowManager.processOpticalFlowLK(inputFrame);
//        mSensorProvider.getDeltaRotationVector();
//        mSensorProvider.getDeltaTranslationVector();
        return newRgba;
    }
}