package com.oneplus.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.animation.Interpolator;
import com.android.settingslib.Utils;
import com.android.systemui.C0002R$attr;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.Interpolators;
public class OpTrustDrawable extends Drawable {
    private int mAlpha;
    private final ValueAnimator.AnimatorUpdateListener mAlphaUpdateListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.phone.OpTrustDrawable.1
        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            OpTrustDrawable.this.mCurAlpha = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            OpTrustDrawable.this.invalidateSelf();
        }
    };
    private boolean mAnimating;
    private int mCurAlpha;
    private Animator mCurAnimator;
    private float mCurInnerRadius;
    private final float mInnerRadiusEnter;
    private final float mInnerRadiusExit;
    private final float mInnerRadiusVisibleMax;
    private final float mInnerRadiusVisibleMin;
    private Paint mPaint;
    private final ValueAnimator.AnimatorUpdateListener mRadiusUpdateListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.phone.OpTrustDrawable.2
        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            OpTrustDrawable.this.mCurInnerRadius = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            OpTrustDrawable.this.invalidateSelf();
        }
    };
    private int mState = -1;
    private final float mThickness;
    private boolean mTrustManaged;
    private final Animator mVisibleAnimator;

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    public OpTrustDrawable(Context context) {
        Resources resources = context.getResources();
        this.mInnerRadiusVisibleMin = resources.getDimension(C0005R$dimen.trust_circle_inner_radius_visible_min);
        this.mInnerRadiusVisibleMax = resources.getDimension(C0005R$dimen.trust_circle_inner_radius_visible_max);
        this.mInnerRadiusExit = resources.getDimension(C0005R$dimen.trust_circle_inner_radius_exit);
        this.mInnerRadiusEnter = resources.getDimension(C0005R$dimen.trust_circle_inner_radius_enter);
        this.mThickness = resources.getDimension(C0005R$dimen.trust_circle_thickness);
        this.mCurInnerRadius = this.mInnerRadiusEnter;
        this.mVisibleAnimator = makeVisibleAnimator();
        Paint paint = new Paint();
        this.mPaint = paint;
        paint.setStyle(Paint.Style.STROKE);
        this.mPaint.setColor(Utils.getColorAttrDefaultColor(context, C0002R$attr.wallpaperTextColor));
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStrokeWidth(this.mThickness);
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        int i = (this.mCurAlpha * this.mAlpha) / 256;
        if (i != 0) {
            Rect bounds = getBounds();
            this.mPaint.setAlpha(i);
            canvas.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), this.mCurInnerRadius, this.mPaint);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.mAlpha = i;
    }

    @Override // android.graphics.drawable.Drawable
    public int getAlpha() {
        return this.mAlpha;
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        throw new UnsupportedOperationException("not implemented");
    }

    public void start() {
        if (!this.mAnimating) {
            this.mAnimating = true;
            updateState(true);
            invalidateSelf();
        }
    }

    public void stop() {
        if (this.mAnimating) {
            this.mAnimating = false;
            Animator animator = this.mCurAnimator;
            if (animator != null) {
                animator.cancel();
                this.mCurAnimator = null;
            }
            this.mState = -1;
            this.mCurAlpha = 0;
            this.mCurInnerRadius = this.mInnerRadiusEnter;
            invalidateSelf();
        }
    }

    public void setTrustManaged(boolean z) {
        if (z != this.mTrustManaged || this.mState == -1) {
            this.mTrustManaged = z;
            updateState(true);
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:24:0x0034 */
    /* JADX DEBUG: Multi-variable search result rejected for r0v9, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001c, code lost:
        if (r6.mTrustManaged == false) goto L_0x001e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0024, code lost:
        if (r6.mTrustManaged == false) goto L_0x001e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x002b, code lost:
        if (r6.mTrustManaged != false) goto L_0x0016;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0014, code lost:
        if (r6.mTrustManaged != false) goto L_0x0016;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateState(boolean r7) {
        /*
            r6 = this;
            boolean r0 = r6.mAnimating
            if (r0 != 0) goto L_0x0005
            return
        L_0x0005:
            int r0 = r6.mState
            r1 = -1
            r2 = 2
            r3 = 3
            r4 = 1
            if (r0 != r1) goto L_0x0010
            boolean r0 = r6.mTrustManaged
            goto L_0x002e
        L_0x0010:
            if (r0 != 0) goto L_0x0018
            boolean r5 = r6.mTrustManaged
            if (r5 == 0) goto L_0x002e
        L_0x0016:
            r0 = r4
            goto L_0x002e
        L_0x0018:
            if (r0 != r4) goto L_0x0020
            boolean r5 = r6.mTrustManaged
            if (r5 != 0) goto L_0x002e
        L_0x001e:
            r0 = r3
            goto L_0x002e
        L_0x0020:
            if (r0 != r2) goto L_0x0027
            boolean r5 = r6.mTrustManaged
            if (r5 != 0) goto L_0x002e
            goto L_0x001e
        L_0x0027:
            if (r0 != r3) goto L_0x002e
            boolean r5 = r6.mTrustManaged
            if (r5 == 0) goto L_0x002e
            goto L_0x0016
        L_0x002e:
            r5 = 0
            if (r7 != 0) goto L_0x0037
            if (r0 != r4) goto L_0x0034
            r0 = r2
        L_0x0034:
            if (r0 != r3) goto L_0x0037
            r0 = r5
        L_0x0037:
            int r7 = r6.mState
            if (r0 == r7) goto L_0x008b
            android.animation.Animator r7 = r6.mCurAnimator
            if (r7 == 0) goto L_0x0045
            r7.cancel()
            r7 = 0
            r6.mCurAnimator = r7
        L_0x0045:
            if (r0 != 0) goto L_0x004e
            r6.mCurAlpha = r5
            float r7 = r6.mInnerRadiusEnter
            r6.mCurInnerRadius = r7
            goto L_0x007f
        L_0x004e:
            if (r0 != r4) goto L_0x0064
            float r7 = r6.mCurInnerRadius
            int r2 = r6.mCurAlpha
            android.animation.Animator r7 = r6.makeEnterAnimator(r7, r2)
            r6.mCurAnimator = r7
            int r2 = r6.mState
            if (r2 != r1) goto L_0x007f
            r1 = 200(0xc8, double:9.9E-322)
            r7.setStartDelay(r1)
            goto L_0x007f
        L_0x0064:
            if (r0 != r2) goto L_0x0073
            r7 = 76
            r6.mCurAlpha = r7
            float r7 = r6.mInnerRadiusVisibleMax
            r6.mCurInnerRadius = r7
            android.animation.Animator r7 = r6.mVisibleAnimator
            r6.mCurAnimator = r7
            goto L_0x007f
        L_0x0073:
            if (r0 != r3) goto L_0x007f
            float r7 = r6.mCurInnerRadius
            int r1 = r6.mCurAlpha
            android.animation.Animator r7 = r6.makeExitAnimator(r7, r1)
            r6.mCurAnimator = r7
        L_0x007f:
            r6.mState = r0
            android.animation.Animator r7 = r6.mCurAnimator
            if (r7 == 0) goto L_0x0088
            r7.start()
        L_0x0088:
            r6.invalidateSelf()
        L_0x008b:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.phone.OpTrustDrawable.updateState(boolean):void");
    }

    private Animator makeVisibleAnimator() {
        return makeAnimators(this.mInnerRadiusVisibleMax, this.mInnerRadiusVisibleMin, 76, 38, 1000, Interpolators.ACCELERATE_DECELERATE, true, false);
    }

    private Animator makeEnterAnimator(float f, int i) {
        return makeAnimators(f, this.mInnerRadiusVisibleMax, i, 76, 500, Interpolators.LINEAR_OUT_SLOW_IN, false, true);
    }

    private Animator makeExitAnimator(float f, int i) {
        return makeAnimators(f, this.mInnerRadiusExit, i, 0, 500, Interpolators.FAST_OUT_SLOW_IN, false, true);
    }

    private Animator makeAnimators(float f, float f2, int i, int i2, long j, Interpolator interpolator, boolean z, boolean z2) {
        ValueAnimator ofInt = ValueAnimator.ofInt(i, i2);
        configureAnimator(ofInt, j, this.mAlphaUpdateListener, interpolator, z);
        ValueAnimator ofFloat = ValueAnimator.ofFloat(f, f2);
        configureAnimator(ofFloat, j, this.mRadiusUpdateListener, interpolator, z);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ofInt, ofFloat);
        if (z2) {
            animatorSet.addListener(new StateUpdateAnimatorListener());
        }
        return animatorSet;
    }

    private ValueAnimator configureAnimator(ValueAnimator valueAnimator, long j, ValueAnimator.AnimatorUpdateListener animatorUpdateListener, Interpolator interpolator, boolean z) {
        valueAnimator.setDuration(j);
        valueAnimator.addUpdateListener(animatorUpdateListener);
        valueAnimator.setInterpolator(interpolator);
        if (z) {
            valueAnimator.setRepeatCount(-1);
            valueAnimator.setRepeatMode(2);
        }
        return valueAnimator;
    }

    /* access modifiers changed from: private */
    public class StateUpdateAnimatorListener extends AnimatorListenerAdapter {
        boolean mCancelled;

        private StateUpdateAnimatorListener() {
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animator) {
            this.mCancelled = false;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animator) {
            this.mCancelled = true;
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            if (!this.mCancelled) {
                OpTrustDrawable.this.updateState(false);
            }
        }
    }
}
