package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
public class OtgTile extends QSTileImpl<QSTile.BooleanState> {
    private static final Intent OTG_SETTINGS = new Intent("oneplus.intent.action.OTG_SETTINGS");
    private BroadcastReceiver mOTGBReceiver = new BroadcastReceiver() { // from class: com.android.systemui.qs.tiles.OtgTile.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("oneplus.intent.action.otg_auto_shutdown".equals(intent.getAction())) {
                OtgTile.this.refreshState();
            }
        }
    };
    private boolean mRegistered = false;
    UsbManager mUsbManager = ((UsbManager) this.mContext.getSystemService("usb"));

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 415;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return true;
    }

    public OtgTile(QSHost qSHost) {
        super(qSHost);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        if (z) {
            if (!this.mRegistered) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("oneplus.intent.action.otg_auto_shutdown");
                this.mContext.registerReceiver(this.mOTGBReceiver, intentFilter);
                this.mRegistered = true;
            }
        } else if (this.mRegistered) {
            this.mContext.unregisterReceiver(this.mOTGBReceiver);
            this.mRegistered = false;
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return OTG_SETTINGS;
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0015R$string.quick_settings_otg_label);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        setOtgEnabled(!isOtgEnabled());
        refreshState();
    }

    private void setOtgEnabled(boolean z) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "oneplus_otg_auto_disable", z ? 1 : 0);
        try {
            UsbManager.class.getMethod("setOtgEnabled", Boolean.TYPE).invoke(this.mUsbManager, Boolean.valueOf(z));
        } catch (Exception e) {
            Log.e(this.TAG, "Cannot setOtgEnabled", e);
        }
    }

    private boolean isOtgEnabled() {
        return SystemProperties.getBoolean("persist.sys.oem.otg_support", false);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.value = isOtgEnabled();
        booleanState.icon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_ic_qs_otg);
        booleanState.label = this.mContext.getString(C0015R$string.quick_settings_otg_label);
        booleanState.contentDescription = this.mContext.getString(C0015R$string.quick_settings_otg_label);
        booleanState.state = booleanState.value ? 2 : 1;
    }
}
