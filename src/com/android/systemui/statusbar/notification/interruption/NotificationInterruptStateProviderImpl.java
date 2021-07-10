package com.android.systemui.statusbar.notification.interruption;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.hardware.display.AmbientDisplayConfiguration;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.service.dreams.IDreamManager;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationFilter;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.statusbar.notification.OpNotificationInterruptStateProvider;
import java.util.ArrayList;
import java.util.List;
public class NotificationInterruptStateProviderImpl extends OpNotificationInterruptStateProvider implements NotificationInterruptStateProvider {
    private final AmbientDisplayConfiguration mAmbientDisplayConfiguration;
    private ContentObserver mContactsObserver;
    private final ContentResolver mContentResolver;
    private final IDreamManager mDreamManager;
    private HeadsUpManager mHeadsUpManager;
    private final ContentObserver mHeadsUpObserver;
    private boolean mIsContactsChanged;
    private final NotificationFilter mNotificationFilter;
    private final PowerManager mPowerManager;
    private final StatusBarStateController mStatusBarStateController;
    private final List<NotificationInterruptSuppressor> mSuppressors = new ArrayList();
    @VisibleForTesting
    protected boolean mUseHeadsUp = false;

    public NotificationInterruptStateProviderImpl(ContentResolver contentResolver, PowerManager powerManager, IDreamManager iDreamManager, AmbientDisplayConfiguration ambientDisplayConfiguration, NotificationFilter notificationFilter, BatteryController batteryController, StatusBarStateController statusBarStateController, HeadsUpManager headsUpManager, Handler handler) {
        this.mContentResolver = contentResolver;
        this.mPowerManager = powerManager;
        this.mDreamManager = iDreamManager;
        this.mAmbientDisplayConfiguration = ambientDisplayConfiguration;
        this.mNotificationFilter = notificationFilter;
        this.mStatusBarStateController = statusBarStateController;
        this.mHeadsUpManager = headsUpManager;
        this.mHeadsUpObserver = new ContentObserver(handler) { // from class: com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProviderImpl.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                NotificationInterruptStateProviderImpl notificationInterruptStateProviderImpl = NotificationInterruptStateProviderImpl.this;
                boolean z2 = notificationInterruptStateProviderImpl.mUseHeadsUp;
                boolean z3 = false;
                if (Settings.Global.getInt(notificationInterruptStateProviderImpl.mContentResolver, "heads_up_notifications_enabled", 0) != 0) {
                    z3 = true;
                }
                notificationInterruptStateProviderImpl.mUseHeadsUp = z3;
                StringBuilder sb = new StringBuilder();
                sb.append("heads up is ");
                sb.append(NotificationInterruptStateProviderImpl.this.mUseHeadsUp ? "enabled" : "disabled");
                Log.d("InterruptionStateProvider", sb.toString());
                boolean z4 = NotificationInterruptStateProviderImpl.this.mUseHeadsUp;
                if (z2 != z4 && !z4) {
                    Log.d("InterruptionStateProvider", "dismissing any existing heads up notification on disable event");
                    NotificationInterruptStateProviderImpl.this.mHeadsUpManager.releaseAllImmediately();
                }
            }
        };
        this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("heads_up_notifications_enabled"), true, this.mHeadsUpObserver);
        this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("ticker_gets_heads_up"), true, this.mHeadsUpObserver);
        this.mHeadsUpObserver.onChange(true);
        init();
        AnonymousClass2 r1 = new ContentObserver(handler) { // from class: com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProviderImpl.2
            @Override // android.database.ContentObserver
            public void onChange(boolean z, Uri uri, int i) {
                NotificationInterruptStateProviderImpl.this.mIsContactsChanged = true;
            }
        };
        this.mContactsObserver = r1;
        this.mContentResolver.registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, r1, -1);
    }

    @Override // com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider
    public void addSuppressor(NotificationInterruptSuppressor notificationInterruptSuppressor) {
        this.mSuppressors.add(notificationInterruptSuppressor);
    }

    @Override // com.oneplus.systemui.statusbar.notification.OpNotificationInterruptStateProvider, com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider
    public boolean shouldBubbleUp(NotificationEntry notificationEntry) {
        StatusBarNotification sbn = notificationEntry.getSbn();
        if (!canAlertCommon(notificationEntry) || !canAlertAwakeCommon(notificationEntry)) {
            return false;
        }
        if (!notificationEntry.canBubble()) {
            Log.d("InterruptionStateProvider", "No bubble up: not allowed to bubble: " + sbn.getKey());
            return false;
        } else if (notificationEntry.getBubbleMetadata() != null && (notificationEntry.getBubbleMetadata().getShortcutId() != null || notificationEntry.getBubbleMetadata().getIntent() != null)) {
            return super.shouldBubbleUp(notificationEntry);
        } else {
            Log.d("InterruptionStateProvider", "No bubble up: notification: " + sbn.getKey() + " doesn't have valid metadata");
            return false;
        }
    }

    @Override // com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider
    public boolean shouldHeadsUp(NotificationEntry notificationEntry) {
        if (this.mStatusBarStateController.isDozing() || OpLsState.getInstance().getPhoneStatusBar().isDozingCustom()) {
            return shouldHeadsUpWhenDozing(notificationEntry);
        }
        return shouldHeadsUpWhenAwake(notificationEntry);
    }

    @Override // com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider
    public boolean shouldLaunchFullScreenIntentWhenAdded(NotificationEntry notificationEntry) {
        if (notificationEntry.getSbn().getNotification().fullScreenIntent == null || (shouldHeadsUp(notificationEntry) && this.mStatusBarStateController.getState() != 1)) {
            return false;
        }
        return true;
    }

    private boolean shouldHeadsUpWhenAwake(NotificationEntry notificationEntry) {
        boolean z;
        StatusBarNotification sbn = notificationEntry.getSbn();
        if (!this.mUseHeadsUp) {
            Log.d("InterruptionStateProvider", "No heads up: no huns");
            return false;
        } else if (!canAlertCommon(notificationEntry) || !canAlertAwakeCommon(notificationEntry)) {
            return false;
        } else {
            if (isSnoozedPackage(sbn)) {
                Log.d("InterruptionStateProvider", "No alerting: snoozed package: " + sbn.getKey());
                return false;
            }
            boolean z2 = true;
            boolean z3 = this.mStatusBarStateController.getState() == 0;
            if (notificationEntry.isBubble() && z3) {
                Log.d("InterruptionStateProvider", "No heads up: in unlocked shade where notification is shown as a bubble: " + sbn.getKey());
                return false;
            } else if (notificationEntry.shouldSuppressPeek()) {
                Log.d("InterruptionStateProvider", "No heads up: suppressed by DND: " + sbn.getKey());
                return false;
            } else if (notificationEntry.getImportance() < 4) {
                Log.d("InterruptionStateProvider", "No heads up: unimportant notification: " + sbn.getKey());
                return false;
            } else {
                try {
                    z = this.mDreamManager.isDreaming();
                } catch (RemoteException e) {
                    Log.e("InterruptionStateProvider", "Failed to query dream manager.", e);
                    z = false;
                }
                if (!this.mPowerManager.isScreenOn() || z) {
                    z2 = false;
                }
                if (!z2) {
                    Log.d("InterruptionStateProvider", "No heads up: not in use: " + sbn.getKey());
                    return false;
                }
                for (int i = 0; i < this.mSuppressors.size(); i++) {
                    if (this.mSuppressors.get(i).suppressAwakeHeadsUp(notificationEntry)) {
                        Log.d("InterruptionStateProvider", "No heads up: aborted by suppressor: " + this.mSuppressors.get(i).getName() + " sbnKey=" + sbn.getKey());
                        return false;
                    }
                }
                return super.shouldHeadsUp(sbn);
            }
        }
    }

    private boolean shouldHeadsUpWhenDozing(NotificationEntry notificationEntry) {
        StatusBarNotification sbn = notificationEntry.getSbn();
        if (!this.mAmbientDisplayConfiguration.pulseOnNotificationEnabled(-2)) {
            Log.d("InterruptionStateProvider", "No pulsing: disabled by setting: " + sbn.getKey());
            return false;
        } else if (!canAlertCommon(notificationEntry)) {
            Log.d("InterruptionStateProvider", "No pulsing: notification shouldn't alert: " + sbn.getKey());
            return false;
        } else if (notificationEntry.shouldSuppressAmbient()) {
            Log.d("InterruptionStateProvider", "No pulsing: ambient effect suppressed: " + sbn.getKey());
            return false;
        } else if (notificationEntry.getImportance() < 3) {
            Log.d("InterruptionStateProvider", "No pulsing: not important enough: " + sbn.getKey());
            return false;
        } else if (sbn.getNotification().fullScreenIntent != null) {
            Log.d("InterruptionStateProvider", "No pulsing: notification with fullScreenIntent: " + sbn.getKey());
            return false;
        } else if ((!"com.oneplus.dialer".equals(sbn.getPackageName()) && !"com.android.dialer".equals(sbn.getPackageName()) && !"com.google.android.dialer".equals(sbn.getPackageName())) || notificationEntry.getImportance() >= 4 || this.mOpNotificationController.getCallState() != 2) {
            return super.shouldPulse(notificationEntry);
        } else {
            Log.d("InterruptionStateProvider", "No pulsing: dialer off hook and not important enough: " + sbn.getKey());
            return false;
        }
    }

    private boolean canAlertCommon(NotificationEntry notificationEntry) {
        StatusBarNotification sbn = notificationEntry.getSbn();
        if (this.mNotificationFilter.shouldFilterOut(notificationEntry)) {
            Log.d("InterruptionStateProvider", "No alerting: filtered notification: " + sbn.getKey());
            return false;
        } else if (!sbn.isGroup() || !sbn.getNotification().suppressAlertingDueToGrouping()) {
            for (int i = 0; i < this.mSuppressors.size(); i++) {
                if (this.mSuppressors.get(i).suppressInterruptions(notificationEntry)) {
                    Log.d("InterruptionStateProvider", "No alerting: aborted by suppressor: " + this.mSuppressors.get(i).getName() + " sbnKey=" + sbn.getKey());
                    return false;
                }
            }
            if (!notificationEntry.hasJustLaunchedFullScreenIntent()) {
                return true;
            }
            Log.d("InterruptionStateProvider", "No alerting: recent fullscreen: " + sbn.getKey());
            return false;
        } else {
            Log.d("InterruptionStateProvider", "No alerting: suppressed due to group alert behavior");
            return false;
        }
    }

    private boolean canAlertAwakeCommon(NotificationEntry notificationEntry) {
        StatusBarNotification sbn = notificationEntry.getSbn();
        for (int i = 0; i < this.mSuppressors.size(); i++) {
            if (this.mSuppressors.get(i).suppressAwakeInterruptions(notificationEntry)) {
                Log.d("InterruptionStateProvider", "No alerting: aborted by suppressor: " + this.mSuppressors.get(i).getName() + " sbnKey=" + sbn.getKey());
                return false;
            }
        }
        return true;
    }

    private boolean isSnoozedPackage(StatusBarNotification statusBarNotification) {
        return this.mHeadsUpManager.isSnoozed(statusBarNotification.getPackageName());
    }

    @Override // com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider
    public int shouldHeadsUpWithReason(NotificationEntry notificationEntry) {
        boolean z;
        StatusBarNotification sbn = notificationEntry.getSbn();
        if (!this.mUseHeadsUp) {
            Log.d("InterruptionStateProvider", "No heads up: no huns");
            return 0;
        } else if (!canAlertCommon(notificationEntry) || !canAlertAwakeCommon(notificationEntry)) {
            return 0;
        } else {
            boolean z2 = true;
            boolean z3 = this.mStatusBarStateController.getState() == 0;
            if (notificationEntry.isBubble() && z3) {
                Log.d("InterruptionStateProvider", "No heads up: in unlocked shade where notification is shown as a bubble: " + sbn.getKey());
                return 0;
            } else if (notificationEntry.shouldSuppressPeek()) {
                Log.d("InterruptionStateProvider", "No heads up: suppressed by DND: " + sbn.getKey());
                return 0;
            } else if (notificationEntry.getImportance() < 4) {
                Log.d("InterruptionStateProvider", "No heads up: unimportant notification: " + sbn.getKey());
                return 0;
            } else {
                try {
                    z = this.mDreamManager.isDreaming();
                } catch (RemoteException e) {
                    Log.e("InterruptionStateProvider", "Failed to query dream manager.", e);
                    z = false;
                }
                if (!this.mPowerManager.isScreenOn() || z) {
                    z2 = false;
                }
                if (!z2) {
                    Log.d("InterruptionStateProvider", "No heads up: not in use: " + sbn.getKey());
                    return 0;
                }
                for (int i = 0; i < this.mSuppressors.size(); i++) {
                    if (this.mSuppressors.get(i).suppressAwakeHeadsUp(notificationEntry)) {
                        Log.d("InterruptionStateProvider", "No heads up: aborted by suppressor: " + this.mSuppressors.get(i).getName() + " sbnKey=" + sbn.getKey());
                        return 0;
                    }
                }
                return this.mOpNotificationController.canHeadsUp(sbn);
            }
        }
    }

    @Override // com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider
    public boolean isContactsChanged() {
        return this.mIsContactsChanged;
    }

    @Override // com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider
    public void setIsContactsChanged(boolean z) {
        this.mIsContactsChanged = z;
    }
}
