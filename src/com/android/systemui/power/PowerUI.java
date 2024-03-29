package com.android.systemui.power;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IThermalEventListener;
import android.os.IThermalService;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.Temperature;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.fuelgauge.Estimate;
import com.android.settingslib.utils.ThreadUtils;
import com.android.systemui.C0009R$integer;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.power.PowerUI;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBar;
import com.oneplus.systemui.power.OpPowerUI;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Future;
public class PowerUI extends OpPowerUI implements CommandQueue.Callbacks {
    static final boolean DEBUG = (Log.isLoggable("PowerUI", 3) || OpPowerUI.OP_DEBUG);
    @VisibleForTesting
    int mBatteryLevel = 100;
    @VisibleForTesting
    int mBatteryStatus = 1;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final CommandQueue mCommandQueue;
    @VisibleForTesting
    BatteryStateSnapshot mCurrentBatteryStateSnapshot;
    private boolean mEnableSkinTemperatureWarning;
    private boolean mEnableUsbTemperatureAlarm;
    private EnhancedEstimates mEnhancedEstimates;
    private final Handler mHandler = new Handler();
    private int mInvalidCharger = 0;
    @VisibleForTesting
    BatteryStateSnapshot mLastBatteryStateSnapshot;
    private final Configuration mLastConfiguration = new Configuration();
    private Future mLastShowWarningTask;
    private int mLowBatteryAlertCloseLevel;
    @VisibleForTesting
    boolean mLowWarningShownThisChargeCycle;
    private InattentiveSleepWarningView mOverlayView;
    private int mPlugType = 0;
    private PowerManager mPowerManager;
    @VisibleForTesting
    final Receiver mReceiver = new Receiver();
    private long mScreenOffTime = -1;
    @VisibleForTesting
    boolean mSevereWarningShownThisChargeCycle;
    private IThermalEventListener mSkinThermalEventListener;
    private final Lazy<StatusBar> mStatusBarLazy;
    @VisibleForTesting
    IThermalService mThermalService;
    private IThermalEventListener mUsbThermalEventListener;
    private WarningsUI mWarnings;

    public interface WarningsUI extends OpPowerUI.OpWarningsUI {
        void dismissHighTemperatureWarning();

        void dismissInvalidChargerWarning();

        void dismissLowBatteryWarning();

        void dump(PrintWriter printWriter);

        boolean isInvalidChargerWarningShowing();

        void showHighTemperatureWarning();

        void showInvalidChargerWarning();

        void showLowBatteryWarning(boolean z);

        void showThermalShutdownWarning();

        void showUsbHighTemperatureAlarm();

        void update(int i, int i2, long j);

        void updateLowBatteryWarning();

        void updateSnapshot(BatteryStateSnapshot batteryStateSnapshot);

        void userSwitched();
    }

    static {
        Duration.ofHours(6).toMillis();
    }

    public PowerUI(Context context, BroadcastDispatcher broadcastDispatcher, CommandQueue commandQueue, Lazy<StatusBar> lazy) {
        super(context);
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mCommandQueue = commandQueue;
        this.mStatusBarLazy = lazy;
    }

    @Override // com.oneplus.systemui.power.OpPowerUI, com.android.systemui.SystemUI
    public void start() {
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mPowerManager = powerManager;
        this.mScreenOffTime = powerManager.isScreenOn() ? -1 : SystemClock.elapsedRealtime();
        this.mWarnings = (WarningsUI) Dependency.get(WarningsUI.class);
        this.mEnhancedEstimates = (EnhancedEstimates) Dependency.get(EnhancedEstimates.class);
        this.mLastConfiguration.setTo(this.mContext.getResources().getConfiguration());
        AnonymousClass1 r0 = new ContentObserver(this.mHandler) { // from class: com.android.systemui.power.PowerUI.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                PowerUI.this.updateBatteryWarningLevels();
            }
        };
        ContentResolver contentResolver = this.mContext.getContentResolver();
        contentResolver.registerContentObserver(Settings.Global.getUriFor("low_power_trigger_level"), false, r0, -1);
        super.start();
        registerObserverInternal(contentResolver, this.mHandler, this.mPowerManager);
        updateBatteryWarningLevels();
        this.mReceiver.init();
        showWarnOnThermalShutdown();
        contentResolver.registerContentObserver(Settings.Global.getUriFor("show_temperature_warning"), false, new ContentObserver(this.mHandler) { // from class: com.android.systemui.power.PowerUI.2
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                PowerUI.this.doSkinThermalEventListenerRegistration();
            }
        });
        contentResolver.registerContentObserver(Settings.Global.getUriFor("show_usb_temperature_alarm"), false, new ContentObserver(this.mHandler) { // from class: com.android.systemui.power.PowerUI.3
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                PowerUI.this.doUsbThermalEventListenerRegistration();
            }
        });
        initThermalEventListeners();
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.SystemUI
    public void onConfigurationChanged(Configuration configuration) {
        if ((this.mLastConfiguration.updateFrom(configuration) & 3) != 0) {
            this.mHandler.post(new Runnable() { // from class: com.android.systemui.power.-$$Lambda$PowerUI$QV7l9YjJI0jIQa7PQUr5PFep9Kg
                @Override // java.lang.Runnable
                public final void run() {
                    PowerUI.this.initThermalEventListeners();
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void updateBatteryWarningLevels() {
        updateBatteryWarningLevelsInternal();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int findBatteryLevelBucket(int i) {
        if (i >= this.mLowBatteryAlertCloseLevel) {
            return 1;
        }
        int[] iArr = this.mLowBatteryReminderLevels;
        if (i > iArr[0]) {
            return 0;
        }
        for (int length = iArr.length - 1; length >= 0; length--) {
            if (i <= this.mLowBatteryReminderLevels[length]) {
                return -1 - length;
            }
        }
        throw new RuntimeException("not possible!");
    }

    @VisibleForTesting
    final class Receiver extends OpPowerUI.OpReceiver {
        private boolean mHasReceivedBattery = false;

        Receiver() {
            super(PowerUI.this);
        }

        public void init() {
            Intent registerReceiver;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.os.action.POWER_SAVE_MODE_CHANGED");
            intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            super.init(intentFilter);
            PowerUI.this.mBroadcastDispatcher.registerReceiverWithHandler(this, intentFilter, PowerUI.this.mHandler);
            if (PowerUI.DEBUG) {
                Slog.d("PowerUI", "Receiver init:" + this.mHasReceivedBattery + ", " + intentFilter);
            }
            if (!this.mHasReceivedBattery && (registerReceiver = ((SystemUI) PowerUI.this).mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"))) != null) {
                onReceive(((SystemUI) PowerUI.this).mContext, registerReceiver);
            }
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (PowerUI.DEBUG) {
                Slog.d("PowerUI", "onReceive:" + action + " start" + this);
            }
            if ("android.os.action.POWER_SAVE_MODE_CHANGED".equals(action)) {
                ThreadUtils.postOnBackgroundThread(new Runnable() { // from class: com.android.systemui.power.-$$Lambda$PowerUI$Receiver$r1RcZjs8DVXWaC4Afqm8W0WAvm8
                    @Override // java.lang.Runnable
                    public final void run() {
                        PowerUI.Receiver.this.lambda$onReceive$0$PowerUI$Receiver();
                    }
                });
            } else if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                this.mHasReceivedBattery = true;
                PowerUI powerUI = PowerUI.this;
                int i = powerUI.mBatteryLevel;
                powerUI.mBatteryLevel = intent.getIntExtra("level", 100);
                PowerUI powerUI2 = PowerUI.this;
                int i2 = powerUI2.mBatteryStatus;
                powerUI2.mBatteryStatus = intent.getIntExtra("status", 1);
                int i3 = PowerUI.this.mPlugType;
                PowerUI.this.mPlugType = intent.getIntExtra("plugged", 1);
                int i4 = PowerUI.this.mInvalidCharger;
                PowerUI.this.mInvalidCharger = intent.getIntExtra("invalid_charger", 0);
                PowerUI powerUI3 = PowerUI.this;
                powerUI3.mLastBatteryStateSnapshot = powerUI3.mCurrentBatteryStateSnapshot;
                boolean z = powerUI3.mPlugType != 0;
                boolean z2 = i3 != 0;
                int findBatteryLevelBucket = PowerUI.this.findBatteryLevelBucket(i);
                PowerUI powerUI4 = PowerUI.this;
                int findBatteryLevelBucket2 = powerUI4.findBatteryLevelBucket(powerUI4.mBatteryLevel);
                if (PowerUI.DEBUG) {
                    Log.d("PowerUI", "buckets:" + PowerUI.this.mLowBatteryAlertCloseLevel + ".." + ((OpPowerUI) PowerUI.this).mLowBatteryReminderLevels[0] + ".." + ((OpPowerUI) PowerUI.this).mLowBatteryReminderLevels[1] + ".." + ((OpPowerUI) PowerUI.this).mLowBatteryReminderLevels[2] + "  level:" + i + "-->" + PowerUI.this.mBatteryLevel + "  status:" + i2 + "-->" + PowerUI.this.mBatteryStatus + "  plugType:" + i3 + "-->" + PowerUI.this.mPlugType + "  invalidCharger:" + i4 + "-->" + PowerUI.this.mInvalidCharger + "  bucket:" + findBatteryLevelBucket + "-->" + findBatteryLevelBucket2 + "  plugged:" + z2 + "-->" + z + "  isPowerSaveMode:" + PowerUI.this.mPowerManager.isPowerSaveMode());
                }
                WarningsUI warningsUI = PowerUI.this.mWarnings;
                PowerUI powerUI5 = PowerUI.this;
                warningsUI.update(powerUI5.mBatteryLevel, findBatteryLevelBucket2, powerUI5.mScreenOffTime);
                if (i4 != 0 || PowerUI.this.mInvalidCharger == 0) {
                    if (i4 != 0 && PowerUI.this.mInvalidCharger == 0) {
                        PowerUI.this.mWarnings.dismissInvalidChargerWarning();
                    } else if (PowerUI.this.mWarnings.isInvalidChargerWarningShowing()) {
                        if (PowerUI.DEBUG) {
                            Slog.d("PowerUI", "Bad Charger");
                            return;
                        }
                        return;
                    }
                    if (PowerUI.this.mLastShowWarningTask != null) {
                        PowerUI.this.mLastShowWarningTask.cancel(true);
                        if (PowerUI.DEBUG) {
                            Slog.d("PowerUI", "cancelled task");
                        }
                    }
                    PowerUI.this.mLastShowWarningTask = ThreadUtils.postOnBackgroundThread(new Runnable(z, findBatteryLevelBucket2) { // from class: com.android.systemui.power.-$$Lambda$PowerUI$Receiver$YHQ7eAdH8G2eZkWaBryO-zqzv1I
                        public final /* synthetic */ boolean f$1;
                        public final /* synthetic */ int f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            PowerUI.Receiver.this.lambda$onReceive$1$PowerUI$Receiver(this.f$1, this.f$2);
                        }
                    });
                } else {
                    Slog.d("PowerUI", "showing invalid charger warning");
                    PowerUI.this.mWarnings.showInvalidChargerWarning();
                    return;
                }
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                PowerUI.this.mScreenOffTime = SystemClock.elapsedRealtime();
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                PowerUI.this.mScreenOffTime = -1;
            } else if ("android.intent.action.USER_SWITCHED".equals(action)) {
                PowerUI.this.mWarnings.userSwitched();
                boolean isPowerSaveMode = PowerUI.this.mPowerManager.isPowerSaveMode();
                if (isPowerSaveMode) {
                    PowerUI.this.restorePowerSavingSettingsForUser();
                    PowerUI.this.updatePowerSavingSettings(isPowerSaveMode);
                }
            } else if ("android.os.action.POWER_SAVE_MODE_CHANGING".equals(action)) {
                PowerUI.this.updatePowerSavingSettings(intent.getBooleanExtra("mode", false));
            } else {
                Slog.w("PowerUI", "unknown intent: " + intent);
            }
            if (PowerUI.DEBUG) {
                Slog.d("PowerUI", "onReceive:" + action + " end");
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onReceive$0 */
        public /* synthetic */ void lambda$onReceive$0$PowerUI$Receiver() {
            if (PowerUI.this.mPowerManager.isPowerSaveMode()) {
                PowerUI.this.mWarnings.dismissLowBatteryWarning();
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onReceive$1 */
        public /* synthetic */ void lambda$onReceive$1$PowerUI$Receiver(boolean z, int i) {
            PowerUI.this.maybeShowBatteryWarningV2(z, i);
        }
    }

    /* access modifiers changed from: protected */
    public void maybeShowBatteryWarningV2(boolean z, int i) {
        boolean isHybridNotificationEnabled = this.mEnhancedEstimates.isHybridNotificationEnabled();
        boolean isPowerSaveMode = this.mPowerManager.isPowerSaveMode();
        int[] iArr = this.mLowBatteryReminderLevels;
        int i2 = iArr[2];
        int i3 = iArr[1];
        if (DEBUG) {
            Slog.d("PowerUI", "evaluating which notification to show");
        }
        if (isHybridNotificationEnabled) {
            if (DEBUG) {
                Slog.d("PowerUI", "using hybrid");
            }
            Estimate refreshEstimateIfNeeded = refreshEstimateIfNeeded();
            this.mCurrentBatteryStateSnapshot = new BatteryStateSnapshot(this.mBatteryLevel, isPowerSaveMode, z, i, this.mBatteryStatus, i2, i3, refreshEstimateIfNeeded.getEstimateMillis(), refreshEstimateIfNeeded.getAverageDischargeTime(), this.mEnhancedEstimates.getSevereWarningThreshold(), this.mEnhancedEstimates.getLowWarningThreshold(), refreshEstimateIfNeeded.isBasedOnUsage(), this.mEnhancedEstimates.getLowWarningEnabled());
        } else {
            if (DEBUG) {
                Slog.d("PowerUI", "using standard");
            }
            this.mCurrentBatteryStateSnapshot = new BatteryStateSnapshot(this.mBatteryLevel, isPowerSaveMode, z, i, this.mBatteryStatus, i2, i3);
        }
        this.mWarnings.updateSnapshot(this.mCurrentBatteryStateSnapshot);
        if (this.mCurrentBatteryStateSnapshot.isHybrid()) {
            maybeShowHybridWarning(this.mCurrentBatteryStateSnapshot, this.mLastBatteryStateSnapshot);
        } else {
            maybeShowBatteryWarning(this.mCurrentBatteryStateSnapshot, this.mLastBatteryStateSnapshot);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Estimate refreshEstimateIfNeeded() {
        BatteryStateSnapshot batteryStateSnapshot = this.mLastBatteryStateSnapshot;
        if (batteryStateSnapshot != null && batteryStateSnapshot.getTimeRemainingMillis() != -1 && this.mBatteryLevel == this.mLastBatteryStateSnapshot.getBatteryLevel()) {
            return new Estimate(this.mLastBatteryStateSnapshot.getTimeRemainingMillis(), this.mLastBatteryStateSnapshot.isBasedOnUsage(), this.mLastBatteryStateSnapshot.getAverageTimeToDischargeMillis());
        }
        Estimate estimate = this.mEnhancedEstimates.getEstimate();
        if (DEBUG) {
            Slog.d("PowerUI", "updated estimate: " + estimate.getEstimateMillis());
        }
        return estimate;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void maybeShowHybridWarning(BatteryStateSnapshot batteryStateSnapshot, BatteryStateSnapshot batteryStateSnapshot2) {
        super.maybeShowHybridWarningInternal(batteryStateSnapshot, batteryStateSnapshot2);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean shouldShowHybridWarning(BatteryStateSnapshot batteryStateSnapshot) {
        boolean z = false;
        boolean z2 = true;
        if (batteryStateSnapshot.getPlugged() || batteryStateSnapshot.getBatteryStatus() == 1) {
            StringBuilder sb = new StringBuilder();
            sb.append("can't show warning due to - plugged: ");
            sb.append(batteryStateSnapshot.getPlugged());
            sb.append(" status unknown: ");
            if (batteryStateSnapshot.getBatteryStatus() != 1) {
                z2 = false;
            }
            sb.append(z2);
            Slog.d("PowerUI", sb.toString());
            return false;
        }
        long timeRemainingMillis = batteryStateSnapshot.getTimeRemainingMillis();
        boolean z3 = batteryStateSnapshot.isLowWarningEnabled() && !this.mLowWarningShownThisChargeCycle && !batteryStateSnapshot.isPowerSaver() && ((timeRemainingMillis != -1 && timeRemainingMillis < batteryStateSnapshot.getLowThresholdMillis()) || batteryStateSnapshot.getBatteryLevel() <= batteryStateSnapshot.getLowLevelThreshold());
        boolean z4 = !this.mSevereWarningShownThisChargeCycle && ((timeRemainingMillis != -1 && timeRemainingMillis < batteryStateSnapshot.getSevereThresholdMillis()) || batteryStateSnapshot.getBatteryLevel() <= batteryStateSnapshot.getSevereLevelThreshold());
        if (z3 || z4) {
            z = true;
        }
        if (DEBUG) {
            Slog.d("PowerUI", "Enhanced trigger is: " + z + "\nwith battery snapshot: mLowWarningShownThisChargeCycle: " + this.mLowWarningShownThisChargeCycle + " mSevereWarningShownThisChargeCycle: " + this.mSevereWarningShownThisChargeCycle + "\n" + batteryStateSnapshot.toString());
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean shouldDismissHybridWarning(BatteryStateSnapshot batteryStateSnapshot) {
        return batteryStateSnapshot.getPlugged() || batteryStateSnapshot.getTimeRemainingMillis() > batteryStateSnapshot.getLowThresholdMillis();
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.systemui.power.OpPowerUI
    public void maybeShowBatteryWarning(BatteryStateSnapshot batteryStateSnapshot, BatteryStateSnapshot batteryStateSnapshot2) {
        super.maybeShowBatteryWarning(batteryStateSnapshot, batteryStateSnapshot2);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean shouldShowLowBatteryWarning(BatteryStateSnapshot batteryStateSnapshot, BatteryStateSnapshot batteryStateSnapshot2) {
        return super.shouldShowLowBatteryWarningInternal(batteryStateSnapshot, batteryStateSnapshot2);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean shouldDismissLowBatteryWarning(BatteryStateSnapshot batteryStateSnapshot, BatteryStateSnapshot batteryStateSnapshot2) {
        return batteryStateSnapshot.isPowerSaver() || batteryStateSnapshot.getPlugged() || (batteryStateSnapshot.getBucket() > batteryStateSnapshot2.getBucket() && batteryStateSnapshot.getBucket() > 0);
    }

    /* access modifiers changed from: private */
    public void initThermalEventListeners() {
        doSkinThermalEventListenerRegistration();
        doUsbThermalEventListenerRegistration();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public synchronized void doSkinThermalEventListenerRegistration() {
        boolean z;
        boolean z2 = this.mEnableSkinTemperatureWarning;
        boolean z3 = true;
        boolean z4 = Settings.Global.getInt(this.mContext.getContentResolver(), "show_temperature_warning", this.mContext.getResources().getInteger(C0009R$integer.config_showTemperatureWarning)) != 0;
        this.mEnableSkinTemperatureWarning = z4;
        if (z4 != z2) {
            try {
                if (this.mSkinThermalEventListener == null) {
                    this.mSkinThermalEventListener = new SkinThermalEventListener();
                }
                if (this.mThermalService == null) {
                    this.mThermalService = IThermalService.Stub.asInterface(ServiceManager.getService("thermalservice"));
                }
                if (this.mEnableSkinTemperatureWarning) {
                    z = this.mThermalService.registerThermalEventListenerWithType(this.mSkinThermalEventListener, 3);
                } else {
                    z = this.mThermalService.unregisterThermalEventListener(this.mSkinThermalEventListener);
                }
            } catch (RemoteException e) {
                Slog.e("PowerUI", "Exception while (un)registering skin thermal event listener.", e);
                z = false;
            }
            if (!z) {
                if (this.mEnableSkinTemperatureWarning) {
                    z3 = false;
                }
                this.mEnableSkinTemperatureWarning = z3;
                Slog.e("PowerUI", "Failed to register or unregister skin thermal event listener.");
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public synchronized void doUsbThermalEventListenerRegistration() {
        boolean z;
        boolean z2 = this.mEnableUsbTemperatureAlarm;
        boolean z3 = true;
        boolean z4 = Settings.Global.getInt(this.mContext.getContentResolver(), "show_usb_temperature_alarm", this.mContext.getResources().getInteger(C0009R$integer.config_showUsbPortAlarm)) != 0;
        this.mEnableUsbTemperatureAlarm = z4;
        if (z4 != z2) {
            try {
                if (this.mUsbThermalEventListener == null) {
                    this.mUsbThermalEventListener = new UsbThermalEventListener();
                }
                if (this.mThermalService == null) {
                    this.mThermalService = IThermalService.Stub.asInterface(ServiceManager.getService("thermalservice"));
                }
                if (this.mEnableUsbTemperatureAlarm) {
                    z = this.mThermalService.registerThermalEventListenerWithType(this.mUsbThermalEventListener, 4);
                } else {
                    z = this.mThermalService.unregisterThermalEventListener(this.mUsbThermalEventListener);
                }
            } catch (RemoteException e) {
                Slog.e("PowerUI", "Exception while (un)registering usb thermal event listener.", e);
                z = false;
            }
            if (!z) {
                if (this.mEnableUsbTemperatureAlarm) {
                    z3 = false;
                }
                this.mEnableUsbTemperatureAlarm = z3;
                Slog.e("PowerUI", "Failed to register or unregister usb thermal event listener.");
            }
        }
    }

    private void showWarnOnThermalShutdown() {
        int i = -1;
        int i2 = this.mContext.getSharedPreferences("powerui_prefs", 0).getInt("boot_count", -1);
        try {
            i = Settings.Global.getInt(this.mContext.getContentResolver(), "boot_count");
        } catch (Settings.SettingNotFoundException unused) {
            Slog.e("PowerUI", "Failed to read system boot count from Settings.Global.BOOT_COUNT");
        }
        if (i > i2) {
            this.mContext.getSharedPreferences("powerui_prefs", 0).edit().putInt("boot_count", i).apply();
            if (this.mPowerManager.getLastShutdownReason() == 4) {
                this.mWarnings.showThermalShutdownWarning();
            }
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showInattentiveSleepWarning() {
        if (this.mOverlayView == null) {
            this.mOverlayView = new InattentiveSleepWarningView(this.mContext);
        }
        this.mOverlayView.show();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void dismissInattentiveSleepWarning(boolean z) {
        InattentiveSleepWarningView inattentiveSleepWarningView = this.mOverlayView;
        if (inattentiveSleepWarningView != null) {
            inattentiveSleepWarningView.dismiss(z);
        }
    }

    @Override // com.android.systemui.SystemUI, com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("mLowBatteryAlertCloseLevel=");
        printWriter.println(this.mLowBatteryAlertCloseLevel);
        printWriter.print("mLowBatteryReminderLevels=");
        printWriter.println(Arrays.toString(this.mLowBatteryReminderLevels));
        printWriter.print("mBatteryLevel=");
        printWriter.println(Integer.toString(this.mBatteryLevel));
        printWriter.print("mBatteryStatus=");
        printWriter.println(Integer.toString(this.mBatteryStatus));
        printWriter.print("mPlugType=");
        printWriter.println(Integer.toString(this.mPlugType));
        printWriter.print("mInvalidCharger=");
        printWriter.println(Integer.toString(this.mInvalidCharger));
        printWriter.print("mScreenOffTime=");
        printWriter.print(this.mScreenOffTime);
        if (this.mScreenOffTime >= 0) {
            printWriter.print(" (");
            printWriter.print(SystemClock.elapsedRealtime() - this.mScreenOffTime);
            printWriter.print(" ago)");
        }
        printWriter.println();
        printWriter.print("soundTimeout=");
        printWriter.println(Settings.Global.getInt(this.mContext.getContentResolver(), "low_battery_sound_timeout", 0));
        printWriter.print("bucket: ");
        printWriter.println(Integer.toString(findBatteryLevelBucket(this.mBatteryLevel)));
        printWriter.print("mEnableSkinTemperatureWarning=");
        printWriter.println(this.mEnableSkinTemperatureWarning);
        printWriter.print("mEnableUsbTemperatureAlarm=");
        printWriter.println(this.mEnableUsbTemperatureAlarm);
        this.mWarnings.dump(printWriter);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public final class SkinThermalEventListener extends IThermalEventListener.Stub {
        SkinThermalEventListener() {
        }

        public void notifyThrottling(Temperature temperature) {
            int status = temperature.getStatus();
            if (status < 5) {
                PowerUI.this.mWarnings.dismissHighTemperatureWarning();
            } else if (!((StatusBar) PowerUI.this.mStatusBarLazy.get()).isDeviceInVrMode()) {
                PowerUI.this.mWarnings.showHighTemperatureWarning();
                Slog.d("PowerUI", "SkinThermalEventListener: notifyThrottling was called , current skin status = " + status + ", temperature = " + temperature.getValue());
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public final class UsbThermalEventListener extends IThermalEventListener.Stub {
        UsbThermalEventListener() {
        }

        public void notifyThrottling(Temperature temperature) {
            int status = temperature.getStatus();
            if (status >= 5) {
                PowerUI.this.mWarnings.showUsbHighTemperatureAlarm();
                Slog.d("PowerUI", "UsbThermalEventListener: notifyThrottling was called , current usb port status = " + status + ", temperature = " + temperature.getValue());
            }
        }
    }
}
