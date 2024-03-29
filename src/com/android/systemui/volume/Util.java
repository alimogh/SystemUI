package com.android.systemui.volume;

import android.content.Context;
import android.provider.Settings;
import android.view.View;
public class Util extends com.android.settingslib.volume.Util {
    public static String logTag(Class<?> cls) {
        String str = "vol." + cls.getSimpleName();
        return str.length() < 23 ? str : str.substring(0, 23);
    }

    public static String ringerModeToString(int i) {
        if (i == 0) {
            return "RINGER_MODE_SILENT";
        }
        if (i == 1) {
            return "RINGER_MODE_VIBRATE";
        }
        if (i == 2) {
            return "RINGER_MODE_NORMAL";
        }
        return "RINGER_MODE_UNKNOWN_" + i;
    }

    public static final void setVisOrGone(View view, boolean z) {
        if (view != null) {
            int i = 0;
            if ((view.getVisibility() == 0) != z) {
                if (!z) {
                    i = 8;
                }
                view.setVisibility(i);
            }
        }
    }

    public static int getThreeKeyStatus(Context context) {
        if (context == null) {
            return -1;
        }
        return Settings.Global.getInt(context.getContentResolver(), "three_Key_mode", -1);
    }
}
