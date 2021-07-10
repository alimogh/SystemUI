package com.oneplus.systemui.statusbar.notification;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.oneplus.notification.OpNotificationController;
import com.oneplus.plugin.OpLsState;
import com.oneplus.util.OpUtils;
public class OpNotificationInterruptStateProvider implements ConfigurationController.ConfigurationListener {
    private static final boolean DEBUG_ONEPLUS = OpUtils.DEBUG_ONEPLUS;
    private final NotificationLockscreenUserManager mLockscreenUserManager = ((NotificationLockscreenUserManager) Dependency.get(NotificationLockscreenUserManager.class));
    protected final OpNotificationController mOpNotificationController = ((OpNotificationController) Dependency.get(OpNotificationController.class));
    protected int mOrientation = 0;

    /* access modifiers changed from: protected */
    public void init() {
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    /* access modifiers changed from: protected */
    public boolean shouldHeadsUp(StatusBarNotification statusBarNotification) {
        return this.mOpNotificationController.canHeadsUp(statusBarNotification) == -1;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider
    public boolean shouldBubbleUp(NotificationEntry notificationEntry) {
        Context systemUIContext = OpLsState.getInstance().getSystemUIContext();
        if (this.mOrientation == 0 && systemUIContext != null) {
            this.mOrientation = systemUIContext.getResources().getConfiguration().orientation;
        }
        StatusBarNotification sbn = notificationEntry.getSbn();
        if (!OpUtils.QUICK_REPLY_BUBBLE || !this.mOpNotificationController.isQuickReplyApp(sbn.getPackageName()) || OpUtils.QUICK_REPLY_PORTRAIT_ENABLED || this.mOrientation != 1) {
            return true;
        }
        if (!DEBUG_ONEPLUS) {
            return false;
        }
        Log.d("OpNotificationInterruptionStateProvider", "No bubble up: quick reply bubble is disabled in portrait: " + sbn.getKey());
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean shouldPulse(NotificationEntry notificationEntry) {
        StatusBarNotification sbn = notificationEntry.getSbn();
        if (!this.mLockscreenUserManager.shouldShowOnKeyguard(notificationEntry)) {
            if (DEBUG_ONEPLUS) {
                Log.d("OpNotificationInterruptionStateProvider", "No pulsing: notification should be hidden on keyguard: " + sbn.getKey());
            }
            return false;
        }
        Bundle bundle = sbn.getNotification().extras;
        CharSequence charSequence = bundle.getCharSequence("android.title");
        CharSequence charSequence2 = bundle.getCharSequence("android.text");
        if (!TextUtils.isEmpty(charSequence) || !TextUtils.isEmpty(charSequence2)) {
            return true;
        }
        if (DEBUG_ONEPLUS) {
            Log.d("OpNotificationInterruptionStateProvider", "No pulsing: title and text are empty: " + sbn.getKey());
        }
        return false;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(Configuration configuration) {
        this.mOrientation = configuration.orientation;
    }
}
