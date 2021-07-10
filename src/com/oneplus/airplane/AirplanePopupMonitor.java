package com.oneplus.airplane;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.BootCompleteCache;
import com.android.systemui.BootCompleteCacheImpl;
import com.android.systemui.SystemUIFactory;
public class AirplanePopupMonitor {
    private static AirplanePopupMonitor sInstance;
    public BootCompleteCacheImpl mBootCompleteCache = null;
    BootCompleteCache.BootCompleteListener mCallback = null;
    private Context mContext;

    private AirplanePopupMonitor(Context context) {
        this.mContext = context;
    }

    public void init() {
        Log.d(AirplanePopupMonitor.class.getSimpleName(), "init()");
        if (this.mBootCompleteCache == null) {
            this.mBootCompleteCache = SystemUIFactory.getInstance().getRootComponent().provideBootCacheImpl();
        }
        this.mCallback = new BootCompleteCache.BootCompleteListener() { // from class: com.oneplus.airplane.AirplanePopupMonitor.1
            @Override // com.android.systemui.BootCompleteCache.BootCompleteListener
            public void onBootComplete() {
                Log.d(AirplanePopupMonitor.class.getSimpleName(), "onBootCompleted");
                AirplanePopupMonitor.this.unRegisterReceiver();
                if (AirplanePopupMonitor.isAirplaneModeOn(AirplanePopupMonitor.this.mContext)) {
                    Intent intent = new Intent(AirplanePopupMonitor.this.mContext, AirplanePopupActivity.class);
                    intent.addFlags(268435456);
                    AirplanePopupMonitor.this.mContext.startActivity(intent);
                }
            }
        };
        registerReceiver();
    }

    public static AirplanePopupMonitor getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AirplanePopupMonitor(context);
        }
        return sInstance;
    }

    /* access modifiers changed from: private */
    public static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0;
    }

    private void registerReceiver() {
        Log.d(AirplanePopupMonitor.class.getSimpleName(), "registering BootCompleteCache");
        BootCompleteCacheImpl bootCompleteCacheImpl = this.mBootCompleteCache;
        if (bootCompleteCacheImpl != null) {
            bootCompleteCacheImpl.addListener(this.mCallback);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unRegisterReceiver() {
        BootCompleteCache.BootCompleteListener bootCompleteListener;
        BootCompleteCacheImpl bootCompleteCacheImpl = this.mBootCompleteCache;
        if (bootCompleteCacheImpl != null && (bootCompleteListener = this.mCallback) != null) {
            bootCompleteCacheImpl.removeListener(bootCompleteListener);
            this.mCallback = null;
        }
    }
}
