package com.oneplus.aod.utils.bitmoji.download;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import com.oneplus.aod.utils.bitmoji.download.OpBitmojiNetworkTypeObserver;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: OpBitmojiNetworkTypeObserver.kt */
public final class OpBitmojiNetworkTypeObserver extends ConnectivityManager.NetworkCallback {
    private static final NetworkRequest REQUEST = new NetworkRequest.Builder().addTransportType(0).addTransportType(1).build();
    @NotNull
    private static final String TAG = "OpBitmojiNetworkTypeObserver";
    private ConnectivityManager mConnectivityManager;
    private Handler mHandler;
    private OnNetworkTypeChangeListener mListener;
    private int mNetworkType = -1;

    /* compiled from: OpBitmojiNetworkTypeObserver.kt */
    public interface OnNetworkTypeChangeListener {
        void onNetworkTypeChange(int i);
    }

    @Override // android.net.ConnectivityManager.NetworkCallback
    public void onAvailable(@NotNull Network network) {
        Intrinsics.checkParameterIsNotNull(network, "network");
    }

    public OpBitmojiNetworkTypeObserver(@NotNull Context context, @NotNull OnNetworkTypeChangeListener onNetworkTypeChangeListener) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(onNetworkTypeChangeListener, "listener");
        this.mListener = onNetworkTypeChangeListener;
        Object systemService = context.getSystemService(ConnectivityManager.class);
        Intrinsics.checkExpressionValueIsNotNull(systemService, "context.getSystemService…ivityManager::class.java)");
        this.mConnectivityManager = (ConnectivityManager) systemService;
        this.mHandler = new Handler();
    }

    public final void prepare() {
        updateCurrentNetworkType();
        this.mConnectivityManager.registerNetworkCallback(REQUEST, this);
    }

    @Override // android.net.ConnectivityManager.NetworkCallback
    public void onCapabilitiesChanged(@NotNull Network network, @NotNull NetworkCapabilities networkCapabilities) {
        Intrinsics.checkParameterIsNotNull(network, "network");
        Intrinsics.checkParameterIsNotNull(networkCapabilities, "networkCapabilities");
        updateCurrentNetworkType();
    }

    @Override // android.net.ConnectivityManager.NetworkCallback
    public void onLost(@NotNull Network network) {
        Intrinsics.checkParameterIsNotNull(network, "network");
        updateCurrentNetworkType();
    }

    public final boolean isNetworkUnavailable() {
        return this.mNetworkType == -1;
    }

    public final boolean isNetworkTypeMobile() {
        return this.mNetworkType == 0;
    }

    public final boolean isNetworkTypeWifi() {
        return this.mNetworkType == 1;
    }

    private final void updateCurrentNetworkType() {
        ConnectivityManager connectivityManager = this.mConnectivityManager;
        NetworkCapabilities networkCapabilities = null;
        Network activeNetwork = connectivityManager != null ? connectivityManager.getActiveNetwork() : null;
        ConnectivityManager connectivityManager2 = this.mConnectivityManager;
        if (connectivityManager2 != null) {
            networkCapabilities = connectivityManager2.getNetworkCapabilities(activeNetwork);
        }
        int i = -1;
        if (networkCapabilities != null) {
            if (networkCapabilities.hasTransport(0)) {
                i = 0;
            } else if (networkCapabilities.hasTransport(1)) {
                i = 1;
            }
        }
        if (this.mNetworkType != i) {
            if (Build.DEBUG_ONEPLUS) {
                Log.i(TAG, "network type change from " + this.mNetworkType + " to " + i);
            }
            this.mNetworkType = i;
            OnNetworkTypeChangeListener onNetworkTypeChangeListener = this.mListener;
            if (onNetworkTypeChangeListener != null) {
                this.mHandler.post(new Runnable(onNetworkTypeChangeListener, this) { // from class: com.oneplus.aod.utils.bitmoji.download.OpBitmojiNetworkTypeObserver$updateCurrentNetworkType$$inlined$let$lambda$1
                    final /* synthetic */ OpBitmojiNetworkTypeObserver.OnNetworkTypeChangeListener $it;
                    final /* synthetic */ OpBitmojiNetworkTypeObserver this$0;

                    {
                        this.$it = r1;
                        this.this$0 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        this.$it.onNetworkTypeChange(OpBitmojiNetworkTypeObserver.access$getMNetworkType$p(this.this$0));
                    }
                });
            }
        }
    }
}
