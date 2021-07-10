package com.android.systemui.statusbar.notification.init;

import android.service.notification.SnoozeCriterion;
import android.service.notification.StatusBarNotification;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.notification.NotificationActivityStarter;
import com.android.systemui.statusbar.notification.NotificationClicker;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationListController;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.TargetSdkResolver;
import com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl;
import com.android.systemui.statusbar.notification.collection.init.NotifPipelineInitializer;
import com.android.systemui.statusbar.notification.interruption.HeadsUpController;
import com.android.systemui.statusbar.notification.interruption.HeadsUpViewBinder;
import com.android.systemui.statusbar.notification.row.NotifBindPipelineInitializer;
import com.android.systemui.statusbar.notification.stack.NotificationListContainer;
import com.android.systemui.statusbar.phone.NotificationGroupAlertTransferHelper;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.RemoteInputUriController;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Optional;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: NotificationsControllerImpl.kt */
public final class NotificationsControllerImpl implements NotificationsController {
    private final NotificationClicker.Builder clickerBuilder;
    private final DeviceProvisionedController deviceProvisionedController;
    private final NotificationEntryManager entryManager;
    private final FeatureFlags featureFlags;
    private final NotificationGroupAlertTransferHelper groupAlertTransferHelper;
    private final NotificationGroupManager groupManager;
    private final HeadsUpController headsUpController;
    private final HeadsUpManager headsUpManager;
    private final HeadsUpViewBinder headsUpViewBinder;
    private final Lazy<NotifPipelineInitializer> newNotifPipeline;
    private final NotifBindPipelineInitializer notifBindPipelineInitializer;
    private final Lazy<NotifPipeline> notifPipeline;
    private final NotificationListener notificationListener;
    private final NotificationRowBinderImpl notificationRowBinder;
    private final RemoteInputUriController remoteInputUriController;
    private final TargetSdkResolver targetSdkResolver;

    public NotificationsControllerImpl(@NotNull FeatureFlags featureFlags, @NotNull NotificationListener notificationListener, @NotNull NotificationEntryManager notificationEntryManager, @NotNull Lazy<NotifPipeline> lazy, @NotNull TargetSdkResolver targetSdkResolver, @NotNull Lazy<NotifPipelineInitializer> lazy2, @NotNull NotifBindPipelineInitializer notifBindPipelineInitializer, @NotNull DeviceProvisionedController deviceProvisionedController, @NotNull NotificationRowBinderImpl notificationRowBinderImpl, @NotNull RemoteInputUriController remoteInputUriController, @NotNull NotificationGroupManager notificationGroupManager, @NotNull NotificationGroupAlertTransferHelper notificationGroupAlertTransferHelper, @NotNull HeadsUpManager headsUpManager, @NotNull HeadsUpController headsUpController, @NotNull HeadsUpViewBinder headsUpViewBinder, @NotNull NotificationClicker.Builder builder) {
        Intrinsics.checkParameterIsNotNull(featureFlags, "featureFlags");
        Intrinsics.checkParameterIsNotNull(notificationListener, "notificationListener");
        Intrinsics.checkParameterIsNotNull(notificationEntryManager, "entryManager");
        Intrinsics.checkParameterIsNotNull(lazy, "notifPipeline");
        Intrinsics.checkParameterIsNotNull(targetSdkResolver, "targetSdkResolver");
        Intrinsics.checkParameterIsNotNull(lazy2, "newNotifPipeline");
        Intrinsics.checkParameterIsNotNull(notifBindPipelineInitializer, "notifBindPipelineInitializer");
        Intrinsics.checkParameterIsNotNull(deviceProvisionedController, "deviceProvisionedController");
        Intrinsics.checkParameterIsNotNull(notificationRowBinderImpl, "notificationRowBinder");
        Intrinsics.checkParameterIsNotNull(remoteInputUriController, "remoteInputUriController");
        Intrinsics.checkParameterIsNotNull(notificationGroupManager, "groupManager");
        Intrinsics.checkParameterIsNotNull(notificationGroupAlertTransferHelper, "groupAlertTransferHelper");
        Intrinsics.checkParameterIsNotNull(headsUpManager, "headsUpManager");
        Intrinsics.checkParameterIsNotNull(headsUpController, "headsUpController");
        Intrinsics.checkParameterIsNotNull(headsUpViewBinder, "headsUpViewBinder");
        Intrinsics.checkParameterIsNotNull(builder, "clickerBuilder");
        this.featureFlags = featureFlags;
        this.notificationListener = notificationListener;
        this.entryManager = notificationEntryManager;
        this.notifPipeline = lazy;
        this.targetSdkResolver = targetSdkResolver;
        this.newNotifPipeline = lazy2;
        this.notifBindPipelineInitializer = notifBindPipelineInitializer;
        this.deviceProvisionedController = deviceProvisionedController;
        this.notificationRowBinder = notificationRowBinderImpl;
        this.remoteInputUriController = remoteInputUriController;
        this.groupManager = notificationGroupManager;
        this.groupAlertTransferHelper = notificationGroupAlertTransferHelper;
        this.headsUpManager = headsUpManager;
        this.headsUpController = headsUpController;
        this.headsUpViewBinder = headsUpViewBinder;
        this.clickerBuilder = builder;
    }

    @Override // com.android.systemui.statusbar.notification.init.NotificationsController
    public void initialize(@NotNull StatusBar statusBar, @NotNull NotificationPresenter notificationPresenter, @NotNull NotificationListContainer notificationListContainer, @NotNull NotificationActivityStarter notificationActivityStarter, @NotNull NotificationRowBinderImpl.BindRowCallback bindRowCallback) {
        Intrinsics.checkParameterIsNotNull(statusBar, "statusBar");
        Intrinsics.checkParameterIsNotNull(notificationPresenter, "presenter");
        Intrinsics.checkParameterIsNotNull(notificationListContainer, "listContainer");
        Intrinsics.checkParameterIsNotNull(notificationActivityStarter, "notificationActivityStarter");
        Intrinsics.checkParameterIsNotNull(bindRowCallback, "bindRowCallback");
        this.notificationListener.registerAsSystemService();
        new NotificationListController(this.entryManager, notificationListContainer, this.deviceProvisionedController).bind();
        this.notificationRowBinder.setNotificationClicker(this.clickerBuilder.build(Optional.of(statusBar), notificationActivityStarter));
        this.notificationRowBinder.setUpWithPresenter(notificationPresenter, notificationListContainer, bindRowCallback);
        this.headsUpViewBinder.setPresenter(notificationPresenter);
        this.notifBindPipelineInitializer.initialize();
        if (this.featureFlags.isNewNotifPipelineEnabled()) {
            this.newNotifPipeline.get().initialize(this.notificationListener, this.notificationRowBinder, notificationListContainer);
        }
        if (this.featureFlags.isNewNotifPipelineRenderingEnabled()) {
            TargetSdkResolver targetSdkResolver = this.targetSdkResolver;
            NotifPipeline notifPipeline = this.notifPipeline.get();
            Intrinsics.checkExpressionValueIsNotNull(notifPipeline, "notifPipeline.get()");
            targetSdkResolver.initialize(notifPipeline);
            return;
        }
        this.targetSdkResolver.initialize(this.entryManager);
        this.remoteInputUriController.attach(this.entryManager);
        this.groupAlertTransferHelper.bind(this.entryManager, this.groupManager);
        this.headsUpManager.addListener(this.groupManager);
        this.headsUpManager.addListener(this.groupAlertTransferHelper);
        this.headsUpController.attach(this.entryManager, this.headsUpManager);
        this.groupManager.setHeadsUpManager(this.headsUpManager);
        this.groupAlertTransferHelper.setHeadsUpManager(this.headsUpManager);
        this.entryManager.attach(this.notificationListener);
    }

    @Override // com.android.systemui.statusbar.notification.init.NotificationsController
    public void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr, boolean z) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        if (z) {
            this.entryManager.dump(printWriter, "  ");
        }
        this.groupManager.dump(fileDescriptor, printWriter, strArr);
    }

    @Override // com.android.systemui.statusbar.notification.init.NotificationsController
    public void requestNotificationUpdate(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "reason");
        this.entryManager.updateNotifications(str);
    }

    @Override // com.android.systemui.statusbar.notification.init.NotificationsController
    public void resetUserExpandedStates() {
        for (NotificationEntry notificationEntry : this.entryManager.getVisibleNotifications()) {
            notificationEntry.resetUserExpansion();
        }
    }

    @Override // com.android.systemui.statusbar.notification.init.NotificationsController
    public void setNotificationSnoozed(@NotNull StatusBarNotification statusBarNotification, @NotNull NotificationSwipeActionHelper.SnoozeOption snoozeOption) {
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
        Intrinsics.checkParameterIsNotNull(snoozeOption, "snoozeOption");
        if (snoozeOption.getSnoozeCriterion() != null) {
            NotificationListener notificationListener = this.notificationListener;
            String key = statusBarNotification.getKey();
            SnoozeCriterion snoozeCriterion = snoozeOption.getSnoozeCriterion();
            Intrinsics.checkExpressionValueIsNotNull(snoozeCriterion, "snoozeOption.snoozeCriterion");
            notificationListener.snoozeNotification(key, snoozeCriterion.getId());
            return;
        }
        this.notificationListener.snoozeNotification(statusBarNotification.getKey(), ((long) (snoozeOption.getMinutesToSnoozeFor() * 60)) * ((long) 1000));
    }

    @Override // com.android.systemui.statusbar.notification.init.NotificationsController
    public int getActiveNotificationsCount() {
        return this.entryManager.getActiveNotificationsCount();
    }

    @Override // com.android.systemui.statusbar.notification.init.NotificationsController
    public void setNotificationSnoozed(@NotNull StatusBarNotification statusBarNotification, int i) {
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
        this.notificationListener.snoozeNotification(statusBarNotification.getKey(), ((long) (i * 60 * 60)) * ((long) 1000));
    }
}
