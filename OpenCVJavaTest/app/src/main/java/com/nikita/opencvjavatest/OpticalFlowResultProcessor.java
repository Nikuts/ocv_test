package com.nikita.opencvjavatest;

import android.util.Log;

import org.opencv.core.Point;

import java.util.ArrayList;

/**
 * Created by n.kutsachenk on 3/28/2017.
 */

public class OpticalFlowResultProcessor {
    private static final String TAG = OpticalFlowResultProcessor.class.getSimpleName();
    private static final int MIN_POINTS_COUNT = 8;

    private ArrayList<Point> mRight = new ArrayList<>();
    private ArrayList<Point> mRightTop = new ArrayList<>();
    private ArrayList<Point> mTop = new ArrayList<>();
    private ArrayList<Point> mLeftTop = new ArrayList<>();
    private ArrayList<Point> mLeft = new ArrayList<>();
    private ArrayList<Point> mLeftBottom = new ArrayList<>();
    private ArrayList<Point> mBottom = new ArrayList<>();
    private ArrayList<Point> mRightBottom = new ArrayList<>();

    public OpticalFlowResultProcessor(){}

    public void clear(){
        mRight.clear();
        mRightTop.clear();
        mTop.clear();
        mLeftTop.clear();
        mLeft.clear();
        mLeftBottom.clear();
        mBottom.clear();
        mRightBottom.clear();
    }

    public void processPoint(Point point){
        if ((point.x > 0) && (point.y < 1/Math.sqrt(3) * point.x) && (point.y >= - 1/Math.sqrt(3) * point.x)) mTop.add(point);//mBottom.add(point); //mRight.add(point);
        if ((point.x > 0) && (point.y < Math.sqrt(3) * point.x) && (point.y >= 1/Math.sqrt(3) * point.x)) mRightBottom.add(point);//mLeftBottom.add(point); //mRightTop.add(point);
        if ((point.y > 0) && (point.x >= point.y / (- Math.sqrt(3))) && (point.x < point.y/Math.sqrt(3))) mRight.add(point);//mLeft.add(point); // mTop.add(point);
        if ((point.x < 0) && (point.y < point.x * - Math.sqrt(3)) && (point.y >= point.x * - 1/Math.sqrt(3))) mRightTop.add(point);//mLeftTop.add(point);
        if ((point.x < 0) && (point.y < -1/Math.sqrt(3) * point.y) && (point.y >= 1/Math.sqrt(3) * point.x)) mBottom.add(point);//mTop.add(point); //mLeft.add(point);
        if ((point.x < 0) && (point.y < 1/Math.sqrt(3) * point.x) && (point.y >= Math.sqrt(3) * point.x)) mLeftTop.add(point);//mRightTop.add(point); //mLeftBottom.add(point);
        if ((point.y < 0) && (point.x >= point.y/Math.sqrt(3)) && (point.x < point.y/-Math.sqrt(3))) mLeft.add(point);//mRight.add(point); //mBottom.add(point);
        if ((point.x > 0) && (point.y >= -Math.sqrt(3) * point.x) && (point.y < -1/Math.sqrt(3) * point.x)) mLeftBottom.add(point);//mRightBottom.add(point);
    }

    public ArrayList<Point> processResults(){
        int max = 0;
        String maxDirection = "";
        ArrayList<Point> maxPoints = null;

        if (max < mRight.size()){
            max = mRight.size();
            maxDirection = "Right";
            maxPoints = mRight;
        }
        if (max < mRightTop.size()){
            max = mRightTop.size();
            maxDirection = "RightTop";
            maxPoints = mRightTop;
        }
        if (max < mTop.size()){
            max = mTop.size();
            maxDirection = "Top";
            maxPoints = mTop;
        }
        if (max < mLeftTop.size()){
            max = mLeftTop.size();
            maxDirection = "LeftTop";
            maxPoints = mLeftTop;
        }
        if (max < mLeft.size()){
            max = mLeft.size();
            maxDirection = "Left";
            maxPoints = mLeft;
        }
        if (max < mLeftBottom.size()){
            max = mLeftBottom.size();
            maxDirection = "LeftBottom";
            maxPoints = mLeftBottom;
        }
        if (max < mBottom.size()){
            max = mBottom.size();
            maxDirection = "Bottom";
            maxPoints = mBottom;
        }
        if (max < mRightBottom.size()){
            max = mRightBottom.size();
            maxDirection = "RightBottom";
            maxPoints = mRightBottom;
        }

        if (max <= MIN_POINTS_COUNT){
            max = 0;
            maxDirection = "";
            maxPoints = null;
        }
        Log.v(TAG, "Direction: " + maxDirection + "; count: " + max);
        return maxPoints;
    }
}
