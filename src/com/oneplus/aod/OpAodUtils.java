package com.oneplus.aod;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.util.OpFeatures;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
public class OpAodUtils {
    private static boolean mIsAlwaysOnModeEnabled;
    private static boolean mIsNotificationWakeUpEnabled;
    private static boolean mMotionAwakeOn;
    private static boolean mSingleTapAwakeOn;

    static {
        boolean z = Build.DEBUG_ONEPLUS;
    }

    public static void init(Context context, int i) {
        updateDozeSettings(context, i);
    }

    public static String getDeviceTag() {
        return SystemProperties.get("ro.boot.project_name");
    }

    public static boolean isMotionAwakeOn() {
        Log.d("OPAodUtils", "mMotionAwakeOn: " + mMotionAwakeOn);
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        if (mMotionAwakeOn || !keyguardUpdateMonitor.isAlwaysOnEnabled()) {
            return mMotionAwakeOn;
        }
        Log.i("OPAodUtils", "isMotionAwakeOn: override because always on is enabled");
        return true;
    }

    public static void updateMotionAwakeState(Context context, int i) {
        boolean z = true;
        if (Settings.System.getIntForUser(context.getContentResolver(), "prox_wake_enabled", 1, i) != 1) {
            z = false;
        }
        mMotionAwakeOn = z;
        updateAlwaysOnState(context, i);
        Log.d("OPAodUtils", "updateMotionAwakeState: " + mMotionAwakeOn + ", user = " + i);
    }

    public static void updateSingleTapAwakeState(Context context, int i) {
        boolean z = false;
        if (((Settings.System.getIntForUser(context.getContentResolver(), "oem_acc_blackscreen_gestrue_enable", 0, i) & 2048) >> 11) == 1) {
            z = true;
        }
        mSingleTapAwakeOn = z;
        Log.d("OPAodUtils", "updateSingleTapAwakeState: " + mSingleTapAwakeOn + ", user = " + i);
    }

    public static boolean isAlwaysOnEnabled() {
        Log.d("OPAodUtils", "isAlwaysOnEnabled: " + mIsAlwaysOnModeEnabled);
        return "1".equals(SystemProperties.get("sys.aod.localtest"));
    }

    public static boolean isAlwaysOnEnabledWithTimer() {
        String str = SystemProperties.get("sys.aod.localtest.timer");
        Log.d("OPAodUtils", "isAlwaysOnEnabledWithTimer: " + "1".equals(str));
        return "1".equals(str);
    }

    public static void updateAlwaysOnState(Context context, int i) {
        boolean z = true;
        if (!mMotionAwakeOn || !isSupportAlwaysOn() || Settings.Secure.getIntForUser(context.getContentResolver(), "aod_display_mode", 0, i) != 1) {
            z = false;
        }
        mIsAlwaysOnModeEnabled = z;
        try {
            SystemProperties.set("sys.aod.disable", z ? "0" : "1");
        } catch (Exception e) {
            Log.d("OPAodUtils", "Exception e = " + e.toString());
        }
        Log.d("OPAodUtils", "updateAlwaysOnState: " + mIsAlwaysOnModeEnabled + ", user = " + i);
    }

    public static boolean isNotificationWakeUpEnabled() {
        return mIsNotificationWakeUpEnabled;
    }

    public static void updateNotificationWakeState(Context context, int i) {
        boolean z = true;
        if (1 != Settings.Secure.getIntForUser(context.getContentResolver(), "notification_wake_enabled", 1, i)) {
            z = false;
        }
        mIsNotificationWakeUpEnabled = z;
        Log.d("OPAodUtils", "updateNotificationWakeState: " + mIsNotificationWakeUpEnabled + ", user = " + i);
    }

    public static void updateDozeSettings(Context context, int i) {
        updateMotionAwakeState(context, i);
        updateSingleTapAwakeState(context, i);
        updateNotificationWakeState(context, i);
    }

    public static boolean isSupportAlwaysOn() {
        return OpFeatures.isSupport(new int[]{300});
    }

    public static boolean isSingleTapEnabled() {
        Log.d("OPAodUtils", "isSingleTapEnabled: " + mSingleTapAwakeOn);
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        if (mSingleTapAwakeOn || !keyguardUpdateMonitor.isAlwaysOnEnabled()) {
            return mSingleTapAwakeOn;
        }
        Log.i("OPAodUtils", "isSingleTapEnabled: override because always on is enabled");
        return true;
    }

    public static boolean isCustomFingerprint() {
        return OpFeatures.isSupport(new int[]{60});
    }

    public static boolean isNotificationLightEnabled() {
        int i = SystemProperties.getInt("sys.aod.notif_light_switch", 0);
        if (1 == i) {
            return true;
        }
        if (2 == i) {
            return false;
        }
        return OpFeatures.isSupport(new int[]{106});
    }

    public static void checkAodStyle(Context context, int i) {
        boolean z = true;
        if (Settings.Secure.getIntForUser(context.getContentResolver(), "aod_clock_style", 0, i) != 1) {
            z = false;
        } else if (isCustomFingerprint()) {
            z = true ^ KeyguardUpdateMonitor.getInstance(context).isUnlockWithFingerprintPossible(i);
        }
        if (z) {
            Log.i("OPAodUtils", "Reset aod clock style for user");
            Settings.Secure.putIntForUser(context.getContentResolver(), "aod_clock_style", 0, i);
        }
    }

    public static boolean isDefaultOrRedAodClockStyle(Context context, int i) {
        int intForUser = Settings.Secure.getIntForUser(context.getContentResolver(), "aod_clock_style", 0, i);
        return intForUser == 0 || intForUser == 50;
    }

    public static boolean isAodNoneClockStyle(Context context, int i) {
        return Settings.Secure.getIntForUser(context.getContentResolver(), "aod_clock_style", 0, i) == 1;
    }

    public static int getCurrentAodClockStyle(Context context, int i) {
        return Settings.Secure.getIntForUser(context.getContentResolver(), "aod_clock_style", 0, i);
    }

    public static void forceTurnOnFpBlackGesture(Context context, int i) {
        FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService("fingerprint");
        if (isCustomFingerprint() && fingerprintManager != null && fingerprintManager.getEnrolledFingerprints(i).size() > 0) {
            Log.d("OPAodUtils", "forceTurnOnFpBlackGesture: user =" + i);
            Settings.System.putIntForUser(context.getContentResolver(), "oem_acc_blackscreen_gestrue_enable", Settings.System.getIntForUser(context.getContentResolver(), "oem_acc_blackscreen_gestrue_enable", 0, i) | 32768, i);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003f, code lost:
        if (android.provider.Settings.Secure.getIntForUser(r11.getContentResolver(), "orig_notification_wake_enabled", 1, r12) == 1) goto L_0x0041;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0043, code lost:
        r8 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005c, code lost:
        if (android.provider.Settings.Secure.getIntForUser(r11.getContentResolver(), "notification_wake_enabled", 1, r12) == 1) goto L_0x0041;
     */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x010c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void shouldTurnOnAodGesture(android.content.Context r11, int r12) {
        /*
        // Method dump skipped, instructions count: 295
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.aod.OpAodUtils.shouldTurnOnAodGesture(android.content.Context, int):void");
    }
}
