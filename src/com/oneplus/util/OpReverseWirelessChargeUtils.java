package com.oneplus.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.util.OpFeatures;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0014R$raw;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import java.util.Arrays;
public class OpReverseWirelessChargeUtils {
    private static final String TAG = "OpReverseWirelessChargeUtils";
    private static AudioManager mAudioManager = null;
    private static SoundPool mDischargingDisconnectedSound = null;
    private static int mDischargingDisconnectedSoundId = 0;
    private static boolean sDischarging = false;
    private static Handler sHandler = null;
    private static SystemSetting sModeSetting = null;
    private static BroadcastReceiver sReverseWirelessReceiver = null;
    private static boolean sShow = false;

    public static void init(final Context context, Handler handler) {
        if (OpFeatures.isSupport(new int[]{237})) {
            sHandler = handler;
            BroadcastReceiver broadcastReceiver = sReverseWirelessReceiver;
            if (broadcastReceiver != null) {
                context.unregisterReceiver(broadcastReceiver);
            }
            sReverseWirelessReceiver = new BroadcastReceiver() { // from class: com.oneplus.util.OpReverseWirelessChargeUtils.1
                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context2, Intent intent) {
                    if ("com.oneplus.systemui.REVERSE_WIRELESS_CHARGING_OPEN".equals(intent.getAction())) {
                        Log.d(OpReverseWirelessChargeUtils.TAG, "ReverseWirelessCharge: open settings received.");
                        ((ActivityStarter) Dependency.get(ActivityStarter.class)).postStartActivityDismissingKeyguard(new Intent("android.settings.OP_REVERSE_WIRELESS_CHARGING_SETTINGS"), 0);
                    } else if ("com.oneplus.systemui.REVERSE_WIRELESS_CHARGING_STOP".equals(intent.getAction())) {
                        Log.d(OpReverseWirelessChargeUtils.TAG, "ReverseWirelessCharge: stop received.");
                        OpReverseWirelessChargeUtils.setEnabled(context2, false);
                        OpReverseWirelessChargeUtils.sHandler.post(new Runnable(context2) { // from class: com.oneplus.util.-$$Lambda$OpReverseWirelessChargeUtils$1$gxLYnE_xP34dSz7ZAOuhQcgSpUA
                            public final /* synthetic */ Context f$0;

                            {
                                this.f$0 = r1;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                OpReverseWirelessChargeUtils.showNotification(this.f$0, false);
                            }
                        });
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.oneplus.systemui.REVERSE_WIRELESS_CHARGING_OPEN");
            intentFilter.addAction("com.oneplus.systemui.REVERSE_WIRELESS_CHARGING_STOP");
            context.registerReceiver(sReverseWirelessReceiver, intentFilter, "android.permission.WRITE_SECURE_SETTINGS", null);
            SystemSetting systemSetting = sModeSetting;
            if (systemSetting != null) {
                systemSetting.setListening(false);
            }
            AnonymousClass2 r11 = new SystemSetting(null, "reverse_wireless_charging_status", true, context) { // from class: com.oneplus.util.OpReverseWirelessChargeUtils.2
                /* access modifiers changed from: protected */
                @Override // com.oneplus.util.SystemSetting
                public void handleValueChanged(int i, boolean z) {
                    String str = OpReverseWirelessChargeUtils.TAG;
                    Log.d(str, "mModeSettingChanged: value=" + i + ", enabled=" + OpReverseWirelessChargeUtils.isEnabled() + ", discharging=" + OpReverseWirelessChargeUtils.isDischarging());
                    OpReverseWirelessChargeUtils.sHandler.post(new Runnable(context) { // from class: com.oneplus.util.-$$Lambda$OpReverseWirelessChargeUtils$2$Gxr5JYNhjCCzZbVRg7uTqs3E_2g
                        public final /* synthetic */ Context f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            OpReverseWirelessChargeUtils.showNotification(this.f$0, OpReverseWirelessChargeUtils.isEnabled());
                        }
                    });
                }
            };
            sModeSetting = r11;
            r11.setListening(true);
            mAudioManager = (AudioManager) context.getSystemService("audio");
            SoundPool soundPool = new SoundPool(1, 1, 0);
            mDischargingDisconnectedSound = soundPool;
            mDischargingDisconnectedSoundId = soundPool.load(context, C0014R$raw.discharging_disconnect_sound, 1);
        }
    }

    public static void showNotification(Context context, boolean z) {
        boolean isDischarging = isDischarging();
        boolean z2 = (sShow == z && sDischarging == isDischarging) ? false : true;
        boolean z3 = sDischarging != isDischarging && !isDischarging;
        String str = TAG;
        Log.d(str, "showNotification: show=" + sShow + "->" + z + ", discharging=" + sDischarging + "->" + isDischarging + ", diff=" + z2);
        sShow = z;
        sDischarging = isDischarging;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NotificationManager.class);
        if (!z) {
            notificationManager.cancelAsUser(null, 1, UserHandle.CURRENT);
        } else if (z2) {
            NotificationChannel notificationChannel = new NotificationChannel("OP_REVERSE_CHARGE", context.getString(C0015R$string.op_reverse_charge_notification_title), 3);
            notificationChannel.setBlockable(true);
            if (!isDischarging) {
                notificationChannel.enableVibration(false);
                notificationChannel.setSound(null, null);
            }
            notificationManager.createNotificationChannels(Arrays.asList(notificationChannel));
            Notification.Builder channelId = new Notification.Builder(context).setSmallIcon(C0006R$drawable.op_qs_reverse_wireless_charge_icon).setContentTitle(context.getString(C0015R$string.op_reverse_charge_notification_title)).setOngoing(true).setContentIntent(PendingIntent.getBroadcast(context, 0, new Intent("com.oneplus.systemui.REVERSE_WIRELESS_CHARGING_OPEN"), 134217728)).addAction(new Notification.Action.Builder(C0006R$drawable.op_qs_reverse_wireless_charge_icon, context.getString(C0015R$string.op_reverse_charge_notification_btn), PendingIntent.getBroadcast(context, 0, new Intent("com.oneplus.systemui.REVERSE_WIRELESS_CHARGING_STOP"), 134217728)).build()).setChannelId("OP_REVERSE_CHARGE");
            if (isDischarging) {
                channelId.setContentText(context.getString(C0015R$string.op_reverse_charge_notification_discharging));
            } else {
                channelId.setContentText(context.getString(C0015R$string.op_reverse_charge_notification_subtitle));
            }
            notificationManager.notifyAsUser(null, 1, channelId.build(), UserHandle.CURRENT);
            if (z3) {
                playDischargingDisconnectedSound();
            }
        }
    }

    public static void setEnabled(Context context, boolean z) {
        Settings.System.putIntForUser(context.getContentResolver(), "reverse_wireless_charging_status", z ? 1 : 0, -2);
    }

    public static boolean isEnabled() {
        SystemSetting systemSetting = sModeSetting;
        if (systemSetting == null || systemSetting.getValue() <= 0) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public static boolean isDischarging() {
        SystemSetting systemSetting = sModeSetting;
        if (systemSetting == null || systemSetting.getValue() != 2) {
            return false;
        }
        return true;
    }

    private static void playDischargingDisconnectedSound() {
        AudioManager audioManager;
        if (mDischargingDisconnectedSound == null || (audioManager = mAudioManager) == null || mDischargingDisconnectedSoundId == 0) {
            Log.d(TAG, "OpReverseWirelessChargeUtils doesn't init yet!!");
            return;
        }
        boolean isStreamMute = audioManager.isStreamMute(2);
        String str = TAG;
        Log.d(str, "play disCharging disconnect sound, " + isStreamMute);
        if (!isStreamMute) {
            mDischargingDisconnectedSound.play(mDischargingDisconnectedSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }
}
