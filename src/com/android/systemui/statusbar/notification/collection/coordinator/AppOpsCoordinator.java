package com.android.systemui.statusbar.notification.collection.coordinator;

import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import com.android.systemui.ForegroundServiceController;
import com.android.systemui.appops.AppOpsController;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.coordinator.AppOpsCoordinator;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.util.HashMap;
import java.util.Map;
public class AppOpsCoordinator implements Coordinator {
    private final NotifLifetimeExtender mForegroundLifetimeExtender = new NotifLifetimeExtender() { // from class: com.android.systemui.statusbar.notification.collection.coordinator.AppOpsCoordinator.2
        private NotifLifetimeExtender.OnEndLifetimeExtensionCallback mEndCallback;
        private Map<NotificationEntry, Runnable> mEndRunnables = new HashMap();

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender
        public String getName() {
            return "AppOpsCoordinator";
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender
        public void setCallback(NotifLifetimeExtender.OnEndLifetimeExtensionCallback onEndLifetimeExtensionCallback) {
            this.mEndCallback = onEndLifetimeExtensionCallback;
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender
        public boolean shouldExtendLifetime(NotificationEntry notificationEntry, int i) {
            boolean z = false;
            if ((notificationEntry.getSbn().getNotification().flags & 64) == 0) {
                return false;
            }
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - notificationEntry.getSbn().getPostTime() < 5000) {
                z = true;
            }
            if (z && !this.mEndRunnables.containsKey(notificationEntry)) {
                this.mEndRunnables.put(notificationEntry, AppOpsCoordinator.this.mMainExecutor.executeDelayed(new Runnable(notificationEntry) { // from class: com.android.systemui.statusbar.notification.collection.coordinator.-$$Lambda$AppOpsCoordinator$2$QL7e9QZrONYPm8OKjbOLeImENQ4
                    public final /* synthetic */ NotificationEntry f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        AppOpsCoordinator.AnonymousClass2.this.lambda$shouldExtendLifetime$0$AppOpsCoordinator$2(this.f$1);
                    }
                }, 5000 - (currentTimeMillis - notificationEntry.getSbn().getPostTime())));
            }
            return z;
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$shouldExtendLifetime$0 */
        public /* synthetic */ void lambda$shouldExtendLifetime$0$AppOpsCoordinator$2(NotificationEntry notificationEntry) {
            this.mEndRunnables.remove(notificationEntry);
            this.mEndCallback.onEndLifetimeExtension(AppOpsCoordinator.this.mForegroundLifetimeExtender, notificationEntry);
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender
        public void cancelLifetimeExtension(NotificationEntry notificationEntry) {
            Runnable remove = this.mEndRunnables.remove(notificationEntry);
            if (remove != null) {
                remove.run();
            }
        }
    };
    private final ForegroundServiceController mForegroundServiceController;
    private final DelayableExecutor mMainExecutor;
    private NotifCollectionListener mNotifCollectionListener = new NotifCollectionListener() { // from class: com.android.systemui.statusbar.notification.collection.coordinator.AppOpsCoordinator.3
        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryAdded(NotificationEntry notificationEntry) {
            tagAppOps(notificationEntry);
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryUpdated(NotificationEntry notificationEntry) {
            tagAppOps(notificationEntry);
        }

        private void tagAppOps(NotificationEntry notificationEntry) {
            StatusBarNotification sbn = notificationEntry.getSbn();
            ArraySet<Integer> appOps = AppOpsCoordinator.this.mForegroundServiceController.getAppOps(sbn.getUser().getIdentifier(), sbn.getPackageName());
            notificationEntry.mActiveAppOps.clear();
            if (appOps != null) {
                notificationEntry.mActiveAppOps.addAll((ArraySet<? extends Integer>) appOps);
            }
        }
    };
    private final NotifFilter mNotifFilter = new NotifFilter("AppOpsCoordinator") { // from class: com.android.systemui.statusbar.notification.collection.coordinator.AppOpsCoordinator.1
        @Override // com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter
        public boolean shouldFilterOut(NotificationEntry notificationEntry, long j) {
            String[] stringArray;
            StatusBarNotification sbn = notificationEntry.getSbn();
            if (AppOpsCoordinator.this.mForegroundServiceController.isDisclosureNotification(sbn) && !AppOpsCoordinator.this.mForegroundServiceController.isDisclosureNeededForUser(sbn.getUser().getIdentifier())) {
                return true;
            }
            if (!AppOpsCoordinator.this.mForegroundServiceController.isSystemAlertNotification(sbn) || (stringArray = sbn.getNotification().extras.getStringArray("android.foregroundApps")) == null || stringArray.length < 1 || AppOpsCoordinator.this.mForegroundServiceController.isSystemAlertWarningNeeded(sbn.getUser().getIdentifier(), stringArray[0])) {
                return false;
            }
            return true;
        }
    };
    private NotifPipeline mNotifPipeline;

    public AppOpsCoordinator(ForegroundServiceController foregroundServiceController, AppOpsController appOpsController, DelayableExecutor delayableExecutor) {
        this.mForegroundServiceController = foregroundServiceController;
        this.mMainExecutor = delayableExecutor;
    }

    @Override // com.android.systemui.statusbar.notification.collection.coordinator.Coordinator
    public void attach(NotifPipeline notifPipeline) {
        this.mNotifPipeline = notifPipeline;
        notifPipeline.addNotificationLifetimeExtender(this.mForegroundLifetimeExtender);
        this.mNotifPipeline.addCollectionListener(this.mNotifCollectionListener);
        this.mNotifPipeline.addPreGroupFilter(this.mNotifFilter);
    }
}
