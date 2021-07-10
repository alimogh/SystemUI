package androidx.dynamicanimation.animation;

import android.os.Looper;
import android.util.AndroidRuntimeException;
import android.view.View;
import androidx.core.view.ViewCompat;
import androidx.dynamicanimation.animation.AnimationHandler;
import androidx.dynamicanimation.animation.DynamicAnimation;
import java.util.ArrayList;
public abstract class DynamicAnimation<T extends DynamicAnimation<T>> implements AnimationHandler.AnimationFrameCallback {
    public static final ViewProperty ALPHA = new ViewProperty("alpha") { // from class: androidx.dynamicanimation.animation.DynamicAnimation.12
        public void setValue(View view, float f) {
            view.setAlpha(f);
        }

        public float getValue(View view) {
            return view.getAlpha();
        }
    };
    public static final ViewProperty ROTATION = new ViewProperty("rotation") { // from class: androidx.dynamicanimation.animation.DynamicAnimation.6
        public void setValue(View view, float f) {
            view.setRotation(f);
        }

        public float getValue(View view) {
            return view.getRotation();
        }
    };
    public static final ViewProperty ROTATION_X = new ViewProperty("rotationX") { // from class: androidx.dynamicanimation.animation.DynamicAnimation.7
        public void setValue(View view, float f) {
            view.setRotationX(f);
        }

        public float getValue(View view) {
            return view.getRotationX();
        }
    };
    public static final ViewProperty ROTATION_Y = new ViewProperty("rotationY") { // from class: androidx.dynamicanimation.animation.DynamicAnimation.8
        public void setValue(View view, float f) {
            view.setRotationY(f);
        }

        public float getValue(View view) {
            return view.getRotationY();
        }
    };
    public static final ViewProperty SCALE_X = new ViewProperty("scaleX") { // from class: androidx.dynamicanimation.animation.DynamicAnimation.4
        public void setValue(View view, float f) {
            view.setScaleX(f);
        }

        public float getValue(View view) {
            return view.getScaleX();
        }
    };
    public static final ViewProperty SCALE_Y = new ViewProperty("scaleY") { // from class: androidx.dynamicanimation.animation.DynamicAnimation.5
        public void setValue(View view, float f) {
            view.setScaleY(f);
        }

        public float getValue(View view) {
            return view.getScaleY();
        }
    };
    public static final ViewProperty SCROLL_X = new ViewProperty("scrollX") { // from class: androidx.dynamicanimation.animation.DynamicAnimation.13
        public void setValue(View view, float f) {
            view.setScrollX((int) f);
        }

        public float getValue(View view) {
            return (float) view.getScrollX();
        }
    };
    public static final ViewProperty SCROLL_Y = new ViewProperty("scrollY") { // from class: androidx.dynamicanimation.animation.DynamicAnimation.14
        public void setValue(View view, float f) {
            view.setScrollY((int) f);
        }

        public float getValue(View view) {
            return (float) view.getScrollY();
        }
    };
    public static final ViewProperty TRANSLATION_X = new ViewProperty("translationX") { // from class: androidx.dynamicanimation.animation.DynamicAnimation.1
        public void setValue(View view, float f) {
            view.setTranslationX(f);
        }

        public float getValue(View view) {
            return view.getTranslationX();
        }
    };
    public static final ViewProperty TRANSLATION_Y = new ViewProperty("translationY") { // from class: androidx.dynamicanimation.animation.DynamicAnimation.2
        public void setValue(View view, float f) {
            view.setTranslationY(f);
        }

        public float getValue(View view) {
            return view.getTranslationY();
        }
    };
    public static final ViewProperty TRANSLATION_Z = new ViewProperty("translationZ") { // from class: androidx.dynamicanimation.animation.DynamicAnimation.3
        public void setValue(View view, float f) {
            ViewCompat.setTranslationZ(view, f);
        }

        public float getValue(View view) {
            return ViewCompat.getTranslationZ(view);
        }
    };
    public static final ViewProperty Y = new ViewProperty("y") { // from class: androidx.dynamicanimation.animation.DynamicAnimation.10
        public void setValue(View view, float f) {
            view.setY(f);
        }

        public float getValue(View view) {
            return view.getY();
        }
    };
    private final ArrayList<OnAnimationEndListener> mEndListeners = new ArrayList<>();
    private long mLastFrameTime = 0;
    float mMaxValue = Float.MAX_VALUE;
    float mMinValue = (-Float.MAX_VALUE);
    private float mMinVisibleChange;
    final FloatPropertyCompat mProperty;
    boolean mRunning = false;
    boolean mStartValueIsSet = false;
    final Object mTarget;
    private final ArrayList<OnAnimationUpdateListener> mUpdateListeners = new ArrayList<>();
    float mValue = Float.MAX_VALUE;
    float mVelocity = 0.0f;

    public interface OnAnimationEndListener {
        void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2);
    }

    public interface OnAnimationUpdateListener {
        void onAnimationUpdate(DynamicAnimation dynamicAnimation, float f, float f2);
    }

    /* access modifiers changed from: package-private */
    public abstract boolean updateValueAndVelocity(long j);

    public static abstract class ViewProperty extends FloatPropertyCompat<View> {
        private ViewProperty(String str) {
            super(str);
        }
    }

    static class MassState {
        float mValue;
        float mVelocity;

        MassState() {
        }
    }

    <K> DynamicAnimation(K k, FloatPropertyCompat<K> floatPropertyCompat) {
        this.mTarget = k;
        this.mProperty = floatPropertyCompat;
        if (floatPropertyCompat == ROTATION || floatPropertyCompat == ROTATION_X || floatPropertyCompat == ROTATION_Y) {
            this.mMinVisibleChange = 0.1f;
        } else if (floatPropertyCompat == ALPHA) {
            this.mMinVisibleChange = 0.00390625f;
        } else if (floatPropertyCompat == SCALE_X || floatPropertyCompat == SCALE_Y) {
            this.mMinVisibleChange = 0.002f;
        } else {
            this.mMinVisibleChange = 1.0f;
        }
    }

    public T setStartValue(float f) {
        this.mValue = f;
        this.mStartValueIsSet = true;
        return this;
    }

    public T setStartVelocity(float f) {
        this.mVelocity = f;
        return this;
    }

    public T setMaxValue(float f) {
        this.mMaxValue = f;
        return this;
    }

    public T setMinValue(float f) {
        this.mMinValue = f;
        return this;
    }

    public T addEndListener(OnAnimationEndListener onAnimationEndListener) {
        if (!this.mEndListeners.contains(onAnimationEndListener)) {
            this.mEndListeners.add(onAnimationEndListener);
        }
        return this;
    }

    public void removeEndListener(OnAnimationEndListener onAnimationEndListener) {
        removeEntry(this.mEndListeners, onAnimationEndListener);
    }

    public T addUpdateListener(OnAnimationUpdateListener onAnimationUpdateListener) {
        if (!isRunning()) {
            if (!this.mUpdateListeners.contains(onAnimationUpdateListener)) {
                this.mUpdateListeners.add(onAnimationUpdateListener);
            }
            return this;
        }
        throw new UnsupportedOperationException("Error: Update listeners must be added beforethe animation.");
    }

    private static <T> void removeNullEntries(ArrayList<T> arrayList) {
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            if (arrayList.get(size) == null) {
                arrayList.remove(size);
            }
        }
    }

    private static <T> void removeEntry(ArrayList<T> arrayList, T t) {
        int indexOf = arrayList.indexOf(t);
        if (indexOf >= 0) {
            arrayList.set(indexOf, null);
        }
    }

    public void start() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new AndroidRuntimeException("Animations may only be started on the main thread");
        } else if (!this.mRunning) {
            startAnimationInternal();
        }
    }

    public void cancel() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new AndroidRuntimeException("Animations may only be canceled on the main thread");
        } else if (this.mRunning) {
            endAnimationInternal(true);
        }
    }

    public boolean isRunning() {
        return this.mRunning;
    }

    private void startAnimationInternal() {
        if (!this.mRunning) {
            this.mRunning = true;
            if (!this.mStartValueIsSet) {
                this.mValue = getPropertyValue();
            }
            float f = this.mValue;
            if (f > this.mMaxValue || f < this.mMinValue) {
                throw new IllegalArgumentException("Starting value need to be in between min value and max value");
            }
            AnimationHandler.getInstance().addAnimationFrameCallback(this, 0);
        }
    }

    @Override // androidx.dynamicanimation.animation.AnimationHandler.AnimationFrameCallback
    public boolean doAnimationFrame(long j) {
        long j2 = this.mLastFrameTime;
        if (j2 == 0) {
            this.mLastFrameTime = j;
            setPropertyValue(this.mValue);
            return false;
        }
        this.mLastFrameTime = j;
        boolean updateValueAndVelocity = updateValueAndVelocity(j - j2);
        float min = Math.min(this.mValue, this.mMaxValue);
        this.mValue = min;
        float max = Math.max(min, this.mMinValue);
        this.mValue = max;
        setPropertyValue(max);
        if (updateValueAndVelocity) {
            endAnimationInternal(false);
        }
        return updateValueAndVelocity;
    }

    private void endAnimationInternal(boolean z) {
        this.mRunning = false;
        AnimationHandler.getInstance().removeCallback(this);
        this.mLastFrameTime = 0;
        this.mStartValueIsSet = false;
        for (int i = 0; i < this.mEndListeners.size(); i++) {
            if (this.mEndListeners.get(i) != null) {
                this.mEndListeners.get(i).onAnimationEnd(this, z, this.mValue, this.mVelocity);
            }
        }
        removeNullEntries(this.mEndListeners);
    }

    /* access modifiers changed from: package-private */
    public void setPropertyValue(float f) {
        this.mProperty.setValue(this.mTarget, f);
        for (int i = 0; i < this.mUpdateListeners.size(); i++) {
            if (this.mUpdateListeners.get(i) != null) {
                this.mUpdateListeners.get(i).onAnimationUpdate(this, this.mValue, this.mVelocity);
            }
        }
        removeNullEntries(this.mUpdateListeners);
    }

    /* access modifiers changed from: package-private */
    public float getValueThreshold() {
        return this.mMinVisibleChange * 0.75f;
    }

    private float getPropertyValue() {
        return this.mProperty.getValue(this.mTarget);
    }
}
