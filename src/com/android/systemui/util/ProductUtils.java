package com.android.systemui.util;

import android.util.OpFeatures;
public class ProductUtils {
    public static boolean isUsvMode() {
        return OpFeatures.isSupport(new int[]{231});
    }

    public static boolean isUsVisMode() {
        return OpFeatures.isSupport(new int[]{232});
    }
}
