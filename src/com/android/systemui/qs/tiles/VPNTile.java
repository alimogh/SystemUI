package com.android.systemui.qs.tiles;

import android.content.Intent;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.SecurityController;
public class VPNTile extends QSTileImpl<QSTile.BooleanState> {
    private static final Intent VPN_SETTINGS = new Intent("android.net.vpn.SETTINGS");
    private final Callback mCallback = new Callback();
    private SecurityController mSecurityController = ((SecurityController) Dependency.get(SecurityController.class));

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 2003;
    }

    public VPNTile(QSHost qSHost) {
        super(qSHost);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        if (z) {
            this.mSecurityController.addCallback(this.mCallback);
        } else {
            this.mSecurityController.removeCallback(this.mCallback);
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return VPN_SETTINGS;
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0015R$string.legacy_vpn_name);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        ((ActivityStarter) Dependency.get(ActivityStarter.class)).postStartActivityDismissingKeyguard(VPN_SETTINGS, 0);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.value = this.mSecurityController.isVpnEnabled();
        booleanState.icon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_ic_qs_vpn);
        booleanState.label = this.mContext.getString(C0015R$string.legacy_vpn_name);
        booleanState.state = booleanState.value ? 2 : 1;
        booleanState.contentDescription = this.mContext.getString(C0015R$string.legacy_vpn_name);
    }

    private class Callback implements SecurityController.SecurityControllerCallback {
        private Callback() {
        }

        @Override // com.android.systemui.statusbar.policy.SecurityController.SecurityControllerCallback
        public void onStateChanged() {
            VPNTile.this.refreshState();
        }
    }
}
