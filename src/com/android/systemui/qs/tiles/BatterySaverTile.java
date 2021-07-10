package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Switch;
import androidx.lifecycle.Lifecycle;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.utils.StringUtil;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.SecureSetting;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.BatteryController;
import com.oneplus.util.OpUtils;
public class BatterySaverTile extends QSTileImpl<QSTile.BooleanState> implements BatteryController.BatteryStateChangeCallback {
    private final BatteryController mBatteryController;
    private QSTile.Icon mIcon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_ic_qs_battery_saver);
    private boolean mPluggedIn;
    private boolean mPowerSave;
    @VisibleForTesting
    protected final SecureSetting mSetting;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 261;
    }

    public BatterySaverTile(QSHost qSHost, BatteryController batteryController) {
        super(qSHost);
        this.mBatteryController = batteryController;
        batteryController.observe(getLifecycle(), (Lifecycle) this);
        this.mSetting = new SecureSetting(this.mContext, this.mHandler, "low_power_warning_acknowledged", qSHost.getUserContext().getUserId()) { // from class: com.android.systemui.qs.tiles.BatterySaverTile.1
            /* access modifiers changed from: protected */
            @Override // com.android.systemui.qs.SecureSetting
            public void handleValueChanged(int i, boolean z) {
                BatterySaverTile.this.handleRefreshState(null);
            }
        };
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleDestroy() {
        super.handleDestroy();
        this.mSetting.setListening(false);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUserSwitch(int i) {
        this.mSetting.setUserId(i);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        this.mSetting.setListening(z);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.BATTERY_SAVER_SETTINGS");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if (getState().state != 0) {
            this.mBatteryController.setPowerSaveMode(!this.mPowerSave);
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0015R$string.battery_detail_switch_title);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        int i;
        Log.d("BatterySaverTile", "handleUpdateState, " + this.mPluggedIn + ", " + this.mPowerSave);
        if (this.mPluggedIn) {
            i = 0;
        } else {
            i = this.mPowerSave ? 2 : 1;
        }
        booleanState.state = i;
        booleanState.icon = this.mIcon;
        String string = this.mContext.getString(C0015R$string.battery_detail_switch_title);
        booleanState.label = string;
        booleanState.contentDescription = string;
        booleanState.value = this.mPowerSave;
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
        booleanState.showRippleEffect = this.mSetting.getValue() == 0;
        String str = null;
        if (this.mPowerSave) {
            long batteryTimeRemaining = OpUtils.getBatteryTimeRemaining(this.mContext);
            if (batteryTimeRemaining > 0) {
                Context context = this.mContext;
                str = context.getString(C0015R$string.battery_usage_remaining_time, StringUtil.formatElapsedTime(context, (double) batteryTimeRemaining, false));
            }
        }
        booleanState.secondaryLabel = str;
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        this.mPluggedIn = z;
        refreshState(Integer.valueOf(i));
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onPowerSaveChanged(boolean z) {
        Log.d("BatterySaverTile", "onPowerSaveChanged, " + z);
        this.mPowerSave = z;
        refreshState(null);
    }
}
