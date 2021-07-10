package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;
public final class OPDndTile_Factory implements Factory<OPDndTile> {
    private final Provider<QSHost> hostProvider;

    public OPDndTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public OPDndTile get() {
        return provideInstance(this.hostProvider);
    }

    public static OPDndTile provideInstance(Provider<QSHost> provider) {
        return new OPDndTile(provider.get());
    }

    public static OPDndTile_Factory create(Provider<QSHost> provider) {
        return new OPDndTile_Factory(provider);
    }
}
