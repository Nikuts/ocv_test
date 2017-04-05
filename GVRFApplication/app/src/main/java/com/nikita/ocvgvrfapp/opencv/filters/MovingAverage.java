package com.nikita.ocvgvrfapp.opencv.filters;

import org.opencv.core.Point;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by n.kutsachenk on 4/5/2017.
 */

public class MovingAverage {
    Deque<Double> mWindowX = new ArrayDeque<>();
    Deque<Double> mWindowY = new ArrayDeque<>();

    int mPeriod = 0;

    float mSumX = 0;
    float mSumY = 0;

    public MovingAverage(int period) {
        mPeriod = period;
    }

    public Point filter(Point point) {
        mSumX += point.x;
        mSumY += point.y;
        mWindowX.addLast(point.x);
        mWindowY.addLast(point.y);
        if (mWindowX.size() > mPeriod) {
            mSumX -= mWindowX.pollFirst();
        }
        if (mWindowY.size() > mPeriod) {
            mSumY -= mWindowY.pollFirst();
        }

        return new Point(mSumX / mWindowX.size(), mSumY/mWindowY.size());
    }
}
