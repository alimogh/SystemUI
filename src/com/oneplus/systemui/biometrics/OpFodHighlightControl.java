package com.oneplus.systemui.biometrics;

import android.util.Log;
import com.oneplus.systemui.biometrics.OpFodDisplayController;
public class OpFodHighlightControl extends OpFodDisplayController.OpDisplayControl {
    private boolean mIsHighlight;

    public OpFodHighlightControl(OpFodDisplayController opFodDisplayController) {
        super(opFodDisplayController);
    }

    @Override // com.oneplus.systemui.biometrics.OpFodDisplayController.OpDisplayControl
    public boolean canEnable() {
        if (this.mIsHighlight) {
            Log.d(this.mTAG, "canEnable: press state not correct");
        }
        return !this.mIsHighlight;
    }

    @Override // com.oneplus.systemui.biometrics.OpFodDisplayController.OpDisplayControl
    public boolean canDisable() {
        if (!this.mIsHighlight) {
            Log.d(this.mTAG, "canDisable: press state not correct");
        }
        return this.mIsHighlight;
    }

    @Override // com.oneplus.systemui.biometrics.OpFodDisplayController.OpDisplayControllerHelper
    public void enableInner(String str) {
        this.mIsHighlight = true;
        if (!getPowerManager().isInteractive()) {
            Log.d(this.mTAG, "device is not interactive, let fp sensor to handle it.");
        } else {
            getNotifier().notifyPressMode(1);
        }
    }

    @Override // com.oneplus.systemui.biometrics.OpFodDisplayController.OpDisplayControllerHelper
    public void disableInner(String str) {
        this.mIsHighlight = false;
        getNotifier().notifyPressMode(0);
    }

    @Override // com.oneplus.systemui.biometrics.OpFodDisplayController.OpDisplayControl
    public boolean isHighlight() {
        return this.mIsHighlight;
    }

    public void changeToAodMode() {
        this.mIsHighlight = false;
        String str = this.mTAG;
        Log.d(str, "keyguardDone: " + getUpdateMonitor().isKeyguardDone() + ", isFingerprintUnlock: " + getUpdateMonitor().isFingerprintAlreadyAuthenticated());
        if (!getUpdateMonitor().isFingerprintAlreadyAuthenticated()) {
            getNotifier().notifyPressMode(4);
        } else {
            Log.i(this.mTAG, "can't set change to aod mode, becuase it's keyguard done");
        }
    }
}
