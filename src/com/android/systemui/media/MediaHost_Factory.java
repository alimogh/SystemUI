package com.android.systemui.media;

import com.android.systemui.media.MediaHost;
import com.android.systemui.statusbar.NotificationMediaManager;
import dagger.internal.Factory;
import javax.inject.Provider;
public final class MediaHost_Factory implements Factory<MediaHost> {
    private final Provider<MediaDataFilter> mediaDataFilterProvider;
    private final Provider<MediaHierarchyManager> mediaHierarchyManagerProvider;
    private final Provider<MediaHostStatesManager> mediaHostStatesManagerProvider;
    private final Provider<NotificationMediaManager> notificationMediaManagerProvider;
    private final Provider<MediaHost.MediaHostStateHolder> stateProvider;

    public MediaHost_Factory(Provider<MediaHost.MediaHostStateHolder> provider, Provider<MediaHierarchyManager> provider2, Provider<MediaDataFilter> provider3, Provider<MediaHostStatesManager> provider4, Provider<NotificationMediaManager> provider5) {
        this.stateProvider = provider;
        this.mediaHierarchyManagerProvider = provider2;
        this.mediaDataFilterProvider = provider3;
        this.mediaHostStatesManagerProvider = provider4;
        this.notificationMediaManagerProvider = provider5;
    }

    @Override // javax.inject.Provider
    public MediaHost get() {
        return provideInstance(this.stateProvider, this.mediaHierarchyManagerProvider, this.mediaDataFilterProvider, this.mediaHostStatesManagerProvider, this.notificationMediaManagerProvider);
    }

    public static MediaHost provideInstance(Provider<MediaHost.MediaHostStateHolder> provider, Provider<MediaHierarchyManager> provider2, Provider<MediaDataFilter> provider3, Provider<MediaHostStatesManager> provider4, Provider<NotificationMediaManager> provider5) {
        return new MediaHost(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static MediaHost_Factory create(Provider<MediaHost.MediaHostStateHolder> provider, Provider<MediaHierarchyManager> provider2, Provider<MediaDataFilter> provider3, Provider<MediaHostStatesManager> provider4, Provider<NotificationMediaManager> provider5) {
        return new MediaHost_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
