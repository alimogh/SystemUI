package com.oneplus.systemui.biometrics;

import android.content.Context;
import com.android.systemui.statusbar.CommandQueue;
import dagger.internal.Factory;
import javax.inject.Provider;
public final class OpBiometricDialogImpl_Factory implements Factory<OpBiometricDialogImpl> {
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<Context> contextProvider;

    public OpBiometricDialogImpl_Factory(Provider<Context> provider, Provider<CommandQueue> provider2) {
        this.contextProvider = provider;
        this.commandQueueProvider = provider2;
    }

    @Override // javax.inject.Provider
    public OpBiometricDialogImpl get() {
        return provideInstance(this.contextProvider, this.commandQueueProvider);
    }

    public static OpBiometricDialogImpl provideInstance(Provider<Context> provider, Provider<CommandQueue> provider2) {
        return new OpBiometricDialogImpl(provider.get(), provider2.get());
    }

    public static OpBiometricDialogImpl_Factory create(Provider<Context> provider, Provider<CommandQueue> provider2) {
        return new OpBiometricDialogImpl_Factory(provider, provider2);
    }
}
