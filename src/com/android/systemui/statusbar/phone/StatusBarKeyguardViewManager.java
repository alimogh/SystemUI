package com.android.systemui.statusbar.phone;

import android.app.INotificationManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.WindowInsets;
import android.view.WindowManagerGlobal;
import androidx.appcompat.R$styleable;
import com.android.internal.util.LatencyTracker;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.KeyguardViewController;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.DejankUtils;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.dock.DockManager;
import com.android.systemui.keyguard.DismissCallbackRegistry;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.shared.system.SysUiStatsLog;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.notification.ViewGroupFadeHelper;
import com.android.systemui.statusbar.phone.KeyguardBouncer;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.statusbar.phone.OpStatusBarKeyguardViewManager;
import com.oneplus.util.OpNavBarUtils;
import java.io.PrintWriter;
import java.util.ArrayList;
public class StatusBarKeyguardViewManager extends OpStatusBarKeyguardViewManager implements RemoteInputController.Callback, StatusBarStateController.StateListener, ConfigurationController.ConfigurationListener, PanelExpansionListener, NavigationModeController.ModeChangedListener, KeyguardViewController {
    private static String TAG = "StatusBarKeyguardViewManager";
    private ActivityStarter.OnDismissAction mAfterKeyguardGoneAction;
    private final ArrayList<Runnable> mAfterKeyguardGoneRunnables = new ArrayList<>();
    private BiometricUnlockController mBiometricUnlockController;
    protected KeyguardBouncer mBouncer;
    private KeyguardBypassController mBypassController;
    private final ConfigurationController mConfigurationController;
    private ViewGroup mContainer;
    protected final Context mContext;
    private final DockManager.DockEventListener mDockEventListener = new DockManager.DockEventListener(this) { // from class: com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager.2
    };
    private final DockManager mDockManager;
    private boolean mDozing;
    private final KeyguardBouncer.BouncerExpansionCallback mExpansionCallback = new KeyguardBouncer.BouncerExpansionCallback() { // from class: com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager.1
        @Override // com.android.systemui.statusbar.phone.KeyguardBouncer.BouncerExpansionCallback
        public void onFullyShown() {
            StatusBarKeyguardViewManager.this.updateStates();
            StatusBarKeyguardViewManager.this.updateLockIcon();
        }

        @Override // com.android.systemui.statusbar.phone.KeyguardBouncer.BouncerExpansionCallback
        public void onStartingToHide() {
            StatusBarKeyguardViewManager.this.updateStates();
        }

        @Override // com.android.systemui.statusbar.phone.KeyguardBouncer.BouncerExpansionCallback
        public void onStartingToShow() {
            StatusBarKeyguardViewManager.this.updateLockIcon();
        }

        @Override // com.android.systemui.statusbar.phone.KeyguardBouncer.BouncerExpansionCallback
        public void onFullyHidden() {
            StatusBarKeyguardViewManager.this.updateStates();
            StatusBarKeyguardViewManager.this.updateLockIcon();
        }
    };
    private boolean mFaceLockUnlocking = false;
    boolean mFingerprintAuthenticated = false;
    protected boolean mFirstUpdate = true;
    private boolean mGesturalNav;
    private boolean mGlobalActionsVisible = false;
    private INotificationManager mINotificationManager;
    private boolean mIsDocked;
    private Runnable mKeyguardGoneCancelAction;
    private final KeyguardStateController mKeyguardStateController;
    private final KeyguardUpdateMonitor mKeyguardUpdateManager;
    private int mLastBiometricMode;
    private boolean mLastBouncerDismissible;
    private boolean mLastBouncerShowing;
    private boolean mLastDozing;
    private boolean mLastFaceLockUnlocking;
    private boolean mLastFingerprintAuthenticated;
    private boolean mLastGesturalNav;
    private boolean mLastGlobalActionsVisible = false;
    private boolean mLastIsDocked;
    private boolean mLastLockVisible;
    protected boolean mLastOccluded;
    private boolean mLastPulsing;
    protected boolean mLastRemoteInputActive;
    protected boolean mLastShowing;
    private ViewGroup mLockIconContainer;
    protected LockPatternUtils mLockPatternUtils;
    private Runnable mMakeNavigationBarVisibleRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager.7
        @Override // java.lang.Runnable
        public void run() {
            if (ViewRootImpl.sNewInsetsMode == 2) {
                StatusBarKeyguardViewManager.this.mStatusBar.getNotificationShadeWindowView().getWindowInsetsController().show(WindowInsets.Type.navigationBars());
            } else {
                StatusBarKeyguardViewManager.this.mStatusBar.getNavigationBarView().getRootView().setVisibility(0);
            }
        }
    };
    private final NotificationMediaManager mMediaManager;
    private final NavigationModeController mNavigationModeController;
    private View mNotificationContainer;
    private NotificationPanelViewController mNotificationPanelViewController;
    private final NotificationShadeWindowController mNotificationShadeWindowController;
    protected boolean mOccluded;
    private DismissWithActionRequest mPendingWakeupAction;
    private boolean mPulsing;
    protected boolean mRemoteInputActive;
    protected boolean mShowing;
    protected StatusBar mStatusBar;
    private final SysuiStatusBarStateController mStatusBarStateController;
    private final KeyguardUpdateMonitorCallback mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager.3
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onEmergencyCallAction() {
            StatusBarKeyguardViewManager statusBarKeyguardViewManager = StatusBarKeyguardViewManager.this;
            if (statusBarKeyguardViewManager.mOccluded) {
                statusBarKeyguardViewManager.reset(true);
            }
        }
    };
    protected ViewMediatorCallback mViewMediatorCallback;

    @Override // com.android.keyguard.KeyguardViewController
    public void onCancelClicked() {
    }

    /* access modifiers changed from: protected */
    public boolean shouldDestroyViewOnReset() {
        return false;
    }

    public StatusBarKeyguardViewManager(Context context, ViewMediatorCallback viewMediatorCallback, LockPatternUtils lockPatternUtils, SysuiStatusBarStateController sysuiStatusBarStateController, ConfigurationController configurationController, KeyguardUpdateMonitor keyguardUpdateMonitor, NavigationModeController navigationModeController, DockManager dockManager, NotificationShadeWindowController notificationShadeWindowController, KeyguardStateController keyguardStateController, NotificationMediaManager notificationMediaManager) {
        this.mContext = context;
        this.mViewMediatorCallback = viewMediatorCallback;
        this.mLockPatternUtils = lockPatternUtils;
        this.mConfigurationController = configurationController;
        this.mNavigationModeController = navigationModeController;
        this.mNotificationShadeWindowController = notificationShadeWindowController;
        this.mKeyguardStateController = keyguardStateController;
        this.mMediaManager = notificationMediaManager;
        this.mKeyguardUpdateManager = keyguardUpdateMonitor;
        this.mStatusBarStateController = sysuiStatusBarStateController;
        this.mDockManager = dockManager;
        init(context);
    }

    public void registerStatusBar(StatusBar statusBar, ViewGroup viewGroup, NotificationPanelViewController notificationPanelViewController, BiometricUnlockController biometricUnlockController, DismissCallbackRegistry dismissCallbackRegistry, ViewGroup viewGroup2, View view, KeyguardBypassController keyguardBypassController, FalsingManager falsingManager) {
        this.mStatusBar = statusBar;
        this.mContainer = viewGroup;
        this.mLockIconContainer = viewGroup2;
        if (viewGroup2 != null) {
            this.mLastLockVisible = viewGroup2.getVisibility() == 0;
        }
        this.mBiometricUnlockController = biometricUnlockController;
        this.mBouncer = SystemUIFactory.getInstance().createKeyguardBouncer(this.mContext, this.mViewMediatorCallback, this.mLockPatternUtils, viewGroup, dismissCallbackRegistry, this.mExpansionCallback, this.mKeyguardStateController, falsingManager, keyguardBypassController);
        this.mNotificationPanelViewController = notificationPanelViewController;
        notificationPanelViewController.addExpansionListener(this);
        this.mBypassController = keyguardBypassController;
        this.mNotificationContainer = view;
        registerListeners();
        this.mINotificationManager = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
    }

    private void registerListeners() {
        this.mKeyguardUpdateManager.registerCallback(this.mUpdateMonitorCallback);
        this.mStatusBarStateController.addCallback(this);
        this.mConfigurationController.addCallback(this);
        this.mGesturalNav = QuickStepContract.isGesturalMode(this.mNavigationModeController.addListener(this));
        DockManager dockManager = this.mDockManager;
        if (dockManager != null) {
            dockManager.addListener(this.mDockEventListener);
            this.mIsDocked = this.mDockManager.isDocked();
        }
    }

    @Override // com.android.systemui.statusbar.phone.PanelExpansionListener
    public void onPanelExpansionChanged(float f, boolean z) {
        if (this.mNotificationPanelViewController.isUnlockHintRunning()) {
            this.mBouncer.setExpansion(1.0f);
        } else if (bouncerNeedsScrimming()) {
            this.mBouncer.setExpansion(0.0f);
        } else if (this.mShowing) {
            if (!isWakeAndUnlocking() && !this.mStatusBar.isInLaunchTransition()) {
                this.mBouncer.setExpansion(f);
            }
            if (f != 1.0f && z && !this.mKeyguardStateController.canDismissLockScreen() && !this.mBouncer.isShowing() && !this.mBouncer.isAnimatingAway()) {
                this.mBouncer.show(false, false);
            }
        } else if (this.mPulsing && f == 0.0f) {
            this.mStatusBar.wakeUpIfDozing(SystemClock.uptimeMillis(), this.mContainer, "BOUNCER_VISIBLE");
        }
    }

    @Override // com.android.systemui.statusbar.phone.PanelExpansionListener
    public void onQsExpansionChanged(float f) {
        updateLockIcon();
    }

    public void setGlobalActionsVisible(boolean z) {
        this.mGlobalActionsVisible = z;
        updateStates();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateLockIcon() {
        long j;
        if (this.mLockIconContainer != null) {
            boolean z = true;
            int i = 0;
            boolean z2 = this.mStatusBarStateController.getState() == 1 && !this.mNotificationPanelViewController.isQsExpanded();
            if ((!this.mBouncer.isShowing() && !z2) || this.mBouncer.isAnimatingAway() || this.mKeyguardStateController.isKeyguardFadingAway()) {
                z = false;
            }
            if (this.mLastLockVisible != z) {
                this.mLastLockVisible = z;
                if (z) {
                    CrossFadeHelper.fadeIn(this.mLockIconContainer, 220, 0);
                    return;
                }
                if (needsBypassFading()) {
                    j = 67;
                } else {
                    j = 110;
                    i = R$styleable.AppCompatTheme_windowFixedHeightMajor;
                }
                CrossFadeHelper.fadeOut(this.mLockIconContainer, j, i, null);
            }
        }
    }

    @Override // com.android.keyguard.KeyguardViewController
    public void show(Bundle bundle) {
        this.mShowing = true;
        this.mNotificationShadeWindowController.setKeyguardShowing(true);
        KeyguardStateController keyguardStateController = this.mKeyguardStateController;
        keyguardStateController.notifyKeyguardState(this.mShowing, keyguardStateController.isOccluded());
        reset(true);
        SysUiStatsLog.write(62, 2);
        if (isSecure()) {
            try {
                this.mINotificationManager.setAppLock();
            } catch (RemoteException unused) {
                Log.w(TAG, "Talk to NotificationManagerService fail");
            }
        }
        this.mFingerprintAuthenticated = false;
    }

    /* access modifiers changed from: protected */
    public void showBouncerOrKeyguard(boolean z) {
        if (Build.DEBUG_ONEPLUS) {
            String str = TAG;
            Log.i(str, "showBouncerOrKeyguard,, mBouncer.isUserUnlocked():" + this.mBouncer.isUserUnlocked() + ", isPreventModeActive():" + OpLsState.getInstance().getPreventModeCtrl().isPreventModeActive() + ", mBouncer.isShowing():" + this.mBouncer.isShowing() + ", KeyguardUpdateMonitor.getInstance(mContext).isScreenOn()):" + KeyguardUpdateMonitor.getInstance(this.mContext).isScreenOn() + ", mBouncer.needsFullscreenBouncer():" + this.mBouncer.needsFullscreenBouncer() + ", mDozing:" + this.mDozing);
        }
        if (this.mBouncer.isUserUnlocked() || OpLsState.getInstance().getPreventModeCtrl().isPreventModeActive() || (this.mBouncer.isShowing() && KeyguardUpdateMonitor.getInstance(this.mContext).isScreenOn())) {
            if (!this.mBouncer.needsFullscreenBouncer() || this.mDozing) {
                this.mStatusBar.showKeyguard();
                if (z) {
                    hideBouncer(shouldDestroyViewOnReset());
                    this.mBouncer.prepare();
                }
            } else {
                this.mStatusBar.hideKeyguard();
                this.mBouncer.show(true);
            }
            updateStates();
            return;
        }
        this.mStatusBar.showKeyguard();
        this.mStatusBar.instantCollapseNotificationPanel();
        this.mBouncer.show(true);
        updateStates();
    }

    /* access modifiers changed from: package-private */
    public void hideBouncer(boolean z) {
        if (this.mBouncer != null) {
            if (this.mShowing) {
                this.mAfterKeyguardGoneAction = null;
                Runnable runnable = this.mKeyguardGoneCancelAction;
                if (runnable != null) {
                    runnable.run();
                    this.mKeyguardGoneCancelAction = null;
                }
            }
            this.mBouncer.hide(z);
            cancelPendingWakeupAction();
        }
    }

    @Override // com.android.keyguard.KeyguardViewController
    public void showBouncer(boolean z) {
        if (this.mShowing && !this.mBouncer.isShowing()) {
            this.mBouncer.show(isFullscreenBouncer(), z);
        }
        updateStates();
    }

    public void dismissWithAction(ActivityStarter.OnDismissAction onDismissAction, Runnable runnable, boolean z) {
        dismissWithAction(onDismissAction, runnable, z, null);
    }

    public void dismissWithAction(ActivityStarter.OnDismissAction onDismissAction, Runnable runnable, boolean z, String str) {
        if (this.mShowing) {
            cancelPendingWakeupAction();
            if (this.mDozing && !isWakeAndUnlocking()) {
                this.mPendingWakeupAction = new DismissWithActionRequest(onDismissAction, runnable, z, str);
                return;
            } else if (!z) {
                this.mBouncer.showWithDismissAction(onDismissAction, runnable);
            } else {
                this.mAfterKeyguardGoneAction = onDismissAction;
                this.mKeyguardGoneCancelAction = runnable;
                this.mBouncer.show(false);
            }
        }
        updateStates();
    }

    private boolean isWakeAndUnlocking() {
        int mode = this.mBiometricUnlockController.getMode();
        return mode == 1 || mode == 2;
    }

    public void addAfterKeyguardGoneRunnable(Runnable runnable) {
        this.mAfterKeyguardGoneRunnables.add(runnable);
    }

    @Override // com.android.keyguard.KeyguardViewController
    public void reset(boolean z) {
        if (this.mShowing) {
            if (!this.mOccluded || this.mDozing) {
                showBouncerOrKeyguard(z);
            } else {
                this.mStatusBar.hideKeyguard();
                if (z || this.mBouncer.needsFullscreenBouncer()) {
                    hideBouncer(false);
                }
            }
            this.mKeyguardUpdateManager.sendKeyguardReset();
            updateStates();
        }
    }

    @Override // com.android.keyguard.KeyguardViewController
    public void onStartedWakingUp() {
        this.mStatusBar.getNotificationShadeWindowView().getWindowInsetsController().setAnimationsDisabled(false);
    }

    @Override // com.android.keyguard.KeyguardViewController
    public void onStartedGoingToSleep() {
        this.mStatusBar.getNotificationShadeWindowView().getWindowInsetsController().setAnimationsDisabled(true);
    }

    @Override // com.android.keyguard.KeyguardViewController
    public void onFinishedGoingToSleep() {
        this.mBouncer.onScreenTurnedOff();
    }

    @Override // com.android.systemui.statusbar.RemoteInputController.Callback
    public void onRemoteInputActive(boolean z) {
        this.mRemoteInputActive = z;
        updateStates();
    }

    private void setDozing(boolean z) {
        if (this.mDozing != z) {
            this.mDozing = z;
            if (z || this.mBouncer.needsFullscreenBouncer() || this.mOccluded) {
                reset(z);
            }
            updateStates();
            if (!z) {
                launchPendingWakeupAction();
            }
        }
    }

    public void setPulsing(boolean z) {
        if (this.mPulsing != z) {
            this.mPulsing = z;
            updateStates();
        }
    }

    @Override // com.android.keyguard.KeyguardViewController
    public void setNeedsInput(boolean z) {
        this.mNotificationShadeWindowController.setKeyguardNeedsInput(z);
    }

    @Override // com.android.keyguard.KeyguardViewController
    public boolean isUnlockWithWallpaper() {
        return this.mNotificationShadeWindowController.isShowingWallpaper();
    }

    @Override // com.android.keyguard.KeyguardViewController
    public void setOccluded(boolean z, boolean z2) {
        this.mStatusBar.setOccluded(z);
        boolean z3 = true;
        if (z && !this.mOccluded && this.mShowing) {
            SysUiStatsLog.write(62, 3);
            if (this.mStatusBar.isInLaunchTransition()) {
                this.mOccluded = true;
                this.mStatusBar.fadeKeyguardAfterLaunchTransition(null, new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager.4
                    @Override // java.lang.Runnable
                    public void run() {
                        StatusBarKeyguardViewManager.this.mNotificationShadeWindowController.setKeyguardOccluded(StatusBarKeyguardViewManager.this.mOccluded);
                        StatusBarKeyguardViewManager.this.reset(true);
                    }
                });
                return;
            }
        } else if (!z && this.mOccluded && this.mShowing) {
            SysUiStatsLog.write(62, 2);
        }
        boolean z4 = !this.mOccluded && z;
        this.mOccluded = z;
        if (this.mShowing) {
            NotificationMediaManager notificationMediaManager = this.mMediaManager;
            if (!z2 || z) {
                z3 = false;
            }
            notificationMediaManager.updateMediaMetaData(false, z3);
        }
        this.mNotificationShadeWindowController.setKeyguardOccluded(z);
        if (!this.mDozing) {
            reset(z4);
        }
        if (z2 && !z && this.mShowing && !this.mBouncer.isShowing()) {
            this.mStatusBar.animateKeyguardUnoccluding();
        }
    }

    public boolean isOccluded() {
        return this.mOccluded;
    }

    @Override // com.android.keyguard.KeyguardViewController
    public void startPreHideAnimation(Runnable runnable) {
        if (this.mBouncer.isShowing()) {
            this.mBouncer.startPreHideAnimation(runnable);
            this.mStatusBar.onBouncerPreHideAnimation();
        } else if (runnable != null) {
            runnable.run();
        }
        this.mNotificationPanelViewController.blockExpansionForCurrentTouch();
        updateLockIcon();
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x007e  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x009a  */
    @Override // com.android.keyguard.KeyguardViewController
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void hide(long r19, long r21) {
        /*
        // Method dump skipped, instructions count: 239
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager.hide(long, long):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$hide$0 */
    public /* synthetic */ void lambda$hide$0$StatusBarKeyguardViewManager() {
        this.mStatusBar.hideKeyguard();
        onKeyguardFadedAway();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$hide$1 */
    public /* synthetic */ void lambda$hide$1$StatusBarKeyguardViewManager() {
        this.mStatusBar.hideKeyguard();
    }

    private boolean needsBypassFading() {
        if ((this.mBiometricUnlockController.getMode() == 7 || this.mBiometricUnlockController.getMode() == 2 || this.mBiometricUnlockController.getMode() == 1) && this.mBypassController.getBypassEnabled()) {
            return true;
        }
        return false;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        if (Build.DEBUG_ONEPLUS) {
            Log.i(TAG, "onDensityOrFontScaleChanged");
        }
        hideBouncer(true);
        reset(true);
    }

    @Override // com.android.systemui.statusbar.phone.NavigationModeController.ModeChangedListener
    public void onNavigationModeChanged(int i) {
        boolean isGesturalMode = QuickStepContract.isGesturalMode(i);
        if (isGesturalMode != this.mGesturalNav) {
            this.mGesturalNav = isGesturalMode;
            updateStates();
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onThemeChanged() {
        hideBouncer(true);
        this.mBouncer.prepare();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onKeyguardFadedAway$2 */
    public /* synthetic */ void lambda$onKeyguardFadedAway$2$StatusBarKeyguardViewManager() {
        this.mNotificationShadeWindowController.setKeyguardFadingAway(false);
    }

    public void onKeyguardFadedAway() {
        this.mContainer.postDelayed(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarKeyguardViewManager$nb9yQRGKq0kAyQz17NqvixIA7LU
            @Override // java.lang.Runnable
            public final void run() {
                StatusBarKeyguardViewManager.this.lambda$onKeyguardFadedAway$2$StatusBarKeyguardViewManager();
            }
        }, 100);
        ViewGroupFadeHelper.reset(this.mNotificationPanelViewController.getView());
        this.mStatusBar.finishKeyguardFadingAway();
        this.mBiometricUnlockController.finishKeyguardFadingAway();
        this.mKeyguardUpdateManager.notifyKeyguardFadedAway();
        WindowManagerGlobal.getInstance().trimMemory(20);
    }

    private void wakeAndUnlockDejank() {
        if (this.mBiometricUnlockController.getMode() == 1 && LatencyTracker.isEnabled(this.mContext)) {
            DejankUtils.postAfterTraversal(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarKeyguardViewManager$WtAkg4w14mbTRLi3kx_TWboxp-s
                @Override // java.lang.Runnable
                public final void run() {
                    StatusBarKeyguardViewManager.this.lambda$wakeAndUnlockDejank$3$StatusBarKeyguardViewManager();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$wakeAndUnlockDejank$3 */
    public /* synthetic */ void lambda$wakeAndUnlockDejank$3$StatusBarKeyguardViewManager() {
        LatencyTracker.getInstance(this.mContext).onActionEnd(2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void executeAfterKeyguardGoneAction() {
        ActivityStarter.OnDismissAction onDismissAction = this.mAfterKeyguardGoneAction;
        if (onDismissAction != null) {
            onDismissAction.onDismiss();
            this.mAfterKeyguardGoneAction = null;
        }
        this.mKeyguardGoneCancelAction = null;
        for (int i = 0; i < this.mAfterKeyguardGoneRunnables.size(); i++) {
            this.mAfterKeyguardGoneRunnables.get(i).run();
        }
        this.mAfterKeyguardGoneRunnables.clear();
    }

    @Override // com.android.keyguard.KeyguardViewController
    public void dismissAndCollapse() {
        this.mStatusBar.executeRunnableDismissingKeyguard(null, null, true, false, true);
    }

    public boolean isSecure() {
        return this.mBouncer.isSecure();
    }

    @Override // com.android.keyguard.KeyguardViewController
    public boolean isShowing() {
        return this.mShowing;
    }

    public boolean onBackPressed(boolean z) {
        if (!this.mBouncer.isShowing()) {
            return false;
        }
        if (this.mBouncer.isCheckingPassword()) {
            Log.d(TAG, "onBackPressed when checking pass");
            return true;
        }
        this.mStatusBar.endAffordanceLaunch();
        if (!this.mBouncer.isUserUnlocked()) {
            reset(z);
            return true;
        }
        if (!this.mBouncer.isScrimmed() || this.mBouncer.needsFullscreenBouncer()) {
            reset(z);
        } else {
            hideBouncer(false);
            updateStates();
        }
        return true;
    }

    @Override // com.android.keyguard.KeyguardViewController
    public boolean isBouncerShowing() {
        return this.mBouncer.isShowing();
    }

    @Override // com.android.keyguard.KeyguardViewController
    public boolean bouncerIsOrWillBeShowing() {
        return this.mBouncer.isShowing() || this.mBouncer.inTransit();
    }

    public boolean isFullscreenBouncer() {
        return this.mBouncer.isFullscreenBouncer();
    }

    private long getNavBarShowDelay() {
        if (this.mKeyguardStateController.isKeyguardFadingAway()) {
            return this.mKeyguardStateController.getKeyguardFadingAwayDelay();
        }
        return this.mBouncer.isShowing() ? 320 : 0;
    }

    /* access modifiers changed from: protected */
    public void updateStates() {
        int systemUiVisibility = this.mContainer.getSystemUiVisibility();
        boolean z = this.mShowing;
        boolean z2 = this.mOccluded;
        boolean isShowing = this.mBouncer.isShowing();
        boolean z3 = true;
        boolean z4 = !this.mBouncer.isFullscreenBouncer();
        boolean z5 = this.mRemoteInputActive;
        if ((z4 || !z || z5) != (this.mLastBouncerDismissible || !this.mLastShowing || this.mLastRemoteInputActive) || this.mFirstUpdate) {
            if (z4 || !z || z5) {
                this.mContainer.setSystemUiVisibility(-4194305 & systemUiVisibility);
            } else {
                this.mContainer.setSystemUiVisibility(4194304 | systemUiVisibility);
            }
        }
        boolean isNavBarVisible = isNavBarVisible();
        boolean lastNavBarVisible = getLastNavBarVisible();
        if (isNavBarVisible != lastNavBarVisible || this.mFirstUpdate) {
            updateNavigationBarVisibility(isNavBarVisible);
        }
        Log.d(TAG, "updateStates, vis:" + systemUiVisibility + ",Showing:" + this.mShowing + ",LastShowing:" + this.mLastShowing + ",Occluded:" + this.mOccluded + ",LastOccluded:" + this.mLastOccluded + ",BouncerShow:" + isShowing + ",LastBouncerShow:" + this.mLastBouncerShowing + ",BouncerDismiss:" + z4 + ",LastBouncerDismiss:" + this.mLastBouncerDismissible + ",navBarVisible:" + isNavBarVisible + ",lastNavBarVisible:" + lastNavBarVisible + ",wakeAndUnlocking:" + isWakeAndUnlocking() + ",mDozing:" + this.mDozing + ",mGesturalNav:" + this.mGesturalNav + ",mFingerprintAuthenticated:" + this.mFingerprintAuthenticated + ",mLastBiometricMode:" + this.mLastBiometricMode + ",mFaceLockUnlocking:" + this.mFaceLockUnlocking + ",mLastFaceLockUnlocking:" + this.mLastFaceLockUnlocking);
        if (isShowing != this.mLastBouncerShowing || this.mFirstUpdate) {
            this.mNotificationShadeWindowController.setBouncerShowing(isShowing);
            this.mStatusBar.setBouncerShowing(isShowing);
            updateLockIcon();
        }
        if ((z && !z2) != (this.mLastShowing && !this.mLastOccluded) || this.mFirstUpdate) {
            if (OpNavBarUtils.isSupportHideNavBar() && this.mStatusBar.getNavigationBarView() != null) {
                this.mStatusBar.getNavigationBarView().onShowKeyguard(z && !z2);
            }
            KeyguardUpdateMonitor keyguardUpdateMonitor = this.mKeyguardUpdateManager;
            if (!z || z2) {
                z3 = false;
            }
            keyguardUpdateMonitor.onKeyguardVisibilityChanged(z3);
        }
        if (isShowing != this.mLastBouncerShowing || this.mFirstUpdate) {
            this.mKeyguardUpdateManager.sendKeyguardBouncerChanged(isShowing);
        }
        this.mFirstUpdate = false;
        this.mLastShowing = z;
        this.mLastGlobalActionsVisible = this.mGlobalActionsVisible;
        this.mLastOccluded = z2;
        this.mLastBouncerShowing = isShowing;
        this.mLastBouncerDismissible = z4;
        this.mLastRemoteInputActive = z5;
        this.mLastDozing = this.mDozing;
        this.mLastPulsing = this.mPulsing;
        this.mLastBiometricMode = this.mBiometricUnlockController.getMode();
        this.mLastGesturalNav = this.mGesturalNav;
        this.mLastIsDocked = this.mIsDocked;
        this.mLastFingerprintAuthenticated = this.mFingerprintAuthenticated;
        this.mLastFaceLockUnlocking = this.mFaceLockUnlocking;
        this.mStatusBar.onKeyguardViewManagerStatesUpdated();
    }

    /* access modifiers changed from: protected */
    public void updateNavigationBarVisibility(boolean z) {
        if (this.mStatusBar.getNavigationBarView() == null) {
            return;
        }
        if (z) {
            long navBarShowDelay = getNavBarShowDelay();
            if (navBarShowDelay == 0) {
                this.mMakeNavigationBarVisibleRunnable.run();
            } else {
                this.mContainer.postOnAnimationDelayed(this.mMakeNavigationBarVisibleRunnable, navBarShowDelay);
            }
        } else {
            this.mContainer.removeCallbacks(this.mMakeNavigationBarVisibleRunnable);
            if (ViewRootImpl.sNewInsetsMode == 2) {
                this.mStatusBar.getNotificationShadeWindowView().getWindowInsetsController().hide(WindowInsets.Type.navigationBars());
            } else {
                this.mStatusBar.getNavigationBarView().getRootView().setVisibility(8);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isNavBarVisible() {
        int mode = this.mBiometricUnlockController.getMode();
        boolean z = this.mShowing && !this.mOccluded;
        return (!z && !(this.mDozing && mode != 2)) || this.mBouncer.isShowing() || this.mRemoteInputActive || (((z && !this.mDozing) || (this.mPulsing && !this.mIsDocked)) && this.mGesturalNav) || (this.mShowing && this.mFingerprintAuthenticated && !this.mGesturalNav) || this.mFaceLockUnlocking || this.mGlobalActionsVisible;
    }

    /* access modifiers changed from: protected */
    public boolean getLastNavBarVisible() {
        boolean z = this.mLastShowing && !this.mLastOccluded;
        return (!z && !(this.mLastDozing && this.mLastBiometricMode != 2)) || this.mLastBouncerShowing || this.mLastRemoteInputActive || (((z && !this.mLastDozing) || (this.mLastPulsing && !this.mLastIsDocked)) && this.mLastGesturalNav) || (this.mLastShowing && this.mLastFingerprintAuthenticated && !this.mGesturalNav) || this.mLastFaceLockUnlocking || this.mLastGlobalActionsVisible;
    }

    public void onFingerprintAuthenticated() {
        this.mFingerprintAuthenticated = true;
        updateStates();
    }

    public void onFacelockUnlocking(boolean z) {
        this.mFaceLockUnlocking = z;
        updateStates();
    }

    public boolean shouldDismissOnMenuPressed() {
        return this.mBouncer.shouldDismissOnMenuPressed();
    }

    public boolean interceptMediaKey(KeyEvent keyEvent) {
        return this.mBouncer.interceptMediaKey(keyEvent);
    }

    public void readyForKeyguardDone() {
        this.mViewMediatorCallback.readyForKeyguardDone();
    }

    @Override // com.android.keyguard.KeyguardViewController
    public boolean shouldDisableWindowAnimationsForUnlock() {
        return this.mStatusBar.isInLaunchTransition();
    }

    @Override // com.android.keyguard.KeyguardViewController
    public boolean shouldSubtleWindowAnimationsForUnlock() {
        return needsBypassFading();
    }

    @Override // com.android.keyguard.KeyguardViewController
    public boolean isGoingToNotificationShade() {
        return this.mStatusBarStateController.leaveOpenOnKeyguardHide();
    }

    @Override // com.android.keyguard.KeyguardViewController
    public void keyguardGoingAway() {
        this.mStatusBar.keyguardGoingAway();
    }

    @Override // com.android.keyguard.KeyguardViewController
    public void setKeyguardGoingAwayState(boolean z) {
        this.mNotificationShadeWindowController.setKeyguardGoingAway(z);
    }

    @Override // com.android.keyguard.KeyguardViewController
    public void notifyKeyguardAuthenticated(boolean z) {
        this.mBouncer.notifyKeyguardAuthenticated(z);
    }

    public void showBouncerMessage(String str, ColorStateList colorStateList) {
        this.mBouncer.showMessage(str, colorStateList);
    }

    @Override // com.android.keyguard.KeyguardViewController
    public ViewRootImpl getViewRootImpl() {
        return this.mStatusBar.getStatusBarView().getViewRootImpl();
    }

    public void launchPendingWakeupAction() {
        DismissWithActionRequest dismissWithActionRequest = this.mPendingWakeupAction;
        this.mPendingWakeupAction = null;
        if (dismissWithActionRequest == null) {
            return;
        }
        if (this.mShowing) {
            dismissWithAction(dismissWithActionRequest.dismissAction, dismissWithActionRequest.cancelAction, dismissWithActionRequest.afterKeyguardGone, dismissWithActionRequest.message);
            return;
        }
        ActivityStarter.OnDismissAction onDismissAction = dismissWithActionRequest.dismissAction;
        if (onDismissAction != null) {
            onDismissAction.onDismiss();
        }
    }

    public void cancelPendingWakeupAction() {
        Runnable runnable;
        DismissWithActionRequest dismissWithActionRequest = this.mPendingWakeupAction;
        this.mPendingWakeupAction = null;
        if (dismissWithActionRequest != null && (runnable = dismissWithActionRequest.cancelAction) != null) {
            runnable.run();
        }
    }

    public boolean bouncerNeedsScrimming() {
        return this.mOccluded || this.mBouncer.willDismissWithAction() || this.mStatusBar.isFullScreenUserSwitcherState() || (this.mBouncer.isShowing() && this.mBouncer.isScrimmed()) || this.mBouncer.isFullscreenBouncer();
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println("StatusBarKeyguardViewManager:");
        printWriter.println("  mShowing: " + this.mShowing);
        printWriter.println("  mOccluded: " + this.mOccluded);
        printWriter.println("  mRemoteInputActive: " + this.mRemoteInputActive);
        printWriter.println("  mDozing: " + this.mDozing);
        printWriter.println("  mAfterKeyguardGoneAction: " + this.mAfterKeyguardGoneAction);
        printWriter.println("  mAfterKeyguardGoneRunnables: " + this.mAfterKeyguardGoneRunnables);
        printWriter.println("  mPendingWakeupAction: " + this.mPendingWakeupAction);
        printWriter.println("  mFingerprintAuthenticated: " + this.mFingerprintAuthenticated);
        KeyguardBouncer keyguardBouncer = this.mBouncer;
        if (keyguardBouncer != null) {
            keyguardBouncer.dump(printWriter);
        }
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
        updateLockIcon();
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
        setDozing(z);
    }

    public KeyguardBouncer getBouncer() {
        return this.mBouncer;
    }

    /* access modifiers changed from: private */
    public static class DismissWithActionRequest {
        final boolean afterKeyguardGone;
        final Runnable cancelAction;
        final ActivityStarter.OnDismissAction dismissAction;
        final String message;

        DismissWithActionRequest(ActivityStarter.OnDismissAction onDismissAction, Runnable runnable, boolean z, String str) {
            this.dismissAction = onDismissAction;
            this.cancelAction = runnable;
            this.afterKeyguardGone = z;
            this.message = str;
        }
    }

    public void onHideNavBar(boolean z) {
        KeyguardBouncer keyguardBouncer = this.mBouncer;
        if (keyguardBouncer != null) {
            keyguardBouncer.onHideNavBar(z);
        }
    }

    public void forceHideBouncer() {
        String str = TAG;
        Log.i(str, "forceHideBouncer isShowing:" + this.mBouncer.isShowing() + " mOccluded:" + this.mOccluded);
        if (this.mOccluded) {
            reset(true);
            updateStates();
        } else if (this.mBouncer.isShowing()) {
            this.mBouncer.forceHide();
        }
    }

    @Override // com.android.keyguard.KeyguardViewController
    public boolean isShowingLiveWallpaper(boolean z) {
        return this.mNotificationShadeWindowController.isShowingLiveWallpaper(z);
    }
}
