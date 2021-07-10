package com.oneplus.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Handler;
import com.android.systemui.statusbar.phone.KeyguardBouncer;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.oneplus.onlineconfig.OpFingerprintConfig;
import com.oneplus.util.OpReflectionUtils;
public class OpStatusBarKeyguardViewManager {
    public OpStatusBarKeyguardViewManager() {
        new Handler();
    }

    /* access modifiers changed from: protected */
    public void init(Context context) {
        new OpFingerprintConfig(context);
    }

    public void showBouncerMessage(String str, ColorStateList colorStateList, int i) {
        if (getBouncer() != null) {
            getBouncer().showMessage(str, colorStateList, i);
        }
    }

    private KeyguardBouncer getBouncer() {
        return (KeyguardBouncer) OpReflectionUtils.getValue(StatusBarKeyguardViewManager.class, this, "mBouncer");
    }
}
