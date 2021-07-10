package com.android.systemui.statusbar.notification;

import android.content.Context;
import com.android.systemui.util.DeviceConfigProxy;
import dagger.internal.Factory;
import javax.inject.Provider;
public final class NotificationSectionsFeatureManager_Factory implements Factory<NotificationSectionsFeatureManager> {
    private final Provider<Context> contextProvider;
    private final Provider<DeviceConfigProxy> proxyProvider;

    public NotificationSectionsFeatureManager_Factory(Provider<DeviceConfigProxy> provider, Provider<Context> provider2) {
        this.proxyProvider = provider;
        this.contextProvider = provider2;
    }

    @Override // javax.inject.Provider
    public NotificationSectionsFeatureManager get() {
        return provideInstance(this.proxyProvider, this.contextProvider);
    }

    public static NotificationSectionsFeatureManager provideInstance(Provider<DeviceConfigProxy> provider, Provider<Context> provider2) {
        return new NotificationSectionsFeatureManager(provider.get(), provider2.get());
    }

    public static NotificationSectionsFeatureManager_Factory create(Provider<DeviceConfigProxy> provider, Provider<Context> provider2) {
        return new NotificationSectionsFeatureManager_Factory(provider, provider2);
    }
}
