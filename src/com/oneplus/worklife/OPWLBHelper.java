package com.oneplus.worklife;

import android.app.AppOpsManager;
import android.app.INotificationManager;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.HashSet;
import java.util.Set;
public class OPWLBHelper {
    private static OPWLBHelper sOpwlbHelper;
    private AppOpsManager mAppOpsManager;
    private int mBreakMode = 0;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() { // from class: com.oneplus.worklife.OPWLBHelper.4
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.oneplus.wlb.intent.ACTION_RELOAD_NOTIFICATION".equals(action)) {
                if (OPWLBHelper.this.mWLBModeChangeListener != null) {
                    OPWLBHelper.this.mWLBModeChangeListener.onModeChanged();
                }
            } else if ("com.oneplus.intent.action_DISABLE_WLB_FEATURE".equals(action)) {
                int intExtra = intent.getIntExtra("enable", 0);
                Log.d("OPSystemUIWLBHelper", "FeatureEnable : " + intExtra);
                Settings.System.putInt(OPWLBHelper.this.mContext.getContentResolver(), "worklife_feature_enable", intExtra);
            }
        }
    };
    private Context mContext;
    private int mCurrentMode = 0;
    ContentObserver mDeviceProvisionedObserver = new ContentObserver(this.mHandler) { // from class: com.oneplus.worklife.OPWLBHelper.6
        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            super.onChange(z);
            boolean isDeviceProvisionedInSettingsDb = OPWLBHelper.this.isDeviceProvisionedInSettingsDb();
            if (isDeviceProvisionedInSettingsDb) {
                OPWLBHelper.this.persistAppStartMillis();
            }
            Log.d("OPSystemUIWLBHelper", "DeviceProvisionedObserver onChange isProvisioned " + isDeviceProvisionedInSettingsDb);
        }
    };
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private INotificationManager mINotificationManager;
    private boolean mLastWifiConnected;
    private Set<String> mMediaNotificationKeys = new HashSet();
    public NotificationEntryListener mNotificationEntryListener = new NotificationEntryListener() { // from class: com.oneplus.worklife.OPWLBHelper.5
        @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
        public void onNotificationAdded(NotificationEntry notificationEntry) {
            Log.d("OPSystemUIWLBHelper", "inside onNotificationAdded");
            OPWLBHelper.this.sendNotificationAddedBroadcast(notificationEntry.getSbn().getPackageName(), OPWLBHelper.this.getAppStartedTimestamp());
        }
    };
    private NotificationMediaManager mNotificationMediaManager;
    private PackageManager mPackageManager;
    private ContentObserver mSettingsObserver = new ContentObserver(new Handler()) { // from class: com.oneplus.worklife.OPWLBHelper.3
        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            int i = OPWLBHelper.this.mCurrentMode;
            int i2 = OPWLBHelper.this.mBreakMode;
            OPWLBHelper.this.readCurrentMode();
            if (!((i2 == OPWLBHelper.this.mBreakMode && i == OPWLBHelper.this.mCurrentMode) || OPWLBHelper.this.mWLBModeChangeListener == null)) {
                OPWLBHelper.this.mHandler.post(new Runnable() { // from class: com.oneplus.worklife.OPWLBHelper.3.1
                    @Override // java.lang.Runnable
                    public void run() {
                        OPWLBHelper.this.mWLBModeChangeListener.onModeChanged();
                    }
                });
            }
            Log.d("OPSystemUIWLBHelper", "Current Mode changed to : " + OPWLBHelper.this.mCurrentMode + " , " + uri + " breakMode: " + OPWLBHelper.this.mBreakMode + " , " + uri);
        }
    };
    private IStatusBarIconChangeListener mStatusBarIconChangeListener;
    private ContentObserver mStatusBarIconChangeObserver = new ContentObserver(new Handler()) { // from class: com.oneplus.worklife.OPWLBHelper.1
        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            final int i = Settings.System.getInt(OPWLBHelper.this.mContext.getContentResolver(), "oneplus_wlb_mode", 0);
            if (OPWLBHelper.this.mStatusBarIconChangeListener != null) {
                OPWLBHelper.this.mHandler.post(new Runnable() { // from class: com.oneplus.worklife.OPWLBHelper.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (i != 1 || !OPWLBHelper.this.isAdminUser()) {
                            OPWLBHelper.this.mStatusBarIconChangeListener.onHideWLBStatusBarIcon();
                        } else {
                            OPWLBHelper.this.mStatusBarIconChangeListener.onShowWLBStatusBarIcon();
                        }
                    }
                });
            }
            Log.d("OPSystemUIWLBHelper", "wlb enabled changed to : " + i + " , " + uri);
        }
    };
    private BroadcastReceiver mStatusBarIconChangeReceiver = new BroadcastReceiver() { // from class: com.oneplus.worklife.OPWLBHelper.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                UserInfo userInfo = ((UserManager) context.getSystemService("user")).getUserInfo(intent.getIntExtra("android.intent.extra.user_handle", -1));
                int i = Settings.System.getInt(OPWLBHelper.this.mContext.getContentResolver(), "oneplus_wlb_mode", 0);
                Log.d("OPSystemUIWLBHelper", "onReceive: isAdmin user " + userInfo.isAdmin() + " wlb enabled:" + i);
                if (OPWLBHelper.this.mStatusBarIconChangeListener == null) {
                    return;
                }
                if (!userInfo.isAdmin() || i != 1) {
                    OPWLBHelper.this.mStatusBarIconChangeListener.onHideWLBStatusBarIcon();
                } else {
                    OPWLBHelper.this.mStatusBarIconChangeListener.onShowWLBStatusBarIcon();
                }
            }
        }
    };
    private IWLBModeChangeListener mWLBModeChangeListener;

    public interface IStatusBarIconChangeListener {
        void onHideWLBStatusBarIcon();

        void onShowWLBStatusBarIcon();
    }

    public interface IWLBModeChangeListener {
        void onModeChanged();
    }

    public static OPWLBHelper getInstance(Context context) {
        if (sOpwlbHelper == null) {
            sOpwlbHelper = new OPWLBHelper(context);
        }
        return sOpwlbHelper;
    }

    public OPWLBHelper(Context context) {
        this.mContext = context;
        this.mINotificationManager = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mPackageManager = this.mContext.getPackageManager();
        readCurrentMode();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void persistAppStartMillis() {
        if (getAppStartedTimestamp().longValue() == 0) {
            Log.d("OPSystemUIWLBHelper", "persistAppStartMillis " + System.currentTimeMillis());
            Settings.System.putLong(this.mContext.getContentResolver(), "app_started_timestamp", System.currentTimeMillis());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Long getAppStartedTimestamp() {
        return Long.valueOf(Settings.System.getLong(this.mContext.getContentResolver(), "app_started_timestamp", 0));
    }

    public void processWifiConnectivity(boolean z) {
        if (this.mLastWifiConnected != z) {
            sendConnectedBroadcast(z);
            this.mLastWifiConnected = z;
        }
    }

    public void sendShutDownBroadcast() {
        Log.d("OPSystemUIWLBHelper", "sending Shutdown event to WLB");
        Intent intent = new Intent("com.oneplus.intent.ACTION_SHUTDOWN");
        intent.setPackage("com.oneplus.opwlb");
        this.mContext.sendBroadcast(intent);
    }

    private void sendConnectedBroadcast(boolean z) {
        Log.d("OPSystemUIWLBHelper", "wificonnected " + z);
        try {
            Intent intent = new Intent("com.oneplus.intent.ACTION_WIFI_CONNECTED");
            intent.setPackage("com.oneplus.opwlb");
            intent.putExtra("is_connected", z);
            this.mContext.sendBroadcast(intent);
        } catch (Exception e) {
            Log.d("OPSystemUIWLBHelper", "error while sending broadcast:" + e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendNotificationAddedBroadcast(String str, Long l) {
        Log.d("OPSystemUIWLBHelper", "sendNotificationAddedBroadcast " + str);
        if (TextUtils.isEmpty(str)) {
            Log.d("OPSystemUIWLBHelper", "Package name not found");
            return;
        }
        Intent intent = new Intent("com.oneplus.intent.NOTIFICATION_ADDED");
        intent.setPackage("com.oneplus.opwlb");
        intent.putExtra("package_name", str);
        intent.putExtra("first_boot_time", l);
        this.mContext.sendBroadcast(intent);
    }

    public NotificationEntryListener getNotificationEntryListener() {
        return this.mNotificationEntryListener;
    }

    public void onAllNotificationsCleared() {
        Intent intent = new Intent("com.oneplus.intent.NOTIFICATION_CLEAR_ALL");
        intent.setPackage("com.oneplus.opwlb");
        intent.putExtra("first_boot_time", getAppStartedTimestamp());
        this.mContext.sendBroadcast(intent);
    }

    public void registerStatusBarObserver(IStatusBarIconChangeListener iStatusBarIconChangeListener) {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("oneplus_wlb_mode"), false, this.mStatusBarIconChangeObserver);
        this.mContext.registerReceiver(this.mStatusBarIconChangeReceiver, new IntentFilter("android.intent.action.USER_SWITCHED"));
        this.mStatusBarIconChangeListener = iStatusBarIconChangeListener;
    }

    public void registerChanges(IWLBModeChangeListener iWLBModeChangeListener) {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("oneplus_wlb_activated_mode"), false, this.mSettingsObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("device_provisioned"), false, this.mDeviceProvisionedObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("oneplus_wlb_break_mode_activated"), false, this.mSettingsObserver);
        IntentFilter intentFilter = new IntentFilter("com.oneplus.wlb.intent.ACTION_RELOAD_NOTIFICATION");
        intentFilter.addAction("com.oneplus.intent.action_DISABLE_WLB_FEATURE");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
        this.mWLBModeChangeListener = iWLBModeChangeListener;
    }

    public boolean isWLBEnabled() {
        return Settings.System.getInt(this.mContext.getContentResolver(), "oneplus_wlb_mode", 0) == 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readCurrentMode() {
        this.mCurrentMode = Settings.System.getInt(this.mContext.getContentResolver(), "oneplus_wlb_activated_mode", 0);
        this.mBreakMode = Settings.System.getInt(this.mContext.getContentResolver(), "oneplus_wlb_break_mode_activated", 0);
    }

    public boolean isApplicationBlocked(StatusBarNotification statusBarNotification, NotificationChannel notificationChannel) {
        String packageName = statusBarNotification.getPackageName();
        boolean isMediaNotificationAllowed = isMediaNotificationAllowed(statusBarNotification);
        if (packageName == null || packageName.isEmpty() || this.mCurrentMode == 0 || this.mBreakMode == 1 || isMediaNotificationAllowed) {
            return false;
        }
        if (isGAccountNotificationAllowed(statusBarNotification, notificationChannel)) {
            return true;
        }
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = this.mPackageManager.getApplicationInfo(packageName, 128);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("OPSystemUIWLBHelper", "Couldn't find package", e);
        }
        if (applicationInfo == null) {
            return false;
        }
        int i = this.mCurrentMode;
        if (i == 2) {
            int checkOpNoThrow = this.mAppOpsManager.checkOpNoThrow(1009, applicationInfo.uid, packageName);
            Log.d("OPSystemUIWLBHelper", "isApplicationBlocked " + packageName + " " + checkOpNoThrow + " " + this.mCurrentMode);
            if (checkOpNoThrow == 0) {
                return true;
            }
            return false;
        } else if (i != 1) {
            return false;
        } else {
            int checkOpNoThrow2 = this.mAppOpsManager.checkOpNoThrow(1008, applicationInfo.uid, packageName);
            Log.d("OPSystemUIWLBHelper", "isApplicationBlocked " + packageName + " " + checkOpNoThrow2 + " " + this.mCurrentMode);
            if (checkOpNoThrow2 == 0) {
                return true;
            }
            return false;
        }
    }

    public void removeNotificationKey(String str) {
        if (!this.mMediaNotificationKeys.isEmpty()) {
            this.mMediaNotificationKeys.remove(str);
        }
    }

    private boolean isGAccountNotificationAllowed(StatusBarNotification statusBarNotification, NotificationChannel notificationChannel) {
        if (this.mINotificationManager != null) {
            String packageName = statusBarNotification.getPackageName();
            int uid = statusBarNotification.getUid();
            String group = notificationChannel.getGroup();
            if (group != null) {
                try {
                    NotificationChannelGroup notificationChannelGroupForPackage = this.mINotificationManager.getNotificationChannelGroupForPackage(group, packageName, uid);
                    if (notificationChannelGroupForPackage != null) {
                        CharSequence name = notificationChannelGroupForPackage.getName();
                        if (!TextUtils.isEmpty(name) && (Settings.System.getInt(this.mContext.getContentResolver(), name.toString(), 0) & this.mCurrentMode) == this.mCurrentMode) {
                            return true;
                        }
                    }
                } catch (RemoteException e) {
                    Log.d("OPSystemUIWLBHelper", "unable to find the notification channel:" + e.getMessage());
                }
            }
        } else {
            Log.d("OPSystemUIWLBHelper", "notification manager is null");
        }
        return false;
    }

    public boolean isMediaNotificationAllowed(StatusBarNotification statusBarNotification) {
        if (statusBarNotification == null || statusBarNotification.getPackageName() == null) {
            return false;
        }
        String key = statusBarNotification.getKey();
        NotificationMediaManager notificationMediaManager = (NotificationMediaManager) Dependency.get(NotificationMediaManager.class);
        this.mNotificationMediaManager = notificationMediaManager;
        if (notificationMediaManager == null) {
            return false;
        }
        String mediaNotificationKey = notificationMediaManager.getMediaNotificationKey();
        if (mediaNotificationKey != null && mediaNotificationKey.equals(key)) {
            this.mMediaNotificationKeys.add(key);
        }
        return this.mMediaNotificationKeys.contains(key);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDeviceProvisionedInSettingsDb() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isAdminUser() {
        long serialNumberForUser = ((UserManager) this.mContext.getSystemService("user")).getSerialNumberForUser(Process.myUserHandle());
        Log.d("OPSystemUIWLBHelper", "uId:" + serialNumberForUser);
        return serialNumberForUser == 0;
    }

    public void checkAndIncludeWLBTile() {
        String stringForUser = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "sysui_qs_tiles", -2);
        boolean z = false;
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "sysui_wlb_tile_added", 0) == 1) {
            z = true;
        }
        Log.d("OPSystemUIWLBHelper", "qs_tile list:" + stringForUser + " \n isWLBTileAdded:" + z);
        if ((TextUtils.isEmpty(stringForUser) || (!stringForUser.contains("custom(com.oneplus.opwlb/com.oneplus.opwlb.services.WLBTileService)") && !stringForUser.contains("custom(com.oneplus.opwlb/.services.WLBTileService)"))) && !z) {
            Log.d("OPSystemUIWLBHelper", "adding WLB tile:");
            Intent intent = new Intent("com.oneplus.systemui.qs.hide_tile");
            intent.putExtra("tile", "custom(com.oneplus.opwlb/.services.WLBTileService)");
            intent.putExtra("position", 11);
            this.mContext.sendBroadcast(intent);
            Settings.Secure.putInt(this.mContext.getContentResolver(), "sysui_wlb_tile_added", 1);
        } else if (!TextUtils.isEmpty(stringForUser) && stringForUser.contains("custom(com.oneplus.opwlb/com.oneplus.opwlb.services.WLBTileService)")) {
            String replace = stringForUser.replace("custom(com.oneplus.opwlb/com.oneplus.opwlb.services.WLBTileService)", "custom(com.oneplus.opwlb/.services.WLBTileService)");
            Log.d("OPSystemUIWLBHelper", "replacing tilelist:" + replace + " \n status: " + Settings.Secure.putString(this.mContext.getContentResolver(), "sysui_qs_tiles", replace));
            Settings.Secure.putInt(this.mContext.getContentResolver(), "sysui_wlb_tile_added", 1);
        }
    }
}
