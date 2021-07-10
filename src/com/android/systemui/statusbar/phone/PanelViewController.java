package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Debug;
import android.os.SystemClock;
import android.util.BoostFramework;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;
import com.android.internal.util.LatencyTracker;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0003R$bool;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.VibratorHelper;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.phone.LockscreenGestureLogger;
import com.android.systemui.statusbar.phone.PanelView;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.oneplus.battery.OpChargingAnimationController;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.statusbar.phone.OpHighlightHintController;
import com.oneplus.systemui.util.OpMdmLogger;
import com.oneplus.util.OpUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
public abstract class PanelViewController {
    public static final String TAG = PanelView.class.getSimpleName();
    private boolean mAnimateAfterExpanding;
    private boolean mAnimatingOnDown;
    protected PanelBar mBar;
    private Interpolator mBounceInterpolator;
    private boolean mClosing;
    private boolean mCollapsedAndHeadsUpOnDown;
    protected long mDownTime;
    private final DozeLog mDozeLog;
    private NotificationEntryManager mEntryManager;
    private boolean mExpandLatencyTracking;
    private float mExpandedFraction = 0.0f;
    protected float mExpandedHeight = 0.0f;
    protected boolean mExpanding;
    protected ArrayList<PanelExpansionListener> mExpansionListeners = new ArrayList<>();
    private final FalsingManager mFalsingManager;
    private int mFixedDuration = -1;
    private FlingAnimationUtils mFlingAnimationUtils;
    private FlingAnimationUtils mFlingAnimationUtilsClosing;
    private FlingAnimationUtils mFlingAnimationUtilsDismissing;
    private final Runnable mFlingCollapseRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.PanelViewController.4
        @Override // java.lang.Runnable
        public void run() {
            PanelViewController panelViewController = PanelViewController.this;
            panelViewController.fling(0.0f, false, panelViewController.mNextCollapseSpeedUpFactor, false);
        }
    };
    protected GestureDetector mGestureDetector;
    private boolean mGestureWaitForTouchSlop;
    private boolean mHasLayoutedSinceDown;
    protected HeadsUpManagerPhone mHeadsUpManager;
    private ValueAnimator mHeightAnimator;
    protected boolean mHintAnimationRunning;
    private float mHintDistance;
    private boolean mIgnoreXTouchSlop;
    private float mInitialOffsetOnTouch;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private boolean mInstantExpanding;
    private boolean mJustPeeked;
    protected KeyguardBottomAreaView mKeyguardBottomArea;
    protected final KeyguardStateController mKeyguardStateController;
    private boolean mLastDone = false;
    private float mLastPrintFraction = 0.0f;
    private boolean mLastSwitching = false;
    private final LatencyTracker mLatencyTracker;
    protected boolean mLaunchingNotification;
    private LockscreenGestureLogger mLockscreenGestureLogger = new LockscreenGestureLogger();
    private float mMinExpandHeight;
    private boolean mMotionAborted;
    private float mNextCollapseSpeedUpFactor = 1.0f;
    private boolean mNotificationsDragEnabled;
    protected int mOrientation;
    private boolean mOverExpandedBeforeFling;
    private boolean mPanelClosedOnDown;
    private boolean mPanelUpdateWhenAnimatorEnds;
    private ObjectAnimator mPeekAnimator;
    private float mPeekHeight;
    private boolean mPeekTouching;
    protected BoostFramework mPerf = null;
    protected final Runnable mPostCollapseRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.PanelViewController.8
        @Override // java.lang.Runnable
        public void run() {
            PanelViewController.this.collapse(false, 1.0f);
        }
    };
    protected final Resources mResources;
    private float mSlopMultiplier;
    protected StatusBar mStatusBar;
    protected final SysuiStatusBarStateController mStatusBarStateController;
    protected final StatusBarTouchableRegionManager mStatusBarTouchableRegionManager;
    private boolean mTouchAboveFalsingThreshold;
    protected float mTouchActionDownX;
    protected float mTouchActionDownY;
    private boolean mTouchDisabled;
    private int mTouchSlop;
    private boolean mTouchSlopExceeded;
    protected boolean mTouchSlopExceededBeforeDown;
    private boolean mTouchStartedInEmptyArea;
    protected boolean mTracking;
    private int mTrackingPointer;
    private int mUnlockFalsingThreshold;
    private boolean mUpdateFlingOnLayout;
    private float mUpdateFlingVelocity;
    private boolean mUpwardsWhenThresholdReached;
    private final VelocityTracker mVelocityTracker = VelocityTracker.obtain();
    private boolean mVibrateOnOpening;
    private final VibratorHelper mVibratorHelper;
    private final PanelView mView;
    private String mViewName;

    /* access modifiers changed from: protected */
    public boolean canCollapsePanelOnTouch() {
        return true;
    }

    /* access modifiers changed from: protected */
    public abstract boolean fullyExpandedClearAllVisible();

    /* access modifiers changed from: protected */
    public abstract int getClearAllHeightWithPadding();

    /* access modifiers changed from: protected */
    public abstract int getMaxPanelHeight();

    /* access modifiers changed from: protected */
    public abstract float getOpeningHeight();

    /* access modifiers changed from: protected */
    public abstract float getOverExpansionAmount();

    /* access modifiers changed from: protected */
    public abstract float getOverExpansionPixels();

    /* access modifiers changed from: protected */
    public abstract void initExpandButton();

    /* access modifiers changed from: protected */
    public abstract boolean isClearAllVisible();

    /* access modifiers changed from: protected */
    public abstract boolean isInContentBounds(float f, float f2);

    /* access modifiers changed from: protected */
    public abstract boolean isPanelVisibleBecauseOfHeadsUp();

    /* access modifiers changed from: protected */
    public abstract boolean isTrackingBlocked();

    /* access modifiers changed from: protected */
    public abstract boolean isWithinGameModeToolBoxRegion();

    /* access modifiers changed from: protected */
    public void onExpandingStarted() {
    }

    /* access modifiers changed from: protected */
    public abstract void onHeightUpdated(float f);

    /* access modifiers changed from: protected */
    public abstract boolean onMiddleClicked();

    /* access modifiers changed from: protected */
    public abstract void opFlingToHeightAnimatorForBiometricUnlock();

    public abstract void resetViews(boolean z);

    /* access modifiers changed from: protected */
    public abstract void setOverExpansion(float f, boolean z);

    /* access modifiers changed from: protected */
    public abstract boolean shouldGestureIgnoreXTouchSlop(float f, float f2);

    /* access modifiers changed from: protected */
    public abstract boolean shouldGestureWaitForTouchSlop();

    /* access modifiers changed from: protected */
    public abstract boolean shouldUseDismissingAnimation();

    /* access modifiers changed from: protected */
    public abstract void showExpandButton();

    /* access modifiers changed from: protected */
    public void onExpandingFinished() {
        this.mBar.onExpandingFinished();
    }

    /* access modifiers changed from: protected */
    public void notifyExpandingStarted() {
        if (!this.mExpanding) {
            this.mExpanding = true;
            onExpandingStarted();
        }
    }

    public final void notifyExpandingFinished() {
        endClosing();
        if (this.mExpanding) {
            this.mExpanding = false;
            onExpandingFinished();
        }
    }

    private void runPeekAnimation(long j, float f, final boolean z) {
        if (!showEmptyShadeView()) {
            this.mPeekHeight = f;
            if (this.mHeightAnimator == null) {
                ObjectAnimator objectAnimator = this.mPeekAnimator;
                if (objectAnimator != null) {
                    objectAnimator.cancel();
                }
                ObjectAnimator duration = ObjectAnimator.ofFloat(this, "expandedHeight", this.mPeekHeight).setDuration(j);
                this.mPeekAnimator = duration;
                duration.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
                this.mPeekAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.phone.PanelViewController.1
                    private boolean mCancelled;

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationCancel(Animator animator) {
                        this.mCancelled = true;
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        PanelViewController.this.mPeekAnimator = null;
                        if (!this.mCancelled && z) {
                            PanelViewController.this.mView.postOnAnimation(PanelViewController.this.mPostCollapseRunnable);
                        }
                    }
                });
                notifyExpandingStarted();
                this.mPeekAnimator.start();
                this.mJustPeeked = true;
            }
        }
    }

    public PanelViewController(PanelView panelView, FalsingManager falsingManager, DozeLog dozeLog, KeyguardStateController keyguardStateController, SysuiStatusBarStateController sysuiStatusBarStateController, VibratorHelper vibratorHelper, LatencyTracker latencyTracker, FlingAnimationUtils.Builder builder, StatusBarTouchableRegionManager statusBarTouchableRegionManager) {
        this.mView = panelView;
        panelView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() { // from class: com.android.systemui.statusbar.phone.PanelViewController.2
            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewDetachedFromWindow(View view) {
            }

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewAttachedToWindow(View view) {
                PanelViewController panelViewController = PanelViewController.this;
                panelViewController.mViewName = panelViewController.mResources.getResourceName(panelViewController.mView.getId());
            }
        });
        this.mView.addOnLayoutChangeListener(createLayoutChangeListener());
        this.mView.setOnTouchListener(createTouchHandler());
        this.mView.setOnConfigurationChangedListener(createOnConfigurationChangedListener());
        this.mResources = this.mView.getResources();
        this.mKeyguardStateController = keyguardStateController;
        this.mStatusBarStateController = sysuiStatusBarStateController;
        builder.reset();
        builder.setMaxLengthSeconds(0.6f);
        builder.setSpeedUpFactor(0.6f);
        this.mFlingAnimationUtils = builder.build();
        builder.reset();
        builder.setMaxLengthSeconds(0.5f);
        builder.setSpeedUpFactor(0.6f);
        this.mFlingAnimationUtilsClosing = builder.build();
        builder.reset();
        builder.setMaxLengthSeconds(0.5f);
        builder.setSpeedUpFactor(0.6f);
        builder.setX2(0.6f);
        builder.setY2(0.84f);
        this.mFlingAnimationUtilsDismissing = builder.build();
        this.mLatencyTracker = latencyTracker;
        this.mBounceInterpolator = new BounceInterpolator();
        this.mFalsingManager = falsingManager;
        this.mDozeLog = dozeLog;
        this.mNotificationsDragEnabled = this.mResources.getBoolean(C0003R$bool.config_enableNotificationShadeDrag);
        this.mVibratorHelper = vibratorHelper;
        this.mVibrateOnOpening = this.mResources.getBoolean(C0003R$bool.config_vibrateOnIconAnimation);
        this.mStatusBarTouchableRegionManager = statusBarTouchableRegionManager;
        this.mPerf = new BoostFramework();
        this.mOrientation = this.mView.getContext().getResources().getConfiguration().orientation;
        this.mEntryManager = (NotificationEntryManager) Dependency.get(NotificationEntryManager.class);
    }

    /* access modifiers changed from: protected */
    public void loadDimens() {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(this.mView.getContext());
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        this.mSlopMultiplier = viewConfiguration.getScaledAmbiguousGestureMultiplier();
        this.mHintDistance = this.mResources.getDimension(C0005R$dimen.hint_move_distance);
        this.mUnlockFalsingThreshold = this.mResources.getDimensionPixelSize(C0005R$dimen.unlock_falsing_threshold);
    }

    /* access modifiers changed from: protected */
    public float getTouchSlop(MotionEvent motionEvent) {
        if (motionEvent.getClassification() == 1) {
            return ((float) this.mTouchSlop) * this.mSlopMultiplier;
        }
        return (float) this.mTouchSlop;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addMovement(MotionEvent motionEvent) {
        float rawX = motionEvent.getRawX() - motionEvent.getX();
        float rawY = motionEvent.getRawY() - motionEvent.getY();
        motionEvent.offsetLocation(rawX, rawY);
        this.mVelocityTracker.addMovement(motionEvent);
        motionEvent.offsetLocation(-rawX, -rawY);
    }

    public void setTouchAndAnimationDisabled(boolean z) {
        this.mTouchDisabled = z;
        if (z) {
            cancelHeightAnimator();
            if (this.mTracking) {
                onTrackingStopped(true);
            }
            notifyExpandingFinished();
        }
    }

    public void startExpandLatencyTracking() {
        if (this.mLatencyTracker.isEnabled()) {
            this.mLatencyTracker.onActionStart(0);
            this.mExpandLatencyTracking = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startOpening(MotionEvent motionEvent) {
        runPeekAnimation(200, getOpeningHeight(), false);
        notifyBarPanelExpansionChanged();
        maybeVibrateOnOpening();
        float displayWidth = this.mStatusBar.getDisplayWidth();
        float displayHeight = this.mStatusBar.getDisplayHeight();
        this.mLockscreenGestureLogger.writeAtFractionalPosition(1328, (int) ((motionEvent.getX() / displayWidth) * 100.0f), (int) ((motionEvent.getY() / displayHeight) * 100.0f), this.mStatusBar.getRotation());
        this.mLockscreenGestureLogger.log(LockscreenGestureLogger.LockscreenUiEvent.LOCKSCREEN_UNLOCKED_NOTIFICATION_PANEL_EXPAND);
    }

    /* access modifiers changed from: protected */
    public void maybeVibrateOnOpening() {
        if (this.mVibrateOnOpening) {
            this.mVibratorHelper.vibrate(2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDirectionUpwards(float f, float f2) {
        float f3 = f - this.mInitialTouchX;
        float f4 = f2 - this.mInitialTouchY;
        if (f4 < 0.0f && Math.abs(f4) >= Math.abs(f3)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void startExpandingFromPeek() {
        this.mStatusBar.handlePeekToExpandTransistion();
    }

    /* access modifiers changed from: protected */
    public void startExpandMotion(float f, float f2, boolean z, float f3) {
        this.mInitialOffsetOnTouch = f3;
        this.mInitialTouchY = f2;
        this.mInitialTouchX = f;
        if (z) {
            this.mTouchSlopExceeded = true;
            setExpandedHeight(f3);
            onTrackingStarted();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void endMotionEvent(MotionEvent motionEvent, float f, float f2, boolean z) {
        this.mTrackingPointer = -1;
        boolean z2 = true;
        if ((this.mTracking && this.mTouchSlopExceeded) || Math.abs(f - this.mInitialTouchX) > ((float) this.mTouchSlop) || Math.abs(f2 - this.mInitialTouchY) > ((float) this.mTouchSlop) || motionEvent.getActionMasked() == 3 || z) {
            this.mVelocityTracker.computeCurrentVelocity(1000);
            float yVelocity = this.mVelocityTracker.getYVelocity();
            float hypot = (float) Math.hypot((double) this.mVelocityTracker.getXVelocity(), (double) this.mVelocityTracker.getYVelocity());
            boolean z3 = this.mStatusBarStateController.getState() == 1;
            boolean flingExpands = (motionEvent.getActionMasked() == 3 || z) ? z3 ? true : !this.mPanelClosedOnDown : flingExpands(yVelocity, hypot, f, f2);
            if (Build.DEBUG_ONEPLUS) {
                Log.d(TAG, "endMotionEvent: action= " + motionEvent.getActionMasked() + ", forceCancel= " + z + ", expand= " + flingExpands);
            }
            this.mDozeLog.traceFling(flingExpands, this.mTouchAboveFalsingThreshold, this.mStatusBar.isFalsingThresholdNeeded(), this.mStatusBar.isWakeUpComingFromTouch());
            if (!flingExpands && z3) {
                float displayDensity = this.mStatusBar.getDisplayDensity();
                this.mLockscreenGestureLogger.write(186, (int) Math.abs((f2 - this.mInitialTouchY) / displayDensity), (int) Math.abs(yVelocity / displayDensity));
                this.mLockscreenGestureLogger.log(LockscreenGestureLogger.LockscreenUiEvent.LOCKSCREEN_UNLOCK);
                if (!OpLsState.getInstance().getStatusBarKeyguardViewManager().isSecure()) {
                    OpMdmLogger.log("lock_unlock_success", "swipe", "1");
                }
            }
            fling(yVelocity, flingExpands, isFalseTouch(f, f2));
            onTrackingStopped(flingExpands);
            if (!flingExpands || !this.mPanelClosedOnDown || this.mHasLayoutedSinceDown) {
                z2 = false;
            }
            this.mUpdateFlingOnLayout = z2;
            if (z2) {
                this.mUpdateFlingVelocity = yVelocity;
            }
        } else if (!this.mPanelClosedOnDown || this.mHeadsUpManager.hasPinnedHeadsUp() || this.mTracking || this.mStatusBar.isBouncerShowing() || this.mKeyguardStateController.isKeyguardFadingAway() || ((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).isHighLightHintShow() || ((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).isCarModeHighlightHintSHow()) {
            if (!this.mStatusBar.isBouncerShowing()) {
                onTrackingStopped(onEmptySpaceClick(this.mInitialTouchX));
            }
        } else if (SystemClock.uptimeMillis() - this.mDownTime < ((long) ViewConfiguration.getLongPressTimeout())) {
            runPeekAnimation(360, getOpeningHeight(), true);
        } else {
            this.mView.postOnAnimation(this.mPostCollapseRunnable);
        }
        this.mVelocityTracker.clear();
        this.mPeekTouching = false;
    }

    /* access modifiers changed from: protected */
    public float getCurrentExpandVelocity() {
        this.mVelocityTracker.computeCurrentVelocity(1000);
        return this.mVelocityTracker.getYVelocity();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getFalsingThreshold() {
        return (int) (((float) this.mUnlockFalsingThreshold) * (this.mStatusBar.isWakeUpComingFromTouch() ? 1.5f : 1.0f));
    }

    /* access modifiers changed from: protected */
    public void onTrackingStopped(boolean z) {
        this.mTracking = false;
        if (Build.DEBUG_ONEPLUS) {
            Log.d("PanelView.UnlockTrack", "onTrackingStopped: " + Debug.getCallers(5));
        }
        this.mBar.onTrackingStopped(z);
        notifyBarPanelExpansionChanged();
    }

    /* access modifiers changed from: protected */
    public void onTrackingStarted() {
        endClosing();
        this.mTracking = true;
        if (Build.DEBUG_ONEPLUS) {
            Log.d("PanelView.UnlockTrack", "onTrackingStarted: " + Debug.getCallers(5));
        }
        this.mBar.onTrackingStarted();
        notifyExpandingStarted();
        notifyBarPanelExpansionChanged();
    }

    /* access modifiers changed from: protected */
    public void cancelHeightAnimator() {
        ValueAnimator valueAnimator = this.mHeightAnimator;
        if (valueAnimator != null) {
            if (valueAnimator.isRunning()) {
                this.mPanelUpdateWhenAnimatorEnds = false;
            }
            this.mHeightAnimator.cancel();
        }
        endClosing();
    }

    private void endClosing() {
        if (this.mClosing) {
            this.mClosing = false;
            if (Build.DEBUG_ONEPLUS) {
                Log.d("PanelView.UnlockTrack", "endClosing: " + Debug.getCallers(5));
            }
            onClosingFinished();
        }
    }

    /* access modifiers changed from: protected */
    public boolean flingExpands(float f, float f2, float f3, float f4) {
        if (this.mFalsingManager.isUnlockingDisabled() || isFalseTouch(f3, f4)) {
            return true;
        }
        if (Math.abs(f2) < this.mFlingAnimationUtils.getMinVelocityPxPerSecond()) {
            return shouldExpandWhenNotFlinging();
        }
        if (f > 0.0f) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean shouldExpandWhenNotFlinging() {
        return getExpandedFraction() > 0.5f;
    }

    private boolean isFalseTouch(float f, float f2) {
        if (this.mUpwardsWhenThresholdReached) {
            return false;
        }
        boolean z = !isDirectionUpwards(f, f2);
        if (z) {
            Log.i(TAG, "isFalseTouch isDirectionUpwards return false, touch:" + f + "," + f2 + " to " + this.mInitialTouchX + "," + this.mInitialTouchY);
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void fling(float f, boolean z) {
        fling(f, z, 1.0f, false);
    }

    /* access modifiers changed from: protected */
    public void fling(float f, boolean z, boolean z2) {
        fling(f, z, 1.0f, z2);
    }

    /* access modifiers changed from: protected */
    public void fling(float f, boolean z, float f2, boolean z2) {
        cancelPeek();
        float maxPanelHeight = z ? (float) getMaxPanelHeight() : 0.0f;
        if (!z) {
            this.mClosing = true;
            if (Build.DEBUG_ONEPLUS) {
                Log.d("PanelView.UnlockTrack", "fling: " + Debug.getCallers(5));
            }
        }
        flingToHeight(f, z, maxPanelHeight, f2, z2);
    }

    /* access modifiers changed from: protected */
    public void flingToHeight(float f, boolean z, float f2, float f3, boolean z2) {
        ValueAnimator valueAnimator;
        int i;
        String str = TAG;
        boolean z3 = true;
        final boolean z4 = z && shouldExpandToTopOfClearAll((float) (getMaxPanelHeight() - getClearAllHeightWithPadding()));
        float maxPanelHeight = z4 ? (float) (getMaxPanelHeight() - getClearAllHeightWithPadding()) : f2;
        if (maxPanelHeight == this.mExpandedHeight || (getOverExpansionAmount() > 0.0f && z)) {
            notifyExpandingFinished();
            return;
        }
        if (Build.DEBUG_ONEPLUS) {
            Log.i(str, " flingToHeight target:" + maxPanelHeight + " getMaxPanelHeight:" + getMaxPanelHeight() + " getClearAllHeight:" + getClearAllHeightWithPadding());
        }
        if (getOverExpansionAmount() <= 0.0f) {
            z3 = false;
        }
        this.mOverExpandedBeforeFling = z3;
        ValueAnimator createHeightAnimator = createHeightAnimator(maxPanelHeight);
        if (z) {
            float f4 = (!z2 || f >= 0.0f) ? f : 0.0f;
            this.mFlingAnimationUtils.apply(createHeightAnimator, this.mExpandedHeight, maxPanelHeight, f4, (float) this.mView.getHeight());
            if (f4 == 0.0f) {
                createHeightAnimator.setDuration(350L);
            }
            i = -1;
            valueAnimator = createHeightAnimator;
        } else {
            if (!shouldUseDismissingAnimation()) {
                i = -1;
                valueAnimator = createHeightAnimator;
                this.mFlingAnimationUtilsClosing.apply(valueAnimator, this.mExpandedHeight, maxPanelHeight, f, (float) this.mView.getHeight());
            } else if (f == 0.0f) {
                createHeightAnimator.setInterpolator(Interpolators.PANEL_CLOSE_ACCELERATED);
                createHeightAnimator.setDuration((long) (((this.mExpandedHeight / ((float) this.mView.getHeight())) * 100.0f) + 200.0f));
                i = -1;
                valueAnimator = createHeightAnimator;
            } else {
                i = -1;
                valueAnimator = createHeightAnimator;
                this.mFlingAnimationUtilsDismissing.apply(createHeightAnimator, this.mExpandedHeight, maxPanelHeight, f, (float) this.mView.getHeight());
            }
            if (f == 0.0f) {
                valueAnimator.setDuration((long) (((float) valueAnimator.getDuration()) / f3));
            }
            int i2 = this.mFixedDuration;
            if (i2 != i) {
                valueAnimator.setDuration((long) i2);
            }
        }
        if (this.mPerf != null) {
            this.mPerf.perfHint(4224, this.mView.getContext().getPackageName(), i, 3);
        }
        boolean isBiometricUnlock = OpLsState.getInstance().getBiometricUnlockController().isBiometricUnlock();
        boolean isKeyguardVisible = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isKeyguardVisible();
        Log.i(str, " isBiometricUnlock:" + isBiometricUnlock + ", " + isKeyguardVisible + ", " + this.mClosing);
        if (!isBiometricUnlock || !isKeyguardVisible) {
            valueAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.phone.PanelViewController.3
                private boolean mCancelled;

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animator) {
                    BoostFramework boostFramework = PanelViewController.this.mPerf;
                    if (boostFramework != null) {
                        boostFramework.perfLockRelease();
                    }
                    this.mCancelled = true;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    BoostFramework boostFramework = PanelViewController.this.mPerf;
                    if (boostFramework != null) {
                        boostFramework.perfLockRelease();
                    }
                    if (z4 && !this.mCancelled) {
                        PanelViewController panelViewController = PanelViewController.this;
                        panelViewController.setExpandedHeightInternal((float) panelViewController.getMaxPanelHeight());
                    }
                    PanelViewController.this.setAnimator(null);
                    if (!this.mCancelled) {
                        PanelViewController.this.notifyExpandingFinished();
                    }
                    PanelViewController.this.notifyBarPanelExpansionChanged();
                }
            });
            setAnimator(valueAnimator);
            valueAnimator.start();
            return;
        }
        opFlingToHeightAnimatorForBiometricUnlock();
    }

    /* access modifiers changed from: protected */
    public boolean shouldExpandToTopOfClearAll(float f) {
        return fullyExpandedClearAllVisible() && this.mExpandedHeight < f && !isClearAllVisible();
    }

    public void setExpandedHeight(float f) {
        if (Float.isNaN(getOverExpansionPixels() + f)) {
            String str = TAG;
            Log.i(str, "setExpandedHeight:" + f + "," + getOverExpansionPixels());
        }
        setExpandedHeightInternal(f + getOverExpansionPixels());
    }

    /* access modifiers changed from: protected */
    public void requestPanelHeightUpdate() {
        float maxPanelHeight = (float) getMaxPanelHeight();
        if (isFullyCollapsed() || maxPanelHeight == this.mExpandedHeight || this.mPeekAnimator != null || this.mPeekTouching) {
            return;
        }
        if (this.mTracking && !isTrackingBlocked()) {
            return;
        }
        if (this.mHeightAnimator != null) {
            this.mPanelUpdateWhenAnimatorEnds = true;
        } else {
            setExpandedHeight(maxPanelHeight);
        }
    }

    public void setExpandedHeightInternal(float f) {
        if (Float.isNaN(f)) {
            String str = TAG;
            Log.wtf(str, "ExpandedHeight set to NaN" + Debug.getCallers(10));
        }
        float f2 = 0.0f;
        if (this.mExpandLatencyTracking && f != 0.0f) {
            DejankUtils.postAfterTraversal(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PanelViewController$3-TJ0A2OT3Q4yelawe6rfaI8nnw
                @Override // java.lang.Runnable
                public final void run() {
                    PanelViewController.this.lambda$setExpandedHeightInternal$0$PanelViewController();
                }
            });
            this.mExpandLatencyTracking = false;
        }
        float maxPanelHeight = ((float) getMaxPanelHeight()) - getOverExpansionAmount();
        if (this.mHeightAnimator == null) {
            float max = Math.max(0.0f, f - maxPanelHeight);
            if (getOverExpansionPixels() != max && this.mTracking) {
                setOverExpansion(max, true);
            }
            this.mExpandedHeight = Math.min(f, maxPanelHeight) + getOverExpansionAmount();
        } else {
            this.mExpandedHeight = f;
            if (this.mOverExpandedBeforeFling) {
                setOverExpansion(Math.max(0.0f, f - maxPanelHeight), false);
            }
        }
        float f3 = this.mExpandedHeight;
        if (f3 < 1.0f && f3 != 0.0f && this.mClosing) {
            this.mExpandedHeight = 0.0f;
            ValueAnimator valueAnimator = this.mHeightAnimator;
            if (valueAnimator != null) {
                valueAnimator.end();
            }
        }
        if (maxPanelHeight != 0.0f) {
            f2 = this.mExpandedHeight / maxPanelHeight;
        }
        this.mExpandedFraction = Math.min(1.0f, f2);
        onHeightUpdated(this.mExpandedHeight);
        notifyBarPanelExpansionChanged();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setExpandedHeightInternal$0 */
    public /* synthetic */ void lambda$setExpandedHeightInternal$0$PanelViewController() {
        this.mLatencyTracker.onActionEnd(0);
    }

    public void setExpandedFraction(float f) {
        setExpandedHeight(((float) getMaxPanelHeight()) * f);
    }

    public float getExpandedHeight() {
        return this.mExpandedHeight;
    }

    public float getExpandedFraction() {
        return this.mExpandedFraction;
    }

    public boolean isFullyExpanded() {
        return this.mExpandedHeight >= ((float) getMaxPanelHeight());
    }

    public boolean isFullyCollapsed() {
        return this.mExpandedFraction <= 0.0f;
    }

    public boolean isCollapsing() {
        return this.mClosing || this.mLaunchingNotification;
    }

    public boolean isTracking() {
        return this.mTracking;
    }

    public void setBar(PanelBar panelBar) {
        this.mBar = panelBar;
    }

    public void collapse(boolean z, float f) {
        if (canPanelBeCollapsed()) {
            cancelHeightAnimator();
            notifyExpandingStarted();
            this.mClosing = true;
            if (Build.DEBUG_ONEPLUS) {
                Log.d("PanelView.UnlockTrack", "collapse: " + Debug.getCallers(5));
            }
            if (z) {
                this.mNextCollapseSpeedUpFactor = f;
                this.mView.postDelayed(this.mFlingCollapseRunnable, 120);
                return;
            }
            fling(0.0f, false, f, false);
        }
    }

    public boolean canPanelBeCollapsed() {
        return !isFullyCollapsed() && !this.mTracking && !this.mClosing;
    }

    public void cancelPeek() {
        boolean z;
        ObjectAnimator objectAnimator = this.mPeekAnimator;
        if (objectAnimator != null) {
            z = true;
            objectAnimator.cancel();
        } else {
            z = false;
        }
        if (z) {
            notifyBarPanelExpansionChanged();
        }
    }

    public void expand(boolean z) {
        if (isFullyCollapsed() || isCollapsing()) {
            this.mInstantExpanding = true;
            this.mAnimateAfterExpanding = z;
            this.mUpdateFlingOnLayout = false;
            abortAnimations();
            cancelPeek();
            if (this.mTracking) {
                onTrackingStopped(true);
            }
            if (this.mExpanding) {
                notifyExpandingFinished();
            }
            notifyBarPanelExpansionChanged();
            if (OpUtils.DEBUG_ONEPLUS) {
                String str = TAG;
                Log.i(str, " expand:" + z + ":" + Debug.getCallers(8));
            }
            this.mView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() { // from class: com.android.systemui.statusbar.phone.PanelViewController.5
                @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
                public void onGlobalLayout() {
                    if (!PanelViewController.this.mInstantExpanding) {
                        PanelViewController.this.mView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else if (PanelViewController.this.mStatusBar.getNotificationShadeWindowView().isVisibleToUser()) {
                        PanelViewController.this.mView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        if (PanelViewController.this.mAnimateAfterExpanding) {
                            PanelViewController.this.notifyExpandingStarted();
                            PanelViewController.this.fling(0.0f, true);
                        } else if (!PanelViewController.this.mKeyguardStateController.canDismissLockScreen() || !((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getUserUnlockedWithBiometric(KeyguardUpdateMonitor.getCurrentUser())) {
                            PanelViewController.this.setExpandedFraction(1.0f);
                        } else {
                            Log.i(PanelViewController.TAG, "onGlobalLayout canDismissLockScreen");
                        }
                        PanelViewController.this.mInstantExpanding = false;
                    }
                }
            });
            this.mView.requestLayout();
        }
    }

    public void instantCollapse() {
        String str = TAG;
        Log.d(str, "instantCollapse: mExpanding = " + this.mExpanding + ", mInstantExpanding = " + this.mInstantExpanding);
        abortAnimations();
        setExpandedFraction(0.0f);
        if (this.mExpanding) {
            notifyExpandingFinished();
        }
        if (this.mInstantExpanding) {
            this.mInstantExpanding = false;
            notifyBarPanelExpansionChanged();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void abortAnimations() {
        cancelPeek();
        cancelHeightAnimator();
        this.mView.removeCallbacks(this.mPostCollapseRunnable);
        this.mView.removeCallbacks(this.mFlingCollapseRunnable);
    }

    /* access modifiers changed from: protected */
    public void onClosingFinished() {
        this.mBar.onClosingFinished();
    }

    /* access modifiers changed from: protected */
    public void startUnlockHintAnimation() {
        if (this.mHeightAnimator == null && !this.mTracking) {
            cancelPeek();
            notifyExpandingStarted();
            startUnlockHintAnimationPhase1(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PanelViewController$GuYBMkURoVUrgoMW3L5UanjAhbw
                @Override // java.lang.Runnable
                public final void run() {
                    PanelViewController.this.lambda$startUnlockHintAnimation$1$PanelViewController();
                }
            });
            onUnlockHintStarted();
            this.mHintAnimationRunning = true;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startUnlockHintAnimation$1 */
    public /* synthetic */ void lambda$startUnlockHintAnimation$1$PanelViewController() {
        notifyExpandingFinished();
        onUnlockHintFinished();
        this.mHintAnimationRunning = false;
    }

    /* access modifiers changed from: protected */
    public void onUnlockHintFinished() {
        this.mStatusBar.onHintFinished();
    }

    /* access modifiers changed from: protected */
    public void onUnlockHintStarted() {
        this.mStatusBar.onUnlockHintStarted();
    }

    public boolean isUnlockHintRunning() {
        return this.mHintAnimationRunning;
    }

    private void startUnlockHintAnimationPhase1(final Runnable runnable) {
        ValueAnimator createHeightAnimator = createHeightAnimator(Math.max(0.0f, ((float) getMaxPanelHeight()) - this.mHintDistance));
        createHeightAnimator.setDuration(250L);
        createHeightAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        createHeightAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.phone.PanelViewController.6
            private boolean mCancelled;

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.mCancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.mCancelled) {
                    PanelViewController.this.setAnimator(null);
                    runnable.run();
                    return;
                }
                PanelViewController.this.startUnlockHintAnimationPhase2(runnable);
            }
        });
        createHeightAnimator.start();
        setAnimator(createHeightAnimator);
        View[] viewArr = {this.mKeyguardBottomArea.getIndicationArea(), this.mStatusBar.getAmbientIndicationContainer()};
        for (int i = 0; i < 2; i++) {
            View view = viewArr[i];
            if (view != null) {
                view.animate().translationY(-this.mHintDistance).setDuration(250).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).withEndAction(new Runnable(view) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PanelViewController$2WGoBUvxneCReDApmWjMb2yffws
                    public final /* synthetic */ View f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        PanelViewController.this.lambda$startUnlockHintAnimationPhase1$2$PanelViewController(this.f$1);
                    }
                }).start();
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startUnlockHintAnimationPhase1$2 */
    public /* synthetic */ void lambda$startUnlockHintAnimationPhase1$2$PanelViewController(View view) {
        view.animate().translationY(0.0f).setDuration(450).setInterpolator(this.mBounceInterpolator).start();
    }

    /* access modifiers changed from: protected */
    public void setAnimator(ValueAnimator valueAnimator) {
        this.mHeightAnimator = valueAnimator;
        if (valueAnimator == null && this.mPanelUpdateWhenAnimatorEnds) {
            this.mPanelUpdateWhenAnimatorEnds = false;
            requestPanelHeightUpdate();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startUnlockHintAnimationPhase2(final Runnable runnable) {
        ValueAnimator createHeightAnimator = createHeightAnimator((float) getMaxPanelHeight());
        createHeightAnimator.setDuration(450L);
        createHeightAnimator.setInterpolator(this.mBounceInterpolator);
        createHeightAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.phone.PanelViewController.7
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                PanelViewController.this.setAnimator(null);
                runnable.run();
                PanelViewController.this.notifyBarPanelExpansionChanged();
            }
        });
        createHeightAnimator.start();
        setAnimator(createHeightAnimator);
    }

    private ValueAnimator createHeightAnimator(float f) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mExpandedHeight, f);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PanelViewController$dSx0idVyG0MoiMqYY5GMAiz4jTg
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                PanelViewController.this.lambda$createHeightAnimator$3$PanelViewController(valueAnimator);
            }
        });
        return ofFloat;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createHeightAnimator$3 */
    public /* synthetic */ void lambda$createHeightAnimator$3$PanelViewController(ValueAnimator valueAnimator) {
        setExpandedHeightInternal(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0060, code lost:
        if (r4 != 1.0f) goto L_0x00a1;
     */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00c7  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00cb  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00dd A[LOOP:0: B:42:0x00d5->B:44:0x00dd, LOOP_END] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyBarPanelExpansionChanged() {
        /*
        // Method dump skipped, instructions count: 240
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.PanelViewController.notifyBarPanelExpansionChanged():void");
    }

    public void addExpansionListener(PanelExpansionListener panelExpansionListener) {
        this.mExpansionListeners.add(panelExpansionListener);
    }

    public boolean onEmptySpaceClick(float f) {
        if (this.mHintAnimationRunning) {
            return true;
        }
        return onMiddleClicked();
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0031: APUT  (r6v2 java.lang.Object[]), (3 ??[int, float, short, byte, char]), (r8v8 java.lang.String) */
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        String str;
        Object[] objArr = new Object[11];
        objArr[0] = getClass().getSimpleName();
        objArr[1] = Float.valueOf(getExpandedHeight());
        objArr[2] = Integer.valueOf(getMaxPanelHeight());
        String str2 = "T";
        objArr[3] = this.mClosing ? str2 : "f";
        objArr[4] = this.mTracking ? str2 : "f";
        objArr[5] = this.mJustPeeked ? str2 : "f";
        ObjectAnimator objectAnimator = this.mPeekAnimator;
        objArr[6] = objectAnimator;
        String str3 = " (started)";
        if (objectAnimator == null || !objectAnimator.isStarted()) {
            str = "";
        } else {
            str = str3;
        }
        objArr[7] = str;
        ValueAnimator valueAnimator = this.mHeightAnimator;
        objArr[8] = valueAnimator;
        if (valueAnimator == null || !valueAnimator.isStarted()) {
            str3 = "";
        }
        objArr[9] = str3;
        if (!this.mTouchDisabled) {
            str2 = "f";
        }
        objArr[10] = str2;
        printWriter.println(String.format("[PanelView(%s): expandedHeight=%f maxPanelHeight=%d closing=%s tracking=%s justPeeked=%s peekAnim=%s%s timeAnim=%s%s touchDisabled=%s]", objArr));
    }

    public void setHeadsUpManager(HeadsUpManagerPhone headsUpManagerPhone) {
        this.mHeadsUpManager = headsUpManagerPhone;
    }

    public void setLaunchingNotification(boolean z) {
        this.mLaunchingNotification = z;
    }

    public void collapseWithDuration(int i) {
        this.mFixedDuration = i;
        collapse(false, 1.0f);
        this.mFixedDuration = -1;
    }

    public ViewGroup getView() {
        return this.mView;
    }

    public boolean isEnabled() {
        return this.mView.isEnabled();
    }

    public OnLayoutChangeListener createLayoutChangeListener() {
        return new OnLayoutChangeListener();
    }

    /* access modifiers changed from: protected */
    public TouchHandler createTouchHandler() {
        return new TouchHandler();
    }

    /* access modifiers changed from: protected */
    public OnConfigurationChangedListener createOnConfigurationChangedListener() {
        return new OnConfigurationChangedListener();
    }

    public class TouchHandler implements View.OnTouchListener {
        public TouchHandler() {
        }

        public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
            int pointerId;
            if (PanelViewController.this.mInstantExpanding || !PanelViewController.this.mNotificationsDragEnabled || PanelViewController.this.mTouchDisabled || (PanelViewController.this.mMotionAborted && motionEvent.getActionMasked() != 0)) {
                return false;
            }
            int i = 1;
            if (OpLsState.getInstance().getPreventModeCtrl().isPreventModeActive()) {
                OpLsState.getInstance().getPreventModeCtrl().disPatchTouchEvent(motionEvent);
                return true;
            } else if (OpLsState.getInstance().getBiometricUnlockController().getMode() == 5 || OpLsState.getInstance().getBiometricUnlockController().getMode() == 7 || OpLsState.getInstance().getBiometricUnlockController().getMode() == 2 || ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isFacelockUnlocking()) {
                return true;
            } else {
                if (((OpChargingAnimationController) Dependency.get(OpChargingAnimationController.class)).isAnimationStarted()) {
                    ((OpChargingAnimationController) Dependency.get(OpChargingAnimationController.class)).disPatchTouchEvent(motionEvent);
                    return true;
                }
                int findPointerIndex = motionEvent.findPointerIndex(PanelViewController.this.mTrackingPointer);
                if (findPointerIndex < 0) {
                    PanelViewController.this.mTrackingPointer = motionEvent.getPointerId(0);
                    findPointerIndex = 0;
                }
                float x = motionEvent.getX(findPointerIndex);
                float y = motionEvent.getY(findPointerIndex);
                boolean canCollapsePanelOnTouch = PanelViewController.this.canCollapsePanelOnTouch();
                int actionMasked = motionEvent.getActionMasked();
                if (actionMasked != 0) {
                    if (actionMasked != 1) {
                        if (actionMasked == 2) {
                            float f = y - PanelViewController.this.mInitialTouchY;
                            PanelViewController.this.addMovement(motionEvent);
                            if (canCollapsePanelOnTouch || PanelViewController.this.mTouchStartedInEmptyArea || PanelViewController.this.mAnimatingOnDown) {
                                float abs = Math.abs(f);
                                float touchSlop = PanelViewController.this.getTouchSlop(motionEvent);
                                if ((f < (-touchSlop) || (PanelViewController.this.mAnimatingOnDown && abs > touchSlop)) && abs > Math.abs(x - PanelViewController.this.mInitialTouchX)) {
                                    PanelViewController.this.cancelHeightAnimator();
                                    PanelViewController panelViewController = PanelViewController.this;
                                    panelViewController.startExpandMotion(x, y, true, panelViewController.mExpandedHeight);
                                    return true;
                                }
                            }
                        } else if (actionMasked != 3) {
                            if (actionMasked != 5) {
                                if (actionMasked == 6 && PanelViewController.this.mTrackingPointer == (pointerId = motionEvent.getPointerId(motionEvent.getActionIndex()))) {
                                    if (motionEvent.getPointerId(0) != pointerId) {
                                        i = 0;
                                    }
                                    PanelViewController.this.mTrackingPointer = motionEvent.getPointerId(i);
                                    PanelViewController.this.mInitialTouchX = motionEvent.getX(i);
                                    PanelViewController.this.mInitialTouchY = motionEvent.getY(i);
                                }
                            } else if (PanelViewController.this.mStatusBarStateController.getState() == 1) {
                                PanelViewController.this.mMotionAborted = true;
                                PanelViewController.this.mVelocityTracker.clear();
                            }
                        }
                    }
                    PanelViewController.this.mVelocityTracker.clear();
                } else {
                    PanelViewController.this.mStatusBar.userActivity();
                    PanelViewController panelViewController2 = PanelViewController.this;
                    panelViewController2.mAnimatingOnDown = panelViewController2.mHeightAnimator != null;
                    PanelViewController.this.mMinExpandHeight = 0.0f;
                    PanelViewController.this.mDownTime = SystemClock.uptimeMillis();
                    if ((!PanelViewController.this.mAnimatingOnDown || !PanelViewController.this.mClosing || PanelViewController.this.mHintAnimationRunning) && PanelViewController.this.mPeekAnimator == null) {
                        PanelViewController.this.mInitialTouchY = y;
                        PanelViewController.this.mInitialTouchX = x;
                        PanelViewController panelViewController3 = PanelViewController.this;
                        panelViewController3.mTouchStartedInEmptyArea = !panelViewController3.isInContentBounds(x, y);
                        PanelViewController panelViewController4 = PanelViewController.this;
                        panelViewController4.mTouchSlopExceeded = panelViewController4.mTouchSlopExceededBeforeDown;
                        PanelViewController.this.mJustPeeked = false;
                        PanelViewController.this.mMotionAborted = false;
                        PanelViewController panelViewController5 = PanelViewController.this;
                        panelViewController5.mPanelClosedOnDown = panelViewController5.isFullyCollapsed();
                        PanelViewController.this.mCollapsedAndHeadsUpOnDown = false;
                        PanelViewController.this.mHasLayoutedSinceDown = false;
                        PanelViewController.this.mUpdateFlingOnLayout = false;
                        PanelViewController.this.mTouchAboveFalsingThreshold = false;
                        PanelViewController.this.addMovement(motionEvent);
                    } else {
                        PanelViewController.this.cancelHeightAnimator();
                        PanelViewController.this.cancelPeek();
                        PanelViewController.this.mTouchSlopExceeded = true;
                        return true;
                    }
                }
                return false;
            }
        }

        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int pointerId;
            if (OpUtils.gameToolboxEnable(PanelViewController.this.mView.getContext())) {
                if (motionEvent.getActionMasked() == 0) {
                    PanelViewController.this.mTouchActionDownX = motionEvent.getRawX();
                    PanelViewController.this.mTouchActionDownY = motionEvent.getRawY();
                }
                if (PanelViewController.this.isWithinGameModeToolBoxRegion()) {
                    Log.d(PanelViewController.TAG, "disable panel in game toolbox region!");
                    PanelViewController.this.fling(0.0f, false, 1.0f, false);
                    return true;
                }
            }
            if (PanelViewController.this.isFullyCollapsed() && OpUtils.isDisableExpandForTouch(PanelViewController.this.mView.getContext())) {
                PanelViewController.this.initExpandButton();
                PanelViewController.this.showExpandButton();
                return true;
            } else if (PanelViewController.this.mInstantExpanding || ((PanelViewController.this.mTouchDisabled && motionEvent.getActionMasked() != 3) || (PanelViewController.this.mMotionAborted && motionEvent.getActionMasked() != 0))) {
                return false;
            } else {
                if (PanelViewController.this.mGestureDetector.onTouchEvent(motionEvent)) {
                    return true;
                }
                if (!PanelViewController.this.mNotificationsDragEnabled) {
                    PanelViewController panelViewController = PanelViewController.this;
                    if (panelViewController.mTracking) {
                        panelViewController.onTrackingStopped(true);
                    }
                    return false;
                } else if (!PanelViewController.this.isFullyCollapsed() || !motionEvent.isFromSource(8194)) {
                    StatusBar statusBar = PanelViewController.this.mStatusBar;
                    if (statusBar == null || !statusBar.isQsDisabled() || PanelViewController.this.mStatusBarStateController.getState() == 1 || !PanelViewController.this.mStatusBar.isKeyguardShowing()) {
                        int findPointerIndex = motionEvent.findPointerIndex(PanelViewController.this.mTrackingPointer);
                        if (findPointerIndex < 0) {
                            PanelViewController.this.mTrackingPointer = motionEvent.getPointerId(0);
                            findPointerIndex = 0;
                        }
                        float x = motionEvent.getX(findPointerIndex);
                        float y = motionEvent.getY(findPointerIndex);
                        if (Float.isNaN(y) || Float.isNaN(x)) {
                            if (Build.DEBUG_ONEPLUS) {
                                Log.d(PanelViewController.TAG, "onTouch - NaN in MotionEvent: x = " + x + ", y = " + y);
                            }
                            return true;
                        }
                        if (motionEvent.getActionMasked() == 0) {
                            PanelViewController panelViewController2 = PanelViewController.this;
                            panelViewController2.mGestureWaitForTouchSlop = panelViewController2.shouldGestureWaitForTouchSlop();
                            PanelViewController panelViewController3 = PanelViewController.this;
                            panelViewController3.mIgnoreXTouchSlop = panelViewController3.isFullyCollapsed() || PanelViewController.this.shouldGestureIgnoreXTouchSlop(x, y);
                        }
                        int actionMasked = motionEvent.getActionMasked();
                        if (actionMasked != 0) {
                            if (actionMasked != 1) {
                                if (actionMasked == 2) {
                                    PanelViewController.this.addMovement(motionEvent);
                                    float f = y - PanelViewController.this.mInitialTouchY;
                                    if (Math.abs(f) > PanelViewController.this.getTouchSlop(motionEvent) && (Math.abs(f) > Math.abs(x - PanelViewController.this.mInitialTouchX) || PanelViewController.this.mIgnoreXTouchSlop)) {
                                        PanelViewController.this.mTouchSlopExceeded = true;
                                        if (PanelViewController.this.mGestureWaitForTouchSlop) {
                                            PanelViewController panelViewController4 = PanelViewController.this;
                                            if (!panelViewController4.mTracking && !panelViewController4.mCollapsedAndHeadsUpOnDown) {
                                                if (!PanelViewController.this.mJustPeeked && PanelViewController.this.mInitialOffsetOnTouch != 0.0f) {
                                                    PanelViewController panelViewController5 = PanelViewController.this;
                                                    panelViewController5.startExpandMotion(x, y, false, panelViewController5.mExpandedHeight);
                                                    f = 0.0f;
                                                }
                                                PanelViewController.this.cancelHeightAnimator();
                                                PanelViewController.this.onTrackingStarted();
                                            }
                                        }
                                    }
                                    float max = Math.max(0.0f, PanelViewController.this.mInitialOffsetOnTouch + f);
                                    if (max > PanelViewController.this.mPeekHeight) {
                                        if (PanelViewController.this.mPeekAnimator != null) {
                                            PanelViewController.this.mPeekAnimator.cancel();
                                        }
                                        PanelViewController.this.mJustPeeked = false;
                                    } else if (PanelViewController.this.mPeekAnimator == null && PanelViewController.this.mJustPeeked) {
                                        PanelViewController panelViewController6 = PanelViewController.this;
                                        panelViewController6.mInitialOffsetOnTouch = panelViewController6.mExpandedHeight;
                                        PanelViewController.this.mInitialTouchY = y;
                                        PanelViewController panelViewController7 = PanelViewController.this;
                                        panelViewController7.mMinExpandHeight = panelViewController7.mExpandedHeight;
                                        PanelViewController.this.mJustPeeked = false;
                                    }
                                    float max2 = Math.max(max, PanelViewController.this.mMinExpandHeight);
                                    if ((-f) >= ((float) PanelViewController.this.getFalsingThreshold())) {
                                        PanelViewController.this.mTouchAboveFalsingThreshold = true;
                                        PanelViewController panelViewController8 = PanelViewController.this;
                                        panelViewController8.mUpwardsWhenThresholdReached = panelViewController8.isDirectionUpwards(x, y);
                                    }
                                    if (Float.isNaN(max2)) {
                                        Log.i(PanelViewController.TAG, "onTouch newHeight is NaN:" + PanelViewController.this.mMinExpandHeight + " mExpandedHeight:" + PanelViewController.this.mExpandedHeight + " mInitialOffsetOnTouch:" + PanelViewController.this.mInitialOffsetOnTouch + " h:" + f + " y:" + y + " mInitialTouchY:" + PanelViewController.this.mInitialTouchY);
                                    }
                                    if (!PanelViewController.this.mJustPeeked && ((!PanelViewController.this.mGestureWaitForTouchSlop || PanelViewController.this.mTracking) && !PanelViewController.this.isTrackingBlocked())) {
                                        PanelViewController.this.setExpandedHeightInternal(max2);
                                    }
                                } else if (actionMasked != 3) {
                                    if (actionMasked != 5) {
                                        if (actionMasked == 6 && PanelViewController.this.mTrackingPointer == (pointerId = motionEvent.getPointerId(motionEvent.getActionIndex()))) {
                                            int i = motionEvent.getPointerId(0) != pointerId ? 0 : 1;
                                            float y2 = motionEvent.getY(i);
                                            float x2 = motionEvent.getX(i);
                                            PanelViewController.this.mTrackingPointer = motionEvent.getPointerId(i);
                                            PanelViewController panelViewController9 = PanelViewController.this;
                                            panelViewController9.startExpandMotion(x2, y2, true, panelViewController9.mExpandedHeight);
                                        }
                                    } else if (PanelViewController.this.mStatusBarStateController.getState() == 1) {
                                        PanelViewController.this.mMotionAborted = true;
                                        PanelViewController.this.endMotionEvent(motionEvent, x, y, true);
                                        return false;
                                    }
                                }
                            }
                            PanelViewController.this.addMovement(motionEvent);
                            PanelViewController.this.endMotionEvent(motionEvent, x, y, false);
                        } else {
                            PanelViewController panelViewController10 = PanelViewController.this;
                            panelViewController10.startExpandMotion(x, y, false, panelViewController10.mExpandedHeight);
                            PanelViewController.this.mJustPeeked = false;
                            PanelViewController.this.mMinExpandHeight = 0.0f;
                            PanelViewController panelViewController11 = PanelViewController.this;
                            panelViewController11.mPanelClosedOnDown = panelViewController11.isFullyCollapsed();
                            PanelViewController.this.mHasLayoutedSinceDown = false;
                            PanelViewController.this.mUpdateFlingOnLayout = false;
                            PanelViewController.this.mMotionAborted = false;
                            PanelViewController panelViewController12 = PanelViewController.this;
                            panelViewController12.mPeekTouching = panelViewController12.mPanelClosedOnDown;
                            PanelViewController.this.mDownTime = SystemClock.uptimeMillis();
                            PanelViewController.this.mTouchAboveFalsingThreshold = false;
                            PanelViewController panelViewController13 = PanelViewController.this;
                            panelViewController13.mCollapsedAndHeadsUpOnDown = panelViewController13.isFullyCollapsed() && PanelViewController.this.mHeadsUpManager.hasPinnedHeadsUp();
                            PanelViewController.this.addMovement(motionEvent);
                            if (!PanelViewController.this.mGestureWaitForTouchSlop || ((PanelViewController.this.mHeightAnimator != null && !PanelViewController.this.mHintAnimationRunning) || PanelViewController.this.mPeekAnimator != null)) {
                                PanelViewController panelViewController14 = PanelViewController.this;
                                panelViewController14.mTouchSlopExceeded = (panelViewController14.mHeightAnimator != null && !PanelViewController.this.mHintAnimationRunning) || PanelViewController.this.mPeekAnimator != null || PanelViewController.this.mTouchSlopExceededBeforeDown;
                                PanelViewController.this.cancelHeightAnimator();
                                PanelViewController.this.cancelPeek();
                                PanelViewController.this.onTrackingStarted();
                            }
                            if (PanelViewController.this.isFullyCollapsed() && !PanelViewController.this.mHeadsUpManager.hasPinnedHeadsUp() && !PanelViewController.this.mStatusBar.isBouncerShowing() && !((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).isHighLightHintShow() && !((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).isCarModeHighlightHintSHow()) {
                                PanelViewController.this.startOpening(motionEvent);
                            }
                        }
                        return !PanelViewController.this.mGestureWaitForTouchSlop || PanelViewController.this.mTracking;
                    }
                    if (motionEvent.getActionMasked() == 0) {
                        Log.d(PanelViewController.TAG, "disable panel touch when QS disabled");
                    }
                    return true;
                } else {
                    if (motionEvent.getAction() == 1) {
                        PanelViewController.this.expand(true);
                    }
                    return true;
                }
            }
        }
    }

    public class OnLayoutChangeListener implements View.OnLayoutChangeListener {
        public OnLayoutChangeListener() {
        }

        @Override // android.view.View.OnLayoutChangeListener
        public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
            PanelViewController.this.mStatusBar.onPanelLaidOut();
            PanelViewController.this.requestPanelHeightUpdate();
            PanelViewController.this.mHasLayoutedSinceDown = true;
            if (PanelViewController.this.mUpdateFlingOnLayout) {
                PanelViewController.this.abortAnimations();
                PanelViewController panelViewController = PanelViewController.this;
                panelViewController.fling(panelViewController.mUpdateFlingVelocity, true);
                PanelViewController.this.mUpdateFlingOnLayout = false;
            }
        }
    }

    public class OnConfigurationChangedListener implements PanelView.OnConfigurationChangedListener {
        public OnConfigurationChangedListener() {
        }

        @Override // com.android.systemui.statusbar.phone.PanelView.OnConfigurationChangedListener
        public void onConfigurationChanged(Configuration configuration) {
            PanelViewController.this.loadDimens();
            PanelViewController panelViewController = PanelViewController.this;
            int i = panelViewController.mOrientation;
            int i2 = configuration.orientation;
            if (i != i2) {
                panelViewController.mOrientation = i2;
            }
        }
    }

    private boolean showEmptyShadeView() {
        if (this.mStatusBarStateController.getState() == 1 || this.mEntryManager.getActiveNotificationsCount() != 0) {
            return false;
        }
        return true;
    }
}
