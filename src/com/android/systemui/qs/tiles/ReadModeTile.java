package com.android.systemui.qs.tiles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.util.OpFeatures;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.C0016R$style;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.SecureSetting;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.oneplus.systemui.util.OpMdmLogger;
import com.oneplus.util.SystemSetting;
import com.oneplus.util.ThemeColorUtils;
public class ReadModeTile extends QSTileImpl<QSTile.BooleanState> {
    private SecureSetting mDaltonizerSetting = new SecureSetting(this.mContext, null, "accessibility_display_daltonizer_enabled") { // from class: com.android.systemui.qs.tiles.ReadModeTile.3
        /* access modifiers changed from: protected */
        @Override // com.android.systemui.qs.SecureSetting
        public void handleValueChanged(int i, boolean z) {
            ReadModeTile.this.refreshState();
        }
    };
    private SystemSetting mDefaultSetting = new SystemSetting(this.mContext, null, "reading_mode_option_manual", true) { // from class: com.android.systemui.qs.tiles.ReadModeTile.5
        /* access modifiers changed from: protected */
        @Override // com.oneplus.util.SystemSetting
        public void handleValueChanged(int i, boolean z) {
            ReadModeTile.this.refreshState();
        }
    };
    private SystemSetting mGrayModeSetting = new SystemSetting(this.mContext, null, "accessibility_display_grayscale_enabled", true) { // from class: com.android.systemui.qs.tiles.ReadModeTile.4
        /* access modifiers changed from: protected */
        @Override // com.oneplus.util.SystemSetting
        public void handleValueChanged(int i, boolean z) {
            ReadModeTile.this.refreshState();
        }
    };
    private SecureSetting mInversionSetting = new SecureSetting(this.mContext, null, "accessibility_display_inversion_enabled") { // from class: com.android.systemui.qs.tiles.ReadModeTile.2
        /* access modifiers changed from: protected */
        @Override // com.android.systemui.qs.SecureSetting
        public void handleValueChanged(int i, boolean z) {
            ReadModeTile.this.refreshState();
        }
    };
    private boolean mIsSupportColorMode = OpFeatures.isSupport(new int[]{210});
    private SystemSetting mModeSetting = new SystemSetting(this.mContext, null, "reading_mode_status", true) { // from class: com.android.systemui.qs.tiles.ReadModeTile.1
        /* access modifiers changed from: protected */
        @Override // com.oneplus.util.SystemSetting
        public void handleValueChanged(int i, boolean z) {
            String str = ((QSTileImpl) ReadModeTile.this).TAG;
            Log.d(str, "handleValueChanged:READ_MODE_STATUS=" + i);
            ReadModeTile.this.refreshState();
        }
    };

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 2001;
    }

    public ReadModeTile(QSHost qSHost) {
        super(qSHost);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return this.mContext.getPackageManager().hasSystemFeature("oem.read_mode.support");
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if (isColorCalibrationAvailable()) {
            boolean z = !((QSTile.BooleanState) this.mState).value;
            MetricsLogger.action(this.mContext, getMetricsCategory(), z);
            String str = "force-on";
            if (!this.mIsSupportColorMode) {
                if (QSTileImpl.DEBUG) {
                    Log.d(this.TAG, "legacy read mode clicked.");
                }
                if (!z) {
                    str = "force-off";
                }
                setEnabled(str);
            } else if (isEnabled()) {
                if (QSTileImpl.DEBUG) {
                    Log.d(this.TAG, "off by user.");
                }
                setEnabled("force-off");
            } else {
                int value = this.mDefaultSetting.getValue();
                if (QSTileImpl.DEBUG) {
                    Log.d(this.TAG, "option=" + value);
                }
                if (value == 1) {
                    setEnabled(str);
                } else if (value != 2) {
                    popDialog();
                } else {
                    setEnabled("force-on-color");
                }
            }
        }
    }

    private void setEnabled(String str) {
        Settings.System.putStringForUser(this.mContext.getContentResolver(), "reading_mode_status_manual", str, -2);
    }

    private boolean isEnabled() {
        return this.mModeSetting.getValue() != 0;
    }

    private boolean isBlackEnabled() {
        return this.mModeSetting.getValue() == 1;
    }

    private boolean isColorEnabled() {
        return this.mModeSetting.getValue() == 2;
    }

    private boolean isGrayEnabled() {
        return this.mGrayModeSetting.getValue(1) == 0;
    }

    private void popDialog() {
        int i;
        CharSequence[] charSequenceArr = {this.mContext.getString(C0015R$string.op_read_mode2_dialog_color), this.mContext.getString(C0015R$string.op_read_mode2_dialog_black)};
        if (ThemeColorUtils.getCurrentTheme() == 1) {
            i = C0016R$style.oneplus_theme_dialog_dark;
        } else {
            i = C0016R$style.oneplus_theme_dialog_light;
        }
        AlertDialog create = new AlertDialog.Builder(this.mContext, i).setTitle(C0015R$string.op_read_mode2_dialog_title).setItems(charSequenceArr, new DialogInterface.OnClickListener() { // from class: com.android.systemui.qs.tiles.-$$Lambda$ReadModeTile$in6zYK0Fb8g8gTkm5jCFznPCRvw
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i2) {
                ReadModeTile.this.lambda$popDialog$0$ReadModeTile(dialogInterface, i2);
            }
        }).setNegativeButton(C0015R$string.cancel, new DialogInterface.OnClickListener() { // from class: com.android.systemui.qs.tiles.-$$Lambda$ReadModeTile$4gqVt6jbv_huC305Rm5UTrtM7yA
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i2) {
                ReadModeTile.this.lambda$popDialog$1$ReadModeTile(dialogInterface, i2);
            }
        }).create();
        create.getWindow().setType(2009);
        SystemUIDialog.setShowForAllUsers(create, true);
        SystemUIDialog.registerDismissListener(create);
        SystemUIDialog.setWindowOnTop(create);
        create.show();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$popDialog$0 */
    public /* synthetic */ void lambda$popDialog$0$ReadModeTile(DialogInterface dialogInterface, int i) {
        String str = this.TAG;
        Log.d(str, "select: " + i);
        if (i == 0) {
            OpMdmLogger.log("sel_effect", "clr", "1");
            setEnabled("force-on-color");
        } else if (i == 1) {
            OpMdmLogger.log("sel_effect", "bw", "1");
            setEnabled("force-on");
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$popDialog$1 */
    public /* synthetic */ void lambda$popDialog$1$ReadModeTile(DialogInterface dialogInterface, int i) {
        Log.d(this.TAG, "user cancel");
        dialogInterface.dismiss();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleLongClick() {
        if (isColorCalibrationAvailable()) {
            super.handleLongClick();
        }
    }

    private boolean isColorCalibrationAvailable() {
        return !(this.mInversionSetting.getValue() == 1) && !(this.mDaltonizerSetting.getValue() == 1) && !isGrayEnabled();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUserSwitch(int i) {
        this.mInversionSetting.setUserId(i);
        this.mDaltonizerSetting.setUserId(i);
        refreshState();
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        int i;
        booleanState.value = isEnabled();
        String str = this.TAG;
        Log.d(str, "handleUpdateState:state.value=" + booleanState.value);
        if (booleanState.slash == null) {
            booleanState.slash = new QSTile.SlashState();
        }
        int i2 = 1;
        booleanState.slash.isSlashed = !booleanState.value;
        booleanState.label = this.mContext.getString(C0015R$string.quick_settings_read_mode_label);
        booleanState.icon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_ic_qs_read_mode);
        Context context = this.mContext;
        if (booleanState.value) {
            i = C0015R$string.quick_settings_read_mode_summary_on;
        } else {
            i = C0015R$string.quick_settings_read_mode_summary_off;
        }
        booleanState.contentDescription = context.getString(i);
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
        if (!isColorCalibrationAvailable()) {
            booleanState.state = 0;
        } else {
            if (booleanState.value) {
                i2 = 2;
            }
            booleanState.state = i2;
        }
        if (!this.mIsSupportColorMode || !booleanState.value) {
            booleanState.secondaryLabel = "";
        } else if (isColorEnabled()) {
            booleanState.secondaryLabel = this.mContext.getString(C0015R$string.op_read_mode2_dialog_color);
        } else if (isBlackEnabled()) {
            booleanState.secondaryLabel = this.mContext.getString(C0015R$string.op_read_mode2_dialog_black);
        } else {
            booleanState.secondaryLabel = "";
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.OP_READING_MODE_SETTINGS");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        this.mModeSetting.setListening(z);
        this.mInversionSetting.setListening(z);
        this.mDaltonizerSetting.setListening(z);
        this.mGrayModeSetting.setListening(z);
        if (this.mIsSupportColorMode) {
            this.mDefaultSetting.setListening(z);
        }
        if (z) {
            refreshState();
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0015R$string.quick_settings_read_mode_label);
    }
}
