package com.oneplus.phone;

import android.graphics.PointF;
public class GesturePointContainer {
    private PointF mPoint;
    private int mRotation;
    private int mState;

    public GesturePointContainer(PointF pointF, int i, int i2, int i3, int i4, int i5) {
        this.mPoint = pointF;
        this.mState = i;
        this.mRotation = i3;
    }

    public PointF getPoint() {
        return this.mPoint;
    }

    public int getState() {
        return this.mState;
    }

    public int getRotation() {
        return this.mRotation;
    }
}
