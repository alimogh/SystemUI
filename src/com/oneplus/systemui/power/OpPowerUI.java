package com.oneplus.systemui.power;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0009R$integer;
import com.android.systemui.Prefs;
import com.android.systemui.SystemUI;
import com.android.systemui.power.BatteryStateSnapshot;
import com.android.systemui.power.PowerUI;
import com.oneplus.util.OpReflectionUtils;
import com.oneplus.util.OpUtils;
import java.time.Duration;
public class OpPowerUI extends SystemUI {
    protected static final boolean OP_DEBUG = OpUtils.DEBUG_ONEPLUS;
    private static final long SIX_HOURS_MILLIS = Duration.ofHours(6).toMillis();
    private final boolean DEBUG;
    private boolean mCurrentPowerSave;
    protected final int[] mLowBatteryReminderLevels;
    private int mScreenTimeout;
    private boolean mSelfChange;
    private boolean mSelfChangeRestore;
    private int mUser;

    /* access modifiers changed from: protected */
    public interface OpWarningsUI {
        boolean isWarningNotificationShow();
    }

    public OpPowerUI(Context context) {
        super(context);
        this.DEBUG = Log.isLoggable("OpPowerUI", 3) || OP_DEBUG;
        this.mLowBatteryReminderLevels = new int[3];
        this.mScreenTimeout = 0;
        this.mUser = 0;
        this.mCurrentPowerSave = false;
        this.mSelfChange = false;
        this.mSelfChangeRestore = false;
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mUser = KeyguardUpdateMonitor.getCurrentUser();
        restoreScreenTimeoutFromPrefsIfNeeded();
        this.mScreenTimeout = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_off_timeout", 30000, this.mUser);
    }

    /* access modifiers changed from: protected */
    public void maybeShowBatteryWarning(BatteryStateSnapshot batteryStateSnapshot, BatteryStateSnapshot batteryStateSnapshot2) {
        PowerUI.WarningsUI warnings = getWarnings();
        boolean z = !(batteryStateSnapshot.getBucket() == batteryStateSnapshot2.getBucket() || batteryStateSnapshot.getBucket() == -2) || batteryStateSnapshot2.getPlugged();
        if (shouldShowLowBatteryWarningInternal(batteryStateSnapshot, batteryStateSnapshot2)) {
            warnings.showLowBatteryWarning(z);
        } else if (shouldDismissLowBatteryWarning(batteryStateSnapshot, batteryStateSnapshot2)) {
            warnings.dismissLowBatteryWarning();
        } else if (batteryStateSnapshot2.getBucket() != batteryStateSnapshot.getBucket()) {
            warnings.updateLowBatteryWarning();
        }
    }

    /* access modifiers changed from: protected */
    public void maybeShowHybridWarningInternal(BatteryStateSnapshot batteryStateSnapshot, BatteryStateSnapshot batteryStateSnapshot2) {
        PowerUI.WarningsUI warnings = getWarnings();
        boolean z = false;
        if (batteryStateSnapshot.getBatteryLevel() >= 45 && batteryStateSnapshot.getTimeRemainingMillis() > SIX_HOURS_MILLIS) {
            setLowWarningShownThisChargeCycle(false);
            setSevereWarningShownThisChargeCycle(false);
            if (this.DEBUG) {
                Slog.d("OpPowerUI", "Charge cycle reset! Can show warnings again");
            }
        }
        if (!(batteryStateSnapshot.getBucket() == batteryStateSnapshot2.getBucket() || batteryStateSnapshot.getBucket() == -2) || batteryStateSnapshot2.getPlugged()) {
            z = true;
        }
        if (shouldShowHybridWarning(batteryStateSnapshot)) {
            warnings.showLowBatteryWarning(z);
            if (batteryStateSnapshot.getTimeRemainingMillis() <= batteryStateSnapshot.getSevereThresholdMillis() || batteryStateSnapshot.getBatteryLevel() <= batteryStateSnapshot.getSevereLevelThreshold()) {
                setSevereWarningShownThisChargeCycle(true);
                setLowWarningShownThisChargeCycle(true);
                if (this.DEBUG) {
                    Slog.d("OpPowerUI", "Severe warning marked as shown this cycle");
                    return;
                }
                return;
            }
            Slog.d("OpPowerUI", "Low warning marked as shown this cycle");
            setLowWarningShownThisChargeCycle(true);
        } else if (shouldDismissHybridWarning(batteryStateSnapshot)) {
            if (this.DEBUG) {
                Slog.d("OpPowerUI", "Dismissing warning");
            }
            warnings.dismissLowBatteryWarning();
        } else {
            if (this.DEBUG) {
                Slog.d("OpPowerUI", "Updating warning");
            }
            warnings.updateLowBatteryWarning();
        }
    }

    /* access modifiers changed from: protected */
    public void registerObserverInternal(ContentResolver contentResolver, Handler handler, final PowerManager powerManager) {
        contentResolver.registerContentObserver(Settings.System.getUriFor("screen_off_timeout"), false, new ContentObserver(handler) { // from class: com.oneplus.systemui.power.OpPowerUI.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z, Uri uri) {
                if (Settings.System.getUriFor("screen_off_timeout").equals(uri)) {
                    Log.d("OpPowerUI", "onChange:SCREEN_OFF_TIMEOUT:mSelfChange=" + OpPowerUI.this.mSelfChange + ", mSelfChangeRestore=" + OpPowerUI.this.mSelfChangeRestore);
                    if (!powerManager.isPowerSaveMode()) {
                        return;
                    }
                    if (OpPowerUI.this.mSelfChange) {
                        OpPowerUI.this.mSelfChange = false;
                    } else if (OpPowerUI.this.mSelfChangeRestore) {
                        OpPowerUI.this.mSelfChangeRestore = false;
                    } else {
                        OpPowerUI opPowerUI = OpPowerUI.this;
                        opPowerUI.mScreenTimeout = Settings.System.getIntForUser(((SystemUI) opPowerUI).mContext.getContentResolver(), "screen_off_timeout", 30000, OpPowerUI.this.mUser);
                        OpPowerUI.this.saveScreenTimeoutToPrefs(0);
                        Log.d("OpPowerUI", "SettingsObserver:onChange:User changed the timeout during power saving mode: mScreenTimeout=" + OpPowerUI.this.mScreenTimeout);
                    }
                }
            }
        }, -1);
    }

    /* access modifiers changed from: protected */
    public void restorePowerSavingSettingsForUser() {
        this.mSelfChangeRestore = true;
        Settings.System.putIntForUser(this.mContext.getContentResolver(), "screen_off_timeout", this.mScreenTimeout, this.mUser);
        Log.d("OpPowerUI", "restorePowerSavingSettingsForUser:mScreenTimeout=" + this.mScreenTimeout + ", user=" + this.mUser);
    }

    private void restoreScreenTimeoutFromPrefsIfNeeded() {
        int i = Prefs.getInt(this.mContext, "PowerSavingTimeoutBackup", 0);
        if (i > 0) {
            Log.d("OpPowerUI", "restoreScreenTimeoutFromPrefsIfNeeded:" + i);
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "screen_off_timeout", i, this.mUser);
            saveScreenTimeoutToPrefs(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void saveScreenTimeoutToPrefs(int i) {
        Log.d("OpPowerUI", "saveScreenTimeoutToPrefs:" + i);
        Prefs.putInt(this.mContext, "PowerSavingTimeoutBackup", i);
    }

    private boolean shouldDismissHybridWarning(BatteryStateSnapshot batteryStateSnapshot) {
        return ((Boolean) OpReflectionUtils.methodInvokeWithArgs(this, OpReflectionUtils.getMethodWithParams(PowerUI.class, "shouldDismissHybridWarning", BatteryStateSnapshot.class), batteryStateSnapshot)).booleanValue();
    }

    private boolean shouldDismissLowBatteryWarning(BatteryStateSnapshot batteryStateSnapshot, BatteryStateSnapshot batteryStateSnapshot2) {
        return ((Boolean) OpReflectionUtils.methodInvokeWithArgs(this, OpReflectionUtils.getMethodWithParams(PowerUI.class, "shouldDismissLowBatteryWarning", BatteryStateSnapshot.class, BatteryStateSnapshot.class), batteryStateSnapshot, batteryStateSnapshot2)).booleanValue();
    }

    private boolean shouldShowHybridWarning(BatteryStateSnapshot batteryStateSnapshot) {
        return ((Boolean) OpReflectionUtils.methodInvokeWithArgs(this, OpReflectionUtils.getMethodWithParams(PowerUI.class, "shouldShowHybridWarning", BatteryStateSnapshot.class), batteryStateSnapshot)).booleanValue();
    }

    /* access modifiers changed from: protected */
    public boolean shouldShowLowBatteryWarningInternal(BatteryStateSnapshot batteryStateSnapshot, BatteryStateSnapshot batteryStateSnapshot2) {
        boolean z = !getWarnings().isWarningNotificationShow() && batteryStateSnapshot.getBucket() == -2 && batteryStateSnapshot2.getBucket() == -1;
        if (batteryStateSnapshot.getPlugged() || batteryStateSnapshot.isPowerSaver()) {
            return false;
        }
        return ((batteryStateSnapshot.getBucket() < batteryStateSnapshot2.getBucket() && !z) || batteryStateSnapshot2.getPlugged()) && batteryStateSnapshot.getBucket() < 0 && batteryStateSnapshot.getBatteryStatus() != 1;
    }

    /* access modifiers changed from: protected */
    public void updateBatteryWarningLevelsInternal() {
        int integer = this.mContext.getResources().getInteger(17694768);
        int integer2 = this.mContext.getResources().getInteger(17694839);
        if (integer2 < integer) {
            integer2 = integer;
        }
        int[] iArr = this.mLowBatteryReminderLevels;
        iArr[0] = integer2;
        iArr[1] = this.mContext.getResources().getInteger(C0009R$integer.config_lowBatteryWarningLevel_1);
        int[] iArr2 = this.mLowBatteryReminderLevels;
        iArr2[2] = integer;
        setLowBatteryAlertCloseLevel(iArr2[0] + this.mContext.getResources().getInteger(17694838));
    }

    /* access modifiers changed from: protected */
    public void updatePowerSavingSettings(boolean z) {
        this.mUser = KeyguardUpdateMonitor.getCurrentUser();
        this.mSelfChange = true;
        if (z != this.mCurrentPowerSave) {
            this.mCurrentPowerSave = z;
            if (z) {
                int intForUser = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_off_timeout", 30000, this.mUser);
                this.mScreenTimeout = intForUser;
                saveScreenTimeoutToPrefs(intForUser);
                Settings.System.putIntForUser(this.mContext.getContentResolver(), "screen_off_timeout", 30000, this.mUser);
                Log.d("OpPowerUI", "updatePowerSavingSettings:Enter PowerSaving Mode: mScreenTimeout=" + this.mScreenTimeout + ", user=" + this.mUser);
                return;
            }
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "screen_off_timeout", this.mScreenTimeout, this.mUser);
            saveScreenTimeoutToPrefs(0);
            Log.d("OpPowerUI", "updatePowerSavingSettings:Leave PowerSaving Mode: mScreenTimeout=" + this.mScreenTimeout + ", user=" + this.mUser);
        }
    }

    private void setLowBatteryAlertCloseLevel(int i) {
        OpReflectionUtils.setValue(PowerUI.class, this, "mLowBatteryAlertCloseLevel", Integer.valueOf(i));
    }

    private void setLowWarningShownThisChargeCycle(boolean z) {
        OpReflectionUtils.setValue(PowerUI.class, this, "mLowWarningShownThisChargeCycle", Boolean.valueOf(z));
    }

    private void setSevereWarningShownThisChargeCycle(boolean z) {
        OpReflectionUtils.setValue(PowerUI.class, this, "mSevereWarningShownThisChargeCycle", Boolean.valueOf(z));
    }

    private PowerUI.WarningsUI getWarnings() {
        return (PowerUI.WarningsUI) OpReflectionUtils.getValue(PowerUI.class, this, "mWarnings");
    }

    /* access modifiers changed from: protected */
    public class OpReceiver extends BroadcastReceiver {
        protected OpReceiver(OpPowerUI opPowerUI) {
        }

        public void init(IntentFilter intentFilter) {
            intentFilter.addAction("android.os.action.POWER_SAVE_MODE_CHANGING");
        }
    }
}
