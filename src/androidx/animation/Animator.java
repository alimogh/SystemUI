package androidx.animation;

import androidx.animation.AnimationHandler;
import java.util.ArrayList;
public abstract class Animator implements Cloneable {
    ArrayList<AnimatorListener> mListeners = null;
    ArrayList<Object> mPauseListeners = null;
    boolean mPaused = false;
    ArrayList<AnimatorUpdateListener> mUpdateListeners = null;

    public interface AnimatorUpdateListener {
        void onAnimationUpdate(Animator animator);
    }

    public void cancel() {
    }

    public void end() {
    }

    public abstract long getDuration();

    public abstract long getStartDelay();

    /* access modifiers changed from: package-private */
    public boolean isInitialized() {
        return true;
    }

    public abstract boolean isRunning();

    /* access modifiers changed from: package-private */
    public boolean pulseAnimationFrame(long j) {
        return false;
    }

    public abstract Animator setDuration(long j);

    public abstract void setInterpolator(Interpolator interpolator);

    /* access modifiers changed from: package-private */
    public void skipToEndValue(boolean z) {
    }

    public void start() {
    }

    public long getTotalDuration() {
        long duration = getDuration();
        if (duration == -1) {
            return -1;
        }
        return getStartDelay() + duration;
    }

    public boolean isStarted() {
        return isRunning();
    }

    public void addListener(AnimatorListener animatorListener) {
        if (this.mListeners == null) {
            this.mListeners = new ArrayList<>();
        }
        this.mListeners.add(animatorListener);
    }

    public void removeListener(AnimatorListener animatorListener) {
        ArrayList<AnimatorListener> arrayList = this.mListeners;
        if (arrayList != null) {
            arrayList.remove(animatorListener);
            if (this.mListeners.size() == 0) {
                this.mListeners = null;
            }
        }
    }

    @Override // java.lang.Object
    public Animator clone() {
        try {
            Animator animator = (Animator) super.clone();
            if (this.mListeners != null) {
                animator.mListeners = new ArrayList<>(this.mListeners);
            }
            if (this.mPauseListeners != null) {
                animator.mPauseListeners = new ArrayList<>(this.mPauseListeners);
            }
            return animator;
        } catch (CloneNotSupportedException unused) {
            throw new AssertionError();
        }
    }

    static void addAnimationCallback(AnimationHandler.AnimationFrameCallback animationFrameCallback) {
        AnimationHandler.getInstance().addAnimationFrameCallback(animationFrameCallback);
    }

    static void removeAnimationCallback(AnimationHandler.AnimationFrameCallback animationFrameCallback) {
        AnimationHandler.getInstance().removeCallback(animationFrameCallback);
    }

    /* access modifiers changed from: package-private */
    public void reverse() {
        throw new IllegalStateException("Reverse is not supported");
    }

    /* access modifiers changed from: package-private */
    public void startWithoutPulsing(boolean z) {
        if (z) {
            reverse();
        } else {
            start();
        }
    }

    public static abstract class AnimatorListener {
        public abstract void onAnimationCancel(Animator animator);

        public abstract void onAnimationEnd(Animator animator);

        public abstract void onAnimationRepeat(Animator animator);

        public abstract void onAnimationStart(Animator animator);

        public void onAnimationStart(Animator animator, boolean z) {
            onAnimationStart(animator);
        }

        public void onAnimationEnd(Animator animator, boolean z) {
            onAnimationEnd(animator);
        }
    }
}
