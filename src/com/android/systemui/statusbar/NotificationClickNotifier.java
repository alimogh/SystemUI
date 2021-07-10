package com.android.systemui.statusbar;

import android.app.Notification;
import android.os.RemoteException;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.systemui.util.Assert;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: NotificationClickNotifier.kt */
public final class NotificationClickNotifier {
    @NotNull
    private final IStatusBarService barService;
    @NotNull
    private final List<NotificationInteractionListener> listeners = new ArrayList();
    @NotNull
    private final Executor mainExecutor;

    public NotificationClickNotifier(@NotNull IStatusBarService iStatusBarService, @NotNull Executor executor) {
        Intrinsics.checkParameterIsNotNull(iStatusBarService, "barService");
        Intrinsics.checkParameterIsNotNull(executor, "mainExecutor");
        this.barService = iStatusBarService;
        this.mainExecutor = executor;
    }

    public final void addNotificationInteractionListener(@NotNull NotificationInteractionListener notificationInteractionListener) {
        Intrinsics.checkParameterIsNotNull(notificationInteractionListener, "listener");
        Assert.isMainThread();
        this.listeners.add(notificationInteractionListener);
    }

    /* access modifiers changed from: private */
    public final void notifyListenersAboutInteraction(String str) {
        for (NotificationInteractionListener notificationInteractionListener : this.listeners) {
            notificationInteractionListener.onNotificationInteraction(str);
        }
    }

    public final void onNotificationActionClick(@NotNull String str, int i, @NotNull Notification.Action action, @NotNull NotificationVisibility notificationVisibility, boolean z) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(action, "action");
        Intrinsics.checkParameterIsNotNull(notificationVisibility, "visibility");
        try {
            this.barService.onNotificationActionClick(str, i, action, notificationVisibility, z);
        } catch (RemoteException unused) {
        }
        this.mainExecutor.execute(new Runnable(this, str) { // from class: com.android.systemui.statusbar.NotificationClickNotifier$onNotificationActionClick$1
            final /* synthetic */ String $key;
            final /* synthetic */ NotificationClickNotifier this$0;

            {
                this.this$0 = r1;
                this.$key = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.this$0.notifyListenersAboutInteraction(this.$key);
            }
        });
    }

    public final void onNotificationClick(@NotNull String str, @NotNull NotificationVisibility notificationVisibility) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(notificationVisibility, "visibility");
        try {
            this.barService.onNotificationClick(str, notificationVisibility);
        } catch (RemoteException unused) {
        }
        this.mainExecutor.execute(new Runnable(this, str) { // from class: com.android.systemui.statusbar.NotificationClickNotifier$onNotificationClick$1
            final /* synthetic */ String $key;
            final /* synthetic */ NotificationClickNotifier this$0;

            {
                this.this$0 = r1;
                this.$key = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.this$0.notifyListenersAboutInteraction(this.$key);
            }
        });
    }
}
