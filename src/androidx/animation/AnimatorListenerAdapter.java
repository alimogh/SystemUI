package androidx.animation;

import androidx.animation.Animator;
public abstract class AnimatorListenerAdapter extends Animator.AnimatorListener {
    @Override // androidx.animation.Animator.AnimatorListener
    public void onAnimationCancel(Animator animator) {
    }

    @Override // androidx.animation.Animator.AnimatorListener
    public void onAnimationRepeat(Animator animator) {
    }

    @Override // androidx.animation.Animator.AnimatorListener
    public void onAnimationStart(Animator animator) {
    }
}
