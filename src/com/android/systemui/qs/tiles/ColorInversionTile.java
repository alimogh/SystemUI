package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.widget.Switch;
import androidx.appcompat.R$styleable;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.SysUIToast;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.SecureSetting;
import com.android.systemui.qs.tileimpl.QSTileImpl;
public class ColorInversionTile extends QSTileImpl<QSTile.BooleanState> {
    private final QSTile.Icon mIcon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_ic_qs_invert_colors);
    private final SecureSetting mSetting = new SecureSetting(this.mContext, null, "accessibility_display_inversion_enabled") { // from class: com.android.systemui.qs.tiles.ColorInversionTile.1
        /* access modifiers changed from: protected */
        @Override // com.android.systemui.qs.SecureSetting
        public void handleValueChanged(int i, boolean z) {
            ColorInversionTile.this.handleRefreshState(Integer.valueOf(i));
        }
    };

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return R$styleable.AppCompatTheme_viewInflaterClass;
    }

    public ColorInversionTile(QSHost qSHost) {
        super(qSHost);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleDestroy() {
        super.handleDestroy();
        this.mSetting.setListening(false);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        this.mSetting.setListening(z);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUserSwitch(int i) {
        this.mSetting.setUserId(i);
        handleRefreshState(Integer.valueOf(this.mSetting.getValue()));
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.ACCESSIBILITY_SETTINGS");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if (!((QSTile.BooleanState) this.mState).value) {
            Context context = this.mContext;
            SysUIToast.makeText(context, context.getString(C0015R$string.oneplus_screen_features_not_available_toast), 1).show();
        }
        this.mSetting.setValue(!((QSTile.BooleanState) this.mState).value ? 1 : 0);
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0015R$string.quick_settings_inversion_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        int i = 1;
        boolean z = (obj instanceof Integer ? ((Integer) obj).intValue() : this.mSetting.getValue()) != 0;
        if (booleanState.slash == null) {
            booleanState.slash = new QSTile.SlashState();
        }
        booleanState.value = z;
        booleanState.slash.isSlashed = !z;
        if (z) {
            i = 2;
        }
        booleanState.state = i;
        booleanState.label = this.mContext.getString(C0015R$string.quick_settings_inversion_label);
        booleanState.icon = this.mIcon;
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
        booleanState.contentDescription = booleanState.label;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(C0015R$string.accessibility_quick_settings_color_inversion_changed_on);
        }
        return this.mContext.getString(C0015R$string.accessibility_quick_settings_color_inversion_changed_off);
    }
}
