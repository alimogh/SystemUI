package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkPolicyManager;
import android.util.Log;
import android.widget.Switch;
import androidx.lifecycle.Lifecycle;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.Prefs;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.util.ProductUtils;
public class DataSaverTile extends QSTileImpl<QSTile.BooleanState> implements DataSaverController.Listener {
    private static final Intent DATA_SAVER_SETTINGS = new Intent("com.oneplus.action.DATAUSAGE_SAVER");
    private final DataSaverController mDataSaverController;
    private int mPcoState = -1;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.android.systemui.qs.tiles.DataSaverTile.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            DataSaverTile.this.mPcoState = intent.getIntExtra("pcoState", -1);
            Log.d("DataSaverTile", "PCO value:" + String.valueOf(DataSaverTile.this.mPcoState));
            if ("oneplus.intent.action.PCO_STATE_CHANGED".equals(action) && ProductUtils.isUsvMode()) {
                if (DataSaverTile.this.mPcoState == 2 || DataSaverTile.this.mPcoState == 3) {
                    DataSaverTile.this.setRestrictBackground(true);
                } else if (DataSaverTile.this.mPcoState == 0) {
                    DataSaverTile.this.setRestrictBackground(false);
                } else {
                    DataSaverTile.this.refreshState();
                }
            }
        }
    };
    private boolean mReceiverRegistered = false;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 284;
    }

    public DataSaverTile(QSHost qSHost, NetworkController networkController) {
        super(qSHost);
        DataSaverController dataSaverController = networkController.getDataSaverController();
        this.mDataSaverController = dataSaverController;
        dataSaverController.observe(getLifecycle(), (Lifecycle) this);
        if (ProductUtils.isUsvMode()) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("oneplus.intent.action.PCO_STATE_CHANGED");
            this.mContext.registerReceiver(this.mReceiver, intentFilter);
            this.mReceiverRegistered = true;
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return ActivityManager.getCurrentUser() == 0;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return DATA_SAVER_SETTINGS;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if (ProductUtils.isUsvMode() && getState().state == 0) {
            return;
        }
        if (((QSTile.BooleanState) this.mState).value || Prefs.getBoolean(this.mContext, "QsDataSaverDialogShown", false)) {
            toggleDataSaver();
            return;
        }
        SystemUIDialog systemUIDialog = new SystemUIDialog(this.mContext);
        systemUIDialog.setTitle(17040008);
        systemUIDialog.setMessage(17040006);
        systemUIDialog.setPositiveButton(17040007, new DialogInterface.OnClickListener() { // from class: com.android.systemui.qs.tiles.-$$Lambda$DataSaverTile$7vpE4nfIgph7ByTloh1_igU2EhI
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                DataSaverTile.this.lambda$handleClick$0$DataSaverTile(dialogInterface, i);
            }
        });
        systemUIDialog.setNegativeButton(17039360, null);
        systemUIDialog.setShowForAllUsers(true);
        systemUIDialog.show();
        Prefs.putBoolean(this.mContext, "QsDataSaverDialogShown", true);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleClick$0 */
    public /* synthetic */ void lambda$handleClick$0$DataSaverTile(DialogInterface dialogInterface, int i) {
        toggleDataSaver();
    }

    private void toggleDataSaver() {
        ((QSTile.BooleanState) this.mState).value = !this.mDataSaverController.isDataSaverEnabled();
        this.mDataSaverController.setDataSaverEnabled(((QSTile.BooleanState) this.mState).value);
        refreshState(Boolean.valueOf(((QSTile.BooleanState) this.mState).value));
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0015R$string.data_saver);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z;
        int i;
        if (obj instanceof Boolean) {
            z = ((Boolean) obj).booleanValue();
        } else {
            z = this.mDataSaverController.isDataSaverEnabled();
        }
        booleanState.value = z;
        int i2 = 2;
        if (!ProductUtils.isUsvMode() || !((i = this.mPcoState) == 2 || i == 3)) {
            if (!booleanState.value) {
                i2 = 1;
            }
            booleanState.state = i2;
        } else {
            booleanState.state = 0;
        }
        String string = this.mContext.getString(C0015R$string.data_saver);
        booleanState.label = string;
        booleanState.contentDescription = string;
        booleanState.icon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_ic_qs_data_saver);
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(C0015R$string.accessibility_quick_settings_data_saver_changed_on);
        }
        return this.mContext.getString(C0015R$string.accessibility_quick_settings_data_saver_changed_off);
    }

    @Override // com.android.systemui.statusbar.policy.DataSaverController.Listener
    public void onDataSaverChanged(boolean z) {
        refreshState(Boolean.valueOf(z));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRestrictBackground(boolean z) {
        NetworkPolicyManager from = NetworkPolicyManager.from(this.mContext);
        if (from != null) {
            from.setRestrictBackground(z);
            Log.d("DataSaverTile", "isRestrick" + String.valueOf(z));
        }
        this.mDataSaverController.setDataSaverEnabled(false);
        ((QSTile.BooleanState) this.mState).value = false;
        refreshState(Boolean.FALSE);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleDestroy() {
        super.handleDestroy();
        if (this.mReceiverRegistered) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mReceiverRegistered = false;
        }
    }
}
