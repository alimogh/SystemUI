package com.android.systemui.statusbar.notification.collection;

import android.app.Notification;
import android.service.notification.StatusBarNotification;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: NotificationRankingManager.kt */
public final class NotificationRankingManagerKt {
    public static final boolean isSystemMax(@NotNull NotificationEntry notificationEntry) {
        if (notificationEntry.getImportance() >= 4) {
            StatusBarNotification sbn = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn, "sbn");
            if (isSystemNotification(sbn)) {
                return true;
            }
        }
        return false;
    }

    private static final boolean isSystemNotification(@NotNull StatusBarNotification statusBarNotification) {
        return Intrinsics.areEqual("android", statusBarNotification.getPackageName()) || Intrinsics.areEqual("com.android.systemui", statusBarNotification.getPackageName());
    }

    public static final boolean isColorizedForegroundService(@NotNull NotificationEntry notificationEntry) {
        StatusBarNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "sbn");
        Notification notification = sbn.getNotification();
        return notification.isForegroundService() && notification.isColorized() && notificationEntry.getImportance() > 1;
    }
}
