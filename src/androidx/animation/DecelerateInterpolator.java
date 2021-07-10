package androidx.animation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
public class DecelerateInterpolator implements Interpolator {
    private float mFactor;

    public DecelerateInterpolator() {
        this.mFactor = 1.0f;
    }

    public DecelerateInterpolator(Context context, AttributeSet attributeSet) {
        this(context.getResources(), context.getTheme(), attributeSet);
    }

    DecelerateInterpolator(Resources resources, Resources.Theme theme, AttributeSet attributeSet) {
        TypedArray typedArray;
        this.mFactor = 1.0f;
        if (theme != null) {
            typedArray = theme.obtainStyledAttributes(attributeSet, AndroidResources.STYLEABLE_DECELERATE_INTERPOLATOR, 0, 0);
        } else {
            typedArray = resources.obtainAttributes(attributeSet, AndroidResources.STYLEABLE_DECELERATE_INTERPOLATOR);
        }
        this.mFactor = typedArray.getFloat(0, 1.0f);
        typedArray.recycle();
    }

    @Override // androidx.animation.Interpolator
    public float getInterpolation(float f) {
        float f2 = this.mFactor;
        if (f2 != 1.0f) {
            return (float) (1.0d - Math.pow((double) (1.0f - f), (double) (f2 * 2.0f)));
        }
        float f3 = 1.0f - f;
        return 1.0f - (f3 * f3);
    }
}
