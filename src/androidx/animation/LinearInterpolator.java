package androidx.animation;

import android.content.Context;
import android.util.AttributeSet;
public class LinearInterpolator implements Interpolator {
    @Override // androidx.animation.Interpolator
    public float getInterpolation(float f) {
        return f;
    }

    public LinearInterpolator() {
    }

    public LinearInterpolator(Context context, AttributeSet attributeSet) {
    }
}
