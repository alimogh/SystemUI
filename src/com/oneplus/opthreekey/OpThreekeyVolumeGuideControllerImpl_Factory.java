package com.oneplus.opthreekey;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;
public final class OpThreekeyVolumeGuideControllerImpl_Factory implements Factory<OpThreekeyVolumeGuideControllerImpl> {
    private final Provider<Context> contextProvider;

    public OpThreekeyVolumeGuideControllerImpl_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public OpThreekeyVolumeGuideControllerImpl get() {
        return provideInstance(this.contextProvider);
    }

    public static OpThreekeyVolumeGuideControllerImpl provideInstance(Provider<Context> provider) {
        return new OpThreekeyVolumeGuideControllerImpl(provider.get());
    }

    public static OpThreekeyVolumeGuideControllerImpl_Factory create(Provider<Context> provider) {
        return new OpThreekeyVolumeGuideControllerImpl_Factory(provider);
    }
}
