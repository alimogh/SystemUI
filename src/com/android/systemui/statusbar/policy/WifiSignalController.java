package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkScoreManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.OpFeatures;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.wifi.WifiStatusTracker;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.SignalController;
import com.android.systemui.util.ProductUtils;
import com.oneplus.systemui.statusbar.policy.OpWifiIcons;
import com.oneplus.util.OpUtils;
import com.oneplus.worklife.OPWLBHelper;
import java.util.Objects;
public class WifiSignalController extends SignalController<WifiState, SignalController.IconGroup> {
    private final SignalController.IconGroup mDefaultWifiIconGroup;
    private final boolean mHasMobileDataFeature;
    private final SignalController.IconGroup mOpDefaultWifiIconGroup;
    private final SignalController.IconGroup mWifi4IconGroup;
    private final SignalController.IconGroup mWifi5IconGroup;
    private final SignalController.IconGroup mWifi6IconGroup;
    private final WifiStatusTracker mWifiTracker;

    public WifiSignalController(Context context, boolean z, CallbackHandler callbackHandler, NetworkControllerImpl networkControllerImpl, WifiManager wifiManager, ConnectivityManager connectivityManager, NetworkScoreManager networkScoreManager) {
        super("WifiSignalController", context, 1, callbackHandler, networkControllerImpl);
        WifiStatusTracker wifiStatusTracker = new WifiStatusTracker(this.mContext, wifiManager, networkScoreManager, connectivityManager, new Runnable() { // from class: com.android.systemui.statusbar.policy.-$$Lambda$WifiSignalController$AffzGdHvQakHA4bIzi_tW1MVLCY
            @Override // java.lang.Runnable
            public final void run() {
                WifiSignalController.lambda$AffzGdHvQakHA4bIzi_tW1MVLCY(WifiSignalController.this);
            }
        });
        this.mWifiTracker = wifiStatusTracker;
        wifiStatusTracker.setListening(true);
        this.mHasMobileDataFeature = z;
        if (wifiManager != null) {
            wifiManager.registerTrafficStateCallback(context.getMainExecutor(), new WifiTrafficStateCallback());
        }
        this.mOpDefaultWifiIconGroup = new SignalController.IconGroup("Wi-Fi Icons", OpWifiIcons.OP_WIFI_SIGNAL_STRENGTH, OpWifiIcons.OP_QS_WIFI_SIGNAL_STRENGTH, AccessibilityContentDescriptions.WIFI_CONNECTION_STRENGTH, OpWifiIcons.OP_WIFI_NO_NETWORK, OpWifiIcons.OP_QS_WIFI_NO_NETWORK, OpWifiIcons.OP_WIFI_NO_NETWORK, OpWifiIcons.OP_QS_WIFI_NO_NETWORK, AccessibilityContentDescriptions.WIFI_NO_CONNECTION);
        this.mDefaultWifiIconGroup = new SignalController.IconGroup("Wi-Fi Icons", WifiIcons.WIFI_SIGNAL_STRENGTH, WifiIcons.QS_WIFI_SIGNAL_STRENGTH, AccessibilityContentDescriptions.WIFI_CONNECTION_STRENGTH, WifiIcons.WIFI_NO_NETWORK, WifiIcons.QS_WIFI_NO_NETWORK, WifiIcons.WIFI_NO_NETWORK, WifiIcons.QS_WIFI_NO_NETWORK, AccessibilityContentDescriptions.WIFI_NO_CONNECTION);
        this.mWifi4IconGroup = new SignalController.IconGroup("Wi-Fi 4 Icons", WifiIcons.WIFI_4_SIGNAL_STRENGTH, WifiIcons.QS_WIFI_4_SIGNAL_STRENGTH, AccessibilityContentDescriptions.WIFI_CONNECTION_STRENGTH, WifiIcons.WIFI_NO_NETWORK, WifiIcons.QS_WIFI_NO_NETWORK, WifiIcons.WIFI_NO_NETWORK, WifiIcons.QS_WIFI_NO_NETWORK, AccessibilityContentDescriptions.WIFI_NO_CONNECTION);
        this.mWifi5IconGroup = new SignalController.IconGroup("Wi-Fi 5 Icons", WifiIcons.WIFI_5_SIGNAL_STRENGTH, WifiIcons.QS_WIFI_5_SIGNAL_STRENGTH, AccessibilityContentDescriptions.WIFI_CONNECTION_STRENGTH, WifiIcons.WIFI_NO_NETWORK, WifiIcons.QS_WIFI_NO_NETWORK, WifiIcons.WIFI_NO_NETWORK, WifiIcons.QS_WIFI_NO_NETWORK, AccessibilityContentDescriptions.WIFI_NO_CONNECTION);
        this.mWifi6IconGroup = new SignalController.IconGroup("Wi-Fi 6 Icons", WifiIcons.WIFI_6_SIGNAL_STRENGTH, WifiIcons.QS_WIFI_6_SIGNAL_STRENGTH, AccessibilityContentDescriptions.WIFI_CONNECTION_STRENGTH, WifiIcons.WIFI_NO_NETWORK, WifiIcons.QS_WIFI_NO_NETWORK, WifiIcons.WIFI_NO_NETWORK, WifiIcons.QS_WIFI_NO_NETWORK, AccessibilityContentDescriptions.WIFI_NO_CONNECTION);
        SignalController.IconGroup iconGroup = this.mDefaultWifiIconGroup;
        ((WifiState) this.mLastState).iconGroup = iconGroup;
        ((WifiState) this.mCurrentState).iconGroup = iconGroup;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.policy.SignalController
    public WifiState cleanState() {
        return new WifiState();
    }

    /* access modifiers changed from: package-private */
    public void refreshLocale() {
        this.mWifiTracker.refreshLocale();
    }

    @Override // com.android.systemui.statusbar.policy.SignalController
    public void notifyListeners(NetworkController.SignalCallback signalCallback) {
        int i;
        T t = this.mCurrentState;
        boolean z = ((WifiState) t).enabled && ((((WifiState) t).connected && ((WifiState) t).inetCondition == 1) || !this.mHasMobileDataFeature || this.mWifiTracker.isDefaultNetwork);
        T t2 = this.mCurrentState;
        String str = ((WifiState) t2).connected ? ((WifiState) t2).ssid : null;
        boolean z2 = z && ((WifiState) this.mCurrentState).ssid != null;
        String charSequence = getTextIfExists(getContentDescription()).toString();
        if (((WifiState) this.mCurrentState).inetCondition == 0) {
            charSequence = charSequence + "," + this.mContext.getString(C0015R$string.data_connection_no_internet);
        }
        NetworkController.IconState iconState = new NetworkController.IconState(z, getCurrentIconId(), charSequence);
        boolean z3 = ((WifiState) this.mCurrentState).connected;
        if (this.mWifiTracker.isCaptivePortal) {
            i = C0006R$drawable.ic_qs_wifi_disconnected;
        } else {
            i = getQsCurrentIconId();
        }
        NetworkController.IconState iconState2 = new NetworkController.IconState(z3, i, charSequence);
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.i("WifiSignalController", "wifi icon res name:" + OpUtils.getResourceName(this.mContext, iconState.icon));
        }
        T t3 = this.mCurrentState;
        boolean z4 = ((WifiState) t3).enabled;
        boolean z5 = z2 && ((WifiState) t3).activityIn;
        boolean z6 = z2 && ((WifiState) this.mCurrentState).activityOut;
        T t4 = this.mCurrentState;
        signalCallback.setWifiIndicators(z4, iconState, iconState2, z5, z6, str, ((WifiState) t4).isTransient, ((WifiState) t4).statusLabel);
    }

    private void updateIconGroup() {
        if (OpUtils.isUSS() || ProductUtils.isUsvMode()) {
            ((WifiState) this.mCurrentState).iconGroup = this.mOpDefaultWifiIconGroup;
        } else if (!OpFeatures.isSupport(new int[]{197})) {
            ((WifiState) this.mCurrentState).iconGroup = this.mDefaultWifiIconGroup;
        } else {
            T t = this.mCurrentState;
            if (((WifiState) t).wifiStandard == 4) {
                ((WifiState) t).iconGroup = this.mWifi4IconGroup;
            } else if (((WifiState) t).wifiStandard == 5) {
                ((WifiState) t).iconGroup = ((WifiState) t).isReady ? this.mWifi6IconGroup : this.mWifi5IconGroup;
            } else if (((WifiState) t).wifiStandard == 6) {
                ((WifiState) t).iconGroup = this.mWifi6IconGroup;
            } else {
                ((WifiState) t).iconGroup = this.mDefaultWifiIconGroup;
            }
        }
    }

    public void fetchInitialState() {
        this.mWifiTracker.fetchInitialState();
        T t = this.mCurrentState;
        WifiStatusTracker wifiStatusTracker = this.mWifiTracker;
        ((WifiState) t).enabled = wifiStatusTracker.enabled;
        ((WifiState) t).connected = wifiStatusTracker.connected;
        ((WifiState) t).ssid = wifiStatusTracker.ssid;
        ((WifiState) t).rssi = wifiStatusTracker.rssi;
        ((WifiState) t).level = wifiStatusTracker.level;
        ((WifiState) t).statusLabel = wifiStatusTracker.statusLabel;
        notifyListenersIfNecessary();
    }

    public void handleBroadcast(Intent intent) {
        this.mWifiTracker.handleBroadcast(intent);
        T t = this.mCurrentState;
        WifiStatusTracker wifiStatusTracker = this.mWifiTracker;
        ((WifiState) t).enabled = wifiStatusTracker.enabled;
        ((WifiState) t).connected = wifiStatusTracker.connected;
        ((WifiState) t).ssid = wifiStatusTracker.ssid;
        ((WifiState) t).rssi = wifiStatusTracker.rssi;
        ((WifiState) t).level = wifiStatusTracker.level;
        ((WifiState) t).statusLabel = wifiStatusTracker.statusLabel;
        ((WifiState) t).wifiStandard = wifiStatusTracker.wifiStandard;
        ((WifiState) t).isReady = wifiStatusTracker.vhtMax8SpatialStreamsSupport && wifiStatusTracker.he8ssCapableAp;
        updateIconGroup();
        if (!"android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction()) || intent.getIntExtra("wifi_enable_cancel", 0) != 1) {
            notifyListenersIfNecessary();
        } else {
            Log.d("WifiSignalController", "wifi change with cancel. force notify.");
            notifyListenersIfNecessary(true);
        }
        sendConnectedBroadcast();
    }

    private void sendConnectedBroadcast() {
        OPWLBHelper.getInstance(this.mContext).processWifiConnectivity(((WifiState) this.mCurrentState).connected);
    }

    /* access modifiers changed from: private */
    public void handleStatusUpdated() {
        ((WifiState) this.mCurrentState).statusLabel = this.mWifiTracker.statusLabel;
        notifyListenersIfNecessary();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setActivity(int i) {
        boolean z = false;
        ((WifiState) this.mCurrentState).activityIn = i == 3 || i == 1;
        WifiState wifiState = (WifiState) this.mCurrentState;
        if (i == 3 || i == 2) {
            z = true;
        }
        wifiState.activityOut = z;
        notifyListenersIfNecessary();
    }

    private class WifiTrafficStateCallback implements WifiManager.TrafficStateCallback {
        private WifiTrafficStateCallback() {
        }

        public void onStateChanged(int i) {
            WifiSignalController.this.setActivity(i);
        }
    }

    /* access modifiers changed from: package-private */
    public static class WifiState extends SignalController.State {
        boolean isReady;
        boolean isTransient;
        String ssid;
        String statusLabel;
        int wifiStandard;

        WifiState() {
        }

        @Override // com.android.systemui.statusbar.policy.SignalController.State
        public void copyFrom(SignalController.State state) {
            super.copyFrom(state);
            WifiState wifiState = (WifiState) state;
            this.ssid = wifiState.ssid;
            this.wifiStandard = wifiState.wifiStandard;
            this.isReady = wifiState.isReady;
            this.isTransient = wifiState.isTransient;
            this.statusLabel = wifiState.statusLabel;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.systemui.statusbar.policy.SignalController.State
        public void toString(StringBuilder sb) {
            super.toString(sb);
            sb.append(",ssid=");
            sb.append(this.ssid);
            sb.append(",wifiStandard=");
            sb.append(this.wifiStandard);
            sb.append(",isReady=");
            sb.append(this.isReady);
            sb.append(",isTransient=");
            sb.append(this.isTransient);
            sb.append(",statusLabel=");
            sb.append(this.statusLabel);
        }

        @Override // com.android.systemui.statusbar.policy.SignalController.State
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            WifiState wifiState = (WifiState) obj;
            if (Objects.equals(wifiState.ssid, this.ssid) && wifiState.wifiStandard == this.wifiStandard && wifiState.isReady == this.isReady && wifiState.isTransient == this.isTransient && TextUtils.equals(wifiState.statusLabel, this.statusLabel)) {
                return true;
            }
            return false;
        }
    }
}
