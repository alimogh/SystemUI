package com.oneplus.aod;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0000R$anim;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
public class OpFpAodIndicationText extends TextView {
    private OpAodDisplayViewManager mAodDisplayViewManager;
    private Context mContext;
    private Handler mHandler;
    private View mIndication;
    private LockPatternUtils mLockPatternUtils;
    private KeyguardSecurityModel.SecurityMode mSecurityMode = KeyguardSecurityModel.SecurityMode.None;
    private KeyguardUpdateMonitor mUpdateMonitor;

    public OpFpAodIndicationText(Context context) {
        super(context);
        this.mContext = context;
    }

    public OpFpAodIndicationText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
    }

    public OpFpAodIndicationText(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mContext = context;
    }

    public void init(OpAodDisplayViewManager opAodDisplayViewManager, Handler handler) {
        this.mAodDisplayViewManager = opAodDisplayViewManager;
        this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mHandler = handler;
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mIndication = this;
    }

    public void updateFPIndicationText(boolean z, String str) {
        int i;
        if (this.mAodDisplayViewManager != null) {
            Log.d("OpFpAodIndicationText", "updateFPIndicationText: lockout:" + this.mUpdateMonitor.isFingerprintLockout() + ", " + z + ", " + str);
            if (this.mUpdateMonitor.isFingerprintLockout()) {
                setAodIndicationText(17040221);
                return;
            }
            int i2 = 0;
            if (z) {
                i = 1;
                setAodIndicationText(str);
                animateErrorText();
            } else {
                KeyguardSecurityModel.SecurityMode securityMode = getSecurityMode();
                this.mSecurityMode = securityMode;
                if (securityMode == KeyguardSecurityModel.SecurityMode.Pattern) {
                    i2 = C0015R$string.op_kg_prompt_reason_timeout_pattern;
                } else if (securityMode == KeyguardSecurityModel.SecurityMode.Password) {
                    i2 = C0015R$string.op_kg_prompt_reason_timeout_password;
                } else if (securityMode == KeyguardSecurityModel.SecurityMode.PIN) {
                    i2 = C0015R$string.op_kg_prompt_reason_timeout_pin;
                }
                if (i2 != 0 && !this.mUpdateMonitor.isUnlockingWithBiometricAllowed()) {
                    setAodIndicationText(i2);
                    animateErrorText();
                }
                i = i2;
            }
            Log.d("OpFpAodIndicationText", "updateFPIndicationText: " + this.mSecurityMode + ", " + i);
            if (i == 0) {
                setAodIndicationText("");
            }
        }
    }

    private KeyguardSecurityModel.SecurityMode getSecurityMode() {
        int keyguardStoredPasswordQuality = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(KeyguardUpdateMonitor.getCurrentUser());
        if (keyguardStoredPasswordQuality == 0) {
            return KeyguardSecurityModel.SecurityMode.None;
        }
        if (keyguardStoredPasswordQuality == 65536) {
            return KeyguardSecurityModel.SecurityMode.Pattern;
        }
        if (keyguardStoredPasswordQuality == 131072 || keyguardStoredPasswordQuality == 196608) {
            return KeyguardSecurityModel.SecurityMode.PIN;
        }
        if (keyguardStoredPasswordQuality == 262144 || keyguardStoredPasswordQuality == 327680 || keyguardStoredPasswordQuality == 393216 || keyguardStoredPasswordQuality == 524288) {
            return KeyguardSecurityModel.SecurityMode.Password;
        }
        throw new IllegalStateException("Unknown security quality:" + keyguardStoredPasswordQuality);
    }

    private void setAodIndicationText(int i) {
        setAodIndicationText(this.mContext.getString(i));
    }

    private void setAodIndicationText(final String str) {
        this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.OpFpAodIndicationText.1
            @Override // java.lang.Runnable
            public void run() {
                OpFpAodIndicationText.this.setText(str);
                OpFpAodIndicationText.this.setVisibility(TextUtils.isEmpty(str) ? 8 : 0);
            }
        });
    }

    public void resetState() {
        if (!this.mUpdateMonitor.isFingerprintLockout() && this.mUpdateMonitor.isUnlockingWithBiometricAllowed()) {
            setAodIndicationText("");
        }
    }

    public void animateErrorText() {
        this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.OpFpAodIndicationText.2
            @Override // java.lang.Runnable
            public void run() {
                Animator loadAnimator = AnimatorInflater.loadAnimator(OpFpAodIndicationText.this.mContext, C0000R$anim.oneplus_control_text_error_message_anim);
                if (loadAnimator != null) {
                    loadAnimator.cancel();
                    loadAnimator.setTarget(OpFpAodIndicationText.this.mIndication);
                    loadAnimator.start();
                }
            }
        });
    }

    public void showOrHide(boolean z) {
        final boolean z2 = z && !TextUtils.isEmpty(getText());
        this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.OpFpAodIndicationText.3
            @Override // java.lang.Runnable
            public void run() {
                OpFpAodIndicationText.this.setVisibility(z2 ? 0 : 8);
            }
        });
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        this.mAodDisplayViewManager.onFodIndicationVisibilityChanged(i == 0);
    }
}
