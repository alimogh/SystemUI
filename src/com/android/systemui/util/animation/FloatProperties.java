package com.android.systemui.util.animation;

import android.graphics.Rect;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import kotlin.jvm.internal.FloatCompanionObject;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: FloatProperties.kt */
public final class FloatProperties {
    @NotNull
    public static final FloatPropertyCompat<Rect> RECT_HEIGHT = new FloatPropertyCompat<Rect>("RectHeight") { // from class: com.android.systemui.util.animation.FloatProperties$Companion$RECT_HEIGHT$1
        public float getValue(@NotNull Rect rect) {
            Intrinsics.checkParameterIsNotNull(rect, "rect");
            return (float) rect.height();
        }

        public void setValue(@NotNull Rect rect, float f) {
            Intrinsics.checkParameterIsNotNull(rect, "rect");
            rect.bottom = rect.top + ((int) f);
        }
    };
    @NotNull
    public static final FloatPropertyCompat<Rect> RECT_WIDTH = new FloatPropertyCompat<Rect>("RectWidth") { // from class: com.android.systemui.util.animation.FloatProperties$Companion$RECT_WIDTH$1
        public float getValue(@NotNull Rect rect) {
            Intrinsics.checkParameterIsNotNull(rect, "rect");
            return (float) rect.width();
        }

        public void setValue(@NotNull Rect rect, float f) {
            Intrinsics.checkParameterIsNotNull(rect, "rect");
            rect.right = rect.left + ((int) f);
        }
    };
    @NotNull
    public static final FloatPropertyCompat<Rect> RECT_X = new FloatPropertyCompat<Rect>("RectX") { // from class: com.android.systemui.util.animation.FloatProperties$Companion$RECT_X$1
        public void setValue(@Nullable Rect rect, float f) {
            if (rect != null) {
                rect.offsetTo((int) f, rect.top);
            }
        }

        public float getValue(@Nullable Rect rect) {
            return rect != null ? (float) rect.left : -FloatCompanionObject.INSTANCE.getMAX_VALUE();
        }
    };
    @NotNull
    public static final FloatPropertyCompat<Rect> RECT_Y = new FloatPropertyCompat<Rect>("RectY") { // from class: com.android.systemui.util.animation.FloatProperties$Companion$RECT_Y$1
        public void setValue(@Nullable Rect rect, float f) {
            if (rect != null) {
                rect.offsetTo(rect.left, (int) f);
            }
        }

        public float getValue(@Nullable Rect rect) {
            return rect != null ? (float) rect.top : -FloatCompanionObject.INSTANCE.getMAX_VALUE();
        }
    };
}
