package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import androidx.appcompat.R$styleable;
import androidx.lifecycle.Lifecycle;
import com.android.ims.ImsManager;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.net.DataUsageController;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.SignalTileView;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.util.ProductUtils;
public class CellularTile extends QSTileImpl<QSTile.SignalState> {
    static final Intent CELLULAR_SETTINGS = new Intent("com.oneplus.security.action.USAGE_DATA_SUMMARY");
    private final ActivityStarter mActivityStarter;
    private final NetworkController mController;
    private final DataUsageController mDataController;
    private final CellularDetailAdapter mDetailAdapter;
    private final CellSignalCallback mSignalCallback = new CellSignalCallback();
    private SubscriptionManager mSubscriptionManager = null;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 115;
    }

    public CellularTile(QSHost qSHost, NetworkController networkController, ActivityStarter activityStarter) {
        super(qSHost);
        this.mController = networkController;
        this.mActivityStarter = activityStarter;
        this.mDataController = networkController.getMobileDataController();
        this.mDetailAdapter = new CellularDetailAdapter();
        this.mController.observe(getLifecycle(), (Lifecycle) this.mSignalCallback);
        CELLULAR_SETTINGS.putExtra("tracker_event", 2);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.SignalState newTileState() {
        return new QSTile.SignalState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public QSIconView createTileView(Context context) {
        return new SignalTileView(context);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0043  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0065  */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.content.Intent getLongClickIntent() {
        /*
            r6 = this;
            java.lang.String r0 = "gsm.sim.state"
            java.lang.String r0 = android.os.SystemProperties.get(r0)
            r1 = 0
            java.lang.String r2 = ","
            java.lang.String[] r0 = android.text.TextUtils.split(r0, r2)     // Catch:{ Exception -> 0x0034 }
            r2 = r1
            r3 = r2
        L_0x000f:
            int r4 = r0.length     // Catch:{ Exception -> 0x0035 }
            if (r2 >= r4) goto L_0x003c
            r4 = r0[r2]     // Catch:{ Exception -> 0x0035 }
            boolean r4 = r4.isEmpty()     // Catch:{ Exception -> 0x0035 }
            if (r4 != 0) goto L_0x0031
            r4 = r0[r2]     // Catch:{ Exception -> 0x0035 }
            java.lang.String r5 = "ABSENT"
            boolean r4 = r4.equalsIgnoreCase(r5)     // Catch:{ Exception -> 0x0035 }
            if (r4 != 0) goto L_0x0031
            r4 = r0[r2]     // Catch:{ Exception -> 0x0035 }
            java.lang.String r5 = "NOT_READY"
            boolean r4 = r4.equalsIgnoreCase(r5)     // Catch:{ Exception -> 0x0035 }
            if (r4 == 0) goto L_0x002f
            goto L_0x0031
        L_0x002f:
            int r3 = r3 + 1
        L_0x0031:
            int r2 = r2 + 1
            goto L_0x000f
        L_0x0034:
            r3 = r1
        L_0x0035:
            java.lang.String r0 = r6.TAG
            java.lang.String r2 = "Error to parse sim state"
            android.util.Log.e(r0, r2)
        L_0x003c:
            android.content.Intent r0 = com.android.systemui.qs.tiles.CellularTile.CELLULAR_SETTINGS
            java.lang.String r2 = "select_tab"
            r4 = 1
            if (r3 <= r4) goto L_0x0065
            int r1 = r6.getDefaultDataSimIndex()
            r0.putExtra(r2, r1)
            java.lang.String r1 = r6.TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Go to selected sim tab="
            r2.append(r3)
            int r6 = r6.getDefaultDataSimIndex()
            r2.append(r6)
            java.lang.String r6 = r2.toString()
            android.util.Log.d(r1, r6)
            goto L_0x006f
        L_0x0065:
            r0.putExtra(r2, r1)
            java.lang.String r6 = r6.TAG
            java.lang.String r1 = "Go to sim tab 0"
            android.util.Log.d(r6, r1)
        L_0x006f:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.tiles.CellularTile.getLongClickIntent():android.content.Intent");
    }

    private int getDefaultDataSimIndex() {
        if (this.mSubscriptionManager == null) {
            this.mSubscriptionManager = SubscriptionManager.from(this.mContext);
        }
        return this.mSubscriptionManager.getDefaultDataPhoneId();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if (getState().state != 0) {
            if (!ProductUtils.isUsvMode() || !this.mDataController.isMobileDataEnabled()) {
                DataUsageController dataUsageController = this.mDataController;
                dataUsageController.setMobileDataEnabled(!dataUsageController.isMobileDataEnabled());
                return;
            }
            int i = 0;
            try {
                i = Settings.Secure.getInt(this.mContext.getContentResolver(), "is_video_call");
            } catch (Exception e) {
                String str = this.TAG;
                Log.d(str, "handleClick Exception" + e.getMessage());
            }
            showEnableDialog(i);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSecondaryClick() {
        if (this.mDataController.isMobileDataSupported()) {
            showDetail(true);
        } else {
            this.mActivityStarter.postStartActivityDismissingKeyguard(getCellularSettingIntent(), 0);
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0015R$string.quick_settings_cellular_detail_title);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.SignalState signalState, Object obj) {
        CallbackInfo callbackInfo = (CallbackInfo) obj;
        if (callbackInfo == null) {
            callbackInfo = this.mSignalCallback.mInfo;
        }
        signalState.label = this.mContext.getResources().getString(C0015R$string.mobile_data);
        boolean z = this.mDataController.isMobileDataSupported() && this.mDataController.isMobileDataEnabled();
        signalState.value = z;
        signalState.activityIn = z && callbackInfo.activityIn;
        signalState.activityOut = z && callbackInfo.activityOut;
        signalState.expandedAccessibilityClassName = Switch.class.getName();
        if (callbackInfo.noSim) {
            signalState.icon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_ic_qs_no_sim);
        } else {
            signalState.icon = QSTileImpl.ResourceIcon.get(C0006R$drawable.ic_swap_vert);
        }
        if (Build.DEBUG_ONEPLUS) {
            String str = this.TAG;
            Log.d(str, "handleUpdateState: supported=" + this.mDataController.isMobileDataSupported() + ", enabled=" + this.mDataController.isMobileDataEnabled() + ", noSim=" + callbackInfo.noSim + ", airplaneMode=" + callbackInfo.airplaneModeEnabled);
        }
        if (callbackInfo.noSim) {
            signalState.state = 0;
        } else if (callbackInfo.airplaneModeEnabled) {
            signalState.state = 0;
        } else if (z) {
            signalState.state = 2;
        } else {
            signalState.state = 1;
        }
        signalState.contentDescription = signalState.label;
        if (signalState.state == 1) {
            signalState.stateDescription = "";
        } else {
            signalState.stateDescription = signalState.secondaryLabel;
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return this.mController.hasMobileDataFeature();
    }

    /* access modifiers changed from: private */
    public static final class CallbackInfo {
        boolean activityIn;
        boolean activityOut;
        boolean airplaneModeEnabled;
        boolean noSim;
        boolean roaming;

        private CallbackInfo() {
        }
    }

    /* access modifiers changed from: private */
    public final class CellSignalCallback implements NetworkController.SignalCallback {
        private final CallbackInfo mInfo;

        private CellSignalCallback() {
            this.mInfo = new CallbackInfo();
        }

        @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setNoSims(boolean z, boolean z2) {
            CallbackInfo callbackInfo = this.mInfo;
            callbackInfo.noSim = z;
            CellularTile.this.refreshState(callbackInfo);
        }

        @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setIsAirplaneMode(NetworkController.IconState iconState) {
            CallbackInfo callbackInfo = this.mInfo;
            callbackInfo.airplaneModeEnabled = iconState.visible;
            CellularTile.this.refreshState(callbackInfo);
        }

        @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setMobileDataEnabled(boolean z) {
            CellularTile.this.mDetailAdapter.setMobileDataEnabled(z);
        }
    }

    static Intent getCellularSettingIntent() {
        Intent intent = new Intent("android.settings.NETWORK_OPERATOR_SETTINGS");
        if (SubscriptionManager.getDefaultDataSubscriptionId() != -1) {
            intent.putExtra("android.provider.extra.SUB_ID", SubscriptionManager.getDefaultDataSubscriptionId());
        }
        return intent;
    }

    /* access modifiers changed from: private */
    public final class CellularDetailAdapter implements DetailAdapter {
        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public int getMetricsCategory() {
            return R$styleable.AppCompatTheme_windowActionBar;
        }

        private CellularDetailAdapter() {
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public CharSequence getTitle() {
            return ((QSTileImpl) CellularTile.this).mContext.getString(C0015R$string.quick_settings_cellular_detail_title);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Boolean getToggleState() {
            if (CellularTile.this.mDataController.isMobileDataSupported()) {
                return Boolean.valueOf(CellularTile.this.mDataController.isMobileDataEnabled());
            }
            return null;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Intent getSettingsIntent() {
            return CellularTile.CELLULAR_SETTINGS;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public void setToggleState(boolean z) {
            MetricsLogger.action(((QSTileImpl) CellularTile.this).mContext, 155, z);
            CellularTile.this.mDataController.setMobileDataEnabled(z);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            int i = 0;
            if (view == null) {
                view = LayoutInflater.from(((QSTileImpl) CellularTile.this).mContext).inflate(C0011R$layout.data_usage, viewGroup, false);
            }
            DataUsageDetailView dataUsageDetailView = (DataUsageDetailView) view;
            DataUsageController.DataUsageInfo dataUsageInfo = CellularTile.this.mDataController.getDataUsageInfo();
            if (dataUsageInfo == null) {
                return dataUsageDetailView;
            }
            dataUsageDetailView.bind(dataUsageInfo);
            View findViewById = dataUsageDetailView.findViewById(C0008R$id.roaming_text);
            if (!CellularTile.this.mSignalCallback.mInfo.roaming) {
                i = 4;
            }
            findViewById.setVisibility(i);
            return dataUsageDetailView;
        }

        public void setMobileDataEnabled(boolean z) {
            CellularTile.this.fireToggleStateChanged(z);
        }
    }

    private void showEnableDialog(int i) {
        int i2;
        String str;
        String str2;
        int i3 = 17039360;
        if (i == 1) {
            str = this.mContext.getResources().getString(C0015R$string.mobile_data_off_alert_video_call);
            i3 = C0015R$string.continue_call;
            i2 = C0015R$string.end_call;
        } else {
            if (getVzwVolteAndNoWiFiCallingStatus()) {
                str2 = this.mContext.getResources().getString(C0015R$string.mobile_data_off_alert_support_volte_no_wfc);
            } else {
                str2 = this.mContext.getResources().getString(C0015R$string.mobile_data_off_alert);
            }
            i2 = 17039370;
            str = str2;
        }
        SystemUIDialog systemUIDialog = new SystemUIDialog(this.mContext);
        systemUIDialog.setTitle(this.mContext.getResources().getString(C0015R$string.mobile_data));
        systemUIDialog.setMessage(str);
        systemUIDialog.setPositiveButton(i2, new DialogInterface.OnClickListener(i) { // from class: com.android.systemui.qs.tiles.-$$Lambda$CellularTile$ftDFwqD-2jEH_znO0fdOkcRe1Pw
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i4) {
                CellularTile.this.lambda$showEnableDialog$1$CellularTile(this.f$1, dialogInterface, i4);
            }
        });
        systemUIDialog.setNegativeButton(i3, null);
        systemUIDialog.setShowForAllUsers(true);
        systemUIDialog.show();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showEnableDialog$1 */
    public /* synthetic */ void lambda$showEnableDialog$1$CellularTile(int i, DialogInterface dialogInterface, int i2) {
        this.mDataController.setMobileDataEnabled(false);
        if (i == 1) {
            this.mContext.sendBroadcast(new Intent("com.android.dialer.action.END_CALL"));
        }
    }

    private boolean getVzwVolteAndNoWiFiCallingStatus() {
        ImsManager instance = ImsManager.getInstance(this.mContext, SubscriptionManager.from(this.mContext).getDefaultDataPhoneId());
        return !instance.isWfcEnabledByUser() && instance.isEnhanced4gLteModeSettingEnabledByUser();
    }
}
