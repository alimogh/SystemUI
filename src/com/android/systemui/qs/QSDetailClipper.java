package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.drawable.TransitionDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewAnimationUtils;
public class QSDetailClipper {
    private Animator mAnimator;
    private final TransitionDrawable mBackground;
    private final View mDetail;
    private final AnimatorListenerAdapter mGoneOnEnd = new AnimatorListenerAdapter() { // from class: com.android.systemui.qs.QSDetailClipper.2
        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            QSDetailClipper.this.mDetail.setVisibility(8);
            QSDetailClipper.this.mBackground.resetTransition();
            QSDetailClipper.this.mAnimator = null;
        }
    };
    private final AnimatorListenerAdapter mVisibleOnStart = new AnimatorListenerAdapter() { // from class: com.android.systemui.qs.QSDetailClipper.1
        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animator) {
            QSDetailClipper.this.mDetail.setVisibility(0);
        }

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            QSDetailClipper.this.mAnimator = null;
        }
    };

    public QSDetailClipper(View view) {
        this.mDetail = view;
        this.mBackground = (TransitionDrawable) view.getBackground();
    }

    public void animateCircularClip(int i, int i2, boolean z, Animator.AnimatorListener animatorListener) {
        animateCircularClip(i, i2, z, animatorListener, 0);
    }

    public void animateCircularClip(int i, int i2, boolean z, Animator.AnimatorListener animatorListener, int i3) {
        Animator animator = this.mAnimator;
        if (animator != null) {
            animator.cancel();
        }
        if (this.mDetail.isAttachedToWindow()) {
            int width = this.mDetail.getWidth();
            int height = this.mDetail.getHeight();
            if (width <= 0 || height <= 0) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                this.mDetail.getDisplay().getMetrics(displayMetrics);
                int i4 = displayMetrics.widthPixels;
                height = displayMetrics.heightPixels;
                width = i4;
            }
            int i5 = width - i;
            int i6 = height - i2;
            int i7 = 0;
            if (i < 0 || i5 < 0 || i2 < 0 || i6 < 0) {
                i7 = Math.min(Math.min(Math.min(Math.abs(i), Math.abs(i2)), Math.abs(i5)), Math.abs(i6));
            }
            int i8 = i * i;
            int i9 = i2 * i2;
            int i10 = i5 * i5;
            int i11 = i6 * i6;
            int max = (int) Math.max((double) ((int) Math.max((double) ((int) Math.max((double) ((int) Math.ceil(Math.sqrt((double) (i8 + i9)))), Math.ceil(Math.sqrt((double) (i9 + i10))))), Math.ceil(Math.sqrt((double) (i10 + i11))))), Math.ceil(Math.sqrt((double) (i8 + i11))));
            if (z) {
                this.mAnimator = ViewAnimationUtils.createCircularReveal(this.mDetail, i, i2, (float) i7, (float) max);
            } else {
                this.mAnimator = ViewAnimationUtils.createCircularReveal(this.mDetail, i, i2, (float) max, (float) i7);
            }
            Animator animator2 = this.mAnimator;
            animator2.setDuration((long) (((double) animator2.getDuration()) * 1.5d));
            if (animatorListener != null) {
                this.mAnimator.addListener(animatorListener);
            }
            if (z) {
                this.mBackground.startTransition((int) (((double) this.mAnimator.getDuration()) * 0.6d));
                this.mAnimator.addListener(this.mVisibleOnStart);
            } else {
                this.mAnimator.addListener(this.mGoneOnEnd);
            }
            this.mAnimator.setStartDelay((long) i3);
            this.mAnimator.start();
        }
    }

    public void showBackground() {
        this.mBackground.showSecondLayer();
    }

    public void cancelAnimator() {
        Animator animator = this.mAnimator;
        if (animator != null) {
            animator.cancel();
        }
    }
}
