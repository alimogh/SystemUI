package com.oneplus.systemui.biometrics;

import android.content.Context;
import android.hardware.biometrics.BiometricSourceType;
import android.os.IPowerManager;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.Dependency;
import com.oneplus.aod.utils.OpCanvasAodHelper;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.biometrics.OpFodHelper;
public class OpFodDisplayController extends KeyguardUpdateMonitorCallback implements OpFodHelper.OnFingerprintStateChangeListener {
    OpFodAodControl mAodControl;
    Context mContext;
    private boolean mDelayDisAodAtFinishedWake;
    OpFodDimControl mDimControl;
    OpFodDisplayNotifier mDisplayNotifier;
    boolean mFaceUnlocked;
    private boolean mHasRecognizeResult = false;
    OpFodHighlightControl mHighlightControl;
    IPowerManager mIPowerManager;
    private boolean mIsInAlwaysOnState = false;
    PowerManager mPm;
    KeyguardUpdateMonitor mUpdateMonitor;

    public interface OpDisplayControllerHelper {
        void disableInner(String str);

        void enableInner(String str);
    }

    public OpFodDisplayController(Context context) {
        this.mContext = context;
        this.mPm = (PowerManager) context.getSystemService("power");
        this.mIPowerManager = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
        this.mDisplayNotifier = new OpFodDisplayNotifier(context);
        this.mHighlightControl = new OpFodHighlightControl(this);
        this.mDimControl = new OpFodDimControl(this);
        this.mAodControl = new OpFodAodControl(this);
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mUpdateMonitor = keyguardUpdateMonitor;
        keyguardUpdateMonitor.registerCallback(this);
        OpFodHelper.getInstance().addFingerprintStateChangeListener(this);
    }

    @Override // com.oneplus.systemui.biometrics.OpFodHelper.OnFingerprintStateChangeListener
    public void onFingerprintStateChanged() {
        if (OpFodHelper.getInstance().isFingerprintDetecting()) {
            this.mFaceUnlocked = false;
            this.mDimControl.enable("fp register or resume");
        } else if (OpFodHelper.getInstance().isFingerprintLockout()) {
            if (this.mPm.isInteractive()) {
                this.mDimControl.disable("lockout");
            }
        } else if (OpFodHelper.getInstance().isFingerprintSuspended()) {
            this.mDimControl.disable("suspend");
            this.mHighlightControl.disable("suspend");
        } else if (!isInAlwaysOnAod()) {
            this.mDimControl.disable("fp unregister");
        }
    }

    public void resetState() {
        if (!OpFodHelper.getInstance().isFingerprintDetecting()) {
            this.mDimControl.disable("reset state");
            this.mHighlightControl.disable("reset state");
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onKeyguardVisibilityChanged(boolean z) {
        if (z) {
            this.mDimControl.enable("keyguard visibility change to show");
        }
    }

    @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
    public void onFacelockStateChanged(int i) {
        Log.d("OpFodDisplayController", "onFacelockStateChanged: type:" + i);
        if (i == 4) {
            this.mFaceUnlocked = true;
            if (OpFodHelper.getInstance().isKeyguardClient()) {
                this.mDimControl.disable("face unlocked");
            }
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onStartedWakingUp() {
        this.mDisplayNotifier.notifyHideAodMode(0);
        this.mDelayDisAodAtFinishedWake = false;
        if (needToDisableAod()) {
            if (this.mUpdateMonitor.isAlwaysOnEnabled()) {
                this.mDelayDisAodAtFinishedWake = true;
            } else {
                this.mAodControl.disable("start waking up");
            }
        } else if (!isPowerKeyWakeupDeviceInAlwaysOn()) {
            this.mAodControl.resetState("finger recognized with aod always on");
        }
        if (this.mUpdateMonitor.isAlwaysOnEnabled() && OpFodHelper.getInstance().isFingerprintLockout()) {
            this.mDimControl.disable("lockout");
        }
        this.mIsInAlwaysOnState = false;
        this.mHasRecognizeResult = false;
    }

    @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
    public void onFinishedWakingUp() {
        if (OpLsState.getInstance().getBiometricUnlockController().isWakeAndUnlock()) {
            this.mDimControl.disable("finished waking up");
        } else {
            if (isPowerKeyWakeupDeviceInAlwaysOn()) {
                this.mAodControl.disable("finished waking up");
                this.mDelayDisAodAtFinishedWake = false;
            }
            this.mDimControl.enable("finished waking up");
        }
        if (this.mDelayDisAodAtFinishedWake) {
            this.mAodControl.disable("finished waking up delay");
            this.mDimControl.enable("finished waking up delay");
            this.mDelayDisAodAtFinishedWake = false;
        }
        boolean isKeyguardDone = this.mUpdateMonitor.isKeyguardDone();
        boolean isUnlockingWithBiometricAllowed = this.mUpdateMonitor.isUnlockingWithBiometricAllowed();
        boolean isFingerprintLockout = this.mUpdateMonitor.isFingerprintLockout();
        Log.d("OpFodDisplayController", "isFingerprintDetecting " + OpFodHelper.getInstance().isFingerprintDetecting() + " isKeyguardDone " + isKeyguardDone + " isUnlockingWithBiometricAllowed " + isUnlockingWithBiometricAllowed + " isFingerprintLockout " + isFingerprintLockout);
        if (OpFodHelper.getInstance().isFingerprintDetecting()) {
            return;
        }
        if (isKeyguardDone || !isUnlockingWithBiometricAllowed || isFingerprintLockout) {
            this.mDisplayNotifier.forceDisableFingerprintMode();
        }
    }

    private String getWakingUpReason() {
        try {
            return this.mIPowerManager.getWakingUpReason();
        } catch (Exception unused) {
            Log.e("OpFodDisplayController", "can't get waking up reason");
            return null;
        }
    }

    private boolean isPowerKeyWakeupDeviceInAlwaysOn() {
        return this.mUpdateMonitor.isAlwaysOnEnabled() && "android.policy:POWER".equals(getWakingUpReason());
    }

    private boolean needToDisableAod() {
        String wakingUpReason = getWakingUpReason();
        Log.i("OpFodDisplayController", "shouldDisableAod: wakingUpReason= " + wakingUpReason);
        return (!this.mUpdateMonitor.isAlwaysOnEnabled() || !"android.policy:BIOMETRIC".equals(wakingUpReason)) && (!this.mUpdateMonitor.isAlwaysOnEnabled() || !"com.android.systemui:FailedAttempts".equals(wakingUpReason)) && !isPowerKeyWakeupDeviceInAlwaysOn();
    }

    @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
    public void onKeyguardFadedAway() {
        if (!OpFodHelper.getInstance().isEmptyClient() && !OpFodHelper.getInstance().isKeyguardClient()) {
            this.mDimControl.enable("keyguard faded away");
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onStartedGoingToSleep(int i) {
        if (this.mUpdateMonitor.isAlwaysOnEnabled() && !OpCanvasAodHelper.isCanvasAodEnabled(this.mContext)) {
            this.mDimControl.disable("going to sleep");
            this.mDisplayNotifier.notifyHideAodMode(1);
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onFinishedGoingToSleep(int i) {
        this.mFaceUnlocked = false;
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onScreenTurnedOn() {
        this.mDimControl.enable("screen on");
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onScreenTurnedOff() {
        this.mAodControl.enable("screen turned off");
    }

    @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
    public void onKeyguardDoneChanged(boolean z) {
        if (z) {
            this.mDimControl.disable("keyguardDone");
            this.mHighlightControl.disable("keyguardDone");
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onBiometricAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
        if (BiometricSourceType.FINGERPRINT == biometricSourceType) {
            boolean isKeyguardVisible = this.mUpdateMonitor.isKeyguardVisible();
            boolean isKeyguardDone = this.mUpdateMonitor.isKeyguardDone();
            Log.d("OpFodDisplayController", "onBiometricAuthenticated isInteractive:" + this.mPm.isInteractive() + ", isKeyguardVisible:" + isKeyguardVisible + ", isStrongBiometric:" + z + ", isKeyguardDone:" + isKeyguardDone);
            if (!isKeyguardDone && !this.mPm.isInteractive()) {
                this.mAodControl.disable("fp authenticated");
            }
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onBiometricAuthFailed(BiometricSourceType biometricSourceType) {
        if (BiometricSourceType.FINGERPRINT == biometricSourceType) {
            Log.i("OpFodDisplayController", "onBiometricAuthFailed: mIsAlwaysOnState = " + this.mIsInAlwaysOnState + ", failAttempts = " + this.mUpdateMonitor.getFingerprintFailedUnlockAttempts() + ", unlockWithFacelock = " + this.mUpdateMonitor.isUnlockWithFacelockPossible());
            if (this.mIsInAlwaysOnState) {
                this.mHasRecognizeResult = true;
                if (this.mUpdateMonitor.getFingerprintFailedUnlockAttempts() < 3) {
                    this.mHighlightControl.disable("finger press up");
                    if (!this.mUpdateMonitor.isUnlockWithFacelockPossible()) {
                        this.mHighlightControl.changeToAodMode();
                    }
                }
            }
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onBiometricHelp(int i, String str, BiometricSourceType biometricSourceType) {
        if (BiometricSourceType.FINGERPRINT == biometricSourceType) {
            Log.i("OpFodDisplayController", "onBiometricAuthHelp: mReceiveRecognizeResult = " + this.mHasRecognizeResult + ", mIsAlwaysOnState = " + this.mIsInAlwaysOnState + ", failAttempts = " + this.mUpdateMonitor.getFingerprintFailedUnlockAttempts());
            if (!this.mHasRecognizeResult && this.mIsInAlwaysOnState) {
                this.mHighlightControl.changeToAodMode();
            }
            this.mHasRecognizeResult = false;
            this.mIsInAlwaysOnState = false;
        }
    }

    @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
    public void onEnvironmentLightChanged(boolean z) {
        this.mAodControl.adjustBrightness(z);
    }

    @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
    public void onAlwaysOnEnableChanged(boolean z) {
        this.mAodControl.onAlwaysOnEnableChanged(z);
    }

    public void dismiss() {
        this.mDimControl.disable("dismiss");
    }

    public void hideFODDim() {
        this.mDimControl.disable("early hide dim");
    }

    public void notifyFingerprintAuthenticated() {
        if (!this.mUpdateMonitor.isKeyguardDone()) {
            this.mDimControl.setDimState(5, "fp unlock");
        }
    }

    public void onFingerPressDown() {
        this.mHasRecognizeResult = false;
        if (isInAlwaysOnAod()) {
            this.mIsInAlwaysOnState = true;
        }
        if (!this.mUpdateMonitor.isDeviceInteractive()) {
            this.mDimControl.setDimState(2, "fp press");
        }
        this.mHighlightControl.enable("finger press down");
    }

    public void onFingerPressUp() {
        this.mHighlightControl.disable("finger press up");
    }

    public boolean isFodHighlighted() {
        return this.mHighlightControl.isHighlight();
    }

    @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
    public void onDisplayPowerStatusChanged(int i) {
        Log.d("OpFodDisplayController", "onDisplayPowerStatusChanged: status= " + i);
        if (i == 1) {
            this.mDimControl.disable("display power status: off");
        }
    }

    private boolean isInAlwaysOnAod() {
        return !this.mUpdateMonitor.isDeviceInteractive() && this.mUpdateMonitor.isAlwaysOnEnabled();
    }

    public void setDimForFingerprintAcquired() {
        this.mDimControl.disable("FINGERPRINT_ACQUIRED_SET_DIM_LAYER");
    }

    /* access modifiers changed from: package-private */
    public static abstract class OpDisplayControl implements OpDisplayControllerHelper {
        private OpFodDisplayController mController;
        protected final String mTAG = getClass().getSimpleName();

        public abstract boolean canDisable();

        public abstract boolean canEnable();

        public OpDisplayControl(OpFodDisplayController opFodDisplayController) {
            this.mController = opFodDisplayController;
        }

        /* access modifiers changed from: protected */
        public Context getContext() {
            return this.mController.mContext;
        }

        /* access modifiers changed from: protected */
        public OpFodDisplayNotifier getNotifier() {
            return this.mController.mDisplayNotifier;
        }

        /* access modifiers changed from: protected */
        public KeyguardUpdateMonitor getUpdateMonitor() {
            return this.mController.mUpdateMonitor;
        }

        /* access modifiers changed from: protected */
        public PowerManager getPowerManager() {
            return this.mController.mPm;
        }

        /* access modifiers changed from: protected */
        public boolean isFaceUnlocked() {
            return this.mController.mFaceUnlocked;
        }

        /* access modifiers changed from: protected */
        public boolean isHighlight() {
            return this.mController.mHighlightControl.isHighlight();
        }

        /* access modifiers changed from: protected */
        public int getAodMode() {
            return this.mController.mAodControl.getAodMode();
        }

        public boolean enable(String str) {
            String str2 = this.mTAG;
            Log.d(str2, "enable: " + str);
            if (!canEnable()) {
                return false;
            }
            enableInner(str);
            return true;
        }

        public boolean disable(String str) {
            String str2 = this.mTAG;
            Log.d(str2, "disable: " + str);
            if (!canDisable()) {
                return false;
            }
            disableInner(str);
            return true;
        }
    }
}
