package com.android.wifitrackerlib;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkScoreCache;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.os.Handler;
import android.text.TextUtils;
import androidx.core.util.Preconditions;
import com.android.internal.annotations.VisibleForTesting;
import com.android.wifitrackerlib.WifiEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
@VisibleForTesting
public class PasspointWifiEntry extends WifiEntry implements WifiEntry.WifiEntryCallback {
    private final Context mContext;
    private final List<ScanResult> mCurrentHomeScanResults = new ArrayList();
    private final List<ScanResult> mCurrentRoamingScanResults = new ArrayList();
    private String mFqdn;
    private String mFriendlyName;
    private final String mKey;
    private final Object mLock = new Object();
    private int mMeteredOverride = 0;
    private OsuWifiEntry mOsuWifiEntry;
    private PasspointConfiguration mPasspointConfig;
    private int mSecurity = 3;
    protected long mSubscriptionExpirationTimeInMillis;
    private WifiConfiguration mWifiConfig;

    @Override // com.android.wifitrackerlib.WifiEntry
    public boolean canSetAutoJoinEnabled() {
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.wifitrackerlib.WifiEntry
    public String getScanResultDescription() {
        return "";
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public WifiConfiguration getWifiConfiguration() {
        return null;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public boolean isSaved() {
        return false;
    }

    PasspointWifiEntry(Context context, Handler handler, PasspointConfiguration passpointConfiguration, WifiManager wifiManager, WifiNetworkScoreCache wifiNetworkScoreCache, boolean z) throws IllegalArgumentException {
        super(handler, wifiManager, wifiNetworkScoreCache, z);
        Preconditions.checkNotNull(passpointConfiguration, "Cannot construct with null PasspointConfiguration!");
        this.mContext = context;
        this.mPasspointConfig = passpointConfiguration;
        this.mKey = uniqueIdToPasspointWifiEntryKey(passpointConfiguration.getUniqueId());
        this.mFqdn = passpointConfiguration.getHomeSp().getFqdn();
        this.mFriendlyName = passpointConfiguration.getHomeSp().getFriendlyName();
        this.mSubscriptionExpirationTimeInMillis = passpointConfiguration.getSubscriptionExpirationTimeMillis();
        this.mMeteredOverride = this.mPasspointConfig.getMeteredOverride();
    }

    PasspointWifiEntry(Context context, Handler handler, WifiConfiguration wifiConfiguration, WifiManager wifiManager, WifiNetworkScoreCache wifiNetworkScoreCache, boolean z) throws IllegalArgumentException {
        super(handler, wifiManager, wifiNetworkScoreCache, z);
        Preconditions.checkNotNull(wifiConfiguration, "Cannot construct with null PasspointConfiguration!");
        if (wifiConfiguration.isPasspoint()) {
            this.mContext = context;
            this.mWifiConfig = wifiConfiguration;
            this.mKey = uniqueIdToPasspointWifiEntryKey(wifiConfiguration.getKey());
            this.mFqdn = wifiConfiguration.FQDN;
            this.mFriendlyName = this.mWifiConfig.providerFriendlyName;
            return;
        }
        throw new IllegalArgumentException("Given WifiConfiguration is not for Passpoint!");
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public String getKey() {
        return this.mKey;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public int getConnectedState() {
        OsuWifiEntry osuWifiEntry;
        if (!isExpired() || super.getConnectedState() != 0 || (osuWifiEntry = this.mOsuWifiEntry) == null) {
            return super.getConnectedState();
        }
        return osuWifiEntry.getConnectedState();
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public String getTitle() {
        return this.mFriendlyName;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0083: APUT  (r5v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r1v19 java.lang.String) */
    @Override // com.android.wifitrackerlib.WifiEntry
    public String getSummary(boolean z) {
        StringJoiner stringJoiner = new StringJoiner(this.mContext.getString(R$string.summary_separator));
        if (isExpired()) {
            OsuWifiEntry osuWifiEntry = this.mOsuWifiEntry;
            if (osuWifiEntry != null) {
                stringJoiner.add(osuWifiEntry.getSummary(z));
            } else {
                stringJoiner.add(this.mContext.getString(R$string.wifi_passpoint_expired));
            }
        } else if (getConnectedState() == 0) {
            String disconnectedStateDescription = Utils.getDisconnectedStateDescription(this.mContext, this);
            if (!TextUtils.isEmpty(disconnectedStateDescription)) {
                stringJoiner.add(disconnectedStateDescription);
            } else if (z) {
                stringJoiner.add(this.mContext.getString(R$string.wifi_disconnected));
            } else if (!this.mForSavedNetworksPage) {
                WifiConfiguration wifiConfiguration = this.mWifiConfig;
                if (wifiConfiguration == null || !wifiConfiguration.fromWifiNetworkSuggestion) {
                    stringJoiner.add(this.mContext.getString(R$string.wifi_remembered));
                } else {
                    Context context = this.mContext;
                    String carrierNameForSubId = Utils.getCarrierNameForSubId(context, Utils.getSubIdForConfig(context, wifiConfiguration));
                    String appLabel = Utils.getAppLabel(this.mContext, this.mWifiConfig.creatorName);
                    if (TextUtils.isEmpty(appLabel)) {
                        appLabel = this.mWifiConfig.creatorName;
                    }
                    Context context2 = this.mContext;
                    int i = R$string.available_via_app;
                    Object[] objArr = new Object[1];
                    if (carrierNameForSubId == null) {
                        carrierNameForSubId = appLabel;
                    }
                    objArr[0] = carrierNameForSubId;
                    stringJoiner.add(context2.getString(i, objArr));
                }
            }
        } else {
            String connectStateDescription = getConnectStateDescription();
            if (!TextUtils.isEmpty(connectStateDescription)) {
                stringJoiner.add(connectStateDescription);
            }
        }
        String speedDescription = Utils.getSpeedDescription(this.mContext, this);
        if (!TextUtils.isEmpty(speedDescription)) {
            stringJoiner.add(speedDescription);
        }
        String autoConnectDescription = Utils.getAutoConnectDescription(this.mContext, this);
        if (!TextUtils.isEmpty(autoConnectDescription)) {
            stringJoiner.add(autoConnectDescription);
        }
        String meteredDescription = Utils.getMeteredDescription(this.mContext, this);
        if (!TextUtils.isEmpty(meteredDescription)) {
            stringJoiner.add(meteredDescription);
        }
        if (!z) {
            String verboseLoggingDescription = Utils.getVerboseLoggingDescription(this);
            if (!TextUtils.isEmpty(verboseLoggingDescription)) {
                stringJoiner.add(verboseLoggingDescription);
            }
        }
        return stringJoiner.toString();
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0040: APUT  (r3v2 java.lang.Object[]), (0 ??[int, short, byte, char]), (r1v5 java.lang.String) */
    private String getConnectStateDescription() {
        if (getConnectedState() == 2) {
            WifiInfo wifiInfo = this.mWifiInfo;
            String str = null;
            String requestingPackageName = wifiInfo != null ? wifiInfo.getRequestingPackageName() : null;
            if (!TextUtils.isEmpty(requestingPackageName)) {
                WifiConfiguration wifiConfiguration = this.mWifiConfig;
                if (wifiConfiguration != null) {
                    Context context = this.mContext;
                    str = Utils.getCarrierNameForSubId(context, Utils.getSubIdForConfig(context, wifiConfiguration));
                }
                String appLabel = Utils.getAppLabel(this.mContext, requestingPackageName);
                if (!TextUtils.isEmpty(appLabel)) {
                    requestingPackageName = appLabel;
                }
                Context context2 = this.mContext;
                int i = R$string.connected_via_app;
                Object[] objArr = new Object[1];
                if (str == null) {
                    str = requestingPackageName;
                }
                objArr[0] = str;
                return context2.getString(i, objArr);
            }
            String currentNetworkCapabilitiesInformation = Utils.getCurrentNetworkCapabilitiesInformation(this.mContext, this.mNetworkCapabilities);
            if (!TextUtils.isEmpty(currentNetworkCapabilitiesInformation)) {
                return currentNetworkCapabilitiesInformation;
            }
        }
        return Utils.getNetworkDetailedState(this.mContext, this.mNetworkInfo);
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public int getSecurity() {
        return this.mSecurity;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public boolean isMetered() {
        if (getMeteredChoice() == 1) {
            return true;
        }
        WifiConfiguration wifiConfiguration = this.mWifiConfig;
        return wifiConfiguration != null && wifiConfiguration.meteredHint;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public boolean isSuggestion() {
        WifiConfiguration wifiConfiguration = this.mWifiConfig;
        return wifiConfiguration != null && wifiConfiguration.fromWifiNetworkSuggestion;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public boolean isSubscription() {
        return this.mPasspointConfig != null;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public int getMeteredChoice() {
        int i = this.mMeteredOverride;
        if (i == 1) {
            return 1;
        }
        return i == 2 ? 2 : 0;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public boolean canSetMeteredChoice() {
        return !isSuggestion() && this.mPasspointConfig != null;
    }

    @Override // com.android.wifitrackerlib.WifiEntry
    public boolean isAutoJoinEnabled() {
        WifiConfiguration wifiConfiguration;
        if (this.mPasspointConfig != null || (wifiConfiguration = this.mWifiConfig) == null) {
            return this.mPasspointConfig.isAutojoinEnabled();
        }
        return wifiConfiguration.allowAutojoin;
    }

    public boolean isExpired() {
        if (this.mSubscriptionExpirationTimeInMillis > 0 && System.currentTimeMillis() >= this.mSubscriptionExpirationTimeInMillis) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void updatePasspointConfig(PasspointConfiguration passpointConfiguration) {
        this.mPasspointConfig = passpointConfiguration;
        if (passpointConfiguration != null) {
            this.mFriendlyName = passpointConfiguration.getHomeSp().getFriendlyName();
            this.mSubscriptionExpirationTimeInMillis = passpointConfiguration.getSubscriptionExpirationTimeMillis();
            this.mMeteredOverride = passpointConfiguration.getMeteredOverride();
        }
        notifyOnUpdated();
    }

    /* access modifiers changed from: package-private */
    public void updateScanResultInfo(WifiConfiguration wifiConfiguration, List<ScanResult> list, List<ScanResult> list2) throws IllegalArgumentException {
        this.mWifiConfig = wifiConfiguration;
        synchronized (this.mLock) {
            this.mCurrentHomeScanResults.clear();
            this.mCurrentRoamingScanResults.clear();
            if (list != null) {
                this.mCurrentHomeScanResults.addAll(list);
            }
            if (list2 != null) {
                this.mCurrentRoamingScanResults.addAll(list2);
            }
        }
        int i = -1;
        if (this.mWifiConfig != null) {
            this.mSecurity = Utils.getSecurityTypeFromWifiConfiguration(wifiConfiguration);
            ArrayList arrayList = new ArrayList();
            if (list != null && !list.isEmpty()) {
                arrayList.addAll(list);
                updateWifiGenerationInfo(list);
            } else if (list2 != null && !list2.isEmpty()) {
                arrayList.addAll(list2);
                updateWifiGenerationInfo(list2);
            }
            ScanResult bestScanResultByLevel = Utils.getBestScanResultByLevel(arrayList);
            if (bestScanResultByLevel != null) {
                WifiConfiguration wifiConfiguration2 = this.mWifiConfig;
                wifiConfiguration2.SSID = "\"" + bestScanResultByLevel.SSID + "\"";
                updateTransitionModeCapa(bestScanResultByLevel);
            }
            if (getConnectedState() == 0) {
                if (bestScanResultByLevel != null) {
                    i = this.mWifiManager.calculateSignalLevel(bestScanResultByLevel.level);
                }
                this.mLevel = i;
                this.mSpeed = Utils.getAverageSpeedFromScanResults(this.mScoreCache, arrayList);
            }
        } else {
            this.mLevel = -1;
        }
        notifyOnUpdated();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.wifitrackerlib.WifiEntry
    public boolean connectionInfoMatches(WifiInfo wifiInfo, NetworkInfo networkInfo) {
        if (!wifiInfo.isPasspointAp()) {
            return false;
        }
        return TextUtils.equals(wifiInfo.getPasspointFqdn(), this.mFqdn);
    }

    static String uniqueIdToPasspointWifiEntryKey(String str) {
        Preconditions.checkNotNull(str, "Cannot create key with null unique id!");
        return "PasspointWifiEntry:" + str;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.wifitrackerlib.WifiEntry
    public String getNetworkSelectionDescription() {
        return Utils.getNetworkSelectionDescription(this.mWifiConfig);
    }

    /* access modifiers changed from: package-private */
    public void setOsuWifiEntry(OsuWifiEntry osuWifiEntry) {
        this.mOsuWifiEntry = osuWifiEntry;
        osuWifiEntry.setListener(this);
    }

    @Override // com.android.wifitrackerlib.WifiEntry.WifiEntryCallback
    public void onUpdated() {
        notifyOnUpdated();
    }
}
