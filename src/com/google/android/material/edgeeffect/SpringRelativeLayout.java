package com.google.android.material.edgeeffect;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.EdgeEffect;
import android.widget.RelativeLayout;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
public class SpringRelativeLayout extends RelativeLayout {
    private static final FloatPropertyCompat<SpringRelativeLayout> DAMPED_SCROLL = new FloatPropertyCompat<SpringRelativeLayout>("value") { // from class: com.google.android.material.edgeeffect.SpringRelativeLayout.1
        public float getValue(SpringRelativeLayout springRelativeLayout) {
            return springRelativeLayout.mDampedScrollShift;
        }

        public void setValue(SpringRelativeLayout springRelativeLayout, float f) {
            springRelativeLayout.setDampedScrollShift(f);
        }
    };
    private DynamicAnimation.OnAnimationEndListener mAnimationEndListener;
    private float mDampedScrollShift;
    private boolean mDisableEffectBottom;
    private boolean mDisableEffectTop;
    private boolean mHorizontal;
    private int mPullCount;
    private final SpringAnimation mSpring;
    protected final SparseBooleanArray mSpringViews;

    public static class SEdgeEffectFactory {
        /* access modifiers changed from: protected */
        public abstract EdgeEffect createEdgeEffect(View view, int i);
    }

    public int getCanvasClipLeftForOverscroll() {
        return 0;
    }

    public int getCanvasClipTopForOverscroll() {
        return 0;
    }

    public SpringRelativeLayout(Context context) {
        this(context, null);
    }

    public SpringRelativeLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SpringRelativeLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDisableEffectTop = false;
        this.mDisableEffectBottom = false;
        this.mSpringViews = new SparseBooleanArray();
        this.mDampedScrollShift = 0.0f;
        this.mHorizontal = false;
        this.mPullCount = 0;
        SpringAnimation springAnimation = new SpringAnimation(this, DAMPED_SCROLL, 0.0f);
        this.mSpring = springAnimation;
        SpringForce springForce = new SpringForce(0.0f);
        springForce.setStiffness(590.0f);
        springForce.setDampingRatio(0.5f);
        springAnimation.setSpring(springForce);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean drawChild(Canvas canvas, View view, long j) {
        if (this.mDampedScrollShift == 0.0f || !this.mSpringViews.get(view.getId())) {
            return super.drawChild(canvas, view, j);
        }
        int save = canvas.save();
        if (this.mHorizontal) {
            canvas.clipRect(getCanvasClipLeftForOverscroll(), 0, getWidth(), getHeight());
            canvas.translate(this.mDampedScrollShift, 0.0f);
        } else {
            canvas.clipRect(0, getCanvasClipTopForOverscroll(), getWidth(), getHeight());
            canvas.translate(0.0f, this.mDampedScrollShift);
        }
        boolean drawChild = super.drawChild(canvas, view, j);
        canvas.restoreToCount(save);
        return drawChild;
    }

    /* access modifiers changed from: protected */
    public void setDampedScrollShift(float f) {
        if (f != this.mDampedScrollShift) {
            this.mDampedScrollShift = f;
            invalidate();
        }
    }

    public void onRecyclerViewScrolled() {
        if (this.mPullCount != 1 && !this.mSpring.isRunning()) {
            this.mPullCount = 0;
            finishScrollWithVelocity(0.0f);
        }
    }

    private void finishScrollWithVelocity(float f) {
        float f2 = this.mDampedScrollShift;
        if (f2 > Float.MAX_VALUE || f2 < -3.4028235E38f) {
            Log.e("SpringRelativeLayout", "animation parameter out of range!");
        } else if (f > 0.0f && this.mDisableEffectTop) {
        } else {
            if (f >= 0.0f || !this.mDisableEffectBottom) {
                DynamicAnimation.OnAnimationEndListener onAnimationEndListener = this.mAnimationEndListener;
                if (onAnimationEndListener != null) {
                    this.mSpring.addEndListener(onAnimationEndListener);
                }
                this.mSpring.setStartVelocity(f);
                this.mSpring.setStartValue(this.mDampedScrollShift);
                this.mSpring.start();
            }
        }
    }
}
