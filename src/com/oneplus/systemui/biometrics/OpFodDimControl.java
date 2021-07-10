package com.oneplus.systemui.biometrics;

import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.biometrics.OpFodDisplayController;
public class OpFodDimControl extends OpFodDisplayController.OpDisplayControl {
    private int mDimMode = 0;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager = OpLsState.getInstance().getStatusBarKeyguardViewManager();

    public OpFodDimControl(OpFodDisplayController opFodDisplayController) {
        super(opFodDisplayController);
    }

    @Override // com.oneplus.systemui.biometrics.OpFodDisplayController.OpDisplayControl
    public boolean canEnable() {
        StatusBarKeyguardViewManager statusBarKeyguardViewManager;
        if (this.mDimMode == 1) {
            Log.d(this.mTAG, "don't eanble HBM due to already turn on");
            return false;
        } else if (OpFodHelper.getInstance().isEmptyClient() || OpFodHelper.getInstance().isForceShowClient()) {
            Log.d(this.mTAG, "don't enable HBM due to no one registering fp");
            return false;
        } else if (getUpdateMonitor().isKeyguardDone() && OpFodHelper.getInstance().isKeyguardClient()) {
            Log.d(this.mTAG, "don't re-enable HBM due to fingerprint unlocking");
            return false;
        } else if (getAodMode() != 0) {
            Log.d(this.mTAG, "aod mode is not turn off yet");
            return false;
        } else if (isHighlight()) {
            Log.d(this.mTAG, "force enable HBM since highlight icon is visible");
            return true;
        } else if (getUpdateMonitor().isGoingToSleep()) {
            Log.d(this.mTAG, "don't enable HBM due to going to sleep");
            return false;
        } else if (OpLsState.getInstance().getBiometricUnlockController().isWakeAndUnlock() || (getUpdateMonitor().isKeyguardDone() && OpFodHelper.getInstance().isKeyguardClient())) {
            Log.d(this.mTAG, "don't enable HBM due to duraing fp wake and unlock");
            return false;
        } else if (getUpdateMonitor().isDeviceInteractive() && (statusBarKeyguardViewManager = this.mStatusBarKeyguardViewManager) != null && statusBarKeyguardViewManager.isOccluded() && !this.mStatusBarKeyguardViewManager.isBouncerShowing() && this.mStatusBarKeyguardViewManager.isShowing() && OpFodHelper.getInstance().isKeyguardClient()) {
            Log.d(this.mTAG, "don't enable HBM due to keyguard is occluded and device is interactive");
            return false;
        } else if (!getUpdateMonitor().isDeviceInteractive() && getUpdateMonitor().isScreenOn() && isHighlight()) {
            Log.d(this.mTAG, "force enable HBM in aod and fp is pressed");
            return true;
        } else if (!getUpdateMonitor().isDeviceInteractive()) {
            Log.d(this.mTAG, "don't enable HBM due to device isn't interactive");
            return false;
        } else if (isFaceUnlocked()) {
            Log.d(this.mTAG, "don't enable HBM due to already face unlocked");
            return false;
        } else if (getUpdateMonitor().isFingerprintLockout() || getUpdateMonitor().isUserInLockdown(KeyguardUpdateMonitor.getCurrentUser())) {
            Log.d(this.mTAG, "don't enable HBM due to lockout");
            return false;
        } else if (!getUpdateMonitor().isUnlockingWithBiometricAllowed() && !getUpdateMonitor().isKeyguardDone() && OpFodHelper.getInstance().isKeyguardClient()) {
            Log.d(this.mTAG, "don't enable HBM due to boot device or biometrice doesn't allow");
            return false;
        } else if (getUpdateMonitor().getDisplayPowerStatus() == 2) {
            return true;
        } else {
            Log.d(this.mTAG, "don't enable HBM due to display power mode not ready");
            return false;
        }
    }

    @Override // com.oneplus.systemui.biometrics.OpFodDisplayController.OpDisplayControl
    public boolean canDisable() {
        return this.mDimMode != 0;
    }

    @Override // com.oneplus.systemui.biometrics.OpFodDisplayController.OpDisplayControllerHelper
    public void enableInner(String str) {
        this.mDimMode = 1;
        getNotifier().notifyDisplayDimMode(1, getAodMode());
    }

    @Override // com.oneplus.systemui.biometrics.OpFodDisplayController.OpDisplayControllerHelper
    public void disableInner(String str) {
        this.mDimMode = 0;
        getNotifier().notifyDisplayDimMode(0, getAodMode());
    }

    public void setDimState(int i, String str) {
        String str2 = this.mTAG;
        Log.i(str2, "set dim state " + i + " in local, reason: " + str);
        this.mDimMode = i;
    }
}
