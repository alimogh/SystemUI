package com.android.systemui.statusbar.phone;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.LinearLayout;
import com.android.systemui.C0003R$bool;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.EncryptionHelper;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.statusbar.phone.OpCollapsedStatusBarFragment;
import com.oneplus.util.OpUtils;
public class CollapsedStatusBarFragment extends OpCollapsedStatusBarFragment implements CommandQueue.Callbacks, StatusBarStateController.StateListener {
    private View mCenteredIconArea;
    private View mClockView;
    private CommandQueue mCommandQueue;
    private StatusBarIconController.DarkIconManager mDarkIconManager;
    private int mDisabled1;
    private KeyguardStateController mKeyguardStateController;
    private NetworkController mNetworkController;
    private View mNotificationIconAreaInner;
    private View mOperatorNameFrame;
    private NetworkController.SignalCallback mSignalCallback = new NetworkController.SignalCallback() { // from class: com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.1
        @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setIsAirplaneMode(NetworkController.IconState iconState) {
            CollapsedStatusBarFragment.this.mCommandQueue.recomputeDisableFlags(CollapsedStatusBarFragment.this.getContext().getDisplayId(), true);
        }
    };
    private PhoneStatusBarView mStatusBar;
    private StatusBar mStatusBarComponent;
    private StatusBarStateController mStatusBarStateController;
    private LinearLayout mSystemIconArea;

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
    }

    @Override // android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mKeyguardStateController = (KeyguardStateController) Dependency.get(KeyguardStateController.class);
        this.mNetworkController = (NetworkController) Dependency.get(NetworkController.class);
        this.mStatusBarStateController = (StatusBarStateController) Dependency.get(StatusBarStateController.class);
        this.mStatusBarComponent = (StatusBar) Dependency.get(StatusBar.class);
        this.mCommandQueue = (CommandQueue) Dependency.get(CommandQueue.class);
        onCreateInternal();
    }

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(C0011R$layout.status_bar, viewGroup, false);
    }

    @Override // android.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.mStatusBar = (PhoneStatusBarView) view;
        if (bundle != null && bundle.containsKey("panel_state")) {
            this.mStatusBar.restoreHierarchyState(bundle.getSparseParcelableArray("panel_state"));
        }
        StatusBarIconController.DarkIconManager darkIconManager = new StatusBarIconController.DarkIconManager((LinearLayout) view.findViewById(C0008R$id.statusIcons), (CommandQueue) Dependency.get(CommandQueue.class));
        this.mDarkIconManager = darkIconManager;
        darkIconManager.setShouldLog(true);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mDarkIconManager);
        LinearLayout linearLayout = (LinearLayout) this.mStatusBar.findViewById(C0008R$id.system_icon_area);
        this.mSystemIconArea = linearLayout;
        adjustSystemIconAreaLayoutParams(linearLayout);
        this.mClockView = this.mStatusBar.findViewById(C0008R$id.clock);
        showSystemIconArea(false);
        showClock(false);
        initEmergencyCryptkeeperText();
        initOperatorName();
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).setTraceView(view.findViewById(C0008R$id.traceview));
    }

    @Override // android.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        SparseArray<? extends Parcelable> sparseArray = new SparseArray<>();
        this.mStatusBar.saveHierarchyState(sparseArray);
        bundle.putSparseParcelableArray("panel_state", sparseArray);
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpCollapsedStatusBarFragment, android.app.Fragment
    public void onResume() {
        super.onResume();
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
        this.mStatusBarStateController.addCallback(this);
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpCollapsedStatusBarFragment, android.app.Fragment
    public void onPause() {
        super.onPause();
        this.mCommandQueue.removeCallback((CommandQueue.Callbacks) this);
        this.mStatusBarStateController.removeCallback(this);
    }

    @Override // android.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mDarkIconManager);
        if (this.mNetworkController.hasEmergencyCryptKeeperText()) {
            this.mNetworkController.removeCallback(this.mSignalCallback);
        }
    }

    public void initNotificationIconArea(NotificationIconAreaController notificationIconAreaController) {
        ViewGroup viewGroup = (ViewGroup) this.mStatusBar.findViewById(C0008R$id.notification_icon_area);
        View notificationInnerAreaView = notificationIconAreaController.getNotificationInnerAreaView();
        this.mNotificationIconAreaInner = notificationInnerAreaView;
        if (notificationInnerAreaView.getParent() != null) {
            ((ViewGroup) this.mNotificationIconAreaInner.getParent()).removeView(this.mNotificationIconAreaInner);
        }
        viewGroup.addView(this.mNotificationIconAreaInner);
        ViewGroup viewGroup2 = (ViewGroup) this.mStatusBar.findViewById(C0008R$id.centered_icon_area);
        View centeredNotificationAreaView = notificationIconAreaController.getCenteredNotificationAreaView();
        this.mCenteredIconArea = centeredNotificationAreaView;
        if (centeredNotificationAreaView.getParent() != null) {
            ((ViewGroup) this.mCenteredIconArea.getParent()).removeView(this.mCenteredIconArea);
        }
        viewGroup2.addView(this.mCenteredIconArea);
        showNotificationIconArea(false);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) {
        if (i != getContext().getDisplayId()) {
            Log.d("CollapsedStatusBarFragment", "disable fail - displayId " + i + " getDisplayId " + getContext().getDisplayId());
            return;
        }
        this.mState1 = i2;
        int adjustDisableFlags = adjustDisableFlags(i2);
        int i4 = this.mDisabled1 ^ adjustDisableFlags;
        this.mDisabled1 = adjustDisableFlags;
        if ((i4 & 1048576) != 0) {
            if ((1048576 & adjustDisableFlags) != 0) {
                hideSystemIconArea(z);
                hideOperatorName(z);
            } else {
                showSystemIconArea(z);
                showOperatorName(z);
            }
        }
        if ((i4 & 131072) != 0) {
            if ((131072 & adjustDisableFlags) != 0) {
                hideNotificationIconArea(z);
            } else {
                showNotificationIconArea(z);
            }
        }
        if ((i4 & 8388608) != 0 || this.mClockView.getVisibility() != clockHiddenMode()) {
            if ((adjustDisableFlags & 8388608) != 0) {
                hideClock(z);
            } else {
                showClock(z);
            }
        }
    }

    /* access modifiers changed from: protected */
    public int adjustDisableFlags(int i) {
        boolean headsUpShouldBeVisible = this.mStatusBarComponent.headsUpShouldBeVisible();
        if (headsUpShouldBeVisible) {
            i |= 8388608;
        }
        boolean z = OpLsState.getInstance().getBiometricUnlockController().getMode() == 2;
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.d("CollapsedStatusBarFragment", "adjustDisableFlags isLaunchTransitionFadingAway " + this.mKeyguardStateController.isLaunchTransitionFadingAway() + " isKeyguardFadingAway " + this.mKeyguardStateController.isKeyguardFadingAway() + " shouldHideNotificationIcons " + shouldHideNotificationIcons() + " getState " + this.mStatusBarStateController.getState() + " headsUpVisible " + headsUpShouldBeVisible + " wakeUpFpUnlock " + z);
        }
        if (!this.mKeyguardStateController.isLaunchTransitionFadingAway() && !this.mKeyguardStateController.isKeyguardFadingAway() && shouldHideNotificationIcons() && ((this.mStatusBarStateController.getState() != 1 || !headsUpShouldBeVisible) && !z)) {
            i = i | 131072 | 1048576 | 8388608;
        }
        NetworkController networkController = this.mNetworkController;
        if (networkController != null && EncryptionHelper.IS_DATA_ENCRYPTED) {
            if (networkController.hasEmergencyCryptKeeperText()) {
                i |= 131072;
            }
            if (!this.mNetworkController.isRadioOn()) {
                i |= 1048576;
            }
        }
        return (!this.mStatusBarStateController.isDozing() || !this.mStatusBarComponent.getPanelController().hasCustomClock()) ? i : i | 9437184;
    }

    private boolean shouldHideNotificationIcons() {
        if (shouldHideNotificationIconsInternal()) {
            return true;
        }
        if ((this.mStatusBar.isClosed() || !this.mStatusBarComponent.hideStatusBarIconsWhenExpanded()) && !this.mStatusBarComponent.hideStatusBarIconsForBouncer()) {
            return false;
        }
        return true;
    }

    public void hideSystemIconArea(boolean z) {
        animateHide(this.mSystemIconArea, z);
    }

    public void showSystemIconArea(boolean z) {
        animateShow(this.mSystemIconArea, z);
    }

    public void hideClock(boolean z) {
        animateHiddenState(this.mClockView, clockHiddenMode(), z);
    }

    public void showClock(boolean z) {
        animateShow(this.mClockView, z);
    }

    private int clockHiddenMode() {
        return (this.mStatusBar.isClosed() || this.mKeyguardStateController.isShowing() || this.mStatusBarStateController.isDozing()) ? 8 : 4;
    }

    public void hideNotificationIconArea(boolean z) {
        animateHide(this.mNotificationIconAreaInner, z);
        animateHide(this.mCenteredIconArea, z);
    }

    public void showNotificationIconArea(boolean z) {
        animateShow(this.mNotificationIconAreaInner, z);
        animateShow(this.mCenteredIconArea, z);
    }

    public void hideOperatorName(boolean z) {
        View view = this.mOperatorNameFrame;
        if (view != null) {
            animateHide(view, z);
        }
    }

    public void showOperatorName(boolean z) {
        View view = this.mOperatorNameFrame;
        if (view != null) {
            animateShow(view, z);
        }
    }

    private void animateHiddenState(View view, int i, boolean z) {
        view.animate().cancel();
        if (!z) {
            view.setAlpha(0.0f);
            view.setVisibility(i);
            return;
        }
        view.animate().alpha(0.0f).setDuration(160).setStartDelay(0).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable(view, i) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$CollapsedStatusBarFragment$27RMKG7VU7GD3kVXbGdyl_3FVd4
            public final /* synthetic */ View f$0;
            public final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                CollapsedStatusBarFragment.lambda$animateHiddenState$0(this.f$0, this.f$1);
            }
        });
    }

    private void animateHide(View view, boolean z) {
        animateHiddenState(view, 4, z);
    }

    private void animateShow(View view, boolean z) {
        view.animate().cancel();
        view.setVisibility(0);
        if (!z) {
            view.setAlpha(1.0f);
            return;
        }
        view.animate().alpha(1.0f).setDuration(320).setInterpolator(Interpolators.ALPHA_IN).setStartDelay(50).withEndAction(null);
        if (this.mKeyguardStateController.isKeyguardFadingAway()) {
            view.animate().setDuration(this.mKeyguardStateController.getKeyguardFadingAwayDuration()).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setStartDelay(this.mKeyguardStateController.getKeyguardFadingAwayDelay()).start();
        }
    }

    private void initEmergencyCryptkeeperText() {
        View findViewById = this.mStatusBar.findViewById(C0008R$id.emergency_cryptkeeper_text);
        if (this.mNetworkController.hasEmergencyCryptKeeperText()) {
            if (findViewById != null) {
                ((ViewStub) findViewById).inflate();
            }
            this.mNetworkController.addCallback(this.mSignalCallback);
        } else if (findViewById != null) {
            ((ViewGroup) findViewById.getParent()).removeView(findViewById);
        }
    }

    private void initOperatorName() {
        if (getResources().getBoolean(C0003R$bool.config_showOperatorNameInStatusBar)) {
            this.mOperatorNameFrame = ((ViewStub) this.mStatusBar.findViewById(C0008R$id.operator_name)).inflate();
        }
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
        int displayId = getContext().getDisplayId();
        int i = this.mDisabled1;
        disable(displayId, i, i, false);
    }
}
