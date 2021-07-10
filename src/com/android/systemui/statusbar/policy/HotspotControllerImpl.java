package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.TetheringManager;
import android.net.wifi.WifiClient;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.util.ConcurrentUtils;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.statusbar.policy.HotspotController;
import com.oneplus.util.OpUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public class HotspotControllerImpl implements HotspotController, WifiManager.SoftApCallback {
    private static final boolean DEBUG = Log.isLoggable("HotspotController", 3);
    private static final boolean DEBUG_ONEPLUS = Build.DEBUG_ONEPLUS;
    private String[] mBluetoothRegexs;
    private final ArrayList<HotspotController.Callback> mCallbacks = new ArrayList<>();
    private ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private boolean mDisableByOperator = false;
    private volatile boolean mHasTetherableWifiRegexs = true;
    private int mHotspotState;
    private volatile boolean mIsTetheringSupported = true;
    private int mLastNotificationId;
    private final Handler mMainHandler;
    private Network mNetwork;
    private volatile int mNumConnectedDevices;
    private final Object mPublicSync = new Object();
    private BroadcastReceiver mTetherChangeReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.policy.HotspotControllerImpl.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.net.conn.TETHER_STATE_CHANGED")) {
                HotspotControllerImpl.this.updateState();
            }
        }
    };
    private GlobalSetting mTetheredData;
    private Notification.Builder mTetheredNotificationBuilder;
    private TetheringManager.TetheringEventCallback mTetheringCallback = new TetheringManager.TetheringEventCallback() { // from class: com.android.systemui.statusbar.policy.HotspotControllerImpl.1
        public void onTetheringSupported(boolean z) {
            if (HotspotControllerImpl.this.mIsTetheringSupported != z) {
                HotspotControllerImpl.this.mIsTetheringSupported = z;
                HotspotControllerImpl.this.fireHotspotAvailabilityChanged();
            }
        }

        public void onTetherableInterfaceRegexpsChanged(TetheringManager.TetheringInterfaceRegexps tetheringInterfaceRegexps) {
            boolean z = tetheringInterfaceRegexps.getTetherableWifiRegexs().size() != 0;
            if (HotspotControllerImpl.this.mHasTetherableWifiRegexs != z) {
                HotspotControllerImpl.this.mHasTetherableWifiRegexs = z;
                HotspotControllerImpl.this.fireHotspotAvailabilityChanged();
            }
        }

        public void onUpstreamChanged(Network network) {
            HotspotControllerImpl.this.mNetwork = network;
        }

        public void onTetheredInterfacesChanged(List<String> list) {
            if (HotspotControllerImpl.DEBUG_ONEPLUS) {
                Log.d("HotspotController", "onTetheredInterfacesChanged size " + list.size());
                for (String str : list) {
                    Log.d("HotspotController", "onTetheredInterfacesChanged:" + list);
                }
            }
        }
    };
    private final TetheringManager mTetheringManager;
    private String[] mUsbRegexs;
    private boolean mWaitingForTerminalState;
    private final WifiManager mWifiManager;
    private String[] mWifiRegexs;

    private static String stateToString(int i) {
        switch (i) {
            case 10:
                return "DISABLING";
            case 11:
                return "DISABLED";
            case 12:
                return "ENABLING";
            case 13:
                return "ENABLED";
            case 14:
                return "FAILED";
            default:
                return null;
        }
    }

    public int getHotspotWifiStandard() {
        return 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateState() {
        try {
            String[] tetheredIfaces = this.mConnectivityManager.getTetheredIfaces();
            boolean z = false;
            boolean z2 = false;
            boolean z3 = false;
            for (String str : tetheredIfaces) {
                for (String str2 : this.mUsbRegexs) {
                    if (str.matches(str2)) {
                        z = true;
                    }
                }
                for (String str3 : this.mBluetoothRegexs) {
                    if (str.matches(str3)) {
                        z3 = true;
                    }
                }
                for (String str4 : this.mWifiRegexs) {
                    if (str.matches(str4)) {
                        z2 = true;
                    }
                }
            }
            if (DEBUG_ONEPLUS) {
                Log.d("HotspotController", "updateState usbTethered " + z + " wifiTethered " + z2 + " bluetoothTethered " + z3 + " isHotspotEnabled " + isHotspotEnabled());
            }
            if (z) {
                if (!z2) {
                    if (!z3) {
                        showTetheredNotification(15);
                        return;
                    }
                }
                showTetheredNotification(14);
            } else if (z2) {
                if (z3) {
                    showTetheredNotification(14);
                } else {
                    showTetheredNotification(54088890);
                }
            } else if (z3) {
                showTetheredNotification(16);
            } else {
                clearTetheredNotification();
            }
        } catch (Exception e) {
            Log.e("HotspotController", "showTetheredNotification e:" + e);
        }
    }

    public HotspotControllerImpl(Context context, Handler handler, Handler handler2) {
        this.mContext = context;
        this.mTetheringManager = (TetheringManager) context.getSystemService(TetheringManager.class);
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mMainHandler = handler;
        this.mTetheringManager.registerTetheringEventCallback(new HandlerExecutor(handler2), this.mTetheringCallback);
    }

    @Override // com.android.systemui.statusbar.policy.HotspotController
    public void onBootCompleted() {
        Log.i("HotspotController", "onBootComplete");
        initRegexs();
    }

    private void initRegexs() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mConnectivityManager = connectivityManager;
        this.mUsbRegexs = connectivityManager.getTetherableUsbRegexs();
        this.mBluetoothRegexs = this.mConnectivityManager.getTetherableBluetoothRegexs();
        this.mWifiRegexs = this.mConnectivityManager.getTetherableWifiRegexs();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.TETHER_STATE_CHANGED");
        this.mContext.registerReceiver(this.mTetherChangeReceiver, intentFilter);
        if (OpUtils.isUSS()) {
            AnonymousClass3 r0 = new GlobalSetting(this.mContext, null, "TetheredData") { // from class: com.android.systemui.statusbar.policy.HotspotControllerImpl.3
                /* access modifiers changed from: protected */
                @Override // com.android.systemui.qs.GlobalSetting
                public void handleValueChanged(int i) {
                    Log.i("HotspotController", "HotspotControllerImpl / handleValueChanged / value" + i);
                    HotspotControllerImpl hotspotControllerImpl = HotspotControllerImpl.this;
                    boolean z = true;
                    if (i != 1) {
                        z = false;
                    }
                    hotspotControllerImpl.mDisableByOperator = z;
                    if (HotspotControllerImpl.this.mDisableByOperator && HotspotControllerImpl.this.isHotspotEnabled()) {
                        HotspotControllerImpl.this.setHotspotEnabled(false);
                    }
                    HotspotControllerImpl hotspotControllerImpl2 = HotspotControllerImpl.this;
                    hotspotControllerImpl2.fireOperatorChangedCallback(hotspotControllerImpl2.isOperatorHotspotDisable());
                }
            };
            this.mTetheredData = r0;
            boolean z = true;
            r0.setListening(true);
            if (this.mTetheredData.getValue() != 1) {
                z = false;
            }
            this.mDisableByOperator = z;
        }
    }

    @Override // com.android.systemui.statusbar.policy.HotspotController
    public boolean isHotspotSupported() {
        return this.mIsTetheringSupported && this.mHasTetherableWifiRegexs && UserManager.get(this.mContext).isUserAdmin(ActivityManager.getCurrentUser());
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("HotspotController state:");
        printWriter.print("  available=");
        printWriter.println(isHotspotSupported());
        printWriter.print("  mHotspotState=");
        printWriter.println(stateToString(this.mHotspotState));
        printWriter.print("  mNumConnectedDevices=");
        printWriter.println(this.mNumConnectedDevices);
        printWriter.print("  mWaitingForTerminalState=");
        printWriter.println(this.mWaitingForTerminalState);
    }

    public void addCallback(HotspotController.Callback callback) {
        synchronized (this.mCallbacks) {
            if (callback != null) {
                if (!this.mCallbacks.contains(callback)) {
                    if (DEBUG) {
                        Log.d("HotspotController", "addCallback " + callback);
                    }
                    this.mCallbacks.add(callback);
                    if (this.mWifiManager != null) {
                        if (this.mCallbacks.size() == 1) {
                            this.mWifiManager.registerSoftApCallback(new HandlerExecutor(this.mMainHandler), this);
                        } else {
                            this.mMainHandler.post(new Runnable(callback) { // from class: com.android.systemui.statusbar.policy.-$$Lambda$HotspotControllerImpl$C17PPPxxCR-pTmr2izVaDhyC9AQ
                                public final /* synthetic */ HotspotController.Callback f$1;

                                {
                                    this.f$1 = r2;
                                }

                                @Override // java.lang.Runnable
                                public final void run() {
                                    HotspotControllerImpl.this.lambda$addCallback$0$HotspotControllerImpl(this.f$1);
                                }
                            });
                        }
                    }
                    if (OpUtils.isUSS()) {
                        callback.onOperatorHotspotChanged(isOperatorHotspotDisable());
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$addCallback$0 */
    public /* synthetic */ void lambda$addCallback$0$HotspotControllerImpl(HotspotController.Callback callback) {
        callback.onHotspotChanged(isHotspotEnabled(), this.mNumConnectedDevices, getHotspotWifiStandard());
    }

    public void removeCallback(HotspotController.Callback callback) {
        if (callback != null) {
            if (DEBUG) {
                Log.d("HotspotController", "removeCallback " + callback);
            }
            synchronized (this.mCallbacks) {
                this.mCallbacks.remove(callback);
                if (this.mCallbacks.isEmpty() && this.mWifiManager != null) {
                    this.mWifiManager.unregisterSoftApCallback(this);
                }
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.HotspotController
    public boolean isHotspotEnabled() {
        return this.mHotspotState == 13;
    }

    @Override // com.android.systemui.statusbar.policy.HotspotController
    public boolean isOperatorHotspotDisable() {
        if (OpUtils.isUSS()) {
            return this.mDisableByOperator;
        }
        return false;
    }

    @Override // com.android.systemui.statusbar.policy.HotspotController
    public boolean isHotspotTransient() {
        return this.mWaitingForTerminalState || this.mHotspotState == 12;
    }

    @Override // com.android.systemui.statusbar.policy.HotspotController
    public void setHotspotEnabled(boolean z) {
        if (this.mWaitingForTerminalState) {
            if (DEBUG) {
                Log.d("HotspotController", "Ignoring setHotspotEnabled; waiting for terminal state.");
            }
        } else if (z) {
            this.mWaitingForTerminalState = true;
            if (DEBUG) {
                Log.d("HotspotController", "Starting tethering");
            }
            this.mTetheringManager.startTethering(new TetheringManager.TetheringRequest.Builder(0).build(), ConcurrentUtils.DIRECT_EXECUTOR, new TetheringManager.StartTetheringCallback() { // from class: com.android.systemui.statusbar.policy.HotspotControllerImpl.4
                public void onTetheringFailed(int i) {
                    if (HotspotControllerImpl.DEBUG) {
                        Log.d("HotspotController", "onTetheringFailed");
                    }
                    HotspotControllerImpl.this.maybeResetSoftApState();
                    HotspotControllerImpl.this.fireHotspotChangedCallback();
                }
            });
        } else {
            this.mTetheringManager.stopTethering(0);
        }
    }

    @Override // com.android.systemui.statusbar.policy.HotspotController
    public int getNumConnectedDevices() {
        return this.mNumConnectedDevices;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fireHotspotChangedCallback() {
        ArrayList<HotspotController.Callback> arrayList;
        synchronized (this.mCallbacks) {
            arrayList = new ArrayList(this.mCallbacks);
        }
        for (HotspotController.Callback callback : arrayList) {
            callback.onHotspotChanged(isHotspotEnabled(), this.mNumConnectedDevices, getHotspotWifiStandard());
        }
        updateState();
    }

    private int getNetworkTransport(Network network) {
        NetworkCapabilities networkCapabilities = this.mConnectivityManager.getNetworkCapabilities(network);
        if (networkCapabilities == null) {
            return -1;
        }
        networkCapabilities.getTransportTypes();
        if (networkCapabilities.hasTransport(0)) {
            return 0;
        }
        if (networkCapabilities.hasTransport(1)) {
            return 1;
        }
        return -1;
    }

    private void showTetheredNotification(int i) {
        showTetheredNotification(i, true);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x0335  */
    /* JADX WARNING: Removed duplicated region for block: B:113:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0098  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x0297  */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x02c2 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void showTetheredNotification(int r18, boolean r19) {
        /*
        // Method dump skipped, instructions count: 832
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.HotspotControllerImpl.showTetheredNotification(int, boolean):void");
    }

    /* access modifiers changed from: protected */
    public void clearTetheredNotification() {
        int i;
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        if (notificationManager != null && (i = this.mLastNotificationId) != 0) {
            notificationManager.cancelAsUser(null, i, UserHandle.ALL);
            this.mLastNotificationId = 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fireHotspotAvailabilityChanged() {
        ArrayList<HotspotController.Callback> arrayList;
        synchronized (this.mCallbacks) {
            arrayList = new ArrayList(this.mCallbacks);
        }
        for (HotspotController.Callback callback : arrayList) {
            callback.onHotspotAvailabilityChanged(isHotspotSupported());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fireOperatorChangedCallback(boolean z) {
        if (DEBUG_ONEPLUS) {
            Log.i("HotspotController", "fireOperatorChangedCallback / enabled:" + z + " / codeStack:" + Log.getStackTraceString(new Throwable()));
        }
        synchronized (this.mCallbacks) {
            Iterator<HotspotController.Callback> it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onOperatorHotspotChanged(z);
            }
        }
    }

    public void onStateChanged(int i, int i2) {
        Log.d("HotspotController", "onStateChanged state " + i + " failureReason " + i2);
        this.mHotspotState = i;
        maybeResetSoftApState();
        if (!isHotspotEnabled()) {
            this.mNumConnectedDevices = 0;
        }
        fireHotspotChangedCallback();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void maybeResetSoftApState() {
        if (this.mWaitingForTerminalState) {
            int i = this.mHotspotState;
            if (!(i == 11 || i == 13)) {
                if (i == 14) {
                    this.mTetheringManager.stopTethering(0);
                } else {
                    return;
                }
            }
            this.mWaitingForTerminalState = false;
        }
    }

    public void onConnectedClientsChanged(List<WifiClient> list) {
        Log.d("HotspotController", "onConnectedClientsChanged " + list.size());
        this.mNumConnectedDevices = list.size();
        fireHotspotChangedCallback();
    }
}
