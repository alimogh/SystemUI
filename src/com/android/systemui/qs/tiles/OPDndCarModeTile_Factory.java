package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;
public final class OPDndCarModeTile_Factory implements Factory<OPDndCarModeTile> {
    private final Provider<QSHost> hostProvider;

    public OPDndCarModeTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public OPDndCarModeTile get() {
        return provideInstance(this.hostProvider);
    }

    public static OPDndCarModeTile provideInstance(Provider<QSHost> provider) {
        return new OPDndCarModeTile(provider.get());
    }

    public static OPDndCarModeTile_Factory create(Provider<QSHost> provider) {
        return new OPDndCarModeTile_Factory(provider);
    }
}
