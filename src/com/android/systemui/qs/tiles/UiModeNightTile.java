package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.app.UiModeManager;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Switch;
import androidx.lifecycle.Lifecycle;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.LocationController;
import com.oneplus.util.OpUtils;
import com.oneplus.util.SystemSetting;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
public class UiModeNightTile extends QSTileImpl<QSTile.BooleanState> implements ConfigurationController.ConfigurationListener, BatteryController.BatteryStateChangeCallback {
    private static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
    private final BatteryController mBatteryController;
    private final QSTile.Icon mIcon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_qs_dark_mode);
    private long mLastClickMillis = 0;
    private final LocationController mLocationController;
    private SystemSetting mSpecialThemeSetting;
    private final UiModeManager mUiModeManager;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 1706;
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onPowerSaveChanged(boolean z) {
    }

    public UiModeNightTile(QSHost qSHost, ConfigurationController configurationController, BatteryController batteryController, LocationController locationController) {
        super(qSHost);
        this.mBatteryController = batteryController;
        this.mUiModeManager = (UiModeManager) this.mContext.getSystemService(UiModeManager.class);
        this.mLocationController = locationController;
        configurationController.observe(getLifecycle(), (Lifecycle) this);
        batteryController.observe(getLifecycle(), (Lifecycle) this);
        this.mSpecialThemeSetting = new SystemSetting(this.mContext, null, "oem_special_theme", true) { // from class: com.android.systemui.qs.tiles.UiModeNightTile.1
            /* access modifiers changed from: protected */
            @Override // com.oneplus.util.SystemSetting
            public void handleValueChanged(int i, boolean z) {
                String str = ((QSTileImpl) UiModeNightTile.this).TAG;
                Log.d(str, "handleValueChanged:special_theme=" + i);
                UiModeNightTile.this.refreshState();
            }
        };
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onUiModeChanged() {
        refreshState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if (getState().state != 0) {
            boolean z = !((QSTile.BooleanState) this.mState).value;
            long uptimeMillis = SystemClock.uptimeMillis();
            if (uptimeMillis - this.mLastClickMillis < 1000) {
                Log.d(this.TAG, "rapid click detected, ignore.");
                return;
            }
            this.mLastClickMillis = uptimeMillis;
            this.mHandler.post(new Runnable(z) { // from class: com.android.systemui.qs.tiles.-$$Lambda$UiModeNightTile$I_e7LDdl69wSZsmxfdEzJT96L8I
                public final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    UiModeNightTile.this.lambda$handleClick$0$UiModeNightTile(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleClick$0 */
    public /* synthetic */ void lambda$handleClick$0$UiModeNightTile(boolean z) {
        toggleDarkMode(z);
        refreshState(Boolean.valueOf(z));
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0074: APUT  (r7v0 java.lang.Object[]), (0 ??[int, short, byte, char]), (r10v9 java.lang.String) */
    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        CharSequence charSequence;
        LocalTime localTime;
        int i;
        int i2;
        int nightMode = this.mUiModeManager.getNightMode();
        this.mBatteryController.isPowerSave();
        int i3 = 1;
        boolean z = (this.mContext.getResources().getConfiguration().uiMode & 48) == 32;
        if (nightMode == 0 && this.mLocationController.isLocationEnabled()) {
            Resources resources = this.mContext.getResources();
            if (z) {
                i2 = C0015R$string.quick_settings_dark_mode_secondary_label_until_sunrise;
            } else {
                i2 = C0015R$string.quick_settings_dark_mode_secondary_label_on_at_sunset;
            }
            booleanState.secondaryLabel = resources.getString(i2);
        } else if (nightMode == 3) {
            boolean is24HourFormat = DateFormat.is24HourFormat(this.mContext);
            if (z) {
                localTime = this.mUiModeManager.getCustomNightModeEnd();
            } else {
                localTime = this.mUiModeManager.getCustomNightModeStart();
            }
            Resources resources2 = this.mContext.getResources();
            if (z) {
                i = C0015R$string.quick_settings_dark_mode_secondary_label_until;
            } else {
                i = C0015R$string.quick_settings_dark_mode_secondary_label_on_at;
            }
            Object[] objArr = new Object[1];
            objArr[0] = is24HourFormat ? localTime.toString() : formatter.format(localTime);
            booleanState.secondaryLabel = resources2.getString(i, objArr);
        } else {
            booleanState.secondaryLabel = null;
        }
        booleanState.value = z;
        booleanState.label = this.mContext.getString(C0015R$string.op_qs_dark_mode_tile_label);
        booleanState.icon = this.mIcon;
        if (TextUtils.isEmpty(booleanState.secondaryLabel)) {
            charSequence = booleanState.label;
        } else {
            charSequence = TextUtils.concat(booleanState.label, ", ", booleanState.secondaryLabel);
        }
        booleanState.contentDescription = charSequence;
        if (OpUtils.isREDVersion()) {
            booleanState.state = 0;
        } else {
            if (booleanState.value) {
                i3 = 2;
            }
            booleanState.state = i3;
        }
        booleanState.showRippleEffect = false;
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.DARK_THEME_SETTINGS");
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return getState().label;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        int currentUser = ActivityManager.getCurrentUser();
        if (DEBUG) {
            String str = this.TAG;
            Log.d(str, "isAvailable: uid=" + currentUser);
        }
        return currentUser == 0;
    }

    private void toggleDarkMode(boolean z) {
        if (DEBUG) {
            String str = this.TAG;
            Log.d(str, "toggleDarkMode(" + z + "), 1");
        }
        updateOneplusTheme(z);
        if (DEBUG) {
            String str2 = this.TAG;
            Log.d(str2, "toggleDarkMode(" + z + "), 2");
        }
        this.mUiModeManager.setNightModeActivated(z);
        if (DEBUG) {
            String str3 = this.TAG;
            Log.d(str3, "toggleDarkMode(" + z + "), 3");
        }
    }

    private void updateOneplusTheme(boolean z) {
        if (z) {
            savePreviousOneplusLightTheme();
            applyOneplusTheme(1);
            return;
        }
        applyOneplusTheme(getLastLightThemeColor());
    }

    private void savePreviousOneplusLightTheme() {
        int themeColor = OpUtils.getThemeColor(this.mContext);
        if (themeColor == 0 || themeColor == 2) {
            Settings.System.putInt(this.mContext.getContentResolver(), "oem_black_mode_last_light", themeColor);
        }
    }

    private int getLastLightThemeColor() {
        return Settings.System.getInt(this.mContext.getContentResolver(), "oem_black_mode_last_light", 0);
    }

    private void applyOneplusTheme(int i) {
        SystemProperties.set("persist.sys.theme.status", String.valueOf(i));
        Settings.System.putInt(this.mContext.getContentResolver(), "oem_black_mode", i);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        this.mSpecialThemeSetting.setListening(z);
        if (z) {
            refreshState();
        }
    }
}
