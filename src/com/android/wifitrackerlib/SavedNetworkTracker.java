package com.android.wifitrackerlib;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import androidx.core.util.Preconditions;
import com.android.wifitrackerlib.SavedNetworkTracker;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
public class SavedNetworkTracker extends BaseWifiTracker {
    private final SavedNetworkTrackerCallback mListener;
    private final Object mLock;
    private final Map<String, PasspointWifiEntry> mPasspointWifiEntryCache;
    private final List<WifiEntry> mSavedWifiEntries;
    private final Map<String, StandardWifiEntry> mStandardWifiEntryCache;
    private final List<WifiEntry> mSubscriptionWifiEntries;

    public interface SavedNetworkTrackerCallback {
        void onSavedWifiEntriesChanged();

        void onSubscriptionWifiEntriesChanged();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.wifitrackerlib.BaseWifiTracker
    public void handleOnStart() {
        updateStandardWifiEntryConfigs(this.mWifiManager.getConfiguredNetworks());
        updatePasspointWifiEntryConfigs(this.mWifiManager.getPasspointConfigurations());
        conditionallyUpdateScanResults(true);
        updateSavedWifiEntries();
        updateSubscriptionWifiEntries();
    }

    private void updateSavedWifiEntries() {
        synchronized (this.mLock) {
            this.mSavedWifiEntries.clear();
            this.mSavedWifiEntries.addAll(this.mStandardWifiEntryCache.values());
            Collections.sort(this.mSavedWifiEntries);
            if (BaseWifiTracker.isVerboseLoggingEnabled()) {
                Log.v("SavedNetworkTracker", "Updated SavedWifiEntries: " + Arrays.toString(this.mSavedWifiEntries.toArray()));
            }
        }
        notifyOnSavedWifiEntriesChanged();
    }

    private void updateSubscriptionWifiEntries() {
        synchronized (this.mLock) {
            this.mSubscriptionWifiEntries.clear();
            this.mSubscriptionWifiEntries.addAll(this.mPasspointWifiEntryCache.values());
            Collections.sort(this.mSubscriptionWifiEntries);
            if (BaseWifiTracker.isVerboseLoggingEnabled()) {
                Log.v("SavedNetworkTracker", "Updated SubscriptionWifiEntries: " + Arrays.toString(this.mSubscriptionWifiEntries.toArray()));
            }
        }
        notifyOnSubscriptionWifiEntriesChanged();
    }

    private void updateStandardWifiEntryScans(List<ScanResult> list) {
        Preconditions.checkNotNull(list, "Scan Result list should not be null!");
        this.mStandardWifiEntryCache.entrySet().forEach(new Consumer(Utils.mapScanResultsToKey(list, false, null, this.mWifiManager.isWpa3SaeSupported(), this.mWifiManager.isWpa3SuiteBSupported(), this.mWifiManager.isEnhancedOpenSupported(), this.mWifiManager.isWapiSupported())) { // from class: com.android.wifitrackerlib.-$$Lambda$SavedNetworkTracker$RngE0pMCth0-JbStnHNgSxFSuio
            public final /* synthetic */ Map f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                SavedNetworkTracker.lambda$updateStandardWifiEntryScans$0(this.f$0, (Map.Entry) obj);
            }
        });
    }

    private void updatePasspointWifiEntryScans(List<ScanResult> list) {
        Preconditions.checkNotNull(list, "Scan Result list should not be null!");
        TreeSet treeSet = new TreeSet();
        for (Pair pair : this.mWifiManager.getAllMatchingWifiConfigs(list)) {
            WifiConfiguration wifiConfiguration = (WifiConfiguration) pair.first;
            String uniqueIdToPasspointWifiEntryKey = PasspointWifiEntry.uniqueIdToPasspointWifiEntryKey(wifiConfiguration.getKey());
            treeSet.add(uniqueIdToPasspointWifiEntryKey);
            if (this.mPasspointWifiEntryCache.containsKey(uniqueIdToPasspointWifiEntryKey)) {
                this.mPasspointWifiEntryCache.get(uniqueIdToPasspointWifiEntryKey).updateScanResultInfo(wifiConfiguration, (List) ((Map) pair.second).get(0), (List) ((Map) pair.second).get(1));
            }
        }
        for (PasspointWifiEntry passpointWifiEntry : this.mPasspointWifiEntryCache.values()) {
            if (!treeSet.contains(passpointWifiEntry.getKey())) {
                passpointWifiEntry.updateScanResultInfo(null, null, null);
            }
        }
    }

    private void conditionallyUpdateScanResults(boolean z) {
        if (this.mWifiManager.getWifiState() == 1) {
            updateStandardWifiEntryScans(Collections.emptyList());
            updatePasspointWifiEntryScans(Collections.emptyList());
            return;
        }
        long j = this.mMaxScanAgeMillis;
        if (z) {
            this.mScanResultUpdater.update(this.mWifiManager.getScanResults());
        } else {
            j += this.mScanIntervalMillis;
        }
        updateStandardWifiEntryScans(this.mScanResultUpdater.getScanResults(j));
        updatePasspointWifiEntryScans(this.mScanResultUpdater.getScanResults(j));
    }

    private void updateStandardWifiEntryConfigs(List<WifiConfiguration> list) {
        Preconditions.checkNotNull(list, "Config list should not be null!");
        Map map = (Map) list.stream().collect(Collectors.toMap($$Lambda$eRhiL3TPu1j8op3nmit378jGeyk.INSTANCE, Function.identity()));
        this.mStandardWifiEntryCache.entrySet().removeIf(new Predicate(map) { // from class: com.android.wifitrackerlib.-$$Lambda$SavedNetworkTracker$Uwm7U8CRSvjmODKGQBR_weIFtK8
            public final /* synthetic */ Map f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return SavedNetworkTracker.lambda$updateStandardWifiEntryConfigs$1(this.f$0, (Map.Entry) obj);
            }
        });
        for (String str : map.keySet()) {
            this.mStandardWifiEntryCache.put(str, new StandardWifiEntry(this.mContext, this.mMainHandler, str, (WifiConfiguration) map.get(str), this.mWifiManager, this.mWifiNetworkScoreCache, true));
        }
    }

    static /* synthetic */ boolean lambda$updateStandardWifiEntryConfigs$1(Map map, Map.Entry entry) {
        StandardWifiEntry standardWifiEntry = (StandardWifiEntry) entry.getValue();
        standardWifiEntry.updateConfig((WifiConfiguration) map.remove(standardWifiEntry.getKey()));
        return !standardWifiEntry.isSaved();
    }

    private void updatePasspointWifiEntryConfigs(List<PasspointConfiguration> list) {
        Preconditions.checkNotNull(list, "Config list should not be null!");
        Map map = (Map) list.stream().collect(Collectors.toMap($$Lambda$SavedNetworkTracker$GiPU7UrK85F3w9N7PMlA7M9niw.INSTANCE, Function.identity()));
        this.mPasspointWifiEntryCache.entrySet().removeIf(new Predicate(map) { // from class: com.android.wifitrackerlib.-$$Lambda$SavedNetworkTracker$eyseTp76DXJATqUf1N-bJ_EBfOw
            public final /* synthetic */ Map f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return SavedNetworkTracker.lambda$updatePasspointWifiEntryConfigs$3(this.f$0, (Map.Entry) obj);
            }
        });
        for (String str : map.keySet()) {
            this.mPasspointWifiEntryCache.put(str, new PasspointWifiEntry(this.mContext, this.mMainHandler, (PasspointConfiguration) map.get(str), this.mWifiManager, this.mWifiNetworkScoreCache, true));
        }
    }

    static /* synthetic */ boolean lambda$updatePasspointWifiEntryConfigs$3(Map map, Map.Entry entry) {
        PasspointWifiEntry passpointWifiEntry = (PasspointWifiEntry) entry.getValue();
        PasspointConfiguration passpointConfiguration = (PasspointConfiguration) map.remove(passpointWifiEntry.getKey());
        if (passpointConfiguration == null) {
            return true;
        }
        passpointWifiEntry.updatePasspointConfig(passpointConfiguration);
        return false;
    }

    private void notifyOnSavedWifiEntriesChanged() {
        SavedNetworkTrackerCallback savedNetworkTrackerCallback = this.mListener;
        if (savedNetworkTrackerCallback != null) {
            Handler handler = this.mMainHandler;
            Objects.requireNonNull(savedNetworkTrackerCallback);
            handler.post(new Runnable() { // from class: com.android.wifitrackerlib.-$$Lambda$h1BuAemuREs5Akn8nay-wLEicng
                @Override // java.lang.Runnable
                public final void run() {
                    SavedNetworkTracker.SavedNetworkTrackerCallback.this.onSavedWifiEntriesChanged();
                }
            });
        }
    }

    private void notifyOnSubscriptionWifiEntriesChanged() {
        SavedNetworkTrackerCallback savedNetworkTrackerCallback = this.mListener;
        if (savedNetworkTrackerCallback != null) {
            Handler handler = this.mMainHandler;
            Objects.requireNonNull(savedNetworkTrackerCallback);
            handler.post(new Runnable() { // from class: com.android.wifitrackerlib.-$$Lambda$bWK0YQGHCIgfdyydB3bBJO0pQoM
                @Override // java.lang.Runnable
                public final void run() {
                    SavedNetworkTracker.SavedNetworkTrackerCallback.this.onSubscriptionWifiEntriesChanged();
                }
            });
        }
    }
}
