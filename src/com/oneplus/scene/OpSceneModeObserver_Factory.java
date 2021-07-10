package com.oneplus.scene;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;
public final class OpSceneModeObserver_Factory implements Factory<OpSceneModeObserver> {
    private final Provider<Context> contextProvider;

    public OpSceneModeObserver_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public OpSceneModeObserver get() {
        return provideInstance(this.contextProvider);
    }

    public static OpSceneModeObserver provideInstance(Provider<Context> provider) {
        return new OpSceneModeObserver(provider.get());
    }

    public static OpSceneModeObserver_Factory create(Provider<Context> provider) {
        return new OpSceneModeObserver_Factory(provider);
    }
}
