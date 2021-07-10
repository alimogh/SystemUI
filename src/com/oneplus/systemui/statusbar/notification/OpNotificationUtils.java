package com.oneplus.systemui.statusbar.notification;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.widget.ImageView;
import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.C0008R$id;
public class OpNotificationUtils {
    public static boolean isGrayscaleInternal(ImageView imageView, ContrastColorUtil contrastColorUtil) {
        Object tag = imageView.getTag(C0008R$id.icon_is_grayscale);
        if (tag != null) {
            return Boolean.TRUE.equals(tag);
        }
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof InsetDrawable) {
            drawable = ((InsetDrawable) drawable).getDrawable();
        }
        boolean isGrayscaleIcon = contrastColorUtil.isGrayscaleIcon(drawable);
        imageView.setTag(C0008R$id.icon_is_grayscale, Boolean.valueOf(isGrayscaleIcon));
        return isGrayscaleIcon;
    }
}
