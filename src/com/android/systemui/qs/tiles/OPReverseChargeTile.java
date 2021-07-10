package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.util.OpFeatures;
import android.widget.Switch;
import android.widget.Toast;
import androidx.lifecycle.Lifecycle;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.BatteryController;
import com.oneplus.util.OpReverseWirelessChargeUtils;
import com.oneplus.util.SystemSetting;
public class OPReverseChargeTile extends QSTileImpl<QSTile.BooleanState> implements BatteryController.BatteryStateChangeCallback {
    private BatteryController mBatteryController;
    protected final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.qs.tiles.OPReverseChargeTile.3
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                boolean booleanExtra = intent.getBooleanExtra("reverse_wireless_charge", false);
                String str = ((QSTileImpl) OPReverseChargeTile.this).TAG;
                Log.d(str, "reverse wireless charging enable:" + booleanExtra);
                if (booleanExtra != OpReverseWirelessChargeUtils.isEnabled()) {
                    OPReverseChargeTile.this.mHandler.removeMessages(100);
                    OPReverseChargeTile.this.mDebounce = false;
                }
            }
        }
    };
    private boolean mCharging = false;
    private boolean mDebounce = false;
    private SettingsObserver mDisableObserver;
    private Handler mHandler = new Handler(Looper.getMainLooper()) { // from class: com.android.systemui.qs.tiles.OPReverseChargeTile.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 100) {
                OPReverseChargeTile.this.mDebounce = false;
            }
        }
    };
    private boolean mLowBatt = false;
    private SystemSetting mModeSetting;
    private boolean mOverHeat = false;
    private Toast mToast;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 2007;
    }

    public OPReverseChargeTile(QSHost qSHost, BatteryController batteryController) {
        super(qSHost);
        this.mBatteryController = batteryController;
        batteryController.observe(getLifecycle(), (Lifecycle) this);
        SettingsObserver settingsObserver = new SettingsObserver(this.mContext.getMainThreadHandler());
        this.mDisableObserver = settingsObserver;
        settingsObserver.onChange(false, null);
        this.mModeSetting = new SystemSetting(this.mContext, null, "reverse_wireless_charging_status", true) { // from class: com.android.systemui.qs.tiles.OPReverseChargeTile.2
            /* access modifiers changed from: protected */
            @Override // com.oneplus.util.SystemSetting
            public void handleValueChanged(int i, boolean z) {
                String str = ((QSTileImpl) OPReverseChargeTile.this).TAG;
                Log.d(str, "mModeSettingChanged: value=" + i);
                OPReverseChargeTile.this.refreshState();
            }
        };
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return OpFeatures.isSupport(new int[]{237}) && (KeyguardUpdateMonitor.getCurrentUser() == 0);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if (this.mCharging) {
            popChargingToast();
        } else if (this.mLowBatt) {
            popLowBatteryToast();
        } else if (this.mOverHeat) {
            popOverHeatToast();
        }
        if (getState().state != 0) {
            if (this.mDebounce) {
                Log.d(this.TAG, "click too quickly, disable change value");
                return;
            }
            this.mDebounce = true;
            this.mHandler.removeMessages(100);
            Handler handler = this.mHandler;
            handler.sendMessageDelayed(handler.obtainMessage(100), 1000);
            boolean isEnabled = OpReverseWirelessChargeUtils.isEnabled();
            String str = this.TAG;
            Log.d(str, "status=" + isEnabled);
            OpReverseWirelessChargeUtils.setEnabled(this.mContext, true ^ isEnabled);
        }
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.value = OpReverseWirelessChargeUtils.isEnabled();
        booleanState.label = this.mContext.getString(C0015R$string.op_reverse_charge_tile_label);
        booleanState.icon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_qs_reverse_wireless_charge_icon);
        if (this.mLowBatt || this.mOverHeat || this.mCharging) {
            String str = this.TAG;
            Log.d(str, "isDisabled: low_batt=" + this.mLowBatt + ", overheat=" + this.mOverHeat + ", charging=" + this.mCharging);
            booleanState.state = 0;
            this.mHandler.post(new Runnable() { // from class: com.android.systemui.qs.tiles.-$$Lambda$OPReverseChargeTile$K8HXUXC9L3n36_CnlT4SSTbiB-U
                @Override // java.lang.Runnable
                public final void run() {
                    OPReverseChargeTile.this.lambda$handleUpdateState$0$OPReverseChargeTile();
                }
            });
        } else {
            booleanState.state = booleanState.value ? 2 : 1;
            this.mHandler.post(new Runnable(booleanState) { // from class: com.android.systemui.qs.tiles.-$$Lambda$OPReverseChargeTile$GOAuS5OHN9G6QYbzIYpoI-wl0uI
                public final /* synthetic */ QSTile.BooleanState f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    OPReverseChargeTile.this.lambda$handleUpdateState$1$OPReverseChargeTile(this.f$1);
                }
            });
        }
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleUpdateState$0 */
    public /* synthetic */ void lambda$handleUpdateState$0$OPReverseChargeTile() {
        OpReverseWirelessChargeUtils.showNotification(this.mContext, false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleUpdateState$1 */
    public /* synthetic */ void lambda$handleUpdateState$1$OPReverseChargeTile(QSTile.BooleanState booleanState) {
        OpReverseWirelessChargeUtils.showNotification(this.mContext, booleanState.value);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.OP_REVERSE_WIRELESS_CHARGING_SETTINGS");
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0015R$string.op_reverse_charge_tile_label);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        this.mModeSetting.setListening(z);
        if (z) {
            this.mDisableObserver.register();
            registerReceiver();
        } else {
            this.mDisableObserver.unregister();
            unRegisterReceiver();
        }
        if (z) {
            refreshState();
        }
        this.mHandler.removeMessages(100);
        this.mDebounce = false;
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    private void unRegisterReceiver() {
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        if (this.mCharging != z2) {
            String str = this.TAG;
            Log.d(str, "charging=" + this.mCharging + "->" + z2);
            this.mCharging = z2;
            refreshState();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getDisabledReason() {
        return Settings.System.getStringForUser(this.mContext.getContentResolver(), "reverse_wireless_disable_reason", 0);
    }

    private void popChargingToast() {
        Toast toast = this.mToast;
        if (toast != null) {
            toast.cancel();
        }
        Toast makeText = Toast.makeText(this.mContext, C0015R$string.op_reverse_charge_toast_charging, 1);
        this.mToast = makeText;
        makeText.show();
    }

    private void popLowBatteryToast() {
        Toast toast = this.mToast;
        if (toast != null) {
            toast.cancel();
        }
        Toast makeText = Toast.makeText(this.mContext, C0015R$string.op_reverse_charge_toast_low_battery, 1);
        this.mToast = makeText;
        makeText.show();
    }

    private void popOverHeatToast() {
        Toast toast = this.mToast;
        if (toast != null) {
            toast.cancel();
        }
        Toast makeText = Toast.makeText(this.mContext, C0015R$string.op_reverse_charge_toast_over_heat, 1);
        this.mToast = makeText;
        makeText.show();
    }

    private final class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        public void register() {
            ((QSTileImpl) OPReverseChargeTile.this).mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("reverse_wireless_disable_reason"), false, this);
            refresh();
        }

        public void unregister() {
            ((QSTileImpl) OPReverseChargeTile.this).mContext.getContentResolver().unregisterContentObserver(this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            super.onChange(z, uri);
            refresh();
            OPReverseChargeTile.this.refreshState();
        }

        private void refresh() {
            String disabledReason = OPReverseChargeTile.this.getDisabledReason();
            boolean equals = "low_power".equals(disabledReason);
            boolean equals2 = "temp_over_heat".equals(disabledReason);
            String str = ((QSTileImpl) OPReverseChargeTile.this).TAG;
            Log.d(str, "disable_reason=" + disabledReason + ", low_batt=" + OPReverseChargeTile.this.mLowBatt + "->" + equals + ", overheat=" + OPReverseChargeTile.this.mOverHeat + "->" + equals2);
            OPReverseChargeTile.this.mLowBatt = equals;
            OPReverseChargeTile.this.mOverHeat = equals2;
        }
    }
}
