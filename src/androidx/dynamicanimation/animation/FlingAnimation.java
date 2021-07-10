package androidx.dynamicanimation.animation;

import androidx.dynamicanimation.animation.DynamicAnimation;
public final class FlingAnimation extends DynamicAnimation<FlingAnimation> {
    private final DragForce mFlingForce;

    public <K> FlingAnimation(K k, FloatPropertyCompat<K> floatPropertyCompat) {
        super(k, floatPropertyCompat);
        DragForce dragForce = new DragForce();
        this.mFlingForce = dragForce;
        dragForce.setValueThreshold(getValueThreshold());
    }

    public FlingAnimation setFriction(float f) {
        if (f > 0.0f) {
            this.mFlingForce.setFrictionScalar(f);
            return this;
        }
        throw new IllegalArgumentException("Friction must be positive");
    }

    @Override // androidx.dynamicanimation.animation.DynamicAnimation
    public FlingAnimation setMinValue(float f) {
        super.setMinValue(f);
        return this;
    }

    @Override // androidx.dynamicanimation.animation.DynamicAnimation
    public FlingAnimation setMaxValue(float f) {
        super.setMaxValue(f);
        return this;
    }

    @Override // androidx.dynamicanimation.animation.DynamicAnimation
    public FlingAnimation setStartVelocity(float f) {
        super.setStartVelocity(f);
        return this;
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.dynamicanimation.animation.DynamicAnimation
    public boolean updateValueAndVelocity(long j) {
        DynamicAnimation.MassState updateValueAndVelocity = this.mFlingForce.updateValueAndVelocity(this.mValue, this.mVelocity, j);
        float f = updateValueAndVelocity.mValue;
        this.mValue = f;
        float f2 = updateValueAndVelocity.mVelocity;
        this.mVelocity = f2;
        float f3 = this.mMinValue;
        if (f < f3) {
            this.mValue = f3;
            return true;
        }
        float f4 = this.mMaxValue;
        if (f > f4) {
            this.mValue = f4;
            return true;
        } else if (isAtEquilibrium(f, f2)) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isAtEquilibrium(float f, float f2) {
        return f >= this.mMaxValue || f <= this.mMinValue || this.mFlingForce.isAtEquilibrium(f, f2);
    }

    /* access modifiers changed from: package-private */
    public static final class DragForce {
        private float mFriction = -4.2f;
        private final DynamicAnimation.MassState mMassState = new DynamicAnimation.MassState();
        private float mVelocityThreshold;

        DragForce() {
        }

        /* access modifiers changed from: package-private */
        public void setFrictionScalar(float f) {
            this.mFriction = f * -4.2f;
        }

        /* access modifiers changed from: package-private */
        public DynamicAnimation.MassState updateValueAndVelocity(float f, float f2, long j) {
            float f3 = (float) j;
            this.mMassState.mVelocity = (float) (((double) f2) * Math.exp((double) ((f3 / 1000.0f) * this.mFriction)));
            DynamicAnimation.MassState massState = this.mMassState;
            float f4 = this.mFriction;
            massState.mValue = (float) (((double) (f - (f2 / f4))) + (((double) (f2 / f4)) * Math.exp((double) ((f4 * f3) / 1000.0f))));
            DynamicAnimation.MassState massState2 = this.mMassState;
            if (isAtEquilibrium(massState2.mValue, massState2.mVelocity)) {
                this.mMassState.mVelocity = 0.0f;
            }
            return this.mMassState;
        }

        public boolean isAtEquilibrium(float f, float f2) {
            return Math.abs(f2) < this.mVelocityThreshold;
        }

        /* access modifiers changed from: package-private */
        public void setValueThreshold(float f) {
            this.mVelocityThreshold = f * 62.5f;
        }
    }
}
