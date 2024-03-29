package com.android.systemui.statusbar.phone;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.notification.StatusBarNotification;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.util.Log;
import android.util.Slog;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.widget.MessagingGroup;
import com.android.internal.widget.MessagingMessage;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0009R$integer;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.ForegroundServiceNotificationListener;
import com.android.systemui.InitController;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.NotificationViewHierarchyManager;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.notification.AboveShelfObserver;
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.DynamicPrivacyController;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptSuppressor;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotificationGutsManager;
import com.android.systemui.statusbar.notification.row.NotificationInfo;
import com.android.systemui.statusbar.notification.stack.NotificationListContainer;
import com.android.systemui.statusbar.phone.LockscreenGestureLogger;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.oneplus.notification.OpNotificationController;
import com.oneplus.util.OpUtils;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
public class StatusBarNotificationPresenter implements NotificationPresenter, ConfigurationController.ConfigurationListener, NotificationRowBinderImpl.BindRowCallback, CommandQueue.Callbacks {
    private final AboveShelfObserver mAboveShelfObserver;
    private final AccessibilityManager mAccessibilityManager;
    private final ActivityLaunchAnimator mActivityLaunchAnimator;
    private final ActivityStarter mActivityStarter = ((ActivityStarter) Dependency.get(ActivityStarter.class));
    private final IStatusBarService mBarService;
    private final NotificationInfo.CheckSaveListener mCheckSaveListener = new NotificationInfo.CheckSaveListener(this) { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationPresenter.3
    };
    private final CommandQueue mCommandQueue;
    private boolean mDispatchUiModeChangeOnUserSwitched;
    private final DozeScrimController mDozeScrimController;
    private final DynamicPrivacyController mDynamicPrivacyController;
    private final NotificationEntryManager mEntryManager = ((NotificationEntryManager) Dependency.get(NotificationEntryManager.class));
    private final NotificationGutsManager mGutsManager = ((NotificationGutsManager) Dependency.get(NotificationGutsManager.class));
    private final HeadsUpManagerPhone mHeadsUpManager;
    private final NotificationInterruptSuppressor mInterruptSuppressor = new NotificationInterruptSuppressor() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationPresenter.5
        @Override // com.android.systemui.statusbar.notification.interruption.NotificationInterruptSuppressor
        public String getName() {
            return "StatusBarNotificationPresenter";
        }

        @Override // com.android.systemui.statusbar.notification.interruption.NotificationInterruptSuppressor
        public boolean suppressAwakeHeadsUp(NotificationEntry notificationEntry) {
            StatusBarNotification sbn = notificationEntry.getSbn();
            if (StatusBarNotificationPresenter.this.mStatusBar.isOccluded()) {
                boolean z = StatusBarNotificationPresenter.this.mLockscreenUserManager.isLockscreenPublicMode(StatusBarNotificationPresenter.this.mLockscreenUserManager.getCurrentUserId()) || StatusBarNotificationPresenter.this.mLockscreenUserManager.isLockscreenPublicMode(sbn.getUserId());
                boolean needsRedaction = StatusBarNotificationPresenter.this.mLockscreenUserManager.needsRedaction(notificationEntry);
                if (z && needsRedaction) {
                    if (StatusBar.DEBUG || OpUtils.DEBUG_ONEPLUS) {
                        Log.d("StatusBarNotificationPresenter", "No heads up: lockscreen is in public mode or needs redaction");
                    }
                    return true;
                }
            }
            if (StatusBarNotificationPresenter.this.mCommandQueue.panelsEnabled() || StatusBarNotificationPresenter.this.mOpNotificationController.isPanelDisabledInBrickMode()) {
                if (sbn.getNotification().fullScreenIntent != null) {
                    if (StatusBarNotificationPresenter.this.mKeyguardStateController.isShowing() && !StatusBarNotificationPresenter.this.mStatusBar.isOccluded()) {
                        if (StatusBar.DEBUG) {
                            Log.d("StatusBarNotificationPresenter", "No heads up: entry has fullscreen intent on lockscreen " + sbn.getKey());
                        }
                        return true;
                    } else if (StatusBarNotificationPresenter.this.mAccessibilityManager.isTouchExplorationEnabled()) {
                        if (StatusBar.DEBUG || OpUtils.DEBUG_ONEPLUS) {
                            Log.d("StatusBarNotificationPresenter", "No heads up: accessible fullscreen: " + sbn.getKey());
                        }
                        return true;
                    }
                }
                return false;
            }
            if (StatusBar.DEBUG || OpUtils.DEBUG_ONEPLUS) {
                Log.d("StatusBarNotificationPresenter", "No heads up: disabled panel : " + sbn.getKey());
            }
            return true;
        }

        @Override // com.android.systemui.statusbar.notification.interruption.NotificationInterruptSuppressor
        public boolean suppressAwakeInterruptions(NotificationEntry notificationEntry) {
            return StatusBarNotificationPresenter.this.isDeviceInVrMode();
        }

        @Override // com.android.systemui.statusbar.notification.interruption.NotificationInterruptSuppressor
        public boolean suppressInterruptions(NotificationEntry notificationEntry) {
            return StatusBarNotificationPresenter.this.mStatusBar.areNotificationAlertsDisabled();
        }
    };
    private final KeyguardIndicationController mKeyguardIndicationController;
    private final KeyguardStateController mKeyguardStateController;
    private final LockscreenGestureLogger mLockscreenGestureLogger = ((LockscreenGestureLogger) Dependency.get(LockscreenGestureLogger.class));
    private final NotificationLockscreenUserManager mLockscreenUserManager = ((NotificationLockscreenUserManager) Dependency.get(NotificationLockscreenUserManager.class));
    private final int mMaxAllowedKeyguardNotifications;
    private int mMaxKeyguardNotifications;
    private final NotificationMediaManager mMediaManager = ((NotificationMediaManager) Dependency.get(NotificationMediaManager.class));
    private final NotificationPanelViewController mNotificationPanel;
    private final NotificationGutsManager.OnSettingsClickListener mOnSettingsClickListener = new NotificationGutsManager.OnSettingsClickListener() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationPresenter.4
        @Override // com.android.systemui.statusbar.notification.row.NotificationGutsManager.OnSettingsClickListener
        public void onSettingsClick(String str) {
            try {
                StatusBarNotificationPresenter.this.mBarService.onNotificationSettingsViewed(str);
            } catch (RemoteException unused) {
            }
        }
    };
    private final OpNotificationController mOpNotificationController = ((OpNotificationController) Dependency.get(OpNotificationController.class));
    private boolean mReinflateNotificationsOnUserSwitched;
    private final ScrimController mScrimController;
    private final ShadeController mShadeController;
    private final StatusBar mStatusBar;
    private final SysuiStatusBarStateController mStatusBarStateController = ((SysuiStatusBarStateController) Dependency.get(StatusBarStateController.class));
    private final NotificationViewHierarchyManager mViewHierarchyManager = ((NotificationViewHierarchyManager) Dependency.get(NotificationViewHierarchyManager.class));
    private final VisualStabilityManager mVisualStabilityManager = ((VisualStabilityManager) Dependency.get(VisualStabilityManager.class));
    protected boolean mVrMode;
    private final IVrStateCallbacks mVrStateCallbacks = new IVrStateCallbacks.Stub() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationPresenter.2
        public void onVrStateChanged(boolean z) {
            StatusBarNotificationPresenter.this.mVrMode = z;
        }
    };

    static /* synthetic */ boolean lambda$onExpandClicked$2() {
        return false;
    }

    public StatusBarNotificationPresenter(Context context, NotificationPanelViewController notificationPanelViewController, HeadsUpManagerPhone headsUpManagerPhone, NotificationShadeWindowView notificationShadeWindowView, ViewGroup viewGroup, DozeScrimController dozeScrimController, ScrimController scrimController, ActivityLaunchAnimator activityLaunchAnimator, DynamicPrivacyController dynamicPrivacyController, KeyguardStateController keyguardStateController, KeyguardIndicationController keyguardIndicationController, StatusBar statusBar, ShadeController shadeController, CommandQueue commandQueue, InitController initController, NotificationInterruptStateProvider notificationInterruptStateProvider) {
        this.mKeyguardStateController = keyguardStateController;
        this.mNotificationPanel = notificationPanelViewController;
        this.mHeadsUpManager = headsUpManagerPhone;
        this.mDynamicPrivacyController = dynamicPrivacyController;
        this.mKeyguardIndicationController = keyguardIndicationController;
        this.mStatusBar = statusBar;
        this.mShadeController = shadeController;
        this.mCommandQueue = commandQueue;
        AboveShelfObserver aboveShelfObserver = new AboveShelfObserver(viewGroup);
        this.mAboveShelfObserver = aboveShelfObserver;
        this.mActivityLaunchAnimator = activityLaunchAnimator;
        aboveShelfObserver.setListener((AboveShelfObserver.HasViewAboveShelfChangedListener) notificationShadeWindowView.findViewById(C0008R$id.notification_container_parent));
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService(AccessibilityManager.class);
        this.mDozeScrimController = dozeScrimController;
        this.mScrimController = scrimController;
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(KeyguardManager.class);
        this.mMaxAllowedKeyguardNotifications = context.getResources().getInteger(C0009R$integer.keyguard_max_notification_count);
        Log.i("StatusBarNotificationPresenter", "mMaxAllowedKeyguardNotifications:" + this.mMaxAllowedKeyguardNotifications);
        this.mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
        IVrManager asInterface = IVrManager.Stub.asInterface(ServiceManager.getService("vrmanager"));
        if (asInterface != null) {
            try {
                asInterface.registerListener(this.mVrStateCallbacks);
            } catch (RemoteException e) {
                Slog.e("StatusBarNotificationPresenter", "Failed to register VR mode state listener: " + e);
            }
        }
        NotificationRemoteInputManager notificationRemoteInputManager = (NotificationRemoteInputManager) Dependency.get(NotificationRemoteInputManager.class);
        notificationRemoteInputManager.setUpWithCallback((NotificationRemoteInputManager.Callback) Dependency.get(NotificationRemoteInputManager.Callback.class), this.mNotificationPanel.createRemoteInputDelegate());
        notificationRemoteInputManager.getController().addCallback((RemoteInputController.Callback) Dependency.get(NotificationShadeWindowController.class));
        initController.addPostInitTask(new Runnable((NotificationListContainer) viewGroup, notificationRemoteInputManager, notificationInterruptStateProvider) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationPresenter$RZolY06L4AtX2ZrvmGw-j2EoYcA
            public final /* synthetic */ NotificationListContainer f$1;
            public final /* synthetic */ NotificationRemoteInputManager f$2;
            public final /* synthetic */ NotificationInterruptStateProvider f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.lang.Runnable
            public final void run() {
                StatusBarNotificationPresenter.this.lambda$new$0$StatusBarNotificationPresenter(this.f$1, this.f$2, this.f$3);
            }
        });
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$StatusBarNotificationPresenter(NotificationListContainer notificationListContainer, NotificationRemoteInputManager notificationRemoteInputManager, NotificationInterruptStateProvider notificationInterruptStateProvider) {
        AnonymousClass1 r0 = new NotificationEntryListener() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationPresenter.1
            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onEntryRemoved(NotificationEntry notificationEntry, NotificationVisibility notificationVisibility, boolean z, int i) {
                StatusBarNotificationPresenter.this.onNotificationRemoved(notificationEntry.getKey(), notificationEntry.getSbn());
                if (z) {
                    StatusBarNotificationPresenter.this.maybeEndAmbientPulse();
                }
            }
        };
        this.mViewHierarchyManager.setUpWithPresenter(this, notificationListContainer);
        this.mEntryManager.setUpWithPresenter(this);
        this.mEntryManager.addNotificationEntryListener(r0);
        this.mEntryManager.addNotificationLifetimeExtender(this.mHeadsUpManager);
        this.mEntryManager.addNotificationLifetimeExtender(this.mGutsManager);
        this.mEntryManager.addNotificationLifetimeExtenders(notificationRemoteInputManager.getLifetimeExtenders());
        notificationInterruptStateProvider.addSuppressor(this.mInterruptSuppressor);
        this.mLockscreenUserManager.setUpWithPresenter(this);
        this.mMediaManager.setUpWithPresenter(this);
        this.mVisualStabilityManager.setUpWithPresenter(this);
        this.mGutsManager.setUpWithPresenter(this, notificationListContainer, this.mCheckSaveListener, this.mOnSettingsClickListener);
        Dependency.get(ForegroundServiceNotificationListener.class);
        onUserSwitched(this.mLockscreenUserManager.getCurrentUserId());
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        MessagingMessage.dropCache();
        MessagingGroup.dropCache();
        if (!((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isSwitchingUser()) {
            updateNotificationsOnDensityOrFontScaleChanged();
        } else {
            this.mReinflateNotificationsOnUserSwitched = true;
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onUiModeChanged() {
        if (!((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isSwitchingUser()) {
            updateNotificationOnUiModeChanged();
        } else {
            this.mDispatchUiModeChangeOnUserSwitched = true;
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onOverlayChanged() {
        onDensityOrFontScaleChanged();
    }

    private void updateNotificationOnUiModeChanged() {
        List<NotificationEntry> activeNotificationsForCurrentUser = this.mEntryManager.getActiveNotificationsForCurrentUser();
        for (int i = 0; i < activeNotificationsForCurrentUser.size(); i++) {
            ExpandableNotificationRow row = activeNotificationsForCurrentUser.get(i).getRow();
            if (row != null) {
                row.onUiModeChanged();
            }
        }
    }

    private void updateNotificationsOnDensityOrFontScaleChanged() {
        List<NotificationEntry> activeNotificationsForCurrentUser = this.mEntryManager.getActiveNotificationsForCurrentUser();
        for (int i = 0; i < activeNotificationsForCurrentUser.size(); i++) {
            NotificationEntry notificationEntry = activeNotificationsForCurrentUser.get(i);
            notificationEntry.onDensityOrFontScaleChanged();
            if (notificationEntry.areGutsExposed()) {
                this.mGutsManager.onDensityOrFontScaleChanged(notificationEntry);
            }
        }
    }

    @Override // com.android.systemui.statusbar.NotificationPresenter
    public boolean isCollapsing() {
        return this.mNotificationPanel.isCollapsing() || this.mActivityLaunchAnimator.isAnimationPending() || this.mActivityLaunchAnimator.isAnimationRunning();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void maybeEndAmbientPulse() {
        if (this.mNotificationPanel.hasPulsingNotifications() && !this.mHeadsUpManager.hasNotifications()) {
            this.mDozeScrimController.pulseOutNow();
        }
    }

    @Override // com.android.systemui.statusbar.NotificationPresenter
    /* renamed from: updateNotificationViews */
    public void lambda$updateNotificationViews$1(String str) {
        if (this.mScrimController != null) {
            if (isCollapsing()) {
                this.mShadeController.addPostCollapseAction(new Runnable(str) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationPresenter$Zv1tw7L-orY0AD_NGXbfZSHaht0
                    public final /* synthetic */ String f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        StatusBarNotificationPresenter.this.lambda$updateNotificationViews$1$StatusBarNotificationPresenter(this.f$1);
                    }
                });
                return;
            }
            this.mViewHierarchyManager.updateNotificationViews();
            this.mNotificationPanel.updateNotificationViews(str);
        }
    }

    public void onNotificationRemoved(String str, StatusBarNotification statusBarNotification) {
        if (StatusBar.SPEW) {
            Log.d("StatusBarNotificationPresenter", "removeNotification key=" + str + " old=" + statusBarNotification);
        }
        if (statusBarNotification != null && !hasActiveNotifications() && !this.mNotificationPanel.isTracking() && !this.mNotificationPanel.isQsExpanded()) {
            if (this.mStatusBarStateController.getState() == 0) {
                this.mCommandQueue.animateCollapsePanels();
            } else if (this.mStatusBarStateController.getState() == 2 && !isCollapsing()) {
                this.mStatusBarStateController.setState(1);
            }
        }
    }

    public boolean hasActiveNotifications() {
        return this.mEntryManager.hasActiveNotifications();
    }

    @Override // com.android.systemui.statusbar.NotificationPresenter
    public void onUserSwitched(int i) {
        this.mHeadsUpManager.setUser(i);
        this.mCommandQueue.animateCollapsePanels();
        if (this.mReinflateNotificationsOnUserSwitched) {
            updateNotificationsOnDensityOrFontScaleChanged();
            this.mReinflateNotificationsOnUserSwitched = false;
        }
        if (this.mDispatchUiModeChangeOnUserSwitched) {
            updateNotificationOnUiModeChanged();
            this.mDispatchUiModeChangeOnUserSwitched = false;
        }
        lambda$updateNotificationViews$1("user switched");
        this.mMediaManager.clearCurrentMediaNotification();
        this.mStatusBar.setLockscreenUser(i);
        updateMediaMetaData(true, false);
        this.mOpNotificationController.updateNotificationRule();
    }

    @Override // com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl.BindRowCallback
    public void onBindRow(ExpandableNotificationRow expandableNotificationRow) {
        expandableNotificationRow.setAboveShelfChangedListener(this.mAboveShelfObserver);
        KeyguardStateController keyguardStateController = this.mKeyguardStateController;
        Objects.requireNonNull(keyguardStateController);
        expandableNotificationRow.setSecureStateProvider(new BooleanSupplier() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$1SZvPIdbqVv78ZfFe1GTnc0G8WM
            @Override // java.util.function.BooleanSupplier
            public final boolean getAsBoolean() {
                return KeyguardStateController.this.canDismissLockScreen();
            }
        });
    }

    @Override // com.android.systemui.statusbar.NotificationPresenter
    public boolean isPresenterFullyCollapsed() {
        return this.mNotificationPanel.isFullyCollapsed();
    }

    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView.OnActivatedListener
    public void onActivated(ActivatableNotificationView activatableNotificationView) {
        onActivated();
        if (activatableNotificationView != null) {
            this.mNotificationPanel.setActivatedChild(activatableNotificationView);
        }
    }

    public void onActivated() {
        this.mLockscreenGestureLogger.write(192, 0, 0);
        this.mLockscreenGestureLogger.log(LockscreenGestureLogger.LockscreenUiEvent.LOCKSCREEN_NOTIFICATION_FALSE_TOUCH);
        this.mNotificationPanel.showTransientIndication(C0015R$string.notification_tap_again);
        ActivatableNotificationView activatedChild = this.mNotificationPanel.getActivatedChild();
        if (activatedChild != null) {
            activatedChild.makeInactive(true);
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView.OnActivatedListener
    public void onActivationReset(ActivatableNotificationView activatableNotificationView) {
        if (activatableNotificationView == this.mNotificationPanel.getActivatedChild()) {
            this.mNotificationPanel.setActivatedChild(null);
            this.mKeyguardIndicationController.hideTransientIndication();
        }
    }

    @Override // com.android.systemui.statusbar.NotificationPresenter
    public void updateMediaMetaData(boolean z, boolean z2) {
        this.mMediaManager.updateMediaMetaData(z, z2);
    }

    @Override // com.android.systemui.statusbar.NotificationPresenter
    public int getMaxNotificationsWhileLocked(boolean z) {
        if (!z) {
            return this.mMaxKeyguardNotifications;
        }
        int max = Math.max(1, this.mNotificationPanel.computeMaxKeyguardNotifications(this.mMaxAllowedKeyguardNotifications));
        this.mMaxKeyguardNotifications = max;
        return max;
    }

    @Override // com.android.systemui.statusbar.NotificationPresenter
    public void onUpdateRowStates() {
        this.mNotificationPanel.onUpdateRowStates();
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow.OnExpandClickListener
    public void onExpandClicked(NotificationEntry notificationEntry, boolean z) {
        this.mHeadsUpManager.setExpanded(notificationEntry, z);
        if (!z) {
            return;
        }
        if (this.mStatusBarStateController.getState() == 1) {
            this.mShadeController.goToLockedShade(notificationEntry.getRow());
        } else if (notificationEntry.isSensitive() && this.mDynamicPrivacyController.isInLockedDownShade()) {
            this.mStatusBarStateController.setLeaveOpenOnKeyguardHide(true);
            this.mActivityStarter.dismissKeyguardThenExecute($$Lambda$StatusBarNotificationPresenter$rp0KjJSj1GdZ6jXaKpka1OFVb7Q.INSTANCE, null, false);
        }
    }

    @Override // com.android.systemui.statusbar.NotificationPresenter
    public boolean isDeviceInVrMode() {
        return this.mVrMode;
    }
}
