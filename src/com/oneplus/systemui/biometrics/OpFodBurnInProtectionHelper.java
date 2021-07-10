package com.oneplus.systemui.biometrics;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.oneplus.aod.OpAodDisplayViewManager;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.biometrics.OpFodHelper;
import com.oneplus.util.OpUtils;
import java.util.concurrent.atomic.AtomicBoolean;
public class OpFodBurnInProtectionHelper implements OpFodHelper.OnFingerprintStateChangeListener {
    private Runnable mCheckRunnable = new Runnable() { // from class: com.oneplus.systemui.biometrics.OpFodBurnInProtectionHelper.3
        @Override // java.lang.Runnable
        public void run() {
            boolean isAodMode = OpFodBurnInProtectionHelper.this.isAodMode();
            boolean needProtection = OpFodBurnInProtectionHelper.this.needProtection();
            Log.d("OpFodBurnInProtectionHelper", "checking: isAodMode= " + isAodMode + ", needProtection= " + needProtection);
            if (!isAodMode || !needProtection) {
                Log.d("OpFodBurnInProtectionHelper", "checking: not needed");
                OpFodBurnInProtectionHelper.this.releaseWakeLock();
                return;
            }
            OpFodBurnInProtectionHelper.this.mShouldHiddenFod.set(true);
            OpFodBurnInProtectionHelper.this.updateUI();
            OpFodBurnInProtectionHelper.this.releaseWakeLock();
        }
    };
    private OpFingerprintDialogView mFodDialogView;
    private Handler mHandler;
    private PowerManager mPowerManager;
    private AtomicBoolean mShouldHiddenFod = new AtomicBoolean(false);
    private KeyguardUpdateMonitor mUpdateMonitor;
    private KeyguardUpdateMonitorCallback mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.oneplus.systemui.biometrics.OpFodBurnInProtectionHelper.2
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStartedWakingUp() {
            super.onStartedWakingUp();
            Log.d("OpFodBurnInProtectionHelper", "onStartedWakingUp");
            OpFodBurnInProtectionHelper.this.mShouldHiddenFod.set(false);
            OpFodBurnInProtectionHelper.this.updateUI();
        }

        @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
        public void onAlwaysOnEnableChanged(boolean z) {
            super.onAlwaysOnEnableChanged(z);
            Log.d("OpFodBurnInProtectionHelper", "onAlwaysOnEnableChanged: active= " + z);
            if (!z) {
                OpFodBurnInProtectionHelper.this.mShouldHiddenFod.set(false);
                OpFodBurnInProtectionHelper.this.updateUI();
            }
        }
    };
    private PowerManager.WakeLock mWakeLock;

    public OpFodBurnInProtectionHelper(Context context, KeyguardUpdateMonitor keyguardUpdateMonitor) {
        this.mUpdateMonitor = keyguardUpdateMonitor;
        this.mHandler = new Handler(Looper.getMainLooper()) { // from class: com.oneplus.systemui.biometrics.OpFodBurnInProtectionHelper.1
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                if (message.what == 1000) {
                    OpFodBurnInProtectionHelper.this.handleStartSchedule((String) message.obj);
                }
            }
        };
        PowerManager powerManager = (PowerManager) context.getSystemService(PowerManager.class);
        this.mPowerManager = powerManager;
        this.mWakeLock = powerManager.newWakeLock(1, "OpFodBurnInProtectionHelper#protect");
    }

    @Override // com.oneplus.systemui.biometrics.OpFodHelper.OnFingerprintStateChangeListener
    public void onFingerprintStateChanged() {
        if (OpFodHelper.getInstance().isKeyguardClient()) {
            startSchedule("fingerprint state changed");
        }
    }

    public void setFodDialogView(OpFingerprintDialogView opFingerprintDialogView) {
        OpFodHelper.getInstance().addFingerprintStateChangeListener(this);
        this.mFodDialogView = opFingerprintDialogView;
        this.mUpdateMonitor.registerCallback(this.mUpdateMonitorCallback);
    }

    public void startSchedule(String str) {
        Log.d("OpFodBurnInProtectionHelper", "startSchedule: reason= " + str);
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.removeMessages(1000);
            Message obtainMessage = this.mHandler.obtainMessage();
            obtainMessage.what = 1000;
            obtainMessage.obj = str;
            this.mHandler.sendMessage(obtainMessage);
        }
    }

    public void handleStartSchedule(String str) {
        boolean isAodMode = isAodMode();
        boolean needProtection = needProtection();
        Log.d("OpFodBurnInProtectionHelper", "handleStartSchedule: reason= " + str + ", isAodMode= " + isAodMode + ", needProtection= " + needProtection);
        releaseWakeLock();
        this.mHandler.removeCallbacks(this.mCheckRunnable);
        if (!isAodMode || !needProtection) {
            Log.d("OpFodBurnInProtectionHelper", "startSchedule: not needed");
            return;
        }
        if (this.mShouldHiddenFod.get()) {
            this.mShouldHiddenFod.set(false);
            updateUI();
        }
        acquireWakeLock();
        this.mHandler.postDelayed(this.mCheckRunnable, (long) getDelayTime());
    }

    public boolean isFodHiddenOnAod() {
        return this.mShouldHiddenFod.get();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateUI() {
        OpFingerprintDialogView opFingerprintDialogView = this.mFodDialogView;
        if (opFingerprintDialogView != null) {
            opFingerprintDialogView.updateIconVisibility(false);
        }
        OpAodDisplayViewManager aodDisplayViewManager = OpLsState.getInstance().getPhoneStatusBar().getAodDisplayViewManager();
        if (aodDisplayViewManager != null) {
            aodDisplayViewManager.onFodShowOrHideOnAod(!this.mShouldHiddenFod.get());
        }
    }

    private int getDelayTime() {
        int i = SystemProperties.getInt("sys.fod.burnin.delay", 0);
        if (i == 0) {
            return 3000;
        }
        Log.d("OpFodBurnInProtectionHelper", "getDelayTime: override to " + i + " ms");
        return i;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isAodMode() {
        return !this.mUpdateMonitor.isDeviceInteractive() && (this.mUpdateMonitor.isScreenTurningOn() || this.mUpdateMonitor.isScreenOn());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean needProtection() {
        if (!OpUtils.isCustomFingerprint()) {
            return false;
        }
        boolean isAlwaysOnEnabled = this.mUpdateMonitor.isAlwaysOnEnabled();
        boolean isKeyguardClient = OpFodHelper.getInstance().isKeyguardClient();
        boolean z = !OpFodHelper.getInstance().isFingerprintStopped();
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpFodBurnInProtectionHelper", "needProtection: isAlwaysOnEnabled= " + isAlwaysOnEnabled + ", isKeyguardClient= " + isKeyguardClient + ", fodOnScreen= " + z);
        }
        if (!isAlwaysOnEnabled || !isKeyguardClient || !z) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void releaseWakeLock() {
        if (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    private void acquireWakeLock() {
        if (!this.mWakeLock.isHeld()) {
            this.mWakeLock.acquire();
        }
    }
}
