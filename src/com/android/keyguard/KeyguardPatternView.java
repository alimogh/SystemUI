package com.android.keyguard;

import android.animation.AnimatorSet;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.biometrics.BiometricSourceType;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Debug;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.LatencyTracker;
import com.android.internal.widget.LockPatternChecker;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.internal.widget.LockscreenCredential;
import com.android.keyguard.EmergencyButton;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardSecurityViewFlipper;
import com.android.settingslib.animation.AppearAnimationCreator;
import com.android.settingslib.animation.AppearAnimationUtils;
import com.android.settingslib.animation.DisappearAnimationUtils;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0013R$plurals;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.assist.ui.DisplayUtils;
import com.oneolus.anim.OpFadeAnim;
import com.oneplus.keyguard.OpEmergencyPanel;
import com.oneplus.keyguard.OpKeyguardMessageArea;
import com.oneplus.keyguard.OpKeyguardPatternView;
import com.oneplus.util.OpUtils;
import com.oneplus.util.VibratorSceneUtils;
import java.util.List;
public class KeyguardPatternView extends OpKeyguardPatternView implements KeyguardSecurityView, AppearAnimationCreator<LockPatternView.CellState>, EmergencyButton.EmergencyButtonCallback {
    private static final String DEBUG_SECURITY_ICON_HEIGHT = SystemProperties.get("debug.security.icon.pattern.height", "");
    private int MAX_RETRY_TIMES;
    private final AppearAnimationUtils mAppearAnimationUtils;
    private KeyguardSecurityCallback mCallback;
    private Runnable mCancelPatternRunnable;
    private ViewGroup mContainer;
    private CountDownTimer mCountdownTimer;
    private final DisappearAnimationUtils mDisappearAnimationUtils;
    private final DisappearAnimationUtils mDisappearAnimationUtilsLocked;
    private View mEcaView;
    private View mEmergencyBubblePanel;
    private EmergencyButton mEmergencyButton;
    private OpEmergencyPanel mEmergencyPanel;
    private boolean mEmergencyPanelShow;
    private View mFingerprintIcon;
    private Runnable mFinishRunnable;
    private boolean mIsMonitorRegistered;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private long mLastPokeTime;
    private boolean mLockOut;
    private final Rect mLockPatternScreenBounds;
    private LockPatternUtils mLockPatternUtils;
    private LockPatternView mLockPatternView;
    private int mMaxCountdownTimes;
    KeyguardUpdateMonitorCallback mMonitorCallback;
    int mOrientation;
    final OpEmergencyPanel.PanelCallback mPanelCallback;
    private AsyncTask<?, ?, ?> mPendingLockCheck;
    @VisibleForTesting
    KeyguardMessageArea mSecurityMessageDisplay;
    private int mSetRetryTimes;
    private final Runnable mStartWhenRelayoutFinishRunnable;
    private final Rect mTempRect;
    private final int[] mTmpPosition;
    private int mUsedScreenWidth;

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean needsInput() {
        return false;
    }

    static /* synthetic */ int access$2808(KeyguardPatternView keyguardPatternView) {
        int i = keyguardPatternView.mSetRetryTimes;
        keyguardPatternView.mSetRetryTimes = i + 1;
        return i;
    }

    public KeyguardPatternView(Context context) {
        this(context, null);
    }

    public KeyguardPatternView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTmpPosition = new int[2];
        this.mTempRect = new Rect();
        this.mLockPatternScreenBounds = new Rect();
        this.mCountdownTimer = null;
        this.mIsMonitorRegistered = false;
        this.mLastPokeTime = -7000;
        this.mCancelPatternRunnable = new Runnable() { // from class: com.android.keyguard.KeyguardPatternView.1
            @Override // java.lang.Runnable
            public void run() {
                KeyguardPatternView.this.mLockPatternView.clearPattern();
                KeyguardPatternView.this.mLockPatternView.setVisibility(0);
            }
        };
        this.mMaxCountdownTimes = 0;
        this.mLockOut = false;
        this.mPanelCallback = new OpEmergencyPanel.PanelCallback() { // from class: com.android.keyguard.KeyguardPatternView.2
            @Override // com.oneplus.keyguard.OpEmergencyPanel.PanelCallback
            public void onTimeout() {
                super.onTimeout();
                KeyguardPatternView.this.showEmergencyPanel(false);
            }

            @Override // com.oneplus.keyguard.OpEmergencyPanel.PanelCallback
            public void onDrop() {
                if (Build.DEBUG_ONEPLUS) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("onDrop:");
                    sb.append(KeyguardPatternView.this.mEmergencyButton != null);
                    Log.i("SecurityPatternView", sb.toString());
                }
                if (KeyguardPatternView.this.mEmergencyButton != null) {
                    KeyguardPatternView.this.mEmergencyButton.takeEmergencyCallAction();
                }
            }

            @Override // com.oneplus.keyguard.OpEmergencyPanel.PanelCallback
            public void onBubbleTouched() {
                if (KeyguardPatternView.this.mCallback != null) {
                    KeyguardPatternView.this.mCallback.userActivity();
                }
            }
        };
        this.mSetRetryTimes = 0;
        this.MAX_RETRY_TIMES = 5;
        this.mStartWhenRelayoutFinishRunnable = new Runnable() { // from class: com.android.keyguard.KeyguardPatternView.4
            @Override // java.lang.Runnable
            public void run() {
                KeyguardPatternView.access$2808(KeyguardPatternView.this);
                boolean z = true;
                boolean z2 = KeyguardPatternView.this.mSetRetryTimes >= KeyguardPatternView.this.MAX_RETRY_TIMES;
                if (!KeyguardPatternView.this.isLayoutRequested() && !KeyguardPatternView.this.mLockPatternView.isLayoutRequested()) {
                    z = false;
                }
                Log.i("SecurityPatternView", "layoutBusy:" + z + ", mSetRetryTimes:" + KeyguardPatternView.this.mSetRetryTimes + ", timeout:" + z2);
                if (!z) {
                    KeyguardPatternView.this.doStartAppearAnimation();
                    KeyguardPatternView.this.mSetRetryTimes = 0;
                } else if (!z || !z2) {
                    Log.i("SecurityPatternView", "postDelayed, mSetRetryTimes:" + KeyguardPatternView.this.mSetRetryTimes);
                    KeyguardPatternView.this.mLockPatternView.removeCallbacks(KeyguardPatternView.this.mStartWhenRelayoutFinishRunnable);
                    KeyguardPatternView.this.mLockPatternView.postDelayed(KeyguardPatternView.this.mStartWhenRelayoutFinishRunnable, 70);
                } else {
                    Log.i("SecurityPatternView", "warning: layoutBusy && timeout, isLayoutRequested():" + KeyguardPatternView.this.isLayoutRequested() + ", mLockPatternView.isLayoutRequested():" + KeyguardPatternView.this.mLockPatternView.isLayoutRequested());
                    KeyguardPatternView.this.mSetRetryTimes = 0;
                    KeyguardPatternView.this.setAlpha(1.0f);
                }
            }
        };
        this.mMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.keyguard.KeyguardPatternView.6
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onScreenTurnedOff() {
                super.onScreenTurnedOff();
                if (Build.DEBUG_ONEPLUS) {
                    Log.d("SecurityPatternView", "onScreenTurnedOff");
                }
                KeyguardPatternView.this.startFinishRunnable();
            }
        };
        this.mOrientation = 1;
        this.mKeyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mAppearAnimationUtils = new AppearAnimationUtils(context, 500, 1.5f, 2.0f, AnimationUtils.loadInterpolator(((LinearLayout) this).mContext, 17563662));
        this.mDisappearAnimationUtils = new DisappearAnimationUtils(context, 125, 1.2f, 0.6f, AnimationUtils.loadInterpolator(((LinearLayout) this).mContext, 17563663));
        this.mDisappearAnimationUtilsLocked = new DisappearAnimationUtils(context, 187, 1.2f, 0.6f, AnimationUtils.loadInterpolator(((LinearLayout) this).mContext, 17563663));
        getResources().getDimensionPixelSize(C0005R$dimen.disappear_y_translation);
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
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        if (lockPatternUtils == null) {
            lockPatternUtils = new LockPatternUtils(((LinearLayout) this).mContext);
        }
        this.mLockPatternUtils = lockPatternUtils;
        LockPatternView findViewById = findViewById(C0008R$id.lockPatternView);
        this.mLockPatternView = findViewById;
        findViewById.setSaveEnabled(false);
        this.mLockPatternView.setOnPatternListener(new UnlockPatternListener());
        this.mLockPatternView.setInStealthMode(!this.mLockPatternUtils.isVisiblePatternEnabled(KeyguardUpdateMonitor.getCurrentUser()));
        this.mLockPatternView.setTactileFeedbackEnabled(this.mLockPatternUtils.isTactileFeedbackEnabled());
        this.mLockPatternView.setInStealthMode(!this.mLockPatternUtils.isVisiblePatternEnabled(KeyguardUpdateMonitor.getCurrentUser()));
        this.mEcaView = findViewById(C0008R$id.keyguard_selector_fade_container);
        this.mContainer = (ViewGroup) findViewById(C0008R$id.container);
        this.mFingerprintIcon = findViewById(C0008R$id.fingerprint_icon);
        EmergencyButton emergencyButton = (EmergencyButton) findViewById(C0008R$id.emergency_call_button);
        if (emergencyButton != null) {
            emergencyButton.setCallback(this);
        }
        this.mEmergencyButton = emergencyButton;
        View findViewById2 = findViewById(C0008R$id.cancel_button);
        if (findViewById2 != null) {
            findViewById2.setOnClickListener(new View.OnClickListener() { // from class: com.android.keyguard.-$$Lambda$KeyguardPatternView$N-2kmt4uZ3ZvQBB4SmVDuZJ_Wqw
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    KeyguardPatternView.this.lambda$onFinishInflate$0$KeyguardPatternView(view);
                }
            });
        }
        updateLayoutParamForDisplayWidth();
        displayDefaultSecurityMessage();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$0 */
    public /* synthetic */ void lambda$onFinishInflate$0$KeyguardPatternView(View view) {
        this.mCallback.reset();
        this.mCallback.onCancelClicked();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mSecurityMessageDisplay = KeyguardMessageArea.findSecurityMessageDisplay(this);
    }

    @Override // com.android.keyguard.EmergencyButton.EmergencyButtonCallback
    public void onEmergencyButtonClickedWhenInCall() {
        this.mCallback.reset();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean onTouchEvent = super.onTouchEvent(motionEvent);
        long elapsedRealtime = SystemClock.elapsedRealtime() - this.mLastPokeTime;
        if (onTouchEvent && elapsedRealtime > 6900) {
            this.mLastPokeTime = SystemClock.elapsedRealtime();
        }
        this.mTempRect.set(0, 0, 0, 0);
        offsetRectIntoDescendantCoords(this.mLockPatternView, this.mTempRect);
        Rect rect = this.mTempRect;
        motionEvent.offsetLocation((float) rect.left, (float) rect.top);
        this.mLockPatternView.dispatchTouchEvent(motionEvent);
        Rect rect2 = this.mTempRect;
        motionEvent.offsetLocation((float) (-rect2.left), (float) (-rect2.top));
        notifyEmergencyPanelTouchEvent(motionEvent);
        return isInArea(this.mLockPatternView, motionEvent);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        this.mLockPatternView.getLocationOnScreen(this.mTmpPosition);
        Rect rect = this.mLockPatternScreenBounds;
        int[] iArr = this.mTmpPosition;
        rect.set(iArr[0] - 40, iArr[1] - 40, iArr[0] + this.mLockPatternView.getWidth() + 40, this.mTmpPosition[1] + this.mLockPatternView.getHeight() + 40);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void reset() {
        this.mLockPatternView.setInStealthMode(!this.mLockPatternUtils.isVisiblePatternEnabled(KeyguardUpdateMonitor.getCurrentUser()));
        this.mLockPatternView.enableInput();
        this.mLockPatternView.setEnabled(true);
        this.mLockPatternView.clearPattern();
        this.mLockPatternView.setVisibility(0);
        if (this.mSecurityMessageDisplay != null) {
            long lockoutAttemptDeadline = this.mLockPatternUtils.getLockoutAttemptDeadline(KeyguardUpdateMonitor.getCurrentUser());
            if (lockoutAttemptDeadline != 0) {
                handleAttemptLockout(lockoutAttemptDeadline);
                return;
            }
            if (this.mLockOut) {
                this.mKeyguardUpdateMonitor.clearFailedUnlockAttempts(true);
                this.mLockOut = false;
            }
            displayDefaultSecurityMessage();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void displayDefaultSecurityMessage() {
        int i = this.mKeyguardUpdateMonitor.isFirstUnlock() ? C0015R$string.kg_first_unlock_instructions : C0015R$string.kg_pattern_instructions;
        boolean isFacelockRecognizing = this.mKeyguardUpdateMonitor.isFacelockRecognizing();
        KeyguardMessageArea keyguardMessageArea = this.mSecurityMessageDisplay;
        if (keyguardMessageArea != null && !isFacelockRecognizing) {
            keyguardMessageArea.setMessage(getMessageWithCount(i));
        }
    }

    private class UnlockPatternListener implements LockPatternView.OnPatternListener {
        public void onPatternCleared() {
        }

        private UnlockPatternListener() {
        }

        public void onPatternStart() {
            KeyguardPatternView.this.mLockPatternView.setVisibility(0);
            KeyguardPatternView.this.mLockPatternView.removeCallbacks(KeyguardPatternView.this.mCancelPatternRunnable);
            KeyguardPatternView.this.mSecurityMessageDisplay.setMessage("");
        }

        public void onPatternCellAdded(List<LockPatternView.Cell> list) {
            KeyguardPatternView.this.mCallback.userActivity();
            KeyguardPatternView.this.mCallback.onUserInput();
        }

        public void onPatternDetected(List<LockPatternView.Cell> list) {
            KeyguardPatternView.this.mKeyguardUpdateMonitor.setCredentialAttempted();
            KeyguardPatternView.this.mLockPatternView.disableInput();
            if (KeyguardPatternView.this.mPendingLockCheck != null) {
                Log.d("SecurityPatternView", "onPatternDetected to cancel");
                KeyguardPatternView.this.mPendingLockCheck.cancel(false);
            }
            final int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            if (list.size() < 4) {
                KeyguardPatternView.this.mLockPatternView.enableInput();
                KeyguardMessageArea keyguardMessageArea = KeyguardPatternView.this.mSecurityMessageDisplay;
                if (keyguardMessageArea != null) {
                    keyguardMessageArea.setMessage(C0015R$string.kg_at_least_four_points, 1);
                }
                KeyguardPatternView.this.mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                onPatternChecked(currentUser, false, 0, false);
                return;
            }
            Log.d("SecurityPatternView", "checkPattern begin");
            if (LatencyTracker.isEnabled(((LinearLayout) KeyguardPatternView.this).mContext)) {
                LatencyTracker.getInstance(((LinearLayout) KeyguardPatternView.this).mContext).onActionStart(3);
                LatencyTracker.getInstance(((LinearLayout) KeyguardPatternView.this).mContext).onActionStart(4);
            }
            KeyguardPatternView keyguardPatternView = KeyguardPatternView.this;
            keyguardPatternView.mPendingLockCheck = LockPatternChecker.checkCredential(keyguardPatternView.mLockPatternUtils, LockscreenCredential.createPattern(list), currentUser, new LockPatternChecker.OnCheckCallback() { // from class: com.android.keyguard.KeyguardPatternView.UnlockPatternListener.1
                public void onEarlyMatched() {
                    Log.d("SecurityPatternView", "onEarlyMatched: " + currentUser);
                    if (LatencyTracker.isEnabled(((LinearLayout) KeyguardPatternView.this).mContext)) {
                        LatencyTracker.getInstance(((LinearLayout) KeyguardPatternView.this).mContext).onActionEnd(3);
                    }
                    KeyguardPatternView.this.mCallback.reportMDMEvent("lock_unlock_success", "pattern", Integer.toString(KeyguardPatternView.this.mKeyguardUpdateMonitor.getFingerprintFailedUnlockAttempts()));
                    UnlockPatternListener.this.onPatternChecked(currentUser, true, 0, true);
                }

                public void onChecked(boolean z, int i) {
                    if (LatencyTracker.isEnabled(((LinearLayout) KeyguardPatternView.this).mContext)) {
                        LatencyTracker.getInstance(((LinearLayout) KeyguardPatternView.this).mContext).onActionEnd(4);
                    }
                    Log.d("SecurityPatternView", "onChecked," + z + "," + i);
                    if (!z) {
                        KeyguardPatternView.this.mCallback.reportMDMEvent("lock_unlock_failed", "pattern", "1");
                    }
                    if (!z) {
                        KeyguardPatternView.this.mLockPatternView.enableInput();
                    }
                    KeyguardPatternView.this.mLockPatternView.enableInput();
                    KeyguardPatternView.this.mPendingLockCheck = null;
                    if (!z) {
                        UnlockPatternListener.this.onPatternChecked(currentUser, false, i, true);
                    }
                }

                public void onCancelled() {
                    if (LatencyTracker.isEnabled(((LinearLayout) KeyguardPatternView.this).mContext)) {
                        LatencyTracker.getInstance(((LinearLayout) KeyguardPatternView.this).mContext).onActionEnd(4);
                    }
                }
            });
            if (list.size() > 2) {
                KeyguardPatternView.this.mCallback.userActivity();
                KeyguardPatternView.this.mCallback.onUserInput();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void onPatternChecked(int i, boolean z, int i2, boolean z2) {
            boolean z3 = KeyguardUpdateMonitor.getCurrentUser() == i;
            if (Build.DEBUG_ONEPLUS) {
                Log.i("SecurityPatternView", "onPatternChecked, userId:" + i + ", matched:" + z + ", timeoutMs:" + i2 + ", isValidPattern:" + z2 + ", dismissKeyguard:" + z3);
            }
            if (z) {
                KeyguardPatternView.this.mLockPatternUtils.sanitizePassword();
                KeyguardPatternView.this.mCallback.reportUnlockAttempt(i, true, 0);
                if (z3) {
                    KeyguardPatternView.this.mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Correct);
                    KeyguardPatternView.this.mCallback.dismiss(true, i);
                    return;
                }
                return;
            }
            KeyguardPatternView.this.mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
            if (z2) {
                KeyguardPatternView.this.mCallback.reportUnlockAttempt(i, false, i2);
                if (KeyguardPatternView.this.mMaxCountdownTimes <= 0 && i2 > 0) {
                    KeyguardPatternView.this.handleAttemptLockout(KeyguardPatternView.this.mLockPatternUtils.setLockoutAttemptDeadline(i, i2));
                }
            }
            if (i2 == 0) {
                int failedUnlockAttempts = KeyguardPatternView.this.mKeyguardUpdateMonitor.getFailedUnlockAttempts(i);
                KeyguardPatternView keyguardPatternView = KeyguardPatternView.this;
                KeyguardMessageArea keyguardMessageArea = keyguardPatternView.mSecurityMessageDisplay;
                if (keyguardMessageArea != null) {
                    int i3 = failedUnlockAttempts % 5;
                    if (i3 == 3) {
                        keyguardMessageArea.setMessage(C0015R$string.kg_wrong_pattern_warning, 1);
                    } else if (i3 == 4) {
                        keyguardMessageArea.setMessage(C0015R$string.kg_wrong_pattern_warning_one, 1);
                    } else {
                        keyguardMessageArea.setMessage(keyguardPatternView.getMessageWithCount(C0015R$string.kg_wrong_pattern), 1);
                    }
                }
                KeyguardPatternView.this.mLockPatternView.postDelayed(KeyguardPatternView.this.mCancelPatternRunnable, 2000);
            }
            if (VibratorSceneUtils.isVibratorSceneSupported(((LinearLayout) KeyguardPatternView.this).mContext, 1014)) {
                VibratorSceneUtils.doVibrateWithSceneMultipleTimes(((LinearLayout) KeyguardPatternView.this).mContext, (Vibrator) ((LinearLayout) KeyguardPatternView.this).mContext.getSystemService("vibrator"), 1014, 0, 70, 3);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getMessageWithCount(int i) {
        String string = getContext().getString(i);
        int failedUnlockAttempts = this.mMaxCountdownTimes - KeyguardUpdateMonitor.getInstance(((LinearLayout) this).mContext).getFailedUnlockAttempts(KeyguardUpdateMonitor.getCurrentUser());
        if (this.mMaxCountdownTimes <= 0 || failedUnlockAttempts <= 0) {
            return string;
        }
        return string + " - " + getContext().getResources().getString(C0015R$string.kg_remaining_attempts, Integer.valueOf(failedUnlockAttempts));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAttemptLockout(long j) {
        this.mLockPatternView.clearPattern();
        this.mLockPatternView.setEnabled(false);
        this.mKeyguardUpdateMonitor.updateBiometricListeningState();
        this.mKeyguardUpdateMonitor.notifyPasswordLockout();
        KeyguardSecurityCallback keyguardSecurityCallback = this.mCallback;
        if (keyguardSecurityCallback != null) {
            keyguardSecurityCallback.hideSecurityIcon();
        }
        this.mLockOut = true;
        this.mCountdownTimer = new CountDownTimer(((long) Math.ceil(((double) (j - SystemClock.elapsedRealtime())) / 1000.0d)) * 1000, 1000) { // from class: com.android.keyguard.KeyguardPatternView.3
            @Override // android.os.CountDownTimer
            public void onTick(long j2) {
                int round = (int) Math.round(((double) j2) / 1000.0d);
                KeyguardPatternView keyguardPatternView = KeyguardPatternView.this;
                keyguardPatternView.mSecurityMessageDisplay.setMessage(((LinearLayout) keyguardPatternView).mContext.getResources().getQuantityString(C0013R$plurals.kg_too_many_failed_attempts_countdown, round, Integer.valueOf(round)));
            }

            @Override // android.os.CountDownTimer
            public void onFinish() {
                KeyguardPatternView.this.mLockPatternView.setEnabled(true);
                KeyguardPatternView.this.displayDefaultSecurityMessage();
                KeyguardPatternView.this.mKeyguardUpdateMonitor.clearFailedUnlockAttempts(true);
                KeyguardPatternView.this.mLockOut = false;
                KeyguardPatternView.this.mKeyguardUpdateMonitor.updateBiometricListeningState();
            }
        }.start();
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onPause() {
        if (this.mCountdownTimer != null) {
            Log.d("SecurityPatternView", "onPause to cancel CountdownTimer, " + Debug.getCallers(7));
            this.mCountdownTimer.cancel();
            this.mCountdownTimer = null;
        }
        if (this.mPendingLockCheck != null) {
            Log.d("SecurityPatternView", "onPause to cancel, " + Debug.getCallers(7));
            this.mPendingLockCheck.cancel(false);
            this.mPendingLockCheck = null;
        }
        displayDefaultSecurityMessage();
        reset();
        showEmergencyPanel(false, true);
        if (Build.DEBUG_ONEPLUS) {
            Log.d("SecurityPatternView", "onPause mIsMonitorRegistered:" + this.mIsMonitorRegistered);
        }
        if (this.mIsMonitorRegistered) {
            this.mKeyguardUpdateMonitor.removeCallback(this.mMonitorCallback);
            this.mIsMonitorRegistered = false;
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onResume(int i) {
        showEmergencyPanel(false, true);
        if (Build.DEBUG_ONEPLUS) {
            Log.d("SecurityPatternView", "onResume:" + i + " mIsMonitorRegistered:" + this.mIsMonitorRegistered);
        }
        if (!this.mIsMonitorRegistered && i == 1) {
            this.mKeyguardUpdateMonitor.registerCallback(this.mMonitorCallback);
            this.mIsMonitorRegistered = true;
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showPromptReason(int i) {
        long lockoutAttemptDeadline = this.mLockPatternUtils.getLockoutAttemptDeadline(KeyguardUpdateMonitor.getCurrentUser());
        long elapsedRealtime = SystemClock.elapsedRealtime();
        if (lockoutAttemptDeadline != 0 && lockoutAttemptDeadline > elapsedRealtime) {
            return;
        }
        if (!this.mKeyguardUpdateMonitor.isFirstUnlock() || KeyguardUpdateMonitor.getInstance(((LinearLayout) this).mContext).isSimPinSecure()) {
            if (OpUtils.DEBUG_ONEPLUS) {
                Log.i("SecurityPatternView", "showPromptReason , faceType:" + this.mKeyguardUpdateMonitor.getFacelockRunningType() + Debug.getCallers(5));
            }
            if (this.mKeyguardUpdateMonitor.isFacelockAvailable() && !this.mKeyguardUpdateMonitor.isFacelockRecognizing()) {
                KeyguardMessageArea keyguardMessageArea = this.mSecurityMessageDisplay;
                if (keyguardMessageArea != null) {
                    keyguardMessageArea.setMessage(C0015R$string.face_unlock_notify_pattern);
                }
            } else if (i == 0) {
                int biometricTimeoutStringWhenPrompt = this.mKeyguardUpdateMonitor.getBiometricTimeoutStringWhenPrompt(BiometricSourceType.FACE, KeyguardSecurityModel.SecurityMode.Pattern);
                if (OpUtils.isWeakFaceUnlockEnabled() && biometricTimeoutStringWhenPrompt != 0) {
                    Log.d("SecurityPatternView", "WeakFace pattern, " + i + ", " + biometricTimeoutStringWhenPrompt);
                    KeyguardMessageArea keyguardMessageArea2 = this.mSecurityMessageDisplay;
                    if (keyguardMessageArea2 != null) {
                        keyguardMessageArea2.setMessage(biometricTimeoutStringWhenPrompt);
                    }
                }
            } else if (i == 1) {
                this.mSecurityMessageDisplay.setMessage(C0015R$string.kg_prompt_reason_restart_pattern);
            } else if (i == 2) {
                this.mSecurityMessageDisplay.setMessage(C0015R$string.kg_prompt_reason_timeout_pattern);
            } else if (i == 3) {
                this.mSecurityMessageDisplay.setMessage(C0015R$string.kg_prompt_reason_device_admin);
            } else if (i == 4) {
                this.mSecurityMessageDisplay.setMessage(C0015R$string.kg_prompt_reason_user_request);
            } else if (i != 6) {
                this.mSecurityMessageDisplay.setMessage(C0015R$string.kg_prompt_reason_timeout_pattern);
            } else {
                this.mSecurityMessageDisplay.setMessage(C0015R$string.kg_prompt_reason_prepare_for_update_pattern);
            }
        } else {
            KeyguardMessageArea keyguardMessageArea3 = this.mSecurityMessageDisplay;
            if (keyguardMessageArea3 != null) {
                keyguardMessageArea3.setMessage(getMessageWithCount(C0015R$string.kg_first_unlock_instructions));
            }
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showMessage(CharSequence charSequence, ColorStateList colorStateList) {
        if (colorStateList != null) {
            this.mSecurityMessageDisplay.setNextMessageColor(colorStateList);
        }
        if (colorStateList != null) {
            this.mSecurityMessageDisplay.setMessage(charSequence, OpKeyguardMessageArea.getOpMsgType(((LinearLayout) this).mContext, colorStateList));
        }
    }

    private void postAnimationDelay() {
        this.mLockPatternView.post(this.mStartWhenRelayoutFinishRunnable);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doStartAppearAnimation() {
        AnimatorSet fadeInOutVisibilityAnimation;
        enableClipping(false);
        setAlpha(1.0f);
        setTranslationY(this.mAppearAnimationUtils.getStartTranslation());
        AppearAnimationUtils.startTranslationYAnimation(this, 0, 500, 0.0f, this.mAppearAnimationUtils.getInterpolator());
        this.mAppearAnimationUtils.startAnimation2d(this.mLockPatternView.getCellStates(), new Runnable() { // from class: com.android.keyguard.KeyguardPatternView.5
            @Override // java.lang.Runnable
            public void run() {
                KeyguardPatternView.this.enableClipping(true);
            }
        }, this);
        if (!TextUtils.isEmpty(this.mSecurityMessageDisplay.getText())) {
            AppearAnimationUtils appearAnimationUtils = this.mAppearAnimationUtils;
            appearAnimationUtils.createAnimation((View) this.mSecurityMessageDisplay, 0L, 220L, appearAnimationUtils.getStartTranslation(), true, this.mAppearAnimationUtils.getInterpolator(), (Runnable) null);
        }
        View view = this.mFingerprintIcon;
        if (view != null && view.getVisibility() == 0 && (fadeInOutVisibilityAnimation = OpFadeAnim.getFadeInOutVisibilityAnimation(this.mFingerprintIcon, 0, null, true)) != null) {
            fadeInOutVisibilityAnimation.start();
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void startAppearAnimation() {
        if (Build.DEBUG_ONEPLUS) {
            Log.i("SecurityPatternView", "startAppearAnimation, before  DisplayUtils.getWidth:" + DisplayUtils.getWidth(((LinearLayout) this).mContext) + ", mLockPatternView.isLayoutRequested:" + this.mLockPatternView.isLayoutRequested() + ", isLayoutRequested:" + isLayoutRequested());
        }
        setAlpha(0.0f);
        postAnimationDelay();
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(Runnable runnable) {
        DisappearAnimationUtils disappearAnimationUtils;
        float f = this.mKeyguardUpdateMonitor.needsSlowUnlockTransition() ? 1.5f : 1.0f;
        this.mLockPatternView.clearPattern();
        enableClipping(false);
        setTranslationY(0.0f);
        AppearAnimationUtils.startTranslationYAnimation(this, 0, (long) (300.0f * f), -this.mDisappearAnimationUtils.getStartTranslation(), this.mDisappearAnimationUtils.getInterpolator());
        if (Build.DEBUG_ONEPLUS) {
            Log.i("SecurityPatternView", "startDisappearAnimation");
        }
        this.mFinishRunnable = runnable;
        if (this.mKeyguardUpdateMonitor.needsSlowUnlockTransition()) {
            disappearAnimationUtils = this.mDisappearAnimationUtilsLocked;
        } else {
            disappearAnimationUtils = this.mDisappearAnimationUtils;
        }
        disappearAnimationUtils.startAnimation2d(this.mLockPatternView.getCellStates(), new Runnable() { // from class: com.android.keyguard.-$$Lambda$KeyguardPatternView$G0EMymZcEbjMZR18rSqGXuoXaF8
            @Override // java.lang.Runnable
            public final void run() {
                KeyguardPatternView.this.lambda$startDisappearAnimation$1$KeyguardPatternView();
            }
        }, this);
        if (!TextUtils.isEmpty(this.mSecurityMessageDisplay.getText())) {
            DisappearAnimationUtils disappearAnimationUtils2 = this.mDisappearAnimationUtils;
            disappearAnimationUtils2.createAnimation((View) this.mSecurityMessageDisplay, 0L, (long) (f * 200.0f), (-disappearAnimationUtils2.getStartTranslation()) * 3.0f, false, this.mDisappearAnimationUtils.getInterpolator(), (Runnable) null);
        }
        View view = this.mFingerprintIcon;
        if (view == null || view.getVisibility() != 0) {
            return true;
        }
        DisappearAnimationUtils disappearAnimationUtils3 = this.mDisappearAnimationUtils;
        disappearAnimationUtils3.createAnimation(this.mFingerprintIcon, 0L, 200L, (-disappearAnimationUtils3.getStartTranslation()) * 3.0f, false, this.mDisappearAnimationUtils.getInterpolator(), (Runnable) null);
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startDisappearAnimation$1 */
    public /* synthetic */ void lambda$startDisappearAnimation$1$KeyguardPatternView() {
        enableClipping(true);
        if (Build.DEBUG_ONEPLUS) {
            Log.i("SecurityPatternView", " disappearAnimationUtils end:");
        }
        startFinishRunnable();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startFinishRunnable() {
        if (this.mFinishRunnable != null) {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("SecurityPatternView", "startFinishRunnable");
            }
            this.mFinishRunnable.run();
            this.mFinishRunnable = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enableClipping(boolean z) {
        setClipChildren(z);
        this.mContainer.setClipToPadding(z);
        this.mContainer.setClipChildren(z);
    }

    public void createAnimation(LockPatternView.CellState cellState, long j, long j2, float f, boolean z, Interpolator interpolator, Runnable runnable) {
        this.mLockPatternView.startCellStateAnimation(cellState, 1.0f, z ? 1.0f : 0.0f, z ? f : 0.0f, z ? 0.0f : f, z ? 0.0f : 1.0f, 1.0f, j, j2, interpolator, runnable);
        if (runnable != null) {
            this.mAppearAnimationUtils.createAnimation(this.mEcaView, j, j2, f, z, interpolator, (Runnable) null);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public CharSequence getTitle() {
        return getContext().getString(17040373);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean isCheckingPassword() {
        return this.mPendingLockCheck != null;
    }

    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        if (this.mOrientation != configuration.orientation) {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("SecurityPatternView", "onConfigurationChanged forom:" + this.mOrientation + " to:" + configuration.orientation);
            }
            int i = configuration.orientation;
            this.mOrientation = i;
            if (i == 1) {
                this.mLockPatternView.setVisibility(4);
                this.mLockPatternView.removeCallbacks(this.mCancelPatternRunnable);
                this.mLockPatternView.postDelayed(this.mCancelPatternRunnable, 800);
            }
        }
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
            Log.i("SecurityPatternView", "showEmergencyPanel is null");
            return;
        }
        Log.i("SecurityPatternView", "showEmergencyPanel:" + z + " mEmergencyPanelShow:" + this.mEmergencyPanelShow);
        this.mKeyguardUpdateMonitor.onEmergencyPanelExpandChanged(z);
        if (z2 || this.mEmergencyPanelShow != z) {
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
    public void onLaunchEmergencyPanel() {
        showEmergencyPanel(true);
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

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        updateLayoutParamForDisplayWidth();
    }

    private void updateLayoutParamForDisplayWidth() {
        if (!DEBUG_SECURITY_ICON_HEIGHT.isEmpty()) {
            ViewGroup.LayoutParams layoutParams = this.mFingerprintIcon.getLayoutParams();
            layoutParams.height = OpUtils.convertPxByResolutionProportion((float) Integer.valueOf(DEBUG_SECURITY_ICON_HEIGHT).intValue(), 1080);
            this.mFingerprintIcon.setLayoutParams(layoutParams);
        }
        int width = DisplayUtils.getWidth(((LinearLayout) this).mContext);
        if (this.mUsedScreenWidth != width) {
            Log.i("SecurityPatternView", "updateLayoutParamForDisplayWidth, displayWidth:" + width + ", mUsedScreenWidth:" + this.mUsedScreenWidth + ", callers: " + Debug.getCallers(2));
            StringBuilder sb = new StringBuilder();
            sb.append("op_keyguard_clock_YRegular:");
            sb.append(((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_YRegular));
            Log.i("SecurityPatternView", sb.toString());
            this.mUsedScreenWidth = width;
            if (width > 1080) {
                KeyguardSecurityViewFlipper.LayoutParams layoutParams2 = (KeyguardSecurityViewFlipper.LayoutParams) getLayoutParams();
                layoutParams2.maxHeight = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_security_max_height), 1080);
                layoutParams2.maxWidth = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_security_width), 1080);
                setLayoutParams(layoutParams2);
                int convertPxByResolutionProportion = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.fingerprint_icon_padding), 1080);
                this.mFingerprintIcon.setPaddingRelative(convertPxByResolutionProportion, convertPxByResolutionProportion, convertPxByResolutionProportion, convertPxByResolutionProportion);
                ViewGroup.LayoutParams layoutParams3 = this.mFingerprintIcon.getLayoutParams();
                layoutParams3.height = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_pattern_view_fingerprint_icon_framelayout_height), 1080);
                this.mFingerprintIcon.setLayoutParams(layoutParams3);
                ImageView imageView = (ImageView) findViewById(C0008R$id.security_image);
                ViewGroup.LayoutParams layoutParams4 = imageView.getLayoutParams();
                layoutParams4.height = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_security_image_height), 1080);
                layoutParams4.width = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_security_image_width), 1080);
                imageView.setLayoutParams(layoutParams4);
                if (this.mContainer != null && this.mLockPatternView != null) {
                    if (Build.DEBUG_ONEPLUS) {
                        Log.i("SecurityPatternView", "relayout mContainer");
                    }
                    this.mLockPatternView.clearAnimation();
                    this.mLockPatternView.callOnClick();
                    this.mContainer.requestLayout();
                }
            }
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onHidden() {
        showEmergencyPanel(false, true);
    }
}
