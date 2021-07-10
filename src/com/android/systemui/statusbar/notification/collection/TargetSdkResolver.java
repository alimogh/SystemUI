package com.android.systemui.statusbar.notification.collection;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.phone.StatusBar;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: TargetSdkResolver.kt */
public final class TargetSdkResolver {
    private final String TAG = "TargetSdkResolver";
    private final Context context;

    public TargetSdkResolver(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.context = context;
    }

    public final void initialize(@NotNull CommonNotifCollection commonNotifCollection) {
        Intrinsics.checkParameterIsNotNull(commonNotifCollection, "collection");
        commonNotifCollection.addCollectionListener(new NotifCollectionListener(this) { // from class: com.android.systemui.statusbar.notification.collection.TargetSdkResolver$initialize$1
            final /* synthetic */ TargetSdkResolver this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
            public void onEntryBind(@NotNull NotificationEntry notificationEntry, @NotNull StatusBarNotification statusBarNotification) {
                Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
                Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
                notificationEntry.targetSdk = this.this$0.resolveNotificationSdk(statusBarNotification);
            }
        });
    }

    /* access modifiers changed from: private */
    public final int resolveNotificationSdk(StatusBarNotification statusBarNotification) {
        Context context = this.context;
        UserHandle user = statusBarNotification.getUser();
        Intrinsics.checkExpressionValueIsNotNull(user, "sbn.user");
        try {
            return StatusBar.getPackageManagerForUser(context, user.getIdentifier()).getApplicationInfo(statusBarNotification.getPackageName(), 0).targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            String str = this.TAG;
            Log.e(str, "Failed looking up ApplicationInfo for " + statusBarNotification.getPackageName(), e);
            return 0;
        }
    }
}
