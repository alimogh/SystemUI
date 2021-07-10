package com.android.systemui.util;

import android.app.INotificationManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0015R$string;
import com.android.systemui.SystemUI;
import java.util.Arrays;
import java.util.List;
public class NotificationChannels extends SystemUI {
    public static String ALERTS = "ALR";
    public static String BATTERY = "BAT";
    public static String GENERAL = "GEN";
    public static String HINTS = "HNT";
    public static String ONEPLUS_PACKAGENAME_MMS = "com.oneplus.mms";
    public static String SCREENSHOTS_HEADSUP = "SCN_HEADSUP";
    public static String SCREENSHOTS_LEGACY = "SCN";
    public static String STORAGE = "DSK";
    public static String TVPIP = "TPP";

    public NotificationChannels(Context context) {
        super(context);
    }

    public static void createAll(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NotificationManager.class);
        NotificationChannel notificationChannel = new NotificationChannel(BATTERY, context.getString(C0015R$string.notification_channel_battery), 5);
        String string = Settings.Global.getString(context.getContentResolver(), "low_battery_sound");
        notificationChannel.setSound(Uri.parse("file://" + string), new AudioAttributes.Builder().setContentType(4).setUsage(10).build());
        notificationChannel.setBlockable(true);
        notificationManager.createNotificationChannels(Arrays.asList(new NotificationChannel(ALERTS, context.getString(C0015R$string.notification_channel_alerts), 4), new NotificationChannel(GENERAL, context.getString(C0015R$string.notification_channel_general), 1), new NotificationChannel(STORAGE, context.getString(C0015R$string.notification_channel_storage), isTv(context) ? 3 : 2), createScreenshotChannel(context.getString(C0015R$string.notification_channel_screenshot), notificationManager.getNotificationChannel(SCREENSHOTS_LEGACY)), notificationChannel, new NotificationChannel(HINTS, context.getString(C0015R$string.notification_channel_hints), 3)));
        notificationManager.deleteNotificationChannel(SCREENSHOTS_LEGACY);
        if (isTv(context)) {
            notificationManager.createNotificationChannel(new NotificationChannel(TVPIP, context.getString(C0015R$string.notification_channel_tv_pip), 5));
        }
        resetMMSDefaultChannelIfNeeded(context, notificationManager);
    }

    private static void resetMMSDefaultChannelIfNeeded(Context context, NotificationManager notificationManager) {
        INotificationManager asInterface = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
        List<ApplicationInfo> installedApplications = context.getPackageManager().getInstalledApplications(512);
        for (int i = 0; i < installedApplications.size(); i++) {
            ApplicationInfo applicationInfo = installedApplications.get(i);
            if (applicationInfo != null && ONEPLUS_PACKAGENAME_MMS.equals(applicationInfo.packageName)) {
                try {
                    NotificationChannel notificationChannelForPackage = asInterface.getNotificationChannelForPackage(applicationInfo.packageName, applicationInfo.uid, "default", (String) null, true);
                    if (!(notificationChannelForPackage == null || notificationChannelForPackage.getImportance() == 4)) {
                        notificationChannelForPackage.setImportance(4);
                        asInterface.updateNotificationChannelForPackage(applicationInfo.packageName, applicationInfo.uid, notificationChannelForPackage);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @VisibleForTesting
    static NotificationChannel createScreenshotChannel(String str, NotificationChannel notificationChannel) {
        NotificationChannel notificationChannel2 = new NotificationChannel(SCREENSHOTS_HEADSUP, str, 4);
        notificationChannel2.setSound(null, new AudioAttributes.Builder().setUsage(5).build());
        notificationChannel2.setBlockable(true);
        if (notificationChannel != null) {
            int userLockedFields = notificationChannel.getUserLockedFields();
            if ((userLockedFields & 4) != 0) {
                notificationChannel2.setImportance(notificationChannel.getImportance());
            }
            if ((userLockedFields & 32) != 0) {
                notificationChannel2.setSound(notificationChannel.getSound(), notificationChannel.getAudioAttributes());
            }
            if ((userLockedFields & 16) != 0) {
                notificationChannel2.setVibrationPattern(notificationChannel.getVibrationPattern());
            }
            if ((userLockedFields & 8) != 0) {
                notificationChannel2.setLightColor(notificationChannel.getLightColor());
            }
        }
        return notificationChannel2;
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        createAll(this.mContext);
    }

    private static boolean isTv(Context context) {
        return context.getPackageManager().hasSystemFeature("android.software.leanback");
    }
}
