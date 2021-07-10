package com.oneplus.keyguard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.android.keyguard.KeyguardAbsKeyInputView;
import com.android.keyguard.SecurityMessageDisplay;
import com.oneplus.util.OpReflectionUtils;
public class OpKeyguardAbsKeyInputView extends LinearLayout {
    public OpKeyguardAbsKeyInputView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void showMessage(CharSequence charSequence, ColorStateList colorStateList, int i) {
        if (getSecurityMessageDisplay() != null) {
            getSecurityMessageDisplay().setNextMessageColor(colorStateList);
            getSecurityMessageDisplay().setMessage(charSequence, i);
        }
    }

    private SecurityMessageDisplay getSecurityMessageDisplay() {
        return (SecurityMessageDisplay) OpReflectionUtils.getValue(KeyguardAbsKeyInputView.class, this, "mSecurityMessageDisplay");
    }
}
