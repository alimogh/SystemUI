package com.oneplus.systemui.biometrics;

import android.util.Log;
import android.util.OpFeatures;
import com.oneplus.aod.utils.OpCanvasAodHelper;
import com.oneplus.systemui.biometrics.OpFodDisplayController;
public class OpFodAodControl extends OpFodDisplayController.OpDisplayControl {
    private int mAodMode = 0;
    private final boolean mIsSupportRealAod = OpFeatures.isSupport(new int[]{302});

    public OpFodAodControl(OpFodDisplayController opFodDisplayController) {
        super(opFodDisplayController);
    }

    @Override // com.oneplus.systemui.biometrics.OpFodDisplayController.OpDisplayControl
    public boolean canEnable() {
        if (this.mAodMode == 0 && getUpdateMonitor().getDisplayPowerStatus() == 1) {
            return true;
        }
        return false;
    }

    @Override // com.oneplus.systemui.biometrics.OpFodDisplayController.OpDisplayControl
    public boolean canDisable() {
        return this.mAodMode != 0;
    }

    @Override // com.oneplus.systemui.biometrics.OpFodDisplayController.OpDisplayControl
    public int getAodMode() {
        return this.mAodMode;
    }

    @Override // com.oneplus.systemui.biometrics.OpFodDisplayController.OpDisplayControllerHelper
    public void enableInner(String str) {
        String str2 = this.mTAG;
        Log.d(str2, "set aod mode on, reason: " + str);
        if (!this.mIsSupportRealAod || !getUpdateMonitor().isAlwaysOnEnabled()) {
            this.mAodMode = 2;
        } else {
            this.mAodMode = OpCanvasAodHelper.isCanvasAodEnabled(getContext()) ? 4 : 5;
        }
        String str3 = this.mTAG;
        Log.i(str3, "notify aod value = " + this.mAodMode);
        getNotifier().notifyAodMode(this.mAodMode);
    }

    @Override // com.oneplus.systemui.biometrics.OpFodDisplayController.OpDisplayControllerHelper
    public void disableInner(String str) {
        String str2 = this.mTAG;
        Log.d(str2, "set aod mode off, reason: " + str);
        this.mAodMode = 0;
        getNotifier().notifyAodMode(0);
    }

    public void resetState(String str) {
        String str2 = this.mTAG;
        Log.d(str2, "reset aod state, reason: " + str);
        this.mAodMode = 0;
    }

    /* access modifiers changed from: protected */
    public void adjustBrightness(boolean z) {
        int i = this.mAodMode;
        if (i == 5 || i == 4) {
            getNotifier().notifyAodMode(z ? 1 : 3);
        }
    }

    /* access modifiers changed from: protected */
    public void onAlwaysOnEnableChanged(boolean z) {
        Log.d(this.mTAG, "onAlwaysOnEnableChanged: active= " + z + ", mode= " + this.mAodMode);
        int i = this.mAodMode;
        int i2 = 4;
        if ((i == 5 || i == 4) && !z) {
            this.mAodMode = 2;
        } else if (this.mAodMode == 2 && z) {
            if (!OpCanvasAodHelper.isCanvasAodEnabled(getContext())) {
                i2 = 5;
            }
            this.mAodMode = i2;
        }
        getNotifier().notifyAodMode(this.mAodMode);
    }
}
