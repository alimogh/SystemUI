package androidx.animation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
public class AccelerateInterpolator implements Interpolator {
    private final double mDoubleFactor;
    private final float mFactor;

    public AccelerateInterpolator() {
        this.mFactor = 1.0f;
        this.mDoubleFactor = 2.0d;
    }

    public AccelerateInterpolator(Context context, AttributeSet attributeSet) {
        this(context.getResources(), context.getTheme(), attributeSet);
    }

    AccelerateInterpolator(Resources resources, Resources.Theme theme, AttributeSet attributeSet) {
        TypedArray typedArray;
        if (theme != null) {
            typedArray = theme.obtainStyledAttributes(attributeSet, AndroidResources.STYLEABLE_ACCELERATE_INTERPOLATOR, 0, 0);
        } else {
            typedArray = resources.obtainAttributes(attributeSet, AndroidResources.STYLEABLE_ACCELERATE_INTERPOLATOR);
        }
        float f = typedArray.getFloat(0, 1.0f);
        this.mFactor = f;
        this.mDoubleFactor = (double) (f * 2.0f);
        typedArray.recycle();
    }

    @Override // androidx.animation.Interpolator
    public float getInterpolation(float f) {
        if (this.mFactor == 1.0f) {
            return f * f;
        }
        return (float) Math.pow((double) f, this.mDoubleFactor);
    }
}
