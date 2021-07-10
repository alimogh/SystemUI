package com.android.systemui.qs.tiles;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.SecureSetting;
import com.android.systemui.qs.tileimpl.QSTileImpl;
public class OPDndCarModeTile extends QSTileImpl<QSTile.BooleanState> {
    private GlobalSetting mCarModeDndSetting;
    private SecureSetting mCarModeSetting;
    private int mCarModeStatus;
    private boolean mListening;

    private boolean isEnabled() {
        return false;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 2004;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return false;
    }

    public OPDndCarModeTile(QSHost qSHost) {
        super(qSHost);
        this.mCarModeStatus = 1;
        this.mCarModeSetting = new SecureSetting(this.mContext, this.mHandler, "oneplus_carmode_switch") { // from class: com.android.systemui.qs.tiles.OPDndCarModeTile.1
            /* access modifiers changed from: protected */
            @Override // com.android.systemui.qs.SecureSetting
            public void handleValueChanged(int i, boolean z) {
                Log.i("OPDndCarModeTile", "CAR_MODE_STATUS  value:" + i + " / observedChange:" + z);
                OPDndCarModeTile.this.mCarModeStatus = i;
                OPDndCarModeTile.this.changeTile();
            }
        };
        this.mCarModeDndSetting = new GlobalSetting(this.mContext, this.mHandler, "zen_mode_car") { // from class: com.android.systemui.qs.tiles.OPDndCarModeTile.2
            /* access modifiers changed from: protected */
            @Override // com.android.systemui.qs.GlobalSetting
            public void handleValueChanged(int i) {
                Log.i("OPDndCarModeTile", "ZEN_MODE_CAR  value:" + i);
                OPDndCarModeTile.this.refreshState();
            }
        };
        this.mCarModeStatus = this.mCarModeSetting.getValue();
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean changeTile() {
        if (this.mCarModeStatus != 0) {
            return false;
        }
        handleDestroy();
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleDestroy() {
        this.mCarModeSetting.setListening(false);
        this.mCarModeDndSetting.setListening(false);
        super.handleDestroy();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent().setComponent(new ComponentName("com.oneplus.carmode", "com.oneplus.carmode.activity.SettingActivity"));
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        int i = !((QSTile.BooleanState) this.mState).value ? 1 : 0;
        Log.d("OPDndCarModeTile", "user clicked dnd: " + i);
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0015R$string.quick_settings_op_car_mode_dnd_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        int i;
        booleanState.value = isEnabled();
        if (booleanState.slash == null) {
            booleanState.slash = new QSTile.SlashState();
        }
        int i2 = 1;
        booleanState.slash.isSlashed = !booleanState.value;
        booleanState.label = this.mContext.getString(C0015R$string.quick_settings_op_car_mode_dnd_label);
        booleanState.icon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_carmode_dnd_qs_icon);
        Context context = this.mContext;
        if (booleanState.value) {
            i = C0015R$string.quick_settings_op_car_mode_dnd_summary_on;
        } else {
            i = C0015R$string.quick_settings_op_car_mode_dnd_summary_off;
        }
        booleanState.contentDescription = context.getString(i);
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
        if (booleanState.value) {
            i2 = 2;
        }
        booleanState.state = i2;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        if (this.mListening != z) {
            this.mListening = z;
            if (z) {
                this.mCarModeSetting.setListening(true);
                this.mCarModeDndSetting.setListening(true);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUserSwitch(int i) {
        this.mCarModeSetting.setUserId(i);
        super.handleUserSwitch(i);
    }
}
