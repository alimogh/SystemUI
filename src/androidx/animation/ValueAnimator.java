package androidx.animation;

import android.os.Looper;
import android.util.AndroidRuntimeException;
import android.view.animation.AnimationUtils;
import androidx.animation.AnimationHandler;
import androidx.animation.Animator;
import androidx.core.os.TraceCompat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
public class ValueAnimator extends Animator implements AnimationHandler.AnimationFrameCallback {
    private static final Interpolator sDefaultInterpolator = new AccelerateDecelerateInterpolator();
    private static float sDurationScale = 1.0f;
    String mAnimTraceName = null;
    private boolean mAnimationEndRequested = false;
    private float mCurrentFraction;
    private long mDuration = 300;
    private float mDurationScale = -1.0f;
    private long mFirstFrameTime;
    boolean mInitialized = false;
    private Interpolator mInterpolator = sDefaultInterpolator;
    private long mLastFrameTime = -1;
    private float mOverallFraction = 0.0f;
    private long mPauseTime;
    private int mRepeatCount = 0;
    private int mRepeatMode = 1;
    private boolean mResumed = false;
    private boolean mReversing;
    private boolean mRunning = false;
    float mSeekFraction = -1.0f;
    private boolean mSelfPulse = true;
    private long mStartDelay = 0;
    private boolean mStartListenersCalled = false;
    long mStartTime = -1;
    private boolean mStarted = false;
    private boolean mSuppressSelfPulseRequested = false;
    PropertyValuesHolder[] mValues;
    HashMap<String, PropertyValuesHolder> mValuesMap;

    static float getDurationScale() {
        return sDurationScale;
    }

    public static ValueAnimator ofFloat(float... fArr) {
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setFloatValues(fArr);
        return valueAnimator;
    }

    public void setFloatValues(float... fArr) {
        if (fArr != null && fArr.length != 0) {
            PropertyValuesHolder[] propertyValuesHolderArr = this.mValues;
            if (propertyValuesHolderArr == null || propertyValuesHolderArr.length == 0) {
                setValues(PropertyValuesHolder.ofFloat("", fArr));
            } else {
                propertyValuesHolderArr[0].setFloatValues(fArr);
            }
            this.mInitialized = false;
        }
    }

    public void setValues(PropertyValuesHolder... propertyValuesHolderArr) {
        int length = propertyValuesHolderArr.length;
        this.mValues = propertyValuesHolderArr;
        this.mValuesMap = new HashMap<>(length);
        for (PropertyValuesHolder propertyValuesHolder : propertyValuesHolderArr) {
            this.mValuesMap.put(propertyValuesHolder.getPropertyName(), propertyValuesHolder);
        }
        this.mInitialized = false;
    }

    public PropertyValuesHolder[] getValues() {
        return this.mValues;
    }

    /* access modifiers changed from: package-private */
    public void initAnimation() {
        if (!this.mInitialized) {
            int length = this.mValues.length;
            for (int i = 0; i < length; i++) {
                this.mValues[i].init();
            }
            this.mInitialized = true;
        }
    }

    @Override // androidx.animation.Animator
    public ValueAnimator setDuration(long j) {
        if (j >= 0) {
            this.mDuration = j;
            return this;
        }
        throw new IllegalArgumentException("Animators cannot have negative duration: " + j);
    }

    private float resolveDurationScale() {
        float f = this.mDurationScale;
        return f >= 0.0f ? f : sDurationScale;
    }

    private long getScaledDuration() {
        return (long) (((float) this.mDuration) * resolveDurationScale());
    }

    @Override // androidx.animation.Animator
    public long getDuration() {
        return this.mDuration;
    }

    @Override // androidx.animation.Animator
    public long getTotalDuration() {
        int i = this.mRepeatCount;
        if (i == -1) {
            return -1;
        }
        return this.mStartDelay + (this.mDuration * ((long) (i + 1)));
    }

    public void setCurrentPlayTime(long j) {
        long j2 = this.mDuration;
        setCurrentFraction(j2 > 0 ? ((float) j) / ((float) j2) : 1.0f);
    }

    public void setCurrentFraction(float f) {
        initAnimation();
        float clampFraction = clampFraction(f);
        if (isPulsingInternal()) {
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis() - ((long) (((float) getScaledDuration()) * clampFraction));
        } else {
            this.mSeekFraction = clampFraction;
        }
        this.mOverallFraction = clampFraction;
        animateValue(getCurrentIterationFraction(clampFraction, this.mReversing));
    }

    private int getCurrentIteration(float f) {
        float clampFraction = clampFraction(f);
        double d = (double) clampFraction;
        double floor = Math.floor(d);
        if (d == floor && clampFraction > 0.0f) {
            floor -= 1.0d;
        }
        return (int) floor;
    }

    private float getCurrentIterationFraction(float f, boolean z) {
        float clampFraction = clampFraction(f);
        int currentIteration = getCurrentIteration(clampFraction);
        float f2 = clampFraction - ((float) currentIteration);
        return shouldPlayBackward(currentIteration, z) ? 1.0f - f2 : f2;
    }

    private float clampFraction(float f) {
        if (f < 0.0f) {
            return 0.0f;
        }
        int i = this.mRepeatCount;
        return i != -1 ? Math.min(f, (float) (i + 1)) : f;
    }

    private boolean shouldPlayBackward(int i, boolean z) {
        if (i > 0 && this.mRepeatMode == 2) {
            int i2 = this.mRepeatCount;
            if (i < i2 + 1 || i2 == -1) {
                return z ? i % 2 == 0 : i % 2 != 0;
            }
        }
        return z;
    }

    @Override // androidx.animation.Animator
    public long getStartDelay() {
        return this.mStartDelay;
    }

    @Override // androidx.animation.Animator
    public void setInterpolator(Interpolator interpolator) {
        if (interpolator != null) {
            this.mInterpolator = interpolator;
        } else {
            this.mInterpolator = new LinearInterpolator();
        }
    }

    private void notifyStartListeners() {
        ArrayList<Animator.AnimatorListener> arrayList = this.mListeners;
        if (arrayList != null && !this.mStartListenersCalled) {
            ArrayList arrayList2 = (ArrayList) arrayList.clone();
            int size = arrayList2.size();
            for (int i = 0; i < size; i++) {
                ((Animator.AnimatorListener) arrayList2.get(i)).onAnimationStart(this, this.mReversing);
            }
        }
        this.mStartListenersCalled = true;
    }

    private void start(boolean z) {
        if (Looper.myLooper() != null) {
            this.mReversing = z;
            this.mSelfPulse = !this.mSuppressSelfPulseRequested;
            if (z) {
                float f = this.mSeekFraction;
                if (!(f == -1.0f || f == 0.0f)) {
                    int i = this.mRepeatCount;
                    if (i == -1) {
                        this.mSeekFraction = 1.0f - ((float) (((double) f) - Math.floor((double) f)));
                    } else {
                        this.mSeekFraction = ((float) (i + 1)) - f;
                    }
                }
            }
            this.mStarted = true;
            this.mPaused = false;
            this.mRunning = false;
            this.mAnimationEndRequested = false;
            this.mLastFrameTime = -1;
            this.mStartTime = -1;
            if (this.mStartDelay == 0 || this.mSeekFraction >= 0.0f || this.mReversing) {
                startAnimation();
                float f2 = this.mSeekFraction;
                if (f2 == -1.0f) {
                    setCurrentPlayTime(0);
                } else {
                    setCurrentFraction(f2);
                }
            }
            addAnimationCallback();
            return;
        }
        throw new AndroidRuntimeException("Animators may only be run on Looper threads");
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.animation.Animator
    public void startWithoutPulsing(boolean z) {
        this.mSuppressSelfPulseRequested = true;
        if (z) {
            reverse();
        } else {
            start();
        }
        this.mSuppressSelfPulseRequested = false;
    }

    @Override // androidx.animation.Animator
    public void start() {
        start(false);
    }

    @Override // androidx.animation.Animator
    public void cancel() {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        } else if (!this.mAnimationEndRequested) {
            if ((this.mStarted || this.mRunning) && this.mListeners != null) {
                if (!this.mRunning) {
                    notifyStartListeners();
                }
                Iterator it = ((ArrayList) this.mListeners.clone()).iterator();
                while (it.hasNext()) {
                    ((Animator.AnimatorListener) it.next()).onAnimationCancel(this);
                }
            }
            endAnimation();
        }
    }

    @Override // androidx.animation.Animator
    public void end() {
        if (Looper.myLooper() != null) {
            if (!this.mRunning) {
                startAnimation();
                this.mStarted = true;
            } else if (!this.mInitialized) {
                initAnimation();
            }
            animateValue(shouldPlayBackward(this.mRepeatCount, this.mReversing) ? 0.0f : 1.0f);
            endAnimation();
            return;
        }
        throw new AndroidRuntimeException("Animators may only be run on Looper threads");
    }

    @Override // androidx.animation.Animator
    public boolean isRunning() {
        return this.mRunning;
    }

    @Override // androidx.animation.Animator
    public boolean isStarted() {
        return this.mStarted;
    }

    @Override // androidx.animation.Animator
    public void reverse() {
        if (isPulsingInternal()) {
            long currentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis();
            this.mStartTime = currentAnimationTimeMillis - (getScaledDuration() - (currentAnimationTimeMillis - this.mStartTime));
            this.mReversing = !this.mReversing;
        } else if (this.mStarted) {
            this.mReversing = !this.mReversing;
            end();
        } else {
            start(true);
        }
    }

    private void endAnimation() {
        ArrayList<Animator.AnimatorListener> arrayList;
        if (!this.mAnimationEndRequested) {
            removeAnimationCallback();
            boolean z = true;
            this.mAnimationEndRequested = true;
            this.mPaused = false;
            if ((!this.mStarted && !this.mRunning) || this.mListeners == null) {
                z = false;
            }
            if (z && !this.mRunning) {
                notifyStartListeners();
            }
            this.mRunning = false;
            this.mStarted = false;
            this.mStartListenersCalled = false;
            this.mLastFrameTime = -1;
            this.mStartTime = -1;
            if (z && (arrayList = this.mListeners) != null) {
                ArrayList arrayList2 = (ArrayList) arrayList.clone();
                int size = arrayList2.size();
                for (int i = 0; i < size; i++) {
                    ((Animator.AnimatorListener) arrayList2.get(i)).onAnimationEnd(this, this.mReversing);
                }
            }
            this.mReversing = false;
            TraceCompat.endSection();
        }
    }

    private void startAnimation() {
        TraceCompat.beginSection(getNameForTrace());
        this.mAnimationEndRequested = false;
        initAnimation();
        this.mRunning = true;
        float f = this.mSeekFraction;
        if (f >= 0.0f) {
            this.mOverallFraction = f;
        } else {
            this.mOverallFraction = 0.0f;
        }
        if (this.mListeners != null) {
            notifyStartListeners();
        }
    }

    private boolean isPulsingInternal() {
        return this.mLastFrameTime >= 0;
    }

    public String getNameForTrace() {
        String str = this.mAnimTraceName;
        return str == null ? "animator" : str;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0052, code lost:
        if (r2 != false) goto L_0x0033;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean animateBasedOnTime(long r7) {
        /*
            r6 = this;
            boolean r0 = r6.mRunning
            r1 = 0
            if (r0 == 0) goto L_0x0064
            long r2 = r6.getScaledDuration()
            r4 = 0
            int r0 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r0 <= 0) goto L_0x0016
            long r4 = r6.mStartTime
            long r7 = r7 - r4
            float r7 = (float) r7
            float r8 = (float) r2
            float r7 = r7 / r8
            goto L_0x0018
        L_0x0016:
            r7 = 1065353216(0x3f800000, float:1.0)
        L_0x0018:
            float r8 = r6.mOverallFraction
            int r2 = (int) r7
            int r8 = (int) r8
            r3 = 1
            if (r2 <= r8) goto L_0x0021
            r8 = r3
            goto L_0x0022
        L_0x0021:
            r8 = r1
        L_0x0022:
            int r2 = r6.mRepeatCount
            int r4 = r2 + 1
            float r4 = (float) r4
            int r4 = (r7 > r4 ? 1 : (r7 == r4 ? 0 : -1))
            if (r4 < 0) goto L_0x0030
            r4 = -1
            if (r2 == r4) goto L_0x0030
            r2 = r3
            goto L_0x0031
        L_0x0030:
            r2 = r1
        L_0x0031:
            if (r0 != 0) goto L_0x0035
        L_0x0033:
            r1 = r3
            goto L_0x0055
        L_0x0035:
            if (r8 == 0) goto L_0x0052
            if (r2 != 0) goto L_0x0052
            java.util.ArrayList<androidx.animation.Animator$AnimatorListener> r8 = r6.mListeners
            if (r8 == 0) goto L_0x0055
            int r8 = r8.size()
            r0 = r1
        L_0x0042:
            if (r0 >= r8) goto L_0x0055
            java.util.ArrayList<androidx.animation.Animator$AnimatorListener> r2 = r6.mListeners
            java.lang.Object r2 = r2.get(r0)
            androidx.animation.Animator$AnimatorListener r2 = (androidx.animation.Animator.AnimatorListener) r2
            r2.onAnimationRepeat(r6)
            int r0 = r0 + 1
            goto L_0x0042
        L_0x0052:
            if (r2 == 0) goto L_0x0055
            goto L_0x0033
        L_0x0055:
            float r7 = r6.clampFraction(r7)
            r6.mOverallFraction = r7
            boolean r8 = r6.mReversing
            float r7 = r6.getCurrentIterationFraction(r7, r8)
            r6.animateValue(r7)
        L_0x0064:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.animation.ValueAnimator.animateBasedOnTime(long):boolean");
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.animation.Animator
    public void skipToEndValue(boolean z) {
        initAnimation();
        float f = 0.0f;
        float f2 = z ? 0.0f : 1.0f;
        if (!(this.mRepeatCount % 2 == 1 && this.mRepeatMode == 2)) {
            f = f2;
        }
        animateValue(f);
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.animation.Animator
    public boolean isInitialized() {
        return this.mInitialized;
    }

    @Override // androidx.animation.AnimationHandler.AnimationFrameCallback
    public final boolean doAnimationFrame(long j) {
        long j2;
        if (this.mStartTime < 0) {
            if (this.mReversing) {
                j2 = j;
            } else {
                j2 = ((long) (((float) this.mStartDelay) * resolveDurationScale())) + j;
            }
            this.mStartTime = j2;
        }
        if (this.mPaused) {
            this.mPauseTime = j;
            removeAnimationCallback();
            return false;
        }
        if (this.mResumed) {
            this.mResumed = false;
            long j3 = this.mPauseTime;
            if (j3 > 0) {
                this.mStartTime += j - j3;
            }
        }
        if (!this.mRunning) {
            if (this.mStartTime > j && this.mSeekFraction == -1.0f) {
                return false;
            }
            this.mRunning = true;
            startAnimation();
        }
        if (this.mLastFrameTime < 0 && this.mSeekFraction >= 0.0f) {
            this.mStartTime = j - ((long) (((float) getScaledDuration()) * this.mSeekFraction));
            this.mSeekFraction = -1.0f;
        }
        this.mLastFrameTime = j;
        boolean animateBasedOnTime = animateBasedOnTime(Math.max(j, this.mStartTime));
        if (animateBasedOnTime) {
            endAnimation();
        }
        return animateBasedOnTime;
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.animation.Animator
    public boolean pulseAnimationFrame(long j) {
        if (this.mSelfPulse) {
            return false;
        }
        return doAnimationFrame(j);
    }

    private void removeAnimationCallback() {
        if (this.mSelfPulse) {
            Animator.removeAnimationCallback(this);
        }
    }

    private void addAnimationCallback() {
        if (this.mSelfPulse) {
            Animator.addAnimationCallback(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void animateValue(float f) {
        float interpolation = this.mInterpolator.getInterpolation(f);
        int length = this.mValues.length;
        for (int i = 0; i < length; i++) {
            this.mValues[i].calculateValue(interpolation);
        }
        ArrayList<Animator.AnimatorUpdateListener> arrayList = this.mUpdateListeners;
        if (arrayList != null) {
            int size = arrayList.size();
            for (int i2 = 0; i2 < size; i2++) {
                this.mUpdateListeners.get(i2).onAnimationUpdate(this);
            }
        }
    }

    @Override // androidx.animation.Animator, java.lang.Object
    public ValueAnimator clone() {
        ValueAnimator valueAnimator = (ValueAnimator) super.clone();
        if (this.mUpdateListeners != null) {
            valueAnimator.mUpdateListeners = new ArrayList<>(this.mUpdateListeners);
        }
        valueAnimator.mSeekFraction = -1.0f;
        valueAnimator.mReversing = false;
        valueAnimator.mInitialized = false;
        valueAnimator.mStarted = false;
        valueAnimator.mRunning = false;
        valueAnimator.mPaused = false;
        valueAnimator.mResumed = false;
        valueAnimator.mStartListenersCalled = false;
        valueAnimator.mStartTime = -1;
        valueAnimator.mAnimationEndRequested = false;
        valueAnimator.mPauseTime = -1;
        valueAnimator.mLastFrameTime = -1;
        valueAnimator.mFirstFrameTime = -1;
        valueAnimator.mOverallFraction = 0.0f;
        valueAnimator.mCurrentFraction = 0.0f;
        valueAnimator.mSelfPulse = true;
        valueAnimator.mSuppressSelfPulseRequested = false;
        PropertyValuesHolder[] propertyValuesHolderArr = this.mValues;
        if (propertyValuesHolderArr != null) {
            int length = propertyValuesHolderArr.length;
            valueAnimator.mValues = new PropertyValuesHolder[length];
            valueAnimator.mValuesMap = new HashMap<>(length);
            for (int i = 0; i < length; i++) {
                PropertyValuesHolder clone = propertyValuesHolderArr[i].clone();
                valueAnimator.mValues[i] = clone;
                valueAnimator.mValuesMap.put(clone.getPropertyName(), clone);
            }
        }
        return valueAnimator;
    }

    @Override // java.lang.Object
    public String toString() {
        String str = "ValueAnimator@" + Integer.toHexString(hashCode());
        if (this.mValues != null) {
            for (int i = 0; i < this.mValues.length; i++) {
                str = str + "\n    " + this.mValues[i].toString();
            }
        }
        return str;
    }
}
