package com.android.systemui.media;

import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.notification.stack.MediaHeaderView;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: KeyguardMediaController.kt */
public final class KeyguardMediaController {
    private final KeyguardBypassController bypassController;
    private final MediaHost mediaHost;
    private final NotificationLockscreenUserManager notifLockscreenUserManager;
    private final NotificationMediaManager notificationMediaManager;
    private final SysuiStatusBarStateController statusBarStateController;
    @Nullable
    private MediaHeaderView view;
    @Nullable
    private Function1<? super Boolean, Unit> visibilityChangedListener;

    public KeyguardMediaController(@NotNull MediaHost mediaHost, @NotNull KeyguardBypassController keyguardBypassController, @NotNull SysuiStatusBarStateController sysuiStatusBarStateController, @NotNull NotificationLockscreenUserManager notificationLockscreenUserManager, @NotNull NotificationMediaManager notificationMediaManager) {
        Intrinsics.checkParameterIsNotNull(mediaHost, "mediaHost");
        Intrinsics.checkParameterIsNotNull(keyguardBypassController, "bypassController");
        Intrinsics.checkParameterIsNotNull(sysuiStatusBarStateController, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(notificationLockscreenUserManager, "notifLockscreenUserManager");
        Intrinsics.checkParameterIsNotNull(notificationMediaManager, "notificationMediaManager");
        this.mediaHost = mediaHost;
        this.bypassController = keyguardBypassController;
        this.statusBarStateController = sysuiStatusBarStateController;
        this.notifLockscreenUserManager = notificationLockscreenUserManager;
        this.notificationMediaManager = notificationMediaManager;
        sysuiStatusBarStateController.addCallback(new StatusBarStateController.StateListener(this) { // from class: com.android.systemui.media.KeyguardMediaController.1
            final /* synthetic */ KeyguardMediaController this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStateChanged(int i) {
                this.this$0.updateVisibility();
            }
        });
    }

    public final void setVisibilityChangedListener(@Nullable Function1<? super Boolean, Unit> function1) {
        this.visibilityChangedListener = function1;
    }

    @Nullable
    public final MediaHeaderView getView() {
        return this.view;
    }

    public final void attach(@NotNull MediaHeaderView mediaHeaderView) {
        Intrinsics.checkParameterIsNotNull(mediaHeaderView, "mediaView");
        this.view = mediaHeaderView;
        this.mediaHost.addVisibilityChangeListener(new Function1<Boolean, Unit>(this) { // from class: com.android.systemui.media.KeyguardMediaController$attach$1
            final /* synthetic */ KeyguardMediaController this$0;

            {
                this.this$0 = r1;
            }

            /* Return type fixed from 'java.lang.Object' to match base method */
            /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Unit invoke(Boolean bool) {
                invoke(bool.booleanValue());
                return Unit.INSTANCE;
            }

            public final void invoke(boolean z) {
                this.this$0.updateVisibility();
            }
        });
        this.mediaHost.setExpansion(0.0f);
        this.mediaHost.setShowsOnlyActiveMedia(true);
        this.mediaHost.setFalsingProtectionNeeded(true);
        this.mediaHost.init(2);
        mediaHeaderView.setContentView(this.mediaHost.getHostView());
        updateVisibility();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void updateVisibility() {
        int i = 0;
        boolean z = true;
        boolean z2 = this.statusBarStateController.getState() == 1 || this.statusBarStateController.getState() == 3;
        if (!this.mediaHost.getVisible() || this.bypassController.getBypassEnabled() || !z2 || !this.notifLockscreenUserManager.shouldShowLockscreenNotifications()) {
            z = false;
        }
        MediaHeaderView mediaHeaderView = this.view;
        int visibility = mediaHeaderView != null ? mediaHeaderView.getVisibility() : 8;
        if (!z) {
            i = 8;
        }
        MediaHeaderView mediaHeaderView2 = this.view;
        if (mediaHeaderView2 != null) {
            mediaHeaderView2.setVisibility(i);
        }
        if (visibility != i) {
            Function1<? super Boolean, Unit> function1 = this.visibilityChangedListener;
            if (function1 != null) {
                function1.invoke(Boolean.valueOf(z));
            }
            if (this.view != null) {
                this.notificationMediaManager.onMediaHeaderViewChanged(i);
            }
        }
    }
}
