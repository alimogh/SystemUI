package com.android.keyguard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.telephony.PinResult;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.keyguard.KeyguardSecurityViewFlipper;
import com.android.systemui.C0002R$attr;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0013R$plurals;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.assist.ui.DisplayUtils;
import com.oneplus.keyguard.OpBounerMessageAreaInfo;
import com.oneplus.keyguard.OpKeyguardSimPinView;
import com.oneplus.util.OpUtils;
public class KeyguardSimPinView extends OpKeyguardSimPinView {
    private CheckSimPin mCheckSimPinThread;
    private int mRemainingAttempts;
    private AlertDialog mRemainingAttemptsDialog;
    private int mRetryCount;
    private boolean mShowDefaultMessage;
    private ImageView mSimImageView;
    private ProgressDialog mSimUnlockProgressDialog;
    private int mSlotId;
    private int mSubId;
    KeyguardUpdateMonitorCallback mUpdateMonitorCallback;
    private int mUsedScreenWidth;

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView
    public int getPromptReasonStringRes(int i) {
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public boolean shouldLockout(long j) {
        return false;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void startAppearAnimation() {
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(Runnable runnable) {
        return false;
    }

    static /* synthetic */ int access$308(KeyguardSimPinView keyguardSimPinView) {
        int i = keyguardSimPinView.mRetryCount;
        keyguardSimPinView.mRetryCount = i + 1;
        return i;
    }

    public KeyguardSimPinView(Context context) {
        this(context, null);
    }

    public KeyguardSimPinView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSimUnlockProgressDialog = null;
        this.mShowDefaultMessage = true;
        this.mRemainingAttempts = -1;
        this.mSubId = -1;
        this.mRetryCount = 0;
        this.mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.keyguard.KeyguardSimPinView.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onSimStateChanged(int i, int i2, int i3) {
                Log.v("KeyguardSimPinView", "onSimStateChanged(subId=" + i + ",slotId=" + i2 + ",state=" + i3 + ") , mSub=" + KeyguardSimPinView.this.mSubId);
                if (i3 == 1) {
                    KeyguardSimPinView.this.mRemainingAttempts = -1;
                    KeyguardSimPinView.this.mShowDefaultMessage = true;
                    KeyguardSimPinView.this.mRetryCount = 0;
                } else if (i3 == 3) {
                    KeyguardSimPinView.this.mShowDefaultMessage = true;
                    KeyguardSimPinView.this.resetState();
                } else if (i3 != 5) {
                    KeyguardSimPinView.this.resetState();
                } else {
                    KeyguardSimPinView.this.mRemainingAttempts = -1;
                    KeyguardSimPinView.this.resetState();
                }
            }
        };
    }

    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView
    public void resetState() {
        super.resetState();
        Log.v("KeyguardSimPinView", "Resetting state, default=" + this.mShowDefaultMessage);
        handleSubInfoChangeIfNeeded();
        int i = 0;
        if (this.mShowDefaultMessage) {
            this.mRetryCount = 0;
            showDefaultMessage(false);
        }
        boolean isEsimLocked = KeyguardEsimArea.isEsimLocked(((LinearLayout) this).mContext, this.mSubId);
        KeyguardEsimArea keyguardEsimArea = (KeyguardEsimArea) findViewById(C0008R$id.keyguard_esim_area);
        if (!isEsimLocked) {
            i = 8;
        }
        keyguardEsimArea.setVisibility(i);
    }

    private void setLockedSimMessage() {
        String str;
        int slotIndex = SubscriptionManager.getSlotIndex(this.mSubId);
        this.mSlotId = slotIndex;
        if (slotIndex == -1) {
            Log.w("KeyguardSimPinView", "get invalid slot index");
            this.mSlotId = KeyguardUpdateMonitor.getInstance(((LinearLayout) this).mContext).getSimSlotId(this.mSubId);
        }
        boolean isEsimLocked = KeyguardEsimArea.isEsimLocked(((LinearLayout) this).mContext, this.mSubId);
        TelephonyManager telephonyManager = (TelephonyManager) ((LinearLayout) this).mContext.getSystemService("phone");
        int i = 1;
        int activeModemCount = telephonyManager != null ? telephonyManager.getActiveModemCount() : 1;
        Resources resources = getResources();
        TypedArray obtainStyledAttributes = ((LinearLayout) this).mContext.obtainStyledAttributes(new int[]{C0002R$attr.wallpaperTextColor});
        int color = obtainStyledAttributes.getColor(0, -1);
        obtainStyledAttributes.recycle();
        TextView textView = (TextView) findViewById(C0008R$id.slot_id_name);
        if (activeModemCount < 2) {
            str = resources.getString(C0015R$string.kg_sim_pin_instructions);
            textView.setVisibility(4);
        } else {
            SubscriptionInfo subscriptionInfoForSubId = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getSubscriptionInfoForSubId(this.mSubId);
            String string = resources.getString(C0015R$string.kg_sim_pin_instructions_multi, subscriptionInfoForSubId != null ? subscriptionInfoForSubId.getDisplayName() : "");
            if (subscriptionInfoForSubId != null) {
                color = subscriptionInfoForSubId.getIconTint();
            }
            textView.setText(((LinearLayout) this).mContext.getString(C0015R$string.kg_slot_name, Integer.valueOf(this.mSlotId + 1)));
            textView.setVisibility(0);
            str = string;
        }
        if (isEsimLocked) {
            str = resources.getString(C0015R$string.kg_sim_lock_esim_instructions, str);
        } else {
            i = 0;
        }
        if (this.mSecurityMessageDisplay != null && getVisibility() == 0) {
            this.mSecurityMessageDisplay.setMessage(str, i);
        }
        this.mSimImageView.setImageTintList(ColorStateList.valueOf(color));
        Log.d("KeyguardSimPinView", "mSubId=" + this.mSubId + " , slot=" + this.mSlotId + ", esim:" + isEsimLocked);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showDefaultMessage(boolean z) {
        KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(((LinearLayout) this).mContext);
        if (!instance.isSimPinSecure()) {
            Log.d("KeyguardSimPinView", "return when no simpin");
            return;
        }
        int nextSubIdForState = instance.getNextSubIdForState(3);
        if (this.mSubId != nextSubIdForState && SubscriptionManager.isValidSubscriptionId(nextSubIdForState)) {
            this.mSubId = nextSubIdForState;
            Log.d("KeyguardSimPinView", "change subId to " + nextSubIdForState);
            z = true;
        }
        TextView textView = (TextView) findViewById(C0008R$id.slot_id_name);
        if (SubscriptionManager.isValidSubscriptionId(this.mSubId)) {
            int i = this.mRemainingAttempts;
            if (i < 0 || z) {
                setLockedSimMessage();
                new CheckSimPin("", this.mSubId) { // from class: com.android.keyguard.KeyguardSimPinView.2
                    /* access modifiers changed from: package-private */
                    @Override // com.android.keyguard.KeyguardSimPinView.CheckSimPin
                    public void onSimCheckResponse(PinResult pinResult) {
                        if (pinResult.getType() == 2 && pinResult.getAttemptsRemaining() == 0 && KeyguardSimPinView.this.mRetryCount <= 10) {
                            Log.w("KeyguardSimPinView", "PIN_GENERAL_FAILURE, retry again, " + KeyguardSimPinView.this.mRetryCount);
                            KeyguardSimPinView.access$308(KeyguardSimPinView.this);
                            KeyguardSimPinView.this.postDelayed(new Runnable() { // from class: com.android.keyguard.KeyguardSimPinView.2.1
                                @Override // java.lang.Runnable
                                public void run() {
                                    KeyguardSimPinView.this.showDefaultMessage(true);
                                }
                            }, 100);
                        }
                        Log.d("KeyguardSimPinView", "onSimCheckResponse  dummy One result " + pinResult.toString());
                        if (pinResult.getAttemptsRemaining() >= 0) {
                            KeyguardSimPinView.this.mRemainingAttempts = pinResult.getAttemptsRemaining();
                            OpBounerMessageAreaInfo pinPasswordErrorMessageObject = KeyguardSimPinView.this.getPinPasswordErrorMessageObject(pinResult.getAttemptsRemaining(), true);
                            SecurityMessageDisplay securityMessageDisplay = KeyguardSimPinView.this.mSecurityMessageDisplay;
                            if (securityMessageDisplay != null && pinPasswordErrorMessageObject != null) {
                                securityMessageDisplay.setMessage(pinPasswordErrorMessageObject.getDisplayMessage(), pinPasswordErrorMessageObject.getType());
                            }
                        }
                    }
                }.start();
                return;
            }
            OpBounerMessageAreaInfo pinPasswordErrorMessageObject = getPinPasswordErrorMessageObject(i, true);
            SecurityMessageDisplay securityMessageDisplay = this.mSecurityMessageDisplay;
            if (securityMessageDisplay != null && pinPasswordErrorMessageObject != null) {
                securityMessageDisplay.setMessage(pinPasswordErrorMessageObject.getDisplayMessage(), pinPasswordErrorMessageObject.getType());
            }
        }
    }

    private void handleSubInfoChangeIfNeeded() {
        int unlockedSubIdForState = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getUnlockedSubIdForState(2);
        if (unlockedSubIdForState != this.mSubId && SubscriptionManager.isValidSubscriptionId(unlockedSubIdForState)) {
            this.mSubId = unlockedSubIdForState;
            this.mShowDefaultMessage = true;
            this.mRemainingAttempts = -1;
            Log.d("KeyguardSimPinView", "subinfo change subId to " + unlockedSubIdForState);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        resetState();
    }

    private String getPinPasswordErrorMessage(int i, boolean z) {
        String str;
        int i2;
        int i3;
        if (i == 0) {
            str = getContext().getString(C0015R$string.kg_password_wrong_pin_code_pukked);
        } else if (i <= 0) {
            str = getContext().getString(z ? C0015R$string.kg_sim_pin_instructions : C0015R$string.kg_password_pin_failed);
        } else if (TelephonyManager.getDefault().getSimCount() > 1) {
            if (z) {
                i3 = C0013R$plurals.kg_password_default_pin_message_multi_sim;
            } else {
                i3 = C0013R$plurals.kg_password_wrong_pin_code_multi_sim;
            }
            str = getContext().getResources().getQuantityString(i3, i, Integer.valueOf(this.mSlotId + 1), Integer.valueOf(i));
        } else {
            if (z) {
                i2 = C0013R$plurals.kg_password_default_pin_message;
            } else {
                i2 = C0013R$plurals.kg_password_wrong_pin_code;
            }
            str = getContext().getResources().getQuantityString(i2, i, Integer.valueOf(i));
        }
        if (KeyguardEsimArea.isEsimLocked(((LinearLayout) this).mContext, this.mSubId)) {
            str = getResources().getString(C0015R$string.kg_sim_lock_esim_instructions, str);
        }
        Log.d("KeyguardSimPinView", "getPinPasswordErrorMessage: attemptsRemaining=" + i + " displayMessage=" + str);
        return str;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getPasswordTextViewId() {
        return C0008R$id.simPinEntry;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        View view = this.mEcaView;
        if (view instanceof EmergencyCarrierArea) {
            ((EmergencyCarrierArea) view).setCarrierTextVisible(true);
        }
        this.mSimImageView = (ImageView) findViewById(C0008R$id.keyguard_sim);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView, android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mRetryCount = 0;
        if (this.mShowDefaultMessage) {
            showDefaultMessage(false);
        }
        KeyguardUpdateMonitor.getInstance(((LinearLayout) this).mContext).registerCallback(this.mUpdateMonitorCallback);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView, android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(((LinearLayout) this).mContext).removeCallback(this.mUpdateMonitorCallback);
    }

    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public void onResume(int i) {
        super.onResume(i);
        resetState();
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public void onPause() {
        ProgressDialog progressDialog = this.mSimUnlockProgressDialog;
        if (progressDialog != null) {
            progressDialog.dismiss();
            this.mSimUnlockProgressDialog = null;
        }
    }

    /* access modifiers changed from: private */
    public abstract class CheckSimPin extends Thread {
        private final String mPin;
        private int mSubId;

        /* access modifiers changed from: package-private */
        public abstract void onSimCheckResponse(PinResult pinResult);

        protected CheckSimPin(String str, int i) {
            this.mPin = str;
            this.mSubId = i;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            Log.v("KeyguardSimPinView", "call supplyPinReportResultForSubscriber(subid=" + this.mSubId + ")");
            final PinResult supplyPinReportPinResult = ((TelephonyManager) ((LinearLayout) KeyguardSimPinView.this).mContext.getSystemService("phone")).createForSubscriptionId(this.mSubId).supplyPinReportPinResult(this.mPin);
            if (supplyPinReportPinResult == null) {
                Log.e("KeyguardSimPinView", "Error result for supplyPinReportResult.");
                KeyguardSimPinView.this.post(new Runnable() { // from class: com.android.keyguard.KeyguardSimPinView.CheckSimPin.1
                    @Override // java.lang.Runnable
                    public void run() {
                        CheckSimPin.this.onSimCheckResponse(PinResult.getDefaultFailedResult());
                    }
                });
                return;
            }
            Log.v("KeyguardSimPinView", "supplyPinReportResult returned: " + supplyPinReportPinResult.toString());
            KeyguardSimPinView.this.post(new Runnable() { // from class: com.android.keyguard.KeyguardSimPinView.CheckSimPin.2
                @Override // java.lang.Runnable
                public void run() {
                    CheckSimPin.this.onSimCheckResponse(supplyPinReportPinResult);
                }
            });
        }
    }

    private Dialog getSimUnlockProgressDialog() {
        if (this.mSimUnlockProgressDialog == null) {
            ProgressDialog progressDialog = new ProgressDialog(((LinearLayout) this).mContext);
            this.mSimUnlockProgressDialog = progressDialog;
            progressDialog.setMessage(((LinearLayout) this).mContext.getString(C0015R$string.kg_sim_unlock_progress_dialog_message));
            this.mSimUnlockProgressDialog.setIndeterminate(true);
            this.mSimUnlockProgressDialog.setCancelable(false);
            this.mSimUnlockProgressDialog.getWindow().setType(2009);
        }
        return this.mSimUnlockProgressDialog;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Dialog getSimRemainingAttemptsDialog(int i) {
        String pinPasswordErrorMessage = getPinPasswordErrorMessage(i, false);
        AlertDialog alertDialog = this.mRemainingAttemptsDialog;
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(((LinearLayout) this).mContext);
            builder.setMessage(pinPasswordErrorMessage);
            builder.setCancelable(false);
            builder.setNeutralButton(C0015R$string.ok, (DialogInterface.OnClickListener) null);
            AlertDialog create = builder.create();
            this.mRemainingAttemptsDialog = create;
            create.getWindow().setType(2009);
        } else {
            alertDialog.setMessage(pinPasswordErrorMessage);
        }
        return this.mRemainingAttemptsDialog;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void verifyPasswordAndUnlock() {
        if (this.mPasswordEntry.getText().length() < 4) {
            SecurityMessageDisplay securityMessageDisplay = this.mSecurityMessageDisplay;
            if (securityMessageDisplay != null) {
                securityMessageDisplay.setMessage(C0015R$string.kg_invalid_sim_pin_hint, 1);
            }
            resetPasswordText(true, true);
            this.mCallback.userActivity();
            return;
        }
        getSimUnlockProgressDialog().show();
        if (this.mCheckSimPinThread == null) {
            Log.d("KeyguardSimPinView", "begin verifyPasswordAndUnlock");
            AnonymousClass3 r0 = new CheckSimPin(this.mPasswordEntry.getText(), this.mSubId) { // from class: com.android.keyguard.KeyguardSimPinView.3
                /* access modifiers changed from: package-private */
                @Override // com.android.keyguard.KeyguardSimPinView.CheckSimPin
                public void onSimCheckResponse(final PinResult pinResult) {
                    KeyguardSimPinView.this.post(new Runnable() { // from class: com.android.keyguard.KeyguardSimPinView.3.1
                        @Override // java.lang.Runnable
                        public void run() {
                            KeyguardSimPinView.this.mRemainingAttempts = pinResult.getAttemptsRemaining();
                            if (KeyguardSimPinView.this.mSimUnlockProgressDialog != null) {
                                KeyguardSimPinView.this.mSimUnlockProgressDialog.hide();
                            }
                            KeyguardSimPinView.this.resetPasswordText(true, pinResult.getType() != 0);
                            if (pinResult.getType() == 0) {
                                KeyguardSimPinView.this.mRemainingAttempts = -1;
                                KeyguardSimPinView.this.mRetryCount = 0;
                                KeyguardSimPinView.this.mShowDefaultMessage = true;
                                ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).reportSimUnlocked(KeyguardSimPinView.this.mSubId);
                                KeyguardSecurityCallback keyguardSecurityCallback = KeyguardSimPinView.this.mCallback;
                                if (keyguardSecurityCallback != null) {
                                    keyguardSecurityCallback.dismiss(true, KeyguardUpdateMonitor.getCurrentUser());
                                }
                            } else {
                                KeyguardSimPinView.this.mShowDefaultMessage = false;
                                if (pinResult.getType() == 1) {
                                    KeyguardSimPinView keyguardSimPinView = KeyguardSimPinView.this;
                                    OpBounerMessageAreaInfo pinPasswordErrorMessageObject = keyguardSimPinView.getPinPasswordErrorMessageObject(keyguardSimPinView.mRemainingAttempts, false);
                                    SecurityMessageDisplay securityMessageDisplay2 = KeyguardSimPinView.this.mSecurityMessageDisplay;
                                    if (!(securityMessageDisplay2 == null || pinPasswordErrorMessageObject == null)) {
                                        securityMessageDisplay2.setMessage(pinPasswordErrorMessageObject.getDisplayMessage(), pinPasswordErrorMessageObject.getType());
                                    }
                                    if (pinResult.getAttemptsRemaining() <= 2) {
                                        KeyguardSimPinView.this.getSimRemainingAttemptsDialog(pinResult.getAttemptsRemaining()).show();
                                    }
                                } else {
                                    KeyguardSimPinView keyguardSimPinView2 = KeyguardSimPinView.this;
                                    SecurityMessageDisplay securityMessageDisplay3 = keyguardSimPinView2.mSecurityMessageDisplay;
                                    if (securityMessageDisplay3 != null) {
                                        securityMessageDisplay3.setMessage(keyguardSimPinView2.getContext().getString(C0015R$string.kg_password_pin_failed), 1);
                                    }
                                }
                                Log.d("KeyguardSimPinView", "verifyPasswordAndUnlock  CheckSimPin.onSimCheckResponse: " + pinResult + " attemptsRemaining=" + pinResult.getAttemptsRemaining());
                            }
                            KeyguardSecurityCallback keyguardSecurityCallback2 = KeyguardSimPinView.this.mCallback;
                            if (keyguardSecurityCallback2 != null) {
                                keyguardSecurityCallback2.userActivity();
                            }
                            KeyguardSimPinView.this.mCheckSimPinThread = null;
                        }
                    });
                }
            };
            this.mCheckSimPinThread = r0;
            r0.start();
        }
    }

    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardSecurityView
    public CharSequence getTitle() {
        return getContext().getString(17040375);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        updateLayoutParamForDisplayWidth();
    }

    private void updateLayoutParamForDisplayWidth() {
        int width = DisplayUtils.getWidth(((LinearLayout) this).mContext);
        if (this.mUsedScreenWidth != width) {
            Log.i("KeyguardSimPinView", "updateLayoutParamForDisplayWidth, displayWidth:" + width + ", mUsedScreenWidth:" + this.mUsedScreenWidth);
            this.mUsedScreenWidth = width;
            if (width > 1080) {
                KeyguardSecurityViewFlipper.LayoutParams layoutParams = (KeyguardSecurityViewFlipper.LayoutParams) getLayoutParams();
                layoutParams.maxHeight = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_security_max_height), 1080);
                layoutParams.maxWidth = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_security_width), 1080);
                setLayoutParams(layoutParams);
                findViewById(C0008R$id.keyguard_security_sim_pin_top_space).getLayoutParams().height = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_security_sim_pin_top_space), 1080);
                ViewGroup.LayoutParams layoutParams2 = findViewById(C0008R$id.keyguard_esim_area).getLayoutParams();
                if (layoutParams2 instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) layoutParams2).topMargin = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.eca_overlap), 1080);
                }
                findViewById(C0008R$id.simPinEntry).getLayoutParams().width = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_security_width), 1080);
            }
        }
    }
}
