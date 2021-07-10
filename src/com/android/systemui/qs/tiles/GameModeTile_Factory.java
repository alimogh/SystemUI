package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;
public final class GameModeTile_Factory implements Factory<GameModeTile> {
    private final Provider<QSHost> hostProvider;

    public GameModeTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public GameModeTile get() {
        return provideInstance(this.hostProvider);
    }

    public static GameModeTile provideInstance(Provider<QSHost> provider) {
        return new GameModeTile(provider.get());
    }

    public static GameModeTile_Factory create(Provider<QSHost> provider) {
        return new GameModeTile_Factory(provider);
    }
}
