package com.oneplus.battery;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;
public final class OpChargingAnimationControllerImpl_Factory implements Factory<OpChargingAnimationControllerImpl> {
    private final Provider<Context> contextProvider;

    public OpChargingAnimationControllerImpl_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public OpChargingAnimationControllerImpl get() {
        return provideInstance(this.contextProvider);
    }

    public static OpChargingAnimationControllerImpl provideInstance(Provider<Context> provider) {
        return new OpChargingAnimationControllerImpl(provider.get());
    }

    public static OpChargingAnimationControllerImpl_Factory create(Provider<Context> provider) {
        return new OpChargingAnimationControllerImpl_Factory(provider);
    }
}
