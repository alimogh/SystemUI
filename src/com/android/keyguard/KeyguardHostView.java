package com.android.keyguard;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import androidx.constraintlayout.widget.R$styleable;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityContainer;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.settingslib.Utils;
import com.android.systemui.C0003R$bool;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.oneplus.keyguard.OpKeyguardHostView;
import com.oneplus.util.OpUtils;
import java.io.File;
public class KeyguardHostView extends OpKeyguardHostView implements KeyguardSecurityContainer.SecurityCallback {
    private AudioManager mAudioManager;
    private Runnable mCancelAction;
    private ActivityStarter.OnDismissAction mDismissAction;
    protected LockPatternUtils mLockPatternUtils;
    protected KeyguardSecurityContainer mSecurityContainer;
    private TelephonyManager mTelephonyManager;
    private int mType;
    private final KeyguardUpdateMonitorCallback mUpdateCallback;
    protected ViewMediatorCallback mViewMediatorCallback;

    public KeyguardHostView(Context context) {
        this(context, null);
    }

    public KeyguardHostView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTelephonyManager = null;
        this.mType = 0;
        this.mUpdateCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.keyguard.KeyguardHostView.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onUserSwitchComplete(int i) {
                KeyguardHostView.this.getSecurityContainer().showPrimarySecurityScreen(false);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onTrustGrantedWithFlags(int i, int i2) {
                if (i2 == KeyguardUpdateMonitor.getCurrentUser() && KeyguardHostView.this.isAttachedToWindow()) {
                    boolean isVisibleToUser = KeyguardHostView.this.isVisibleToUser();
                    boolean z = true;
                    boolean z2 = (i & 1) != 0;
                    if ((i & 2) == 0) {
                        z = false;
                    }
                    if (!z2 && !z) {
                        return;
                    }
                    if (!KeyguardHostView.this.mViewMediatorCallback.isScreenOn() || (!isVisibleToUser && !z)) {
                        KeyguardHostView.this.mViewMediatorCallback.playTrustedSound();
                        return;
                    }
                    if (!isVisibleToUser) {
                        Log.i("KeyguardViewBase", "TrustAgent dismissed Keyguard.");
                    }
                    KeyguardHostView.this.dismiss(false, i2, false);
                }
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onKeyguardBouncerChanged(boolean z) {
                if (!KeyguardUpdateMonitor.getInstance(((FrameLayout) KeyguardHostView.this).mContext).isKeyguardVisible() && z && KeyguardHostView.this.mSecurityContainer != null) {
                    Log.d("KeyguardViewBase", "update security icon when occluded");
                    KeyguardHostView.this.mSecurityContainer.updateSecurityIcon();
                }
            }

            @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
            public void onFacelockStateChanged(int i) {
                if (KeyguardHostView.this.isVisibleToUser() && KeyguardHostView.this.mType != i && i == 0) {
                    KeyguardHostView keyguardHostView = KeyguardHostView.this;
                    if (keyguardHostView.mSecurityContainer != null && !KeyguardUpdateMonitor.getInstance(((FrameLayout) keyguardHostView).mContext).isFacelockUnlocking()) {
                        KeyguardHostView.this.mSecurityContainer.updateSecurityIcon();
                    }
                }
                KeyguardHostView.this.mType = i;
            }
        };
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mUpdateCallback);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        ViewMediatorCallback viewMediatorCallback = this.mViewMediatorCallback;
        if (viewMediatorCallback != null) {
            viewMediatorCallback.keyguardDoneDrawing();
        }
    }

    public void setOnDismissAction(ActivityStarter.OnDismissAction onDismissAction, Runnable runnable) {
        Runnable runnable2 = this.mCancelAction;
        if (runnable2 != null) {
            runnable2.run();
            this.mCancelAction = null;
        }
        this.mDismissAction = onDismissAction;
        this.mCancelAction = runnable;
    }

    public boolean hasDismissActions() {
        return (this.mDismissAction == null && this.mCancelAction == null) ? false : true;
    }

    public void cancelDismissAction() {
        setOnDismissAction(null, null);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        this.mSecurityContainer = (KeyguardSecurityContainer) findViewById(C0008R$id.keyguard_security_container);
        LockPatternUtils lockPatternUtils = new LockPatternUtils(((FrameLayout) this).mContext);
        this.mLockPatternUtils = lockPatternUtils;
        this.mSecurityContainer.setLockPatternUtils(lockPatternUtils);
        this.mSecurityContainer.setSecurityCallback(this);
        this.mSecurityContainer.showPrimarySecurityScreen(false);
        this.mKeyguardSecurityNavigationSpace = findViewById(C0008R$id.keyguard_security_navigation_space);
        updateNavigationSpace();
        if (OpUtils.isCustomFingerprint() && this.mSecurityContainer.getCurrentSecuritySelection() != KeyguardSecurityModel.SecurityMode.Password) {
            setPaddingRelative(getPaddingStart(), getResources().getDimensionPixelSize(C0005R$dimen.emergency_button_margin_top) - getResources().getDimensionPixelSize(C0005R$dimen.keyguard_security_view_top_margin), getPaddingEnd(), getPaddingBottom());
        }
    }

    public void showPrimarySecurityScreen() {
        Log.d("KeyguardViewBase", "show()");
        this.mSecurityContainer.showPrimarySecurityScreen(false);
        updateNavigationSpace();
    }

    public KeyguardSecurityView getCurrentSecurityView() {
        KeyguardSecurityContainer keyguardSecurityContainer = this.mSecurityContainer;
        if (keyguardSecurityContainer != null) {
            return keyguardSecurityContainer.getCurrentSecurityView();
        }
        return null;
    }

    public void showPromptReason(int i) {
        this.mSecurityContainer.showPromptReason(i);
    }

    public void showMessage(CharSequence charSequence, ColorStateList colorStateList) {
        this.mSecurityContainer.showMessage(charSequence, colorStateList);
    }

    public void showErrorMessage(CharSequence charSequence) {
        showMessage(charSequence, Utils.getColorError(((FrameLayout) this).mContext));
    }

    public boolean dismiss(int i) {
        return dismiss(false, i, false);
    }

    /* access modifiers changed from: protected */
    public KeyguardSecurityContainer getSecurityContainer() {
        return this.mSecurityContainer;
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public boolean dismiss(boolean z, int i, boolean z2) {
        return this.mSecurityContainer.showNextSecurityScreenOrFinish(z, i, z2);
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void finish(boolean z, int i) {
        boolean z2;
        ActivityStarter.OnDismissAction onDismissAction = this.mDismissAction;
        if (onDismissAction != null) {
            z2 = onDismissAction.onDismiss();
            this.mDismissAction = null;
            this.mCancelAction = null;
        } else {
            z2 = false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("finish, deferKeyguardDone:");
        sb.append(z2);
        sb.append(", mViewMediatorCallback:");
        sb.append(this.mViewMediatorCallback == null ? "null" : "not null");
        Log.d("KeyguardViewBase", sb.toString());
        ViewMediatorCallback viewMediatorCallback = this.mViewMediatorCallback;
        if (viewMediatorCallback == null) {
            return;
        }
        if (z2) {
            viewMediatorCallback.keyguardDonePending(z, i);
        } else {
            viewMediatorCallback.keyguardDone(z, i);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void reset() {
        this.mViewMediatorCallback.resetKeyguard();
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void onCancelClicked() {
        this.mViewMediatorCallback.onCancelClicked();
    }

    public void resetSecurityContainer() {
        this.mSecurityContainer.reset();
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void onSecurityModeChanged(KeyguardSecurityModel.SecurityMode securityMode, boolean z) {
        ViewMediatorCallback viewMediatorCallback = this.mViewMediatorCallback;
        if (viewMediatorCallback != null) {
            viewMediatorCallback.setNeedsInput(z);
        }
    }

    public CharSequence getAccessibilityTitleForCurrentMode() {
        return this.mSecurityContainer.getTitle();
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void userActivity() {
        ViewMediatorCallback viewMediatorCallback = this.mViewMediatorCallback;
        if (viewMediatorCallback != null) {
            viewMediatorCallback.userActivity();
        }
    }

    public void onPause() {
        Log.d("KeyguardViewBase", String.format("screen off, instance %s at %s", Integer.toHexString(hashCode()), Long.valueOf(SystemClock.uptimeMillis())));
        this.mSecurityContainer.showPrimarySecurityScreen(true);
        this.mSecurityContainer.onPause();
        clearFocus();
    }

    public void onResume() {
        Log.d("KeyguardViewBase", "screen on, instance " + Integer.toHexString(hashCode()));
        this.mSecurityContainer.onResume(1);
        requestFocus();
    }

    public void startAppearAnimation() {
        this.mSecurityContainer.startAppearAnimation();
    }

    public void startDisappearAnimation(Runnable runnable) {
        if (!this.mSecurityContainer.startDisappearAnimation(runnable) && runnable != null) {
            runnable.run();
        }
    }

    public void cleanUp() {
        getSecurityContainer().onPause();
    }

    @Override // android.view.View, android.view.ViewGroup
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (interceptMediaKey(keyEvent)) {
            return true;
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    public boolean interceptMediaKey(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        if (keyEvent.getAction() == 0) {
            if (!(keyCode == 79 || keyCode == 130 || keyCode == 222)) {
                if (!(keyCode == 126 || keyCode == 127)) {
                    switch (keyCode) {
                        case 85:
                            break;
                        case 86:
                        case 87:
                        case 88:
                        case 89:
                        case R$styleable.Constraint_layout_constraintVertical_chainStyle /* 90 */:
                        case R$styleable.Constraint_layout_constraintVertical_weight /* 91 */:
                            break;
                        default:
                            return false;
                    }
                }
                if (this.mTelephonyManager == null) {
                    this.mTelephonyManager = (TelephonyManager) getContext().getSystemService("phone");
                }
                TelephonyManager telephonyManager = this.mTelephonyManager;
                if (!(telephonyManager == null || telephonyManager.getCallState() == 0)) {
                    return true;
                }
            }
            handleMediaKeyEvent(keyEvent);
            return true;
        } else if (keyEvent.getAction() != 1) {
            return false;
        } else {
            if (!(keyCode == 79 || keyCode == 130 || keyCode == 222 || keyCode == 126 || keyCode == 127)) {
                switch (keyCode) {
                    case 85:
                    case 86:
                    case 87:
                    case 88:
                    case 89:
                    case R$styleable.Constraint_layout_constraintVertical_chainStyle /* 90 */:
                    case R$styleable.Constraint_layout_constraintVertical_weight /* 91 */:
                        break;
                    default:
                        return false;
                }
            }
            handleMediaKeyEvent(keyEvent);
            return true;
        }
    }

    private void handleMediaKeyEvent(KeyEvent keyEvent) {
        synchronized (this) {
            if (this.mAudioManager == null) {
                this.mAudioManager = (AudioManager) getContext().getSystemService("audio");
            }
        }
        this.mAudioManager.dispatchMediaKeyEvent(keyEvent);
    }

    public boolean shouldEnableMenuKey() {
        return !getResources().getBoolean(C0003R$bool.config_disableMenuKeyInLockScreen) || ActivityManager.isRunningInTestHarness() || new File("/data/local/enable_menu_key").exists();
    }

    public void setViewMediatorCallback(ViewMediatorCallback viewMediatorCallback) {
        this.mViewMediatorCallback = viewMediatorCallback;
        viewMediatorCallback.setNeedsInput(this.mSecurityContainer.needsInput());
    }

    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
        this.mSecurityContainer.setLockPatternUtils(lockPatternUtils);
    }

    public KeyguardSecurityModel.SecurityMode getSecurityMode() {
        return this.mSecurityContainer.getSecurityMode();
    }

    public KeyguardSecurityModel.SecurityMode getCurrentSecurityMode() {
        return this.mSecurityContainer.getCurrentSecurityMode();
    }

    public void onStartingToHide() {
        this.mSecurityContainer.onStartingToHide();
    }

    public boolean isCheckingPassword() {
        return this.mSecurityContainer.isCheckingPassword();
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void reportMDMEvent(String str, String str2, String str3) {
        this.mViewMediatorCallback.reportMDMEvent(str, str2, str3);
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void tryToStartFaceLockFromBouncer() {
        this.mViewMediatorCallback.tryToStartFaceLockFromBouncer();
    }
}
