package com.oneplus.aod;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Build;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.PathInterpolator;
public class OpWakingUpScrim extends View {
    private float mAnimationFrame;
    private float mAnimationInitAlpha;
    private int mCenterX;
    private int mCenterY;
    private int mCircle1Colr;
    private int mCircle2Colr;
    private int mCircle3Colr;
    private Paint mCirclePaint;
    private int mHeight;
    private Paint mPaint;
    private Path mPath;
    private float mRadius;
    private Paint mTestPaint;
    private boolean mTestUnlockSpeed;
    private int mWidth;
    private int mWithoutDelayAnimationDuration;
    private float mWithoutDelayAnimationStartFrame;

    public OpWakingUpScrim(Context context) {
        this(context, null);
    }

    public OpWakingUpScrim(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpWakingUpScrim(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mWithoutDelayAnimationStartFrame = 0.0f;
        this.mWithoutDelayAnimationDuration = 0;
        this.mAnimationInitAlpha = 1.0f;
        this.mRadius = 0.0f;
        this.mCircle1Colr = -16777216;
        this.mCircle2Colr = -16777216;
        this.mCircle3Colr = -16777216;
        Paint paint = new Paint();
        this.mPaint = paint;
        paint.setStyle(Paint.Style.FILL);
        this.mPaint.setStrokeWidth(10.0f);
        this.mCirclePaint = new Paint();
        Paint paint2 = new Paint();
        this.mTestPaint = paint2;
        paint2.setStyle(Paint.Style.FILL);
        this.mTestPaint.setColor(-1);
        this.mTestPaint.setTextSize(100.0f);
        this.mPath = new Path();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        this.mWidth = i;
        this.mHeight = i2;
        this.mCenterX = i / 2;
        this.mCenterY = i2 / 2;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mPath.reset();
        this.mPath.addRect(0.0f, 0.0f, (float) this.mWidth, (float) this.mHeight, Path.Direction.CCW);
        this.mPaint.setColor(this.mCircle3Colr);
        float f = this.mRadius;
        if (f > 0.0f) {
            this.mPath.addCircle((float) this.mCenterX, (float) this.mCenterY, f, Path.Direction.CW);
            this.mPath.setFillType(Path.FillType.EVEN_ODD);
            canvas.drawPath(this.mPath, this.mPaint);
            canvas.save();
            this.mCirclePaint.setShader(new RadialGradient((float) this.mCenterX, (float) this.mCenterY, this.mRadius, new int[]{this.mCircle1Colr, this.mCircle2Colr, this.mCircle3Colr}, (float[]) null, Shader.TileMode.CLAMP));
            canvas.drawCircle((float) this.mCenterX, (float) this.mCenterY, this.mRadius, this.mCirclePaint);
            canvas.restore();
            if (this.mTestUnlockSpeed) {
                canvas.save();
                canvas.drawText(Float.toString(this.mAnimationFrame), 200.0f, 200.0f, this.mTestPaint);
                canvas.restore();
                if (Build.DEBUG_ONEPLUS) {
                    Log.i("OpWakingUpScrim", "mTestUnlockSpeed draw mRadius: " + this.mAnimationFrame);
                    return;
                }
                return;
            }
            return;
        }
        this.mPath.setFillType(Path.FillType.WINDING);
        canvas.drawPath(this.mPath, this.mPaint);
    }

    public void reset() {
        setAlpha(1.0f);
        this.mRadius = 0.0f;
        boolean z = SystemProperties.getBoolean("debug.wakingup.scrim", false);
        this.mTestUnlockSpeed = z;
        if (z) {
            this.mWithoutDelayAnimationStartFrame = ((float) SystemProperties.getInt("debug.wakingup.scrim.animation.start.frame", 0)) / 100.0f;
            this.mWithoutDelayAnimationDuration = SystemProperties.getInt("debug.wakingup.scrim.animation.start.duration", 0);
            this.mAnimationInitAlpha = ((float) SystemProperties.getInt("debug.wakingup.scrim.animation.init.alpha", 100)) / 100.0f;
            if (Build.DEBUG_ONEPLUS) {
                Log.i("OpWakingUpScrim", "debug AnimationStartFrame:" + this.mWithoutDelayAnimationStartFrame + " AnimationDuration:" + this.mWithoutDelayAnimationDuration + " mAnimationInitAlpha:" + this.mAnimationInitAlpha);
            }
        }
        if (SystemProperties.getBoolean("debug.wakingup.scrim2", false)) {
            String str = "#" + SystemProperties.get("debug.wakingup.scrim.color", "FF000000");
            int parseColor = Color.parseColor(str);
            this.mCircle1Colr = parseColor;
            this.mCircle2Colr = parseColor;
            this.mCircle3Colr = parseColor;
            if (Build.DEBUG_ONEPLUS) {
                Log.i("OpWakingUpScrim", "debug mCircle3Colr:" + Integer.toHexString(this.mCircle3Colr) + " debugColor:" + Integer.toHexString(parseColor) + " debugColorText:" + str);
            }
        }
        invalidate();
    }

    public Animator getDisappearAnimationWithoutDelay(boolean z) {
        int i;
        float f = this.mWithoutDelayAnimationStartFrame;
        if (f <= 0.0f) {
            f = 0.5f;
        }
        int i2 = 475;
        if (z && (i = this.mWithoutDelayAnimationDuration) > 0) {
            i2 = i;
        }
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(f, 1.0f);
        ofFloat.setDuration((long) i2);
        ofFloat.setInterpolator(new PathInterpolator(0.3f, 0.0f, 0.4f, 1.0f));
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.aod.OpWakingUpScrim.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                OpWakingUpScrim opWakingUpScrim = OpWakingUpScrim.this;
                opWakingUpScrim.mRadius = (((7.7f * floatValue) + 0.5f) * ((float) opWakingUpScrim.mWidth)) / 2.0f;
                OpWakingUpScrim.this.mAnimationFrame = floatValue;
                if (Build.DEBUG_ONEPLUS) {
                    Log.i("OpWakingUpScrim", "DisappearAnimationWithoutDelay mRadius:" + floatValue);
                }
                OpWakingUpScrim.this.calculateCircleColor(floatValue);
                OpWakingUpScrim.this.invalidate();
            }
        });
        ofFloat.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.aod.OpWakingUpScrim.2
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                OpWakingUpScrim opWakingUpScrim = OpWakingUpScrim.this;
                opWakingUpScrim.setAlpha(opWakingUpScrim.mAnimationInitAlpha);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                OpWakingUpScrim.this.setAlpha(1.0f);
                if (Build.DEBUG_ONEPLUS) {
                    Log.i("OpWakingUpScrim", "onAnimationEnd");
                }
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                if (Build.DEBUG_ONEPLUS) {
                    Log.i("OpWakingUpScrim", "onAnimationCancel");
                }
            }
        });
        animatorSet.play(ofFloat);
        return animatorSet;
    }

    public AnimatorSet getDisappearAnimationWithDelay() {
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setInterpolator(new PathInterpolator(0.4f, 0.0f, 0.3f, 1.0f));
        ofFloat.setDuration(475L);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.aod.OpWakingUpScrim.3
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                OpWakingUpScrim opWakingUpScrim = OpWakingUpScrim.this;
                opWakingUpScrim.mRadius = (((7.7f * floatValue) + 0.5f) * ((float) opWakingUpScrim.mWidth)) / 2.0f;
                OpWakingUpScrim.this.mAnimationFrame = floatValue;
                if (Build.DEBUG_ONEPLUS) {
                    Log.i("OpWakingUpScrim", "circleAnimator mRadius:" + floatValue);
                }
                OpWakingUpScrim.this.calculateCircleColor(floatValue);
                OpWakingUpScrim.this.invalidate();
            }
        });
        ofFloat.addListener(new Animator.AnimatorListener(this) { // from class: com.oneplus.aod.OpWakingUpScrim.4
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (Build.DEBUG_ONEPLUS) {
                    Log.i("OpWakingUpScrim", "circleAnimator onAnimationEnd");
                }
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                if (Build.DEBUG_ONEPLUS) {
                    Log.i("OpWakingUpScrim", "circleAnimator onAnimationCancel");
                }
            }
        });
        animatorSet.playTogether(ofFloat);
        return animatorSet;
    }

    private int getColor(float f) {
        return (this.mCircle3Colr & 16777215) | (((int) (f * 255.0f)) << 24);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void calculateCircleColor(float f) {
        float f2;
        float f3;
        float f4 = (((float) this.mWidth) * 8.2f) / 2.0f;
        float f5 = f4 / 2.0f;
        float f6 = (9.0f * f4) / 10.0f;
        float f7 = this.mRadius;
        float f8 = 0.0f;
        float f9 = 0.91f;
        if (f4 >= f7 && f7 >= f5) {
            if (f7 > f6) {
                f3 = ((f4 - f7) / (f4 - f6)) * 0.91f;
            } else {
                float f10 = 1.0f - f;
                if (0.91f < f10) {
                    f9 = f10;
                }
                f3 = f9;
            }
            f8 = ((f4 - this.mRadius) / (f4 - f5)) * 0.81f * f3;
            f2 = f3;
        } else if (this.mRadius < f5) {
            float f11 = 1.0f - f;
            float f12 = 0.91f < f11 ? f11 : 0.91f;
            float f13 = 0.81f * f12;
            if (f11 > f13) {
                f13 = f11;
            }
            f2 = f12;
            f8 = f13;
        } else {
            f2 = 0.0f;
        }
        this.mCircle1Colr = getColor(f8);
        this.mCircle2Colr = getColor(f2);
    }
}
