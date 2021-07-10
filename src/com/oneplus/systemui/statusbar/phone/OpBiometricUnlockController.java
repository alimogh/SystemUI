package com.oneplus.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.content.Context;
import android.hardware.biometrics.BiometricSourceType;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.KeyguardViewController;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.phone.ShadeController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarWindowController;
import com.oneplus.battery.OpChargingAnimationController;
import com.oneplus.keyguard.OpKeyguardUpdateMonitor;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.keyguard.OpKeyguardViewMediator;
import com.oneplus.systemui.util.OpMdmLogger;
import com.oneplus.util.OpReflectionUtils;
import com.oneplus.util.OpUtils;
public class OpBiometricUnlockController extends KeyguardUpdateMonitorCallback {
    private static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    private static int FP_FAILED_ATTEMPTS_TO_WAKEUP = 3;
    private static int FP_FAILED_ATTEMPTS_TO_WAKEUP_IN_DOZE = 1;
    private final boolean IS_SUPPORT_CUSTOM_FINGERPRINT = OpUtils.isCustomFingerprint();
    private int mFaceLockMode = 0;
    private boolean mForceShowBouncer = false;
    private boolean mIsFingerprintAuthenticating = false;
    protected boolean mIsScreenOffUnlock;
    private final KeyguardViewMediator mKeyguardViewMediator;
    private boolean mNoBouncerAnim = false;
    private final PowerManager mPowerManager;
    private boolean mSlowAnimateCollapsePanels = false;
    private final StatusBar mStatusBar;
    private final KeyguardUpdateMonitor mUpdateMonitor = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class));

    /* access modifiers changed from: protected */
    public abstract ShadeController getShadeController();

    public void resetSpeedUpPolicy() {
    }

    public OpBiometricUnlockController(Context context, KeyguardViewMediator keyguardViewMediator, StatusBar statusBar) {
        this.mKeyguardViewMediator = keyguardViewMediator;
        this.mPowerManager = (PowerManager) context.getSystemService(PowerManager.class);
        StatusBarWindowController statusBarWindowController = (StatusBarWindowController) Dependency.get(StatusBarWindowController.class);
        this.mStatusBar = statusBar;
    }

    /* access modifiers changed from: protected */
    public void opShowBouncer() {
        this.mNoBouncerAnim = false;
        if (this.IS_SUPPORT_CUSTOM_FINGERPRINT && this.mForceShowBouncer) {
            this.mNoBouncerAnim = true;
        }
        this.mNoBouncerAnim = false;
        if (calculateMode(BiometricSourceType.FINGERPRINT, true) == 3) {
            getKeyguardViewController().showBouncer(false);
        }
        if (!getKeyguardViewController().isBouncerShowing()) {
            float f = 1.1f;
            if (this.mSlowAnimateCollapsePanels) {
                f = 0.5f;
            } else if (this.mNoBouncerAnim) {
                f = 999.0f;
            }
            Log.i("OpBiometricUnlockController", "opShowBouncer, speedUpFactor:" + f);
            animateCollapsePanels(f);
        }
        this.mSlowAnimateCollapsePanels = false;
        this.mNoBouncerAnim = false;
        setPendingShowBouncer(false);
        this.mForceShowBouncer = false;
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onScreenTurnedOn() {
        this.mIsScreenOffUnlock = false;
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onScreenTurnedOff() {
        this.mUpdateMonitor.setWakingUpReason(null);
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onStartedWakingUp() {
        if (isFingerprintAuthenticating() && !this.mIsScreenOffUnlock && OpUtils.isCustomFingerprint()) {
            Log.d("OpBiometricUnlockController", "reset panel and fp state due to waking up during fingerprint authenticating");
            changePanelVisibilityByAlpha(1, true);
            setFingerprintState(false, 0);
        }
    }

    private void onFingerprintUnlockStart() {
        boolean isFinishedScreenTuredOn = OpLsState.getInstance().isFinishedScreenTuredOn();
        boolean isShowingLiveWallpaper = getKeyguardViewController().isShowingLiveWallpaper(true);
        boolean isDreaming = this.mUpdateMonitor.isDreaming();
        boolean isInteractive = this.mPowerManager.isInteractive();
        if (DEBUG) {
            Log.d("OpBiometricUnlockController", "onFingerprintUnlockStart, screenOn:" + isFinishedScreenTuredOn + " , dream:" + isDreaming + " , live:" + isShowingLiveWallpaper + ", interactive: " + isInteractive);
        }
        if (!isFinishedScreenTuredOn && !isShowingLiveWallpaper) {
            if (this.mKeyguardViewMediator.isShowingAndNotOccluded()) {
                changePanelVisibilityByAlpha(0, false);
            }
            this.mUpdateMonitor.setWakingUpReason("com.android.systemui:UnlockStart");
            this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:UnlockStart");
            this.mIsScreenOffUnlock = true;
        } else if (isFinishedScreenTuredOn && !isInteractive && this.mKeyguardViewMediator.isShowingAndNotOccluded()) {
            if (!isShowingLiveWallpaper) {
                changePanelVisibilityByAlpha(0, false);
            }
            if (!this.mUpdateMonitor.isUserUnlocked()) {
                Log.d("OpBiometricUnlockController", "onFingerprintUnlockStart, screenOn && !interactive, forceHideBouncer");
                if (OpLsState.getInstance().getStatusBarKeyguardViewManager() != null) {
                    OpLsState.getInstance().getStatusBarKeyguardViewManager().forceHideBouncer();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onFingerprintUnlockCancel(int i) {
        boolean userCanSkipBouncer = this.mUpdateMonitor.getUserCanSkipBouncer(KeyguardUpdateMonitor.getCurrentUser());
        int i2 = FP_FAILED_ATTEMPTS_TO_WAKEUP;
        if (this.IS_SUPPORT_CUSTOM_FINGERPRINT) {
            KeyguardUpdateMonitor keyguardUpdateMonitor = this.mUpdateMonitor;
            if (!OpKeyguardUpdateMonitor.IS_SUPPORT_MOTOR_CAMERA && !keyguardUpdateMonitor.isDeviceInteractive() && this.mUpdateMonitor.isUnlockWithFacelockPossible()) {
                i2 = FP_FAILED_ATTEMPTS_TO_WAKEUP_IN_DOZE;
            }
        }
        Log.d("OpBiometricUnlockController", "onFingerprintUnlockCancel: Reason:" + i + ", OffUnlock:" + this.mIsScreenOffUnlock + ", attemps:" + this.mUpdateMonitor.getFingerprintFailedUnlockAttempts() + ", Authenticating:" + isFingerprintAuthenticating() + ", prevent:" + OpLsState.getInstance().getPreventModeCtrl().isPreventModeActive() + ", skip:" + userCanSkipBouncer + ", ScreenOn:" + this.mUpdateMonitor.isScreenOn() + ", interactive:" + this.mUpdateMonitor.isDeviceInteractive() + ", bouncer:" + getKeyguardViewController().isBouncerShowing() + ", threshold:" + i2 + ", motor:" + OpKeyguardUpdateMonitor.IS_SUPPORT_MOTOR_CAMERA);
        if (this.IS_SUPPORT_CUSTOM_FINGERPRINT && i == 0 && !this.mUpdateMonitor.isDeviceInteractive() && !getKeyguardViewController().isShowing()) {
            this.mKeyguardViewMediator.doKeyguardTimeout(null);
        }
        if (isFingerprintAuthenticating()) {
            changePanelVisibilityByAlpha(1, false);
            if (i == 0 && this.mUpdateMonitor.getFingerprintFailedUnlockAttempts() >= i2) {
                if (!userCanSkipBouncer && !OpLsState.getInstance().getPreventModeCtrl().isPreventModeActive()) {
                    KeyguardUpdateMonitor keyguardUpdateMonitor2 = this.mUpdateMonitor;
                    if (OpKeyguardUpdateMonitor.IS_SUPPORT_MOTOR_CAMERA) {
                        if (keyguardUpdateMonitor2.isDeviceInteractive()) {
                            getKeyguardViewController().showBouncer(false);
                            if (!this.mStatusBar.isBouncerShowing()) {
                                animateCollapsePanels(1.1f);
                            }
                        } else if (!this.mUpdateMonitor.isDeviceInteractive()) {
                            this.mForceShowBouncer = true;
                            setPendingShowBouncer(true);
                        }
                    } else if (!keyguardUpdateMonitor2.isScreenOn() || this.mUpdateMonitor.isDeviceInteractive()) {
                        if (!this.mStatusBar.isBouncerShowing()) {
                            animateCollapsePanels(1.1f);
                        }
                        getKeyguardViewController().showBouncer(false);
                    } else {
                        if (this.mUpdateMonitor.isAlwaysOnEnabled()) {
                            this.mSlowAnimateCollapsePanels = true;
                        }
                        this.mForceShowBouncer = true;
                        setPendingShowBouncer(true);
                    }
                }
                this.mUpdateMonitor.setWakingUpReason("com.android.systemui:FailedAttempts");
                this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:FailedAttempts");
                if (!this.IS_SUPPORT_CUSTOM_FINGERPRINT) {
                    if (this.mIsScreenOffUnlock) {
                        this.mStatusBar.getFacelockController().tryToStartFaceLockAfterScreenOn();
                    }
                } else if (!getKeyguardViewController().isBouncerShowing()) {
                    this.mStatusBar.getFacelockController().tryToStartFaceLockAfterScreenOn();
                }
            } else if (this.mIsScreenOffUnlock) {
                this.mPowerManager.goToSleep(SystemClock.uptimeMillis(), 11, 0);
                this.mStatusBar.getFacelockController().onPreStartedGoingToSleep();
                OpLsState.getInstance().onFingerprintStartedGoingToSleep();
            }
        }
        setFingerprintState(false, 5);
    }

    @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
    public void onFingerprintAcquired(int i) {
        Log.d("OpBiometricUnlockController", "onFingerprintAcquired: accquireInfo=" + i);
        releaseBiometricWakeLock();
        boolean isInteractive = this.mPowerManager.isInteractive();
        Log.i("OpBiometricUnlockController", "isCustomFingerprint = " + OpUtils.isCustomFingerprint() + " , acquireInfo = " + i + " , isInteractive = " + isInteractive + " , isHomeApp = " + OpUtils.isHomeApp() + " , mUpdateMonitor.isUnlockingWithBiometricAllowed() = " + this.mUpdateMonitor.isUnlockingWithBiometricAllowed() + " , OpLsState.getInstance().isFinishedScreenTuredOn() = " + OpLsState.getInstance().isFinishedScreenTuredOn() + " , mUpdateMonitor.isDeviceInteractive() = " + this.mUpdateMonitor.isDeviceInteractive());
        if (OpUtils.isCustomFingerprint() && !OpLsState.getInstance().isFinishedScreenTuredOn()) {
            Log.d("OpBiometricUnlockController", "don't deal with event if screen does not turne on");
        } else if (i == 6) {
            if (!this.mUpdateMonitor.isUnlockingWithBiometricAllowed()) {
                Log.d("OpBiometricUnlockController", "not allow unlock with biometric");
                this.mUpdateMonitor.setWakingUpReason("com.android.systemui:onAcquired");
                this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:onAcquired");
                return;
            }
            if (OpUtils.isCustomFingerprint()) {
                if (isInteractive) {
                    this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
                } else {
                    this.mStatusBar.onFingerprintPoke();
                }
                if (isInteractive || OpUtils.isHomeApp()) {
                    setFingerprintState(true, 7);
                } else {
                    setFingerprintState(true, 1);
                }
            } else if (!isInteractive) {
                setFingerprintState(true, 1);
            } else {
                setFingerprintState(true, 7);
            }
            onFingerprintUnlockStart();
        } else if (!this.mUpdateMonitor.isDeviceInteractive()) {
            OpLsState.getInstance().getBiometricUnlockController().acquireWakeLock();
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onBiometricHelp(int i, String str, BiometricSourceType biometricSourceType) {
        if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
            Log.d("OpBiometricUnlockController", "onFingerprintHelp: msgId = " + i + ", helpString = " + str);
            if (i != -1) {
                onFingerprintUnlockCancel(1);
            }
        }
        cleanup();
    }

    /* access modifiers changed from: protected */
    public void onFingerprintAuthFailed() {
        Log.d("OpBiometricUnlockController", "onFingerprintAuthFailed: " + this.mUpdateMonitor.getFingerprintFailedUnlockAttempts() + ", " + isFingerprintAuthenticating() + ", " + OpLsState.getInstance().isFinishedScreenTuredOn());
        if (this.mPowerManager.isInteractive()) {
            OpMdmLogger.log("lock_unlock_failed", "finger", "1");
        } else {
            OpMdmLogger.log("lock_unlock_failed", "finger", "0");
        }
        onFingerprintUnlockCancel(0);
    }

    @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
    public void onFingerprintTimeout() {
        onFingerprintUnlockCancel(3);
        cleanup();
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onKeyguardVisibilityChanged(boolean z) {
        Log.d("OpBiometricUnlockController", "onKeyguardVisibilityChanged: " + z + " , " + this.mIsFingerprintAuthenticating);
        if (!z || !this.mIsFingerprintAuthenticating) {
            changePanelVisibilityByAlpha(1, true);
        } else {
            changePanelVisibilityByAlpha(0, false);
        }
    }

    /* access modifiers changed from: protected */
    public void setFingerprintState(boolean z, int i) {
        if (this.mIsFingerprintAuthenticating != z) {
            Log.d("OpBiometricUnlockController", "setFingerprintState: " + z + ", result: " + i);
        }
        this.mIsFingerprintAuthenticating = z;
        this.mKeyguardViewMediator.notifyScreenOffAuthenticate(z, OpKeyguardViewMediator.AUTHENTICATE_FINGERPRINT, i);
    }

    public boolean isFingerprintAuthenticating() {
        return this.mIsFingerprintAuthenticating;
    }

    /* access modifiers changed from: protected */
    public void changePanelVisibilityByAlpha(int i, boolean z) {
        Log.d("OpBiometricUnlockController", "changePanelVisibilityByAlpha: alpha= " + i + ", reset= " + z);
        if (!this.mUpdateMonitor.isAlwaysOnEnabled() || i != 1 || !((OpChargingAnimationController) Dependency.get(OpChargingAnimationController.class)).isAnimationStarted()) {
            if (this.mUpdateMonitor.isUserUnlocked() || this.mStatusBar.isKeyguardShowing() || i != 1) {
                if (z) {
                    this.mKeyguardViewMediator.changePanelAlpha(i, OpKeyguardViewMediator.AUTHENTICATE_IGNORE);
                } else {
                    this.mKeyguardViewMediator.changePanelAlpha(i, OpKeyguardViewMediator.AUTHENTICATE_FINGERPRINT);
                }
            } else if (Build.DEBUG_ONEPLUS) {
                Log.d("OpBiometricUnlockController", "keyguard not showing and alpha == 1, return");
            }
        } else if (Build.DEBUG_ONEPLUS) {
            Log.d("OpBiometricUnlockController", "changePanelVisibilityByAlpha: charging animation is not yet finished.");
        }
    }

    public boolean shouldApplySpeedUpPolicy() {
        return this.IS_SUPPORT_CUSTOM_FINGERPRINT && !getKeyguardViewController().isShowingLiveWallpaper(true);
    }

    public void opResetMode() {
        resetMode();
    }

    public void setFaceLockMode(int i) {
        this.mFaceLockMode = i;
    }

    public int getFaceLockMode() {
        return this.mFaceLockMode;
    }

    public void startWakeAndUnlockForFace(int i) {
        setFaceLockMode(i);
        boolean isDeviceInteractive = this.mUpdateMonitor.isDeviceInteractive();
        Log.d("OpBiometricUnlockController", "startWakeAndUnlockForFace:" + i + ", " + isDeviceInteractive);
        if (i == 1) {
            this.mKeyguardViewMediator.onWakeAndUnlocking(isLauncherOnTop());
            if (this.mStatusBar.getNavigationBarView() != null) {
                this.mStatusBar.getNavigationBarView().setWakeAndUnlocking(true);
            }
            Trace.endSection();
        } else if (i != 5) {
            if (i == 8) {
                getKeyguardViewController().notifyKeyguardAuthenticated(true);
            }
        } else if (!isDeviceInteractive) {
            setPendingShowBouncer(true);
        } else {
            opShowBouncer();
        }
        this.mStatusBar.notifyBiometricAuthModeChanged();
    }

    private boolean isLauncherOnTop() {
        try {
            int activityType = ((ActivityManager.RunningTaskInfo) ActivityManager.getService().getTasks(1).get(0)).configuration.windowConfiguration.getActivityType();
            Log.d("OpBiometricUnlockController", "isLauncherOnTop: " + activityType);
            return activityType == 2;
        } catch (Exception e) {
            Log.w("OpBiometricUnlockController", "Exception e = " + e.toString());
        }
    }

    /* access modifiers changed from: protected */
    public boolean opIsBiometricUnlock() {
        KeyguardUpdateMonitor keyguardUpdateMonitor;
        return isWakeAndUnlock() || getMode() == 5 || getMode() == 7 || ((keyguardUpdateMonitor = this.mUpdateMonitor) != null && keyguardUpdateMonitor.isFacelockUnlocking());
    }

    private int calculateMode(BiometricSourceType biometricSourceType, boolean z) {
        return ((Integer) OpReflectionUtils.methodInvokeWithArgs(this, OpReflectionUtils.getMethodWithParams(BiometricUnlockController.class, "calculateMode", BiometricSourceType.class, Boolean.TYPE), biometricSourceType, Boolean.valueOf(z))).intValue();
    }

    private void setPendingShowBouncer(boolean z) {
        OpReflectionUtils.setValue(BiometricUnlockController.class, this, "mPendingShowBouncer", Boolean.valueOf(z));
    }

    private KeyguardViewController getKeyguardViewController() {
        return (KeyguardViewController) OpReflectionUtils.getValue(BiometricUnlockController.class, this, "mKeyguardViewController");
    }

    private void cleanup() {
        OpReflectionUtils.methodInvokeVoid(BiometricUnlockController.class, this, "cleanup", new Object[0]);
    }

    private void resetMode() {
        OpReflectionUtils.methodInvokeVoid(BiometricUnlockController.class, this, "resetMode", new Object[0]);
    }

    private void releaseBiometricWakeLock() {
        OpReflectionUtils.methodInvokeVoid(BiometricUnlockController.class, this, "releaseBiometricWakeLock", new Object[0]);
    }

    private int getMode() {
        return ((Integer) OpReflectionUtils.getValue(BiometricUnlockController.class, this, "mMode")).intValue();
    }

    private boolean isWakeAndUnlock() {
        return ((Boolean) OpReflectionUtils.methodInvokeVoid(BiometricUnlockController.class, this, "isWakeAndUnlock", new Object[0])).booleanValue();
    }

    private void animateCollapsePanels(float f) {
        if (getShadeController() != null) {
            getShadeController().animateCollapsePanels(0, true, false, f);
        } else {
            Log.e("OpBiometricUnlockController", "animateCollapsePanels: ShadeController is null");
        }
    }
}
