package com.oneplus.networkspeed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import com.android.systemui.C0011R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.tuner.TunerService;
import com.oneplus.networkspeed.NetworkSpeedController;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Timer;
public class NetworkSpeedControllerImpl extends BroadcastReceiver implements TunerService.Tunable, NetworkSpeedController {
    private static boolean DEBUG = false;
    private static long ERTRY_POINT = 0;
    private static int HANDRED = 0;
    private static String TAG = "NetworkSpeedController";
    private static int TEN = 10;
    private static int THOUSAND = 1000;
    private static String UNIT_GB = "GB";
    private static String UNIT_KB = "KB";
    private static String UNIT_MB = "MB";
    private static int UPDATE_INTERVAL = 3;
    private int MSG_MAYBE_STOP_NETWORTSPEED = 1001;
    private int MSG_UPDATE_NETWORTSPEED = 1000;
    private int MSG_UPDATE_SHOW = 1002;
    private int MSG_UPDATE_SPEED_ON_BG = 2001;
    private MyBackgroundHandler mBackgroundHandler = new MyBackgroundHandler(BackgroundThread.getHandler().getLooper());
    private boolean mBlockNetworkSpeed = true;
    private ConnectivityManager mConnectivityManager = null;
    private Context mContext;
    private MyHandler mHandler = new MyHandler(Looper.getMainLooper());
    private boolean mHotSpotEnable = false;
    private StatusBarIconController mIconController;
    private boolean mIsFirstLoad = true;
    private boolean mLastNetworkConnected = false;
    private final ArrayList<NetworkSpeedController.INetworkSpeedStateCallBack> mNetworkSpeedStateCallBack = new ArrayList<>();
    private boolean mNetworkTraceState = false;
    private boolean mShow = false;
    private String mSpeed = "";
    private MySpeedMachine mSpeedMachine = new MySpeedMachine();

    static {
        DEBUG = Build.DEBUG_ONEPLUS;
        HANDRED = 100;
        ERTRY_POINT = 1024;
    }

    public NetworkSpeedControllerImpl(Context context) {
        this.mContext = context;
        new Timer();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.TIME_SET");
        intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        this.mContext.registerReceiver(this, intentFilter);
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        StatusBarIconController statusBarIconController = (StatusBarIconController) Dependency.get(StatusBarIconController.class);
        this.mIconController = statusBarIconController;
        statusBarIconController.setOPCustView("networkspeed", C0011R$layout.status_bar_network_speed, this.mShow);
        this.mIconController.setIconVisibility("networkspeed", this.mShow);
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "icon_blacklist");
    }

    @Override // com.oneplus.networkspeed.NetworkSpeedController
    public void updateConnectivity(BitSet bitSet, BitSet bitSet2) {
        if (DEBUG) {
            String str = TAG;
            Log.d(str, "updateConnectivity connectedTransports:" + bitSet + " validatedTransports:" + bitSet2);
        }
        updateState();
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == "android.intent.action.TIME_SET") {
            updateState();
        } else if (action == "android.intent.action.TIMEZONE_CHANGED") {
            updateState();
        } else if (action == "android.net.wifi.WIFI_AP_STATE_CHANGED") {
            this.mHotSpotEnable = intent.getIntExtra("wifi_state", 14) == 13;
            if (DEBUG) {
                String str = TAG;
                Log.i(str, "HotSpot enable:" + this.mHotSpotEnable);
            }
            updateState();
        }
    }

    private String divToFractionDigits(long j, long j2, int i) {
        if (j2 == 0) {
            Log.i(TAG, "divisor shouldn't be 0");
            return "Error";
        }
        StringBuffer stringBuffer = new StringBuffer();
        long j3 = j / j2;
        long j4 = j % j2;
        stringBuffer.append(j3);
        if (i > 0) {
            stringBuffer.append(".");
            for (int i2 = 0; i2 < i; i2++) {
                long j5 = j4 * 10;
                long j6 = j5 / j2;
                j4 = j5 % j2;
                stringBuffer.append(j6);
            }
        }
        return stringBuffer.toString();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String formateSpeed(long j) {
        long j2;
        StringBuffer stringBuffer = new StringBuffer();
        String str = UNIT_KB;
        long j3 = ERTRY_POINT;
        int i = 0;
        if (j >= j3) {
            if (j < j3 || j >= ((long) THOUSAND) * j3) {
                long j4 = ERTRY_POINT;
                int i2 = THOUSAND;
                if (j < ((long) i2) * j4 || j >= j4 * j4 * ((long) i2)) {
                    long j5 = ERTRY_POINT;
                    long j6 = j5 * j5 * j5;
                    String str2 = UNIT_GB;
                    if (j < j5 * j5 * ((long) THOUSAND) || j >= ((long) TEN) * j6) {
                        long j7 = ERTRY_POINT;
                        if (j < j7 * j7 * j7 * ((long) TEN) || j >= ((long) HANDRED) * j6) {
                            j2 = j6;
                            str = str2;
                            stringBuffer.append(divToFractionDigits(j, j2, i));
                            stringBuffer.append(":");
                            stringBuffer.append(str);
                            stringBuffer.append("/S");
                            return stringBuffer.toString();
                        }
                        i = 1;
                    } else {
                        i = 2;
                    }
                    str = str2;
                    j2 = j6;
                    stringBuffer.append(divToFractionDigits(j, j2, i));
                    stringBuffer.append(":");
                    stringBuffer.append(str);
                    stringBuffer.append("/S");
                    return stringBuffer.toString();
                }
                long j8 = j4 * j4;
                String str3 = UNIT_MB;
                if (j < j4 * ((long) i2) || j >= ((long) TEN) * j8) {
                    long j9 = ERTRY_POINT;
                    if (j < j9 * j9 * ((long) TEN) || j >= ((long) HANDRED) * j8) {
                        long j10 = ERTRY_POINT;
                        if (j >= j10 * j10 * ((long) HANDRED)) {
                            int i3 = (j > (((long) THOUSAND) * j8) ? 1 : (j == (((long) THOUSAND) * j8) ? 0 : -1));
                        }
                        str = str3;
                    } else {
                        str = str3;
                        i = 1;
                    }
                } else {
                    str = str3;
                    i = 2;
                }
                j2 = j8;
                stringBuffer.append(divToFractionDigits(j, j2, i));
                stringBuffer.append(":");
                stringBuffer.append(str);
                stringBuffer.append("/S");
                return stringBuffer.toString();
            } else if (j < j3 || j >= ((long) TEN) * j3) {
                long j11 = ERTRY_POINT;
                if (j < ((long) TEN) * j11 || j >= j11 * ((long) HANDRED)) {
                    long j12 = ERTRY_POINT;
                    if (j >= ((long) HANDRED) * j12) {
                        int i4 = (j > (j12 * ((long) THOUSAND)) ? 1 : (j == (j12 * ((long) THOUSAND)) ? 0 : -1));
                    }
                    j2 = j3;
                    stringBuffer.append(divToFractionDigits(j, j2, i));
                    stringBuffer.append(":");
                    stringBuffer.append(str);
                    stringBuffer.append("/S");
                    return stringBuffer.toString();
                }
                i = 1;
                j2 = j3;
                stringBuffer.append(divToFractionDigits(j, j2, i));
                stringBuffer.append(":");
                stringBuffer.append(str);
                stringBuffer.append("/S");
                return stringBuffer.toString();
            }
        }
        i = 2;
        j2 = j3;
        stringBuffer.append(divToFractionDigits(j, j2, i));
        stringBuffer.append(":");
        stringBuffer.append(str);
        stringBuffer.append("/S");
        return stringBuffer.toString();
    }

    public void updateState() {
        boolean isNetworkSpeedTracing = isNetworkSpeedTracing();
        if (DEBUG) {
            String str = TAG;
            Log.d(str, "updateState traceState:" + isNetworkSpeedTracing);
        }
        if (this.mNetworkTraceState != isNetworkSpeedTracing) {
            this.mNetworkTraceState = isNetworkSpeedTracing;
            if (isNetworkSpeedTracing) {
                onStartTraceSpeed();
            } else {
                onStopTraceSpeed();
            }
            Message obtainMessage = this.mHandler.obtainMessage();
            obtainMessage.what = this.MSG_UPDATE_SHOW;
            this.mHandler.sendMessage(obtainMessage);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onShowStateChange() {
        boolean z = this.mNetworkTraceState;
        if (this.mShow != z) {
            this.mShow = z;
            if (DEBUG) {
                String str = TAG;
                Log.d(str, "onShowStateChange s:" + z);
            }
            StatusBarIconController statusBarIconController = this.mIconController;
            if (statusBarIconController != null) {
                statusBarIconController.setIconVisibility("networkspeed", z);
            }
            Iterator<NetworkSpeedController.INetworkSpeedStateCallBack> it = this.mNetworkSpeedStateCallBack.iterator();
            while (it.hasNext()) {
                it.next().onSpeedShow(z);
            }
        }
    }

    private void onStartTraceSpeed() {
        if (DEBUG) {
            Log.d(TAG, "onStartTraceSpeed");
        }
        updateSpeed();
    }

    private void onStopTraceSpeed() {
        if (DEBUG) {
            Log.d(TAG, "onStopTraceSpeed");
        }
        this.mIsFirstLoad = true;
        stopSpeed();
        this.mSpeed = "";
    }

    private void updateSpeed() {
        this.mIsFirstLoad = true;
        if (DEBUG) {
            Log.d(TAG, "updateSpeed");
        }
        this.mSpeed = "";
        Message obtainMessage = this.mHandler.obtainMessage();
        obtainMessage.what = this.MSG_UPDATE_NETWORTSPEED;
        obtainMessage.obj = this.mSpeed;
        this.mHandler.sendMessage(obtainMessage);
        MySpeedMachine mySpeedMachine = this.mSpeedMachine;
        if (mySpeedMachine != null) {
            mySpeedMachine.reset();
            this.mSpeedMachine.setTurnOn();
        }
        this.mBackgroundHandler.removeMessages(this.MSG_UPDATE_SPEED_ON_BG);
        Message message = new Message();
        message.what = this.MSG_UPDATE_SPEED_ON_BG;
        this.mBackgroundHandler.sendMessage(message);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleNextUpdate() {
        long uptimeMillis = SystemClock.uptimeMillis() + ((long) (UPDATE_INTERVAL * 1000));
        Message message = new Message();
        message.what = this.MSG_UPDATE_SPEED_ON_BG;
        this.mBackgroundHandler.sendMessageAtTime(message, uptimeMillis);
    }

    private void stopSpeed() {
        MySpeedMachine mySpeedMachine = this.mSpeedMachine;
        if (mySpeedMachine != null) {
            mySpeedMachine.reset();
            this.mSpeedMachine.setTurnOff();
        }
        this.mBackgroundHandler.removeMessages(this.MSG_UPDATE_SPEED_ON_BG);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshSpeed() {
        Iterator<NetworkSpeedController.INetworkSpeedStateCallBack> it = this.mNetworkSpeedStateCallBack.iterator();
        while (it.hasNext()) {
            NetworkSpeedController.INetworkSpeedStateCallBack next = it.next();
            if (next != null) {
                next.onSpeedChange(this.mSpeed);
            }
        }
    }

    /* access modifiers changed from: private */
    public class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == NetworkSpeedControllerImpl.this.MSG_UPDATE_NETWORTSPEED) {
                Object obj = message.obj;
                if (obj instanceof String) {
                    NetworkSpeedControllerImpl.this.mSpeed = (String) obj;
                    NetworkSpeedControllerImpl.this.refreshSpeed();
                }
            } else if (i == NetworkSpeedControllerImpl.this.MSG_MAYBE_STOP_NETWORTSPEED) {
                NetworkSpeedControllerImpl.this.updateState();
            } else if (i == NetworkSpeedControllerImpl.this.MSG_UPDATE_SHOW) {
                NetworkSpeedControllerImpl.this.onShowStateChange();
            }
        }
    }

    /* access modifiers changed from: private */
    public class MyBackgroundHandler extends Handler {
        public MyBackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == NetworkSpeedControllerImpl.this.MSG_UPDATE_SPEED_ON_BG) {
                NetworkSpeedControllerImpl.this.mBackgroundHandler.removeMessages(NetworkSpeedControllerImpl.this.MSG_UPDATE_SPEED_ON_BG);
                if (NetworkSpeedControllerImpl.this.mSpeedMachine.isTurnOn()) {
                    NetworkSpeedControllerImpl.this.mSpeedMachine.updateSpeedonBG();
                    NetworkSpeedControllerImpl.this.scheduleNextUpdate();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class MySpeedMachine {
        long incrementRxBytes = 0;
        long incrementTxBytes = 0;
        boolean isTurnOn = false;
        long oldRxBytes = 0;
        long oldTxBytes = 0;

        public MySpeedMachine() {
            reset();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void updateSpeedonBG() {
            if (NetworkSpeedControllerImpl.this.isNetworkSpeedTracing()) {
                long totalTxBytes = TrafficStats.getTotalTxBytes();
                long totalRxBytes = TrafficStats.getTotalRxBytes();
                this.incrementTxBytes = totalTxBytes - this.oldTxBytes;
                this.incrementRxBytes = totalRxBytes - this.oldRxBytes;
                this.oldTxBytes = totalTxBytes;
                this.oldRxBytes = totalRxBytes;
                if (NetworkSpeedControllerImpl.this.mIsFirstLoad) {
                    if (NetworkSpeedControllerImpl.DEBUG) {
                        Log.d(NetworkSpeedControllerImpl.TAG, "NetWorkSpeed is first load.");
                    }
                    this.incrementTxBytes = 0;
                    this.incrementRxBytes = 0;
                    NetworkSpeedControllerImpl.this.mIsFirstLoad = false;
                }
                if (this.incrementTxBytes < 0) {
                    this.incrementTxBytes = 0;
                }
                if (this.incrementRxBytes < 0) {
                    this.incrementRxBytes = 0;
                }
                long j = this.incrementRxBytes;
                long j2 = this.incrementTxBytes;
                if (j <= j2) {
                    j = j2;
                }
                long j3 = j / ((long) NetworkSpeedControllerImpl.UPDATE_INTERVAL);
                String formateSpeed = NetworkSpeedControllerImpl.this.formateSpeed(j3);
                if (NetworkSpeedControllerImpl.DEBUG) {
                    String str = NetworkSpeedControllerImpl.TAG;
                    Log.d(str, "NetWorkSpeed refresh totalTxBytes=" + totalTxBytes + ", totalRxBytes=" + totalRxBytes + ", incrementPs=" + j3 + ", mSpeed=" + formateSpeed + " ,incrementBytes:" + j);
                }
                Message obtainMessage = NetworkSpeedControllerImpl.this.mHandler.obtainMessage();
                obtainMessage.what = NetworkSpeedControllerImpl.this.MSG_UPDATE_NETWORTSPEED;
                obtainMessage.obj = formateSpeed;
                NetworkSpeedControllerImpl.this.mHandler.sendMessage(obtainMessage);
                return;
            }
            Message obtainMessage2 = NetworkSpeedControllerImpl.this.mHandler.obtainMessage();
            obtainMessage2.what = NetworkSpeedControllerImpl.this.MSG_MAYBE_STOP_NETWORTSPEED;
            NetworkSpeedControllerImpl.this.mHandler.sendMessage(obtainMessage2);
            Log.d(NetworkSpeedControllerImpl.TAG, "send MSG_CLOSE_NETWORTSPEED");
        }

        public void reset() {
            this.oldTxBytes = 0;
            this.incrementTxBytes = 0;
            this.oldRxBytes = 0;
            this.incrementRxBytes = 0;
        }

        public void setTurnOn() {
            this.isTurnOn = true;
        }

        public void setTurnOff() {
            this.isTurnOn = false;
        }

        public boolean isTurnOn() {
            return this.isTurnOn;
        }
    }

    private boolean isNetworkConnected() {
        boolean z = false;
        if (this.mContext == null) {
            return false;
        }
        NetworkInfo networkInfo = null;
        ConnectivityManager connectivityManager = this.mConnectivityManager;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        if (networkInfo != null && networkInfo.isAvailable()) {
            z = true;
        }
        if (this.mLastNetworkConnected != z) {
            if (DEBUG) {
                String str = TAG;
                Log.v(str, "isNetworkConnected = " + z);
            }
            this.mLastNetworkConnected = z;
        }
        return z;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNetworkSpeedTracing() {
        return isNetworkConnected() && !this.mBlockNetworkSpeed;
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        boolean contains;
        if ("icon_blacklist".equals(str) && (contains = StatusBarIconController.getIconBlacklist(this.mContext, str2).contains("networkspeed")) != this.mBlockNetworkSpeed) {
            String str3 = TAG;
            Log.i(str3, " onTuningChanged blocknetworkSpeed:" + contains);
            this.mBlockNetworkSpeed = contains;
            updateState();
        }
    }

    public void addCallback(NetworkSpeedController.INetworkSpeedStateCallBack iNetworkSpeedStateCallBack) {
        synchronized (this) {
            this.mNetworkSpeedStateCallBack.add(iNetworkSpeedStateCallBack);
            try {
                iNetworkSpeedStateCallBack.onSpeedChange(this.mSpeed);
                iNetworkSpeedStateCallBack.onSpeedShow(this.mShow);
            } catch (Exception e) {
                Slog.w(TAG, "Failed to call to IKeyguardStateCallback", e);
            }
        }
    }

    public void removeCallback(NetworkSpeedController.INetworkSpeedStateCallBack iNetworkSpeedStateCallBack) {
        synchronized (this) {
            this.mNetworkSpeedStateCallBack.remove(iNetworkSpeedStateCallBack);
        }
    }
}
