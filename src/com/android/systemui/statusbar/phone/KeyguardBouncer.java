package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.UserManager;
import android.util.Log;
import android.util.MathUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardHostView;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardSecurityView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.DismissCallbackRegistry;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.shared.system.SysUiStatsLog;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.oneplus.keyguard.OpKeyguardUpdateMonitor;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.statusbar.phone.OpKeyguardBouncer;
import java.io.PrintWriter;
public class KeyguardBouncer extends OpKeyguardBouncer {
    private int mBouncerPromptReason;
    protected final ViewMediatorCallback mCallback;
    protected final ViewGroup mContainer;
    protected final Context mContext;
    private final DismissCallbackRegistry mDismissCallbackRegistry;
    private float mExpansion = 1.0f;
    private final BouncerExpansionCallback mExpansionCallback;
    private final FalsingManager mFalsingManager;
    private final Handler mHandler;
    boolean mHideNavBar = false;
    private boolean mIsAnimatingAway;
    private boolean mIsScrimmed;
    private final KeyguardBypassController mKeyguardBypassController;
    private final KeyguardStateController mKeyguardStateController;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    protected KeyguardHostView mKeyguardView;
    protected final LockPatternUtils mLockPatternUtils;
    private boolean mNoAnim = false;
    private final Runnable mRemoveViewRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$iQsniWdIxLGqyYwRi09kQ-Ah02M
        @Override // java.lang.Runnable
        public final void run() {
            KeyguardBouncer.this.removeView();
        }
    };
    private final Runnable mResetRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$KeyguardBouncer$Y9Hvfk0n3yPK2FQ39O1Z5j49gj0
        @Override // java.lang.Runnable
        public final void run() {
            KeyguardBouncer.this.lambda$new$0$KeyguardBouncer();
        }
    };
    protected ViewGroup mRoot;
    private final Runnable mShowRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.KeyguardBouncer.2
        @Override // java.lang.Runnable
        public void run() {
            Log.d("KeyguardBouncer", "start to show");
            KeyguardBouncer.this.mRoot.setVisibility(0);
            KeyguardBouncer keyguardBouncer = KeyguardBouncer.this;
            keyguardBouncer.showPromptReason(keyguardBouncer.mBouncerPromptReason);
            CharSequence consumeCustomMessage = KeyguardBouncer.this.mCallback.consumeCustomMessage();
            if (consumeCustomMessage != null) {
                KeyguardBouncer.this.mKeyguardView.showErrorMessage(consumeCustomMessage);
            }
            if (KeyguardBouncer.this.mKeyguardView.getHeight() == 0 || KeyguardBouncer.this.mKeyguardView.getHeight() == KeyguardBouncer.this.mStatusBarHeight) {
                KeyguardBouncer.this.mKeyguardView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() { // from class: com.android.systemui.statusbar.phone.KeyguardBouncer.2.1
                    @Override // android.view.ViewTreeObserver.OnPreDrawListener
                    public boolean onPreDraw() {
                        KeyguardBouncer.this.mKeyguardView.getViewTreeObserver().removeOnPreDrawListener(this);
                        KeyguardBouncer.this.mKeyguardView.startAppearAnimation();
                        return true;
                    }
                });
                KeyguardBouncer.this.mKeyguardView.requestLayout();
            } else if (!KeyguardBouncer.this.mNoAnim) {
                KeyguardBouncer.this.mKeyguardView.startAppearAnimation();
            }
            KeyguardBouncer.this.mNoAnim = false;
            KeyguardBouncer.this.mShowingSoon = false;
            if (KeyguardBouncer.this.mExpansion == 0.0f) {
                KeyguardBouncer.this.mKeyguardView.onResume();
                KeyguardBouncer.this.mKeyguardView.resetSecurityContainer();
                KeyguardBouncer keyguardBouncer2 = KeyguardBouncer.this;
                keyguardBouncer2.showPromptReason(keyguardBouncer2.mBouncerPromptReason);
            }
            SysUiStatsLog.write(63, 2);
        }
    };
    private boolean mShowingSoon;
    private int mStatusBarHeight;
    private final KeyguardUpdateMonitorCallback mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.systemui.statusbar.phone.KeyguardBouncer.1
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStrongAuthStateChanged(int i) {
            KeyguardBouncer keyguardBouncer = KeyguardBouncer.this;
            keyguardBouncer.mBouncerPromptReason = keyguardBouncer.mCallback.getBouncerPromptReason();
        }

        @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
        public void onFacelockStateChanged(int i) {
            int i2 = -1;
            if (i == 3 || i == 2) {
                KeyguardBouncer.this.showMessage(" ", ColorStateList.valueOf(-1));
                return;
            }
            int facelockNotifyMsgId = KeyguardUpdateMonitor.getInstance(KeyguardBouncer.this.mContext).getFacelockNotifyMsgId(i);
            if (facelockNotifyMsgId != 0) {
                if (i == 1 || i == 7 || i == 6 || i == 8 || i == 9 || i == 10 || i == 11) {
                    i2 = -65536;
                }
                KeyguardBouncer keyguardBouncer = KeyguardBouncer.this;
                keyguardBouncer.showMessage(keyguardBouncer.mContext.getString(facelockNotifyMsgId), ColorStateList.valueOf(i2));
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onScreenTurnedOn() {
            ViewGroup viewGroup;
            if (!KeyguardBouncer.this.isUserUnlocked()) {
                KeyguardBouncer keyguardBouncer = KeyguardBouncer.this;
                if (keyguardBouncer.mKeyguardView != null && (viewGroup = keyguardBouncer.mRoot) != null && viewGroup.getVisibility() == 0) {
                    OpLsState.getInstance().getPhoneStatusBar().instantCollapseNotificationPanel();
                    KeyguardBouncer.this.mKeyguardView.onResume();
                }
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onDevicePolicyManagerStateChanged() {
            KeyguardHostView keyguardHostView;
            super.onDevicePolicyManagerStateChanged();
            if (!KeyguardBouncer.this.mKeyguardUpdateMonitor.isKeyguardDone() && (keyguardHostView = KeyguardBouncer.this.mKeyguardView) != null && keyguardHostView.getCurrentSecurityMode() != KeyguardSecurityModel.SecurityMode.Invalid && KeyguardBouncer.this.mKeyguardView.getCurrentSecurityMode() != KeyguardSecurityModel.SecurityMode.None && KeyguardBouncer.this.mKeyguardView.getSecurityMode() == KeyguardSecurityModel.SecurityMode.None) {
                Log.d("KeyguardBouncer", "onDevicePolicyManagerStateChanged: Retail mode clear password");
                KeyguardBouncer.this.showPrimarySecurityScreen();
            }
        }
    };

    public interface BouncerExpansionCallback {
        void onFullyHidden();

        void onFullyShown();

        void onStartingToHide();

        void onStartingToShow();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$KeyguardBouncer() {
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        if (keyguardHostView != null) {
            keyguardHostView.resetSecurityContainer();
        }
    }

    public KeyguardBouncer(Context context, ViewMediatorCallback viewMediatorCallback, LockPatternUtils lockPatternUtils, ViewGroup viewGroup, DismissCallbackRegistry dismissCallbackRegistry, FalsingManager falsingManager, BouncerExpansionCallback bouncerExpansionCallback, KeyguardStateController keyguardStateController, KeyguardUpdateMonitor keyguardUpdateMonitor, KeyguardBypassController keyguardBypassController, Handler handler) {
        this.mContext = context;
        this.mCallback = viewMediatorCallback;
        this.mLockPatternUtils = lockPatternUtils;
        this.mContainer = viewGroup;
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        this.mFalsingManager = falsingManager;
        this.mDismissCallbackRegistry = dismissCallbackRegistry;
        this.mExpansionCallback = bouncerExpansionCallback;
        this.mHandler = handler;
        this.mKeyguardStateController = keyguardStateController;
        keyguardUpdateMonitor.registerCallback(this.mUpdateMonitorCallback);
        this.mKeyguardBypassController = keyguardBypassController;
    }

    public void show(boolean z) {
        show(z, true);
    }

    public void show(boolean z, boolean z2) {
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        if (currentUser != 0 || !UserManager.isSplitSystemUser()) {
            ensureView();
            this.mIsScrimmed = z2;
            if (z2) {
                setExpansion(0.0f);
            }
            if (z) {
                showPrimarySecurityScreen();
            }
            if (this.mRoot.getVisibility() == 0 || this.mShowingSoon) {
                Log.d("KeyguardBouncer", "show already or showing soon. " + this.mRoot.getVisibility() + " " + this.mShowingSoon);
                return;
            }
            int currentUser2 = KeyguardUpdateMonitor.getCurrentUser();
            boolean z3 = false;
            if (!(UserManager.isSplitSystemUser() && currentUser2 == 0) && currentUser2 == currentUser) {
                z3 = true;
            }
            if (!z3 || !this.mKeyguardView.dismiss(currentUser2)) {
                if (!z3) {
                    Log.w("KeyguardBouncer", "User can't dismiss keyguard: " + currentUser2 + " != " + currentUser);
                }
                this.mShowingSoon = true;
                DejankUtils.removeCallbacks(this.mResetRunnable);
                if (!this.mKeyguardStateController.isFaceAuthEnabled() || needsFullscreenBouncer() || this.mKeyguardUpdateMonitor.userNeedsStrongAuth() || this.mKeyguardBypassController.getBypassEnabled()) {
                    DejankUtils.postAfterTraversal(this.mShowRunnable);
                } else {
                    this.mHandler.postDelayed(this.mShowRunnable, 1200);
                }
                this.mCallback.onBouncerVisiblityChanged(true);
                this.mExpansionCallback.onStartingToShow();
                return;
            }
            Log.d("KeyguardBouncer", "dismissing keyguard");
            return;
        }
        Log.d("KeyguardBouncer", "In split system user mode, we never unlock system user.");
    }

    public boolean isScrimmed() {
        return this.mIsScrimmed;
    }

    private void onFullyShown() {
        this.mFalsingManager.onBouncerShown();
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        if (keyguardHostView == null) {
            Log.wtf("KeyguardBouncer", "onFullyShown when view was null");
            return;
        }
        keyguardHostView.onResume();
        ViewGroup viewGroup = this.mRoot;
        if (viewGroup != null) {
            viewGroup.announceForAccessibility(this.mKeyguardView.getAccessibilityTitleForCurrentMode());
        }
    }

    private void onFullyHidden() {
        KeyguardSecurityView currentSecurityView;
        cancelShowRunnable();
        if (this.mRoot != null) {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("KeyguardBouncer", "onFullyHidden: root set to invisible");
            }
            this.mRoot.setVisibility(4);
        }
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        if (!(keyguardHostView == null || this.mIsAnimatingAway || (currentSecurityView = keyguardHostView.getCurrentSecurityView()) == null)) {
            currentSecurityView.onHidden();
        }
        this.mFalsingManager.onBouncerHidden();
        DejankUtils.postAfterTraversal(this.mResetRunnable);
    }

    public void showPromptReason(int i) {
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        if (keyguardHostView != null) {
            keyguardHostView.showPromptReason(i);
        } else {
            Log.w("KeyguardBouncer", "Trying to show prompt reason on empty bouncer");
        }
    }

    public void showMessage(String str, ColorStateList colorStateList) {
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        if (keyguardHostView != null) {
            keyguardHostView.showMessage(str, colorStateList);
        } else {
            Log.w("KeyguardBouncer", "Trying to show message on empty bouncer");
        }
    }

    private void cancelShowRunnable() {
        DejankUtils.removeCallbacks(this.mShowRunnable);
        this.mHandler.removeCallbacks(this.mShowRunnable);
        if (Build.DEBUG_ONEPLUS) {
            Log.d("KeyguardBouncer", "cancelShowRunnable: mShowingSoon set to false, callers= " + Debug.getCallers(3));
        }
        this.mShowingSoon = false;
        this.mNoAnim = false;
    }

    public void showWithDismissAction(ActivityStarter.OnDismissAction onDismissAction, Runnable runnable) {
        ensureView();
        this.mKeyguardView.setOnDismissAction(onDismissAction, runnable);
        show(false);
    }

    public void hide(boolean z) {
        Log.i("KeyguardBouncer", "hide / destroyView:" + z + " isShowing:" + isShowing());
        if (isShowing()) {
            SysUiStatsLog.write(63, 1);
            this.mDismissCallbackRegistry.notifyDismissCancelled();
        }
        this.mIsScrimmed = false;
        this.mFalsingManager.onBouncerHidden();
        this.mCallback.onBouncerVisiblityChanged(false);
        cancelShowRunnable();
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        if (keyguardHostView != null) {
            keyguardHostView.cancelDismissAction();
            this.mKeyguardView.cleanUp();
        }
        this.mIsAnimatingAway = false;
        ViewGroup viewGroup = this.mRoot;
        if (viewGroup != null) {
            viewGroup.setVisibility(4);
            if (z) {
                this.mHandler.postDelayed(this.mRemoveViewRunnable, 50);
            }
        }
        KeyguardHostView keyguardHostView2 = this.mKeyguardView;
        if (keyguardHostView2 != null) {
            keyguardHostView2.resetFlipperY();
        }
    }

    public void startPreHideAnimation(Runnable runnable) {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("KeyguardBouncer", "startPreHideAnimation: mIsAnimatingAway set to true");
        }
        this.mIsAnimatingAway = true;
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        if (keyguardHostView != null) {
            keyguardHostView.startDisappearAnimation(runnable);
        } else if (runnable != null) {
            runnable.run();
        }
    }

    public void onScreenTurnedOff() {
        ViewGroup viewGroup;
        if (this.mKeyguardView != null && (viewGroup = this.mRoot) != null && viewGroup.getVisibility() == 0) {
            this.mKeyguardView.onPause();
        }
    }

    public boolean isShowing() {
        ViewGroup viewGroup;
        return (this.mShowingSoon || ((viewGroup = this.mRoot) != null && viewGroup.getVisibility() == 0)) && this.mExpansion == 0.0f && !isAnimatingAway();
    }

    public boolean inTransit() {
        if (!this.mShowingSoon) {
            float f = this.mExpansion;
            if (f == 1.0f || f == 0.0f) {
                return false;
            }
        }
        return true;
    }

    public boolean isAnimatingAway() {
        return this.mIsAnimatingAway;
    }

    public void prepare() {
        boolean z = this.mRoot != null;
        ensureView();
        if (z) {
            showPrimarySecurityScreen();
        }
        this.mBouncerPromptReason = this.mCallback.getBouncerPromptReason();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showPrimarySecurityScreen() {
        this.mKeyguardView.showPrimarySecurityScreen();
    }

    public void setExpansion(float f) {
        float f2 = this.mExpansion;
        this.mExpansion = f;
        if (Build.DEBUG_ONEPLUS) {
            Log.d("KeyguardBouncer", "setExpansion: (" + f2 + " -> " + f + "), callers= " + Debug.getCallers(3));
        }
        if (this.mKeyguardView != null && !this.mIsAnimatingAway) {
            this.mKeyguardView.setAlpha(MathUtils.constrain(MathUtils.map(0.95f, 1.0f, 1.0f, 0.0f, f), 0.0f, 1.0f));
            float f3 = ((double) f) <= 0.1d ? 1.0f - (20.0f * f) : 0.0f;
            ViewGroup viewGroup = this.mRoot;
            if (viewGroup != null) {
                viewGroup.setAlpha(f3);
            }
            KeyguardHostView keyguardHostView = this.mKeyguardView;
            keyguardHostView.setTranslationY(((float) keyguardHostView.getHeight()) * f);
        }
        int i = (f > 0.0f ? 1 : (f == 0.0f ? 0 : -1));
        if (i == 0 && f2 != 0.0f) {
            onFullyShown();
            this.mExpansionCallback.onFullyShown();
        } else if (f == 1.0f && f2 != 1.0f) {
            onFullyHidden();
            this.mExpansionCallback.onFullyHidden();
        } else if (i != 0 && f2 == 0.0f) {
            this.mExpansionCallback.onStartingToHide();
            KeyguardHostView keyguardHostView2 = this.mKeyguardView;
            if (keyguardHostView2 != null) {
                keyguardHostView2.onStartingToHide();
            }
        }
    }

    public boolean willDismissWithAction() {
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        return keyguardHostView != null && keyguardHostView.hasDismissActions();
    }

    /* access modifiers changed from: protected */
    public void ensureView() {
        boolean hasCallbacks = this.mHandler.hasCallbacks(this.mRemoveViewRunnable);
        if (this.mRoot == null || hasCallbacks) {
            inflateView();
        }
    }

    /* access modifiers changed from: protected */
    public void inflateView() {
        removeView();
        this.mHandler.removeCallbacks(this.mRemoveViewRunnable);
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(this.mContext).inflate(C0011R$layout.keyguard_bouncer, (ViewGroup) null);
        this.mRoot = viewGroup;
        KeyguardHostView keyguardHostView = (KeyguardHostView) viewGroup.findViewById(C0008R$id.keyguard_host_view);
        this.mKeyguardView = keyguardHostView;
        keyguardHostView.setLockPatternUtils(this.mLockPatternUtils);
        this.mKeyguardView.setViewMediatorCallback(this.mCallback);
        this.mKeyguardView.onHideNavBar(this.mHideNavBar);
        int childCount = this.mContainer.getChildCount();
        int i = childCount - 1;
        int i2 = 0;
        while (true) {
            if (i2 >= childCount) {
                break;
            } else if (this.mContainer.getChildAt(i2).getId() == C0008R$id.op_utils_container) {
                i = i2;
                break;
            } else {
                i2++;
            }
        }
        this.mContainer.addView(this.mRoot, i);
        this.mStatusBarHeight = this.mRoot.getResources().getDimensionPixelOffset(C0005R$dimen.status_bar_height);
        if (Build.DEBUG_ONEPLUS) {
            Log.d("KeyguardBouncer", "inflateView: root set to invisible, callers= " + Debug.getCallers(3));
        }
        this.mRoot.setVisibility(4);
        WindowInsets rootWindowInsets = this.mRoot.getRootWindowInsets();
        if (rootWindowInsets != null) {
            this.mRoot.dispatchApplyWindowInsets(rootWindowInsets);
        }
    }

    /* access modifiers changed from: protected */
    public void removeView() {
        ViewGroup viewGroup;
        ViewGroup viewGroup2 = this.mRoot;
        if (viewGroup2 != null && viewGroup2.getParent() == (viewGroup = this.mContainer)) {
            viewGroup.removeView(this.mRoot);
            this.mRoot = null;
        }
    }

    public boolean needsFullscreenBouncer() {
        KeyguardSecurityModel.SecurityMode securityMode = ((KeyguardSecurityModel) Dependency.get(KeyguardSecurityModel.class)).getSecurityMode(KeyguardUpdateMonitor.getCurrentUser());
        return securityMode == KeyguardSecurityModel.SecurityMode.SimPin || securityMode == KeyguardSecurityModel.SecurityMode.SimPuk;
    }

    public boolean isFullscreenBouncer() {
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        if (keyguardHostView == null) {
            return false;
        }
        KeyguardSecurityModel.SecurityMode currentSecurityMode = keyguardHostView.getCurrentSecurityMode();
        if (currentSecurityMode == KeyguardSecurityModel.SecurityMode.SimPin || currentSecurityMode == KeyguardSecurityModel.SecurityMode.SimPuk) {
            return true;
        }
        return false;
    }

    public boolean isSecure() {
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        return keyguardHostView == null || keyguardHostView.getSecurityMode() != KeyguardSecurityModel.SecurityMode.None;
    }

    public boolean shouldDismissOnMenuPressed() {
        return this.mKeyguardView.shouldEnableMenuKey();
    }

    public boolean interceptMediaKey(KeyEvent keyEvent) {
        ensureView();
        return this.mKeyguardView.interceptMediaKey(keyEvent);
    }

    public void notifyKeyguardAuthenticated(boolean z) {
        ensureView();
        this.mKeyguardView.finish(z, KeyguardUpdateMonitor.getCurrentUser());
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println("KeyguardBouncer");
        printWriter.println("  isShowing(): " + isShowing());
        printWriter.println("  mStatusBarHeight: " + this.mStatusBarHeight);
        printWriter.println("  mExpansion: " + this.mExpansion);
        printWriter.println("  mKeyguardView; " + this.mKeyguardView);
        printWriter.println("  mShowingSoon: " + this.mShowingSoon);
        printWriter.println("  mBouncerPromptReason: " + this.mBouncerPromptReason);
        printWriter.println("  mIsAnimatingAway: " + this.mIsAnimatingAway);
        StringBuilder sb = new StringBuilder();
        sb.append("  mKeyguardView alpha: ");
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        float f = 0.0f;
        sb.append(keyguardHostView != null ? keyguardHostView.getAlpha() : 0.0f);
        printWriter.println(sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append("  mRoot alpha: ");
        ViewGroup viewGroup = this.mRoot;
        if (viewGroup != null) {
            f = viewGroup.getAlpha();
        }
        sb2.append(f);
        printWriter.println(sb2.toString());
    }

    public boolean isCheckingPassword() {
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        if (keyguardHostView != null) {
            return keyguardHostView.isCheckingPassword();
        }
        return false;
    }

    public boolean isUserUnlocked() {
        if (!OpKeyguardUpdateMonitor.IS_SUPPORT_BOOT_TO_ENTER_BOUNCER || !this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser())) {
            return true;
        }
        return KeyguardUpdateMonitor.getInstance(this.mContext).isUserUnlocked();
    }

    public void onHideNavBar(boolean z) {
        if (this.mHideNavBar != z) {
            KeyguardHostView keyguardHostView = this.mKeyguardView;
            if (keyguardHostView != null) {
                keyguardHostView.onHideNavBar(z);
            }
            this.mHideNavBar = z;
        }
    }

    public void updateBouncerPromptReason() {
        ViewMediatorCallback viewMediatorCallback = this.mCallback;
        if (viewMediatorCallback != null) {
            this.mBouncerPromptReason = viewMediatorCallback.getBouncerPromptReason();
        }
    }

    public void forceHide() {
        if (this.mRoot != null) {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("KeyguardBouncer", "forceHide: root set to invisible, callers= " + Debug.getCallers(3));
            }
            this.mRoot.setVisibility(4);
        }
    }
}
