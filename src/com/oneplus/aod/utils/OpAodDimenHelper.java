package com.oneplus.aod.utils;

import android.content.Context;
import com.oneplus.util.OpUtils;
public class OpAodDimenHelper {
    public static int convertDpToFixedPx(Context context, int i) {
        return convertDpToFixedPx(context, i, 0);
    }

    public static int convertDpToFixedPx(Context context, int i, int i2) {
        return i != -1 ? OpUtils.convertDpToFixedPx(context.getResources().getDimension(i)) : i2;
    }

    public static int convertDpToFixedPx2(Context context, int i) {
        if (i != -1) {
            return OpUtils.convertDpToFixedPx2(context.getResources().getDimension(i));
        }
        return 0;
    }
}
