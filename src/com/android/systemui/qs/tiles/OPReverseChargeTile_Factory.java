package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.BatteryController;
import dagger.internal.Factory;
import javax.inject.Provider;
public final class OPReverseChargeTile_Factory implements Factory<OPReverseChargeTile> {
    private final Provider<BatteryController> batteryControllerProvider;
    private final Provider<QSHost> hostProvider;

    public OPReverseChargeTile_Factory(Provider<QSHost> provider, Provider<BatteryController> provider2) {
        this.hostProvider = provider;
        this.batteryControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public OPReverseChargeTile get() {
        return provideInstance(this.hostProvider, this.batteryControllerProvider);
    }

    public static OPReverseChargeTile provideInstance(Provider<QSHost> provider, Provider<BatteryController> provider2) {
        return new OPReverseChargeTile(provider.get(), provider2.get());
    }

    public static OPReverseChargeTile_Factory create(Provider<QSHost> provider, Provider<BatteryController> provider2) {
        return new OPReverseChargeTile_Factory(provider, provider2);
    }
}
