package com.android.systemui.util.concurrency;

import android.os.Looper;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;
public final class ConcurrencyModule_ProvideMainDelayableExecutorFactory implements Factory<DelayableExecutor> {
    private final Provider<Looper> looperProvider;

    public ConcurrencyModule_ProvideMainDelayableExecutorFactory(Provider<Looper> provider) {
        this.looperProvider = provider;
    }

    @Override // javax.inject.Provider
    public DelayableExecutor get() {
        return provideInstance(this.looperProvider);
    }

    public static DelayableExecutor provideInstance(Provider<Looper> provider) {
        return proxyProvideMainDelayableExecutor(provider.get());
    }

    public static ConcurrencyModule_ProvideMainDelayableExecutorFactory create(Provider<Looper> provider) {
        return new ConcurrencyModule_ProvideMainDelayableExecutorFactory(provider);
    }

    public static DelayableExecutor proxyProvideMainDelayableExecutor(Looper looper) {
        DelayableExecutor provideMainDelayableExecutor = ConcurrencyModule.provideMainDelayableExecutor(looper);
        Preconditions.checkNotNull(provideMainDelayableExecutor, "Cannot return null from a non-@Nullable @Provides method");
        return provideMainDelayableExecutor;
    }
}
