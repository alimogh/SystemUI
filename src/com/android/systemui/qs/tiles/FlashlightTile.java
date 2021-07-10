package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Switch;
import androidx.appcompat.R$styleable;
import androidx.lifecycle.Lifecycle;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.SysUIToast;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.FlashlightController;
public class FlashlightTile extends QSTileImpl<QSTile.BooleanState> implements FlashlightController.FlashlightListener {
    private final FlashlightController mFlashlightController;
    private final QSTile.Icon mIcon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_ic_qs_flashlight);
    private final GlobalSetting mOverHeatMode;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return R$styleable.AppCompatTheme_windowActionModeOverlay;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUserSwitch(int i) {
    }

    public FlashlightTile(QSHost qSHost, FlashlightController flashlightController) {
        super(qSHost);
        this.mFlashlightController = flashlightController;
        flashlightController.observe(getLifecycle(), (Lifecycle) this);
        this.mOverHeatMode = new GlobalSetting(this, this.mContext, null, "op_overheat_temperature_type") { // from class: com.android.systemui.qs.tiles.FlashlightTile.1
            /* access modifiers changed from: protected */
            @Override // com.android.systemui.qs.GlobalSetting
            public void handleValueChanged(int i) {
            }
        };
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
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        this.mOverHeatMode.setListening(z);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.media.action.STILL_IMAGE_CAMERA");
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return this.mFlashlightController.hasFlashlight();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if (!ActivityManager.isUserAMonkey() && this.mFlashlightController.isAvailable()) {
            boolean z = !((QSTile.BooleanState) this.mState).value;
            if (z && this.mOverHeatMode.getValue() != 0) {
                Context context = this.mContext;
                SysUIToast.makeText(context, context.getString(C0015R$string.overheat_toast_content), 1).show();
            }
            refreshState(Boolean.valueOf(z));
            this.mFlashlightController.setFlashlight(z);
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0015R$string.quick_settings_flashlight_label);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleLongClick() {
        handleClick();
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        if (booleanState.slash == null) {
            booleanState.slash = new QSTile.SlashState();
        }
        booleanState.label = this.mHost.getContext().getString(C0015R$string.quick_settings_flashlight_label);
        booleanState.secondaryLabel = "";
        booleanState.stateDescription = "";
        int i = 1;
        if (!this.mFlashlightController.isAvailable()) {
            booleanState.icon = this.mIcon;
            booleanState.slash.isSlashed = true;
            String string = this.mContext.getString(C0015R$string.quick_settings_flashlight_camera_in_use);
            booleanState.secondaryLabel = string;
            booleanState.stateDescription = string;
            booleanState.state = 0;
            return;
        }
        if (obj instanceof Boolean) {
            boolean booleanValue = ((Boolean) obj).booleanValue();
            if (booleanValue != booleanState.value) {
                booleanState.value = booleanValue;
            } else {
                return;
            }
        } else {
            booleanState.value = this.mFlashlightController.isEnabled();
        }
        booleanState.icon = this.mIcon;
        booleanState.slash.isSlashed = !booleanState.value;
        booleanState.contentDescription = this.mContext.getString(C0015R$string.quick_settings_flashlight_label);
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
        if (booleanState.value) {
            i = 2;
        }
        booleanState.state = i;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(C0015R$string.accessibility_quick_settings_flashlight_changed_on);
        }
        return this.mContext.getString(C0015R$string.accessibility_quick_settings_flashlight_changed_off);
    }

    @Override // com.android.systemui.statusbar.policy.FlashlightController.FlashlightListener
    public void onFlashlightChanged(boolean z) {
        refreshState(Boolean.valueOf(z));
    }

    @Override // com.android.systemui.statusbar.policy.FlashlightController.FlashlightListener
    public void onFlashlightError() {
        refreshState(Boolean.FALSE);
    }

    @Override // com.android.systemui.statusbar.policy.FlashlightController.FlashlightListener
    public void onFlashlightAvailabilityChanged(boolean z) {
        refreshState();
    }
}
