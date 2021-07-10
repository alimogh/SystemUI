package com.oneplus.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.Dependency;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import com.oneplus.systemui.util.OpMdmLogger;
import java.util.Calendar;
public class ScheduleWorker extends BroadcastReceiver {
    private static boolean mIsDebugModeOn;
    private static final String[] mNeedToCheckEverShowClockArray = {"insight", "bitmoji"};
    private int mAlwaysOnState = 0;
    public Context mContext;
    private int mLastRecordAlwaysOnState = 0;

    static {
        boolean z = false;
        if (SystemProperties.getInt("persist.cust.opsysui.schedule", 0) == 1) {
            z = true;
        }
        mIsDebugModeOn = z;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        if (intent != null) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                setSchedule();
            } else if ("action.trigger.record.mdm".equals(intent.getAction())) {
                doAction();
            }
        }
    }

    private PendingIntent getPendingIntentBroadcast(int i) {
        Intent intent = new Intent(this.mContext, ScheduleWorker.class);
        intent.setAction("action.trigger.record.mdm");
        return PendingIntent.getBroadcast(this.mContext, i, intent, 134217728);
    }

    private void doAction() {
        this.mAlwaysOnState = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "always_on_state", 0, -2);
        this.mLastRecordAlwaysOnState = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "opsysui_last_record_always_on_state", 0, -2);
        Log.i("ScheduleWorker", "doAction, mAlwaysOnState:" + this.mAlwaysOnState + ", mLastRecordAlwaysOnState:" + this.mLastRecordAlwaysOnState);
        OpMdmLogger.log("always_on_new", "always_on_amount", intToString(this.mAlwaysOnState));
        if (200 == Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "opsysui_ever_used_always_on", 100, -2)) {
            OpMdmLogger.log("always_on_new", "always_on_amount", intToString(200));
        } else if (this.mLastRecordAlwaysOnState == 0 || this.mAlwaysOnState == 0) {
            OpMdmLogger.log("always_on_new", "always_on_amount", intToString(100));
        } else {
            Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "opsysui_ever_used_always_on", 200, -2);
            OpMdmLogger.log("always_on_new", "always_on_amount", intToString(200));
        }
        Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "opsysui_last_record_always_on_state", this.mAlwaysOnState, -2);
        String[] strArr = mNeedToCheckEverShowClockArray;
        for (String str : strArr) {
            int intForUser = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "showinfo_ever_show_".concat(str), 0, -2);
            if (intForUser == 0) {
                OpMdmLogger.log(str, str.concat("_amount"), intToString(0));
            } else if (intForUser == 1) {
                OpMdmLogger.log(str, str.concat("_amount"), intToString(1));
            }
            Log.i("ScheduleWorker", "element:" + str + ", everShowInfo:" + intForUser);
        }
        try {
            ((OpBitmojiManager) Dependency.get(OpBitmojiManager.class)).uploadMdm();
        } catch (Exception unused) {
        }
    }

    private void setSchedule() {
        AlarmManager alarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        Calendar instance = Calendar.getInstance();
        instance.set(11, 15);
        instance.set(12, 30);
        instance.set(13, 0);
        if (mIsDebugModeOn) {
            instance.set(11, SystemProperties.getInt("persist.cust.opsysui.schedule.hour", 15));
            instance.set(12, SystemProperties.getInt("persist.cust.opsysui.schedule.minute", 30));
            instance.set(13, SystemProperties.getInt("persist.cust.opsysui.schedule.second", 0));
        }
        alarmManager.setRepeating(0, instance.getTimeInMillis(), 86400000, getPendingIntentBroadcast(0));
    }

    private String intToString(int i) {
        return String.valueOf(i);
    }
}
