package androidx.animation;
public abstract class Keyframe<T> implements Cloneable {
    float mFraction;
    boolean mHasValue;
    private Interpolator mInterpolator = null;
    boolean mValueWasSetOnStart;

    @Override // java.lang.Object
    public abstract Keyframe clone();

    public abstract T getValue();

    public abstract void setValue(T t);

    public static FloatKeyframe ofFloat(float f, float f2) {
        return new FloatKeyframe(f, f2);
    }

    public static FloatKeyframe ofFloat(float f) {
        return new FloatKeyframe(f);
    }

    public boolean hasValue() {
        return this.mHasValue;
    }

    /* access modifiers changed from: package-private */
    public boolean valueWasSetOnStart() {
        return this.mValueWasSetOnStart;
    }

    /* access modifiers changed from: package-private */
    public void setValueWasSetOnStart(boolean z) {
        this.mValueWasSetOnStart = z;
    }

    public float getFraction() {
        return this.mFraction;
    }

    public Interpolator getInterpolator() {
        return this.mInterpolator;
    }

    public void setInterpolator(Interpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    static class FloatKeyframe extends Keyframe<Float> {
        float mValue;

        FloatKeyframe(float f, float f2) {
            this.mFraction = f;
            this.mValue = f2;
            Class cls = Float.TYPE;
            this.mHasValue = true;
        }

        FloatKeyframe(float f) {
            this.mFraction = f;
            Class cls = Float.TYPE;
        }

        public float getFloatValue() {
            return this.mValue;
        }

        @Override // androidx.animation.Keyframe
        public Float getValue() {
            return Float.valueOf(this.mValue);
        }

        public void setValue(Float f) {
            if (f != null && f.getClass() == Float.class) {
                this.mValue = f.floatValue();
                this.mHasValue = true;
            }
        }

        @Override // androidx.animation.Keyframe, java.lang.Object
        public FloatKeyframe clone() {
            FloatKeyframe floatKeyframe;
            if (this.mHasValue) {
                floatKeyframe = new FloatKeyframe(getFraction(), this.mValue);
            } else {
                floatKeyframe = new FloatKeyframe(getFraction());
            }
            floatKeyframe.setInterpolator(getInterpolator());
            floatKeyframe.mValueWasSetOnStart = this.mValueWasSetOnStart;
            return floatKeyframe;
        }
    }
}
