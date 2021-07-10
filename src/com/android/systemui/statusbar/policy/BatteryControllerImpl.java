package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.util.OpFeatures;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.fuelgauge.BatterySaverUtils;
import com.android.settingslib.fuelgauge.Estimate;
import com.android.settingslib.utils.PowerUtil;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.power.EnhancedEstimates;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.util.ProductUtils;
import com.oneplus.util.OpUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
public class BatteryControllerImpl extends BroadcastReceiver implements BatteryController {
    private static final boolean DEBUG = Log.isLoggable("BatteryController", 3);
    private boolean mAodPowerSave;
    private int mBatteryStyle = 0;
    private final Handler mBgHandler;
    private final BroadcastDispatcher mBroadcastDispatcher;
    protected final ArrayList<BatteryController.BatteryStateChangeCallback> mChangeCallbacks = new ArrayList<>();
    private boolean mCharged;
    protected boolean mCharging;
    protected final Context mContext;
    private boolean mDemoMode;
    private Estimate mEstimate;
    private final EnhancedEstimates mEstimates;
    private int mFastchargeType = 0;
    private final ArrayList<BatteryController.EstimateFetchCompletion> mFetchCallbacks = new ArrayList<>();
    private boolean mFetchingEstimate = false;
    @VisibleForTesting
    boolean mHasReceivedBattery = false;
    private int mInvalidCharger = 0;
    private boolean mIsOptimizatedCharge = false;
    protected int mLevel;
    private final Handler mMainHandler;
    protected boolean mPluggedIn;
    private final PowerManager mPowerManager;
    protected boolean mPowerSave;
    protected boolean mSWarpCharging = false;
    protected long mSWarpDuration;
    protected float mSWarpLevel;
    protected float mSWarpLevelNext;
    private final SettingObserver mSettingObserver = new SettingObserver();
    private boolean mShowPercent = false;
    private boolean mTestmode = false;
    protected boolean mWirelessCharging = false;
    protected boolean mWirelessWarpCharging = false;

    @Override // com.android.systemui.statusbar.policy.BatteryController
    public boolean isFastCharging(int i) {
        return i == 1;
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController
    public boolean isWarpCharging(int i) {
        return i == 2 || i == 3 || i == 4;
    }

    @VisibleForTesting
    public BatteryControllerImpl(Context context, EnhancedEstimates enhancedEstimates, PowerManager powerManager, BroadcastDispatcher broadcastDispatcher, Handler handler, Handler handler2) {
        this.mContext = context;
        this.mMainHandler = handler;
        this.mBgHandler = handler2;
        this.mPowerManager = powerManager;
        this.mEstimates = enhancedEstimates;
        this.mBroadcastDispatcher = broadcastDispatcher;
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.os.action.POWER_SAVE_MODE_CHANGED");
        intentFilter.addAction("com.android.systemui.BATTERY_LEVEL_TEST");
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        intentFilter.addAction("android.intent.action.BATTERY_LEVEL_DECIMAL");
        Log.d("BatteryController", "registerReceiver");
        this.mBroadcastDispatcher.registerReceiver(this, intentFilter);
        this.mSettingObserver.observe();
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController
    public void init() {
        Intent registerReceiver;
        registerReceiver();
        if (!this.mHasReceivedBattery && (registerReceiver = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"))) != null && !this.mHasReceivedBattery) {
            onReceive(this.mContext, registerReceiver);
        }
        updatePowerSave();
        updateEstimate();
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("BatteryController state:");
        printWriter.print("  mLevel=");
        printWriter.println(this.mLevel);
        printWriter.print("  mPluggedIn=");
        printWriter.println(this.mPluggedIn);
        printWriter.print("  mCharging=");
        printWriter.println(this.mCharging);
        printWriter.print("  mCharged=");
        printWriter.println(this.mCharged);
        printWriter.print("  mPowerSave=");
        printWriter.println(this.mPowerSave);
        printWriter.print("  mShowPercent=");
        printWriter.println(this.mShowPercent);
        printWriter.print("  mBatteryStyle=");
        printWriter.println(this.mBatteryStyle);
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController
    public void setPowerSaveMode(boolean z) {
        Log.d("BatteryController", "setPowerSaveMode to " + z);
        BatterySaverUtils.setPowerSaveMode(this.mContext, z, true);
    }

    public void addCallback(BatteryController.BatteryStateChangeCallback batteryStateChangeCallback) {
        synchronized (this.mChangeCallbacks) {
            this.mChangeCallbacks.add(batteryStateChangeCallback);
        }
        batteryStateChangeCallback.onOptimizatedStatusChange(this.mIsOptimizatedCharge);
        if (ProductUtils.isUsvMode()) {
            batteryStateChangeCallback.onInvalidChargeChanged(this.mInvalidCharger);
        }
        if (this.mHasReceivedBattery) {
            batteryStateChangeCallback.onBatteryLevelChanged(this.mLevel, this.mPluggedIn, this.mCharging);
            batteryStateChangeCallback.onPowerSaveChanged(this.mPowerSave);
            batteryStateChangeCallback.onFastChargeChanged(this.mFastchargeType);
            batteryStateChangeCallback.onBatteryStyleChanged(this.mBatteryStyle);
            batteryStateChangeCallback.onBatteryPercentShowChange(this.mShowPercent);
            if (OpFeatures.isSupport(new int[]{237})) {
                batteryStateChangeCallback.onWirelessWarpChargeChanged(isWirelessWarpCharging());
            }
        }
    }

    public void removeCallback(BatteryController.BatteryStateChangeCallback batteryStateChangeCallback) {
        synchronized (this.mChangeCallbacks) {
            this.mChangeCallbacks.remove(batteryStateChangeCallback);
        }
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        int i = 1;
        if (action.equals("android.intent.action.BATTERY_CHANGED")) {
            if (!this.mTestmode || intent.getBooleanExtra("testmode", false)) {
                boolean z = !this.mHasReceivedBattery;
                this.mHasReceivedBattery = true;
                this.mLevel = (int) ((((float) intent.getIntExtra("level", 0)) * 100.0f) / ((float) intent.getIntExtra("scale", 100)));
                this.mPluggedIn = intent.getIntExtra("plugged", 0) != 0;
                int intExtra = intent.getIntExtra("status", 1);
                boolean z2 = intExtra == 5;
                this.mCharged = z2;
                this.mCharging = z2 || intExtra == 2;
                int intExtra2 = intent.getIntExtra("fastcharge_status", 0);
                long longExtra = intent.getLongExtra("estimate_time_to_full", 0);
                if (SystemProperties.getBoolean("persist.test.swarp", false)) {
                    this.mSWarpLevel = ((float) SystemProperties.getInt("persist.test.swarp.start", 60)) + 0.98f;
                    this.mSWarpLevelNext = ((float) SystemProperties.getInt("persist.test.swarp.next", 61)) + 0.08f;
                    this.mSWarpDuration = 6000;
                }
                int i2 = 4;
                if (OpUtils.SUPPORT_SWARP_CHARGING) {
                    this.mSWarpCharging = intExtra2 == 4;
                    Log.d("BatteryController", "estimate_time_to_full:" + longExtra + " isSwarp:" + this.mSWarpCharging);
                }
                if (OpFeatures.isSupport(new int[]{237})) {
                    this.mWirelessWarpCharging = intent.getBooleanExtra("wireless_fastcharge_type", false);
                    this.mWirelessCharging = intent.getBooleanExtra("wireless_status", false);
                    Log.d("BatteryController", "mWirelessWarpCharging " + this.mWirelessWarpCharging + " mWirelessCharging " + this.mWirelessCharging);
                }
                if (OpUtils.SUPPORT_WARP_CHARGING) {
                    i = intExtra2;
                } else if (intExtra2 <= 0) {
                    i = 0;
                }
                this.mFastchargeType = i;
                if (!OpUtils.SUPPORT_SWARP_CHARGING || !this.mSWarpCharging) {
                    i2 = this.mFastchargeType;
                }
                this.mFastchargeType = i2;
                this.mInvalidCharger = intent.getIntExtra("invalid_charger", 0);
                fireInvalidChargerChange();
                if (z) {
                    fireBatteryStylechange();
                }
                fireBatteryLevelChanged();
            }
        } else if (action.equals("android.os.action.POWER_SAVE_MODE_CHANGED")) {
            updatePowerSave();
        } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            this.mSettingObserver.update(null);
        } else if (action.equals("com.android.systemui.BATTERY_LEVEL_TEST")) {
            this.mTestmode = true;
            this.mMainHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.policy.BatteryControllerImpl.1
                int curLevel = 0;
                Intent dummy;
                int incr = 1;
                int saveLevel;
                boolean savePlugged;

                {
                    BatteryControllerImpl batteryControllerImpl = BatteryControllerImpl.this;
                    this.saveLevel = batteryControllerImpl.mLevel;
                    this.savePlugged = batteryControllerImpl.mPluggedIn;
                    this.dummy = new Intent("android.intent.action.BATTERY_CHANGED");
                }

                @Override // java.lang.Runnable
                public void run() {
                    int i3 = this.curLevel;
                    int i4 = 0;
                    if (i3 < 0) {
                        BatteryControllerImpl.this.mTestmode = false;
                        this.dummy.putExtra("level", this.saveLevel);
                        this.dummy.putExtra("plugged", this.savePlugged);
                        this.dummy.putExtra("testmode", false);
                    } else {
                        this.dummy.putExtra("level", i3);
                        Intent intent2 = this.dummy;
                        if (this.incr > 0) {
                            i4 = 1;
                        }
                        intent2.putExtra("plugged", i4);
                        this.dummy.putExtra("testmode", true);
                    }
                    context.sendBroadcast(this.dummy);
                    if (BatteryControllerImpl.this.mTestmode) {
                        int i5 = this.curLevel;
                        int i6 = this.incr;
                        int i7 = i5 + i6;
                        this.curLevel = i7;
                        if (i7 == 100) {
                            this.incr = i6 * -1;
                        }
                        BatteryControllerImpl.this.mMainHandler.postDelayed(this, 200);
                    }
                }
            });
        } else if (action.equals("android.intent.action.BATTERY_LEVEL_DECIMAL")) {
            float floatExtra = intent.getFloatExtra("estimate_remain", 0.0f);
            float floatExtra2 = intent.getFloatExtra("estimate_next_remain", 0.0f);
            long longExtra2 = intent.getLongExtra("estimate_time", 0);
            this.mSWarpLevel = floatExtra;
            this.mSWarpLevelNext = floatExtra2;
            this.mSWarpDuration = longExtra2;
            Log.d("BatteryController", "estimate_remain:" + floatExtra + "; estimate_next_remain:" + floatExtra2 + "; estimate_time:" + longExtra2);
            fireBatteryLevelChanged();
        }
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController
    public boolean isPowerSave() {
        return this.mPowerSave;
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController
    public boolean isAodPowerSave() {
        return this.mAodPowerSave;
    }

    public boolean isWirelessWarpCharging() {
        return this.mWirelessCharging && this.mWirelessWarpCharging;
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController
    public void getEstimatedTimeRemainingString(BatteryController.EstimateFetchCompletion estimateFetchCompletion) {
        synchronized (this.mFetchCallbacks) {
            this.mFetchCallbacks.add(estimateFetchCompletion);
        }
        updateEstimateInBackground();
    }

    private String generateTimeRemainingString() {
        synchronized (this.mFetchCallbacks) {
            if (this.mEstimate == null) {
                return null;
            }
            return PowerUtil.getBatteryRemainingShortStringFormatted(this.mContext, this.mEstimate.getEstimateMillis());
        }
    }

    private void updateEstimateInBackground() {
        if (!this.mFetchingEstimate) {
            this.mFetchingEstimate = true;
            this.mBgHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.policy.-$$Lambda$BatteryControllerImpl$Q2m5_jQFbUIrN5-x5MkihyCoos8
                @Override // java.lang.Runnable
                public final void run() {
                    BatteryControllerImpl.this.lambda$updateEstimateInBackground$0$BatteryControllerImpl();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateEstimateInBackground$0 */
    public /* synthetic */ void lambda$updateEstimateInBackground$0$BatteryControllerImpl() {
        synchronized (this.mFetchCallbacks) {
            this.mEstimate = null;
            if (this.mEstimates.isHybridNotificationEnabled()) {
                updateEstimate();
            }
        }
        this.mFetchingEstimate = false;
        this.mMainHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.policy.-$$Lambda$BatteryControllerImpl$xVvPxv9usTpbGvWx3jH4_VH1nvI
            @Override // java.lang.Runnable
            public final void run() {
                BatteryControllerImpl.this.notifyEstimateFetchCallbacks();
            }
        });
    }

    /* access modifiers changed from: private */
    public void notifyEstimateFetchCallbacks() {
        synchronized (this.mFetchCallbacks) {
            String generateTimeRemainingString = generateTimeRemainingString();
            Iterator<BatteryController.EstimateFetchCompletion> it = this.mFetchCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onBatteryRemainingEstimateRetrieved(generateTimeRemainingString);
            }
            this.mFetchCallbacks.clear();
        }
    }

    private void updateEstimate() {
        Estimate cachedEstimateIfAvailable = Estimate.getCachedEstimateIfAvailable(this.mContext);
        this.mEstimate = cachedEstimateIfAvailable;
        if (cachedEstimateIfAvailable == null) {
            Estimate estimate = this.mEstimates.getEstimate();
            this.mEstimate = estimate;
            if (estimate != null) {
                Estimate.storeCachedEstimate(this.mContext, estimate);
            }
        }
    }

    private void updatePowerSave() {
        setPowerSave(this.mPowerManager.isPowerSaveMode());
    }

    private void setPowerSave(boolean z) {
        Log.d("BatteryController", "setPowerSave: " + this.mPowerSave + " to " + z);
        if (z != this.mPowerSave) {
            this.mPowerSave = z;
            this.mAodPowerSave = this.mPowerManager.getPowerSaveState(14).batterySaverEnabled;
            if (DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("Power save is ");
                sb.append(this.mPowerSave ? "on" : "off");
                Log.d("BatteryController", sb.toString());
            }
            firePowerSaveChanged();
        }
    }

    /* access modifiers changed from: protected */
    public void fireBatteryLevelChanged() {
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.i("BatteryController", " fireBatteryLevelChanged mLevel:" + this.mLevel + " PluggedIn:" + this.mPluggedIn + " Charging:" + this.mCharging + " mFastchargeType:" + this.mFastchargeType + " show:" + this.mShowPercent + " style:" + this.mBatteryStyle + " SWarplevel:" + this.mSWarpLevel + " SWarpNextlevel:" + this.mSWarpLevelNext);
        }
        synchronized (this.mChangeCallbacks) {
            int size = this.mChangeCallbacks.size();
            for (int i = 0; i < size; i++) {
                if (OpUtils.SUPPORT_SWARP_CHARGING && this.mSWarpCharging && this.mSWarpLevel > 0.0f && this.mSWarpLevelNext > 0.0f) {
                    this.mChangeCallbacks.get(i).onSWarpBatteryLevelChanged(this.mSWarpLevel, this.mSWarpLevelNext, this.mSWarpDuration);
                }
                this.mChangeCallbacks.get(i).onBatteryLevelChanged(this.mLevel, this.mPluggedIn, this.mCharging);
                this.mChangeCallbacks.get(i).onFastChargeChanged(this.mFastchargeType);
                if (OpFeatures.isSupport(new int[]{237})) {
                    this.mChangeCallbacks.get(i).onWirelessWarpChargeChanged(isWirelessWarpCharging());
                }
            }
        }
    }

    private void firePowerSaveChanged() {
        Log.d("BatteryController", " firePowerSaveChanged mPowerSave:" + this.mPowerSave);
        synchronized (this.mChangeCallbacks) {
            int size = this.mChangeCallbacks.size();
            for (int i = 0; i < size; i++) {
                this.mChangeCallbacks.get(i).onPowerSaveChanged(this.mPowerSave);
            }
        }
    }

    private void fireInvalidChargerChange() {
        if (ProductUtils.isUsvMode()) {
            synchronized (this.mChangeCallbacks) {
                int size = this.mChangeCallbacks.size();
                Log.i("BatteryController", " fireInvalidChargerChange mInvalidCharger:" + this.mInvalidCharger);
                for (int i = 0; i < size; i++) {
                    try {
                        this.mChangeCallbacks.get(i).onInvalidChargeChanged(this.mInvalidCharger);
                    } catch (IndexOutOfBoundsException e) {
                        Log.i("BatteryController", " fireBatteryStylechange:" + e.getMessage());
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fireBatteryStylechange() {
        synchronized (this.mChangeCallbacks) {
            int size = this.mChangeCallbacks.size();
            Log.i("BatteryController", " fireBatteryStylechange mShowPercent:" + this.mShowPercent + " mBatteryStyle:" + this.mBatteryStyle + " mFastchargeType:" + this.mFastchargeType);
            for (int i = 0; i < size; i++) {
                try {
                    this.mChangeCallbacks.get(i).onBatteryPercentShowChange(this.mShowPercent);
                    this.mChangeCallbacks.get(i).onBatteryStyleChanged(this.mBatteryStyle);
                } catch (IndexOutOfBoundsException e) {
                    Log.i("BatteryController", " fireBatteryStylechange:" + e.getMessage());
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fireOptimizatedStatusChange() {
        synchronized (this.mChangeCallbacks) {
            int size = this.mChangeCallbacks.size();
            Log.i("BatteryController", " fireOptimizatedStatusChange mIsOptimizatedCharge:" + this.mIsOptimizatedCharge);
            for (int i = 0; i < size; i++) {
                try {
                    this.mChangeCallbacks.get(i).onOptimizatedStatusChange(this.mIsOptimizatedCharge);
                } catch (IndexOutOfBoundsException e) {
                    Log.i("BatteryController", " fireOptimizatedStatusChange:" + e.getMessage());
                }
            }
        }
    }

    @Override // com.android.systemui.DemoMode
    public void dispatchDemoCommand(String str, Bundle bundle) {
        if (!this.mDemoMode && str.equals("enter")) {
            this.mDemoMode = true;
            this.mBroadcastDispatcher.unregisterReceiver(this);
        } else if (this.mDemoMode && str.equals("exit")) {
            this.mDemoMode = false;
            registerReceiver();
            updatePowerSave();
        } else if (this.mDemoMode && str.equals("battery")) {
            String string = bundle.getString("level");
            String string2 = bundle.getString("plugged");
            String string3 = bundle.getString("powersave");
            String string4 = bundle.getString("powerOptimizated");
            String string5 = bundle.getString("invalidCharge");
            if (string != null) {
                this.mLevel = Math.min(Math.max(Integer.parseInt(string), 0), 100);
            }
            if (string2 != null) {
                this.mPluggedIn = Boolean.parseBoolean(string2);
            }
            if (string3 != null) {
                this.mPowerSave = string3.equals("true");
                firePowerSaveChanged();
            }
            if (string4 != null) {
                this.mIsOptimizatedCharge = string4.equals("true");
                fireOptimizatedStatusChange();
            }
            if (string5 != null) {
                this.mInvalidCharger = Integer.valueOf(string5).intValue();
                fireInvalidChargerChange();
            }
            fireBatteryLevelChanged();
        }
    }

    /* access modifiers changed from: private */
    public final class SettingObserver extends ContentObserver {
        private final Uri CHARING_OPTIMIZATED_STATUS_URI = Settings.System.getUriFor("charging_optimized_status");
        private final Uri SHOW_BATTERY_PERCENT = Settings.System.getUriFor("status_bar_show_battery_percent");
        private final Uri STATUS_BAR_BATTERY_STYLE = Settings.System.getUriFor("status_bar_battery_style");

        public SettingObserver() {
            super(new Handler());
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            ContentResolver contentResolver = BatteryControllerImpl.this.mContext.getContentResolver();
            contentResolver.registerContentObserver(this.SHOW_BATTERY_PERCENT, false, this, -1);
            contentResolver.registerContentObserver(this.STATUS_BAR_BATTERY_STYLE, false, this, -1);
            contentResolver.registerContentObserver(this.CHARING_OPTIMIZATED_STATUS_URI, false, this, -1);
            update(null);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            boolean z;
            ContentResolver contentResolver = BatteryControllerImpl.this.mContext.getContentResolver();
            boolean z2 = true;
            boolean z3 = false;
            if (uri == null || this.SHOW_BATTERY_PERCENT.equals(uri)) {
                BatteryControllerImpl.this.mShowPercent = Settings.System.getInt(contentResolver, "status_bar_show_battery_percent", 0) != 0;
                z = true;
            } else {
                z = false;
            }
            if (uri == null || this.STATUS_BAR_BATTERY_STYLE.equals(uri)) {
                BatteryControllerImpl.this.mBatteryStyle = Settings.System.getInt(contentResolver, "status_bar_battery_style", 0);
                if (BatteryControllerImpl.this.mBatteryStyle == 3) {
                    BatteryControllerImpl.this.mBatteryStyle = 0;
                    BatteryControllerImpl.this.mShowPercent = true;
                    Log.d("BatteryController", "Migrate battery style from percent to bar and show percentage.");
                    Settings.System.putInt(contentResolver, "status_bar_battery_style", BatteryControllerImpl.this.mBatteryStyle);
                    Settings.System.putInt(contentResolver, "status_bar_show_battery_percent", 1);
                }
                z = true;
            }
            if (uri == null || this.CHARING_OPTIMIZATED_STATUS_URI.equals(uri)) {
                BatteryControllerImpl batteryControllerImpl = BatteryControllerImpl.this;
                if (Settings.System.getInt(contentResolver, "charging_optimized_status", 0) != 0) {
                    z3 = true;
                }
                batteryControllerImpl.mIsOptimizatedCharge = z3;
            } else {
                z2 = false;
            }
            Log.d("BatteryController", "update uri: " + uri + " mShowPercent: " + BatteryControllerImpl.this.mShowPercent + " mBatteryStyle: " + BatteryControllerImpl.this.mBatteryStyle + " mIsOptimizatedCharge:" + BatteryControllerImpl.this.mIsOptimizatedCharge);
            if (z) {
                BatteryControllerImpl.this.fireBatteryStylechange();
            }
            if (z2) {
                BatteryControllerImpl.this.fireOptimizatedStatusChange();
            }
        }
    }
}
