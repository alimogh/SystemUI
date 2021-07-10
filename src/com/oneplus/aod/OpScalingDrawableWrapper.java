package com.oneplus.aod;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
class OpScalingDrawableWrapper extends DrawableWrapper {
    private float mScaleFactor;

    public OpScalingDrawableWrapper(Drawable drawable, float f) {
        super(drawable);
        this.mScaleFactor = f;
    }

    @Override // android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return (int) (((float) super.getIntrinsicWidth()) * this.mScaleFactor);
    }

    @Override // android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return (int) (((float) super.getIntrinsicHeight()) * this.mScaleFactor);
    }
}
