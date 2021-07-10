package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0000R$anim;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.oneplus.phone.OpTrustDrawable;
import com.oneplus.util.OpReflectionUtils;
import com.oneplus.util.OpUtils;
public class OpLockscreenLockIconController {
    protected Context mContext;
    protected boolean mDeviceInteractive;
    private Animation mFacelockAnimationSet;
    private Animation mFacelockFailAnimationSet;
    private AnimatorSet mFacelockRetryAnimationSet;
    private int mFacelockRunningType = 0;
    protected Handler mHandler;
    protected boolean mLastDeviceInteractive;
    private LockPatternUtils mLockPatternUtils;
    protected final Runnable mPaddingRetryRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$OpLockscreenLockIconController$CZhdpKDblH5uV1JYAsyTk1evmXo
        @Override // java.lang.Runnable
        public final void run() {
            OpLockscreenLockIconController.this.lambda$new$0$OpLockscreenLockIconController();
        }
    };
    protected OpTrustDrawable mTrustDrawable;

    /* access modifiers changed from: protected */
    public void initOpLockscreenLockIconController() {
        this.mHandler = new Handler();
        this.mTrustDrawable = new OpTrustDrawable(this.mContext);
        this.mFacelockAnimationSet = AnimationUtils.loadAnimation(this.mContext, C0000R$anim.facelock_lock_blink);
        this.mFacelockFailAnimationSet = AnimationUtils.loadAnimation(this.mContext, C0000R$anim.facelock_lock_fail_blink);
        this.mFacelockRetryAnimationSet = getFacelockRetryAnimator();
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
    }

    private AnimatorSet getFacelockRetryAnimator() {
        AnimatorSet animatorSet = new AnimatorSet();
        AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(1.0f, 0.5f);
        ofFloat.setDuration(150L);
        ofFloat.setInterpolator(accelerateInterpolator);
        ofFloat.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.statusbar.phone.OpLockscreenLockIconController.1
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                if (Build.DEBUG_ONEPLUS) {
                    Log.d("LockIcon", "zoomOutAnimtor start");
                }
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (Build.DEBUG_ONEPLUS) {
                    Log.d("LockIcon", "zoomOutAnimtor end");
                }
            }
        });
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.phone.OpLockscreenLockIconController.2
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                OpLockscreenLockIconController.this.getLockIcon().setScaleX(floatValue);
                OpLockscreenLockIconController.this.getLockIcon().setScaleY(floatValue);
            }
        });
        DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(0.5f, 1.0f);
        ofFloat2.setDuration(150L);
        ofFloat2.setInterpolator(decelerateInterpolator);
        ofFloat2.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.phone.OpLockscreenLockIconController.3
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                if (OpLockscreenLockIconController.this.getLastState() == 12) {
                    if (Build.DEBUG_ONEPLUS) {
                        Log.d("LockIcon", "zoomInAnimtor start");
                    }
                    OpLockscreenLockIconController.this.setRetryIcon();
                }
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (Build.DEBUG_ONEPLUS) {
                    Log.d("LockIcon", "zoomInAnimtor end");
                }
            }
        });
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.phone.OpLockscreenLockIconController.4
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                OpLockscreenLockIconController.this.getLockIcon().setScaleX(floatValue);
                OpLockscreenLockIconController.this.getLockIcon().setScaleY(floatValue);
            }
        });
        animatorSet.playSequentially(ofFloat, ofFloat2);
        animatorSet.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.phone.OpLockscreenLockIconController.5
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                OpLockscreenLockIconController.this.getLockIcon().setScaleX(1.0f);
                OpLockscreenLockIconController.this.getLockIcon().setScaleY(1.0f);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                OpLockscreenLockIconController.this.getLockIcon().setScaleX(1.0f);
                OpLockscreenLockIconController.this.getLockIcon().setScaleY(1.0f);
            }
        });
        return animatorSet;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRetryIcon() {
        getLockIcon().setBackground(this.mContext.getDrawable(C0006R$drawable.op_facelock_lock_ripple_drawable));
        boolean isFacelockWaitingTap = KeyguardUpdateMonitor.getInstance(this.mContext).isFacelockWaitingTap();
        if (Build.DEBUG_ONEPLUS) {
            Log.d("LockIcon", "setRetryIcon, " + isFacelockWaitingTap);
        }
        if (!isFacelockWaitingTap) {
            getLockIcon().setImageDrawable(this.mContext.getDrawable(C0006R$drawable.facelock_refresh_fod), false);
        } else {
            getLockIcon().setImageDrawable(this.mContext.getDrawable(C0006R$drawable.facelock_lock_icon_fod), false);
        }
    }

    public void opSetScreenOn(boolean z) {
        if (!z) {
            if (this.mFacelockRetryAnimationSet.isStarted()) {
                this.mFacelockRetryAnimationSet.cancel();
            }
            this.mHandler.removeCallbacks(this.mPaddingRetryRunnable);
        }
    }

    public void setFacelockRunning(int i, boolean z) {
        if (this.mFacelockRunningType != i) {
            Log.d("LockIcon", "setFacelockRunning , type:" + i + ", updateIcon:" + z);
            this.mFacelockRunningType = i;
            if (z) {
                opUpdate();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void opUpdateIconAnimation(final View view, int i) {
        if (this.mFacelockAnimationSet != null && this.mFacelockFailAnimationSet != null) {
            if (!KeyguardUpdateMonitor.getInstance(this.mContext).isFacelockRecognizing()) {
                if (Build.DEBUG_ONEPLUS) {
                    Log.d("LockIcon", "stop recog anim");
                }
                view.clearAnimation();
                this.mFacelockAnimationSet.setAnimationListener(null);
                if (this.mFacelockRetryAnimationSet.isStarted()) {
                    this.mFacelockRetryAnimationSet.cancel();
                }
                if (i == 12) {
                    if (getScreenOn()) {
                        if (Build.DEBUG_ONEPLUS) {
                            Log.d("LockIcon", "play retry anim");
                        }
                        this.mFacelockRetryAnimationSet.start();
                        return;
                    }
                    if (Build.DEBUG_ONEPLUS) {
                        Log.d("LockIcon", "screen is off, padding show try icon");
                    }
                    this.mHandler.removeCallbacks(this.mPaddingRetryRunnable);
                    this.mHandler.postDelayed(this.mPaddingRetryRunnable, 150);
                } else if (KeyguardUpdateMonitor.getInstance(this.mContext).shouldPlayFacelockFailAnim()) {
                    if (Build.DEBUG_ONEPLUS) {
                        Log.d("LockIcon", "play fail anim");
                    }
                    view.startAnimation(this.mFacelockFailAnimationSet);
                }
            } else {
                this.mFacelockAnimationSet.setAnimationListener(new Animation.AnimationListener() { // from class: com.android.systemui.statusbar.phone.OpLockscreenLockIconController.6
                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationEnd(Animation animation) {
                        if (KeyguardUpdateMonitor.getInstance(OpLockscreenLockIconController.this.mContext).isFacelockRecognizing() && OpLockscreenLockIconController.this.mFacelockAnimationSet != null) {
                            if (Build.DEBUG_ONEPLUS) {
                                Log.d("LockIcon", "start recog anim again");
                            }
                            OpLockscreenLockIconController.this.mFacelockAnimationSet.setAnimationListener(this);
                            view.startAnimation(OpLockscreenLockIconController.this.mFacelockAnimationSet);
                        }
                    }
                });
                if (Build.DEBUG_ONEPLUS) {
                    Log.d("LockIcon", "start anim");
                }
                view.startAnimation(this.mFacelockAnimationSet);
            }
        }
    }

    /* access modifiers changed from: protected */
    public int opGetState() {
        KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(this.mContext);
        boolean isFingerprintDetectionRunning = instance.isFingerprintDetectionRunning();
        boolean isUnlockingWithBiometricAllowed = instance.isUnlockingWithBiometricAllowed();
        if (KeyguardUpdateMonitor.getInstance(this.mContext).isFacelockAvailable()) {
            return 12;
        }
        if (KeyguardUpdateMonitor.getInstance(this.mContext).isFacelockRecognizing()) {
            return 11;
        }
        if (KeyguardUpdateMonitor.getInstance(this.mContext).isCameraErrorState()) {
            return 13;
        }
        if (!getKeyguardStateController().isMethodSecure()) {
            return 14;
        }
        if (getTransientBiometricsError() && !OpUtils.isCustomFingerprint()) {
            return 3;
        }
        if (!this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser()) || getKeyguardUpdateMonitor().getUserCanSkipBouncer(KeyguardUpdateMonitor.getCurrentUser())) {
            return 1;
        }
        return (!isFingerprintDetectionRunning || !isUnlockingWithBiometricAllowed || OpUtils.isCustomFingerprint()) ? 0 : 15;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$OpLockscreenLockIconController() {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("LockIcon", "mPaddingRetryRunnable run");
        }
        setRetryIcon();
    }

    /* access modifiers changed from: protected */
    public void opUpdateClickability() {
        if (getAccessibilityController() != null) {
            boolean isAccessibilityEnabled = getAccessibilityController().isAccessibilityEnabled();
            boolean z = true;
            boolean z2 = getKeyguardUpdateMonitor().getUserTrustIsManaged(KeyguardUpdateMonitor.getCurrentUser()) && !isAccessibilityEnabled;
            boolean z3 = !this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser()) || getKeyguardUpdateMonitor().getUserCanSkipBouncer(KeyguardUpdateMonitor.getCurrentUser());
            boolean z4 = getKeyguardUpdateMonitor().getUserTrustIsManaged(KeyguardUpdateMonitor.getCurrentUser()) && !isAccessibilityEnabled && z3;
            boolean isFacelockAvailable = KeyguardUpdateMonitor.getInstance(this.mContext).isFacelockAvailable();
            boolean z5 = OpUtils.isCustomFingerprint() && KeyguardUpdateMonitor.getInstance(this.mContext).isCameraErrorState();
            if (Build.DEBUG_ONEPLUS) {
                Log.d("LockIcon", "opUpdateClickability: clickToUnlock= " + isAccessibilityEnabled + ", canSkipBouncer= " + z3 + ", clickToForceLock= " + z2 + ", longClickToForceLock= " + z4 + ", isFacelockAvailable= " + isFacelockAvailable + ", isCameraErrorState= " + z5);
            }
            boolean z6 = isFacelockAvailable || z5;
            getLockIcon().setClickable(z2 || isAccessibilityEnabled || z6);
            LockIcon lockIcon = getLockIcon();
            if (!z4 && !z6) {
                z = false;
            }
            lockIcon.setLongClickable(z);
            getLockIcon().setFocusable(getAccessibilityController().isAccessibilityEnabled());
        }
    }

    public void opSetDeviceInteractive(boolean z) {
        this.mDeviceInteractive = z;
        opUpdate();
    }

    /* access modifiers changed from: protected */
    public Drawable opGetIconForState(int i, boolean z, boolean z2) {
        int i2;
        if (i == 0) {
            i2 = C0006R$drawable.ic_lock_24dp;
        } else if (i != 1) {
            if (i != 3) {
                switch (i) {
                    case 11:
                        return this.mContext.getDrawable(C0006R$drawable.facelock_lock_icon_fod);
                    case 12:
                        return this.mContext.getDrawable(C0006R$drawable.facelock_lock_icon_fod);
                    case 13:
                        return this.mContext.getDrawable(C0006R$drawable.facelock_alert_fod);
                    case 14:
                        i2 = C0006R$drawable.ic_lock_empty;
                        break;
                    case 15:
                        if (z && z2) {
                            i2 = C0006R$drawable.ic_fingerprint;
                            break;
                        } else {
                            i2 = C0006R$drawable.lockscreen_fingerprint_draw_on_animation;
                            break;
                        }
                    default:
                        throw new IllegalArgumentException();
                }
            } else {
                i2 = C0006R$drawable.ic_fingerprint_error;
            }
        } else if (KeyguardUpdateMonitor.getInstance(this.mContext).canSkipBouncerByFacelock() || !KeyguardUpdateMonitor.getInstance(this.mContext).isFacelockUnlocking()) {
            i2 = C0006R$drawable.ic_lock_open_fod;
        } else {
            i2 = C0006R$drawable.facelock_lock_icon_fod;
        }
        return this.mContext.getDrawable(i2);
    }

    /* access modifiers changed from: protected */
    public int opGetAnimationResForTransition(int i, int i2, boolean z, boolean z2, boolean z3, boolean z4) {
        if (KeyguardUpdateMonitor.getInstance(this.mContext).shouldShowFacelockIcon()) {
            return -1;
        }
        if (i == 15 && i2 == 3) {
            return C0006R$drawable.lockscreen_fingerprint_fp_to_error_state_animation;
        }
        if (i == 1 && i2 == 3) {
            return C0006R$drawable.op_trusted_state_to_error_animation;
        }
        if (i == 3 && i2 == 1) {
            return C0006R$drawable.op_error_to_trustedstate_animation;
        }
        if (i == 3 && i2 == 15) {
            return C0006R$drawable.lockscreen_fingerprint_error_state_to_fp_animation;
        }
        if (i == 15 && i2 == 1 && !getKeyguardStateController().isTrusted()) {
            return C0006R$drawable.lockscreen_fingerprint_draw_off_animation;
        }
        if (i2 != 15 || ((z3 || !z4 || !z2) && (!z4 || z || !z2))) {
            return -1;
        }
        return C0006R$drawable.lockscreen_fingerprint_draw_on_animation;
    }

    private AccessibilityController getAccessibilityController() {
        return (AccessibilityController) OpReflectionUtils.getValue(LockscreenLockIconController.class, this, "mAccessibilityController");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private LockIcon getLockIcon() {
        return (LockIcon) OpReflectionUtils.getValue(LockscreenLockIconController.class, this, "mLockIcon");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getLastState() {
        return ((Integer) OpReflectionUtils.getValue(LockscreenLockIconController.class, this, "mLastState")).intValue();
    }

    private boolean getScreenOn() {
        return ((Boolean) OpReflectionUtils.getValue(LockscreenLockIconController.class, this, "mScreenOn")).booleanValue();
    }

    private void opUpdate() {
        OpReflectionUtils.methodInvokeVoid(LockscreenLockIconController.class, this, "update", new Object[0]);
    }

    private KeyguardStateController getKeyguardStateController() {
        return (KeyguardStateController) OpReflectionUtils.getValue(LockscreenLockIconController.class, this, "mKeyguardStateController");
    }

    private boolean getTransientBiometricsError() {
        return ((Boolean) OpReflectionUtils.getValue(LockscreenLockIconController.class, this, "mTransientBiometricsError")).booleanValue();
    }

    private KeyguardUpdateMonitor getKeyguardUpdateMonitor() {
        return (KeyguardUpdateMonitor) OpReflectionUtils.getValue(LockscreenLockIconController.class, this, "mKeyguardUpdateMonitor");
    }

    /* access modifiers changed from: protected */
    public static class IntrinsicSizeDrawable extends InsetDrawable {
        private final int mIntrinsicHeight;
        private final int mIntrinsicWidth;

        public IntrinsicSizeDrawable(Drawable drawable, int i, int i2) {
            super(drawable, 0);
            this.mIntrinsicWidth = i;
            this.mIntrinsicHeight = i2;
        }

        @Override // android.graphics.drawable.InsetDrawable, android.graphics.drawable.Drawable, android.graphics.drawable.DrawableWrapper
        public int getIntrinsicWidth() {
            return this.mIntrinsicWidth;
        }

        @Override // android.graphics.drawable.InsetDrawable, android.graphics.drawable.Drawable, android.graphics.drawable.DrawableWrapper
        public int getIntrinsicHeight() {
            return this.mIntrinsicHeight;
        }
    }
}
