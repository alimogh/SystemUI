package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.sysprop.TelephonyProperties;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Switch;
import com.android.ims.ImsManager;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.util.ProductUtils;
public class AirplaneModeTile extends QSTileImpl<QSTile.BooleanState> {
    private SharedPreferences airplanePref;
    private final ActivityStarter mActivityStarter;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final QSTile.Icon mIcon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_ic_qs_airplane);
    private boolean mListening;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.android.systemui.qs.tiles.AirplaneModeTile.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.AIRPLANE_MODE".equals(intent.getAction())) {
                AirplaneModeTile.this.refreshState();
            }
        }
    };
    private final GlobalSetting mSetting;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 112;
    }

    public AirplaneModeTile(QSHost qSHost, ActivityStarter activityStarter, BroadcastDispatcher broadcastDispatcher) {
        super(qSHost);
        this.mActivityStarter = activityStarter;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mSetting = new GlobalSetting(this.mContext, null, "airplane_mode_on") { // from class: com.android.systemui.qs.tiles.AirplaneModeTile.1
            /* access modifiers changed from: protected */
            @Override // com.android.systemui.qs.GlobalSetting
            public void handleValueChanged(int i) {
                AirplaneModeTile.this.handleRefreshState(Integer.valueOf(i));
            }
        };
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        QSTile.BooleanState booleanState = new QSTile.BooleanState();
        booleanState.lottiePrefix = "qs_airplane_tile";
        booleanState.lottieSupport = 63;
        return booleanState;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        boolean z = ((QSTile.BooleanState) this.mState).value;
        MetricsLogger.action(this.mContext, getMetricsCategory(), !z);
        if (z || !((Boolean) TelephonyProperties.in_ecm_mode().orElse(Boolean.FALSE)).booleanValue()) {
            SharedPreferences sharedPreferences = this.mContext.getSharedPreferences("OPPref_airplane", 0);
            this.airplanePref = sharedPreferences;
            boolean z2 = sharedPreferences.getBoolean("airplanechecked", false);
            if (!ProductUtils.isUsvMode() || z || z2) {
                setEnabled(!z);
            } else {
                showEnableDialog(!z);
            }
        } else {
            this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.telephony.action.SHOW_NOTICE_ECM_BLOCK_OTHERS"), 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setEnabled(boolean z) {
        ((ConnectivityManager) this.mContext.getSystemService("connectivity")).setAirplaneMode(z);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.AIRPLANE_MODE_SETTINGS");
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0015R$string.airplane_mode);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        checkIfRestrictionEnforcedByAdminOnly(booleanState, "no_airplane_mode");
        int i = 1;
        boolean z = (obj instanceof Integer ? ((Integer) obj).intValue() : this.mSetting.getValue()) != 0;
        booleanState.value = z;
        booleanState.label = this.mContext.getString(C0015R$string.airplane_mode);
        booleanState.icon = this.mIcon;
        if (booleanState.slash == null) {
            booleanState.slash = new QSTile.SlashState();
        }
        booleanState.slash.isSlashed = !z;
        if (z) {
            i = 2;
        }
        booleanState.state = i;
        booleanState.contentDescription = booleanState.label;
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(C0015R$string.accessibility_quick_settings_airplane_changed_on);
        }
        return this.mContext.getString(C0015R$string.accessibility_quick_settings_airplane_changed_off);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        if (this.mListening != z) {
            this.mListening = z;
            if (z) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
                this.mBroadcastDispatcher.registerReceiver(this.mReceiver, intentFilter);
            } else {
                this.mBroadcastDispatcher.unregisterReceiver(this.mReceiver);
            }
            this.mSetting.setListening(z);
        }
    }

    private void showEnableDialog(final boolean z) {
        String str;
        String string = this.mContext.getResources().getString(C0015R$string.airplane_mode);
        if (getVzwVolteOrWiFiCallingStatus()) {
            str = this.mContext.getResources().getString(C0015R$string.airplane_mode_enable_dialog_message_wifi_volte);
        } else {
            str = this.mContext.getResources().getString(C0015R$string.airplane_mode_enable_dialog_message);
        }
        View inflate = View.inflate(this.mContext, C0011R$layout.dialog_checkbox_airplanemode, null);
        final CheckBox checkBox = (CheckBox) inflate.findViewById(C0008R$id.checkbox);
        SystemUIDialog systemUIDialog = new SystemUIDialog(this.mContext);
        systemUIDialog.setTitle(string);
        systemUIDialog.setMessage(str);
        systemUIDialog.setPositiveButton(17039370, new DialogInterface.OnClickListener() { // from class: com.android.systemui.qs.tiles.AirplaneModeTile.3
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor edit = AirplaneModeTile.this.airplanePref.edit();
                edit.putBoolean("airplanechecked", checkBox.isChecked());
                edit.commit();
                AirplaneModeTile.this.setEnabled(z);
            }
        });
        systemUIDialog.setNegativeButton(17039360, null);
        systemUIDialog.setShowForAllUsers(true);
        systemUIDialog.setView(inflate);
        systemUIDialog.show();
    }

    private boolean getVzwVolteOrWiFiCallingStatus() {
        ImsManager instance = ImsManager.getInstance(this.mContext, SubscriptionManager.from(this.mContext).getDefaultDataPhoneId());
        boolean isWfcEnabledByUser = instance.isWfcEnabledByUser();
        boolean isEnhanced4gLteModeSettingEnabledByUser = instance.isEnhanced4gLteModeSettingEnabledByUser();
        String str = this.TAG;
        Log.d(str, "AirplaneModeTile: vzwWFC = " + isWfcEnabledByUser + " vzwVolte = " + isEnhanced4gLteModeSettingEnabledByUser);
        return isWfcEnabledByUser || isEnhanced4gLteModeSettingEnabledByUser;
    }
}
