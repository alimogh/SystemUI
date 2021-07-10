package com.oneplus.keyguard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.android.keyguard.KeyguardPatternView;
import com.android.keyguard.SecurityMessageDisplay;
import com.oneplus.util.OpReflectionUtils;
public class OpKeyguardPatternView extends LinearLayout {
    public OpKeyguardPatternView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void showMessage(CharSequence charSequence, ColorStateList colorStateList, int i) {
        if (getSecurityMessageDisplay() != null) {
            getSecurityMessageDisplay().setNextMessageColor(colorStateList);
            getSecurityMessageDisplay().setMessage(charSequence, i);
        }
    }

    private SecurityMessageDisplay getSecurityMessageDisplay() {
        return (SecurityMessageDisplay) OpReflectionUtils.getValue(KeyguardPatternView.class, this, "mSecurityMessageDisplay");
    }
}
