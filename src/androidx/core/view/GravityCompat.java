package androidx.core.view;

import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;
public final class GravityCompat {
    public static void apply(int i, int i2, int i3, Rect rect, Rect rect2, int i4) {
        if (Build.VERSION.SDK_INT >= 17) {
            Gravity.apply(i, i2, i3, rect, rect2, i4);
        } else {
            Gravity.apply(i, i2, i3, rect, rect2);
        }
    }

    public static int getAbsoluteGravity(int i, int i2) {
        return Build.VERSION.SDK_INT >= 17 ? Gravity.getAbsoluteGravity(i, i2) : i & -8388609;
    }
}
