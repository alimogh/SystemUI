package com.android.systemui.statusbar.notification.people;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.os.UserManager;
import android.util.IconDrawableFactory;
import android.util.SparseArray;
import com.android.settingslib.notification.ConversationIconFactory;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: PeopleHubNotificationListener.kt */
public final class PeopleHubDataSourceImpl implements DataSource<Object> {
    private final Executor bgExecutor;
    private final List<Object<Object>> dataListeners = new ArrayList();
    private final NotificationPersonExtractor extractor;
    private final ConversationIconFactory iconFactory;
    private final Executor mainExecutor;
    private final NotificationLockscreenUserManager notifLockscreenUserMgr;
    private final NotificationListener notificationListener;
    private final SparseArray<Object> peopleHubManagerForUser = new SparseArray<>();
    private final PeopleNotificationIdentifier peopleNotificationIdentifier;
    private final UserManager userManager;

    public PeopleHubDataSourceImpl(@NotNull NotificationEntryManager notificationEntryManager, @NotNull NotificationPersonExtractor notificationPersonExtractor, @NotNull UserManager userManager, @NotNull LauncherApps launcherApps, @NotNull PackageManager packageManager, @NotNull Context context, @NotNull NotificationListener notificationListener, @NotNull Executor executor, @NotNull Executor executor2, @NotNull NotificationLockscreenUserManager notificationLockscreenUserManager, @NotNull PeopleNotificationIdentifier peopleNotificationIdentifier) {
        Intrinsics.checkParameterIsNotNull(notificationEntryManager, "notificationEntryManager");
        Intrinsics.checkParameterIsNotNull(notificationPersonExtractor, "extractor");
        Intrinsics.checkParameterIsNotNull(userManager, "userManager");
        Intrinsics.checkParameterIsNotNull(launcherApps, "launcherApps");
        Intrinsics.checkParameterIsNotNull(packageManager, "packageManager");
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(notificationListener, "notificationListener");
        Intrinsics.checkParameterIsNotNull(executor, "bgExecutor");
        Intrinsics.checkParameterIsNotNull(executor2, "mainExecutor");
        Intrinsics.checkParameterIsNotNull(notificationLockscreenUserManager, "notifLockscreenUserMgr");
        Intrinsics.checkParameterIsNotNull(peopleNotificationIdentifier, "peopleNotificationIdentifier");
        this.extractor = notificationPersonExtractor;
        this.userManager = userManager;
        this.notificationListener = notificationListener;
        this.bgExecutor = executor;
        this.mainExecutor = executor2;
        this.notifLockscreenUserMgr = notificationLockscreenUserManager;
        this.peopleNotificationIdentifier = peopleNotificationIdentifier;
        Context applicationContext = context.getApplicationContext();
        IconDrawableFactory newInstance = IconDrawableFactory.newInstance(applicationContext);
        Intrinsics.checkExpressionValueIsNotNull(applicationContext, "appContext");
        this.iconFactory = new ConversationIconFactory(applicationContext, launcherApps, packageManager, newInstance, applicationContext.getResources().getDimensionPixelSize(C0005R$dimen.notification_guts_conversation_icon_size));
    }
}
