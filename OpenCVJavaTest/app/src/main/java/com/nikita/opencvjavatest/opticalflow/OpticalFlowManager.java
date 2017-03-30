package com.nikita.opencvjavatest.opticalflow;

import android.util.Log;

import com.nikita.opencvjavatest.filters.KalmanFilterSimple;
import com.nikita.opencvjavatest.sensors.SensorProvider;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.List;

public class OpticalFlowManager {
    private static final String TAG = OpticalFlowManager.class.getSimpleName();
    private static final float MIN_OPTFLOW_BORDER = 6.0f; //8
    private static final float MAX_OPTFLOW_BORDER = 30.0f;

    private static final float ROTATION_BORDER = 10.0f;
//    private static final float TRANSLATION_BORDER = 400.0f;

    private static final float SCALE = 1;

    private Mat mFlow;
    private Mat mRgba;
    private Mat matOpFlowThis;
    private Mat matOpFlowPrev;
    private Mat mMOP2fptsSafe;
    private Mat mConvertedThis;

    private MatOfPoint2f mMOP2fptsPrev;
    private MatOfPoint2f mMOP2fptsThis;
    private MatOfByte mMOBStatus;
    private MatOfFloat mMOFerr;

    private Point mCenterPoint;

    private SensorProvider mSensorProvider;
    private OpticalFlowResultProcessor mOpticalFlowResultProcessor;
    private KalmanFilterSimple mKalmanFilterSimple;

    public OpticalFlowManager(SensorProvider sensorProvider) {
        mSensorProvider = sensorProvider;
        mSensorProvider.setGyroscopeScale(SCALE, SCALE);

        mKalmanFilterSimple = new KalmanFilterSimple(4, 8, 1, 1);
        mKalmanFilterSimple.setState(new Point(0, 0), new Point(0.1, 0.1));

        mOpticalFlowResultProcessor = new OpticalFlowResultProcessor();

        mFlow = new Mat();
        mRgba = new Mat();
        matOpFlowThis = new Mat();
        matOpFlowPrev = new Mat();
        mMOP2fptsSafe = new Mat();
        mConvertedThis = new Mat();

        mMOP2fptsPrev = new MatOfPoint2f();
        mMOP2fptsThis = new MatOfPoint2f();

        mMOBStatus = new MatOfByte();
        mMOFerr = new MatOfFloat();

        mCenterPoint = new Point();
    }

    public Mat processOpticalFlowLK(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        mRgba = inputFrame.rgba();
        MatOfPoint MOPcorners = new MatOfPoint();
        int iGFFTMax = 80;
        if (mMOP2fptsPrev.rows() == 0) {
            Imgproc.cvtColor(mRgba, matOpFlowThis, Imgproc.COLOR_RGBA2GRAY);

            matOpFlowThis.copyTo(matOpFlowPrev);

            Imgproc.goodFeaturesToTrack(matOpFlowPrev, MOPcorners, iGFFTMax, 0.008, 20);

            mMOP2fptsPrev.fromArray(MOPcorners.toArray());
            mMOP2fptsPrev.copyTo(mMOP2fptsSafe);

            mCenterPoint.x = mRgba.cols()/2;
            mCenterPoint.y = mRgba.rows()/2;
        }
        else
        {
            matOpFlowThis.copyTo(matOpFlowPrev);

            Imgproc.cvtColor(mRgba, matOpFlowThis, Imgproc.COLOR_RGBA2GRAY);
            Imgproc.goodFeaturesToTrack(matOpFlowThis, MOPcorners, iGFFTMax, 0.008, 20);

            mMOP2fptsThis.fromArray(MOPcorners.toArray());

            mMOP2fptsSafe.copyTo(mMOP2fptsPrev);
            mMOP2fptsThis.copyTo(mMOP2fptsSafe);
        }
        Video.calcOpticalFlowPyrLK(matOpFlowPrev, matOpFlowThis, mMOP2fptsPrev, mMOP2fptsThis, mMOBStatus, mMOFerr, new Size(20,20), 3);

        List<Point> cornersPrev = mMOP2fptsPrev.toList();
        List<Point> cornersThis = mMOP2fptsThis.toList();
        List<Byte> byteStatus = mMOBStatus.toList();
        int y = byteStatus.size() - 1;
        int trackedPoints = 0;

        float [] deltaRotationVector = mSensorProvider.getDeltaRotationVector();
//        Log.v(TAG, "deltaRotationVector length: " + (Math.sqrt(Math.pow(deltaRotationVector[0], 2) + Math.pow(deltaRotationVector[1], 2))));
//        Log.v(TAG, "correctedRotationVector length: " + (Math.sqrt(Math.pow(correctedRotationVector.x, 2) + Math.pow(correctedRotationVector.y, 2))));
        for (int x = 0; x < y; x++) {
            if (byteStatus.get(x) == 1 && (Math.sqrt(Math.pow(deltaRotationVector[0], 2) + Math.pow(deltaRotationVector[1], 2)) < ROTATION_BORDER)) {

                trackedPoints++;
                Point pt = cornersThis.get(x);
                Point pt2 = cornersPrev.get(x);

                Point fromCenter = new Point();
                fromCenter.x = pt2.x - pt.x + deltaRotationVector[0];
                fromCenter.y = pt2.y - pt.y - deltaRotationVector[1];

                if ((Math.sqrt(Math.pow(fromCenter.x, 2) + Math.pow(fromCenter.y, 2)) >= MIN_OPTFLOW_BORDER) &&
                        (Math.sqrt(Math.pow(fromCenter.x, 2) + Math.pow(fromCenter.y, 2)) <= MAX_OPTFLOW_BORDER))
                    mOpticalFlowResultProcessor.processPoint(fromCenter);

//                Imgproc.circle(mRgba, pt, 2, new Scalar(255,0,0), 2);
//                Imgproc.line(mRgba, pt, pt2, new Scalar(255,0,0), 2);
            }
        }
        Point sum = new Point(0, 0);
        ArrayList<Point> probablyPoints = mOpticalFlowResultProcessor.processResults();
        if (trackedPoints != 0 && probablyPoints != null) {
            for (Point point : probablyPoints){
                sum.x -= point.x;
                sum.y -= point.y;
            }
            sum.x /= probablyPoints.size();
            sum.y /= probablyPoints.size();

            sum.x *= SCALE;
            sum.y *= SCALE;
        }

        sum = mKalmanFilterSimple.correct(sum);
        if ((Math.sqrt(Math.pow(sum.x, 2) + Math.pow(sum.y, 2)) < MIN_OPTFLOW_BORDER) &&
                (Math.sqrt(Math.pow(sum.x, 2) + Math.pow(sum.y, 2)) > MAX_OPTFLOW_BORDER))
            sum = new Point(0, 0);

        Log.v(TAG, "sum length: " + (Math.sqrt(Math.pow(sum.x, 2) + Math.pow(sum.y, 2))));

        Point outPoint = new Point(mCenterPoint.x + sum.x, mCenterPoint.y + sum.y);
        Imgproc.line(mRgba, mCenterPoint, outPoint, new Scalar(255, 0, 0), 2);
        Imgproc.line(mRgba, new Point(mCenterPoint.x - 10, mCenterPoint.y), new Point(mCenterPoint.x - 10 + deltaRotationVector[0], mCenterPoint.y - deltaRotationVector[1]), new Scalar(0,255,0), 2);

        mOpticalFlowResultProcessor.clear();
        return mRgba;

    }

    public Mat processOpticalFlowFarneback(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        mRgba = inputFrame.rgba();
        if (matOpFlowPrev.rows() == 0) {
            Imgproc.cvtColor(mRgba, matOpFlowThis, Imgproc.COLOR_RGBA2GRAY);
            matOpFlowThis.convertTo(mConvertedThis, CvType.CV_8UC1);
            mConvertedThis.copyTo(matOpFlowPrev);
        }
        else
        {
            mConvertedThis.copyTo(matOpFlowPrev);
            Imgproc.cvtColor(mRgba, matOpFlowThis, Imgproc.COLOR_RGBA2GRAY);
            matOpFlowThis.convertTo(mConvertedThis, CvType.CV_8UC1);
            Video.calcOpticalFlowFarneback(matOpFlowPrev, mConvertedThis, mFlow, 0.5f, 3, 30, 2, 7, 1.5, Video.OPTFLOW_USE_INITIAL_FLOW);
            Log.v("Farneback", "mFlow: " + mFlow.size());
        }
        for (int y = 0; y < mRgba.rows();y += 30){
            for (int x = 0; x < mRgba.cols(); x += 30){
                Point flowAtXY = new Point(mFlow.get(x,y));
                Imgproc.line(mRgba, new Point(x,y), new Point(Math.round(x + flowAtXY.x), Math.round(y + flowAtXY.y)), new Scalar(255,0,0));
                Imgproc.circle(mRgba, new Point(x,y), 1, new Scalar(0,0,0),-1);
            }
        }
        return mRgba;
    }
}
