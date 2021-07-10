package androidx.animation;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Choreographer;
import androidx.collection.SimpleArrayMap;
import java.util.ArrayList;
/* access modifiers changed from: package-private */
public class AnimationHandler {
    private static ThreadLocal<AnimationCallbackData> mAnimationCallbackData = new ThreadLocal<>();
    private static final ThreadLocal<Handler> mHandler = new ThreadLocal<>();
    public static AnimationHandler sAnimationHandler = null;
    private static AnimationHandler sTestHandler = null;
    private final AnimationFrameCallbackProvider mProvider;

    /* access modifiers changed from: package-private */
    public interface AnimationFrameCallback {
        boolean doAnimationFrame(long j);
    }

    /* access modifiers changed from: package-private */
    public interface AnimationFrameCallbackProvider {
        void onNewCallbackAdded(AnimationFrameCallback animationFrameCallback);

        void postFrameCallback();
    }

    /* access modifiers changed from: package-private */
    public void onAnimationFrame(long j) {
        doAnimationFrame(j);
        if (getAnimationCallbacks().size() > 0) {
            this.mProvider.postFrameCallback();
        }
    }

    /* access modifiers changed from: package-private */
    public static class AnimationCallbackData {
        final ArrayList<AnimationFrameCallback> mAnimationCallbacks = new ArrayList<>();
        final SimpleArrayMap<AnimationFrameCallback, Long> mDelayedCallbackStartTime = new SimpleArrayMap<>();
        boolean mListDirty = false;

        AnimationCallbackData() {
        }
    }

    AnimationHandler(AnimationFrameCallbackProvider animationFrameCallbackProvider) {
        if (animationFrameCallbackProvider != null) {
            this.mProvider = animationFrameCallbackProvider;
        } else if (Build.VERSION.SDK_INT >= 16) {
            this.mProvider = new FrameCallbackProvider16();
        } else {
            this.mProvider = new FrameCallbackProvider14();
        }
    }

    public static AnimationHandler getInstance() {
        AnimationHandler animationHandler = sTestHandler;
        if (animationHandler != null) {
            return animationHandler;
        }
        if (sAnimationHandler == null) {
            sAnimationHandler = new AnimationHandler(null);
        }
        return sAnimationHandler;
    }

    private SimpleArrayMap<AnimationFrameCallback, Long> getDelayedCallbackStartTime() {
        AnimationCallbackData animationCallbackData = mAnimationCallbackData.get();
        if (animationCallbackData == null) {
            animationCallbackData = new AnimationCallbackData();
            mAnimationCallbackData.set(animationCallbackData);
        }
        return animationCallbackData.mDelayedCallbackStartTime;
    }

    private ArrayList<AnimationFrameCallback> getAnimationCallbacks() {
        AnimationCallbackData animationCallbackData = mAnimationCallbackData.get();
        if (animationCallbackData == null) {
            animationCallbackData = new AnimationCallbackData();
            mAnimationCallbackData.set(animationCallbackData);
        }
        return animationCallbackData.mAnimationCallbacks;
    }

    private boolean isListDirty() {
        AnimationCallbackData animationCallbackData = mAnimationCallbackData.get();
        if (animationCallbackData == null) {
            animationCallbackData = new AnimationCallbackData();
            mAnimationCallbackData.set(animationCallbackData);
        }
        return animationCallbackData.mListDirty;
    }

    private void setListDirty(boolean z) {
        AnimationCallbackData animationCallbackData = mAnimationCallbackData.get();
        if (animationCallbackData == null) {
            animationCallbackData = new AnimationCallbackData();
            mAnimationCallbackData.set(animationCallbackData);
        }
        animationCallbackData.mListDirty = z;
    }

    /* access modifiers changed from: package-private */
    public void addAnimationFrameCallback(AnimationFrameCallback animationFrameCallback) {
        if (getAnimationCallbacks().size() == 0) {
            this.mProvider.postFrameCallback();
        }
        if (!getAnimationCallbacks().contains(animationFrameCallback)) {
            getAnimationCallbacks().add(animationFrameCallback);
        }
        this.mProvider.onNewCallbackAdded(animationFrameCallback);
    }

    public void removeCallback(AnimationFrameCallback animationFrameCallback) {
        getDelayedCallbackStartTime().remove(animationFrameCallback);
        int indexOf = getAnimationCallbacks().indexOf(animationFrameCallback);
        if (indexOf >= 0) {
            getAnimationCallbacks().set(indexOf, null);
            setListDirty(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void autoCancelBasedOn(ObjectAnimator objectAnimator) {
        for (int size = getAnimationCallbacks().size() - 1; size >= 0; size--) {
            AnimationFrameCallback animationFrameCallback = getAnimationCallbacks().get(size);
            if (animationFrameCallback != null && objectAnimator.shouldAutoCancel(animationFrameCallback)) {
                ((Animator) getAnimationCallbacks().get(size)).cancel();
            }
        }
    }

    private void doAnimationFrame(long j) {
        long uptimeMillis = SystemClock.uptimeMillis();
        for (int i = 0; i < getAnimationCallbacks().size(); i++) {
            AnimationFrameCallback animationFrameCallback = getAnimationCallbacks().get(i);
            if (animationFrameCallback != null && isCallbackDue(animationFrameCallback, uptimeMillis)) {
                animationFrameCallback.doAnimationFrame(j);
            }
        }
        cleanUpList();
    }

    private boolean isCallbackDue(AnimationFrameCallback animationFrameCallback, long j) {
        Long l = getDelayedCallbackStartTime().get(animationFrameCallback);
        if (l == null) {
            return true;
        }
        if (l.longValue() >= j) {
            return false;
        }
        getDelayedCallbackStartTime().remove(animationFrameCallback);
        return true;
    }

    private void cleanUpList() {
        if (isListDirty()) {
            for (int size = getAnimationCallbacks().size() - 1; size >= 0; size--) {
                if (getAnimationCallbacks().get(size) == null) {
                    getAnimationCallbacks().remove(size);
                }
            }
            setListDirty(false);
        }
    }

    private class FrameCallbackProvider16 implements AnimationFrameCallbackProvider, Choreographer.FrameCallback {
        @Override // androidx.animation.AnimationHandler.AnimationFrameCallbackProvider
        public void onNewCallbackAdded(AnimationFrameCallback animationFrameCallback) {
        }

        FrameCallbackProvider16() {
        }

        @Override // android.view.Choreographer.FrameCallback
        public void doFrame(long j) {
            AnimationHandler.this.onAnimationFrame(j / 1000000);
        }

        @Override // androidx.animation.AnimationHandler.AnimationFrameCallbackProvider
        public void postFrameCallback() {
            Choreographer.getInstance().postFrameCallback(this);
        }
    }

    private class FrameCallbackProvider14 implements AnimationFrameCallbackProvider, Runnable {
        private long mFrameDelay = 16;
        private long mLastFrameTime = -1;

        @Override // androidx.animation.AnimationHandler.AnimationFrameCallbackProvider
        public void onNewCallbackAdded(AnimationFrameCallback animationFrameCallback) {
        }

        FrameCallbackProvider14() {
        }

        /* access modifiers changed from: package-private */
        public Handler getHandler() {
            if (AnimationHandler.mHandler.get() == null) {
                AnimationHandler.mHandler.set(new Handler(Looper.myLooper()));
            }
            return (Handler) AnimationHandler.mHandler.get();
        }

        @Override // java.lang.Runnable
        public void run() {
            long uptimeMillis = SystemClock.uptimeMillis();
            this.mLastFrameTime = uptimeMillis;
            AnimationHandler.this.onAnimationFrame(uptimeMillis);
        }

        @Override // androidx.animation.AnimationHandler.AnimationFrameCallbackProvider
        public void postFrameCallback() {
            getHandler().postDelayed(this, Math.max(this.mFrameDelay - (SystemClock.uptimeMillis() - this.mLastFrameTime), 0L));
        }
    }
}
