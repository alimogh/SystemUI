package com.oneplus.systemui.statusbar.phone;

import android.content.res.ColorStateList;
import android.util.Log;
import com.android.keyguard.KeyguardHostView;
import com.android.keyguard.KeyguardPasswordView;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardSecurityView;
import com.android.systemui.statusbar.phone.KeyguardBouncer;
import com.oneplus.util.OpReflectionUtils;
public class OpKeyguardBouncer {
    public void showMessage(String str, ColorStateList colorStateList, int i) {
        if (getKeyguardView() != null) {
            getKeyguardView().showMessage(str, colorStateList, i);
        } else {
            Log.w("OpKeyguardBouncer", "Trying to show message on empty bouncer");
        }
    }

    private KeyguardHostView getKeyguardView() {
        return (KeyguardHostView) OpReflectionUtils.getValue(KeyguardBouncer.class, this, "mKeyguardView");
    }

    public boolean isKeyguardPasswordInputMethodPickerShow() {
        KeyguardSecurityView currentSecurityView = getKeyguardView().getCurrentSecurityView();
        if (currentSecurityView == null || !(currentSecurityView instanceof KeyguardPasswordView)) {
            return false;
        }
        return ((KeyguardPasswordView) currentSecurityView).isInputMethodPickerShown();
    }

    public boolean isSecurityModePassword() {
        return getKeyguardView().getCurrentSecurityMode() == KeyguardSecurityModel.SecurityMode.Password;
    }
}
