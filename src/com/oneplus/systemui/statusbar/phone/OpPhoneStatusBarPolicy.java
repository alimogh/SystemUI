package com.oneplus.systemui.statusbar.phone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.util.OpFeatures;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.android.systemui.statusbar.phone.PhoneStatusBarPolicy;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.util.NotificationChannels;
import com.android.systemui.util.ProductUtils;
import com.oneplus.opzenmode.OpZenModeController;
import com.oneplus.util.OpReflectionUtils;
import com.oneplus.util.OpUtils;
public class OpPhoneStatusBarPolicy implements OpZenModeController.Callback {
    private static boolean DEBUG_ONEPLUS = Build.DEBUG_ONEPLUS;
    private final Handler mBgHandler = new Handler((Looper) Dependency.get(Dependency.BG_LOOPER));
    private BluetoothController mBluetooth;
    private int mBluetoothBatteryLevel = -1;
    private boolean mBluetoothConnected = false;
    private String mBluetoothContentDescription = null;
    private int mBluetoothIconId = 0;
    private boolean mBluetoothIconVisible = false;
    private Context mContext;
    private StatusBarIconController mIconController;
    BroadcastReceiver mIntentReceiver = new BroadcastReceiver() { // from class: com.oneplus.systemui.statusbar.phone.OpPhoneStatusBarPolicy.1
        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            char c;
            String action = intent.getAction();
            boolean z = false;
            switch (action.hashCode()) {
                case -1945282276:
                    if (action.equals("oneplus.intent.action.VZW_VICE_STATE_CHANGED")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -688653806:
                    if (action.equals("oneplus.intent.action.VZW_FEMTOCELL_STATE_CHANGED")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 579327048:
                    if (action.equals("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 880816517:
                    if (action.equals("com.oem.intent.action.ACTION_USB_HEADSET_PLUG")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                if (OpPhoneStatusBarPolicy.DEBUG_ONEPLUS) {
                    Log.d("OpPhoneStatusBarPolicy", "BT battery level changed");
                }
                OpPhoneStatusBarPolicy.this.updateBluetooth();
            } else if (c == 1) {
                OpPhoneStatusBarPolicy.this.updateHeadsetPlug(intent);
            } else if (c == 2) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    z = extras.getBoolean("isVzwFemtocell");
                }
                Log.d("OpPhoneStatusBarPolicy", "onReceive: status = " + z);
                OpPhoneStatusBarPolicy.this.updateFemtocell(z);
            } else if (c == 3) {
                Bundle extras2 = intent.getExtras();
                if (extras2 != null) {
                    z = extras2.getBoolean("isVzwVice");
                }
                Log.d("OpPhoneStatusBarPolicy", "onReceive: viceStatus = " + z);
                OpPhoneStatusBarPolicy.this.updateVice(z);
            }
        }
    };
    private boolean mIsNotifyShown = false;
    private final BroadcastReceiver mNfcReceiver = new BroadcastReceiver() { // from class: com.oneplus.systemui.statusbar.phone.OpPhoneStatusBarPolicy.3
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int intExtra = intent.getIntExtra("android.nfc.extra.ADAPTER_STATE", 0);
            if (intExtra == 3 || intExtra == 2) {
                OpPhoneStatusBarPolicy.this.mIconController.setIconVisibility("nfc", true);
            } else {
                OpPhoneStatusBarPolicy.this.mIconController.setIconVisibility("nfc", false);
            }
        }
    };
    private NotificationManager mNotificationManager = null;
    private OpZenModeController mOpZenModeController;
    private final SettingObserver mSettingObserver = new SettingObserver();
    private SharedPreferences mSharedPreferences;
    private Runnable mShowRunnable = new Runnable() { // from class: com.oneplus.systemui.statusbar.phone.OpPhoneStatusBarPolicy.2
        @Override // java.lang.Runnable
        public void run() {
            Log.d("OpPhoneStatusBarPolicy", "set BT Icon: " + OpPhoneStatusBarPolicy.this.mBluetoothIconVisible);
            OpPhoneStatusBarPolicy.this.mIconController.setIcon(OpPhoneStatusBarPolicy.this.getSlotBluetooth(), OpPhoneStatusBarPolicy.this.mBluetoothIconId, OpPhoneStatusBarPolicy.this.mBluetoothContentDescription);
            OpPhoneStatusBarPolicy.this.mIconController.setIconVisibility(OpPhoneStatusBarPolicy.this.getSlotBluetooth(), OpPhoneStatusBarPolicy.this.mBluetoothIconVisible);
        }
    };
    private String mSlotFemtoCell;
    private String mSlotVice;
    private int mVibrateWhenMute = 0;

    public OpPhoneStatusBarPolicy(Context context, StatusBarIconController statusBarIconController, SharedPreferences sharedPreferences) {
        this.mContext = context;
        this.mIconController = statusBarIconController;
        this.mSharedPreferences = sharedPreferences;
        context.getContentResolver().registerContentObserver(Settings.System.getUriFor("oem_vibrate_under_silent"), false, this.mSettingObserver, -1);
        OpZenModeController opZenModeController = (OpZenModeController) Dependency.get(OpZenModeController.class);
        this.mOpZenModeController = opZenModeController;
        opZenModeController.addCallback(this);
        this.mBluetooth = (BluetoothController) Dependency.get(BluetoothController.class);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED");
        boolean z = true;
        if (OpFeatures.isSupport(new int[]{48})) {
            boolean z2 = this.mContext.getSharedPreferences("pref_name_notify_shown", 0).getBoolean("pref_key_notify_shown", false);
            this.mIsNotifyShown = z2;
            if (!z2) {
                intentFilter.addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
                this.mNotificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
            }
        }
        if (ProductUtils.isUsvMode()) {
            this.mSlotFemtoCell = "femtocell";
            intentFilter.addAction("oneplus.intent.action.VZW_FEMTOCELL_STATE_CHANGED");
            this.mSlotVice = "vice";
            intentFilter.addAction("oneplus.intent.action.VZW_VICE_STATE_CHANGED");
        }
        intentFilter.addAction("com.oem.intent.action.ACTION_USB_HEADSET_PLUG");
        this.mContext.registerReceiver(this.mIntentReceiver, intentFilter, null, getHandler());
        this.mIconController.setIcon("nfc", C0006R$drawable.stat_sys_nfc, null);
        try {
            NfcAdapter nfcAdapter = NfcAdapter.getNfcAdapter(this.mContext);
            StatusBarIconController statusBarIconController2 = this.mIconController;
            if (nfcAdapter == null || !nfcAdapter.isEnabled()) {
                z = false;
            }
            statusBarIconController2.setIconVisibility("nfc", z);
        } catch (UnsupportedOperationException e) {
            Log.e("OpPhoneStatusBarPolicy", "Fail to get Nfc adapter " + e);
            this.mIconController.setIconVisibility("nfc", false);
        }
        this.mContext.registerReceiverAsUser(this.mNfcReceiver, UserHandle.ALL, new IntentFilter("android.nfc.action.ADAPTER_STATE_CHANGED"), null, null);
    }

    /* access modifiers changed from: protected */
    public void OpUpdateBluetooth() {
        this.mBgHandler.post(new Runnable() { // from class: com.oneplus.systemui.statusbar.phone.-$$Lambda$OpPhoneStatusBarPolicy$YxXtr18s6494UMI7t9rSTB_EmFk
            @Override // java.lang.Runnable
            public final void run() {
                OpPhoneStatusBarPolicy.this.lambda$OpUpdateBluetooth$0$OpPhoneStatusBarPolicy();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$OpUpdateBluetooth$0 */
    public /* synthetic */ void lambda$OpUpdateBluetooth$0$OpPhoneStatusBarPolicy() {
        int i;
        String str;
        int i2;
        boolean z;
        BluetoothController bluetoothController = this.mBluetooth;
        boolean z2 = false;
        if (bluetoothController != null) {
            boolean isBluetoothConnected = bluetoothController.isBluetoothConnected();
            if (OpUtils.isUST()) {
                z2 = this.mBluetooth.isBluetoothEnabled();
            } else if (isBluetoothConnected) {
                z2 = this.mBluetooth.isBluetoothEnabled();
            }
            if (isBluetoothConnected) {
                str = this.mContext.getString(C0015R$string.accessibility_bluetooth_connected);
                i = this.mBluetooth.getBatteryLevel();
                if (i == -1 || getBluetoothBatteryIcon(i) == 0) {
                    i2 = C0006R$drawable.stat_sys_data_bluetooth_connected;
                } else {
                    i2 = getBluetoothBatteryIcon(i);
                }
                z2 = isBluetoothConnected;
                z = z2;
            } else {
                z2 = isBluetoothConnected;
                z = z2;
                i = -1;
                i2 = C0006R$drawable.stat_sys_data_bluetooth;
                str = this.mContext.getString(C0015R$string.accessibility_quick_settings_bluetooth_on);
            }
        } else {
            Log.e("OpPhoneStatusBarPolicy", "BluetoothController == null");
            int i3 = C0006R$drawable.stat_sys_data_bluetooth;
            str = this.mContext.getString(C0015R$string.accessibility_quick_settings_bluetooth_on);
            i = -1;
            i2 = i3;
            z = false;
        }
        Log.d("OpPhoneStatusBarPolicy", "OpUpdateBluetooth, " + z2 + ", " + z);
        if (!(this.mBluetoothIconId == i2 && this.mBluetoothIconVisible == z && this.mBluetoothConnected == z2 && this.mBluetoothBatteryLevel == i && (str == null || str.equals(this.mBluetoothContentDescription)))) {
            StringBuilder sb = new StringBuilder("updateBluetooth");
            sb.append(" mBluetooth is ");
            sb.append(this.mBluetooth == null ? "" : "not ");
            sb.append("null");
            sb.append(" mIconId=");
            sb.append(this.mBluetoothIconId);
            sb.append(" iconId=");
            sb.append(i2);
            sb.append(" mConnected=");
            sb.append(this.mBluetoothConnected);
            sb.append(" connected=");
            sb.append(z2);
            sb.append(" mVisible=");
            sb.append(this.mBluetoothIconVisible);
            sb.append(", visible=");
            sb.append(z);
            sb.append(" mDescrip=");
            sb.append(this.mBluetoothContentDescription);
            sb.append(", descrip=");
            sb.append(str);
            sb.append(" mLevel=");
            sb.append(this.mBluetoothBatteryLevel);
            sb.append(" batteryLevel=");
            sb.append(i);
            Log.d("OpPhoneStatusBarPolicy", sb.toString());
            this.mBluetoothIconId = i2;
            this.mBluetoothIconVisible = z;
            this.mBluetoothConnected = z2;
            this.mBluetoothBatteryLevel = i;
            this.mBluetoothContentDescription = str;
        }
        postShowBT();
    }

    private void postShowBT() {
        getHandler().removeCallbacks(this.mShowRunnable);
        getHandler().postDelayed(this.mShowRunnable, 50);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x00ad  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00ef  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.oneplus.systemui.statusbar.phone.OpPhoneStatusBarPolicy.OpUpdateVolumeZenObject updateVolumeZen(int r13) {
        /*
        // Method dump skipped, instructions count: 287
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.systemui.statusbar.phone.OpPhoneStatusBarPolicy.updateVolumeZen(int):com.oneplus.systemui.statusbar.phone.OpPhoneStatusBarPolicy$OpUpdateVolumeZenObject");
    }

    public class OpUpdateVolumeZenObject {
        public String volumeDescription = null;
        public int volumeIconId = 0;
        public boolean volumeVisible = false;
        public String zenDescription = null;
        public int zenIconId = 0;
        public boolean zenVisible = false;

        OpUpdateVolumeZenObject(OpPhoneStatusBarPolicy opPhoneStatusBarPolicy, boolean z, int i, String str, boolean z2, int i2, String str2) {
            this.zenVisible = z;
            this.zenIconId = i;
            this.zenDescription = str;
            this.volumeVisible = z2;
            this.volumeIconId = i2;
            this.volumeDescription = str2;
        }
    }

    private int getBluetoothBatteryIcon(int i) {
        switch (i / 10) {
            case 0:
                return C0006R$drawable.stat_sys_bt_battery_0;
            case 1:
                return C0006R$drawable.stat_sys_bt_battery_1;
            case 2:
                return C0006R$drawable.stat_sys_bt_battery_2;
            case 3:
                return C0006R$drawable.stat_sys_bt_battery_3;
            case 4:
                return C0006R$drawable.stat_sys_bt_battery_4;
            case 5:
                return C0006R$drawable.stat_sys_bt_battery_5;
            case 6:
                return C0006R$drawable.stat_sys_bt_battery_6;
            case 7:
                return C0006R$drawable.stat_sys_bt_battery_7;
            case 8:
                return C0006R$drawable.stat_sys_bt_battery_8;
            case 9:
                return C0006R$drawable.stat_sys_bt_battery_9;
            case 10:
                return C0006R$drawable.stat_sys_bt_battery_10;
            default:
                return 0;
        }
    }

    @Override // com.oneplus.opzenmode.OpZenModeController.Callback
    public void onThreeKeyStatus(int i) {
        Log.i("OpPhoneStatusBarPolicy", " onThreeKeyStatus");
        getHandler().post(new Runnable() { // from class: com.oneplus.systemui.statusbar.phone.OpPhoneStatusBarPolicy.4
            @Override // java.lang.Runnable
            public void run() {
                OpPhoneStatusBarPolicy.this.updateVolumeZen();
            }
        });
    }

    @Override // com.oneplus.opzenmode.OpZenModeController.Callback
    public void onDndChanged(boolean z) {
        getHandler().post(new Runnable() { // from class: com.oneplus.systemui.statusbar.phone.OpPhoneStatusBarPolicy.5
            @Override // java.lang.Runnable
            public void run() {
                OpPhoneStatusBarPolicy.this.updateVolumeZen();
            }
        });
    }

    /* access modifiers changed from: protected */
    public void sendHeadsetNotify() {
        if (this.mNotificationManager != null && !this.mIsNotifyShown) {
            Notification.Builder contentText = new Notification.Builder(this.mContext, NotificationChannels.ALERTS).setSmallIcon(C0006R$drawable.ic_earphone).setContentTitle(this.mContext.getString(C0015R$string.non_op_earphone_notification_title)).setContentText(this.mContext.getString(C0015R$string.non_op_earphone_notification_content));
            Intent intent = new Intent("android.oneplus.EARPHONE_MODE_SETTINGS");
            intent.putExtra("earmode_from_notify", true);
            contentText.setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, intent, 0, null, UserHandle.CURRENT));
            SystemUI.overrideNotificationAppName(this.mContext, contentText, true);
            this.mNotificationManager.notifyAsUser("headset", 54088, contentText.build(), UserHandle.ALL);
            this.mIsNotifyShown = true;
            this.mContext.getSharedPreferences("pref_name_notify_shown", 0).edit().putBoolean("pref_key_notify_shown", true).commit();
        }
    }

    /* access modifiers changed from: protected */
    public void cancelHeadsetNotify() {
        NotificationManager notificationManager = this.mNotificationManager;
        if (notificationManager != null) {
            notificationManager.cancelAsUser("headset", 54088, UserHandle.ALL);
        }
    }

    private final class SettingObserver extends ContentObserver {
        public SettingObserver() {
            super(new Handler());
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            super.onChange(z, uri);
            OpPhoneStatusBarPolicy.this.updateVolumeZen();
        }
    }

    private Handler getHandler() {
        return (Handler) OpReflectionUtils.getValue(PhoneStatusBarPolicy.class, this, "mHandler");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateVolumeZen() {
        OpReflectionUtils.methodInvokeVoid(PhoneStatusBarPolicy.class, this, "updateVolumeZen", new Object[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getSlotBluetooth() {
        String str = (String) OpReflectionUtils.getValue(PhoneStatusBarPolicy.class, this, "mSlotBluetooth");
        if (str != null) {
            return str;
        }
        Log.d("OpPhoneStatusBarPolicy", "getSlotBluetooth fail");
        return this.mContext.getString(17041303);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBluetooth() {
        OpReflectionUtils.methodInvokeVoid(PhoneStatusBarPolicy.class, this, "updateBluetooth", new Object[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateHeadsetPlug(Intent intent) {
        OpReflectionUtils.methodInvokeWithArgs(this, OpReflectionUtils.getMethodWithParams(PhoneStatusBarPolicy.class, "updateHeadsetPlug", Intent.class), intent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFemtocell(boolean z) {
        this.mIconController.setIcon(this.mSlotFemtoCell, C0006R$drawable.stat_sys_femtocell, null);
        if (z) {
            this.mIconController.setIconVisibility(this.mSlotFemtoCell, true);
        } else {
            this.mIconController.setIconVisibility(this.mSlotFemtoCell, false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateVice(boolean z) {
        this.mIconController.setIcon(this.mSlotVice, C0006R$drawable.stat_sys_vice_call_pull, null);
        if (z) {
            this.mIconController.setIconVisibility(this.mSlotVice, true);
        } else {
            this.mIconController.setIconVisibility(this.mSlotVice, false);
        }
    }
}
