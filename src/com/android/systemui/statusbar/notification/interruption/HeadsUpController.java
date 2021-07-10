package com.android.systemui.statusbar.notification.interruption;

import android.app.Notification;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.oneplus.notification.OpNotificationController;
import com.oneplus.plugin.OpLsState;
import java.util.Objects;
public class HeadsUpController {
    private NotifCollectionListener mCollectionListener = new NotifCollectionListener() { // from class: com.android.systemui.statusbar.notification.interruption.HeadsUpController.1
        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryAdded(final NotificationEntry notificationEntry) {
            if (HeadsUpController.this.shouldDelayInComingCall(notificationEntry)) {
                HeadsUpController.this.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.statusbar.notification.interruption.HeadsUpController.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (HeadsUpController.this.mInterruptStateProvider.shouldHeadsUp(notificationEntry)) {
                            HeadsUpController.this.mHeadsUpViewBinder.bindHeadsUpView(notificationEntry, new NotifBindPipeline.BindCallback() { // from class: com.android.systemui.statusbar.notification.interruption.-$$Lambda$HeadsUpController$1$1$BFH-ZgAVuDmDtc5cjDcODlD5CUM
                                @Override // com.android.systemui.statusbar.notification.row.NotifBindPipeline.BindCallback
                                public final void onBindFinished(NotificationEntry notificationEntry2) {
                                    HeadsUpController.this.showAlertingView(notificationEntry2);
                                }
                            });
                        }
                        HeadsUpController.this.mOpNotificationController.maybeShowSimpleHeadsUp(HeadsUpController.this.mInterruptStateProvider.shouldHeadsUpWithReason(notificationEntry), notificationEntry.getSbn());
                        HeadsUpController.this.mInterruptStateProvider.setIsContactsChanged(false);
                    }
                }, 1100);
                return;
            }
            if (HeadsUpController.this.mInterruptStateProvider.shouldHeadsUp(notificationEntry)) {
                HeadsUpController.this.mHeadsUpViewBinder.bindHeadsUpView(notificationEntry, new NotifBindPipeline.BindCallback() { // from class: com.android.systemui.statusbar.notification.interruption.-$$Lambda$HeadsUpController$1$KAqgh1j8KCQjqOCPlwm79iJxTXk
                    @Override // com.android.systemui.statusbar.notification.row.NotifBindPipeline.BindCallback
                    public final void onBindFinished(NotificationEntry notificationEntry2) {
                        HeadsUpController.this.showAlertingView(notificationEntry2);
                    }
                });
            }
            HeadsUpController.this.mOpNotificationController.maybeShowSimpleHeadsUp(HeadsUpController.this.mInterruptStateProvider.shouldHeadsUpWithReason(notificationEntry), notificationEntry.getSbn());
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryUpdated(final NotificationEntry notificationEntry) {
            if (HeadsUpController.this.shouldDelayInComingCall(notificationEntry)) {
                HeadsUpController.this.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.statusbar.notification.interruption.HeadsUpController.1.2
                    @Override // java.lang.Runnable
                    public void run() {
                        HeadsUpController.this.updateHunState(notificationEntry);
                        HeadsUpController.this.mOpNotificationController.maybeShowSimpleHeadsUp(HeadsUpController.this.mInterruptStateProvider.shouldHeadsUpWithReason(notificationEntry), notificationEntry.getSbn());
                    }
                }, 1100);
                return;
            }
            HeadsUpController.this.updateHunState(notificationEntry);
            HeadsUpController.this.mOpNotificationController.maybeShowSimpleHeadsUp(HeadsUpController.this.mInterruptStateProvider.shouldHeadsUpWithReason(notificationEntry), notificationEntry.getSbn());
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryRemoved(NotificationEntry notificationEntry, int i) {
            HeadsUpController.this.stopAlerting(notificationEntry);
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryCleanUp(NotificationEntry notificationEntry) {
            HeadsUpController.this.mHeadsUpViewBinder.abortBindCallback(notificationEntry);
        }
    };
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private final HeadsUpManager mHeadsUpManager;
    private final HeadsUpViewBinder mHeadsUpViewBinder;
    private final NotificationInterruptStateProvider mInterruptStateProvider;
    private final NotificationListener mNotificationListener;
    private OnHeadsUpChangedListener mOnHeadsUpChangedListener = new OnHeadsUpChangedListener() { // from class: com.android.systemui.statusbar.notification.interruption.HeadsUpController.2
        @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
        public void onHeadsUpStateChanged(NotificationEntry notificationEntry, boolean z) {
            if (!z && !notificationEntry.getRow().isRemoved()) {
                HeadsUpController.this.mHeadsUpViewBinder.unbindHeadsUpView(notificationEntry);
            }
        }
    };
    private final OpNotificationController mOpNotificationController = ((OpNotificationController) Dependency.get(OpNotificationController.class));
    private final NotificationRemoteInputManager mRemoteInputManager;
    private final StatusBarStateController mStatusBarStateController;
    private final VisualStabilityManager mVisualStabilityManager;

    HeadsUpController(HeadsUpViewBinder headsUpViewBinder, NotificationInterruptStateProvider notificationInterruptStateProvider, HeadsUpManager headsUpManager, NotificationRemoteInputManager notificationRemoteInputManager, StatusBarStateController statusBarStateController, VisualStabilityManager visualStabilityManager, NotificationListener notificationListener) {
        this.mHeadsUpViewBinder = headsUpViewBinder;
        this.mHeadsUpManager = headsUpManager;
        this.mInterruptStateProvider = notificationInterruptStateProvider;
        this.mRemoteInputManager = notificationRemoteInputManager;
        this.mStatusBarStateController = statusBarStateController;
        this.mVisualStabilityManager = visualStabilityManager;
        this.mNotificationListener = notificationListener;
    }

    public void attach(NotificationEntryManager notificationEntryManager, HeadsUpManager headsUpManager) {
        notificationEntryManager.addCollectionListener(this.mCollectionListener);
        headsUpManager.addListener(this.mOnHeadsUpChangedListener);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldDelayInComingCall(NotificationEntry notificationEntry) {
        return "phone_incoming_call".equals(notificationEntry.getSbn().getNotification().getChannelId()) && (((ZenModeController) Dependency.get(ZenModeController.class)).getZen() != 0) && this.mInterruptStateProvider.isContactsChanged();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void showAlertingView(NotificationEntry notificationEntry) {
        this.mHeadsUpManager.showNotification(notificationEntry);
        if (!this.mStatusBarStateController.isDozing()) {
            setNotificationShown(notificationEntry.getSbn());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateHunState(NotificationEntry notificationEntry) {
        boolean alertAgain = alertAgain(notificationEntry, notificationEntry.getSbn().getNotification());
        boolean shouldHeadsUp = this.mInterruptStateProvider.shouldHeadsUp(notificationEntry);
        if ((OpLsState.getInstance().getPhoneStatusBar().isDozing() || OpLsState.getInstance().getPhoneStatusBar().isDozingCustom()) ? false : this.mHeadsUpManager.isAlerting(notificationEntry.getKey())) {
            if (shouldHeadsUp) {
                this.mHeadsUpManager.updateNotification(notificationEntry.getKey(), alertAgain);
            } else if (!this.mHeadsUpManager.isEntryAutoHeadsUpped(notificationEntry.getKey())) {
                this.mHeadsUpManager.removeNotification(notificationEntry.getKey(), false);
            }
        } else if (shouldHeadsUp && alertAgain) {
            HeadsUpViewBinder headsUpViewBinder = this.mHeadsUpViewBinder;
            HeadsUpManager headsUpManager = this.mHeadsUpManager;
            Objects.requireNonNull(headsUpManager);
            headsUpViewBinder.bindHeadsUpView(notificationEntry, new NotifBindPipeline.BindCallback() { // from class: com.android.systemui.statusbar.notification.interruption.-$$Lambda$mD6sR9LpFDm1_8U141Um56czoN4
                @Override // com.android.systemui.statusbar.notification.row.NotifBindPipeline.BindCallback
                public final void onBindFinished(NotificationEntry notificationEntry2) {
                    HeadsUpManager.this.showNotification(notificationEntry2);
                }
            });
        }
    }

    private void setNotificationShown(StatusBarNotification statusBarNotification) {
        try {
            this.mNotificationListener.setNotificationsShown(new String[]{statusBarNotification.getKey()});
        } catch (RuntimeException e) {
            Log.d("HeadsUpBindController", "failed setNotificationsShown: ", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopAlerting(NotificationEntry notificationEntry) {
        String key = notificationEntry.getKey();
        if (this.mHeadsUpManager.isAlerting(key)) {
            this.mHeadsUpManager.removeNotification(key, (this.mRemoteInputManager.getController().isSpinning(key) && !NotificationRemoteInputManager.FORCE_REMOTE_INPUT_HISTORY) || !this.mVisualStabilityManager.isReorderingAllowed());
        }
    }

    public static boolean alertAgain(NotificationEntry notificationEntry, Notification notification) {
        return notificationEntry == null || !notificationEntry.hasInterrupted() || (notification.flags & 8) == 0;
    }
}
