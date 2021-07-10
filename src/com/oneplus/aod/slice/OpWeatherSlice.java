package com.oneplus.aod.slice;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.Dependency;
import com.oneplus.aod.slice.OpSliceManager;
import com.oneplus.util.OpUtils;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
public class OpWeatherSlice extends OpSlice {
    private static final Uri WEATHER_CONTENT_URI = Uri.parse("content://com.oneplus.weather.ContentProvider/data");
    private LocalDateTime mActiveStart;
    private Context mContext;
    private boolean mFirstQueryInfo = false;
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor = null;
    private WeatherObserver mObserver;
    private boolean mReady = false;
    private int mState = 0;
    private LocalDateTime mUserActiveTime;

    /* access modifiers changed from: private */
    public enum WeatherColumns {
        TIMESTAMP(0),
        WEATHER_CODE(2),
        WEATHER_NAME(6),
        TEMP(3),
        TEMP_HIGH(4),
        TEMP_LOW(5);
        
        private int index;

        private WeatherColumns(int i) {
            this.index = i;
        }
    }

    /* access modifiers changed from: private */
    public enum WeatherType {
        SUNNY(1001, C0006R$drawable.op_ic_weather_sunny),
        SUNNY_INTERVALS(1002, C0006R$drawable.op_ic_weather_sunny),
        CLOUDY(1003, C0006R$drawable.op_ic_weather_cloudy),
        OVERCAST(1004, C0006R$drawable.op_ic_weather_overcast),
        DRIZZLE(1005, C0006R$drawable.op_ic_weather_rain),
        RAIN(1006, C0006R$drawable.op_ic_weather_rain),
        SHOWER(1007, C0006R$drawable.op_ic_weather_rain),
        DOWNPOUR(1008, C0006R$drawable.op_ic_weather_rain),
        RAINSTORM(1009, C0006R$drawable.op_ic_weather_rain),
        SLEET(1010, C0006R$drawable.op_ic_weather_sleet),
        FLURRY(1011, C0006R$drawable.op_ic_weather_snow),
        SNOW(1012, C0006R$drawable.op_ic_weather_snow),
        SNOWSTORM(1013, C0006R$drawable.op_ic_weather_snow),
        HAIL(1014, C0006R$drawable.op_ic_weather_hail),
        THUNDERSHOWER(1015, C0006R$drawable.op_ic_weather_rain),
        SANDSTORM(1016, C0006R$drawable.op_ic_weather_sandstorm),
        FOG(1017, C0006R$drawable.op_ic_weather_fog),
        HURRICANE(1018, C0006R$drawable.op_ic_weather_typhoon),
        HAZE(1019, C0006R$drawable.op_ic_weather_haze),
        NONE(9999, 0);
        
        int iconId;
        int weatherCode;

        private WeatherType(int i, int i2) {
            this.weatherCode = i;
            this.iconId = i2;
        }

        public static WeatherType getWeather(int i) {
            WeatherType[] values = values();
            for (WeatherType weatherType : values) {
                if (weatherType.weatherCode == i) {
                    return weatherType;
                }
            }
            return NONE;
        }

        @Override // java.lang.Enum, java.lang.Object
        public String toString() {
            return String.valueOf(this.weatherCode);
        }
    }

    public OpWeatherSlice(Context context, OpSliceManager.Callback callback) {
        super(callback);
        this.mContext = context;
        this.mKeyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.slice.OpSlice
    public void handleSetEnabled(boolean z) {
        if (z) {
            if (this.mObserver == null) {
                this.mObserver = new WeatherObserver();
                try {
                    this.mContext.getContentResolver().registerContentObserver(WEATHER_CONTENT_URI, true, this.mObserver);
                } catch (SecurityException e) {
                    String str = this.mTag;
                    Log.d(str, "Register observer fail: " + WEATHER_CONTENT_URI, e);
                    this.mObserver = null;
                }
            }
            queryWeatherInfo();
        } else if (this.mObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
            this.mObserver = null;
            this.mFirstQueryInfo = false;
        }
        refreshActive();
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.slice.OpSlice
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        String str = this.mTag;
        Log.d(str, "handleSetListening listening=" + z + " mFirstQueryInfo=" + this.mFirstQueryInfo + " mReady=" + this.mReady);
        if (!this.mKeyguardUpdateMonitor.isUserUnlocked() || !z) {
            onUserActive();
            return;
        }
        refreshState();
        if (!this.mFirstQueryInfo) {
            queryWeatherInfo();
            this.mFirstQueryInfo = true;
        }
    }

    public void refreshState() {
        Context context = this.mContext;
        String string = context.getSharedPreferences(context.getPackageName(), 0).getString("pref_name_sleep_end", null);
        if (string != null) {
            try {
                this.mActiveStart = LocalDateTime.parse(string).plusMinutes(15);
            } catch (DateTimeParseException e) {
                String str = this.mTag;
                Log.e(str, "Parse sleep end time fail: e" + e);
            }
        }
        Context context2 = this.mContext;
        String string2 = context2.getSharedPreferences(context2.getPackageName(), 0).getString("pref_name_initiative_pulse", null);
        if (TextUtils.isEmpty(string2)) {
            this.mUserActiveTime = null;
        } else {
            try {
                this.mUserActiveTime = LocalDateTime.parse(string2);
            } catch (DateTimeParseException e2) {
                String str2 = this.mTag;
                Log.e(str2, "Parse sleep end time fail: e" + e2);
            }
        }
        refreshActive();
        if (OpSlice.DEBUG) {
            String str3 = this.mTag;
            Log.d(str3, "time from sp=" + string2 + " mActiveStart=" + this.mActiveStart + " mUserActiveTime=" + this.mUserActiveTime + " now=" + LocalDateTime.now());
        }
    }

    private void refreshActive() {
        if (!isEnabled() || !this.mReady || this.mUserActiveTime == null || !LocalDateTime.now().isBefore(this.mUserActiveTime.plusMinutes(60))) {
            setActive(false);
        } else {
            setActive(true);
        }
        updateUI();
    }

    @Override // com.oneplus.aod.slice.OpSlice
    public void handleTimeChanged() {
        super.handleTimeChanged();
        if (!this.mFirstQueryInfo && !this.mReady && !isActive() && this.mKeyguardUpdateMonitor.isUserUnlocked()) {
            refreshState();
            queryWeatherInfo();
            this.mFirstQueryInfo = true;
            Log.i(this.mTag, "query weather info");
        }
    }

    private class WeatherObserver extends ContentObserver {
        public WeatherObserver() {
            super(OpWeatherSlice.this.mHandler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            super.onChange(z);
            String str = OpWeatherSlice.this.mTag;
            Log.d(str, "weather info onChange query mState=" + OpWeatherSlice.this.mState);
            OpWeatherSlice.this.queryWeatherInfo();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void queryWeatherInfo() {
        new Thread(new Runnable() { // from class: com.oneplus.aod.slice.OpWeatherSlice.1
            @Override // java.lang.Runnable
            public void run() {
                String str = OpWeatherSlice.this.mTag;
                Log.d(str, "run queryWeatherInfo mState=" + OpWeatherSlice.this.mState);
                if (OpWeatherSlice.this.mState != 0) {
                    String str2 = OpWeatherSlice.this.mTag;
                    Log.d(str2, "skip mRunnable mState=" + OpWeatherSlice.this.mState);
                } else if (!OpUtils.isPackageInstalled(OpWeatherSlice.this.mContext, "net.oneplus.weather")) {
                    OpWeatherSlice.this.mReady = false;
                    Log.d(OpWeatherSlice.this.mTag, "Query weather info fail: app is not installed");
                } else {
                    OpWeatherSlice.this.mState = 1;
                    ExecutorService newSingleThreadExecutor = Executors.newSingleThreadExecutor();
                    Future<?> submit = newSingleThreadExecutor.submit(new Runnable() { // from class: com.oneplus.aod.slice.OpWeatherSlice.1.1
                        @Override // java.lang.Runnable
                        public void run() {
                            Cursor query = OpWeatherSlice.this.mContext.getContentResolver().query(OpWeatherSlice.WEATHER_CONTENT_URI, null, null, null, null);
                            OpWeatherSlice.this.mState = 0;
                            OpWeatherSlice.this.processWeatherInfo(query);
                        }
                    });
                    try {
                        submit.get(3, TimeUnit.SECONDS);
                    } catch (TimeoutException unused) {
                        submit.cancel(true);
                        newSingleThreadExecutor.shutdownNow();
                        OpWeatherSlice.this.mState = 0;
                        Log.d(OpWeatherSlice.this.mTag, "Query weather info timeout: 3 seconds");
                    } catch (InterruptedException | ExecutionException e) {
                        OpWeatherSlice.this.mState = 0;
                        String str3 = OpWeatherSlice.this.mTag;
                        Log.e(str3, "Query weather info fail: " + e);
                    }
                }
            }
        }).start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processWeatherInfo(Cursor cursor) {
        if (cursor == null) {
            Log.d(this.mTag, "Query weather info fail: cursor is null");
        } else if (!cursor.moveToFirst()) {
            Log.d(this.mTag, "Query weather info fail: cannot move to first");
            cursor.close();
        } else if (cursor.getColumnCount() < WeatherColumns.values().length) {
            Log.d(this.mTag, "Column count is not met the spec, need to check with OPWeather");
            String str = this.mTag;
            Log.d(str, "expected columns: " + WeatherColumns.values().length + ", actual columns: " + cursor.getColumnCount());
            cursor.close();
        } else {
            try {
                String string = cursor.getString(WeatherColumns.WEATHER_CODE.index);
                String string2 = cursor.getString(WeatherColumns.WEATHER_NAME.index);
                String string3 = cursor.getString(WeatherColumns.TEMP.index);
                String string4 = cursor.getString(WeatherColumns.TEMP_HIGH.index);
                String string5 = cursor.getString(WeatherColumns.TEMP_LOW.index);
                Log.d(this.mTag, "weatherCode: " + string + " weatherName: " + string2 + " temperature: " + string3 + " temperatureHigh: " + string4 + " temperatureLow: " + string5);
                WeatherType weather = WeatherType.getWeather(Integer.parseInt(string));
                if (weather.weatherCode != 9999) {
                    this.mIcon = weather.iconId;
                    this.mPrimary = string2 + " " + string3 + "˚";
                    this.mSecondary = string4 + "˚/ " + string5 + "˚";
                    if (OpSlice.DEBUG) {
                        String str2 = this.mTag;
                        Log.i(str2, "processWeatherInfo: primary = " + this.mPrimary + ", secondary = " + this.mSecondary);
                    }
                    this.mReady = true;
                }
            } catch (IllegalStateException e) {
                String str3 = this.mTag;
                Log.e(str3, "invalid cursor data: " + e);
            } catch (NullPointerException | NumberFormatException e2) {
                String str4 = this.mTag;
                Log.e(str4, "unexpected weather data: " + e2);
            } catch (Throwable th) {
                cursor.close();
                throw th;
            }
            cursor.close();
            refreshActive();
        }
    }

    public void onUserActive() {
        if (this.mUserActiveTime == null && this.mActiveStart != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(this.mActiveStart)) {
                this.mUserActiveTime = now;
                Context context = this.mContext;
                context.getSharedPreferences(context.getPackageName(), 0).edit().putString("pref_name_initiative_pulse", now.toString()).apply();
                String str = this.mTag;
                Log.d(str, "save user initiative pulse time: " + now);
            }
        }
        refreshActive();
    }

    @Override // com.oneplus.aod.slice.OpSlice
    public void dump(PrintWriter printWriter) {
        super.dump(printWriter);
        printWriter.print("  mUserActiveTime=");
        printWriter.print(this.mUserActiveTime);
        printWriter.print(" mActiveStart=");
        printWriter.println(this.mActiveStart);
        printWriter.print(" now=");
        printWriter.println(LocalDateTime.now());
    }
}
