package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
public class NfcTile extends QSTileImpl<QSTile.BooleanState> {
    private NfcAdapter mAdapter;
    private BroadcastDispatcher mBroadcastDispatcher;
    private final QSTile.Icon mIcon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_ic_qs_nfc_enabled);
    private boolean mListening;
    private BroadcastReceiver mNfcReceiver = new BroadcastReceiver() { // from class: com.android.systemui.qs.tiles.NfcTile.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            NfcTile.this.refreshState();
        }
    };

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 800;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUserSwitch(int i) {
    }

    public NfcTile(QSHost qSHost, BroadcastDispatcher broadcastDispatcher) {
        super(qSHost);
        this.mBroadcastDispatcher = broadcastDispatcher;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        this.mListening = z;
        if (z) {
            this.mBroadcastDispatcher.registerReceiver(this.mNfcReceiver, new IntentFilter("android.nfc.action.ADAPTER_STATE_CHANGED"));
        } else {
            this.mBroadcastDispatcher.unregisterReceiver(this.mNfcReceiver);
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.nfc");
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.NFC_SETTINGS");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if (getAdapter() != null) {
            if (!getAdapter().isEnabled()) {
                if (Build.DEBUG_ONEPLUS) {
                    Log.d(this.TAG, "Calling nfc enable.");
                }
                getAdapter().enable();
                return;
            }
            if (Build.DEBUG_ONEPLUS) {
                Log.d(this.TAG, "Calling nfc disable.");
            }
            getAdapter().disable();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSecondaryClick() {
        handleClick();
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0015R$string.quick_settings_nfc_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        int i = 1;
        booleanState.value = getAdapter() != null && getAdapter().isEnabled();
        if (getAdapter() == null) {
            i = 0;
        } else if (booleanState.value) {
            i = 2;
        }
        booleanState.state = i;
        booleanState.icon = this.mIcon;
        booleanState.label = this.mContext.getString(C0015R$string.quick_settings_nfc_label);
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
        booleanState.contentDescription = booleanState.label;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(C0015R$string.quick_settings_nfc_on);
        }
        return this.mContext.getString(C0015R$string.quick_settings_nfc_off);
    }

    private NfcAdapter getAdapter() {
        if (this.mAdapter == null) {
            try {
                this.mAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
            } catch (UnsupportedOperationException unused) {
                this.mAdapter = null;
            }
        }
        return this.mAdapter;
    }
}
