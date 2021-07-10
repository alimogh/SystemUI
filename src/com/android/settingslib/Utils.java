package com.android.settingslib;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.ServiceState;
import android.text.format.Formatter;
import com.android.internal.annotations.VisibleForTesting;
import com.android.launcher3.icons.IconFactory;
import java.text.NumberFormat;
import java.util.Locale;
public class Utils {
    @VisibleForTesting
    static final String STORAGE_MANAGER_ENABLED_PROPERTY = "ro.storage_manager.enabled";
    public static final String[] UNIT_OF_STORAGE = {"%28?<%21[吉千兆太]%29比特", "%28?<%21[吉千兆太]%29字节", "吉比特", "吉字节", "千比特", "千字节", "兆比特", "兆字节", "太比特", "太字节"};
    public static final String[] UNIT_OF_STORAGE_REPLACE = {"b", "B", "Gb", "GB", "Kb", "KB", "Mb", "MB", "Tb", "TB"};
    private static String sPermissionControllerPackageName;
    private static String sServicesSystemSharedLibPackageName;
    private static String sSharedSystemSharedLibPackageName;
    private static Signature[] sSystemSignature;

    public static void updateLocationEnabled(Context context, boolean z, int i, int i2) {
        Settings.Secure.putIntForUser(context.getContentResolver(), "location_changer", i2, i);
        ((LocationManager) context.getSystemService(LocationManager.class)).setLocationEnabledForUser(z, UserHandle.of(i));
    }

    public static String formatPercentage(long j, long j2) {
        return formatPercentage(((double) j) / ((double) j2));
    }

    public static String formatPercentage(int i) {
        return formatPercentage(((double) i) / 100.0d);
    }

    public static String formatPercentage(double d) {
        return NumberFormat.getPercentInstance().format(d);
    }

    public static ColorStateList getColorAccent(Context context) {
        return getColorAttr(context, 16843829);
    }

    public static ColorStateList getColorError(Context context) {
        return getColorAttr(context, 16844099);
    }

    public static int getColorAccentDefaultColor(Context context) {
        return getColorAttrDefaultColor(context, 16843829);
    }

    public static int getColorErrorDefaultColor(Context context) {
        return getColorAttrDefaultColor(context, 16844099);
    }

    public static int getColorStateListDefaultColor(Context context, int i) {
        return context.getResources().getColorStateList(i, context.getTheme()).getDefaultColor();
    }

    public static int getDisabled(Context context, int i) {
        return applyAlphaAttr(context, 16842803, i);
    }

    public static int applyAlphaAttr(Context context, int i, int i2) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(new int[]{i});
        float f = obtainStyledAttributes.getFloat(0, 0.0f);
        obtainStyledAttributes.recycle();
        return applyAlpha(f, i2);
    }

    public static int applyAlpha(float f, int i) {
        return Color.argb((int) (f * ((float) Color.alpha(i))), Color.red(i), Color.green(i), Color.blue(i));
    }

    public static int getColorAttrDefaultColor(Context context, int i) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(new int[]{i});
        int color = obtainStyledAttributes.getColor(0, 0);
        obtainStyledAttributes.recycle();
        return color;
    }

    public static ColorStateList getColorAttr(Context context, int i) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(new int[]{i});
        try {
            return obtainStyledAttributes.getColorStateList(0);
        } finally {
            obtainStyledAttributes.recycle();
        }
    }

    public static int getThemeAttr(Context context, int i) {
        return getThemeAttr(context, i, 0);
    }

    public static int getThemeAttr(Context context, int i, int i2) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(new int[]{i});
        int resourceId = obtainStyledAttributes.getResourceId(0, i2);
        obtainStyledAttributes.recycle();
        return resourceId;
    }

    public static Drawable getDrawable(Context context, int i) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(new int[]{i});
        Drawable drawable = obtainStyledAttributes.getDrawable(0);
        obtainStyledAttributes.recycle();
        return drawable;
    }

    public static boolean isSystemPackage(Resources resources, PackageManager packageManager, PackageInfo packageInfo) {
        if (sSystemSignature == null) {
            sSystemSignature = new Signature[]{getSystemSignature(packageManager)};
        }
        if (sPermissionControllerPackageName == null) {
            sPermissionControllerPackageName = packageManager.getPermissionControllerPackageName();
        }
        if (sServicesSystemSharedLibPackageName == null) {
            sServicesSystemSharedLibPackageName = packageManager.getServicesSystemSharedLibraryPackageName();
        }
        if (sSharedSystemSharedLibPackageName == null) {
            sSharedSystemSharedLibPackageName = packageManager.getSharedSystemSharedLibraryPackageName();
        }
        Signature[] signatureArr = sSystemSignature;
        return (signatureArr[0] != null && signatureArr[0].equals(getFirstSignature(packageInfo))) || packageInfo.packageName.equals(sPermissionControllerPackageName) || packageInfo.packageName.equals(sServicesSystemSharedLibPackageName) || packageInfo.packageName.equals(sSharedSystemSharedLibPackageName) || packageInfo.packageName.equals("com.android.printspooler") || isDeviceProvisioningPackage(resources, packageInfo.packageName);
    }

    private static Signature getFirstSignature(PackageInfo packageInfo) {
        Signature[] signatureArr;
        if (packageInfo == null || (signatureArr = packageInfo.signatures) == null || signatureArr.length <= 0) {
            return null;
        }
        return signatureArr[0];
    }

    private static Signature getSystemSignature(PackageManager packageManager) {
        try {
            return getFirstSignature(packageManager.getPackageInfo("android", 64));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isDeviceProvisioningPackage(Resources resources, String str) {
        String string = resources.getString(17039890);
        return string != null && string.equals(str);
    }

    public static boolean isInService(ServiceState serviceState) {
        int combinedServiceState;
        return (serviceState == null || (combinedServiceState = getCombinedServiceState(serviceState)) == 3 || combinedServiceState == 1 || combinedServiceState == 2) ? false : true;
    }

    public static int getCombinedServiceState(ServiceState serviceState) {
        if (serviceState == null) {
            return 1;
        }
        int state = serviceState.getState();
        int dataRegistrationState = serviceState.getDataRegistrationState();
        if ((state == 1 || state == 2) && dataRegistrationState == 0 && isNotInIwlan(serviceState)) {
            return 0;
        }
        return state;
    }

    public static Drawable getBadgedIcon(Context context, Drawable drawable, UserHandle userHandle) {
        IconFactory obtain = IconFactory.obtain(context);
        try {
            BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), obtain.createBadgedIconBitmap(drawable, userHandle, true).icon);
            if (obtain != null) {
                obtain.close();
            }
            return bitmapDrawable;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public static Drawable getBadgedIcon(Context context, ApplicationInfo applicationInfo) {
        return getBadgedIcon(context, applicationInfo.loadUnbadgedIcon(context.getPackageManager()), UserHandle.getUserHandleForUid(applicationInfo.uid));
    }

    private static boolean isNotInIwlan(ServiceState serviceState) {
        NetworkRegistrationInfo networkRegistrationInfo = serviceState.getNetworkRegistrationInfo(2, 2);
        if (networkRegistrationInfo == null) {
            return true;
        }
        return !(networkRegistrationInfo.getRegistrationState() == 1 || networkRegistrationInfo.getRegistrationState() == 5);
    }

    public static String formatFileSize(Context context, long j) {
        String formatFileSize = Formatter.formatFileSize(context, j);
        if (Build.VERSION.SDK_INT > 26) {
            Locale locale = Locale.getDefault();
            String language = locale.getLanguage();
            String country = locale.getCountry();
            if (language.equalsIgnoreCase("zh") && country.equalsIgnoreCase("CN")) {
                int i = 0;
                while (true) {
                    String[] strArr = UNIT_OF_STORAGE;
                    if (i >= strArr.length) {
                        break;
                    }
                    formatFileSize = formatFileSize.replaceAll(strArr[i], UNIT_OF_STORAGE_REPLACE[i]);
                    i++;
                }
            }
        }
        return formatFileSize;
    }
}
