package com.oneplus.aod.utils.bitmoji.triggers;

import android.app.AlarmManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.IndentingPrintWriter;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import com.oneplus.aod.utils.bitmoji.triggers.WeatherTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.base.Trigger;
import java.io.FileDescriptor;
import java.util.concurrent.TimeUnit;
public class WeatherTrigger extends CategoryTrigger implements AlarmManager.OnAlarmListener {
    private static final Uri WEATHER_INFO_CONTENT_URI = Uri.parse("content://com.oneplus.weather.WeatherInfoProvider/*");
    private AlarmManager mAlarmManager;
    private int mCurrentWeatherCode;
    private volatile boolean mDataValid;
    private int mLastWeatherCode;
    private int mNextWeatherCode;
    private long mOnAlarmTime;
    private Thread mQueryTask;
    private final KeyguardUpdateMonitorCallback mUpdateCallback = new KeyguardUpdateMonitorCallback() { // from class: com.oneplus.aod.utils.bitmoji.triggers.WeatherTrigger.2
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserUnlocked() {
            if (WeatherTrigger.this.getHandler() != null) {
                WeatherTrigger.this.getHandler().post(new Runnable() { // from class: com.oneplus.aod.utils.bitmoji.triggers.-$$Lambda$WeatherTrigger$2$Gnpnz7CZ8NvwQOazotwJVa91ksI
                    @Override // java.lang.Runnable
                    public final void run() {
                        WeatherTrigger.AnonymousClass2.lambda$onUserUnlocked$0(WeatherTrigger.this);
                    }
                });
            } else {
                Log.e(((Trigger) WeatherTrigger.this).mTag, "onUserUnlocked: handler is null");
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onDreamingStateChanged(boolean z) {
            if (WeatherTrigger.this.getHandler() == null) {
                Log.e(((Trigger) WeatherTrigger.this).mTag, "onDreamingStateChanged: handler is null");
            } else if (z) {
                WeatherTrigger.this.getHandler().post(new Runnable() { // from class: com.oneplus.aod.utils.bitmoji.triggers.-$$Lambda$WeatherTrigger$2$2acyip_WkAawhWEA3JgvLh-C6PE
                    @Override // java.lang.Runnable
                    public final void run() {
                        WeatherTrigger.AnonymousClass2.lambda$onDreamingStateChanged$1(WeatherTrigger.this);
                    }
                });
            } else if (!WeatherTrigger.this.mDataValid) {
                WeatherTrigger.this.getHandler().post(new Runnable() { // from class: com.oneplus.aod.utils.bitmoji.triggers.-$$Lambda$WeatherTrigger$2$UQyXLOIJVA1DTyOJKdEqRatBu3s
                    @Override // java.lang.Runnable
                    public final void run() {
                        WeatherTrigger.AnonymousClass2.lambda$onDreamingStateChanged$2(WeatherTrigger.this);
                    }
                });
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onTimeChanged() {
            if (WeatherTrigger.this.getHandler() != null) {
                WeatherTrigger.this.getHandler().post(new Runnable() { // from class: com.oneplus.aod.utils.bitmoji.triggers.-$$Lambda$WeatherTrigger$2$gBNjxQ5bnuzyZoW6qJGBZV50lRI
                    @Override // java.lang.Runnable
                    public final void run() {
                        WeatherTrigger.AnonymousClass2.lambda$onTimeChanged$3(WeatherTrigger.this);
                    }
                });
            }
        }
    };
    private SparseArray<WeatherInfo> mWeatherData;

    public WeatherTrigger(Context context, OpBitmojiManager opBitmojiManager) {
        super(context, opBitmojiManager);
        this.mAlarmManager = (AlarmManager) context.getSystemService(AlarmManager.class);
        this.mNextWeatherCode = 9999;
        this.mLastWeatherCode = 9999;
        this.mCurrentWeatherCode = 9999;
        this.mWeatherData = new SparseArray<>();
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public void init() {
        super.init();
        getKeyguardUpdateMonitor().registerCallback(this.mUpdateCallback);
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger
    public String[] getCategories() {
        return new String[]{"sun", "cloud", "rain", "snow"};
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger
    public String getCurrentCategory() {
        if (!isActive()) {
            return null;
        }
        switch (this.mCurrentWeatherCode) {
            case 1001:
            case 1002:
                return "sun";
            case 1003:
                return "cloud";
            case 1004:
            case 1014:
            default:
                return null;
            case 1005:
            case 1006:
            case 1007:
            case 1008:
            case 1009:
            case 1015:
                return "rain";
            case 1010:
            case 1011:
            case 1012:
            case 1013:
                return "snow";
        }
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public boolean isActive() {
        int i;
        int i2;
        int i3 = this.mCurrentWeatherCode;
        return i3 != 9999 && (i = this.mLastWeatherCode) != 9999 && (i2 = this.mNextWeatherCode) != 9999 && i3 == i && i3 == i2 && i == i2;
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public void dynamicConfig(String[] strArr) {
        if (strArr == null || strArr.length <= 0) {
            Log.w(this.mTag, "dynamicConfig: args error");
            return;
        }
        try {
            if (strArr.length == 3) {
                String str = strArr[2];
                if ("create".equals(str)) {
                    this.mDataValid = true;
                    synchronized (this.mWeatherData) {
                        this.mWeatherData.clear();
                        int currentTime = getCurrentTime();
                        for (int i = 0; i < 25; i++) {
                            WeatherInfo testCreate = WeatherInfo.testCreate(currentTime);
                            this.mWeatherData.put(testCreate.getTime(), testCreate);
                            currentTime += 3600;
                        }
                    }
                    if (getHandler() != null) {
                        getHandler().post(new Runnable() { // from class: com.oneplus.aod.utils.bitmoji.triggers.-$$Lambda$WeatherTrigger$ScUIxfleCPnrWT5W3pOHCJTa5dk
                            @Override // java.lang.Runnable
                            public final void run() {
                                WeatherTrigger.lambda$ScUIxfleCPnrWT5W3pOHCJTa5dk(WeatherTrigger.this);
                            }
                        });
                    }
                } else if ("clear".equals(str)) {
                    synchronized (this.mWeatherData) {
                        this.mWeatherData.clear();
                    }
                    this.mDataValid = false;
                }
            } else if (strArr.length == 4) {
                int parseInt = Integer.parseInt(strArr[2]);
                int parseInt2 = Integer.parseInt(strArr[3]);
                synchronized (this.mWeatherData) {
                    if (this.mWeatherData.size() > 0) {
                        WeatherInfo weatherInfo = this.mWeatherData.get(parseInt);
                        if (weatherInfo != null) {
                            weatherInfo.setWeatherCode(parseInt2);
                            if (getHandler() != null) {
                                getHandler().post(new Runnable() { // from class: com.oneplus.aod.utils.bitmoji.triggers.-$$Lambda$WeatherTrigger$ScUIxfleCPnrWT5W3pOHCJTa5dk
                                    @Override // java.lang.Runnable
                                    public final void run() {
                                        WeatherTrigger.lambda$ScUIxfleCPnrWT5W3pOHCJTa5dk(WeatherTrigger.this);
                                    }
                                });
                            }
                        } else {
                            String str2 = this.mTag;
                            Log.d(str2, "key not found " + parseInt);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(this.mTag, "dynamicConfig: occur error", e);
        }
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger, com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public void dumpDetail(FileDescriptor fileDescriptor, IndentingPrintWriter indentingPrintWriter, String[] strArr) {
        int currentTimeMillis = (int) (System.currentTimeMillis() / 1000);
        indentingPrintWriter.println("currentTime=" + currentTimeMillis);
        indentingPrintWriter.println("valid=" + this.mDataValid);
        indentingPrintWriter.println("lastWeatherCode=" + this.mLastWeatherCode);
        indentingPrintWriter.println("currentWeatherCode=" + this.mCurrentWeatherCode);
        indentingPrintWriter.println("nextWeatherCode=" + this.mNextWeatherCode);
        long j = this.mOnAlarmTime;
        int i = 0;
        if (j > 0) {
            long currentTimeMillis2 = (j - System.currentTimeMillis()) / 1000;
            indentingPrintWriter.println("nextQueryTime= after " + String.format("%02d:%02d:%02d", Long.valueOf(TimeUnit.SECONDS.toHours(currentTimeMillis2)), Long.valueOf(TimeUnit.SECONDS.toMinutes(currentTimeMillis2) % 60), Long.valueOf(TimeUnit.SECONDS.toSeconds(currentTimeMillis2) % 60)));
        } else {
            indentingPrintWriter.println("nextQueryTime= not valid");
        }
        indentingPrintWriter.println();
        indentingPrintWriter.println("list for 24 hours:");
        indentingPrintWriter.println("-----------------------------------------------");
        synchronized (this.mWeatherData) {
            if (this.mWeatherData.size() > 0) {
                indentingPrintWriter.increaseIndent();
                while (i < this.mWeatherData.size()) {
                    StringBuilder sb = new StringBuilder();
                    int i2 = i + 1;
                    sb.append(i2);
                    sb.append(": ");
                    sb.append(this.mWeatherData.valueAt(i).toString(currentTimeMillis));
                    indentingPrintWriter.println(sb.toString());
                    indentingPrintWriter.println("------------------------------------------");
                    i = i2;
                }
                indentingPrintWriter.decreaseIndent();
            }
        }
    }

    @Override // android.app.AlarmManager.OnAlarmListener
    public void onAlarm() {
        requeryWeatherData("onAlarm");
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public String getMdmLabel() {
        char c;
        String currentCategory = getCurrentCategory();
        switch (currentCategory.hashCode()) {
            case 114252:
                if (currentCategory.equals("sun")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 3492756:
                if (currentCategory.equals("rain")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 3535235:
                if (currentCategory.equals("snow")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 94756405:
                if (currentCategory.equals("cloud")) {
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
            return "weather_sun";
        }
        if (c == 1) {
            return "weather_cloud";
        }
        if (c == 2) {
            return "weather_rain";
        }
        if (c != 3) {
            return null;
        }
        return "weather_snow";
    }

    /* access modifiers changed from: public */
    private void updateCurrentWeather() {
        int currentTime = getCurrentTime();
        if (Build.DEBUG_ONEPLUS) {
            String str = this.mTag;
            Log.d(str, "updateCurrentWeather: currentTime= " + currentTime);
        }
        synchronized (this.mWeatherData) {
            WeatherInfo weatherInfo = this.mWeatherData.get(currentTime);
            if (weatherInfo != null) {
                this.mCurrentWeatherCode = weatherInfo.getWeatherCode();
                int indexOfKey = this.mWeatherData.indexOfKey(currentTime);
                if (indexOfKey == 0) {
                    this.mLastWeatherCode = this.mCurrentWeatherCode;
                } else {
                    this.mLastWeatherCode = this.mWeatherData.valueAt(indexOfKey - 1).getWeatherCode();
                }
                if (indexOfKey == this.mWeatherData.size() - 1) {
                    this.mNextWeatherCode = this.mCurrentWeatherCode;
                } else {
                    this.mNextWeatherCode = this.mWeatherData.valueAt(indexOfKey + 1).getWeatherCode();
                }
            } else {
                this.mNextWeatherCode = 9999;
                this.mLastWeatherCode = 9999;
                this.mCurrentWeatherCode = 9999;
            }
        }
    }

    private int getCurrentTime() {
        long currentTimeMillis = System.currentTimeMillis() / 1000;
        return (int) (currentTimeMillis - (currentTimeMillis % 3600));
    }

    /* access modifiers changed from: public */
    private void queryWeatherInfoIfNeeded() {
        if (Build.DEBUG_ONEPLUS) {
            String str = this.mTag;
            Log.d(str, "queryWeatherInfoIfNeeded: callers= " + Debug.getCallers(1));
        }
        if (!this.mDataValid) {
            if (this.mQueryTask != null) {
                if (Build.DEBUG_ONEPLUS) {
                    Log.d(this.mTag, "queryWeatherInfoIfNeeded: query task is still running, interrupt it");
                }
                this.mQueryTask.interrupt();
            }
            Thread thread = new Thread(new Runnable() { // from class: com.oneplus.aod.utils.bitmoji.triggers.-$$Lambda$WeatherTrigger$J7fKmbAbVjV20DXw2Fmbx8Y8fe0
                @Override // java.lang.Runnable
                public final void run() {
                    WeatherTrigger.this.lambda$queryWeatherInfoIfNeeded$0$WeatherTrigger();
                }
            });
            this.mQueryTask = thread;
            thread.start();
        } else if (Build.DEBUG_ONEPLUS) {
            Log.d(this.mTag, "data is valid, no need to requery");
        }
    }

    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00c7, code lost:
        if (android.os.Build.DEBUG_ONEPLUS != false) goto L_0x00e7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00e5, code lost:
        if (android.os.Build.DEBUG_ONEPLUS != false) goto L_0x00e7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00e7, code lost:
        android.util.Log.d(r10.mTag, "queryWeatherInfoIfNeeded: data is still invalid");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0108, code lost:
        if (android.os.Build.DEBUG_ONEPLUS != false) goto L_0x00e7;
     */
    /* renamed from: lambda$queryWeatherInfoIfNeeded$0 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private /* synthetic */ void lambda$queryWeatherInfoIfNeeded$0$WeatherTrigger() {
        /*
        // Method dump skipped, instructions count: 294
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.aod.utils.bitmoji.triggers.WeatherTrigger.lambda$queryWeatherInfoIfNeeded$0$WeatherTrigger():void");
    }

    private void startSchedule(long j) {
        cancelSchedule();
        long currentTimeMillis = j - System.currentTimeMillis();
        if (Build.DEBUG_ONEPLUS) {
            String str = this.mTag;
            Log.d(str, "startSchedule: nextQueryTime= " + j + ", delayTime= " + currentTimeMillis);
        }
        if (currentTimeMillis > 0) {
            this.mOnAlarmTime = j;
            this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + currentTimeMillis, "Bitmoji#WeatherTrigger", this, this.mBitmojiManager.getHandler());
        }
    }

    /* access modifiers changed from: public */
    private void cancelSchedule() {
        this.mAlarmManager.cancel(this);
        this.mOnAlarmTime = 0;
    }

    /* access modifiers changed from: public */
    private void onUserUnlocked() {
        queryWeatherInfoIfNeeded();
    }

    private void requeryWeatherData(String str) {
        if (Build.DEBUG_ONEPLUS) {
            String str2 = this.mTag;
            Log.d(str2, "requeryWeatherData: reason= " + str);
        }
        this.mDataValid = false;
        this.mNextWeatherCode = 9999;
        this.mLastWeatherCode = 9999;
        this.mCurrentWeatherCode = 9999;
        synchronized (this.mWeatherData) {
            this.mWeatherData.clear();
        }
        queryWeatherInfoIfNeeded();
    }

    public static class WeatherInfo {
        private int mTime;
        private int mWeatherCode;

        private WeatherInfo() {
        }

        private WeatherInfo(int i, int i2) {
            this.mWeatherCode = i;
            this.mTime = i2;
        }

        public static WeatherInfo parse(Bundle bundle) throws RuntimeException {
            String string = bundle.getString("hourly_weather_type", null);
            if (!TextUtils.isEmpty(string)) {
                int parseInt = Integer.parseInt(string);
                int i = bundle.getInt("hourly_epochDateTime", -1);
                if (i >= 0) {
                    return new WeatherInfo(parseInt, i);
                }
                throw new RuntimeException("epochTime is less than 0");
            }
            throw new RuntimeException("type must not be empty");
        }

        public static WeatherInfo testCreate(int i) {
            WeatherInfo weatherInfo = new WeatherInfo();
            weatherInfo.mTime = i;
            weatherInfo.testSetWeatherCode((int) (Math.random() * 4.0d));
            return weatherInfo;
        }

        public void setWeatherCode(int i) {
            this.mWeatherCode = i;
        }

        public int getTime() {
            return this.mTime;
        }

        public int getWeatherCode() {
            return this.mWeatherCode;
        }

        public String toString() {
            return "time=" + this.mTime + "\nweatherCode=" + getWeatherCode();
        }

        public String toString(int i) {
            int i2 = i - this.mTime;
            StringBuilder sb = new StringBuilder();
            sb.append(toString());
            sb.append((i2 < 0 || i2 >= 3600) ? "" : "\ncurrent");
            return sb.toString();
        }

        private void testSetWeatherCode(int i) {
            if (i == 0) {
                this.mWeatherCode = 1001;
            } else if (i == 1) {
                this.mWeatherCode = 1003;
            } else if (i == 2) {
                this.mWeatherCode = 1006;
            } else if (i == 3) {
                this.mWeatherCode = 1012;
            }
        }
    }
}
