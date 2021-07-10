package com.oneolus.anim;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.PathInterpolator;
import com.oneplus.util.OpUtils;
public class OpFadeAnim {
    public static AnimatorSet getFadeInOutVisibilityAnimation(final View view, int i, Float f, boolean z) {
        final float f2;
        final float f3;
        if (z || view.getVisibility() != i) {
            if (i == 0) {
                f3 = 1.0f;
                f2 = 0.0f;
            } else if (i != 8 && i != 4) {
                return null;
            } else {
                f2 = 1.0f;
                f3 = 0.0f;
            }
            if (f != null) {
                f2 = f.floatValue();
            }
            if (OpUtils.DEBUG_ONEPLUS) {
                Log.i("OpFadeAnim", "initValue:" + f2 + " / endValue:" + f3);
            }
            new ValueAnimator();
            ValueAnimator ofFloat = ValueAnimator.ofFloat(f2, f3);
            ofFloat.setInterpolator(new PathInterpolator(0.4f, 0.0f, 0.6f, 1.0f));
            ofFloat.setDuration(225L);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneolus.anim.OpFadeAnim.1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    view.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
                }
            });
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(225L);
            animatorSet.addListener(new Animator.AnimatorListener() { // from class: com.oneolus.anim.OpFadeAnim.2
                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationRepeat(Animator animator) {
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    view.setAlpha(f2);
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    if (OpUtils.DEBUG_ONEPLUS) {
                        Log.i("OpFadeAnim", "onAnimationEnd");
                    }
                    view.setAlpha(f3);
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animator) {
                    if (OpUtils.DEBUG_ONEPLUS) {
                        Log.i("OpFadeAnim", "onAnimationCancel");
                    }
                    view.setAlpha(f3);
                }
            });
            animatorSet.play(ofFloat);
            return animatorSet;
        }
        Log.i("OpFadeAnim", "return null");
        return null;
    }
}
