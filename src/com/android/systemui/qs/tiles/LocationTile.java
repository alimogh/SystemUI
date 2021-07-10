package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.widget.Switch;
import androidx.appcompat.R$styleable;
import androidx.lifecycle.LifecycleOwner;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.LocationController;
public class LocationTile extends QSTileImpl<QSTile.BooleanState> {
    private final ActivityStarter mActivityStarter;
    private final Callback mCallback;
    private final LocationController mController;
    private final QSTile.Icon mIcon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_ic_qs_location);
    private final KeyguardStateController mKeyguard;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return R$styleable.AppCompatTheme_windowFixedWidthMajor;
    }

    public LocationTile(QSHost qSHost, LocationController locationController, KeyguardStateController keyguardStateController, ActivityStarter activityStarter) {
        super(qSHost);
        Callback callback = new Callback();
        this.mCallback = callback;
        this.mController = locationController;
        this.mKeyguard = keyguardStateController;
        this.mActivityStarter = activityStarter;
        locationController.observe((LifecycleOwner) this, (LocationTile) callback);
        this.mKeyguard.observe((LifecycleOwner) this, (LocationTile) this.mCallback);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.LOCATION_SOURCE_SETTINGS");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if (!this.mKeyguard.isMethodSecure() || !this.mKeyguard.isShowing()) {
            this.mController.setLocationEnabled(!((QSTile.BooleanState) this.mState).value);
            return;
        }
        this.mActivityStarter.postQSRunnableDismissingKeyguard(new Runnable() { // from class: com.android.systemui.qs.tiles.-$$Lambda$LocationTile$cnlxD4jGztrpcRYGbQTKRSm3Ng0
            @Override // java.lang.Runnable
            public final void run() {
                LocationTile.this.lambda$handleClick$0$LocationTile();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleClick$0 */
    public /* synthetic */ void lambda$handleClick$0$LocationTile() {
        boolean z = ((QSTile.BooleanState) this.mState).value;
        this.mHost.openPanels();
        this.mController.setLocationEnabled(!z);
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0015R$string.quick_settings_location_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        if (booleanState.slash == null) {
            booleanState.slash = new QSTile.SlashState();
        }
        booleanState.value = this.mController.isLocationEnabled();
        checkIfRestrictionEnforcedByAdminOnly(booleanState, "no_share_location");
        if (!booleanState.disabledByPolicy) {
            checkIfRestrictionEnforcedByAdminOnly(booleanState, "no_config_location");
        }
        booleanState.icon = this.mIcon;
        int i = 1;
        booleanState.slash.isSlashed = !booleanState.value;
        String string = this.mContext.getString(C0015R$string.quick_settings_location_label);
        booleanState.label = string;
        booleanState.contentDescription = string;
        if (booleanState.value) {
            i = 2;
        }
        booleanState.state = i;
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(C0015R$string.accessibility_quick_settings_location_changed_on);
        }
        return this.mContext.getString(C0015R$string.accessibility_quick_settings_location_changed_off);
    }

    private final class Callback implements LocationController.LocationChangeCallback, KeyguardStateController.Callback {
        private Callback() {
        }

        @Override // com.android.systemui.statusbar.policy.LocationController.LocationChangeCallback
        public void onLocationSettingsChanged(boolean z) {
            LocationTile.this.refreshState();
        }

        @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
        public void onKeyguardShowingChanged() {
            LocationTile.this.refreshState();
        }
    }
}
