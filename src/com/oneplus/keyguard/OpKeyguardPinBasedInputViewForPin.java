package com.oneplus.keyguard;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import com.android.internal.widget.LockscreenCredential;
import com.android.keyguard.KeyguardAbsKeyInputView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0015R$string;
import com.oneplus.keyguard.OpPasswordTextViewForPin;
public abstract class OpKeyguardPinBasedInputViewForPin extends KeyguardAbsKeyInputView implements View.OnKeyListener {
    private static String TAG = "OpKeyguardPinBasedInputViewForPin";
    private View mButton0;
    private View mButton1;
    private View mButton2;
    private View mButton3;
    private View mButton4;
    private View mButton5;
    private View mButton6;
    private View mButton7;
    private View mButton8;
    private View mButton9;
    private TextView mDeleteButton;
    private View mOkButton;
    protected OpPasswordTextViewForPin mPasswordEntry;

    public OpKeyguardPinBasedInputViewForPin(Context context) {
        this(context, null);
    }

    public OpKeyguardPinBasedInputViewForPin(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public void reset() {
        this.mPasswordEntry.requestFocus();
        super.reset();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean onRequestFocusInDescendants(int i, Rect rect) {
        return this.mPasswordEntry.requestFocus(i, rect);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void resetState() {
        this.mPasswordEntry.setEnabled(true);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void setPasswordEntryEnabled(boolean z) {
        this.mPasswordEntry.setEnabled(z);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void setPasswordEntryInputEnabled(boolean z) {
        this.mPasswordEntry.setEnabled(z);
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, android.view.KeyEvent.Callback, android.view.View
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (KeyEvent.isConfirmKey(i)) {
            performClick(this.mOkButton);
            return true;
        } else if (i == 67) {
            performClick(this.mDeleteButton);
            return true;
        } else if (i >= 7 && i <= 16) {
            performNumberClick(i - 7);
            return true;
        } else if (i < 144 || i > 153) {
            return super.onKeyDown(i, keyEvent);
        } else {
            performNumberClick(i - 144);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getPromptReasonStringRes(int i) {
        if (i == 0) {
            return 0;
        }
        if (i == 1) {
            return C0015R$string.kg_prompt_reason_restart_pin;
        }
        if (i == 2) {
            return C0015R$string.kg_prompt_reason_timeout_pin;
        }
        if (i == 3) {
            return C0015R$string.kg_prompt_reason_device_admin;
        }
        if (i != 4) {
            return C0015R$string.kg_prompt_reason_timeout_pin;
        }
        return C0015R$string.kg_prompt_reason_user_request;
    }

    private void performClick(View view) {
        if (view != null) {
            view.performClick();
        }
    }

    private void performNumberClick(int i) {
        switch (i) {
            case 0:
                performClick(this.mButton0);
                return;
            case 1:
                performClick(this.mButton1);
                return;
            case 2:
                performClick(this.mButton2);
                return;
            case 3:
                performClick(this.mButton3);
                return;
            case 4:
                performClick(this.mButton4);
                return;
            case 5:
                performClick(this.mButton5);
                return;
            case 6:
                performClick(this.mButton6);
                return;
            case 7:
                performClick(this.mButton7);
                return;
            case 8:
                performClick(this.mButton8);
                return;
            case 9:
                performClick(this.mButton9);
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void resetPasswordText(boolean z, boolean z2) {
        this.mPasswordEntry.reset(z, z2);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public LockscreenCredential getEnteredCredential() {
        return LockscreenCredential.createPinOrNone(this.mPasswordEntry.getText());
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView, android.view.View
    public void onFinishInflate() {
        OpPasswordTextViewForPin opPasswordTextViewForPin = (OpPasswordTextViewForPin) findViewById(getPasswordTextViewId());
        this.mPasswordEntry = opPasswordTextViewForPin;
        opPasswordTextViewForPin.setOnKeyListener(this);
        this.mPasswordEntry.setSelected(true);
        this.mPasswordEntry.setUserActivityListener(new OpPasswordTextViewForPin.UserActivityListener() { // from class: com.oneplus.keyguard.OpKeyguardPinBasedInputViewForPin.1
            @Override // com.oneplus.keyguard.OpPasswordTextViewForPin.UserActivityListener
            public void onUserActivity() {
                OpKeyguardPinBasedInputViewForPin.this.onUserInput();
            }

            @Override // com.oneplus.keyguard.OpPasswordTextViewForPin.UserActivityListener
            public void onCheckPasswordAndUnlock() {
                if (OpKeyguardPinBasedInputViewForPin.this.mPasswordEntry.isEnabled()) {
                    OpKeyguardPinBasedInputViewForPin.this.verifyPasswordAndUnlock();
                }
            }
        });
        if (!KeyguardUpdateMonitor.getInstance(getContext()).isAutoCheckPinEnabled()) {
            View findViewById = findViewById(C0008R$id.key_enter);
            this.mOkButton = findViewById;
            if (findViewById != null) {
                findViewById.setOnClickListener(new View.OnClickListener() { // from class: com.oneplus.keyguard.OpKeyguardPinBasedInputViewForPin.2
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        OpKeyguardPinBasedInputViewForPin.this.doHapticKeyClick();
                        if (OpKeyguardPinBasedInputViewForPin.this.mPasswordEntry.isEnabled()) {
                            OpKeyguardPinBasedInputViewForPin.this.verifyPasswordAndUnlock();
                        }
                    }
                });
                View.OnHoverListener createLiftToActivateListener = createLiftToActivateListener();
                if (createLiftToActivateListener != null) {
                    this.mOkButton.setOnHoverListener(createLiftToActivateListener);
                }
            }
        }
        TextView textView = (TextView) findViewById(C0008R$id.deleteOrCancel);
        this.mDeleteButton = textView;
        textView.setText(getContext().getResources().getString(17040057));
        this.mDeleteButton.setVisibility(0);
        this.mDeleteButton.setOnClickListener(new View.OnClickListener() { // from class: com.oneplus.keyguard.OpKeyguardPinBasedInputViewForPin.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (OpKeyguardPinBasedInputViewForPin.this.mPasswordEntry.isEnabled()) {
                    OpKeyguardPinBasedInputViewForPin.this.mPasswordEntry.deleteLastChar();
                }
                OpKeyguardPinBasedInputViewForPin.this.doHapticKeyClick();
            }
        });
        this.mDeleteButton.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.oneplus.keyguard.OpKeyguardPinBasedInputViewForPin.4
            @Override // android.view.View.OnLongClickListener
            public boolean onLongClick(View view) {
                if (OpKeyguardPinBasedInputViewForPin.this.mPasswordEntry.isEnabled()) {
                    OpKeyguardPinBasedInputViewForPin.this.resetPasswordText(true, true);
                }
                OpKeyguardPinBasedInputViewForPin.this.doHapticKeyClick();
                return true;
            }
        });
        this.mPasswordEntry.setTextChangeListener(new OpPasswordTextViewForPin.onTextChangedListerner(this) { // from class: com.oneplus.keyguard.OpKeyguardPinBasedInputViewForPin.5
            @Override // com.oneplus.keyguard.OpPasswordTextViewForPin.onTextChangedListerner
            public void onTextChanged(String str) {
            }
        });
        this.mButton0 = findViewById(C0008R$id.key0);
        this.mButton1 = findViewById(C0008R$id.key1);
        this.mButton2 = findViewById(C0008R$id.key2);
        this.mButton3 = findViewById(C0008R$id.key3);
        this.mButton4 = findViewById(C0008R$id.key4);
        this.mButton5 = findViewById(C0008R$id.key5);
        this.mButton6 = findViewById(C0008R$id.key6);
        this.mButton7 = findViewById(C0008R$id.key7);
        this.mButton8 = findViewById(C0008R$id.key8);
        this.mButton9 = findViewById(C0008R$id.key9);
        this.mPasswordEntry.requestFocus();
        super.onFinishInflate();
    }

    @Override // android.view.View.OnKeyListener
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (keyEvent.getAction() == 0) {
            return onKeyDown(i, keyEvent);
        }
        return false;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public CharSequence getTitle() {
        return getContext().getString(17040374);
    }

    private View.OnHoverListener createLiftToActivateListener() {
        Object createInstanceWithArgs = createInstanceWithArgs(getContext(), "com.android.keyguard.LiftToActivateListener", new Class[]{Context.class}, getContext());
        if (createInstanceWithArgs instanceof View.OnHoverListener) {
            return (View.OnHoverListener) createInstanceWithArgs;
        }
        return null;
    }

    private Object createInstanceWithArgs(Context context, String str, Class[] clsArr, Object... objArr) {
        if (str == null || str.length() == 0) {
            Log.e(TAG, "invalide class name ");
        }
        try {
            return Class.forName(str).getConstructor(clsArr).newInstance(objArr);
        } catch (Exception e) {
            String str2 = TAG;
            Log.e(str2, "createInstanceWithArgs fail:" + str);
            e.printStackTrace();
            return null;
        }
    }
}
