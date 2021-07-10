package com.android.keyguard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.hardware.biometrics.BiometricSourceType;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import com.android.internal.util.LatencyTracker;
import com.android.internal.widget.LockPatternChecker;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockscreenCredential;
import com.android.keyguard.EmergencyButton;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0009R$integer;
import com.android.systemui.C0013R$plurals;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.oneplus.keyguard.OpEmergencyPanel;
import com.oneplus.keyguard.OpKeyguardAbsKeyInputView;
import com.oneplus.keyguard.OpKeyguardMessageArea;
import com.oneplus.util.OpUtils;
import com.oneplus.util.VibratorSceneUtils;
public abstract class KeyguardAbsKeyInputView extends OpKeyguardAbsKeyInputView implements KeyguardSecurityView, EmergencyButton.EmergencyButtonCallback {
    protected KeyguardSecurityCallback mCallback;
    private CountDownTimer mCountdownTimer;
    private boolean mDismissing;
    protected View mEcaView;
    private View mEmergencyBubblePanel;
    private EmergencyButton mEmergencyButton;
    private OpEmergencyPanel mEmergencyPanel;
    private boolean mEmergencyPanelShow;
    protected boolean mEnableHaptics;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private boolean mLockOut;
    protected LockPatternUtils mLockPatternUtils;
    private int mMaxCountdownTimes;
    final OpEmergencyPanel.PanelCallback mPanelCallback;
    protected AsyncTask<?, ?, ?> mPendingLockCheck;
    protected boolean mResumed;
    protected SecurityMessageDisplay mSecurityMessageDisplay;
    private KeyguardUpdateMonitorCallback mUpdateCallback;

    /* access modifiers changed from: protected */
    public abstract LockscreenCredential getEnteredCredential();

    /* access modifiers changed from: protected */
    public abstract int getPasswordTextViewId();

    /* access modifiers changed from: protected */
    public abstract int getPromptReasonStringRes(int i);

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean needsInput() {
        return false;
    }

    /* access modifiers changed from: protected */
    public abstract void resetPasswordText(boolean z, boolean z2);

    /* access modifiers changed from: protected */
    public abstract void resetState();

    /* access modifiers changed from: protected */
    public abstract void setPasswordEntryEnabled(boolean z);

    /* access modifiers changed from: protected */
    public abstract void setPasswordEntryInputEnabled(boolean z);

    /* access modifiers changed from: protected */
    public boolean shouldLockout(long j) {
        return j != 0;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(Runnable runnable) {
        return false;
    }

    public KeyguardAbsKeyInputView(Context context) {
        this(context, null);
    }

    public KeyguardAbsKeyInputView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mCountdownTimer = null;
        this.mMaxCountdownTimes = 0;
        this.mLockOut = false;
        this.mPanelCallback = new OpEmergencyPanel.PanelCallback() { // from class: com.android.keyguard.KeyguardAbsKeyInputView.1
            @Override // com.oneplus.keyguard.OpEmergencyPanel.PanelCallback
            public void onTimeout() {
                super.onTimeout();
                KeyguardAbsKeyInputView.this.showEmergencyPanel(false);
            }

            @Override // com.oneplus.keyguard.OpEmergencyPanel.PanelCallback
            public void onDrop() {
                if (Build.DEBUG_ONEPLUS) {
                    Log.i("KeyguardAbsKeyInputView", "onDrop:" + KeyguardAbsKeyInputView.this.mEmergencyButton);
                }
                if (KeyguardAbsKeyInputView.this.mEmergencyButton != null) {
                    KeyguardAbsKeyInputView.this.mEmergencyButton.takeEmergencyCallAction();
                }
            }

            @Override // com.oneplus.keyguard.OpEmergencyPanel.PanelCallback
            public void onBubbleTouched() {
                KeyguardSecurityCallback keyguardSecurityCallback = KeyguardAbsKeyInputView.this.mCallback;
                if (keyguardSecurityCallback != null) {
                    keyguardSecurityCallback.userActivity();
                }
            }
        };
        this.mUpdateCallback = new KeyguardUpdateMonitorCallback(this) { // from class: com.android.keyguard.KeyguardAbsKeyInputView.2
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onFinishedGoingToSleep(int i) {
            }
        };
        this.mKeyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setKeyguardCallback(KeyguardSecurityCallback keyguardSecurityCallback) {
        this.mCallback = keyguardSecurityCallback;
        if (isShowEmergencyPanel()) {
            OpEmergencyPanel emergencyPanel = this.mCallback.getEmergencyPanel();
            this.mEmergencyPanel = emergencyPanel;
            if (emergencyPanel != null) {
                this.mEmergencyBubblePanel = emergencyPanel.findViewById(C0008R$id.bubble_panel);
            }
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
        this.mEnableHaptics = lockPatternUtils.isTactileFeedbackEnabled();
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void reset() {
        this.mDismissing = false;
        resetPasswordText(false, false);
        long lockoutAttemptDeadline = this.mLockPatternUtils.getLockoutAttemptDeadline(KeyguardUpdateMonitor.getCurrentUser());
        if (shouldLockout(lockoutAttemptDeadline)) {
            handleAttemptLockout(lockoutAttemptDeadline);
            return;
        }
        if (this.mLockOut) {
            KeyguardUpdateMonitor.getInstance(((LinearLayout) this).mContext).clearFailedUnlockAttempts(true);
            this.mLockOut = false;
        }
        resetState();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        this.mLockPatternUtils = new LockPatternUtils(((LinearLayout) this).mContext);
        this.mEcaView = findViewById(C0008R$id.keyguard_selector_fade_container);
        EmergencyButton emergencyButton = (EmergencyButton) findViewById(C0008R$id.emergency_call_button);
        this.mMaxCountdownTimes = ((LinearLayout) this).mContext.getResources().getInteger(C0009R$integer.config_max_unlock_countdown_times);
        if (emergencyButton != null) {
            emergencyButton.setCallback(this);
        }
        this.mEmergencyButton = emergencyButton;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mSecurityMessageDisplay = KeyguardMessageArea.findSecurityMessageDisplay(this);
        KeyguardUpdateMonitor.getInstance(((LinearLayout) this).mContext).registerCallback(this.mUpdateCallback);
    }

    @Override // com.android.keyguard.EmergencyButton.EmergencyButtonCallback
    public void onEmergencyButtonClickedWhenInCall() {
        this.mCallback.reset();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(((LinearLayout) this).mContext).removeCallback(this.mUpdateCallback);
    }

    /* access modifiers changed from: protected */
    public int getWrongPasswordStringId() {
        return C0015R$string.kg_wrong_password;
    }

    /* access modifiers changed from: protected */
    public void verifyPasswordAndUnlock() {
        if (!this.mDismissing) {
            final LockscreenCredential enteredCredential = getEnteredCredential();
            setPasswordEntryInputEnabled(false);
            if (this.mPendingLockCheck != null) {
                Log.d("KeyguardAbsKeyInputView", "verifyPasswordAndUnlock to cancel");
                this.mPendingLockCheck.cancel(false);
            }
            final int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            if (enteredCredential.size() <= 3) {
                setPasswordEntryInputEnabled(true);
                onPasswordChecked(currentUser, false, 0, false);
                enteredCredential.zeroize();
                return;
            }
            Log.d("KeyguardAbsKeyInputView", "checkPassword begin");
            if (LatencyTracker.isEnabled(((LinearLayout) this).mContext)) {
                LatencyTracker.getInstance(((LinearLayout) this).mContext).onActionStart(3);
                LatencyTracker.getInstance(((LinearLayout) this).mContext).onActionStart(4);
            }
            this.mKeyguardUpdateMonitor.setCredentialAttempted();
            this.mPendingLockCheck = LockPatternChecker.checkCredential(this.mLockPatternUtils, enteredCredential, currentUser, new LockPatternChecker.OnCheckCallback() { // from class: com.android.keyguard.KeyguardAbsKeyInputView.3
                public void onEarlyMatched() {
                    Log.d("KeyguardAbsKeyInputView", "onEarlyMatched: " + currentUser);
                    if (LatencyTracker.isEnabled(((LinearLayout) KeyguardAbsKeyInputView.this).mContext)) {
                        LatencyTracker.getInstance(((LinearLayout) KeyguardAbsKeyInputView.this).mContext).onActionEnd(3);
                    }
                    int fingerprintFailedUnlockAttempts = KeyguardUpdateMonitor.getInstance(((LinearLayout) KeyguardAbsKeyInputView.this).mContext).getFingerprintFailedUnlockAttempts();
                    KeyguardAbsKeyInputView keyguardAbsKeyInputView = KeyguardAbsKeyInputView.this;
                    keyguardAbsKeyInputView.mCallback.reportMDMEvent("lock_unlock_success", keyguardAbsKeyInputView.getSecurityModeLabel(), Integer.toString(fingerprintFailedUnlockAttempts));
                    KeyguardAbsKeyInputView.this.onPasswordChecked(currentUser, true, 0, true);
                    enteredCredential.zeroize();
                }

                public void onChecked(boolean z, int i) {
                    Log.d("KeyguardAbsKeyInputView", "onChecked, " + z + ", " + i);
                    if (!z) {
                        KeyguardAbsKeyInputView keyguardAbsKeyInputView = KeyguardAbsKeyInputView.this;
                        keyguardAbsKeyInputView.mCallback.reportMDMEvent("lock_unlock_failed", keyguardAbsKeyInputView.getSecurityModeLabel(), "1");
                    }
                    if (LatencyTracker.isEnabled(((LinearLayout) KeyguardAbsKeyInputView.this).mContext)) {
                        LatencyTracker.getInstance(((LinearLayout) KeyguardAbsKeyInputView.this).mContext).onActionEnd(4);
                    }
                    if (!z) {
                        KeyguardAbsKeyInputView.this.setPasswordEntryInputEnabled(true);
                    }
                    KeyguardAbsKeyInputView.this.setPasswordEntryInputEnabled(true);
                    KeyguardAbsKeyInputView keyguardAbsKeyInputView2 = KeyguardAbsKeyInputView.this;
                    keyguardAbsKeyInputView2.mPendingLockCheck = null;
                    if (!z) {
                        keyguardAbsKeyInputView2.onPasswordChecked(currentUser, false, i, true);
                    }
                    enteredCredential.zeroize();
                }

                public void onCancelled() {
                    if (LatencyTracker.isEnabled(((LinearLayout) KeyguardAbsKeyInputView.this).mContext)) {
                        LatencyTracker.getInstance(((LinearLayout) KeyguardAbsKeyInputView.this).mContext).onActionEnd(4);
                    }
                    enteredCredential.zeroize();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getSecurityModeLabel() {
        KeyguardSecurityModel.SecurityMode currentSecurityMode = this.mCallback.getCurrentSecurityMode();
        if (currentSecurityMode == KeyguardSecurityModel.SecurityMode.PIN) {
            return "pin";
        }
        return currentSecurityMode == KeyguardSecurityModel.SecurityMode.Password ? "password" : "";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPasswordChecked(int i, boolean z, int i2, boolean z2) {
        SecurityMessageDisplay securityMessageDisplay;
        boolean z3 = KeyguardUpdateMonitor.getCurrentUser() == i;
        if (z) {
            this.mLockPatternUtils.sanitizePassword();
            this.mCallback.reportUnlockAttempt(i, true, 0);
            if (z3) {
                this.mDismissing = true;
                this.mCallback.dismiss(true, i);
            }
        } else {
            if (z2) {
                this.mCallback.reportUnlockAttempt(i, false, i2);
                if (this.mMaxCountdownTimes <= 0 && i2 > 0) {
                    handleAttemptLockout(this.mLockPatternUtils.setLockoutAttemptDeadline(i, i2));
                }
            }
            if (i2 == 0 && (securityMessageDisplay = this.mSecurityMessageDisplay) != null) {
                securityMessageDisplay.setMessage(getWrongPasswordStringId(), 1);
            }
            Vibrator vibrator = (Vibrator) ((LinearLayout) this).mContext.getSystemService("vibrator");
            if (!OpUtils.isSupportLinearVibration()) {
                vibrator.vibrate(1000);
            } else if (VibratorSceneUtils.isVibratorSceneSupported(((LinearLayout) this).mContext, 1014)) {
                VibratorSceneUtils.doVibrateWithSceneMultipleTimes(((LinearLayout) this).mContext, vibrator, 1014, 80, 50, 2);
            }
        }
        resetPasswordText(true, !z);
    }

    /* access modifiers changed from: protected */
    public void handleAttemptLockout(long j) {
        setPasswordEntryEnabled(false);
        long elapsedRealtime = SystemClock.elapsedRealtime();
        KeyguardUpdateMonitor.getInstance(((LinearLayout) this).mContext).updateBiometricListeningState();
        KeyguardUpdateMonitor.getInstance(((LinearLayout) this).mContext).notifyPasswordLockout();
        KeyguardSecurityCallback keyguardSecurityCallback = this.mCallback;
        if (keyguardSecurityCallback != null) {
            keyguardSecurityCallback.hideSecurityIcon();
        }
        this.mLockOut = true;
        this.mCountdownTimer = new CountDownTimer(((long) Math.ceil(((double) (j - elapsedRealtime)) / 1000.0d)) * 1000, 1000) { // from class: com.android.keyguard.KeyguardAbsKeyInputView.4
            @Override // android.os.CountDownTimer
            public void onTick(long j2) {
                int round = (int) Math.round(((double) j2) / 1000.0d);
                KeyguardAbsKeyInputView keyguardAbsKeyInputView = KeyguardAbsKeyInputView.this;
                keyguardAbsKeyInputView.mSecurityMessageDisplay.setMessage(((LinearLayout) keyguardAbsKeyInputView).mContext.getResources().getQuantityString(C0013R$plurals.kg_too_many_failed_attempts_countdown, round, Integer.valueOf(round)));
            }

            @Override // android.os.CountDownTimer
            public void onFinish() {
                KeyguardAbsKeyInputView.this.resetState();
                KeyguardUpdateMonitor.getInstance(((LinearLayout) KeyguardAbsKeyInputView.this).mContext).clearFailedUnlockAttempts(true);
                KeyguardAbsKeyInputView.this.mLockOut = false;
                KeyguardUpdateMonitor.getInstance(((LinearLayout) KeyguardAbsKeyInputView.this).mContext).updateBiometricListeningState();
            }
        }.start();
    }

    /* access modifiers changed from: protected */
    public String getMessageWithCount(int i) {
        String string = getContext().getString(i);
        int failedUnlockAttempts = this.mMaxCountdownTimes - KeyguardUpdateMonitor.getInstance(((LinearLayout) this).mContext).getFailedUnlockAttempts(KeyguardUpdateMonitor.getCurrentUser());
        if (this.mMaxCountdownTimes <= 0 || failedUnlockAttempts <= 0) {
            return string;
        }
        return string + " - " + getContext().getResources().getString(C0015R$string.kg_remaining_attempts, Integer.valueOf(failedUnlockAttempts));
    }

    /* access modifiers changed from: protected */
    public void onUserInput() {
        KeyguardSecurityCallback keyguardSecurityCallback = this.mCallback;
        if (keyguardSecurityCallback != null) {
            keyguardSecurityCallback.userActivity();
            this.mCallback.onUserInput();
        }
        this.mSecurityMessageDisplay.setMessage("");
    }

    @Override // android.view.KeyEvent.Callback, android.view.View
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == 3) {
            KeyguardSecurityCallback keyguardSecurityCallback = this.mCallback;
            if (keyguardSecurityCallback != null) {
                keyguardSecurityCallback.userActivity();
            }
        } else if (i != 0) {
            if (i == 4) {
                KeyguardSecurityCallback keyguardSecurityCallback2 = this.mCallback;
                if (keyguardSecurityCallback2 != null) {
                    keyguardSecurityCallback2.userActivity();
                }
                return false;
            }
            onUserInput();
        }
        return false;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onPause() {
        this.mResumed = false;
        CountDownTimer countDownTimer = this.mCountdownTimer;
        if (countDownTimer != null) {
            countDownTimer.cancel();
            this.mCountdownTimer = null;
        }
        AsyncTask<?, ?, ?> asyncTask = this.mPendingLockCheck;
        if (asyncTask != null) {
            asyncTask.cancel(false);
            this.mPendingLockCheck = null;
        }
        reset();
        showEmergencyPanel(false, true);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onResume(int i) {
        this.mResumed = true;
        showEmergencyPanel(false, true);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showPromptReason(int i) {
        KeyguardSecurityCallback keyguardSecurityCallback;
        SecurityMessageDisplay securityMessageDisplay;
        long lockoutAttemptDeadline = this.mLockPatternUtils.getLockoutAttemptDeadline(KeyguardUpdateMonitor.getCurrentUser());
        long elapsedRealtime = SystemClock.elapsedRealtime();
        if (lockoutAttemptDeadline != 0 && lockoutAttemptDeadline > elapsedRealtime) {
            return;
        }
        if (!KeyguardUpdateMonitor.getInstance(((LinearLayout) this).mContext).isUserUnlocked() && !KeyguardUpdateMonitor.getInstance(((LinearLayout) this).mContext).isSimPinSecure()) {
            SecurityMessageDisplay securityMessageDisplay2 = this.mSecurityMessageDisplay;
            if (securityMessageDisplay2 != null) {
                securityMessageDisplay2.setMessage(C0015R$string.kg_first_unlock_instructions);
            }
        } else if (KeyguardUpdateMonitor.getInstance(((LinearLayout) this).mContext).isFacelockAvailable()) {
            SecurityMessageDisplay securityMessageDisplay3 = this.mSecurityMessageDisplay;
            if (securityMessageDisplay3 != null) {
                securityMessageDisplay3.setMessage(C0015R$string.face_unlock_notify_password);
            }
        } else if (i != 0) {
            int promptReasonStringRes = getPromptReasonStringRes(i);
            if (promptReasonStringRes != 0) {
                this.mSecurityMessageDisplay.setMessage(promptReasonStringRes);
            }
        } else if (OpUtils.isWeakFaceUnlockEnabled() && (keyguardSecurityCallback = this.mCallback) != null) {
            int i2 = 0;
            KeyguardSecurityModel.SecurityMode currentSecurityMode = keyguardSecurityCallback.getCurrentSecurityMode();
            if (currentSecurityMode == KeyguardSecurityModel.SecurityMode.PIN || currentSecurityMode == KeyguardSecurityModel.SecurityMode.Password) {
                i2 = KeyguardUpdateMonitor.getInstance(((LinearLayout) this).mContext).getBiometricTimeoutStringWhenPrompt(BiometricSourceType.FACE, currentSecurityMode);
            }
            Log.d("KeyguardAbsKeyInputView", "WeakFace, mode " + currentSecurityMode + ", " + i + ", resId:" + i2);
            if (i2 != 0 && (securityMessageDisplay = this.mSecurityMessageDisplay) != null) {
                securityMessageDisplay.setMessage(i2);
            }
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showMessage(CharSequence charSequence, ColorStateList colorStateList) {
        this.mSecurityMessageDisplay.setMessage(charSequence, OpKeyguardMessageArea.getOpMsgType(((LinearLayout) this).mContext, colorStateList));
    }

    public void doHapticKeyClick() {
        if (this.mEnableHaptics) {
            performHapticFeedback(1, 3);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean isCheckingPassword() {
        return this.mPendingLockCheck != null;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean onTouchEvent = super.onTouchEvent(motionEvent);
        notifyEmergencyPanelTouchEvent(motionEvent);
        return onTouchEvent;
    }

    @Override // com.android.keyguard.EmergencyButton.EmergencyButtonCallback
    public void onLaunchEmergencyPanel() {
        showEmergencyPanel(true);
    }

    private boolean isInArea(View view, MotionEvent motionEvent) {
        if (!(view == null || motionEvent == null)) {
            int[] iArr = new int[2];
            view.getLocationOnScreen(iArr);
            int i = iArr[0];
            int i2 = iArr[1];
            int width = view.getWidth();
            int height = view.getHeight();
            float rawY = motionEvent.getRawY();
            float rawX = motionEvent.getRawX();
            if (rawX <= ((float) (width + i)) && rawX >= ((float) i) && rawY <= ((float) (height + i2)) && rawY >= ((float) i2)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showEmergencyPanel(boolean z) {
        showEmergencyPanel(z, false);
    }

    private void showEmergencyPanel(boolean z, boolean z2) {
        if (this.mEmergencyPanel == null) {
            Log.i("KeyguardAbsKeyInputView", "showEmergencyPanel is null");
            return;
        }
        if (Build.DEBUG_ONEPLUS) {
            Log.i("KeyguardAbsKeyInputView", "showEmergencyPanel:" + z + " mEmergencyPanelShow:" + this.mEmergencyPanelShow);
        }
        this.mKeyguardUpdateMonitor.onEmergencyPanelExpandChanged(z);
        if (this.mEmergencyPanelShow != z || z2) {
            this.mEmergencyPanelShow = z;
            if (z) {
                this.mEmergencyPanel.setVisibility(0);
                this.mEmergencyPanel.onStart();
                this.mEmergencyPanel.addCallback(this.mPanelCallback);
                this.mEcaView.setVisibility(4);
                KeyguardSecurityCallback keyguardSecurityCallback = this.mCallback;
                if (keyguardSecurityCallback != null) {
                    keyguardSecurityCallback.userActivity();
                    return;
                }
                return;
            }
            this.mEmergencyPanel.setVisibility(4);
            this.mEmergencyPanel.onStop();
            this.mEmergencyPanel.removeCallback();
            this.mEcaView.setVisibility(0);
        }
    }

    @Override // com.android.keyguard.EmergencyButton.EmergencyButtonCallback
    public boolean isShowEmergencyPanel() {
        return OpUtils.isSupportEmergencyPanel();
    }

    private void notifyEmergencyPanelTouchEvent(MotionEvent motionEvent) {
        if (this.mEmergencyPanel != null && this.mEmergencyBubblePanel != null) {
            int action = motionEvent.getAction();
            if (isInArea(this.mEmergencyBubblePanel, motionEvent) || action != 0) {
                this.mEmergencyPanel.dispatchTouchEvent(motionEvent);
            } else {
                showEmergencyPanel(false);
            }
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onHidden() {
        showEmergencyPanel(false, true);
    }
}
