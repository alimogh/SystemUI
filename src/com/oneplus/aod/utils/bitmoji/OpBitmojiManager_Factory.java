package com.oneplus.aod.utils.bitmoji;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;
public final class OpBitmojiManager_Factory implements Factory<OpBitmojiManager> {
    private final Provider<Context> contextProvider;

    public OpBitmojiManager_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public OpBitmojiManager get() {
        return provideInstance(this.contextProvider);
    }

    public static OpBitmojiManager provideInstance(Provider<Context> provider) {
        return new OpBitmojiManager(provider.get());
    }

    public static OpBitmojiManager_Factory create(Provider<Context> provider) {
        return new OpBitmojiManager_Factory(provider);
    }
}
