package com.oneplus.keyguard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.android.keyguard.KeyguardSecurityContainer;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardSecurityView;
import com.android.keyguard.KeyguardSecurityViewFlipper;
import com.oneplus.util.OpReflectionUtils;
public class OpKeyguardSecurityContainer extends FrameLayout {
    public OpKeyguardSecurityContainer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public void showMessage(CharSequence charSequence, ColorStateList colorStateList, int i) {
        if (getCurrentSecuritySelection() != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(getCurrentSecuritySelection()).showMessage(charSequence, colorStateList, i);
        }
    }

    private KeyguardSecurityView getSecurityView(KeyguardSecurityModel.SecurityMode securityMode) {
        return (KeyguardSecurityView) OpReflectionUtils.methodInvokeWithArgs(this, OpReflectionUtils.getMethodWithParams(KeyguardSecurityContainer.class, "getSecurityView", KeyguardSecurityModel.SecurityMode.class), securityMode);
    }

    private KeyguardSecurityModel.SecurityMode getCurrentSecuritySelection() {
        return (KeyguardSecurityModel.SecurityMode) OpReflectionUtils.getValue(KeyguardSecurityContainer.class, this, "mCurrentSecuritySelection");
    }

    private KeyguardSecurityViewFlipper getFlipper() {
        return (KeyguardSecurityViewFlipper) OpReflectionUtils.getValue(KeyguardSecurityContainer.class, this, "mSecurityViewFlipper");
    }

    public void resetFlipperY() {
        if (getFlipper() != null) {
            getFlipper().setTranslationY(0.0f);
        }
    }
}
