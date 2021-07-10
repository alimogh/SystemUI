package com.oneplus.util;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.OpFeatures;
import com.android.systemui.C0015R$string;
public class OpNavBarUtils {
    public static boolean isSupportCustomKeys() {
        return true;
    }

    public static boolean isKeySwapped(Context context) {
        try {
            return Settings.System.getInt(context.getContentResolver(), "oem_acc_key_define") == 1;
        } catch (Settings.SettingNotFoundException unused) {
            return true;
        }
    }

    public static boolean isBackKeyRight(Context context) {
        if (!OpUtils.isGlobalROM(context) || !isKeySwapped(context)) {
            return !OpUtils.isGlobalROM(context) && !isKeySwapped(context);
        }
        return true;
    }

    public static boolean isSupportCustomNavBar() {
        return OpFeatures.isSupport(new int[]{51});
    }

    public static boolean isSupportHideNavBar() {
        return OpFeatures.isSupport(new int[]{29});
    }

    public static int getNavBarLayout(Context context, boolean z) {
        int layoutDirectionFromLocale = TextUtils.getLayoutDirectionFromLocale(context.getResources().getConfiguration().locale);
        boolean isBackKeyRight = isBackKeyRight(context);
        int i = C0015R$string.oneplus_config_navBarLayout;
        int i2 = C0015R$string.oneplus_config_navBarLayout_RTL;
        int i3 = C0015R$string.oneplus_config_navBarLayoutQuickstep;
        int i4 = C0015R$string.oneplus_config_navBarLayoutQuickstep_RTL;
        if (z) {
            i = i3;
        }
        if (z) {
            i2 = i4;
        }
        return isBackKeyRight ? layoutDirectionFromLocale == 0 ? i2 : i : layoutDirectionFromLocale == 1 ? i2 : i;
    }
}
