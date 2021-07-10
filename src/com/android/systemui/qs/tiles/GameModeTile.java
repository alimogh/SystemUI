package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.oneplus.util.SystemSetting;
public class GameModeTile extends QSTileImpl<QSTile.BooleanState> {
    private SystemSetting mEsportModeSetting = new SystemSetting(this.mContext, null, "esport_mode_enabled", true) { // from class: com.android.systemui.qs.tiles.GameModeTile.2
        /* access modifiers changed from: protected */
        @Override // com.oneplus.util.SystemSetting
        public void handleValueChanged(int i, boolean z) {
            String str = ((QSTileImpl) GameModeTile.this).TAG;
            Log.d(str, "handleValueChanged: ESPORT_MODE_STATUS=" + i);
            GameModeTile.this.refreshState();
        }
    };
    private Handler mHandler = new Handler();
    private long mLastUpdateNavBarTime = 0;
    private SystemSetting mModeSetting = new SystemSetting(this.mContext, null, "game_mode_status", true) { // from class: com.android.systemui.qs.tiles.GameModeTile.1
        /* access modifiers changed from: protected */
        @Override // com.oneplus.util.SystemSetting
        public void handleValueChanged(int i, boolean z) {
            String str = ((QSTileImpl) GameModeTile.this).TAG;
            Log.d(str, "handleValueChanged: GAME_MODE_STATUS=" + i);
            GameModeTile.this.refreshState();
        }
    };

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 2000;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return false;
    }

    public GameModeTile(QSHost qSHost) {
        super(qSHost);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        boolean z = !((QSTile.BooleanState) this.mState).value;
        MetricsLogger.action(this.mContext, getMetricsCategory(), z);
        setEnabled(z);
    }

    private void setEnabled(final boolean z) {
        long uptimeMillis = 700 - (SystemClock.uptimeMillis() - this.mLastUpdateNavBarTime);
        if (uptimeMillis < 0) {
            uptimeMillis = 0;
        }
        this.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.qs.tiles.GameModeTile.3
            @Override // java.lang.Runnable
            public void run() {
                Settings.System.putStringForUser(((QSTileImpl) GameModeTile.this).mContext.getContentResolver(), "game_mode_status_manual", z ? "force-on" : "force-off", -2);
            }
        }, uptimeMillis);
        this.mLastUpdateNavBarTime = SystemClock.uptimeMillis();
    }

    private boolean isEnabled() {
        return this.mModeSetting.getValue() != 0;
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
        booleanState.label = this.mContext.getString(C0015R$string.quick_settings_game_mode_label);
        booleanState.secondaryLabel = "";
        booleanState.icon = QSTileImpl.ResourceIcon.get(C0006R$drawable.ic_qs_game_mode_on);
        Context context = this.mContext;
        if (booleanState.value) {
            i = C0015R$string.quick_settings_game_mode_summary_on;
        } else {
            i = C0015R$string.quick_settings_game_mode_summary_off;
        }
        booleanState.contentDescription = context.getString(i);
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
        if (booleanState.value) {
            i2 = 2;
        }
        booleanState.state = i2;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.OP_GAMING_MODE_SETTINGS");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        this.mModeSetting.setListening(z);
        this.mEsportModeSetting.setListening(z);
        if (z) {
            refreshState();
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0015R$string.quick_settings_game_mode_label);
    }
}
