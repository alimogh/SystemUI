package com.android.wifitrackerlib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkKey;
import android.net.NetworkRequest;
import android.net.NetworkScoreManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkScoreCache;
import android.os.Handler;
import android.util.Log;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import com.android.wifitrackerlib.BaseWifiTracker;
import java.util.Objects;
import java.util.Set;
public class BaseWifiTracker implements LifecycleObserver {
    private static boolean sVerboseLogging;
    private final BroadcastReceiver mBroadcastReceiver;
    protected final ConnectivityManager mConnectivityManager;
    protected final Context mContext;
    protected final Handler mMainHandler;
    protected final long mMaxScanAgeMillis;
    private final ConnectivityManager.NetworkCallback mNetworkCallback;
    private final NetworkRequest mNetworkRequest;
    protected final NetworkScoreManager mNetworkScoreManager;
    private final Set<NetworkKey> mRequestedScoreKeys;
    protected final long mScanIntervalMillis;
    protected final ScanResultUpdater mScanResultUpdater;
    private final Scanner mScanner;
    private final String mTag;
    protected final WifiManager mWifiManager;
    protected final WifiNetworkScoreCache mWifiNetworkScoreCache;
    protected final Handler mWorkerHandler;

    public void handleOnStart() {
    }

    public static boolean isVerboseLoggingEnabled() {
        return sVerboseLogging;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        intentFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter, null, this.mWorkerHandler);
        this.mConnectivityManager.registerNetworkCallback(this.mNetworkRequest, this.mNetworkCallback, this.mWorkerHandler);
        this.mNetworkScoreManager.registerNetworkScoreCache(1, this.mWifiNetworkScoreCache, 2);
        if (this.mWifiManager.getWifiState() == 3) {
            Handler handler = this.mWorkerHandler;
            Scanner scanner = this.mScanner;
            Objects.requireNonNull(scanner);
            handler.post(new Runnable() { // from class: com.android.wifitrackerlib.-$$Lambda$BaseWifiTracker$pw9dhfMm5LbxL1gY1jP_JJmuRkU
                @Override // java.lang.Runnable
                public final void run() {
                    BaseWifiTracker.lambda$onStart$0(BaseWifiTracker.Scanner.this);
                }
            });
        } else {
            Handler handler2 = this.mWorkerHandler;
            Scanner scanner2 = this.mScanner;
            Objects.requireNonNull(scanner2);
            handler2.post(new Runnable() { // from class: com.android.wifitrackerlib.-$$Lambda$BaseWifiTracker$KvY90710q6wRJD152kDBMW9ndFs
                @Override // java.lang.Runnable
                public final void run() {
                    BaseWifiTracker.lambda$onStart$1(BaseWifiTracker.Scanner.this);
                }
            });
        }
        this.mWorkerHandler.post(new Runnable() { // from class: com.android.wifitrackerlib.-$$Lambda$S9fuCAjG-YC38JCa05_AkNB8B-E
            @Override // java.lang.Runnable
            public final void run() {
                BaseWifiTracker.this.handleOnStart();
            }
        });
    }

    static /* synthetic */ void lambda$onStart$0(Scanner scanner) {
        scanner.start();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        Handler handler = this.mWorkerHandler;
        Scanner scanner = this.mScanner;
        Objects.requireNonNull(scanner);
        handler.post(new Runnable() { // from class: com.android.wifitrackerlib.-$$Lambda$BaseWifiTracker$QlWe0ki0RbTEAz4j0GPGXOVBvC0
            @Override // java.lang.Runnable
            public final void run() {
                BaseWifiTracker.lambda$onStop$2(BaseWifiTracker.Scanner.this);
            }
        });
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        this.mConnectivityManager.unregisterNetworkCallback(this.mNetworkCallback);
        this.mNetworkScoreManager.unregisterNetworkScoreCache(1, this.mWifiNetworkScoreCache);
        Handler handler2 = this.mWorkerHandler;
        Set<NetworkKey> set = this.mRequestedScoreKeys;
        Objects.requireNonNull(set);
        handler2.post(new Runnable(set) { // from class: com.android.wifitrackerlib.-$$Lambda$gWMviBWa7YejsjM1KbUOIv9j1JA
            public final /* synthetic */ Set f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.clear();
            }
        });
    }

    public class Scanner extends Handler {
        private int mRetry;
        final /* synthetic */ BaseWifiTracker this$0;

        private void start() {
            if (BaseWifiTracker.isVerboseLoggingEnabled()) {
                Log.v(this.this$0.mTag, "Scanner start");
            }
            postScan();
        }

        /* access modifiers changed from: public */
        private void stop() {
            if (BaseWifiTracker.isVerboseLoggingEnabled()) {
                Log.v(this.this$0.mTag, "Scanner stop");
            }
            this.mRetry = 0;
            removeCallbacksAndMessages(null);
        }

        /* access modifiers changed from: public */
        private void postScan() {
            if (this.this$0.mWifiManager.startScan()) {
                this.mRetry = 0;
            } else {
                int i = this.mRetry + 1;
                this.mRetry = i;
                if (i >= 3) {
                    if (BaseWifiTracker.isVerboseLoggingEnabled()) {
                        String str = this.this$0.mTag;
                        Log.v(str, "Scanner failed to start scan " + this.mRetry + " times!");
                    }
                    this.mRetry = 0;
                    return;
                }
            }
            postDelayed(new Runnable() { // from class: com.android.wifitrackerlib.-$$Lambda$BaseWifiTracker$Scanner$Lob1PHu6bdjiK_7H86IDLNF_WiM
                @Override // java.lang.Runnable
                public final void run() {
                    BaseWifiTracker.Scanner.lambda$Lob1PHu6bdjiK_7H86IDLNF_WiM(BaseWifiTracker.Scanner.this);
                }
            }, this.this$0.mScanIntervalMillis);
        }
    }
}
