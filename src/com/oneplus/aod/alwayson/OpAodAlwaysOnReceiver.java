package com.oneplus.aod.alwayson;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.StatusBar;
import com.oneplus.plugin.OpLsState;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
public class OpAodAlwaysOnReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Log.i("AodAlwaysOnReceiver", "onReceive = " + intent);
        updateScheduleAlwaysOn(context, KeyguardUpdateMonitor.getCurrentUser());
    }

    public static void updateScheduleAlwaysOn(Context context, int i) {
        ContentResolver contentResolver = context.getContentResolver();
        boolean z = 1 == Settings.Secure.getIntForUser(contentResolver, "aod_use_ambient_display_enabled", 1, i);
        int intForUser = Settings.Secure.getIntForUser(contentResolver, "always_on_state", 0, i);
        boolean z2 = intForUser == 1;
        boolean z3 = intForUser == 2;
        String stringForUser = Settings.Secure.getStringForUser(contentResolver, "always_on_time_start", i);
        String stringForUser2 = Settings.Secure.getStringForUser(contentResolver, "always_on_time_end", i);
        Log.i("AodAlwaysOnReceiver", "updateScheduleAlwaysOn , aodMainSwitchEnabled:" + z + ", aodScheduleEnabled:" + z2 + ", aodScheduleAllDayOn:" + z3 + ", aodScheduleTimeStart:" + stringForUser + ", aodScheduleTimeEnd:" + stringForUser2);
        if (stringForUser == null) {
            stringForUser = "08:00";
        }
        if (stringForUser2 == null) {
            stringForUser2 = "22:00";
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        if (!z || !z2) {
            alarmManager.cancel(createAodSchedulePeningIntent(context, 0, false));
            alarmManager.cancel(createAodSchedulePeningIntent(context, 1, false));
        }
        if (z && z2 && z3) {
            alarmManager.cancel(createAodSchedulePeningIntent(context, 0, false));
            alarmManager.cancel(createAodSchedulePeningIntent(context, 1, false));
        }
        if (z && z2 && !z3 && stringForUser != null && stringForUser2 != null && stringForUser.length() != 0 && stringForUser2.length() != 0) {
            String[] split = stringForUser.split(":");
            if (split == null || split.length < 2) {
                split = stringForUser.split("\\.");
            }
            int parseInt = (Integer.parseInt(split[0]) * 100) + Integer.parseInt(split[1]);
            String[] split2 = stringForUser2.split(":");
            if (split2 == null || split2.length < 2) {
                split2 = stringForUser2.split("\\.");
            }
            int parseInt2 = (Integer.parseInt(split2[0]) * 100) + Integer.parseInt(split2[1]);
            String[] split3 = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime()).split(":");
            int parseInt3 = (Integer.parseInt(split3[0]) * 100) + Integer.parseInt(split3[1]);
            boolean z4 = (parseInt2 >= parseInt && parseInt3 >= parseInt && parseInt2 > parseInt3) || (parseInt >= parseInt2 && (parseInt3 >= parseInt || parseInt3 < parseInt2));
            boolean z5 = 1 == Settings.Secure.getIntForUser(context.getContentResolver(), "doze_always_on", 0, i);
            KeyguardUpdateMonitor updateMonitor = OpLsState.getInstance().getUpdateMonitor();
            if (updateMonitor == null) {
                Log.e("AodAlwaysOnReceiver", "can't get KeyguardUpdateMonitor object");
                return;
            }
            updateMonitor.getAodAlwaysOnController().setAlwaysOnState(z4);
            boolean isAlwaysOnEnabled = updateMonitor.isAlwaysOnEnabled();
            Log.i("AodAlwaysOnReceiver", "updateScheduleAlwaysOn, start:" + parseInt + ", end:" + parseInt2 + ", current:" + parseInt3 + ", alwaysOnEnabled:" + z4 + ", lastAlwaysOnEnabled:" + z5 + ", alwaysOnCanEnabled: " + isAlwaysOnEnabled);
            ContentResolver contentResolver2 = context.getContentResolver();
            int i2 = z4 ? 1 : 0;
            int i3 = z4 ? 1 : 0;
            int i4 = z4 ? 1 : 0;
            Settings.Secure.putIntForUser(contentResolver2, "doze_always_on", i2, i);
            if (z5 != z4) {
                ((StatusBar) Dependency.get(StatusBar.class)).onAlwaysOnEnableChanged(z4 && isAlwaysOnEnabled);
            }
            Calendar instance = Calendar.getInstance();
            instance.set(11, Integer.parseInt(split[0]));
            instance.set(12, Integer.parseInt(split[1]));
            instance.set(13, 0);
            Calendar instance2 = Calendar.getInstance();
            instance2.set(11, Integer.parseInt(split2[0]));
            instance2.set(12, Integer.parseInt(split2[1]));
            instance2.set(13, 0);
            if (System.currentTimeMillis() > instance.getTimeInMillis()) {
                instance.add(6, 1);
            }
            if (System.currentTimeMillis() > instance2.getTimeInMillis()) {
                instance2.add(6, 1);
            }
            Log.i("AodAlwaysOnReceiver", "splitTimeStartString:." + split[0] + " / " + split[1] + ", splitTimeEndString:" + split2[0] + " / " + split2[1] + ", calendarStart.getTimeInMillis():" + instance.getTimeInMillis() + ", Date:" + new Date(instance.getTimeInMillis()) + ", calendarEnd.getTimeInMillis():" + instance2.getTimeInMillis() + ", Date:" + new Date(instance2.getTimeInMillis()));
            alarmManager.cancel(createAodSchedulePeningIntent(context, 0, z4));
            alarmManager.cancel(createAodSchedulePeningIntent(context, 1, z4));
            if (System.currentTimeMillis() < instance.getTimeInMillis()) {
                alarmManager.setExactAndAllowWhileIdle(0, instance.getTimeInMillis(), createAodSchedulePeningIntent(context, 0, z4));
            }
            if (System.currentTimeMillis() < instance2.getTimeInMillis()) {
                alarmManager.setExactAndAllowWhileIdle(0, instance2.getTimeInMillis(), createAodSchedulePeningIntent(context, 1, z4));
            }
        }
    }

    private static PendingIntent createAodSchedulePeningIntent(Context context, int i, boolean z) {
        Intent intent = new Intent("com.oneplus.intent.action.aod.schedule");
        intent.addFlags(16777216);
        intent.putExtra("alwaysOnEnabled", z);
        intent.putExtra("id", i);
        return PendingIntent.getBroadcast(context, i, intent, 0);
    }
}
