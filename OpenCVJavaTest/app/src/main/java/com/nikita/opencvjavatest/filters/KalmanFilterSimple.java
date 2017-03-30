package com.nikita.opencvjavatest.filters;

import org.opencv.core.Point;

/**
 * Created by n.kutsachenk on 3/29/2017.
 */

public class KalmanFilterSimple {

    private Point X0 = new Point(); // predicted state
    private Point P0 = new Point(); // predicted covariance

    private float F; // factor of real value to previous real value
    private float Q; // measurement noise
    private float H; // factor of measured value to real value
    private float R; // environment noise

    private Point mState = new Point();
    private Point mCovariance = new Point();

    public KalmanFilterSimple(float q, float r, float f, float h) //f = 1, h =1
    {
        Q = q;
        R = r;
        F = f;
        H = h;
    }

    public void setState(Point state, Point covariance)
    {
        mState = state;
        mCovariance = covariance;
    }

    public Point correct(Point data)
    {
        //time update - prediction
        X0.x = F * mState.x;
        P0.x = F * mCovariance.x * F + Q;
        X0.y = F * mState.y;
        P0.y = F * mCovariance.y * F + Q;

        //measurement update - correction
        Point K = new Point();
        K.x = H * P0.x / (H * P0.x * H + R);
        K.y = H * P0.y / (H * P0.y * H + R);
        mState.x = X0.x + K.x * (data.x - H * X0.x);
        mState.y = X0.y + K.y * (data.y - H * X0.y);
        mCovariance.x = (1 - K.x * H) * P0.x;
        mCovariance.y = (1 - K.y * H) * P0.y;
        return mState;
    }

}
