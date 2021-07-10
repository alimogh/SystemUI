package androidx.animation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
public class AnticipateOvershootInterpolator implements Interpolator {
    private final float mTension;

    private static float a(float f, float f2) {
        return f * f * (((1.0f + f2) * f) - f2);
    }

    private static float o(float f, float f2) {
        return f * f * (((1.0f + f2) * f) + f2);
    }

    public AnticipateOvershootInterpolator() {
        this.mTension = 3.0f;
    }

    public AnticipateOvershootInterpolator(Context context, AttributeSet attributeSet) {
        this(context.getResources(), context.getTheme(), attributeSet);
    }

    AnticipateOvershootInterpolator(Resources resources, Resources.Theme theme, AttributeSet attributeSet) {
        TypedArray typedArray;
        if (theme != null) {
            typedArray = theme.obtainStyledAttributes(attributeSet, AndroidResources.STYLEABLE_ANTICIPATEOVERSHOOT_INTERPOLATOR, 0, 0);
        } else {
            typedArray = resources.obtainAttributes(attributeSet, AndroidResources.STYLEABLE_ANTICIPATEOVERSHOOT_INTERPOLATOR);
        }
        this.mTension = typedArray.getFloat(0, 2.0f) * typedArray.getFloat(1, 1.5f);
        typedArray.recycle();
    }

    @Override // androidx.animation.Interpolator
    public float getInterpolation(float f) {
        float o;
        if (f < 0.5f) {
            o = a(f * 2.0f, this.mTension);
        } else {
            o = o((f * 2.0f) - 2.0f, this.mTension) + 2.0f;
        }
        return o * 0.5f;
    }
}
