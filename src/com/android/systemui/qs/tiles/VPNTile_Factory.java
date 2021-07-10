package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;
public final class VPNTile_Factory implements Factory<VPNTile> {
    private final Provider<QSHost> hostProvider;

    public VPNTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public VPNTile get() {
        return provideInstance(this.hostProvider);
    }

    public static VPNTile provideInstance(Provider<QSHost> provider) {
        return new VPNTile(provider.get());
    }

    public static VPNTile_Factory create(Provider<QSHost> provider) {
        return new VPNTile_Factory(provider);
    }
}
