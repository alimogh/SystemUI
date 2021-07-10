package com.oneplus.util;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.OpFeatures;
import android.util.PathParser;
import android.view.Display;
import com.android.internal.os.BatteryStatsHelper;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.settingslib.utils.PowerUtil;
import com.android.systemui.Dependency;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.util.ProductUtils;
import com.oneplus.custom.utils.OpCustomizeSettings;
import com.oneplus.systemui.OpSystemUIInjector;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.codeaurora.internal.IExtTelephony;
public class OpUtils {
    public static final boolean DEBUG_ONEPLUS = Build.DEBUG_ONEPLUS;
    private static final Uri LAUNCHER_FEATURE_URI = Uri.parse("content://net.oneplus.launcher.features");
    private static String[] MmcMnc3UK = {"23420", "23594"};
    public static boolean QUICK_REPLY_BUBBLE = SystemProperties.getBoolean("feature.quick_reply_bubble", true);
    public static boolean QUICK_REPLY_PORTRAIT_ENABLED = false;
    public static final boolean SUPPORT_CHARGING_ANIM_V1 = isSupportChargingAnimationV1();
    public static final boolean SUPPORT_CHARGING_ANIM_V2 = isSupportChargingAnimationV2();
    public static final boolean SUPPORT_SWARP_CHARGING = isSupportSWarpCharging();
    public static final boolean SUPPORT_WARP_CHARGING = isSupportWarpCharging();
    private static String[] SprintMmcMnc = {"310120", "312530", "311870", "311490", "310000"};
    private static String[] WindTreMmcMnc = {"22288", "22299"};
    public static boolean isNavigationBarShowing = false;
    private static int mDensityDpi;
    private static boolean mEditTileBefore = true;
    private static boolean mIsCTS = false;
    private static boolean mIsCTSAdded = false;
    private static boolean mIsCtsInputmethodservice = false;
    public static boolean mIsFullScreenListApp = false;
    private static boolean mIsHomeApp = false;
    private static boolean mIsNeedDarkNavBar = false;
    private static boolean mIsOnePlusHomeApp = false;
    private static boolean mIsRecentUnlockBiometricFace = false;
    private static boolean mIsRecentUnlockBiometricFinger = false;
    private static boolean mIsScreenCompat = false;
    private static boolean mIsSupportResolutionSwitch = false;
    private static boolean mIsSystemUI = false;
    private static IOverlayManager mOverlayManager;
    private static String mPackageName = "";
    public static int mScreenResolution;
    private static String[] mSimType = {"UNKNOWN", "UNKNOWN"};
    private static String mTopClassName = "";
    private static ConcurrentHashMap<Integer, Typeface> mTypefaceCache = new ConcurrentHashMap<>();
    private static final boolean sIsMCLCustomType = OpCustomizeSettings.CUSTOM_TYPE.MCL.equals(OpCustomizeSettings.getCustomType());
    private static final boolean sIsREDCustomType = OpCustomizeSettings.CUSTOM_TYPE.RED.equals(OpCustomizeSettings.getCustomType());
    public static boolean sIsSupportAssistantGesture = true;
    private static boolean sStatusBarIconsDark = false;

    public static float convertPxByResolutionProportionWithoutRound(float f, int i, int i2) {
        int i3 = 1440;
        if (i2 != 1440) {
            i3 = 1080;
        }
        return (f * ((float) i3)) / ((float) i);
    }

    public static int getMaxDotsForStatusIconContainer() {
        return 0;
    }

    public static boolean isSupportShowDualChannel() {
        return false;
    }

    public static boolean needLargeQSClock(Context context) {
        return true;
    }

    public static <T> void safeForeach(List<T> list, Consumer<T> consumer) {
        for (int size = list.size() - 1; size >= 0; size--) {
            consumer.accept(list.get(size));
        }
    }

    public static void init(Context context) {
        updateDensityDpi(context.getResources().getConfiguration().densityDpi);
        new SettingsObserver(context);
        mIsSupportResolutionSwitch = checkIsSupportResolutionSwitch(context);
        loadMCLTypeface();
        mOverlayManager = IOverlayManager.Stub.asInterface(ServiceManager.getService("overlay"));
    }

    public static boolean isGlobalROM(Context context) {
        return OpFeatures.isSupport(new int[]{1});
    }

    public static boolean isHydrogen() {
        return OpFeatures.isSupport(new int[]{0});
    }

    public static boolean isGoogleDarkTheme(Context context) {
        return (context.getResources().getConfiguration().uiMode & 32) != 0;
    }

    public static int getThemeColor(Context context) {
        return isGoogleDarkTheme(context) ? 1 : 0;
    }

    public static int getThemeAccentColor(Context context, int i) {
        String str = SystemProperties.get("persist.sys.theme.accentcolor");
        if (DEBUG_ONEPLUS) {
            Log.d("OpUtils", "getThemeAccentColor color: " + str);
        }
        if (str == null || TextUtils.isEmpty(str)) {
            return context.getResources().getColor(i);
        }
        if (str.charAt(0) != '#') {
            str = '#' + str;
        }
        return Color.parseColor(str);
    }

    public static int getSubAccentColor(Context context, int i) {
        String str = SystemProperties.get("persist.sys.theme.sub_accentcolor");
        if (DEBUG_ONEPLUS) {
            Log.d("OpUtils", "getSubAccentColor: " + str);
        }
        if (str == null || TextUtils.isEmpty(str)) {
            return context.getResources().getColor(i);
        }
        if (str.charAt(0) != '#') {
            str = '#' + str;
        }
        return Color.parseColor(str);
    }

    public static boolean hasCtaFeature(Context context) {
        return context.getPackageManager().hasSystemFeature("oem.ctaSwitch.support");
    }

    public static boolean isCurrentGuest(Context context) {
        UserInfo userInfo = ((UserManager) context.getSystemService("user")).getUserInfo(ActivityManager.getCurrentUser());
        if (userInfo == null) {
            return false;
        }
        return userInfo.isGuest();
    }

    public static boolean isSpecialTheme(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "oem_special_theme", 0) == 1;
    }

    public static boolean isSupportCustomStatusBar() {
        return OpFeatures.isSupport(new int[]{37});
    }

    public static boolean isSupportSOCThreekey() {
        return OpFeatures.isSupport(new int[]{138});
    }

    public static boolean isSupportMultiLTEstatus(Context context) {
        if (context != null && context.getResources().getBoolean(17891433) && !isUSS()) {
            return true;
        }
        return false;
    }

    public static boolean isSupportShowHD() {
        return OpFeatures.isSupport(new int[]{52});
    }

    public static boolean isSupportRefreshRateSwitch() {
        return OpFeatures.isSupport(new int[]{96});
    }

    private static int getReleaseType() {
        boolean equals = "1".equals(SystemProperties.get("ro.build.alpha"));
        boolean equals2 = "1".equals(SystemProperties.get("ro.build.beta"));
        int i = (!equals || !equals2) ? (equals || !equals2) ? (equals || equals2) ? 0 : 1 : 2 : 3;
        Log.i("OpUtils", "getROMType:" + i);
        return i;
    }

    public static boolean isClosedBeta() {
        boolean z = getReleaseType() == 3;
        Log.i("OpUtils", "isClosedBeta:" + z);
        return z;
    }

    public static boolean isPreventModeEnabled(Context context) {
        if (KeyguardUpdateMonitor.getCurrentUser() != 0) {
            return false;
        }
        try {
            if (Settings.System.getInt(context.getContentResolver(), "oem_acc_anti_misoperation_screen") != 0) {
                return true;
            }
            return false;
        } catch (Settings.SettingNotFoundException unused) {
            return false;
        }
    }

    public static void updateTopPackage(Context context, String str, String str2) {
        mTopClassName = str2;
        mPackageName = str;
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        if (context != null) {
            ResolveInfo resolveActivity = context.getPackageManager().resolveActivity(intent, 65536);
            String str3 = null;
            if (resolveActivity != null) {
                str3 = resolveActivity.activityInfo.packageName;
            }
            boolean z = true;
            if (str3 == null || str == null) {
                mIsHomeApp = false;
                mIsOnePlusHomeApp = false;
            } else {
                mIsHomeApp = str.equals("net.oneplus.launcher") || str.equals("net.oneplus.h2launcher") || str.equals(str3);
                mIsOnePlusHomeApp = str.equals("net.oneplus.launcher") || str.equals("net.oneplus.h2launcher");
            }
            if (str != null) {
                mIsSystemUI = str.equals("com.android.systemui");
            } else {
                mIsSystemUI = false;
            }
            if (str != null) {
                mIsCTS = str.equals("android.systemui.cts");
            } else {
                mIsCTS = false;
            }
            if (str != null) {
                mIsCtsInputmethodservice = str.contains("android.inputmethodservice.cts");
            } else {
                mIsCtsInputmethodservice = false;
            }
            if (str != null) {
                mIsNeedDarkNavBar = str.equals("com.mobile.legends") || str.equals("com.tencent.tmgp.sgame");
            } else {
                mIsNeedDarkNavBar = false;
            }
            if (str != null) {
                mIsFullScreenListApp = OpSystemUIInjector.isInNavGestureFullscreenList(str);
            } else {
                mIsFullScreenListApp = false;
            }
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService("appops");
            if (str != null) {
                try {
                    if (appOpsManager.checkOpNoThrow(1006, context.getPackageManager().getApplicationInfo(str, 1).uid, str) != 0) {
                        z = false;
                    }
                    mIsScreenCompat = z;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    mIsScreenCompat = false;
                }
            } else {
                mIsScreenCompat = false;
            }
        }
    }

    public static boolean isHomeApp() {
        return mIsHomeApp;
    }

    public static boolean isOnePlusHomeApp() {
        return mIsOnePlusHomeApp;
    }

    public static boolean isSystemUI() {
        return mIsSystemUI;
    }

    public static boolean isInFullScreenListApp() {
        return mIsFullScreenListApp;
    }

    public static boolean isScreenCompat() {
        return mIsScreenCompat;
    }

    public static boolean isCTS() {
        return mIsCTS;
    }

    public static boolean isCtsInputmethodservice() {
        return mIsCtsInputmethodservice;
    }

    public static void setCTSAdded(boolean z) {
        mIsCTSAdded = z;
    }

    public static boolean isCTSAdded() {
        return mIsCTSAdded;
    }

    public static boolean isNeedDarkNavBar(Context context) {
        return mIsNeedDarkNavBar;
    }

    public static boolean isRemoveRoamingIcon() {
        return isUST();
    }

    public static boolean isSupportShow4GLTE() {
        return isUST() || ProductUtils.isUsvMode();
    }

    public static boolean isSupportFiveBar() {
        return (isUST() || isUSS() || ProductUtils.isUsvMode()) && SignalStrength.NUM_SIGNAL_STRENGTH_BINS == 6;
    }

    public static boolean isUST() {
        return OpFeatures.isSupport(new int[]{73}) && ("tmo".equals(SystemProperties.get("ro.boot.opcarrier")) || "C427".equals(SystemProperties.get("ro.boot.opcarrier")));
    }

    public static boolean isUSS() {
        return OpFeatures.isSupport(new int[]{128}) && "sprint".equals(SystemProperties.get("ro.boot.opcarrier"));
    }

    public static boolean isSupportShowVoLTE(Context context) {
        return !isUST() && !is3UKMccMnc(context) && !ProductUtils.isUsvMode();
    }

    public static boolean isSupportShowVoWifi() {
        return !isUST() && !ProductUtils.isUsvMode();
    }

    public static boolean isCustomFingerprint() {
        return OpFeatures.isSupport(new int[]{60});
    }

    public static boolean isSupportQuickLaunch() {
        return OpFeatures.isSupport(new int[]{86});
    }

    private static boolean isSupportWarpCharging() {
        boolean z = false;
        if (isMCLVersion() || OpFeatures.isSupport(new int[]{121}) || OpFeatures.isSupport(new int[]{238})) {
            z = true;
        }
        Log.i("OpUtils", "isSupportWarpCharging:" + z);
        return z;
    }

    private static boolean isSupportChargingAnimationV1() {
        boolean isSupport = OpFeatures.isSupport(new int[]{272});
        Log.i("OpUtils", "isSupportChargingAnimationV1:" + isSupport);
        return isSupport;
    }

    private static boolean isSupportChargingAnimationV2() {
        boolean z = true;
        if (!OpFeatures.isSupport(new int[]{305}) && !SystemProperties.getBoolean("persist.test.chargingv2", false)) {
            z = false;
        }
        Log.i("OpUtils", "isSupportChargingAnimationV2:" + z);
        return z;
    }

    private static boolean isSupportSWarpCharging() {
        boolean z = true;
        if (!OpFeatures.isSupport(new int[]{238}) && !SystemProperties.getBoolean("persist.test.chargingv2", false)) {
            z = false;
        }
        Log.i("OpUtils", "isSupportSWarpCharging:" + z);
        return z;
    }

    public static boolean isSupportREDCharging() {
        Log.i("OpUtils", "isSupportREDCharging:" + sIsREDCustomType);
        return sIsREDCustomType;
    }

    public static boolean isCMCC() {
        return OpCustomizeSettings.CUSTOM_TYPE.C88.equals(OpCustomizeSettings.getCustomType());
    }

    public static boolean isMCLVersion() {
        return sIsMCLCustomType;
    }

    public static boolean isMCLVersionFont() {
        return isMCLVersion() && OpFeatures.isSupport(new int[]{223});
    }

    public static boolean isREDVersion() {
        if (ThemeColorUtils.getCurrentTheme() == 1 && ThemeColorUtils.isSpecialTheme()) {
            return sIsREDCustomType || SystemProperties.getBoolean("persist.test.red", false);
        }
        return false;
    }

    public static boolean isSupportCustomFingerprintType2() {
        return SystemProperties.get("persist.vendor.oem.fp.version").equals("6");
    }

    public static boolean isSupportLinearVibration() {
        return OpFeatures.isSupport(new int[]{97});
    }

    public static boolean isSupportResolutionSwitch(Context context) {
        return mIsSupportResolutionSwitch;
    }

    private static boolean checkIsSupportResolutionSwitch(Context context) {
        if (context == null) {
            Log.e("OpUtils", "It can't accept null context");
            return false;
        }
        Display.Mode[] supportedModes = ((DisplayManager) context.getSystemService("display")).getDisplay(0).getSupportedModes();
        if (supportedModes == null || supportedModes.length <= 2) {
            return false;
        }
        return true;
    }

    public static boolean isSupportZVibrationMotor() {
        return OpFeatures.isSupport(new int[]{192});
    }

    public static boolean isSupportEmergencyPanel() {
        return !OpFeatures.isSupport(new int[]{116}) || OpFeatures.isSupport(new int[]{291});
    }

    public static boolean isPackageInstalled(Context context, String str) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(str, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("OpUtils", str + " is not installed." + e);
            packageInfo = null;
        }
        if (packageInfo != null) {
            return true;
        }
        return false;
    }

    public static boolean isPackageInstalledAsUser(Context context, String str, int i, int i2) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfoAsUser(str, 0, i2);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("OpUtils", str + " is not installed." + e);
            packageInfo = null;
        }
        if (packageInfo != null) {
            return true;
        }
        return false;
    }

    private static final class SettingsObserver extends ContentObserver {
        private static final Uri OP_DISPLAY_DENSITY_FORCE = Settings.Secure.getUriFor("display_density_forced");
        private static final Uri OP_QUICK_REPLY_PORTRAIT_ENABLE = Settings.System.getUriFor("quick_reply_portrait_enable");
        private static final Uri OP_SCREEN_RESOLUTION_ADJUST_URI = Settings.Global.getUriFor("oneplus_screen_resolution_adjust");
        private Context mContext;

        public SettingsObserver(Context context) {
            super(new Handler());
            this.mContext = context;
            ContentResolver contentResolver = context.getContentResolver();
            contentResolver.registerContentObserver(OP_SCREEN_RESOLUTION_ADJUST_URI, false, this, -1);
            contentResolver.registerContentObserver(OP_DISPLAY_DENSITY_FORCE, false, this, -1);
            contentResolver.registerContentObserver(OP_QUICK_REPLY_PORTRAIT_ENABLE, false, this, -1);
            onChange(true, null);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            boolean z2 = true;
            boolean z3 = uri == null;
            if (z3 || OP_SCREEN_RESOLUTION_ADJUST_URI.equals(uri)) {
                OpUtils.mScreenResolution = Settings.Global.getInt(contentResolver, "oneplus_screen_resolution_adjust", 2);
            }
            if (z3 || OP_QUICK_REPLY_PORTRAIT_ENABLE.equals(uri)) {
                if (Settings.System.getInt(contentResolver, "quick_reply_portrait_enable", 0) == 0) {
                    z2 = false;
                }
                OpUtils.QUICK_REPLY_PORTRAIT_ENABLED = z2;
                BubbleController bubbleController = (BubbleController) Dependency.get(BubbleController.class);
            }
            if (OpUtils.DEBUG_ONEPLUS) {
                Log.d("OpUtils", "update settings observer uri=" + uri + " mScreenResolution=" + OpUtils.mScreenResolution);
            }
        }
    }

    public static void updateScreenResolutionManually(Context context) {
        if (context != null) {
            mScreenResolution = Settings.Global.getInt(context.getContentResolver(), "oneplus_screen_resolution_adjust", 2);
            if (DEBUG_ONEPLUS) {
                Log.d("OpUtils", "updateScreenResolutionManually =" + mScreenResolution);
            }
        }
    }

    public static void updateDensityDpi(int i) {
        mDensityDpi = i;
    }

    private static int getCurrentDefaultDpi() {
        if (!mIsSupportResolutionSwitch) {
            return 420;
        }
        int i = mScreenResolution;
        if (i == 0 || i == 2) {
            return 560;
        }
        return 420;
    }

    private static int getCurrentDefaultDpi2() {
        if (!mIsSupportResolutionSwitch) {
            return 450;
        }
        int i = mScreenResolution;
        if (i == 0 || i == 2) {
            return 600;
        }
        return 450;
    }

    private static float getCurrentDefaultDensity() {
        if (!mIsSupportResolutionSwitch) {
            return 2.625f;
        }
        int i = mScreenResolution;
        if (i == 0 || i == 2) {
            return 3.5f;
        }
        return 2.625f;
    }

    public static boolean is2KResolution() {
        if (!mIsSupportResolutionSwitch) {
            return false;
        }
        int i = mScreenResolution;
        if (i == 0 || i == 2) {
            return true;
        }
        return false;
    }

    public static int convertDpToFixedPx(float f) {
        return Math.round(f * (((float) getCurrentDefaultDpi()) / ((float) mDensityDpi)));
    }

    public static int convertDpToFixedPx2(float f) {
        return Math.round(f * (((float) getCurrentDefaultDpi2()) / ((float) mDensityDpi)));
    }

    public static int convertSpToFixedPx(float f, float f2) {
        return Math.round((f / f2) * getCurrentDefaultDensity());
    }

    public static int convertPxByResolutionProportion(float f, int i) {
        return Math.round((f * ((float) (is2KResolution() ? 1440 : 1080))) / ((float) i));
    }

    public static boolean isEnableCustShutdownAnim(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "enable_cust_shutdown_anim", 0) == 1;
    }

    public static boolean isSprintMccMnc(Context context) {
        String mmcMnc = getMmcMnc(context, 0);
        if (mmcMnc != null) {
            int i = 0;
            while (true) {
                String[] strArr = SprintMmcMnc;
                if (i >= strArr.length) {
                    break;
                } else if (mmcMnc.equals(strArr[i])) {
                    return true;
                } else {
                    i++;
                }
            }
        }
        return false;
    }

    public static String getMmcMnc(Context context, int i) {
        TelephonyManager telephonyManager;
        String str = null;
        if (context == null || (telephonyManager = (TelephonyManager) context.getSystemService("phone")) == null) {
            return null;
        }
        if (i == 0) {
            String subscriberId = telephonyManager.getSubscriberId();
            if (DEBUG_ONEPLUS) {
                Log.i("OpUtils", "getMmcMnc / imsi:" + subscriberId);
            }
            if (!TextUtils.isEmpty(subscriberId) && subscriberId.length() > 6) {
                str = subscriberId.substring(0, 6);
            }
        } else if (i == 1) {
            str = telephonyManager.getSimOperator();
        }
        if (DEBUG_ONEPLUS) {
            Log.i("OpUtils", "getMmcMnc / mccmnc:" + str);
        }
        return str;
    }

    public static String getResourceName(Context context, int i) {
        if (i == 0) {
            return "(null)";
        }
        try {
            return context.getResources().getResourceName(i);
        } catch (Resources.NotFoundException unused) {
            return "(unknown)";
        }
    }

    public static Typeface getMclTypeface(int i) {
        return mTypefaceCache.get(Integer.valueOf(i));
    }

    private static Typeface getTypefaceByPath(String str) {
        try {
            return Typeface.createFromFile(str);
        } catch (RuntimeException e) {
            Log.e("OpUtils", "RuntimeException, " + e.getMessage());
            return null;
        }
    }

    private static void loadMCLTypeface() {
        if (new File("/op1/fonts/McLarenBespoke_Lt.ttf").exists()) {
            mTypefaceCache.put(1, getTypefaceByPath("/op1/fonts/McLarenBespoke_Lt.ttf"));
            mTypefaceCache.put(2, getTypefaceByPath("/op1/fonts/McLarenBespoke_Bd.ttf"));
            mTypefaceCache.put(3, getTypefaceByPath("/op1/fonts/McLarenBespoke_Rg.ttf"));
            return;
        }
        Log.d("OpUtils", "Load MCL Typeface failed. Font does not exist: /op1/fonts/McLarenBespoke_Lt.ttf");
    }

    public static void notifyStatusBarIconsDark(boolean z) {
        sStatusBarIconsDark = z;
    }

    public static boolean isStatusBarIconsDark() {
        return sStatusBarIconsDark;
    }

    public static long getBatteryTimeRemaining(Context context) {
        BatteryStatsHelper batteryStatsHelper = new BatteryStatsHelper(context, true);
        batteryStatsHelper.create((Bundle) null);
        return PowerUtil.convertUsToMs(batteryStatsHelper.getStats().computeBatteryTimeRemaining(SystemClock.elapsedRealtime()));
    }

    public static boolean isWLBAllowed(Context context) {
        return isAppExists(context, "com.oneplus.opwlb");
    }

    private static boolean isAppExists(Context context, String str) {
        return getApplicationInfo(context, str) != null;
    }

    private static ApplicationInfo getApplicationInfo(Context context, String str) {
        try {
            return context.getPackageManager().getApplicationInfo(str, 0);
        } catch (PackageManager.NameNotFoundException unused) {
            Log.d("OpUtils", "App not exists");
            return null;
        }
    }

    public static boolean is3UKMccMnc(Context context) {
        String mmcMnc = getMmcMnc(context, 1);
        if (mmcMnc != null) {
            int i = 0;
            while (true) {
                String[] strArr = MmcMnc3UK;
                if (i >= strArr.length) {
                    break;
                } else if (mmcMnc.equals(strArr[i])) {
                    return true;
                } else {
                    i++;
                }
            }
        }
        return false;
    }

    public static boolean isSupportHolePunchFrontCam() {
        return OpFeatures.isSupport(new int[]{230});
    }

    public static boolean isHolePunchCutoutHide(Context context) {
        return isSupportHolePunchFrontCam() && context.getResources().getBoolean(17891488);
    }

    public static boolean isSupportShowDisabledIcon(Context context) {
        if (context == null) {
            return true;
        }
        return !isWindTreMccMnc(context);
    }

    public static boolean isWindTreMccMnc(Context context) {
        String mmcMnc = getMmcMnc(context, 1);
        if (mmcMnc != null) {
            int i = 0;
            while (true) {
                String[] strArr = WindTreMmcMnc;
                if (i >= strArr.length) {
                    break;
                } else if (mmcMnc.equals(strArr[i])) {
                    return true;
                } else {
                    i++;
                }
            }
        }
        return false;
    }

    public static boolean isCutoutHide(Context context) {
        if (context != null) {
            return context.getResources().getBoolean(17891488);
        }
        Log.d("OpUtils", "context is null");
        return false;
    }

    public static int getCutoutPathdataHeight(Context context) {
        String string = (!isSupportResolutionSwitch(context) || !is2KResolution()) ? "" : context.getResources().getString(84869171);
        if (string.isEmpty()) {
            string = context.getResources().getString(17039922);
        }
        try {
            Path createPathFromPathData = PathParser.createPathFromPathData(string);
            RectF rectF = new RectF();
            createPathFromPathData.computeBounds(rectF, false);
            Rect rect = new Rect();
            rectF.round(rect);
            Log.i("OpUtils", "outRect:" + rect + ", height:" + rect.height());
            return rect.height();
        } catch (Throwable th) {
            Log.i("OpUtils", "Could not inflate path: " + th.toString());
            return 0;
        }
    }

    public static boolean isCutoutEmulationEnabled() {
        new ArrayList();
        try {
            if (mOverlayManager != null) {
                for (OverlayInfo overlayInfo : mOverlayManager.getOverlayInfosForTarget("android", 0)) {
                    if ("com.android.internal.display_cutout_emulation".equals(overlayInfo.category) && overlayInfo.isEnabled()) {
                        if (!DEBUG_ONEPLUS) {
                            return true;
                        }
                        Log.d("OpUtils", "CutoutEmulation is enabled");
                        return true;
                    }
                }
            }
        } catch (RemoteException e) {
            Log.d("OpUtils", "isCutoutEmulationEnabled: " + e.toString());
        }
        return false;
    }

    public static int getMaxDotsForNotificationIconContainer(Context context) {
        if (context != null) {
            return (!OpFeatures.isSupport(new int[]{49}) || OpFeatures.isSupport(new int[]{230}) || isCutoutHide(context)) ? 1 : 0;
        }
        Log.d("OpUtils", "getMaxDotsForNotificationIconContainer context is null");
        return 0;
    }

    public static boolean isWeakFaceUnlockEnabled() {
        return OpFeatures.isSupport(new int[]{263});
    }

    public static boolean isFaceUnlockSupportPassiveWakeup() {
        return OpFeatures.isSupport(new int[]{261});
    }

    public static int getSimCount() {
        return TelephonyManager.getDefault().getSimCount();
    }

    public static boolean isSupportCutout() {
        return OpFeatures.isSupport(new int[]{50});
    }

    public static boolean isSupportDoubleTapAlexa() {
        return OpFeatures.isSupport(new int[]{273});
    }

    public static void setIsEditTileBefore(Context context, boolean z) {
        if (isSupportDoubleTapAlexa()) {
            if (z && !mEditTileBefore) {
                Settings.Secure.putIntForUser(context.getContentResolver(), "sysui_qs_edited", 1, 0);
                Log.d("OpUtils", "edit tile");
            }
            mEditTileBefore = z;
        }
    }

    public static boolean getIsEditTileBefore() {
        if (!isSupportDoubleTapAlexa()) {
            return true;
        }
        return mEditTileBefore;
    }

    public static boolean isReallyHasOneSim() {
        return getSimCount() == 1 && TelephonyManager.getDefault().getPhoneCount() == 1;
    }

    public static boolean isVzwSIM(int i) {
        String[] strArr = mSimType;
        if (strArr == null || i < 0 || i >= strArr.length) {
            return false;
        }
        return "VZW4G".equalsIgnoreCase(strArr[i]) || "VZW3G".equalsIgnoreCase(mSimType[i]);
    }

    public static void setSimType(Context context, int i) {
        int[] subId;
        String[] strArr = mSimType;
        if (strArr == null || i < 0 || i >= strArr.length) {
            Log.d("OpUtils", "setSimType: Invalid phoneId " + i);
            return;
        }
        strArr[i] = getVzwSIM();
        if ("UNKNOWN".equals(mSimType[i]) && (subId = SubscriptionManager.getSubId(i)) != null && subId.length > 0) {
            mSimType[i] = getTmoSIM(context, subId[0]);
        }
        Log.d("OpUtils", "setSimType[" + i + "]: " + mSimType[i]);
    }

    private static String getVzwSIM() {
        try {
            IExtTelephony asInterface = IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone"));
            if (asInterface == null) {
                return "UNKNOWN";
            }
            Bundle bundle = new Bundle();
            bundle.putInt("phone", 0);
            Method declaredMethod = asInterface.getClass().getDeclaredMethod("generalGetter", String.class, Bundle.class);
            declaredMethod.setAccessible(true);
            return ((Bundle) declaredMethod.invoke(asInterface, "getVzwSimType", bundle)).getString("result", "UNKNOWN");
        } catch (Exception e) {
            e.printStackTrace();
            return "UNKNOWN";
        }
    }

    private static String getTmoSIM(Context context, int i) {
        CarrierConfigManager carrierConfigManager;
        PersistableBundle configForSubId;
        String string;
        try {
            String simOperator = ((TelephonyManager) context.getSystemService("phone")).getSimOperator(i);
            if ((simOperator == null || !"310260".equals(simOperator)) && ((carrierConfigManager = (CarrierConfigManager) context.getSystemService("carrier_config")) == null || (configForSubId = carrierConfigManager.getConfigForSubId(i)) == null || (string = configForSubId.getString("carrier_name_string")) == null || !"T-Mobile".equalsIgnoreCase(string))) {
                return "UNKNOWN";
            }
            return "TMO";
        } catch (Exception e) {
            e.printStackTrace();
            return "UNKNOWN";
        }
    }

    public static boolean isNavigationBarShowing() {
        return isNavigationBarShowing;
    }

    public static void setNavigationBarShowing(int i, boolean z) {
        if (DEBUG_ONEPLUS) {
            Log.d("OpUtils", "mode " + i + " visible " + z);
        }
        isNavigationBarShowing = i == 0 && z;
    }

    public static boolean isPrimaryOwnerMode(Context context) {
        UserInfo userInfo = ((UserManager) context.getSystemService("user")).getUserInfo(ActivityManager.getCurrentUser());
        if (userInfo == null) {
            return false;
        }
        return userInfo.isPrimary();
    }

    public static int getDimensionPixelSize(Resources resources, int i, int i2) {
        if (resources == null) {
            return 0;
        }
        int dimensionPixelSize = resources.getDimensionPixelSize(i);
        return mIsSupportResolutionSwitch ? convertPxByResolutionProportion((float) dimensionPixelSize, i2) : dimensionPixelSize;
    }

    public static boolean isRecentUnlockBiometricFace() {
        return mIsRecentUnlockBiometricFace;
    }

    public static boolean isRecentUnlockBiometricFinger() {
        return mIsRecentUnlockBiometricFinger;
    }

    public static void setRecentUnlockBiometricFace(boolean z) {
        mIsRecentUnlockBiometricFace = z;
    }

    public static void setRecentUnlockBiometricFinger(boolean z) {
        mIsRecentUnlockBiometricFinger = z;
    }

    public static boolean isKeyguardLocked(Context context) {
        return ((KeyguardManager) context.getSystemService("keyguard")).isKeyguardLocked();
    }

    public static boolean isGameModeOn(Context context) {
        if (context == null) {
            return false;
        }
        return TextUtils.equals(Settings.System.getStringForUser(context.getContentResolver(), "game_mode_status", -2), "1");
    }

    public static boolean gameToolboxEnable(Context context) {
        if (context == null) {
            return false;
        }
        return isGameModeOn(context) && Settings.System.getIntForUser(context.getContentResolver(), "game_toolbox_enable", 1, -2) == 1 && !isKeyguardLocked(context);
    }

    public static boolean isDisableExpandForTouch(Context context) {
        int intForUser = Settings.System.getIntForUser(context.getContentResolver(), "game_mode_prevent_mistouch", 0, -2);
        if (!isGameModeOn(context) || intForUser != 1 || isKeyguardLocked(context)) {
            return false;
        }
        return true;
    }

    public static String getTopClassName() {
        return mTopClassName;
    }

    public static String getTopPackageName() {
        return mPackageName;
    }

    public static String getTopActivityName(Context context) {
        List<ActivityManager.RunningTaskInfo> runningTasks = ((ActivityManager) context.getSystemService(ActivityManager.class)).getRunningTasks(1);
        if (runningTasks.isEmpty()) {
            return null;
        }
        return runningTasks.get(0).topActivity.getPackageName();
    }

    public static void updateSupportAssistantGestureState(Context context) {
        Log.d("OpUtils", "updateSupportAssistantGestureState ", new Throwable());
        sIsSupportAssistantGesture = isSupportAssistantGesture(context);
    }

    private static boolean isSupportAssistantGesture(Context context) {
        try {
            Bundle call = context.getContentResolver().call(LAUNCHER_FEATURE_URI, "checkFeature", "assistant_gesture", new Bundle());
            if (call == null) {
                Log.d("OpUtils", "methodIsSupportAssistantGesture bundle null");
                return true;
            } else if (call.getInt("result_code", -1) != 0) {
                Log.e("OpUtils", "methodIsSupportAssistantGesture " + call.getString("result_message", null));
                return true;
            } else if (!call.getBoolean("is_supported", true)) {
                Log.d("OpUtils", "not support assistant gesture");
                return false;
            } else {
                Log.d("OpUtils", "support assistant gesture");
                return true;
            }
        } catch (Exception e) {
            Log.d("OpUtils", "methodIsSupportAssistantGesture e1 = " + e);
            return true;
        }
    }

    public static void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println(" isClosedBeta:" + isClosedBeta());
        printWriter.println(" isOnePlusHomeApp:" + isOnePlusHomeApp());
        printWriter.println(" isHomeApp:" + isHomeApp());
        printWriter.println(" isScreenCompat:" + isScreenCompat());
        printWriter.println(" isInFullScreenListApp:" + isInFullScreenListApp());
        printWriter.println(" isCTS:" + isCTS());
        printWriter.println(" isCtsInputmethodservice:" + isCtsInputmethodservice());
        printWriter.println(" isCTSAdded:" + isCTSAdded());
        printWriter.println(" isSupportREDCharging:" + isSupportREDCharging());
        printWriter.println(" isREDVersion:" + isREDVersion());
        printWriter.println(" isSupportResolutionSwitch:" + mIsSupportResolutionSwitch);
        printWriter.println(" isEditTileBefore:" + getIsEditTileBefore());
        printWriter.println(" isNavigationBarShowing:" + isNavigationBarShowing());
        printWriter.println(" isRecentUnlockBiometricFace:" + isRecentUnlockBiometricFace());
        printWriter.println(" isRecentUnlockBiometricFinger:" + isRecentUnlockBiometricFinger());
        printWriter.println(" getTopPackageName:" + getTopPackageName());
        printWriter.println(" getTopClassName:" + getTopClassName());
    }

    public static int getPackageVersionCode(Context context, String str) {
        try {
            return context.getPackageManager().getPackageInfo(str, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("OpUtils", str + " is not installed." + e);
            return -1;
        }
    }
}
