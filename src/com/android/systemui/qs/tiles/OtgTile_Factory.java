package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;
public final class OtgTile_Factory implements Factory<OtgTile> {
    private final Provider<QSHost> hostProvider;

    public OtgTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public OtgTile get() {
        return provideInstance(this.hostProvider);
    }

    public static OtgTile provideInstance(Provider<QSHost> provider) {
        return new OtgTile(provider.get());
    }

    public static OtgTile_Factory create(Provider<QSHost> provider) {
        return new OtgTile_Factory(provider);
    }
}
