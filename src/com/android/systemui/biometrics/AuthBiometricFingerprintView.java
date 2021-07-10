package com.android.systemui.biometrics;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.LinearLayout;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.biometrics.AuthBiometricView;
import com.oneplus.systemui.biometrics.OpBiometricDialogImpl;
import com.oneplus.systemui.biometrics.OpFodViewSettings;
import com.oneplus.util.OpUtils;
import com.oneplus.util.ThemeColorUtils;
public class AuthBiometricFingerprintView extends AuthBiometricView {
    private boolean mFodShowing;

    private boolean shouldAnimateForTransition(int i, int i2) {
        return (i2 == 1 || i2 == 2) ? i == 4 || i == 3 : i2 == 3 || i2 == 4;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public int getDelayAfterAuthenticatedDurationMs() {
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public int getStateForAfterError() {
        return 2;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public boolean supportsSmallDialog() {
        return false;
    }

    public AuthBiometricFingerprintView(Context context) {
        this(context, null);
    }

    public AuthBiometricFingerprintView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public void handleResetAfterError() {
        showTouchSensorString();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public void handleResetAfterHelp() {
        showTouchSensorString();
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    public void updateState(int i) {
        updateIcon(this.mState, i);
        super.updateState(i);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public void onAttachedToWindowInternal() {
        super.onAttachedToWindowInternal();
        showTouchSensorString();
    }

    private void showTouchSensorString() {
        this.mIndicatorView.setText(C0015R$string.fingerprint_dialog_touch_sensor);
        this.mIndicatorView.setTextColor(getResources().getColor(C0004R$color.biometric_dialog_gray));
    }

    private void updateIcon(int i, int i2) {
        if (OpUtils.isCustomFingerprint()) {
            boolean z = this.mIconView.getVisibility() == 0;
            Log.d("BiometricPrompt/AuthBiometricFingerprintView", "updateIcon: lastState=" + i + ", newState= " + i2 + ", fodShowing=" + this.mFodShowing + ", iconVisible= " + z);
            if (i2 == 4 && i != i2) {
                this.mIconView.setImageResource(C0006R$drawable.fp_icon_default_disable);
                this.mIconView.setColorFilter(getResources().getColor(C0004R$color.biometric_fingerprint_error_color), PorterDuff.Mode.MULTIPLY);
            }
            if (!this.mFodShowing && !z) {
                this.mIconView.setVisibility(0);
            } else if (this.mFodShowing && z) {
                this.mIconView.setVisibility(4);
            }
        } else {
            Drawable animationForTransition = getAnimationForTransition(i, i2);
            if (animationForTransition == null) {
                Log.e("BiometricPrompt/AuthBiometricFingerprintView", "Animation not found, " + i + " -> " + i2);
                return;
            }
            AnimatedVectorDrawable animatedVectorDrawable = animationForTransition instanceof AnimatedVectorDrawable ? (AnimatedVectorDrawable) animationForTransition : null;
            opUpdateAccentColor(animatedVectorDrawable);
            this.mIconView.setImageDrawable(animationForTransition);
            if (animatedVectorDrawable != null && shouldAnimateForTransition(i, i2)) {
                animatedVectorDrawable.forceAnimationOnUI();
                animatedVectorDrawable.start();
            }
        }
    }

    private Drawable getAnimationForTransition(int i, int i2) {
        int i3;
        if (i2 == 1 || i2 == 2) {
            if (i == 4 || i == 3) {
                i3 = C0006R$drawable.fingerprint_dialog_error_to_fp;
            } else {
                i3 = C0006R$drawable.fingerprint_dialog_fp_to_error;
            }
        } else if (i2 == 3 || i2 == 4) {
            i3 = C0006R$drawable.fingerprint_dialog_fp_to_error;
        } else if (i2 != 6) {
            return null;
        } else {
            i3 = C0006R$drawable.fingerprint_dialog_fp_to_error;
        }
        return ((LinearLayout) this).mContext.getDrawable(i3);
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    public void onError(String str) {
        hideFodImmediately();
        super.onError(str);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public int getBiometricIconSize() {
        if (!OpUtils.isCustomFingerprint()) {
            return super.getBiometricIconSize();
        }
        return OpFodViewSettings.getFodIconSize(((LinearLayout) this).mContext);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public int getBottomSpaceHeight(DisplayMetrics displayMetrics) {
        if (!OpUtils.isCustomFingerprint()) {
            return super.getBottomSpaceHeight(displayMetrics);
        }
        int i = displayMetrics.heightPixels;
        float fodAnimationSize = (float) getFodAnimationSize();
        float biometricIconSize = (float) getBiometricIconSize();
        int fodAnimViewY = OpFodViewSettings.getFodAnimViewY(((LinearLayout) this).mContext);
        boolean isSupportCustomFingerprintType2 = OpUtils.isSupportCustomFingerprintType2();
        boolean is2KResolution = OpUtils.is2KResolution();
        boolean isSupportCutout = OpUtils.isSupportCutout();
        boolean isCutoutHide = OpUtils.isCutoutHide(getContext());
        boolean isSupportResolutionSwitch = OpUtils.isSupportResolutionSwitch(getContext());
        Log.d("BiometricPrompt/AuthBiometricFingerprintView", "adjusting bottom space. isFpType2= " + isSupportCustomFingerprintType2 + ", is2kDisplay= " + is2KResolution + ", isSupportCutout= " + isSupportCutout + ", isCutoutHide= " + isCutoutHide + ", isSupportResolutionSwitch= " + isSupportResolutionSwitch);
        if (isSupportCutout && isCutoutHide) {
            i += OpUtils.getCutoutPathdataHeight(getContext());
        }
        return (int) (((((float) (i - fodAnimViewY)) - biometricIconSize) - ((fodAnimationSize - biometricIconSize) / 2.0f)) + getResources().getDimension(C0005R$dimen.oneplus_contorl_radius_r16));
    }

    private int getFodAnimationSize() {
        return OpFodViewSettings.getFodAnimViewHeight(((LinearLayout) this).mContext);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public void onBiometricPromptReady() {
        if (OpUtils.isCustomFingerprint()) {
            int i = getBiometricPromptBundle().getInt("key_cookie", 0);
            if (i == 0) {
                Log.d("BiometricPrompt/AuthBiometricFingerprintView", "onBiometricPromptReady: cookie must not be zero.");
                return;
            }
            OpBiometricDialogImpl opBiometricDialogImpl = (OpBiometricDialogImpl) Dependency.get(OpBiometricDialogImpl.class);
            if (opBiometricDialogImpl != null) {
                opBiometricDialogImpl.onBiometricPromptReady(i);
            } else {
                Log.e("BiometricPrompt/AuthBiometricFingerprintView", "onBiometricPromptReady: fodDialogImpl is null!!!");
            }
            this.mFodShowing = true;
            updateState(2);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.biometrics.AuthBiometricView
    public AuthBiometricView.Callback needWrap(final AuthBiometricView.Callback callback) {
        if (OpUtils.isCustomFingerprint()) {
            return new AuthBiometricView.Callback() { // from class: com.android.systemui.biometrics.AuthBiometricFingerprintView.1
                @Override // com.android.systemui.biometrics.AuthBiometricView.Callback
                public void onAction(int i) {
                    if (i == 2 || i == 3 || i == 5) {
                        AuthBiometricFingerprintView.this.hideFodImmediately();
                    }
                    callback.onAction(i);
                }
            };
        }
        super.needWrap(callback);
        return callback;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideFodImmediately() {
        ((OpBiometricDialogImpl) Dependency.get(OpBiometricDialogImpl.class)).hideFodImmediately();
        this.mFodShowing = false;
        updateState(this.mState);
    }

    private void opUpdateAccentColor(AnimatedVectorDrawable animatedVectorDrawable) {
        if (animatedVectorDrawable != null) {
            int color = ThemeColorUtils.getColor(100);
            String[] strArr = {"_R_G_L_1_G_D_0_P_0", "_R_G_L_1_G_D_1_P_0", "_R_G_L_1_G_D_2_P_0", "_R_G_L_1_G_D_3_P_0", "_R_G_L_1_G_D_4_P_0"};
            for (int i = 0; i < 5; i++) {
                animatedVectorDrawable.changePathStrokeColor(strArr[i], color);
            }
        }
    }

    @Override // com.android.systemui.biometrics.AuthBiometricView
    public void onBackKeyClicked() {
        hideFodImmediately();
    }
}
