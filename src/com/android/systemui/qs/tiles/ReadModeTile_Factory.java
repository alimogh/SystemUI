package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;
public final class ReadModeTile_Factory implements Factory<ReadModeTile> {
    private final Provider<QSHost> hostProvider;

    public ReadModeTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public ReadModeTile get() {
        return provideInstance(this.hostProvider);
    }

    public static ReadModeTile provideInstance(Provider<QSHost> provider) {
        return new ReadModeTile(provider.get());
    }

    public static ReadModeTile_Factory create(Provider<QSHost> provider) {
        return new ReadModeTile_Factory(provider);
    }
}
