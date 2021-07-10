package com.oneplus.aod.alwayson;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.Dependency;
import com.oneplus.aod.OpAodUtils;
import com.oneplus.battery.OpBatteryStatus;
import com.oneplus.plugin.OpLsState;
import com.oneplus.scene.OpSceneModeObserver;
public class OpAodAlwaysOnController implements OpSceneModeObserver.Callback {
    private static final boolean IS_SUPPORT_ALWAYS_ON = OpAodUtils.isSupportAlwaysOn();
    private boolean mAlwaysOnEnabled = false;
    private OpAodAlwaysOnSettingObserver mAlwaysOnSettingsObserver;
    private int mBatteryLevel;
    private Context mContext;
    private Handler mHandler;
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private OpSceneModeObserver mOpSceneModeObserver;
    private PackageManager mPM;
    private int mPhoneState;
    protected PowerManager mPowerManager;
    private OpAodAlwaysOnKeyguardUpdateMonitorCallback mUpdateMonitorCallback;

    public OpAodAlwaysOnController(Context context) {
        this.mContext = context;
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mPM = context.getPackageManager();
    }

    public void init(KeyguardUpdateMonitor keyguardUpdateMonitor, Handler handler) {
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        this.mHandler = handler;
        if (IS_SUPPORT_ALWAYS_ON) {
            this.mAlwaysOnSettingsObserver = new OpAodAlwaysOnSettingObserver(this.mContext, this.mHandler);
            OpAodAlwaysOnKeyguardUpdateMonitorCallback opAodAlwaysOnKeyguardUpdateMonitorCallback = new OpAodAlwaysOnKeyguardUpdateMonitorCallback();
            this.mUpdateMonitorCallback = opAodAlwaysOnKeyguardUpdateMonitorCallback;
            this.mKeyguardUpdateMonitor.registerCallback(opAodAlwaysOnKeyguardUpdateMonitorCallback);
            registerObserver();
            OpSceneModeObserver opSceneModeObserver = (OpSceneModeObserver) Dependency.get(OpSceneModeObserver.class);
            this.mOpSceneModeObserver = opSceneModeObserver;
            if (opSceneModeObserver != null) {
                opSceneModeObserver.addCallback(this);
            }
        }
    }

    private void registerObserver() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("doze_always_on"), false, this.mAlwaysOnSettingsObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("low_power"), false, this.mAlwaysOnSettingsObserver, -1);
        this.mAlwaysOnSettingsObserver.onChange(true);
    }

    public boolean isAlwaysOnEnabled() {
        if (!IS_SUPPORT_ALWAYS_ON) {
            try {
                if (this.mPM != null) {
                    this.mPM.getPackageInfo("com.google.android.doze.gts", 0);
                    Log.e("AodAlwaysOnController", "Found GTS package");
                    return true;
                }
                Log.e("AodAlwaysOnController", "PackageManager = " + this.mPM);
                return false;
            } catch (Exception unused) {
            }
        } else {
            int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            boolean isPowerSaveMode = this.mPowerManager.isPowerSaveMode();
            OpSceneModeObserver opSceneModeObserver = this.mOpSceneModeObserver;
            boolean isInBrickMode = opSceneModeObserver != null ? opSceneModeObserver.isInBrickMode() : false;
            if (Build.DEBUG_ONEPLUS) {
                Log.d("AodAlwaysOnController", "check isAlwaysOnEnabled: mAlwaysOnEnabled= " + this.mAlwaysOnEnabled + ", currentUserId= " + currentUser + ", isInBrickMode= " + isInBrickMode + ", isPowerSaveMode= " + isPowerSaveMode + ", phoneState= " + this.mPhoneState + ", batteryLevel= " + this.mBatteryLevel);
            }
            return this.mAlwaysOnEnabled && IS_SUPPORT_ALWAYS_ON && !isInBrickMode && currentUser == 0 && !isPowerSaveMode && this.mPhoneState == 0 && this.mBatteryLevel >= 5;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        String lineSeparator = System.lineSeparator();
        sb.append("    isAlwaysOnEnabled=" + this.mAlwaysOnEnabled + lineSeparator);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("    isInBrickMode=");
        OpSceneModeObserver opSceneModeObserver = this.mOpSceneModeObserver;
        sb2.append(opSceneModeObserver != null ? Boolean.valueOf(opSceneModeObserver.isInBrickMode()) : "null");
        sb2.append(lineSeparator);
        sb.append(sb2.toString());
        sb.append("    isPowerSaveMode=" + this.mPowerManager.isPowerSaveMode() + lineSeparator);
        sb.append("    PhoneState=" + this.mPhoneState + lineSeparator);
        return sb.toString();
    }

    /* access modifiers changed from: private */
    public class OpAodAlwaysOnSettingObserver extends ContentObserver {
        private Context mContext;

        public OpAodAlwaysOnSettingObserver(Context context, Handler handler) {
            super(handler);
            this.mContext = context;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            super.onChange(z);
            boolean z2 = true;
            OpAodAlwaysOnController.this.mAlwaysOnEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "doze_always_on", 0, 0) == 1;
            if (Settings.Global.getInt(this.mContext.getContentResolver(), "low_power", 0) != 1) {
                z2 = false;
            }
            boolean isInteractive = OpAodAlwaysOnController.this.mPowerManager.isInteractive();
            boolean isScreenOn = OpAodAlwaysOnController.this.mKeyguardUpdateMonitor.isScreenOn();
            Log.d("AlwaysOnSettingsObserver", "mAlwaysOnEnabled changed= " + OpAodAlwaysOnController.this.mAlwaysOnEnabled + ", isPowerSaveEnabled = " + z2 + ", isInteractive = " + OpAodAlwaysOnController.this.mPowerManager.isInteractive() + ", isScreenOn = " + OpAodAlwaysOnController.this.mKeyguardUpdateMonitor.isScreenOn());
            if (!isInteractive && isScreenOn && z2 && OpAodAlwaysOnController.this.mAlwaysOnEnabled) {
                OpLsState.getInstance().getPhoneStatusBar().onAlwaysOnEnableChanged(false);
            }
        }
    }

    /* access modifiers changed from: private */
    public class OpAodAlwaysOnKeyguardUpdateMonitorCallback extends KeyguardUpdateMonitorCallback {
        private OpAodAlwaysOnKeyguardUpdateMonitorCallback() {
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onPhoneStateChanged(int i) {
            OpAodAlwaysOnController.this.mPhoneState = i;
            Log.d("AodAlwaysOnController", "phone state changed= " + OpAodAlwaysOnController.this.mPhoneState);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onRefreshBatteryInfo(OpBatteryStatus opBatteryStatus) {
            if (opBatteryStatus != null) {
                if (OpAodAlwaysOnController.this.isAlwaysOnEnabled() && OpAodAlwaysOnController.this.mKeyguardUpdateMonitor.isDreaming() && OpAodAlwaysOnController.this.mKeyguardUpdateMonitor.isScreenOn() && opBatteryStatus.level < 5) {
                    OpLsState.getInstance().getPhoneStatusBar().onAlwaysOnEnableChanged(false);
                }
                OpAodAlwaysOnController.this.mBatteryLevel = opBatteryStatus.level;
                Log.d("AodAlwaysOnController", "onRefreshBatteryInfo: " + OpAodAlwaysOnController.this.mBatteryLevel);
            }
        }
    }

    @Override // com.oneplus.scene.OpSceneModeObserver.Callback
    public void onBrickModeChanged() {
        if (IS_SUPPORT_ALWAYS_ON) {
            OpSceneModeObserver opSceneModeObserver = this.mOpSceneModeObserver;
            boolean isInBrickMode = opSceneModeObserver != null ? opSceneModeObserver.isInBrickMode() : false;
            boolean z = !this.mKeyguardUpdateMonitor.isScreenOn();
            boolean isInteractive = this.mPowerManager.isInteractive();
            boolean isAlwaysOnEnabled = isAlwaysOnEnabled();
            Log.i("AodAlwaysOnController", "inBrickMode = " + isInBrickMode + ", isScreenOff = " + z + ", isInteractive = " + isInteractive + ", isAlwaysOn = " + isAlwaysOnEnabled);
            OpLsState instance = OpLsState.getInstance();
            if (instance == null || instance.getPhoneStatusBar() == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("onBrickModeChanged= lsState: ");
                sb.append(instance);
                sb.append(", PhoneStatusBar: ");
                sb.append(instance == null ? "null" : instance.getPhoneStatusBar());
                Log.w("AodAlwaysOnController", sb.toString());
            } else if (z && !isInteractive && isAlwaysOnEnabled) {
                Log.i("AodAlwaysOnController", "Trigger AOD always on");
                OpLsState.getInstance().getPhoneStatusBar().onAlwaysOnEnableChanged(true);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setAlwaysOnState(boolean z) {
        Log.i("AodAlwaysOnController", "setAlwaysOnState = " + z + ", callers = " + Debug.getCallers(2));
        this.mAlwaysOnEnabled = z;
    }
}
