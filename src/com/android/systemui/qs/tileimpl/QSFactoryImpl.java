package com.android.systemui.qs.tileimpl;

import android.os.Build;
import android.util.Log;
import android.view.ContextThemeWrapper;
import com.android.systemui.C0016R$style;
import com.android.systemui.plugins.qs.QSFactory;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.external.CustomTile;
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
import dagger.Lazy;
import javax.inject.Provider;
public class QSFactoryImpl implements QSFactory {
    private final Provider<AirplaneModeTile> mAirplaneModeTileProvider;
    private final Provider<BatterySaverTile> mBatterySaverTileProvider;
    private final Provider<BluetoothTile> mBluetoothTileProvider;
    private final Provider<CastTile> mCastTileProvider;
    private final Provider<CellularTile> mCellularTileProvider;
    private final Provider<ColorInversionTile> mColorInversionTileProvider;
    private final Provider<DataSaverTile> mDataSaverTileProvider;
    private final Provider<DataSwitchTile> mDataSwitchTileProvider;
    private final Provider<DndTile> mDndTileProvider;
    private final Provider<FlashlightTile> mFlashlightTileProvider;
    private final Provider<GameModeTile> mGameModeTileProvider;
    private final Provider<HotspotTile> mHotspotTileProvider;
    private final Provider<LocationTile> mLocationTileProvider;
    private final Provider<GarbageMonitor.MemoryTile> mMemoryTileProvider;
    private final Provider<NfcTile> mNfcTileProvider;
    private final Provider<NightDisplayTile> mNightDisplayTileProvider;
    private final Provider<OPDndCarModeTile> mOPDndCarModeTileProvider;
    private final Provider<OPDndTile> mOPDndTileProvider;
    private final Provider<OPReverseChargeTile> mOPReverseChargeTile;
    private final Provider<OtgTile> mOtgTileProvider;
    private final Lazy<QSHost> mQsHostLazy;
    private final Provider<ReadModeTile> mReadModeTileProvider;
    private final Provider<RotationLockTile> mRotationLockTileProvider;
    private final Provider<ScreenRecordTile> mScreenRecordTileProvider;
    private final Provider<UiModeNightTile> mUiModeNightTileProvider;
    private final Provider<UserTile> mUserTileProvider;
    private final Provider<VPNTile> mVPNTileProvider;
    private final Provider<WifiTile> mWifiTileProvider;
    private final Provider<WorkModeTile> mWorkModeTileProvider;

    public QSFactoryImpl(Lazy<QSHost> lazy, Provider<WifiTile> provider, Provider<BluetoothTile> provider2, Provider<CellularTile> provider3, Provider<DndTile> provider4, Provider<ColorInversionTile> provider5, Provider<AirplaneModeTile> provider6, Provider<WorkModeTile> provider7, Provider<RotationLockTile> provider8, Provider<FlashlightTile> provider9, Provider<LocationTile> provider10, Provider<CastTile> provider11, Provider<HotspotTile> provider12, Provider<UserTile> provider13, Provider<BatterySaverTile> provider14, Provider<DataSaverTile> provider15, Provider<NightDisplayTile> provider16, Provider<NfcTile> provider17, Provider<GarbageMonitor.MemoryTile> provider18, Provider<UiModeNightTile> provider19, Provider<ScreenRecordTile> provider20, Provider<ReadModeTile> provider21, Provider<GameModeTile> provider22, Provider<OPDndCarModeTile> provider23, Provider<OtgTile> provider24, Provider<DataSwitchTile> provider25, Provider<VPNTile> provider26, Provider<OPDndTile> provider27, Provider<OPReverseChargeTile> provider28) {
        this.mQsHostLazy = lazy;
        this.mWifiTileProvider = provider;
        this.mBluetoothTileProvider = provider2;
        this.mCellularTileProvider = provider3;
        this.mDndTileProvider = provider4;
        this.mColorInversionTileProvider = provider5;
        this.mAirplaneModeTileProvider = provider6;
        this.mWorkModeTileProvider = provider7;
        this.mRotationLockTileProvider = provider8;
        this.mFlashlightTileProvider = provider9;
        this.mLocationTileProvider = provider10;
        this.mCastTileProvider = provider11;
        this.mHotspotTileProvider = provider12;
        this.mUserTileProvider = provider13;
        this.mBatterySaverTileProvider = provider14;
        this.mDataSaverTileProvider = provider15;
        this.mNightDisplayTileProvider = provider16;
        this.mNfcTileProvider = provider17;
        this.mMemoryTileProvider = provider18;
        this.mUiModeNightTileProvider = provider19;
        this.mScreenRecordTileProvider = provider20;
        this.mGameModeTileProvider = provider22;
        this.mReadModeTileProvider = provider21;
        this.mOPDndCarModeTileProvider = provider23;
        this.mOtgTileProvider = provider24;
        this.mDataSwitchTileProvider = provider25;
        this.mVPNTileProvider = provider26;
        this.mOPDndTileProvider = provider27;
        this.mOPReverseChargeTile = provider28;
    }

    @Override // com.android.systemui.plugins.qs.QSFactory
    public QSTile createTile(String str) {
        QSTileImpl createTileInternal = createTileInternal(str);
        if (createTileInternal != null) {
            createTileInternal.handleStale();
        }
        return createTileInternal;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private QSTileImpl createTileInternal(String str) {
        char c;
        switch (str.hashCode()) {
            case -2099331234:
                if (str.equals("dataswitch")) {
                    c = 23;
                    break;
                }
                c = 65535;
                break;
            case -2016941037:
                if (str.equals("inversion")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -1183073498:
                if (str.equals("flashlight")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -805491779:
                if (str.equals("screenrecord")) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case -677011630:
                if (str.equals("airplane")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -331239923:
                if (str.equals("battery")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case -40300674:
                if (str.equals("rotation")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 3154:
                if (str.equals("bt")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 99610:
                if (str.equals("dnd")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 108971:
                if (str.equals("nfc")) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case 110370:
                if (str.equals("otg")) {
                    c = 22;
                    break;
                }
                c = 65535;
                break;
            case 116980:
                if (str.equals("vpn")) {
                    c = 24;
                    break;
                }
                c = 65535;
                break;
            case 3046207:
                if (str.equals("cast")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 3049826:
                if (str.equals("cell")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 3075958:
                if (str.equals("dark")) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case 3165170:
                if (str.equals("game")) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case 3496342:
                if (str.equals("read")) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case 3599307:
                if (str.equals("user")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 3649301:
                if (str.equals("wifi")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 3655441:
                if (str.equals("work")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 104817688:
                if (str.equals("night")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case 105947033:
                if (str.equals("opdnd")) {
                    c = 25;
                    break;
                }
                c = 65535;
                break;
            case 109211285:
                if (str.equals("saver")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case 1099603663:
                if (str.equals("hotspot")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 1196133333:
                if (str.equals("opreversecharge")) {
                    c = 26;
                    break;
                }
                c = 65535;
                break;
            case 1361772990:
                if (str.equals("opdndcarmode")) {
                    c = 21;
                    break;
                }
                c = 65535;
                break;
            case 1901043637:
                if (str.equals("location")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return this.mWifiTileProvider.get();
            case 1:
                return this.mBluetoothTileProvider.get();
            case 2:
                return this.mCellularTileProvider.get();
            case 3:
                return this.mDndTileProvider.get();
            case 4:
                return this.mColorInversionTileProvider.get();
            case 5:
                return this.mAirplaneModeTileProvider.get();
            case 6:
                return this.mWorkModeTileProvider.get();
            case 7:
                return this.mRotationLockTileProvider.get();
            case '\b':
                return this.mFlashlightTileProvider.get();
            case '\t':
                return this.mLocationTileProvider.get();
            case '\n':
                return this.mCastTileProvider.get();
            case 11:
                return this.mHotspotTileProvider.get();
            case '\f':
                return this.mUserTileProvider.get();
            case '\r':
                return this.mBatterySaverTileProvider.get();
            case 14:
                return this.mDataSaverTileProvider.get();
            case 15:
                return this.mNightDisplayTileProvider.get();
            case 16:
                return this.mNfcTileProvider.get();
            case 17:
                return this.mUiModeNightTileProvider.get();
            case 18:
                return this.mScreenRecordTileProvider.get();
            case 19:
                return this.mGameModeTileProvider.get();
            case 20:
                return this.mReadModeTileProvider.get();
            case 21:
                return this.mOPDndCarModeTileProvider.get();
            case 22:
                return this.mOtgTileProvider.get();
            case 23:
                return this.mDataSwitchTileProvider.get();
            case 24:
                return this.mVPNTileProvider.get();
            case 25:
                return this.mOPDndTileProvider.get();
            case 26:
                return this.mOPReverseChargeTile.get();
            default:
                if (str.startsWith("custom(")) {
                    return CustomTile.create(this.mQsHostLazy.get(), str, this.mQsHostLazy.get().getUserContext());
                }
                if (Build.IS_DEBUGGABLE && str.equals("dbg:mem")) {
                    return this.mMemoryTileProvider.get();
                }
                Log.w("QSFactory", "No stock tile spec: " + str);
                return null;
        }
    }

    @Override // com.android.systemui.plugins.qs.QSFactory
    public QSTileView createTileView(QSTile qSTile, boolean z) {
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(this.mQsHostLazy.get().getContext(), C0016R$style.qs_theme);
        QSIconView createTileView = qSTile.createTileView(contextThemeWrapper);
        if (z) {
            return new QSTileBaseView(contextThemeWrapper, createTileView, z);
        }
        return new QSTileView(contextThemeWrapper, createTileView);
    }
}
