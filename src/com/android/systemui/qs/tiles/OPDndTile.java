package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.util.Log;
import androidx.appcompat.R$styleable;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.oneplus.opzenmode.OpZenModeController;
public class OPDndTile extends QSTileImpl<QSTile.BooleanState> implements OpZenModeController.Callback {
    private static final Intent ZEN_PRIORITY_SETTINGS = new Intent("android.settings.ZEN_MODE_PRIORITY_SETTINGS");
    private boolean isDedEnable = false;
    private final QSTile.Icon mIcon = QSTileImpl.ResourceIcon.get(C0006R$drawable.ic_qs_dnd_on);
    private boolean mListening;
    private OpZenModeController mOPZenModeController = ((OpZenModeController) Dependency.get(OpZenModeController.class));

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public DetailAdapter getDetailAdapter() {
        return null;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return R$styleable.AppCompatTheme_windowActionBarOverlay;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSecondaryClick() {
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return false;
    }

    static {
        new Intent("android.settings.ZEN_MODE_SETTINGS");
        QSTileImpl.ResourceIcon.get(C0006R$drawable.ic_qs_dnd_on_total_silence);
    }

    public OPDndTile(QSHost qSHost) {
        super(qSHost);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleDestroy() {
        super.handleDestroy();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return ZEN_PRIORITY_SETTINGS;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        String str = this.TAG;
        Log.i(str, "handleClick:" + isDndEnabled());
        boolean isDndEnabled = isDndEnabled() ^ true;
        this.isDedEnable = isDndEnabled;
        this.mOPZenModeController.setDndEnable(isDndEnabled);
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0015R$string.quick_settings_dnd_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        if (this.mOPZenModeController == null) {
            Log.w(this.TAG, "mOPZenModeController is empty!!");
            return;
        }
        booleanState.state = this.isDedEnable ? 2 : 1;
        booleanState.value = this.isDedEnable;
        CharSequence tileLabel = getTileLabel();
        booleanState.label = tileLabel;
        booleanState.icon = this.mIcon;
        booleanState.contentDescription = tileLabel;
        String str = this.TAG;
        Log.w(str, "handleUpdateState state:" + booleanState);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(C0015R$string.accessibility_quick_settings_dnd_changed_on);
        }
        return this.mContext.getString(C0015R$string.accessibility_quick_settings_dnd_changed_off);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        if (this.mListening != z) {
            this.mListening = z;
            if (z) {
                this.mOPZenModeController.addCallback(this);
                refreshState();
                return;
            }
            this.mOPZenModeController.removeCallback(this);
        }
    }

    private boolean isDndEnabled() {
        return this.mOPZenModeController.getDndEnable();
    }

    @Override // com.oneplus.opzenmode.OpZenModeController.Callback
    public void onDndChanged(boolean z) {
        this.isDedEnable = isDndEnabled();
        refreshState();
    }

    @Override // com.oneplus.opzenmode.OpZenModeController.Callback
    public void onThreeKeyStatus(int i) {
        refreshState();
    }
}
