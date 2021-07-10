package com.android.keyguard;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import com.android.internal.widget.LockscreenCredential;
import com.android.keyguard.PasswordTextView;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0015R$string;
public abstract class KeyguardPinBasedInputView extends KeyguardAbsKeyInputView implements View.OnKeyListener, View.OnTouchListener {
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
    private View mDeleteButton;
    private View mOkButton;
    protected PasswordTextView mPasswordEntry;

    public KeyguardPinBasedInputView(Context context) {
        this(context, null);
    }

    public KeyguardPinBasedInputView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean onRequestFocusInDescendants(int i, Rect rect) {
        return this.mPasswordEntry.requestFocus(i, rect);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void resetState() {
        setPasswordEntryEnabled(true);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void setPasswordEntryEnabled(boolean z) {
        this.mPasswordEntry.setEnabled(z);
        this.mOkButton.setEnabled(z);
        if (z && !this.mPasswordEntry.hasFocus()) {
            if (!isShown()) {
                Log.i("KeyguardPinBasedInputView", "return to avoid simpin requestFocus when VIEW.GONE");
            } else {
                this.mPasswordEntry.requestFocus();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void setPasswordEntryInputEnabled(boolean z) {
        this.mPasswordEntry.setEnabled(z);
        this.mOkButton.setEnabled(z);
        if (z && !this.mPasswordEntry.hasFocus()) {
            this.mPasswordEntry.requestFocus();
        }
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
        if (i == 4) {
            return C0015R$string.kg_prompt_reason_user_request;
        }
        if (i != 6) {
            return C0015R$string.kg_prompt_reason_timeout_pin;
        }
        return C0015R$string.kg_prompt_reason_prepare_for_update_pin;
    }

    private void performClick(View view) {
        view.performClick();
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
        PasswordTextView passwordTextView = (PasswordTextView) findViewById(getPasswordTextViewId());
        this.mPasswordEntry = passwordTextView;
        passwordTextView.setOnKeyListener(this);
        this.mPasswordEntry.setSelected(true);
        this.mPasswordEntry.setUserActivityListener(new PasswordTextView.UserActivityListener() { // from class: com.android.keyguard.KeyguardPinBasedInputView.1
            @Override // com.android.keyguard.PasswordTextView.UserActivityListener
            public void onUserActivity() {
                KeyguardPinBasedInputView.this.onUserInput();
            }
        });
        View findViewById = findViewById(C0008R$id.key_enter);
        this.mOkButton = findViewById;
        if (findViewById != null) {
            findViewById.setOnTouchListener(this);
            this.mOkButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.keyguard.KeyguardPinBasedInputView.2
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    if (KeyguardPinBasedInputView.this.mPasswordEntry.isEnabled()) {
                        KeyguardPinBasedInputView.this.verifyPasswordAndUnlock();
                    }
                }
            });
            this.mOkButton.setOnHoverListener(new LiftToActivateListener(getContext()));
        }
        View findViewById2 = findViewById(C0008R$id.delete_button);
        this.mDeleteButton = findViewById2;
        findViewById2.setVisibility(0);
        this.mDeleteButton.setOnTouchListener(this);
        this.mDeleteButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.keyguard.KeyguardPinBasedInputView.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (KeyguardPinBasedInputView.this.mPasswordEntry.isEnabled()) {
                    KeyguardPinBasedInputView.this.mPasswordEntry.deleteLastChar();
                }
            }
        });
        this.mDeleteButton.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.keyguard.KeyguardPinBasedInputView.4
            @Override // android.view.View.OnLongClickListener
            public boolean onLongClick(View view) {
                if (KeyguardPinBasedInputView.this.mPasswordEntry.isEnabled()) {
                    KeyguardPinBasedInputView.this.resetPasswordText(true, true);
                }
                KeyguardPinBasedInputView.this.doHapticKeyClick();
                return true;
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

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public void onResume(int i) {
        super.onResume(i);
        this.mPasswordEntry.requestFocus();
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() != 0) {
            return false;
        }
        doHapticKeyClick();
        return false;
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
}
