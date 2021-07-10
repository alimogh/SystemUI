package com.android.wifitrackerlib;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkScoreCache;
import android.net.wifi.hotspot2.OsuProvider;
import android.os.Handler;
import android.text.TextUtils;
import androidx.core.util.Preconditions;
import java.util.ArrayList;
import java.util.List;
/* access modifiers changed from: package-private */
public class OsuWifiEntry extends WifiEntry {
    private final Context mContext;
    private final List<ScanResult> mCurrentScanResults = new ArrayList();
    private boolean mIsAlreadyProvisioned = false;
    private final String mKey;
    private final Object mLock = new Object();
    private OsuProvider mOsuProvider;
    private String mOsuStatusString;

    @Override // com.android.wifitrackerlib.WifiEntry
    public boolean canSetAutoJoinEnabled() {
        return false;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public boolean canSetMeteredChoice() {
        return false;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public int getMeteredChoice() {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.wifitrackerlib.WifiEntry
    public String getScanResultDescription() {
        return "";
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public int getSecurity() {
        return 0;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public WifiConfiguration getWifiConfiguration() {
        return null;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public boolean isAutoJoinEnabled() {
        return false;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public boolean isMetered() {
        return false;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public boolean isSaved() {
        return false;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public boolean isSubscription() {
        return false;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public boolean isSuggestion() {
        return false;
    }

    OsuWifiEntry(Context context, Handler handler, OsuProvider osuProvider, WifiManager wifiManager, WifiNetworkScoreCache wifiNetworkScoreCache, boolean z) throws IllegalArgumentException {
        super(handler, wifiManager, wifiNetworkScoreCache, z);
        Preconditions.checkNotNull(osuProvider, "Cannot construct with null osuProvider!");
        this.mContext = context;
        this.mOsuProvider = osuProvider;
        this.mKey = osuProviderToOsuWifiEntryKey(osuProvider);
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public String getKey() {
        return this.mKey;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public String getTitle() {
        return this.mOsuProvider.getFriendlyName();
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public String getSummary(boolean z) {
        String str = this.mOsuStatusString;
        if (str != null) {
            return str;
        }
        if (!isAlreadyProvisioned()) {
            return this.mContext.getString(R$string.tap_to_sign_up);
        }
        if (z) {
            return this.mContext.getString(R$string.wifi_passpoint_expired);
        }
        return this.mContext.getString(R$string.tap_to_renew_subscription_and_connect);
    }

    /* access modifiers changed from: package-private */
    public void updateScanResultInfo(List<ScanResult> list) throws IllegalArgumentException {
        if (list == null) {
            list = new ArrayList<>();
        }
        synchronized (this.mLock) {
            this.mCurrentScanResults.clear();
            this.mCurrentScanResults.addAll(list);
        }
        ScanResult bestScanResultByLevel = Utils.getBestScanResultByLevel(list);
        if (bestScanResultByLevel != null) {
            updateTransitionModeCapa(bestScanResultByLevel);
        }
        if (getConnectedState() == 0) {
            this.mLevel = bestScanResultByLevel != null ? this.mWifiManager.calculateSignalLevel(bestScanResultByLevel.level) : -1;
        }
        updateWifiGenerationInfo(this.mCurrentScanResults);
        notifyOnUpdated();
    }

    static String osuProviderToOsuWifiEntryKey(OsuProvider osuProvider) {
        Preconditions.checkNotNull(osuProvider, "Cannot create key with null OsuProvider!");
        return "OsuWifiEntry:" + osuProvider.getFriendlyName() + "," + osuProvider.getServerUri().toString();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.wifitrackerlib.WifiEntry
    public boolean connectionInfoMatches(WifiInfo wifiInfo, NetworkInfo networkInfo) {
        return wifiInfo.isOsuAp() && TextUtils.equals(wifiInfo.getPasspointProviderFriendlyName(), this.mOsuProvider.getFriendlyName());
    }

    /* access modifiers changed from: package-private */
    public OsuProvider getOsuProvider() {
        return this.mOsuProvider;
    }

    /* access modifiers changed from: package-private */
    public boolean isAlreadyProvisioned() {
        return this.mIsAlreadyProvisioned;
    }

    /* access modifiers changed from: package-private */
    public void setAlreadyProvisioned(boolean z) {
        this.mIsAlreadyProvisioned = z;
    }
}
