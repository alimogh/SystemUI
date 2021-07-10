package com.oneplus.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import com.android.systemui.C0001R$array;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0016R$style;
public class ThemeColorUtils {
    private static int sAccentColor = 0;
    private static int[] sColors = null;
    private static int sCurrentTheme = -1;
    private static int[] sRedColors = null;
    private static boolean sSpecialTheme = false;
    private static int sSubAccentColor;
    private static String[] sThemeName;

    public static void init(Context context) {
        Resources resources = context.getResources();
        if (sCurrentTheme == -1) {
            sThemeName = resources.getStringArray(C0001R$array.qs_theme_colors);
        }
        boolean isGoogleDarkTheme = OpUtils.isGoogleDarkTheme(context);
        int themeColor = OpUtils.getThemeColor(context);
        Log.d("ThemeColorUtils", "isGoogleDark=" + isGoogleDarkTheme + ", opTheme=" + themeColor);
        boolean isSpecialTheme = OpUtils.isSpecialTheme(context);
        if (!(sCurrentTheme == themeColor && sSpecialTheme == isSpecialTheme)) {
            sCurrentTheme = themeColor;
            sSpecialTheme = isSpecialTheme;
            sColors = resources.getIntArray(resources.getIdentifier(sThemeName[themeColor], null, "com.android.systemui"));
            sRedColors = resources.getIntArray(C0001R$array.op_qs_theme_color_red);
        }
        updateAccentColor(context);
    }

    private static void updateAccentColor(Context context) {
        int themeAccentColor = OpUtils.getThemeAccentColor(context, C0004R$color.oneplus_accent_color);
        int subAccentColor = OpUtils.getSubAccentColor(context, C0004R$color.oneplus_sub_accent_color);
        if (Build.DEBUG_ONEPLUS) {
            Log.d("ThemeColorUtils", "updateAccentColor: accentColor=" + Integer.toHexString(themeAccentColor) + ", subAccentColor=" + Integer.toHexString(subAccentColor));
        }
        sAccentColor = themeAccentColor;
        sSubAccentColor = subAccentColor;
    }

    public static int getColor(int i) {
        if (i == 100) {
            return sAccentColor;
        }
        if (i == 101) {
            if (OpUtils.isREDVersion()) {
                return sAccentColor;
            }
            return sSubAccentColor;
        } else if (OpUtils.isREDVersion()) {
            return sRedColors[i];
        } else {
            return sColors[i];
        }
    }

    public static int getEditTheme() {
        int i = sCurrentTheme;
        if (i == 1) {
            return C0016R$style.op_edit_theme_dark;
        }
        if (i != 2) {
            return C0016R$style.op_edit_theme_light;
        }
        return C0016R$style.op_edit_theme_android;
    }

    public static int getThumbBackground() {
        int i = sCurrentTheme;
        if (i == 1) {
            return C0006R$drawable.ripple_background_dark;
        }
        if (i != 2) {
            return C0006R$drawable.ripple_background_white;
        }
        return C0006R$drawable.ripple_background_dark;
    }

    public static int getCurrentTheme() {
        return sCurrentTheme;
    }

    public static boolean isSpecialTheme() {
        return sSpecialTheme;
    }
}
