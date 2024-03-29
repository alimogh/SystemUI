package com.android.systemui.media;

import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import dagger.internal.Factory;
import javax.inject.Provider;
public final class KeyguardMediaController_Factory implements Factory<KeyguardMediaController> {
    private final Provider<KeyguardBypassController> bypassControllerProvider;
    private final Provider<MediaHost> mediaHostProvider;
    private final Provider<NotificationLockscreenUserManager> notifLockscreenUserManagerProvider;
    private final Provider<NotificationMediaManager> notificationMediaManagerProvider;
    private final Provider<SysuiStatusBarStateController> statusBarStateControllerProvider;

    public KeyguardMediaController_Factory(Provider<MediaHost> provider, Provider<KeyguardBypassController> provider2, Provider<SysuiStatusBarStateController> provider3, Provider<NotificationLockscreenUserManager> provider4, Provider<NotificationMediaManager> provider5) {
        this.mediaHostProvider = provider;
        this.bypassControllerProvider = provider2;
        this.statusBarStateControllerProvider = provider3;
        this.notifLockscreenUserManagerProvider = provider4;
        this.notificationMediaManagerProvider = provider5;
    }

    @Override // javax.inject.Provider
    public KeyguardMediaController get() {
        return provideInstance(this.mediaHostProvider, this.bypassControllerProvider, this.statusBarStateControllerProvider, this.notifLockscreenUserManagerProvider, this.notificationMediaManagerProvider);
    }

    public static KeyguardMediaController provideInstance(Provider<MediaHost> provider, Provider<KeyguardBypassController> provider2, Provider<SysuiStatusBarStateController> provider3, Provider<NotificationLockscreenUserManager> provider4, Provider<NotificationMediaManager> provider5) {
        return new KeyguardMediaController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static KeyguardMediaController_Factory create(Provider<MediaHost> provider, Provider<KeyguardBypassController> provider2, Provider<SysuiStatusBarStateController> provider3, Provider<NotificationLockscreenUserManager> provider4, Provider<NotificationMediaManager> provider5) {
        return new KeyguardMediaController_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
