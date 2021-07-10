package com.android.systemui.qs.tiles;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import androidx.appcompat.R$styleable;
import androidx.lifecycle.LifecycleOwner;
import com.android.settingslib.WirelessUtils;
import com.android.settingslib.net.DataUsageController;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0013R$plurals;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.SysUIToast;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.android.systemui.util.ProductUtils;
import com.oneplus.systemui.util.OpTetherUtils;
import com.oneplus.util.OpUtils;
import com.verizon.loginenginesvc.clientsdk.MhsAuthorizedClient;
public class HotspotTile extends QSTileImpl<QSTile.BooleanState> {
    private static final boolean DEBUG_ONEPLUS = Build.DEBUG_ONEPLUS;
    private WifiConfiguration config;
    private MhsAuthorizedClient.ICallback mCallBack = new MhsAuthorizedClient.ICallback() { // from class: com.android.systemui.qs.tiles.HotspotTile.3
        @Override // com.verizon.loginenginesvc.clientsdk.MhsAuthorizedClient.ICallback
        public void onSuccess(boolean z) {
            Log.e("mhs", "Is SU MHS Allowed == " + z);
            HotspotTile.this.dismissDialog();
            if (z && (!WirelessUtils.isAirplaneModeOn(((QSTileImpl) HotspotTile.this).mContext))) {
                HotspotTile.this.refreshState(QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING);
                HotspotTile.this.mHotspotController.setHotspotEnabled(true);
                if (Settings.System.getInt(((QSTileImpl) HotspotTile.this).mContext.getContentResolver(), "op_mhi_alert_on_dont_show_again", 0) == 0) {
                    HotspotTile hotspotTile = HotspotTile.this;
                    hotspotTile.mIsAuthorizationClient = 1;
                    hotspotTile.mWifiManager = (WifiManager) ((QSTileImpl) hotspotTile).mContext.getSystemService("wifi");
                    HotspotTile hotspotTile2 = HotspotTile.this;
                    hotspotTile2.config = hotspotTile2.mWifiManager.getWifiApConfiguration();
                    HotspotTile hotspotTile3 = HotspotTile.this;
                    hotspotTile3.showMHSErrorDialog(((QSTileImpl) hotspotTile3).mContext.getString(C0015R$string.mobile_hotspot), ((QSTileImpl) HotspotTile.this).mContext.getString(C0015R$string.hotspot_tip_message, HotspotTile.this.config.SSID, HotspotTile.this.config.preSharedKey));
                    return;
                }
                return;
            }
            HotspotTile hotspotTile4 = HotspotTile.this;
            hotspotTile4.mIsAuthorizationClient = 0;
            hotspotTile4.showMHSErrorDialog(((QSTileImpl) hotspotTile4).mContext.getString(C0015R$string.mobile_hotspot), ((QSTileImpl) HotspotTile.this).mContext.getString(C0015R$string.mobile_hotspot_authoration_error));
        }

        @Override // com.verizon.loginenginesvc.clientsdk.MhsAuthorizedClient.ICallback
        public void onError(int i, String str) {
            Log.e("mhs", "MHS SU error == " + str);
            SysUIToast.makeText(((QSTileImpl) HotspotTile.this).mContext, ((QSTileImpl) HotspotTile.this).mContext.getString(C0015R$string.mhs_call_back_error), 1).show();
            HotspotTile.this.dismissDialog();
        }

        @Override // com.verizon.loginenginesvc.clientsdk.MhsAuthorizedClient.ICallback
        public void onTimeout() {
            Log.e("mhs", "MHS SU time out");
            SysUIToast.makeText(((QSTileImpl) HotspotTile.this).mContext, ((QSTileImpl) HotspotTile.this).mContext.getString(C0015R$string.mhs_call_back_error), 1).show();
            HotspotTile.this.dismissDialog();
        }
    };
    private final HotspotAndDataSaverCallbacks mCallbacks = new HotspotAndDataSaverCallbacks();
    MhsAuthorizedClient mClient;
    private final DataUsageController mDataController;
    private final DataSaverController mDataSaverController;
    private final QSTile.Icon mEnabledStatic = QSTileImpl.ResourceIcon.get(C0006R$drawable.ic_hotspot);
    private final HotspotController mHotspotController;
    int mIsAuthorizationClient = -1;
    private boolean mListening;
    private MobileDataContentObserver mMobileDataObserver;
    protected final NetworkController mNetworkController;
    private boolean mNoSimError = false;
    private boolean mOperatorDialogShowing = false;
    private final GlobalSetting mOverHeatMode;
    private boolean mReguireTileToGray = false;
    protected final HotspotSignalCallback mSignalCallback = new HotspotSignalCallback();
    private ProgressDialog mSimUnlockProgressDialog;
    private boolean mVirtualSimExist = false;
    private final QSTile.Icon mWifi4EnabledStatic = QSTileImpl.ResourceIcon.get(C0006R$drawable.ic_wifi_4_hotspot);
    private final QSTile.Icon mWifi5EnabledStatic = QSTileImpl.ResourceIcon.get(C0006R$drawable.ic_wifi_5_hotspot);
    private final QSTile.Icon mWifi6EnabledStatic = QSTileImpl.ResourceIcon.get(C0006R$drawable.ic_wifi_6_hotspot);
    private WifiManager mWifiManager;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return R$styleable.AppCompatTheme_windowFixedHeightMajor;
    }

    public HotspotTile(QSHost qSHost, HotspotController hotspotController, DataSaverController dataSaverController) {
        super(qSHost);
        this.mHotspotController = hotspotController;
        this.mDataSaverController = dataSaverController;
        hotspotController.observe((LifecycleOwner) this, (HotspotTile) this.mCallbacks);
        this.mDataSaverController.observe((LifecycleOwner) this, (HotspotTile) this.mCallbacks);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mNetworkController = (NetworkController) Dependency.get(NetworkController.class);
        this.mOverHeatMode = new GlobalSetting(this, this.mContext, null, "op_overheat_temperature_type") { // from class: com.android.systemui.qs.tiles.HotspotTile.1
            @Override // com.android.systemui.qs.GlobalSetting
            public void handleValueChanged(int i) {
            }
        };
        this.mDataController = this.mNetworkController.getMobileDataController();
        if (ProductUtils.isUsvMode()) {
            this.mMobileDataObserver = new MobileDataContentObserver();
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mobile_data"), false, this.mMobileDataObserver);
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return this.mHotspotController.isHotspotSupported() && !this.mHotspotController.isOperatorHotspotDisable();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleDestroy() {
        super.handleDestroy();
        if (ProductUtils.isUsvMode() && this.mMobileDataObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mMobileDataObserver);
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        if (this.mListening != z) {
            this.mListening = z;
            if (z) {
                this.mNetworkController.addCallback((NetworkController.SignalCallback) this.mSignalCallback);
                refreshState();
            } else {
                this.mNetworkController.removeCallback((NetworkController.SignalCallback) this.mSignalCallback);
                if (ProductUtils.isUsvMode()) {
                    this.mClient.cancelRequest();
                    dismissDialog();
                }
            }
            this.mOverHeatMode.setListening(z);
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        Intent intent = new Intent("android.settings.TETHER_SETTINGS");
        intent.putExtra("from_quick_setting", "1");
        return intent;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        Object obj;
        if (OpUtils.isUSS()) {
            if (((QSTile.BooleanState) this.mState).state != 0) {
                if (DEBUG_ONEPLUS) {
                    String str = this.TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("handleClick / mNoSimError");
                    sb.append(this.mNoSimError);
                    sb.append(" / !isHaveProfile():");
                    sb.append(!isHaveProfile());
                    Log.d(str, sb.toString());
                }
                if (!isOperatorValid()) {
                    Log.d(this.TAG, "!isOperatorValid() AlertDialog");
                    this.mHotspotController.setHotspotEnabled(false);
                    operatorAlertDialog();
                    refreshState();
                    return;
                }
            } else {
                return;
            }
        }
        if (ProductUtils.isUsvMode() && this.mSignalCallback.mCallbackInfo.airplaneModeEnabled && ((QSTile.BooleanState) this.mState).state == 1) {
            this.mIsAuthorizationClient = -1;
            showMHSErrorDialog(this.mContext.getString(C0015R$string.mobile_hotspot), this.mContext.getString(C0015R$string.mobile_hotspot_airplane_off_error));
        } else if (ProductUtils.isUsvMode() && !this.mDataController.isMobileDataEnabled() && ((QSTile.BooleanState) this.mState).state == 1) {
            this.mIsAuthorizationClient = -1;
            showMHSErrorDialog(this.mContext.getString(C0015R$string.mobile_hotspot), this.mContext.getString(C0015R$string.mobile_hotspot_data_off_error));
        } else if (ProductUtils.isUsvMode() && this.mSignalCallback.mCallbackInfo.airplaneModeEnabled) {
        } else {
            if (!ProductUtils.isUsvMode() || ((QSTile.BooleanState) this.mState).state != 1 || !isVerizonSim()) {
                boolean z = ((QSTile.BooleanState) this.mState).value;
                if (!z && this.mDataSaverController.isDataSaverEnabled()) {
                    return;
                }
                if (this.mVirtualSimExist) {
                    Log.d(this.TAG, "virtual sim exist. ignore click.");
                    return;
                }
                if (!z && this.mOverHeatMode.getValue() != 0) {
                    Context context = this.mContext;
                    SysUIToast.makeText(context, context.getString(C0015R$string.overheat_toast_content), 1).show();
                }
                if (z) {
                    obj = null;
                } else {
                    obj = QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING;
                }
                refreshState(obj);
                if (OpUtils.isUST() && OpTetherUtils.isWifiEnable(this.mContext) && !z) {
                    if (OpTetherUtils.isNeedShowDialog(this.mContext)) {
                        OpTetherUtils.showUstAlertDialog(this.mContext, null, true);
                    }
                    OpTetherUtils.disableTmoWifi(this.mContext);
                }
                this.mHotspotController.setHotspotEnabled(!z);
                return;
            }
            getMhsProgressDialog().show();
            MhsAuthorizedClient mhsAuthorizedClient = new MhsAuthorizedClient(this.mContext, this.mCallBack, Integer.valueOf(SubscriptionManager.getDefaultDataSubscriptionId()), Looper.getMainLooper());
            this.mClient = mhsAuthorizedClient;
            mhsAuthorizedClient.sendRequest(25000);
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0015R$string.quick_settings_hotspot_label);
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x0093 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00ab  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00b5  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00d7  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00ed  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x00f9  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0103  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0106  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x0118  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleUpdateState(com.android.systemui.plugins.qs.QSTile.BooleanState r10, java.lang.Object r11) {
        /*
        // Method dump skipped, instructions count: 391
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.tiles.HotspotTile.handleUpdateState(com.android.systemui.plugins.qs.QSTile$BooleanState, java.lang.Object):void");
    }

    private String getSecondaryLabel(boolean z, boolean z2, boolean z3, int i) {
        if (z2) {
            return this.mContext.getString(C0015R$string.quick_settings_hotspot_secondary_label_transient);
        }
        if (z3) {
            return this.mContext.getString(C0015R$string.quick_settings_hotspot_secondary_label_data_saver_enabled);
        }
        if (i > 0 && z) {
            return this.mContext.getResources().getQuantityString(C0013R$plurals.quick_settings_hotspot_secondary_label_num_devices, i, Integer.valueOf(i));
        }
        if (!ProductUtils.isUsvMode() || !this.mDataController.isMobileDataEnabled()) {
        }
        return null;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(C0015R$string.accessibility_quick_settings_hotspot_changed_on);
        }
        return this.mContext.getString(C0015R$string.accessibility_quick_settings_hotspot_changed_off);
    }

    /* access modifiers changed from: private */
    public final class HotspotAndDataSaverCallbacks implements HotspotController.Callback, DataSaverController.Listener {
        CallbackInfo mCallbackInfo;

        private HotspotAndDataSaverCallbacks() {
            HotspotTile.this = r1;
            this.mCallbackInfo = new CallbackInfo();
        }

        @Override // com.android.systemui.statusbar.policy.DataSaverController.Listener
        public void onDataSaverChanged(boolean z) {
            if (HotspotTile.DEBUG_ONEPLUS && this.mCallbackInfo.isDataSaverEnabled != z) {
                String str = ((QSTileImpl) HotspotTile.this).TAG;
                Log.d(str, "onDataSaverChanged: " + this.mCallbackInfo.isDataSaverEnabled + "->" + z);
            }
            CallbackInfo callbackInfo = this.mCallbackInfo;
            callbackInfo.isDataSaverEnabled = z;
            HotspotTile.this.refreshState(callbackInfo);
        }

        @Override // com.android.systemui.statusbar.policy.HotspotController.Callback
        public void onHotspotChanged(boolean z, int i) {
            if (HotspotTile.DEBUG_ONEPLUS) {
                CallbackInfo callbackInfo = this.mCallbackInfo;
                if (!(callbackInfo.isHotspotEnabled == z && callbackInfo.numConnectedDevices == i)) {
                    String str = ((QSTileImpl) HotspotTile.this).TAG;
                    Log.d(str, "onHotspotChanged: enabled=" + this.mCallbackInfo.isHotspotEnabled + "->" + z + ", numConnectedDevices=" + this.mCallbackInfo.numConnectedDevices + "->" + i);
                }
            }
            CallbackInfo callbackInfo2 = this.mCallbackInfo;
            callbackInfo2.isHotspotEnabled = z;
            callbackInfo2.numConnectedDevices = i;
            HotspotTile.this.refreshState(callbackInfo2);
        }

        @Override // com.android.systemui.statusbar.policy.HotspotController.Callback
        public void onHotspotAvailabilityChanged(boolean z) {
            if (!z) {
                Log.d(((QSTileImpl) HotspotTile.this).TAG, "Tile removed. Hotspot no longer available");
                ((QSTileImpl) HotspotTile.this).mHost.removeTile(HotspotTile.this.getTileSpec());
            }
        }
    }

    public static final class CallbackInfo {
        boolean airplaneModeEnabled;
        boolean isDataSaverEnabled;
        boolean isHotspotEnabled;
        boolean mobileDataEnabled;
        int numConnectedDevices;

        protected CallbackInfo() {
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("CallbackInfo[");
            sb.append("isHotspotEnabled=");
            sb.append(this.isHotspotEnabled);
            sb.append(",numConnectedDevices=");
            sb.append(this.numConnectedDevices);
            sb.append(",isDataSaverEnabled=");
            sb.append(this.isDataSaverEnabled);
            sb.append(",airplaneModeEnabled=");
            sb.append(ProductUtils.isUsvMode() ? Boolean.valueOf(this.airplaneModeEnabled) : "");
            sb.append(']');
            return sb.toString();
        }
    }

    /* access modifiers changed from: protected */
    public final class HotspotSignalCallback implements NetworkController.SignalCallback {
        CallbackInfo mCallbackInfo = new CallbackInfo();

        protected HotspotSignalCallback() {
            HotspotTile.this = r1;
        }

        @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setVirtualSimstate(int[] iArr) {
            boolean z = false;
            if (iArr != null && iArr.length > 0) {
                int length = iArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    } else if (iArr[i] == NetworkControllerImpl.SOFTSIM_ENABLE_PILOT) {
                        z = true;
                        break;
                    } else {
                        i++;
                    }
                }
            }
            Log.d(((QSTileImpl) HotspotTile.this).TAG, "virtual sim state change: " + HotspotTile.this.mVirtualSimExist + " to " + z);
            HotspotTile.this.mVirtualSimExist = z;
            HotspotTile.this.refreshState();
        }

        @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setIsAirplaneMode(NetworkController.IconState iconState) {
            if (ProductUtils.isUsvMode()) {
                CallbackInfo callbackInfo = this.mCallbackInfo;
                callbackInfo.airplaneModeEnabled = iconState.visible;
                HotspotTile.this.refreshState(callbackInfo);
            }
        }

        @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setHasAnySimReady(boolean z) {
            if (OpUtils.isUSS()) {
                if (HotspotTile.DEBUG_ONEPLUS) {
                    String str = ((QSTileImpl) HotspotTile.this).TAG;
                    Log.i(str, "setHasAnySimReady / simReady:" + z);
                }
                HotspotTile.this.mNoSimError = !z;
                if (z) {
                    HotspotTile.this.mReguireTileToGray = false;
                    HotspotTile.this.refreshState();
                } else if (!z) {
                    HotspotTile.this.mHotspotController.isHotspotEnabled();
                }
            }
        }
    }

    private boolean isHaveProfile() {
        if (!OpUtils.isSprintMccMnc(this.mContext)) {
            return true;
        }
        String mmcMnc = OpUtils.getMmcMnc(this.mContext, 1);
        if (mmcMnc == null || mmcMnc.length() == 0) {
            Log.i(this.TAG, "no mccmnc");
            return false;
        }
        Cursor query = this.mContext.getContentResolver().query(Telephony.Carriers.CONTENT_URI, new String[]{"apn"}, "type = ? and numeric = ? and user_visible != ? and name != ?", new String[]{"dun", mmcMnc, "0", "3G_HOT"}, null);
        if (query == null || query.getCount() <= 0 || !query.moveToFirst()) {
            query.close();
            return false;
        }
        query.close();
        return true;
    }

    private boolean isOperatorValid() {
        if (this.mHotspotController.isHotspotEnabled()) {
            return true;
        }
        if (this.mNoSimError || !isHaveProfile()) {
            return false;
        }
        return true;
    }

    private void operatorAlertDialog() {
        int i;
        int i2;
        int i3;
        if (!this.mOperatorDialogShowing) {
            if (this.mNoSimError) {
                i3 = C0015R$string.hotspot_operator_dialog_nosim_title;
                i2 = C0015R$string.hotspot_operator_dialog_nosim_msg;
                i = C0015R$string.hotspot_operator_dialog_nosim_button;
            } else if (!isHaveProfile()) {
                i3 = C0015R$string.hotspot_operator_dialog_othererror_title;
                i2 = C0015R$string.hotspot_operator_dialog_othererror_msg;
                i = C0015R$string.hotspot_operator_dialog_othererror_button;
            } else {
                i3 = 0;
                i2 = 0;
                i = 0;
            }
            AlertDialog create = new AlertDialog.Builder(this.mContext).setMessage(i2).setTitle(i3).setCancelable(false).setPositiveButton(i, new DialogInterface.OnClickListener() { // from class: com.android.systemui.qs.tiles.HotspotTile.2
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i4) {
                    HotspotTile.this.mOperatorDialogShowing = false;
                }
            }).create();
            create.getWindow().setType(2009);
            create.setOnShowListener(new DialogInterface.OnShowListener(create) { // from class: com.android.systemui.qs.tiles.-$$Lambda$HotspotTile$27H8jHShXYGUqAqpjyv366eDPu8
                public final /* synthetic */ AlertDialog f$0;

                {
                    this.f$0 = r1;
                }

                @Override // android.content.DialogInterface.OnShowListener
                public final void onShow(DialogInterface dialogInterface) {
                    HotspotTile.lambda$operatorAlertDialog$0(this.f$0, dialogInterface);
                }
            });
            this.mOperatorDialogShowing = true;
            create.show();
        }
    }

    /* access modifiers changed from: private */
    public class MobileDataContentObserver extends ContentObserver {
        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public MobileDataContentObserver() {
            super(new Handler());
            HotspotTile.this = r1;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            boolean z2 = true;
            if (Settings.Global.getInt(((QSTileImpl) HotspotTile.this).mContext.getContentResolver(), "mobile_data", 1) != 1) {
                z2 = false;
            }
            String str = ((QSTileImpl) HotspotTile.this).TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Mobile data enabled status: ");
            sb.append(z2 ? "ON" : "OFF");
            Log.d(str, sb.toString());
            HotspotTile.this.onMobileDataEnabled(z2);
        }
    }

    private void onMobileDataEnabled(boolean z) {
        String str = this.TAG;
        Log.d(str, "onMobileDataEnabled: " + z);
        if (ProductUtils.isUsvMode()) {
            CallbackInfo callbackInfo = new CallbackInfo();
            callbackInfo.isDataSaverEnabled = !z;
            String str2 = this.TAG;
            Log.d(str2, "isUsvMode onMobileDataEnabled() called with: enabled = [" + z + "]");
            refreshState(callbackInfo);
            if (!z && ((QSTile.BooleanState) this.mState).state == 2) {
                this.mHotspotController.setHotspotEnabled(false);
            }
        }
    }

    public void showMHSErrorDialog(String str, String str2) {
        int i = this.mIsAuthorizationClient == 0 ? C0015R$string.mhs_app : 17039370;
        View inflate = View.inflate(this.mContext, C0011R$layout.dialog_checkbox_airplanemode, null);
        final CheckBox checkBox = (CheckBox) inflate.findViewById(C0008R$id.checkbox);
        SystemUIDialog systemUIDialog = new SystemUIDialog(this.mContext);
        systemUIDialog.setTitle(str);
        systemUIDialog.setMessage(str2);
        systemUIDialog.setPositiveButton(i, new DialogInterface.OnClickListener() { // from class: com.android.systemui.qs.tiles.HotspotTile.4
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                HotspotTile hotspotTile = HotspotTile.this;
                int i3 = hotspotTile.mIsAuthorizationClient;
                if (i3 == 0) {
                    try {
                        Uri parse = Uri.parse("https://mobile.vzw.com/hybridClient/mvm/hotspot");
                        Intent launchIntentForPackage = ((QSTileImpl) HotspotTile.this).mContext.getPackageManager().getLaunchIntentForPackage("com.vzw.hss.myverizon");
                        if (launchIntentForPackage != null) {
                            ((QSTileImpl) HotspotTile.this).mContext.sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
                            launchIntentForPackage.setAction("android.intent.action.VIEW");
                            launchIntentForPackage.setFlags(268435456);
                            launchIntentForPackage.setData(parse);
                            ((QSTileImpl) HotspotTile.this).mContext.startActivity(launchIntentForPackage);
                        }
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (i3 == 1) {
                    Settings.System.putInt(((QSTileImpl) hotspotTile).mContext.getContentResolver(), "op_mhi_alert_on_dont_show_again", checkBox.isChecked() ? 1 : 0);
                }
                dialogInterface.dismiss();
            }
        });
        int i2 = this.mIsAuthorizationClient;
        if (i2 == 0) {
            systemUIDialog.setNegativeButton(17039360, null);
        } else if (i2 == 1) {
            systemUIDialog.setView(inflate);
        }
        systemUIDialog.setShowForAllUsers(true);
        systemUIDialog.show();
    }

    private void dismissDialog() {
        ProgressDialog progressDialog = this.mSimUnlockProgressDialog;
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private Dialog getMhsProgressDialog() {
        if (this.mSimUnlockProgressDialog == null) {
            ProgressDialog progressDialog = new ProgressDialog(this.mContext);
            this.mSimUnlockProgressDialog = progressDialog;
            progressDialog.setMessage(this.mContext.getString(C0015R$string.dialog_mhs_error));
            this.mSimUnlockProgressDialog.setIndeterminate(true);
            this.mSimUnlockProgressDialog.setCancelable(false);
            if (!(this.mContext instanceof Activity)) {
                this.mSimUnlockProgressDialog.getWindow().setType(2009);
            }
        }
        return this.mSimUnlockProgressDialog;
    }

    private boolean isVerizonSim() {
        return TextUtils.equals(SystemProperties.get("ril.sim.carrier.name.slot0"), "VZW");
    }
}
