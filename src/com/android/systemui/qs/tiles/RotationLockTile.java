package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.content.res.Resources;
import android.widget.Switch;
import androidx.appcompat.R$styleable;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.RotationLockController;
public class RotationLockTile extends QSTileImpl<QSTile.BooleanState> {
    private final RotationLockController.RotationLockControllerCallback mCallback;
    private final RotationLockController mController;
    private final QSTile.Icon mIcon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_ic_qs_auto_rotate);

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return R$styleable.AppCompatTheme_windowFixedWidthMinor;
    }

    public RotationLockTile(QSHost qSHost, RotationLockController rotationLockController) {
        super(qSHost);
        AnonymousClass1 r1 = new RotationLockController.RotationLockControllerCallback() { // from class: com.android.systemui.qs.tiles.RotationLockTile.1
            @Override // com.android.systemui.statusbar.policy.RotationLockController.RotationLockControllerCallback
            public void onRotationLockStateChanged(boolean z, boolean z2) {
                RotationLockTile.this.refreshState(Boolean.valueOf(z));
            }
        };
        this.mCallback = r1;
        this.mController = rotationLockController;
        rotationLockController.observe(this, r1);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.DISPLAY_SETTINGS");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        boolean z = !((QSTile.BooleanState) this.mState).value;
        this.mController.setRotationLocked(!z);
        refreshState(Boolean.valueOf(z));
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return getState().label;
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean isRotationLocked = this.mController.isRotationLocked();
        booleanState.value = !isRotationLocked;
        booleanState.label = this.mContext.getString(C0015R$string.quick_settings_rotation_unlocked_label);
        booleanState.icon = this.mIcon;
        booleanState.contentDescription = getAccessibilityString(isRotationLocked);
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
        booleanState.state = booleanState.value ? 2 : 1;
    }

    public static boolean isCurrentOrientationLockPortrait(RotationLockController rotationLockController, Resources resources) {
        int rotationLockOrientation = rotationLockController.getRotationLockOrientation();
        return rotationLockOrientation == 0 ? resources.getConfiguration().orientation != 2 : rotationLockOrientation != 2;
    }

    private String getAccessibilityString(boolean z) {
        return this.mContext.getString(C0015R$string.accessibility_quick_settings_rotation);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        return getAccessibilityString(((QSTile.BooleanState) this.mState).value);
    }
}
