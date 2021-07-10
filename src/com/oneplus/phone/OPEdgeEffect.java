package com.oneplus.phone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.SystemProperties;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
public class OPEdgeEffect {
    private static final float COS = ((float) Math.cos(0.5235987755982988d));
    private static final float SIN = ((float) Math.sin(0.5235987755982988d));
    private float mBaseGlowScale;
    private final Rect mBounds = new Rect();
    private float mDisplacement;
    private float mDuration;
    private float mGlowAlpha;
    private float mGlowAlphaFinish;
    private float mGlowAlphaStart;
    private float mGlowScaleY;
    private float mGlowScaleYFinish;
    private float mGlowScaleYStart;
    private final Interpolator mInterpolator;
    private final Paint mPaint;
    private float mPullDistance;
    private float mRadius;
    private long mStartTime;
    private int mState = 0;
    private float mTargetDisplacement;

    public OPEdgeEffect(Context context) {
        Paint paint = new Paint();
        this.mPaint = paint;
        this.mDisplacement = 0.5f;
        this.mTargetDisplacement = 0.5f;
        paint.setAntiAlias(true);
        this.mPaint.setColor(-10066330);
        this.mPaint.setStyle(Paint.Style.FILL);
        this.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.values()[SystemProperties.getInt("persist.gesture_button.mode", 1)]));
        this.mInterpolator = new DecelerateInterpolator();
    }

    public void setSize(int i, int i2) {
        float f = SIN;
        float f2 = (((float) i) * 0.4f) / f;
        float f3 = COS;
        float f4 = f2 - (f3 * f2);
        float f5 = (float) i2;
        float f6 = (0.4f * f5) / f;
        float f7 = f6 - (f3 * f6);
        this.mRadius = f2;
        float f8 = 1.0f;
        if (f4 > 0.0f) {
            f8 = Math.min(f7 / f4, 1.0f);
        }
        this.mBaseGlowScale = f8;
        Rect rect = this.mBounds;
        rect.set(rect.left, rect.top, i, (int) Math.min(f5, f4));
    }

    public boolean isFinished() {
        return this.mState == 0;
    }

    public void finish() {
        this.mState = 0;
    }

    public void onPull(float f) {
        onPull(f, 0.5f);
    }

    public void onPull(float f, float f2) {
        long currentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis();
        this.mTargetDisplacement = f2;
        if (this.mState != 4 || ((float) (currentAnimationTimeMillis - this.mStartTime)) >= this.mDuration) {
            if (this.mState != 1) {
                this.mGlowScaleY = Math.max(0.0f, this.mGlowScaleY);
            }
            this.mState = 1;
            this.mStartTime = currentAnimationTimeMillis;
            this.mDuration = 167.0f;
            this.mPullDistance += f;
            float min = Math.min(0.15f, this.mGlowAlpha + (Math.abs(f) * 0.8f));
            this.mGlowAlphaStart = min;
            this.mGlowAlpha = min;
            if (this.mPullDistance == 0.0f) {
                this.mGlowScaleYStart = 0.0f;
                this.mGlowScaleY = 0.0f;
            } else {
                this.mGlowScaleYStart = f;
                this.mGlowScaleY = f;
            }
            this.mGlowAlphaFinish = this.mGlowAlpha;
            this.mGlowScaleYFinish = this.mGlowScaleY;
        }
    }

    public void onRelease() {
        this.mPullDistance = 0.0f;
        int i = this.mState;
        if (i == 1 || i == 4) {
            this.mState = 3;
            this.mGlowAlphaStart = this.mGlowAlpha;
            this.mGlowScaleYStart = this.mGlowScaleY;
            this.mGlowAlphaFinish = 0.0f;
            this.mGlowScaleYFinish = 0.0f;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mDuration = 600.0f;
        }
    }

    public boolean draw(Canvas canvas) {
        boolean z;
        update();
        int save = canvas.save();
        float centerX = (float) this.mBounds.centerX();
        float height = ((float) this.mBounds.height()) - this.mRadius;
        canvas.scale(1.0f, Math.min(this.mGlowScaleY, 1.0f) * this.mBaseGlowScale, centerX, 0.0f);
        canvas.translate((((float) this.mBounds.width()) * (Math.max(0.0f, Math.min(this.mDisplacement, 1.0f)) - 0.5f)) / 2.0f, 0.0f);
        this.mPaint.setAlpha((int) (this.mGlowAlpha * 255.0f));
        canvas.drawCircle(centerX, height, this.mRadius, this.mPaint);
        canvas.restoreToCount(save);
        if (this.mState == 3 && this.mGlowScaleY == 0.0f) {
            this.mState = 0;
            z = true;
        } else {
            z = false;
        }
        return this.mState != 0 || z;
    }

    private void update() {
        float min = Math.min(((float) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime)) / this.mDuration, 1.0f);
        float interpolation = this.mInterpolator.getInterpolation(min);
        float f = this.mGlowAlphaStart;
        this.mGlowAlpha = f + ((this.mGlowAlphaFinish - f) * interpolation);
        float f2 = this.mGlowScaleYStart;
        this.mGlowScaleY = f2 + ((this.mGlowScaleYFinish - f2) * interpolation);
        this.mDisplacement = (this.mDisplacement + this.mTargetDisplacement) / 2.0f;
        if (min >= 0.999f) {
            int i = this.mState;
            if (i == 1) {
                this.mState = 4;
                this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
                this.mDuration = 2000.0f;
                this.mGlowAlphaStart = this.mGlowAlpha;
                this.mGlowScaleYStart = this.mGlowScaleY;
                this.mGlowAlphaFinish = 0.0f;
                this.mGlowScaleYFinish = 0.0f;
            } else if (i == 2) {
                this.mState = 3;
                this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
                this.mDuration = 600.0f;
                this.mGlowAlphaStart = this.mGlowAlpha;
                this.mGlowScaleYStart = this.mGlowScaleY;
                this.mGlowAlphaFinish = 0.0f;
                this.mGlowScaleYFinish = 0.0f;
            } else if (i == 3) {
                this.mState = 0;
            } else if (i == 4) {
                this.mState = 3;
            }
        }
    }
}
