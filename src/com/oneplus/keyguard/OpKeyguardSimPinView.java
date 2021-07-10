package com.oneplus.keyguard;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import com.android.keyguard.KeyguardEsimArea;
import com.android.keyguard.KeyguardPinBasedInputView;
import com.android.keyguard.KeyguardSimPinView;
import com.android.systemui.C0013R$plurals;
import com.android.systemui.C0015R$string;
import com.oneplus.util.OpReflectionUtils;
public abstract class OpKeyguardSimPinView extends KeyguardPinBasedInputView {
    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.EmergencyButton.EmergencyButtonCallback
    public boolean isShowEmergencyPanel() {
        return false;
    }

    public OpKeyguardSimPinView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public OpBounerMessageAreaInfo getPinPasswordErrorMessageObject(int i, boolean z) {
        int i2;
        int i3;
        OpBounerMessageAreaInfo opBounerMessageAreaInfo = new OpBounerMessageAreaInfo();
        opBounerMessageAreaInfo.setType(1);
        if (i == 0) {
            opBounerMessageAreaInfo.setDisplayMessage(getContext().getString(C0015R$string.kg_password_wrong_pin_code_pukked));
        } else if (i <= 0) {
            opBounerMessageAreaInfo.setDisplayMessage(getContext().getString(z ? C0015R$string.kg_sim_pin_instructions : C0015R$string.kg_password_pin_failed));
        } else if (TelephonyManager.getDefault().getSimCount() > 1) {
            if (z) {
                i3 = C0013R$plurals.kg_password_default_pin_message_multi_sim;
            } else {
                i3 = C0013R$plurals.kg_password_wrong_pin_code_multi_sim;
            }
            opBounerMessageAreaInfo.setDisplayMessage(getContext().getResources().getQuantityString(i3, i, Integer.valueOf(getSlotId() + 1), Integer.valueOf(i)));
            if (i3 == C0013R$plurals.kg_password_default_pin_message_multi_sim) {
                opBounerMessageAreaInfo.setType(0);
            }
        } else {
            if (z) {
                i2 = C0013R$plurals.kg_password_default_pin_message;
            } else {
                i2 = C0013R$plurals.kg_password_wrong_pin_code;
            }
            opBounerMessageAreaInfo.setDisplayMessage(getContext().getResources().getQuantityString(i2, i, Integer.valueOf(i)));
            if (i2 == C0013R$plurals.kg_password_default_pin_message) {
                opBounerMessageAreaInfo.setType(0);
            }
        }
        if (KeyguardEsimArea.isEsimLocked(((LinearLayout) this).mContext, getSubId())) {
            opBounerMessageAreaInfo.setDisplayMessage(getResources().getString(C0015R$string.kg_sim_lock_esim_instructions, opBounerMessageAreaInfo));
        }
        Log.d("OpKeyguardSimPinView", "getPinPasswordErrorMessageObject: attemptsRemaining=" + i + " displayMessage=" + opBounerMessageAreaInfo.toString());
        return opBounerMessageAreaInfo;
    }

    private int getSlotId() {
        return ((Integer) OpReflectionUtils.getValue(KeyguardSimPinView.class, this, "mSlotId")).intValue();
    }

    private int getSubId() {
        return ((Integer) OpReflectionUtils.getValue(KeyguardSimPinView.class, this, "mSubId")).intValue();
    }
}
