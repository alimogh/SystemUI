package androidx.leanback.widget;

import android.os.Build;
final class ShadowHelper {
    static boolean supportsDynamicShadow() {
        return Build.VERSION.SDK_INT >= 21;
    }
}
