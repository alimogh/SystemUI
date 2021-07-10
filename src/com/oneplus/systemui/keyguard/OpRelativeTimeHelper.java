package com.oneplus.systemui.keyguard;

import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
public class OpRelativeTimeHelper {
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    private long mBeginTime;
    private Context mContext;

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0042  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x004d  */
    /* JADX WARNING: Removed duplicated region for block: B:20:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public OpRelativeTimeHelper(android.content.Context r12) {
        /*
            r11 = this;
            r11.<init>()
            r11.mContext = r12
            boolean r12 = r11.isDeviceBoot()
            java.lang.String r0 = "OpRelativeTimeHelper"
            if (r12 == 0) goto L_0x008a
            long r1 = r11.getLastElapsedRealtime()
            java.lang.String r12 = r11.getLastActiveTime()
            java.util.Date r3 = new java.util.Date
            r3.<init>()
            boolean r4 = android.text.TextUtils.isEmpty(r12)
            r5 = 0
            if (r4 != 0) goto L_0x003d
            java.text.SimpleDateFormat r4 = com.oneplus.systemui.keyguard.OpRelativeTimeHelper.TIMESTAMP_FORMAT     // Catch:{ ParseException -> 0x0037 }
            java.util.Date r4 = r4.parse(r12)     // Catch:{ ParseException -> 0x0037 }
            long r7 = r3.getTime()     // Catch:{ ParseException -> 0x0037 }
            long r9 = android.os.SystemClock.elapsedRealtime()     // Catch:{ ParseException -> 0x0037 }
            long r7 = r7 - r9
            long r9 = r4.getTime()     // Catch:{ ParseException -> 0x0037 }
            long r7 = r7 - r9
            goto L_0x003e
        L_0x0037:
            r4 = move-exception
            java.lang.String r7 = "lastActiveTime parse error"
            android.util.Log.d(r0, r7, r4)
        L_0x003d:
            r7 = r5
        L_0x003e:
            int r4 = (r7 > r5 ? 1 : (r7 == r5 ? 0 : -1))
            if (r4 <= 0) goto L_0x0043
            r5 = r7
        L_0x0043:
            long r5 = r5 + r1
            r11.mBeginTime = r5
            r11.recordBeginTime()
            boolean r4 = android.os.Build.DEBUG_ONEPLUS
            if (r4 == 0) goto L_0x00aa
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "Device has just boot. lastElapsedRealtime= "
            r4.append(r5)
            r4.append(r1)
            java.lang.String r1 = ", lastActiveTime= "
            r4.append(r1)
            r4.append(r12)
            java.lang.String r12 = ", currentTime= "
            r4.append(r12)
            java.text.SimpleDateFormat r12 = com.oneplus.systemui.keyguard.OpRelativeTimeHelper.TIMESTAMP_FORMAT
            java.lang.String r12 = r12.format(r3)
            r4.append(r12)
            java.lang.String r12 = ", deltaTime= "
            r4.append(r12)
            r4.append(r7)
            java.lang.String r12 = ", beginTime= "
            r4.append(r12)
            long r11 = r11.mBeginTime
            r4.append(r11)
            java.lang.String r11 = r4.toString()
            android.util.Log.d(r0, r11)
            goto L_0x00aa
        L_0x008a:
            long r1 = r11.getBeginTimeFromFile()
            r11.mBeginTime = r1
            boolean r12 = android.os.Build.DEBUG_ONEPLUS
            if (r12 == 0) goto L_0x00aa
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            java.lang.String r1 = "maybe crash happened. beginTime= "
            r12.append(r1)
            long r1 = r11.mBeginTime
            r12.append(r1)
            java.lang.String r11 = r12.toString()
            android.util.Log.d(r0, r11)
        L_0x00aa:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.systemui.keyguard.OpRelativeTimeHelper.<init>(android.content.Context):void");
    }

    public long getBeginTime() {
        return this.mBeginTime;
    }

    private boolean isDeviceBoot() {
        int i = -1;
        int i2 = this.mContext.getSharedPreferences("unlockcounter_prefs", 0).getInt("boot_count", -1);
        try {
            i = Settings.Global.getInt(this.mContext.getContentResolver(), "boot_count");
        } catch (Settings.SettingNotFoundException unused) {
            Log.e("OpRelativeTimeHelper", "Failed to read system boot count from Settings.Global.BOOT_COUNT");
        }
        if (i <= i2) {
            return false;
        }
        this.mContext.getSharedPreferences("unlockcounter_prefs", 0).edit().putInt("boot_count", i).apply();
        return true;
    }

    private String getLastActiveTime() {
        return this.mContext.getSharedPreferences("unlockcounter_prefs", 0).getString("last_active_time", null);
    }

    private long getLastElapsedRealtime() {
        return this.mContext.getSharedPreferences("unlockcounter_prefs", 0).getLong("last_elapsed_real_time", 0);
    }

    private long getBeginTimeFromFile() {
        return this.mContext.getSharedPreferences("unlockcounter_prefs", 0).getLong("begin_time", 0);
    }

    private void recordBeginTime() {
        this.mContext.getSharedPreferences("unlockcounter_prefs", 0).edit().putLong("begin_time", this.mBeginTime).apply();
    }

    public void recordLastElapsedRealtime() {
        this.mContext.getSharedPreferences("unlockcounter_prefs", 0).edit().putLong("last_elapsed_real_time", this.mBeginTime + SystemClock.elapsedRealtime()).apply();
    }

    public void recordLastActiveTime() {
        this.mContext.getSharedPreferences("unlockcounter_prefs", 0).edit().putString("last_active_time", TIMESTAMP_FORMAT.format(new Date())).apply();
    }
}
