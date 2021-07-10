package androidx.animation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
public class AnticipateInterpolator implements Interpolator {
    private final float mTension;

    public AnticipateInterpolator() {
        this.mTension = 2.0f;
    }

    public AnticipateInterpolator(Context context, AttributeSet attributeSet) {
        this(context.getResources(), context.getTheme(), attributeSet);
    }

    AnticipateInterpolator(Resources resources, Resources.Theme theme, AttributeSet attributeSet) {
        TypedArray typedArray;
        if (theme != null) {
            typedArray = theme.obtainStyledAttributes(attributeSet, AndroidResources.STYLEABLE_ANTICIPATEOVERSHOOT_INTERPOLATOR, 0, 0);
        } else {
            typedArray = resources.obtainAttributes(attributeSet, AndroidResources.STYLEABLE_ANTICIPATEOVERSHOOT_INTERPOLATOR);
        }
        this.mTension = typedArray.getFloat(0, 2.0f);
        typedArray.recycle();
    }

    @Override // androidx.animation.Interpolator
    public float getInterpolation(float f) {
        float f2 = this.mTension;
        return f * f * (((1.0f + f2) * f) - f2);
    }
}
