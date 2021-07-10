package androidx.animation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
public class CycleInterpolator implements Interpolator {
    private float mCycles;

    public CycleInterpolator(Context context, AttributeSet attributeSet) {
        this(context.getResources(), context.getTheme(), attributeSet);
    }

    CycleInterpolator(Resources resources, Resources.Theme theme, AttributeSet attributeSet) {
        TypedArray typedArray;
        if (theme != null) {
            typedArray = theme.obtainStyledAttributes(attributeSet, AndroidResources.STYLEABLE_CYCLE_INTERPOLATOR, 0, 0);
        } else {
            typedArray = resources.obtainAttributes(attributeSet, AndroidResources.STYLEABLE_CYCLE_INTERPOLATOR);
        }
        this.mCycles = typedArray.getFloat(0, 1.0f);
        typedArray.recycle();
    }

    @Override // androidx.animation.Interpolator
    public float getInterpolation(float f) {
        return (float) Math.sin(((double) (this.mCycles * 2.0f)) * 3.141592653589793d * ((double) f));
    }
}
