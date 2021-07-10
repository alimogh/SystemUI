package com.oneplus.notification;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;
public final class OpNotificationController_Factory implements Factory<OpNotificationController> {
    private final Provider<Context> contextProvider;

    public OpNotificationController_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public OpNotificationController get() {
        return provideInstance(this.contextProvider);
    }

    public static OpNotificationController provideInstance(Provider<Context> provider) {
        return new OpNotificationController(provider.get());
    }

    public static OpNotificationController_Factory create(Provider<Context> provider) {
        return new OpNotificationController_Factory(provider);
    }
}
