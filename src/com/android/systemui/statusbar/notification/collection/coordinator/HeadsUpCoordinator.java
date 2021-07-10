package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.notification.collection.ListEntry;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifPromoter;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifSection;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender;
import com.android.systemui.statusbar.notification.interruption.HeadsUpController;
import com.android.systemui.statusbar.notification.interruption.HeadsUpViewBinder;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import com.oneplus.plugin.OpLsState;
import java.util.Objects;
public class HeadsUpCoordinator implements Coordinator {
    private NotificationEntry mCurrentHun;
    private NotifLifetimeExtender.OnEndLifetimeExtensionCallback mEndLifetimeExtension;
    private final HeadsUpManager mHeadsUpManager;
    private final HeadsUpViewBinder mHeadsUpViewBinder;
    private final NotifLifetimeExtender mLifetimeExtender = new NotifLifetimeExtender() { // from class: com.android.systemui.statusbar.notification.collection.coordinator.HeadsUpCoordinator.2
        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender
        public String getName() {
            return "HeadsUpCoordinator";
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender
        public void setCallback(NotifLifetimeExtender.OnEndLifetimeExtensionCallback onEndLifetimeExtensionCallback) {
            HeadsUpCoordinator.this.mEndLifetimeExtension = onEndLifetimeExtensionCallback;
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender
        public boolean shouldExtendLifetime(NotificationEntry notificationEntry, int i) {
            boolean isCurrentlyShowingHun = HeadsUpCoordinator.this.isCurrentlyShowingHun(notificationEntry);
            if (isCurrentlyShowingHun) {
                HeadsUpCoordinator.this.mNotifExtendingLifetime = notificationEntry;
            }
            return isCurrentlyShowingHun;
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender
        public void cancelLifetimeExtension(NotificationEntry notificationEntry) {
            if (Objects.equals(HeadsUpCoordinator.this.mNotifExtendingLifetime, notificationEntry)) {
                HeadsUpCoordinator.this.mNotifExtendingLifetime = null;
            }
        }
    };
    private final NotifCollectionListener mNotifCollectionListener = new NotifCollectionListener() { // from class: com.android.systemui.statusbar.notification.collection.coordinator.HeadsUpCoordinator.1
        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryAdded(NotificationEntry notificationEntry) {
            if (HeadsUpCoordinator.this.mNotificationInterruptStateProvider.shouldHeadsUp(notificationEntry)) {
                HeadsUpCoordinator.this.mHeadsUpViewBinder.bindHeadsUpView(notificationEntry, new NotifBindPipeline.BindCallback() { // from class: com.android.systemui.statusbar.notification.collection.coordinator.-$$Lambda$HeadsUpCoordinator$1$ifvJDWb4RvXhnAbnBVrfGOQJUGM
                    @Override // com.android.systemui.statusbar.notification.row.NotifBindPipeline.BindCallback
                    public final void onBindFinished(NotificationEntry notificationEntry2) {
                        HeadsUpCoordinator.this.onHeadsUpViewBound(notificationEntry2);
                    }
                });
            }
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryUpdated(NotificationEntry notificationEntry) {
            boolean alertAgain = HeadsUpController.alertAgain(notificationEntry, notificationEntry.getSbn().getNotification());
            boolean shouldHeadsUp = HeadsUpCoordinator.this.mNotificationInterruptStateProvider.shouldHeadsUp(notificationEntry);
            if ((OpLsState.getInstance().getPhoneStatusBar().isDozing() || OpLsState.getInstance().getPhoneStatusBar().isDozingCustom()) ? false : HeadsUpCoordinator.this.mHeadsUpManager.isAlerting(notificationEntry.getKey())) {
                if (shouldHeadsUp) {
                    HeadsUpCoordinator.this.mHeadsUpManager.updateNotification(notificationEntry.getKey(), alertAgain);
                } else if (!HeadsUpCoordinator.this.mHeadsUpManager.isEntryAutoHeadsUpped(notificationEntry.getKey())) {
                    HeadsUpCoordinator.this.mHeadsUpManager.removeNotification(notificationEntry.getKey(), false);
                }
            } else if (shouldHeadsUp && alertAgain) {
                HeadsUpCoordinator.this.mHeadsUpViewBinder.bindHeadsUpView(notificationEntry, new NotifBindPipeline.BindCallback() { // from class: com.android.systemui.statusbar.notification.collection.coordinator.-$$Lambda$HeadsUpCoordinator$1$7Uuyx_f2KfHu3jWNg-GxLQ4f6Hw
                    @Override // com.android.systemui.statusbar.notification.row.NotifBindPipeline.BindCallback
                    public final void onBindFinished(NotificationEntry notificationEntry2) {
                        HeadsUpCoordinator.this.onHeadsUpViewBound(notificationEntry2);
                    }
                });
            }
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryRemoved(NotificationEntry notificationEntry, int i) {
            String key = notificationEntry.getKey();
            if (HeadsUpCoordinator.this.mHeadsUpManager.isAlerting(key)) {
                HeadsUpCoordinator.this.mHeadsUpManager.removeNotification(notificationEntry.getKey(), HeadsUpCoordinator.this.mRemoteInputManager.getController().isSpinning(key) && !NotificationRemoteInputManager.FORCE_REMOTE_INPUT_HISTORY);
            }
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryCleanUp(NotificationEntry notificationEntry) {
            HeadsUpCoordinator.this.mHeadsUpViewBinder.abortBindCallback(notificationEntry);
        }
    };
    private NotificationEntry mNotifExtendingLifetime;
    private final NotifPromoter mNotifPromoter = new NotifPromoter("HeadsUpCoordinator") { // from class: com.android.systemui.statusbar.notification.collection.coordinator.HeadsUpCoordinator.3
        @Override // com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifPromoter
        public boolean shouldPromoteToTopLevel(NotificationEntry notificationEntry) {
            return HeadsUpCoordinator.this.isCurrentlyShowingHun(notificationEntry);
        }
    };
    private final NotifSection mNotifSection = new NotifSection("HeadsUpCoordinator") { // from class: com.android.systemui.statusbar.notification.collection.coordinator.HeadsUpCoordinator.4
        @Override // com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifSection
        public boolean isInSection(ListEntry listEntry) {
            return HeadsUpCoordinator.this.isCurrentlyShowingHun(listEntry);
        }
    };
    private final NotificationInterruptStateProvider mNotificationInterruptStateProvider;
    private final OnHeadsUpChangedListener mOnHeadsUpChangedListener = new OnHeadsUpChangedListener() { // from class: com.android.systemui.statusbar.notification.collection.coordinator.HeadsUpCoordinator.5
        @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
        public void onHeadsUpStateChanged(NotificationEntry notificationEntry, boolean z) {
            NotificationEntry topEntry = HeadsUpCoordinator.this.mHeadsUpManager.getTopEntry();
            if (!Objects.equals(HeadsUpCoordinator.this.mCurrentHun, topEntry)) {
                HeadsUpCoordinator.this.endNotifLifetimeExtension();
                HeadsUpCoordinator.this.mCurrentHun = topEntry;
                HeadsUpCoordinator.this.mNotifPromoter.invalidateList();
                HeadsUpCoordinator.this.mNotifSection.invalidateList();
            }
            if (!z) {
                HeadsUpCoordinator.this.mHeadsUpViewBinder.unbindHeadsUpView(notificationEntry);
            }
        }
    };
    private final NotificationRemoteInputManager mRemoteInputManager;

    public HeadsUpCoordinator(HeadsUpManager headsUpManager, HeadsUpViewBinder headsUpViewBinder, NotificationInterruptStateProvider notificationInterruptStateProvider, NotificationRemoteInputManager notificationRemoteInputManager) {
        this.mHeadsUpManager = headsUpManager;
        this.mHeadsUpViewBinder = headsUpViewBinder;
        this.mNotificationInterruptStateProvider = notificationInterruptStateProvider;
        this.mRemoteInputManager = notificationRemoteInputManager;
    }

    @Override // com.android.systemui.statusbar.notification.collection.coordinator.Coordinator
    public void attach(NotifPipeline notifPipeline) {
        this.mHeadsUpManager.addListener(this.mOnHeadsUpChangedListener);
        notifPipeline.addCollectionListener(this.mNotifCollectionListener);
        notifPipeline.addPromoter(this.mNotifPromoter);
        notifPipeline.addNotificationLifetimeExtender(this.mLifetimeExtender);
    }

    @Override // com.android.systemui.statusbar.notification.collection.coordinator.Coordinator
    public NotifSection getSection() {
        return this.mNotifSection;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void onHeadsUpViewBound(NotificationEntry notificationEntry) {
        this.mHeadsUpManager.showNotification(notificationEntry);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isCurrentlyShowingHun(ListEntry listEntry) {
        return this.mCurrentHun == listEntry.getRepresentativeEntry();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void endNotifLifetimeExtension() {
        NotificationEntry notificationEntry = this.mNotifExtendingLifetime;
        if (notificationEntry != null) {
            this.mEndLifetimeExtension.onEndLifetimeExtension(this.mLifetimeExtender, notificationEntry);
            this.mNotifExtendingLifetime = null;
        }
    }
}
