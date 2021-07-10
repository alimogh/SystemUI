package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.hardware.display.ColorDisplayManager;
import android.hardware.display.NightDisplayListener;
import android.metrics.LogMaker;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.SecureSetting;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.LocationController;
import com.oneplus.util.SystemSetting;
import java.text.DateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.TimeZone;
public class NightDisplayTile extends QSTileImpl<QSTile.BooleanState> implements NightDisplayListener.Callback {
    private SecureSetting mDaltonizerSetting = new SecureSetting(this.mContext, null, "accessibility_display_daltonizer_enabled") { // from class: com.android.systemui.qs.tiles.NightDisplayTile.2
        /* access modifiers changed from: protected */
        @Override // com.android.systemui.qs.SecureSetting
        public void handleValueChanged(int i, boolean z) {
            NightDisplayTile.this.refreshState();
        }
    };
    private SystemSetting mGrayModeSetting = new SystemSetting(this.mContext, null, "accessibility_display_grayscale_enabled", true) { // from class: com.android.systemui.qs.tiles.NightDisplayTile.3
        /* access modifiers changed from: protected */
        @Override // com.oneplus.util.SystemSetting
        public void handleValueChanged(int i, boolean z) {
            NightDisplayTile.this.refreshState();
        }
    };
    private SecureSetting mInversionSetting = new SecureSetting(this.mContext, null, "accessibility_display_inversion_enabled") { // from class: com.android.systemui.qs.tiles.NightDisplayTile.1
        /* access modifiers changed from: protected */
        @Override // com.android.systemui.qs.SecureSetting
        public void handleValueChanged(int i, boolean z) {
            NightDisplayTile.this.refreshState();
        }
    };
    private boolean mIsListening;
    private NightDisplayListener mListener = new NightDisplayListener(this.mContext, new Handler(Looper.myLooper()));
    private final LocationController mLocationController;
    private final ColorDisplayManager mManager = ((ColorDisplayManager) this.mContext.getSystemService(ColorDisplayManager.class));

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 491;
    }

    public NightDisplayTile(QSHost qSHost, LocationController locationController) {
        super(qSHost);
        this.mLocationController = locationController;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return ColorDisplayManager.isNightDisplayAvailable(this.mContext);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if (isColorCalibrationAvailable()) {
            if ("1".equals(Settings.Global.getString(this.mContext.getContentResolver(), "night_display_forced_auto_mode_available")) && this.mManager.getNightDisplayAutoModeRaw() == -1) {
                this.mManager.setNightDisplayAutoMode(1);
                Log.i("NightDisplayTile", "Enrolled in forced night display auto mode");
            }
            this.mManager.setNightDisplayActivated(!((QSTile.BooleanState) this.mState).value);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUserSwitch(int i) {
        if (this.mIsListening) {
            this.mListener.setCallback((NightDisplayListener.Callback) null);
        }
        NightDisplayListener nightDisplayListener = new NightDisplayListener(this.mContext, i, new Handler(Looper.myLooper()));
        this.mListener = nightDisplayListener;
        if (this.mIsListening) {
            nightDisplayListener.setCallback(this);
        }
        this.mInversionSetting.setUserId(i);
        this.mDaltonizerSetting.setUserId(i);
        super.handleUserSwitch(i);
    }

    private boolean isGrayEnabled() {
        return this.mGrayModeSetting.getValue(1) == 0;
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
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.value = this.mManager.isNightDisplayActivated();
        booleanState.label = this.mContext.getString(C0015R$string.op_quick_settings_night_display_label);
        booleanState.icon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_ic_qs_night_display_on);
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
        if (!isColorCalibrationAvailable()) {
            booleanState.state = 0;
        } else {
            booleanState.state = booleanState.value ? 2 : 1;
        }
        booleanState.secondaryLabel = getSecondaryLabel(booleanState.value);
        booleanState.contentDescription = booleanState.label;
    }

    private String getSecondaryLabel(boolean z) {
        LocalTime localTime;
        int i;
        int nightDisplayAutoMode = this.mManager.getNightDisplayAutoMode();
        if (nightDisplayAutoMode == 1) {
            if (z) {
                localTime = this.mManager.getNightDisplayCustomEndTime();
                i = C0015R$string.quick_settings_secondary_label_until;
            } else {
                localTime = this.mManager.getNightDisplayCustomStartTime();
                i = C0015R$string.quick_settings_night_secondary_label_on_at;
            }
            Calendar instance = Calendar.getInstance();
            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(this.mContext);
            timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            instance.setTimeZone(timeFormat.getTimeZone());
            instance.set(11, localTime.getHour());
            instance.set(12, localTime.getMinute());
            instance.set(13, 0);
            instance.set(14, 0);
            return this.mContext.getString(i, timeFormat.format(instance.getTime()));
        } else if (nightDisplayAutoMode != 2 || !this.mLocationController.isLocationEnabled()) {
            return null;
        } else {
            if (z) {
                return this.mContext.getString(C0015R$string.quick_settings_night_secondary_label_until_sunrise);
            }
            return this.mContext.getString(C0015R$string.quick_settings_night_secondary_label_on_at_sunset);
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public LogMaker populate(LogMaker logMaker) {
        return super.populate(logMaker).addTaggedData(1311, Integer.valueOf(this.mManager.getNightDisplayAutoModeRaw()));
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.NIGHT_DISPLAY_SETTINGS");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        this.mIsListening = z;
        this.mInversionSetting.setListening(z);
        this.mDaltonizerSetting.setListening(z);
        this.mGrayModeSetting.setListening(z);
        if (z) {
            this.mListener.setCallback(this);
            refreshState();
            return;
        }
        this.mListener.setCallback((NightDisplayListener.Callback) null);
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0015R$string.op_quick_settings_night_display_label);
    }

    public void onActivated(boolean z) {
        refreshState();
    }
}
