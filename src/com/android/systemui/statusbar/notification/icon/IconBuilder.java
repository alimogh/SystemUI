package com.android.systemui.statusbar.notification.icon;

import android.app.Notification;
import android.content.Context;
import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: IconBuilder.kt */
public final class IconBuilder {
    private final Context context;

    public IconBuilder(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.context = context;
    }

    @NotNull
    public final StatusBarIconView createIconView(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        Context context = this.context;
        StringBuilder sb = new StringBuilder();
        StatusBarNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
        sb.append(sbn.getPackageName());
        sb.append("/0x");
        StatusBarNotification sbn2 = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn2, "entry.sbn");
        sb.append(Integer.toHexString(sbn2.getId()));
        return new StatusBarIconView(context, sb.toString(), notificationEntry.getSbn());
    }

    @NotNull
    public final CharSequence getIconContentDescription(@NotNull Notification notification) {
        Intrinsics.checkParameterIsNotNull(notification, "n");
        String contentDescForNotification = StatusBarIconView.contentDescForNotification(this.context, notification);
        Intrinsics.checkExpressionValueIsNotNull(contentDescForNotification, "StatusBarIconView.conten…rNotification(context, n)");
        return contentDescForNotification;
    }
}
