package com.android.systemui.statusbar.notification.icon;

import android.content.pm.LauncherApps;
import com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection;
import dagger.internal.Factory;
import javax.inject.Provider;
public final class IconManager_Factory implements Factory<IconManager> {
    private final Provider<IconBuilder> iconBuilderProvider;
    private final Provider<LauncherApps> launcherAppsProvider;
    private final Provider<CommonNotifCollection> notifCollectionProvider;

    public IconManager_Factory(Provider<CommonNotifCollection> provider, Provider<LauncherApps> provider2, Provider<IconBuilder> provider3) {
        this.notifCollectionProvider = provider;
        this.launcherAppsProvider = provider2;
        this.iconBuilderProvider = provider3;
    }

    @Override // javax.inject.Provider
    public IconManager get() {
        return provideInstance(this.notifCollectionProvider, this.launcherAppsProvider, this.iconBuilderProvider);
    }

    public static IconManager provideInstance(Provider<CommonNotifCollection> provider, Provider<LauncherApps> provider2, Provider<IconBuilder> provider3) {
        return new IconManager(provider.get(), provider2.get(), provider3.get());
    }

    public static IconManager_Factory create(Provider<CommonNotifCollection> provider, Provider<LauncherApps> provider2, Provider<IconBuilder> provider3) {
        return new IconManager_Factory(provider, provider2, provider3);
    }
}
