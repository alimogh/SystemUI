package com.oneplus.systemui.statusbar.policy;

import com.android.systemui.Dependency;
import com.android.systemui.statusbar.AlertingNotificationManager;
import com.oneplus.notification.OpNotificationController;
public abstract class OpHeadsUpManager extends AlertingNotificationManager {
    /* access modifiers changed from: protected */
    public void snoozeAlertEntry(AlertingNotificationManager.AlertEntry alertEntry) {
        ((OpNotificationController) Dependency.get(OpNotificationController.class)).snoozeHeadsUp(alertEntry.mEntry.getSbn().getNotification());
    }
}
