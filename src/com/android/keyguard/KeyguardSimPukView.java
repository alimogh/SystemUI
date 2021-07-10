package com.android.keyguard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Resources;
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
import com.android.systemui.C0001R$array;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0013R$plurals;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.assist.ui.DisplayUtils;
import com.oneplus.util.OpUtils;
import java.util.HashMap;
import java.util.Map;
public class KeyguardSimPukView extends KeyguardPinBasedInputView {
    private CheckSimPuk mCheckSimPukThread;
    private String mPinText;
    private String mPukText;
    private int mRemainingAttempts;
    private AlertDialog mRemainingAttemptsDialog;
    private boolean mShowDefaultMessage;
    private ImageView mSimImageView;
    private ProgressDialog mSimUnlockProgressDialog;
    private int mSlotId;
    private StateMachine mStateMachine;
    private int mSubId;
    KeyguardUpdateMonitorCallback mUpdateMonitorCallback;
    private int mUsedScreenWidth;
    private Map<String, String> mWrongPukCodeMessageMap;

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView
    public int getPromptReasonStringRes(int i) {
        return 0;
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.EmergencyButton.EmergencyButtonCallback
    public boolean isShowEmergencyPanel() {
        return false;
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

    public KeyguardSimPukView(Context context) {
        this(context, null);
    }

    public KeyguardSimPukView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSimUnlockProgressDialog = null;
        this.mShowDefaultMessage = true;
        this.mStateMachine = new StateMachine();
        this.mSubId = -1;
        this.mWrongPukCodeMessageMap = new HashMap(4);
        this.mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.keyguard.KeyguardSimPukView.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onSimStateChanged(int i, int i2, int i3) {
                Log.v("KeyguardSimPukView", "onSimStateChanged(subId=" + i + ",mSubId=" + KeyguardSimPukView.this.mSubId + ",slotId=" + i2 + ",mSlotId=" + KeyguardSimPukView.this.mSlotId + ",state=" + i3 + ")");
                if (i != KeyguardSimPukView.this.mSubId && KeyguardSimPukView.this.mSubId > 0) {
                    if (i2 == KeyguardSimPukView.this.mSlotId) {
                        KeyguardSimPukView.this.mShowDefaultMessage = true;
                    } else {
                        return;
                    }
                }
                if (i3 == 1 || i3 == 5 || i3 == 6) {
                    KeyguardSimPukView.this.mRemainingAttempts = -1;
                    KeyguardSimPukView.this.mShowDefaultMessage = true;
                    KeyguardSecurityCallback keyguardSecurityCallback = KeyguardSimPukView.this.mCallback;
                    if (keyguardSecurityCallback != null) {
                        keyguardSecurityCallback.userActivity();
                        KeyguardSimPukView.this.mCallback.dismiss(true, KeyguardUpdateMonitor.getCurrentUser());
                    }
                    KeyguardSimPukView.this.resetState();
                    return;
                }
                KeyguardSimPukView.this.resetState();
            }
        };
        updateWrongPukMessageMap(context);
    }

    /* access modifiers changed from: package-private */
    public void updateWrongPukMessageMap(Context context) {
        String[] stringArray = context.getResources().getStringArray(C0001R$array.kg_wrong_puk_code_message_list);
        if (stringArray.length == 0) {
            Log.d("KeyguardSimPukView", "There is no customization PUK prompt");
            return;
        }
        for (String str : stringArray) {
            String[] split = str.trim().split(":");
            if (split.length != 2) {
                Log.e("KeyguardSimPukView", "invalid key value config " + str);
            } else {
                this.mWrongPukCodeMessageMap.put(split[0], split[1]);
            }
        }
    }

    private String getMessageTextForWrongPukCode(int i) {
        SubscriptionInfo subscriptionInfoForSubId = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getSubscriptionInfoForSubId(this.mSubId);
        if (subscriptionInfoForSubId == null) {
            return null;
        }
        return this.mWrongPukCodeMessageMap.get(subscriptionInfoForSubId.getMccString() + subscriptionInfoForSubId.getMncString());
    }

    /* access modifiers changed from: private */
    public class StateMachine {
        private int state;

        private StateMachine() {
            this.state = 0;
        }

        /* JADX WARNING: Removed duplicated region for block: B:22:0x004f  */
        /* JADX WARNING: Removed duplicated region for block: B:26:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void next() {
            /*
                r5 = this;
                int r0 = r5.state
                r1 = 0
                r2 = 1
                if (r0 != 0) goto L_0x001a
                com.android.keyguard.KeyguardSimPukView r0 = com.android.keyguard.KeyguardSimPukView.this
                boolean r0 = com.android.keyguard.KeyguardSimPukView.access$500(r0)
                if (r0 == 0) goto L_0x0016
                r5.state = r2
                int r0 = com.android.systemui.C0015R$string.kg_puk_enter_pin_hint
            L_0x0012:
                r4 = r1
                r1 = r0
                r0 = r4
                goto L_0x0048
            L_0x0016:
                int r1 = com.android.systemui.C0015R$string.kg_invalid_sim_puk_hint
            L_0x0018:
                r0 = r2
                goto L_0x0048
            L_0x001a:
                r3 = 2
                if (r0 != r2) goto L_0x002d
                com.android.keyguard.KeyguardSimPukView r0 = com.android.keyguard.KeyguardSimPukView.this
                boolean r0 = com.android.keyguard.KeyguardSimPukView.access$600(r0)
                if (r0 == 0) goto L_0x002a
                r5.state = r3
                int r0 = com.android.systemui.C0015R$string.kg_enter_confirm_pin_hint
                goto L_0x0012
            L_0x002a:
                int r1 = com.android.systemui.C0015R$string.kg_invalid_sim_pin_hint
                goto L_0x0018
            L_0x002d:
                if (r0 != r3) goto L_0x0047
                com.android.keyguard.KeyguardSimPukView r0 = com.android.keyguard.KeyguardSimPukView.this
                boolean r0 = r0.confirmPin()
                if (r0 == 0) goto L_0x0042
                r0 = 3
                r5.state = r0
                int r0 = com.android.systemui.C0015R$string.keyguard_sim_unlock_progress_dialog_message
                com.android.keyguard.KeyguardSimPukView r3 = com.android.keyguard.KeyguardSimPukView.this
                com.android.keyguard.KeyguardSimPukView.access$700(r3)
                goto L_0x0012
            L_0x0042:
                r5.state = r2
                int r1 = com.android.systemui.C0015R$string.kg_invalid_confirm_pin_hint
                goto L_0x0018
            L_0x0047:
                r0 = r1
            L_0x0048:
                com.android.keyguard.KeyguardSimPukView r3 = com.android.keyguard.KeyguardSimPukView.this
                r3.resetPasswordText(r2, r2)
                if (r1 == 0) goto L_0x0058
                com.android.keyguard.KeyguardSimPukView r5 = com.android.keyguard.KeyguardSimPukView.this
                com.android.keyguard.SecurityMessageDisplay r5 = r5.mSecurityMessageDisplay
                if (r5 == 0) goto L_0x0058
                r5.setMessage(r1, r0)
            L_0x0058:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardSimPukView.StateMachine.next():void");
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            int i;
            String str;
            SecurityMessageDisplay securityMessageDisplay;
            KeyguardSimPukView.this.mPinText = "";
            KeyguardSimPukView.this.mPukText = "";
            int i2 = 0;
            this.state = 0;
            KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(((LinearLayout) KeyguardSimPukView.this).mContext);
            TextView textView = (TextView) KeyguardSimPukView.this.findViewById(C0008R$id.slot_id_name);
            KeyguardSimPukView.this.mSubId = instance.getNextSubIdForState(3);
            Log.d("KeyguardSimPukView", "reset, mSubId=" + KeyguardSimPukView.this.mSubId + ", slotId=" + SubscriptionManager.getPhoneId(KeyguardSimPukView.this.mSubId));
            if (SubscriptionManager.isValidSubscriptionId(KeyguardSimPukView.this.mSubId)) {
                String string = ((LinearLayout) KeyguardSimPukView.this).mContext.getString(C0015R$string.kg_slot_name, Integer.valueOf(SubscriptionManager.getPhoneId(KeyguardSimPukView.this.mSubId) + 1));
                KeyguardSimPukView keyguardSimPukView = KeyguardSimPukView.this;
                keyguardSimPukView.mSlotId = SubscriptionManager.getPhoneId(keyguardSimPukView.mSubId);
                int simCount = TelephonyManager.getDefault().getSimCount();
                Resources resources = KeyguardSimPukView.this.getResources();
                if (simCount < 2) {
                    str = resources.getString(C0015R$string.kg_puk_enter_puk_hint);
                } else {
                    SubscriptionInfo subscriptionInfoForSubId = instance.getSubscriptionInfoForSubId(KeyguardSimPukView.this.mSubId);
                    if (subscriptionInfoForSubId != null) {
                        subscriptionInfoForSubId.getDisplayName();
                    }
                    String string2 = resources.getString(C0015R$string.kg_puk_enter_puk_hint_multi, string);
                    if (subscriptionInfoForSubId != null) {
                        i = subscriptionInfoForSubId.getIconTint();
                        str = string2;
                        if (KeyguardSimPukView.this.mShowDefaultMessage && (securityMessageDisplay = KeyguardSimPukView.this.mSecurityMessageDisplay) != null) {
                            securityMessageDisplay.setMessage(str, 1);
                        }
                        KeyguardSimPukView.this.mShowDefaultMessage = true;
                        KeyguardSimPukView.this.mSimImageView.setImageTintList(ColorStateList.valueOf(i));
                        textView.setText(string);
                        textView.setTextColor(-1);
                        textView.setVisibility(0);
                    } else {
                        str = string2;
                    }
                }
                i = -1;
                securityMessageDisplay.setMessage(str, 1);
                KeyguardSimPukView.this.mShowDefaultMessage = true;
                KeyguardSimPukView.this.mSimImageView.setImageTintList(ColorStateList.valueOf(i));
                textView.setText(string);
                textView.setTextColor(-1);
                textView.setVisibility(0);
            }
            boolean isEsimLocked = KeyguardEsimArea.isEsimLocked(((LinearLayout) KeyguardSimPukView.this).mContext, KeyguardSimPukView.this.mSubId);
            KeyguardEsimArea keyguardEsimArea = (KeyguardEsimArea) KeyguardSimPukView.this.findViewById(C0008R$id.keyguard_esim_area);
            if (!isEsimLocked) {
                i2 = 8;
            }
            keyguardEsimArea.setVisibility(i2);
            KeyguardSimPukView.this.mPasswordEntry.requestFocus();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getPukPasswordErrorMessage(int i, boolean z) {
        String str;
        int i2;
        int i3;
        if (i == 0) {
            str = getMessageTextForWrongPukCode(this.mSubId);
            if (str == null) {
                str = getContext().getString(C0015R$string.kg_password_wrong_puk_code_dead);
            }
        } else if (i > 0) {
            if (z) {
                i3 = C0013R$plurals.kg_password_default_puk_message;
            } else {
                i3 = C0013R$plurals.kg_password_wrong_puk_code;
            }
            str = getContext().getResources().getQuantityString(i3, i, Integer.valueOf(i));
        } else {
            if (z) {
                i2 = C0015R$string.kg_puk_enter_puk_hint;
            } else {
                i2 = C0015R$string.kg_password_puk_failed;
            }
            str = getContext().getString(i2);
        }
        if (KeyguardEsimArea.isEsimLocked(((LinearLayout) this).mContext, this.mSubId)) {
            str = getResources().getString(C0015R$string.kg_sim_lock_esim_instructions, str);
        }
        Log.d("KeyguardSimPukView", "getPukPasswordErrorMessage: attemptsRemaining=" + i + " displayMessage=" + str);
        return str;
    }

    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardAbsKeyInputView
    public void resetState() {
        super.resetState();
        this.mStateMachine.reset();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getPasswordTextViewId() {
        return C0008R$id.pukEntry;
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
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mUpdateMonitorCallback);
        resetState();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView, android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mUpdateMonitorCallback);
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
    public abstract class CheckSimPuk extends Thread {
        private final String mPin;
        private final String mPuk;
        private final int mSubId;

        /* access modifiers changed from: package-private */
        public abstract void onSimLockChangedResponse(PinResult pinResult);

        protected CheckSimPuk(String str, String str2, int i) {
            this.mPuk = str;
            this.mPin = str2;
            this.mSubId = i;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            Log.v("KeyguardSimPukView", "call supplyPukReportResult() , mSubId = " + this.mSubId);
            final PinResult supplyPukReportPinResult = ((TelephonyManager) ((LinearLayout) KeyguardSimPukView.this).mContext.getSystemService("phone")).createForSubscriptionId(this.mSubId).supplyPukReportPinResult(this.mPuk, this.mPin);
            if (supplyPukReportPinResult == null) {
                Log.e("KeyguardSimPukView", "Error result for supplyPukReportResult.");
                KeyguardSimPukView.this.post(new Runnable() { // from class: com.android.keyguard.KeyguardSimPukView.CheckSimPuk.1
                    @Override // java.lang.Runnable
                    public void run() {
                        CheckSimPuk.this.onSimLockChangedResponse(PinResult.getDefaultFailedResult());
                    }
                });
                return;
            }
            Log.v("KeyguardSimPukView", "supplyPukReportResult returned: " + supplyPukReportPinResult.toString());
            KeyguardSimPukView.this.post(new Runnable() { // from class: com.android.keyguard.KeyguardSimPukView.CheckSimPuk.2
                @Override // java.lang.Runnable
                public void run() {
                    CheckSimPuk.this.onSimLockChangedResponse(supplyPukReportPinResult);
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
            if (!(((LinearLayout) this).mContext instanceof Activity)) {
                this.mSimUnlockProgressDialog.getWindow().setType(2009);
            }
        }
        return this.mSimUnlockProgressDialog;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Dialog getPukRemainingAttemptsDialog(int i) {
        String pukPasswordErrorMessage = getPukPasswordErrorMessage(i, false);
        AlertDialog alertDialog = this.mRemainingAttemptsDialog;
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(((LinearLayout) this).mContext);
            builder.setMessage(pukPasswordErrorMessage);
            builder.setCancelable(false);
            builder.setNeutralButton(C0015R$string.ok, (DialogInterface.OnClickListener) null);
            AlertDialog create = builder.create();
            this.mRemainingAttemptsDialog = create;
            create.getWindow().setType(2009);
        } else {
            alertDialog.setMessage(pukPasswordErrorMessage);
        }
        return this.mRemainingAttemptsDialog;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkPuk() {
        if (this.mPasswordEntry.getText().length() != 8) {
            return false;
        }
        this.mPukText = this.mPasswordEntry.getText();
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkPin() {
        int length = this.mPasswordEntry.getText().length();
        if (length < 4 || length > 8) {
            return false;
        }
        this.mPinText = this.mPasswordEntry.getText();
        return true;
    }

    public boolean confirmPin() {
        return this.mPinText.equals(this.mPasswordEntry.getText());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSim() {
        getSimUnlockProgressDialog().show();
        if (this.mCheckSimPukThread == null) {
            Log.d("KeyguardSimPukView", "begin verifyPasswordAndUnlock , mSubId = " + this.mSubId + ", slot=" + this.mSlotId);
            AnonymousClass3 r0 = new CheckSimPuk(this.mPukText, this.mPinText, this.mSubId) { // from class: com.android.keyguard.KeyguardSimPukView.3
                /* access modifiers changed from: package-private */
                @Override // com.android.keyguard.KeyguardSimPukView.CheckSimPuk
                public void onSimLockChangedResponse(final PinResult pinResult) {
                    KeyguardSimPukView.this.post(new Runnable() { // from class: com.android.keyguard.KeyguardSimPukView.3.1
                        @Override // java.lang.Runnable
                        public void run() {
                            if (KeyguardSimPukView.this.mSimUnlockProgressDialog != null) {
                                KeyguardSimPukView.this.mSimUnlockProgressDialog.hide();
                            }
                            KeyguardSimPukView.this.resetPasswordText(true, pinResult.getType() != 0);
                            if (pinResult.getType() == 0) {
                                ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).reportSimUnlocked(KeyguardSimPukView.this.mSubId);
                                KeyguardSimPukView.this.mRemainingAttempts = -1;
                                KeyguardSimPukView.this.mShowDefaultMessage = true;
                                KeyguardSecurityCallback keyguardSecurityCallback = KeyguardSimPukView.this.mCallback;
                                if (keyguardSecurityCallback != null) {
                                    keyguardSecurityCallback.dismiss(true, KeyguardUpdateMonitor.getCurrentUser());
                                }
                            } else {
                                KeyguardSimPukView.this.mShowDefaultMessage = false;
                                if (pinResult.getType() == 1) {
                                    KeyguardSimPukView keyguardSimPukView = KeyguardSimPukView.this;
                                    SecurityMessageDisplay securityMessageDisplay = keyguardSimPukView.mSecurityMessageDisplay;
                                    if (securityMessageDisplay != null) {
                                        securityMessageDisplay.setMessage(keyguardSimPukView.getPukPasswordErrorMessage(pinResult.getAttemptsRemaining(), false), 1);
                                    }
                                    if (pinResult.getAttemptsRemaining() <= 2) {
                                        KeyguardSimPukView.this.getPukRemainingAttemptsDialog(pinResult.getAttemptsRemaining()).show();
                                    }
                                } else {
                                    KeyguardSimPukView keyguardSimPukView2 = KeyguardSimPukView.this;
                                    SecurityMessageDisplay securityMessageDisplay2 = keyguardSimPukView2.mSecurityMessageDisplay;
                                    if (securityMessageDisplay2 != null) {
                                        securityMessageDisplay2.setMessage(keyguardSimPukView2.getContext().getString(C0015R$string.kg_password_puk_failed), 1);
                                    }
                                }
                                Log.d("KeyguardSimPukView", "verifyPasswordAndUnlock  UpdateSim.onSimCheckResponse:  attemptsRemaining=" + pinResult.getAttemptsRemaining());
                            }
                            KeyguardSimPukView.this.mStateMachine.reset();
                            KeyguardSimPukView.this.mCheckSimPukThread = null;
                        }
                    });
                }
            };
            this.mCheckSimPukThread = r0;
            r0.start();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void verifyPasswordAndUnlock() {
        this.mStateMachine.next();
    }

    @Override // com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.KeyguardSecurityView
    public CharSequence getTitle() {
        return getContext().getString(17040376);
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
            Log.i("KeyguardSimPukView", "updateLayoutParamForDisplayWidth, displayWidth:" + width + ", mUsedScreenWidth:" + this.mUsedScreenWidth);
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
                findViewById(C0008R$id.pukEntry).getLayoutParams().width = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_security_width), 1080);
            }
        }
    }
}
