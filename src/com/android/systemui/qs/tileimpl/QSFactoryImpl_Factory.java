package com.android.systemui.qs.tileimpl;

import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tiles.AirplaneModeTile;
import com.android.systemui.qs.tiles.BatterySaverTile;
import com.android.systemui.qs.tiles.BluetoothTile;
import com.android.systemui.qs.tiles.CastTile;
import com.android.systemui.qs.tiles.CellularTile;
import com.android.systemui.qs.tiles.ColorInversionTile;
import com.android.systemui.qs.tiles.DataSaverTile;
import com.android.systemui.qs.tiles.DataSwitchTile;
import com.android.systemui.qs.tiles.DndTile;
import com.android.systemui.qs.tiles.FlashlightTile;
import com.android.systemui.qs.tiles.GameModeTile;
import com.android.systemui.qs.tiles.HotspotTile;
import com.android.systemui.qs.tiles.LocationTile;
import com.android.systemui.qs.tiles.NfcTile;
import com.android.systemui.qs.tiles.NightDisplayTile;
import com.android.systemui.qs.tiles.OPDndCarModeTile;
import com.android.systemui.qs.tiles.OPDndTile;
import com.android.systemui.qs.tiles.OPReverseChargeTile;
import com.android.systemui.qs.tiles.OtgTile;
import com.android.systemui.qs.tiles.ReadModeTile;
import com.android.systemui.qs.tiles.RotationLockTile;
import com.android.systemui.qs.tiles.ScreenRecordTile;
import com.android.systemui.qs.tiles.UiModeNightTile;
import com.android.systemui.qs.tiles.UserTile;
import com.android.systemui.qs.tiles.VPNTile;
import com.android.systemui.qs.tiles.WifiTile;
import com.android.systemui.qs.tiles.WorkModeTile;
import com.android.systemui.util.leak.GarbageMonitor;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import javax.inject.Provider;
public final class QSFactoryImpl_Factory implements Factory<QSFactoryImpl> {
    private final Provider<AirplaneModeTile> airplaneModeTileProvider;
    private final Provider<BatterySaverTile> batterySaverTileProvider;
    private final Provider<BluetoothTile> bluetoothTileProvider;
    private final Provider<CastTile> castTileProvider;
    private final Provider<CellularTile> cellularTileProvider;
    private final Provider<ColorInversionTile> colorInversionTileProvider;
    private final Provider<DataSaverTile> dataSaverTileProvider;
    private final Provider<DataSwitchTile> dataSwitchTileProvider;
    private final Provider<DndTile> dndTileProvider;
    private final Provider<FlashlightTile> flashlightTileProvider;
    private final Provider<GameModeTile> gameModeTileProvider;
    private final Provider<HotspotTile> hotspotTileProvider;
    private final Provider<LocationTile> locationTileProvider;
    private final Provider<GarbageMonitor.MemoryTile> memoryTileProvider;
    private final Provider<NfcTile> nfcTileProvider;
    private final Provider<NightDisplayTile> nightDisplayTileProvider;
    private final Provider<OPDndCarModeTile> opDndCarModeTileProvider;
    private final Provider<OPDndTile> opDndTileProvider;
    private final Provider<OPReverseChargeTile> opReverseChargeTileProvider;
    private final Provider<OtgTile> otgTileProvider;
    private final Provider<QSHost> qsHostLazyProvider;
    private final Provider<ReadModeTile> readModeTileProvider;
    private final Provider<RotationLockTile> rotationLockTileProvider;
    private final Provider<ScreenRecordTile> screenRecordTileProvider;
    private final Provider<UiModeNightTile> uiModeNightTileProvider;
    private final Provider<UserTile> userTileProvider;
    private final Provider<VPNTile> vpnTileProvider;
    private final Provider<WifiTile> wifiTileProvider;
    private final Provider<WorkModeTile> workModeTileProvider;

    public QSFactoryImpl_Factory(Provider<QSHost> provider, Provider<WifiTile> provider2, Provider<BluetoothTile> provider3, Provider<CellularTile> provider4, Provider<DndTile> provider5, Provider<ColorInversionTile> provider6, Provider<AirplaneModeTile> provider7, Provider<WorkModeTile> provider8, Provider<RotationLockTile> provider9, Provider<FlashlightTile> provider10, Provider<LocationTile> provider11, Provider<CastTile> provider12, Provider<HotspotTile> provider13, Provider<UserTile> provider14, Provider<BatterySaverTile> provider15, Provider<DataSaverTile> provider16, Provider<NightDisplayTile> provider17, Provider<NfcTile> provider18, Provider<GarbageMonitor.MemoryTile> provider19, Provider<UiModeNightTile> provider20, Provider<ScreenRecordTile> provider21, Provider<ReadModeTile> provider22, Provider<GameModeTile> provider23, Provider<OPDndCarModeTile> provider24, Provider<OtgTile> provider25, Provider<DataSwitchTile> provider26, Provider<VPNTile> provider27, Provider<OPDndTile> provider28, Provider<OPReverseChargeTile> provider29) {
        this.qsHostLazyProvider = provider;
        this.wifiTileProvider = provider2;
        this.bluetoothTileProvider = provider3;
        this.cellularTileProvider = provider4;
        this.dndTileProvider = provider5;
        this.colorInversionTileProvider = provider6;
        this.airplaneModeTileProvider = provider7;
        this.workModeTileProvider = provider8;
        this.rotationLockTileProvider = provider9;
        this.flashlightTileProvider = provider10;
        this.locationTileProvider = provider11;
        this.castTileProvider = provider12;
        this.hotspotTileProvider = provider13;
        this.userTileProvider = provider14;
        this.batterySaverTileProvider = provider15;
        this.dataSaverTileProvider = provider16;
        this.nightDisplayTileProvider = provider17;
        this.nfcTileProvider = provider18;
        this.memoryTileProvider = provider19;
        this.uiModeNightTileProvider = provider20;
        this.screenRecordTileProvider = provider21;
        this.readModeTileProvider = provider22;
        this.gameModeTileProvider = provider23;
        this.opDndCarModeTileProvider = provider24;
        this.otgTileProvider = provider25;
        this.dataSwitchTileProvider = provider26;
        this.vpnTileProvider = provider27;
        this.opDndTileProvider = provider28;
        this.opReverseChargeTileProvider = provider29;
    }

    @Override // javax.inject.Provider
    public QSFactoryImpl get() {
        return provideInstance(this.qsHostLazyProvider, this.wifiTileProvider, this.bluetoothTileProvider, this.cellularTileProvider, this.dndTileProvider, this.colorInversionTileProvider, this.airplaneModeTileProvider, this.workModeTileProvider, this.rotationLockTileProvider, this.flashlightTileProvider, this.locationTileProvider, this.castTileProvider, this.hotspotTileProvider, this.userTileProvider, this.batterySaverTileProvider, this.dataSaverTileProvider, this.nightDisplayTileProvider, this.nfcTileProvider, this.memoryTileProvider, this.uiModeNightTileProvider, this.screenRecordTileProvider, this.readModeTileProvider, this.gameModeTileProvider, this.opDndCarModeTileProvider, this.otgTileProvider, this.dataSwitchTileProvider, this.vpnTileProvider, this.opDndTileProvider, this.opReverseChargeTileProvider);
    }

    public static QSFactoryImpl provideInstance(Provider<QSHost> provider, Provider<WifiTile> provider2, Provider<BluetoothTile> provider3, Provider<CellularTile> provider4, Provider<DndTile> provider5, Provider<ColorInversionTile> provider6, Provider<AirplaneModeTile> provider7, Provider<WorkModeTile> provider8, Provider<RotationLockTile> provider9, Provider<FlashlightTile> provider10, Provider<LocationTile> provider11, Provider<CastTile> provider12, Provider<HotspotTile> provider13, Provider<UserTile> provider14, Provider<BatterySaverTile> provider15, Provider<DataSaverTile> provider16, Provider<NightDisplayTile> provider17, Provider<NfcTile> provider18, Provider<GarbageMonitor.MemoryTile> provider19, Provider<UiModeNightTile> provider20, Provider<ScreenRecordTile> provider21, Provider<ReadModeTile> provider22, Provider<GameModeTile> provider23, Provider<OPDndCarModeTile> provider24, Provider<OtgTile> provider25, Provider<DataSwitchTile> provider26, Provider<VPNTile> provider27, Provider<OPDndTile> provider28, Provider<OPReverseChargeTile> provider29) {
        return new QSFactoryImpl(DoubleCheck.lazy(provider), provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15, provider16, provider17, provider18, provider19, provider20, provider21, provider22, provider23, provider24, provider25, provider26, provider27, provider28, provider29);
    }

    public static QSFactoryImpl_Factory create(Provider<QSHost> provider, Provider<WifiTile> provider2, Provider<BluetoothTile> provider3, Provider<CellularTile> provider4, Provider<DndTile> provider5, Provider<ColorInversionTile> provider6, Provider<AirplaneModeTile> provider7, Provider<WorkModeTile> provider8, Provider<RotationLockTile> provider9, Provider<FlashlightTile> provider10, Provider<LocationTile> provider11, Provider<CastTile> provider12, Provider<HotspotTile> provider13, Provider<UserTile> provider14, Provider<BatterySaverTile> provider15, Provider<DataSaverTile> provider16, Provider<NightDisplayTile> provider17, Provider<NfcTile> provider18, Provider<GarbageMonitor.MemoryTile> provider19, Provider<UiModeNightTile> provider20, Provider<ScreenRecordTile> provider21, Provider<ReadModeTile> provider22, Provider<GameModeTile> provider23, Provider<OPDndCarModeTile> provider24, Provider<OtgTile> provider25, Provider<DataSwitchTile> provider26, Provider<VPNTile> provider27, Provider<OPDndTile> provider28, Provider<OPReverseChargeTile> provider29) {
        return new QSFactoryImpl_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15, provider16, provider17, provider18, provider19, provider20, provider21, provider22, provider23, provider24, provider25, provider26, provider27, provider28, provider29);
    }
}
