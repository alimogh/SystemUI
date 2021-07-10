package com.android.systemui.statusbar.policy;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.util.OpFeatures;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.C0001R$array;
import com.android.systemui.C0009R$integer;
import com.android.systemui.statusbar.policy.MobileSignalController;
import com.android.systemui.util.ProductUtils;
import com.google.android.collect.Lists;
import com.oneplus.util.OpUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import org.codeaurora.internal.BearerAllocationStatus;
import org.codeaurora.internal.Client;
import org.codeaurora.internal.IExtTelephony;
import org.codeaurora.internal.INetworkCallback;
import org.codeaurora.internal.NetworkCallbackBase;
import org.codeaurora.internal.NrConfigType;
import org.codeaurora.internal.NrIconType;
import org.codeaurora.internal.ServiceUtil;
import org.codeaurora.internal.SignalStrength;
import org.codeaurora.internal.Status;
import org.codeaurora.internal.Token;
public class FiveGServiceClient {
    private static final boolean DEBUG = true;
    private static final int DELAY_NO_NR_ICON = (OpFeatures.isSupport(new int[]{0}) ? 30000 : 10000);
    private static FiveGServiceClient sInstance;
    private String isChinaMobile = SystemProperties.get("persist.radio.ischinamobile", "0");
    private String isChinaTelecom = SystemProperties.get("persist.radio.ischinatelecom", "0");
    private int mBindRetryTimes = 0;
    @VisibleForTesting
    protected INetworkCallback mCallback;
    private Client mClient;
    private Context mContext;
    private NrIconDelayTimer mCountDownTimer;
    private final SparseArray<FiveGServiceState> mCurrentServiceStates = new SparseArray<>();
    private final int[] mFivebarRsrpThresholds;
    private Handler mHandler;
    private int mInitRetryTimes = 0;
    private final ArrayList<WeakReference<KeyguardUpdateMonitorCallback>> mKeyguardUpdateMonitorCallbacks = Lists.newArrayList();
    private final SparseBooleanArray mLast5GEnabledStatus = new SparseBooleanArray();
    private final SparseIntArray mLastNrIconType = new SparseIntArray();
    private final SparseArray<FiveGServiceState> mLastServiceStates = new SparseArray<>();
    private IExtTelephony mNetworkService;
    private String mPackageName;
    private final SparseBooleanArray mRegisterListenerStatus = new SparseBooleanArray();
    private final int[] mRsrpThresholds;
    private final SparseIntArray mScreenOnQueryToken = new SparseIntArray();
    private boolean mServiceConnected;
    private ServiceConnection mServiceConnection;
    @VisibleForTesting
    final SparseArray<IFiveGStateListener> mStatesListeners = new SparseArray<>();
    private SubscriptionManager mSubscriptionManager;

    public interface IFiveGStateListener {
        void onStateChanged(FiveGServiceState fiveGServiceState);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean is5GIcon(int i) {
        return i == 1 || i == 2;
    }

    static {
        Log.isLoggable("FiveGServiceClient", 3);
    }

    /* access modifiers changed from: private */
    public class NrIconDelayTimer extends CountDownTimer {
        private int mPhoneId = -1;
        private boolean mStarted = false;

        @Override // android.os.CountDownTimer
        public void onTick(long j) {
        }

        public NrIconDelayTimer(long j, long j2) {
            super(j, j2);
        }

        @Override // android.os.CountDownTimer
        public void onFinish() {
            Log.d("FiveGServiceClient", "delay complete " + this.mStarted + " " + this.mPhoneId);
            onNoneNrIcon();
            reset();
        }

        public synchronized void startDelay(int i) {
            if (!this.mStarted || this.mPhoneId != i) {
                if (this.mStarted) {
                    Log.w("FiveGServiceClient", "delay in progress but interrupt with other slot, notify no icon");
                    cancel();
                    onNoneNrIcon();
                }
                Log.d("FiveGServiceClient", "delay 30s to hide nr icon");
                this.mStarted = true;
                this.mPhoneId = i;
                start();
                return;
            }
            Log.w("FiveGServiceClient", "delay in progress");
        }

        public synchronized void cancelDelay(int i, int i2) {
            if (this.mStarted) {
                boolean z = false;
                boolean z2 = true;
                if (this.mPhoneId == i) {
                    Log.d("FiveGServiceClient", "delay canceled");
                    z = true;
                }
                if (!SubscriptionManager.isValidPhoneId(i2) || this.mPhoneId == i2) {
                    z2 = z;
                } else {
                    Log.w("FiveGServiceClient", "found no dds delay, notify no icon right now");
                    onNoneNrIcon();
                }
                Log.d("FiveGServiceClient", "cancelDelay " + z2);
                if (z2) {
                    reset();
                    cancel();
                }
            } else {
                Log.w("FiveGServiceClient", "delay not started.");
            }
        }

        private void reset() {
            this.mPhoneId = -1;
            this.mStarted = false;
        }

        private void onNoneNrIcon() {
            FiveGServiceClient.this.onNrIconTypeInternal(this.mPhoneId, 0);
        }
    }

    public static class FiveGServiceState {
        private int mBearerAllocationStatus = 0;
        private MobileSignalController.MobileIconGroup mIconGroup = TelephonyIcons.UNKNOWN;
        private int mLevel = 0;
        private int mNrConfigType = 0;
        private int mNrIconType = -1;
        private int mRsrp = -32768;

        public boolean isConnectedOnSaMode() {
            return this.mNrConfigType == 1 && this.mIconGroup != TelephonyIcons.UNKNOWN;
        }

        public boolean isConnectedOnNsaMode() {
            return this.mNrConfigType == 0 && this.mIconGroup != TelephonyIcons.UNKNOWN;
        }

        public boolean isNrIconTypeValid() {
            int i = this.mNrIconType;
            return (i == -1 || i == 0) ? false : true;
        }

        @VisibleForTesting
        public MobileSignalController.MobileIconGroup getIconGroup() {
            return this.mIconGroup;
        }

        @VisibleForTesting
        public int getSignalLevel() {
            return this.mLevel;
        }

        @VisibleForTesting
        public int getAllocated() {
            return this.mBearerAllocationStatus;
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public int getNrConfigType() {
            return this.mNrConfigType;
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public int getNrIconType() {
            return this.mNrIconType;
        }

        public int getRsrp() {
            return this.mRsrp;
        }

        public void copyFrom(FiveGServiceState fiveGServiceState) {
            this.mBearerAllocationStatus = fiveGServiceState.mBearerAllocationStatus;
            this.mLevel = fiveGServiceState.mLevel;
            this.mNrConfigType = fiveGServiceState.mNrConfigType;
            this.mIconGroup = fiveGServiceState.mIconGroup;
            this.mNrIconType = fiveGServiceState.mNrIconType;
            this.mRsrp = fiveGServiceState.mRsrp;
        }

        public boolean equals(FiveGServiceState fiveGServiceState) {
            return this.mLevel == fiveGServiceState.mLevel && this.mNrConfigType == fiveGServiceState.mNrConfigType && this.mIconGroup == fiveGServiceState.mIconGroup && this.mNrIconType == fiveGServiceState.mNrIconType && this.mBearerAllocationStatus == fiveGServiceState.mBearerAllocationStatus;
        }

        public String toString() {
            return "mLevel=" + this.mLevel + ", mNrConfigType=" + this.mNrConfigType + ", mIconGroup=" + this.mIconGroup + ", mNrIconType=" + this.mNrIconType + ", mBearerAllocationStatus=" + this.mBearerAllocationStatus + ", mRsrp=" + this.mRsrp;
        }
    }

    public FiveGServiceClient(Context context) {
        int i = DELAY_NO_NR_ICON;
        this.mCountDownTimer = new NrIconDelayTimer((long) i, (long) i);
        this.mHandler = new Handler() { // from class: com.android.systemui.statusbar.policy.FiveGServiceClient.1
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1024:
                        FiveGServiceClient.this.binderService();
                        return;
                    case 1025:
                        FiveGServiceClient.this.initFiveGServiceState();
                        return;
                    case 1026:
                        FiveGServiceClient.this.notifyMonitorCallback();
                        return;
                    case 1027:
                        Log.d("FiveGServiceClient", "MESSAGE_QUERY_FIVEG_SIGNAL");
                        for (int i2 = 0; i2 < FiveGServiceClient.this.mStatesListeners.size(); i2++) {
                            FiveGServiceClient.this.queryNrSignalStrength(FiveGServiceClient.this.mStatesListeners.keyAt(i2));
                        }
                        return;
                    default:
                        Log.d("FiveGServiceClient", "handleMessage: Unknown msg = " + message.what);
                        return;
                }
            }
        };
        this.mServiceConnection = new ServiceConnection() { // from class: com.android.systemui.statusbar.policy.FiveGServiceClient.2
            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d("FiveGServiceClient", "onServiceConnected:" + iBinder);
                try {
                    FiveGServiceClient.this.mNetworkService = IExtTelephony.Stub.asInterface(iBinder);
                    FiveGServiceClient.this.mClient = FiveGServiceClient.this.mNetworkService.registerCallback(FiveGServiceClient.this.mPackageName, FiveGServiceClient.this.mCallback);
                    FiveGServiceClient.this.mServiceConnected = true;
                    FiveGServiceClient.this.initFiveGServiceState();
                    Log.d("FiveGServiceClient", "Client = " + FiveGServiceClient.this.mClient);
                } catch (Exception e) {
                    Log.d("FiveGServiceClient", "onServiceConnected: Exception = " + e);
                }
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d("FiveGServiceClient", "onServiceDisconnected:" + componentName);
                cleanup();
            }

            @Override // android.content.ServiceConnection
            public void onBindingDied(ComponentName componentName) {
                Log.d("FiveGServiceClient", "onBindingDied:" + componentName);
                cleanup();
                if (FiveGServiceClient.this.mBindRetryTimes < 4) {
                    Log.d("FiveGServiceClient", "try to re-bind");
                    FiveGServiceClient.this.mHandler.sendEmptyMessageDelayed(1024, (long) ((FiveGServiceClient.this.mBindRetryTimes * 2000) + 3000));
                }
            }

            private void cleanup() {
                Log.d("FiveGServiceClient", "cleanup");
                FiveGServiceClient.this.mServiceConnected = false;
                FiveGServiceClient.this.mNetworkService = null;
                FiveGServiceClient.this.mClient = null;
            }
        };
        this.mCallback = new NetworkCallbackBase() { // from class: com.android.systemui.statusbar.policy.FiveGServiceClient.3
            public void onSignalStrength(int i2, Token token, Status status, SignalStrength signalStrength) throws RemoteException {
                if (FiveGServiceClient.DEBUG) {
                    Log.d("FiveGServiceClient", "onSignalStrength: slotId=" + i2 + " token=" + token + " status=" + status + " signalStrength=" + signalStrength);
                }
                if (status.get() == 1 && signalStrength != null) {
                    FiveGServiceState currentServiceState = FiveGServiceClient.this.getCurrentServiceState(i2);
                    currentServiceState.mLevel = FiveGServiceClient.this.getRsrpLevel(signalStrength.getRsrp());
                    currentServiceState.mRsrp = signalStrength.getRsrp();
                    FiveGServiceClient.this.update5GIcon(currentServiceState, i2);
                    FiveGServiceClient.this.notifyListenersIfNecessary(i2);
                }
            }

            public void onAnyNrBearerAllocation(int i2, Token token, Status status, BearerAllocationStatus bearerAllocationStatus) throws RemoteException {
                if (FiveGServiceClient.DEBUG) {
                    Log.d("FiveGServiceClient", "onAnyNrBearerAllocation bearerStatus=" + bearerAllocationStatus.get());
                }
                if (status.get() == 1) {
                    FiveGServiceState currentServiceState = FiveGServiceClient.this.getCurrentServiceState(i2);
                    currentServiceState.mBearerAllocationStatus = bearerAllocationStatus.get();
                    FiveGServiceClient.this.update5GIcon(currentServiceState, i2);
                    FiveGServiceClient.this.notifyListenersIfNecessary(i2);
                }
            }

            public void on5gConfigInfo(int i2, Token token, Status status, NrConfigType nrConfigType) throws RemoteException {
                Log.d("FiveGServiceClient", "on5gConfigInfo: slotId = " + i2 + " token = " + token + " status" + status + " NrConfigType = " + nrConfigType);
                if (status.get() == 1) {
                    FiveGServiceState currentServiceState = FiveGServiceClient.this.getCurrentServiceState(i2);
                    currentServiceState.mNrConfigType = nrConfigType.get();
                    FiveGServiceClient.this.update5GIcon(currentServiceState, i2);
                    FiveGServiceClient.this.notifyListenersIfNecessary(i2);
                }
            }

            public void onNrIconType(int i2, Token token, Status status, NrIconType nrIconType) throws RemoteException {
                Log.d("FiveGServiceClient", "onNrIconType: slotId = " + i2 + " token = " + token + " status" + status + " NrIconType = " + nrIconType);
                boolean z = true;
                if (status.get() != 1) {
                    return;
                }
                if (!FiveGServiceClient.this.mLast5GEnabledStatus.get(i2, true) && FiveGServiceClient.this.is5GIcon(nrIconType.get())) {
                    Log.d("FiveGServiceClient", "5g disabled, not show 5g nr icon");
                    FiveGServiceClient.this.onNrIconNone(i2);
                } else if (!"0".equals(FiveGServiceClient.this.isChinaTelecom) || !"0".equals(FiveGServiceClient.this.isChinaMobile)) {
                    FiveGServiceClient.this.onNrIconTypeInternal(i2, nrIconType.get());
                } else {
                    int i3 = FiveGServiceClient.this.mLastNrIconType.get(i2, -1);
                    int defaultDataPhoneId = FiveGServiceClient.this.mSubscriptionManager.getDefaultDataPhoneId();
                    boolean z2 = FiveGServiceClient.this.mScreenOnQueryToken.get(i2, -100) == token.get();
                    if (z2) {
                        FiveGServiceClient.this.mScreenOnQueryToken.delete(i2);
                    }
                    boolean z3 = FiveGServiceClient.this.is5GIcon(i3) && nrIconType.get() == 0;
                    boolean z4 = z3 && defaultDataPhoneId == i2 && !z2;
                    if (i3 != 0 || !FiveGServiceClient.this.is5GIcon(nrIconType.get())) {
                        z = false;
                    }
                    Log.d("FiveGServiceClient", "leave5G: " + z3 + " defaultPhoneId: " + defaultDataPhoneId + " isScreenOnQuery: " + z2 + " lastNrIconType: " + i3);
                    if (z4) {
                        FiveGServiceClient.this.mCountDownTimer.startDelay(i2);
                        FiveGServiceClient.this.mLastNrIconType.put(i2, nrIconType.get());
                    } else if (!FiveGServiceClient.this.mCountDownTimer.mStarted) {
                        FiveGServiceClient.this.onNrIconTypeInternal(i2, nrIconType.get());
                    } else if (z) {
                        FiveGServiceClient.this.mCountDownTimer.cancelDelay(i2, defaultDataPhoneId);
                        FiveGServiceClient.this.onNrIconTypeInternal(i2, nrIconType.get());
                    } else {
                        Log.d("FiveGServiceClient", "delay in progress and icon not change");
                    }
                }
            }

            public void on5gStatus(int i2, Token token, Status status, boolean z) throws RemoteException {
                Log.d("FiveGServiceClient", "on5gStatus: slotId = " + i2 + " token = " + token + " status" + status + " enableStatus = " + z);
                if (status.get() == 1) {
                    boolean z2 = FiveGServiceClient.this.mLast5GEnabledStatus.get(i2, true);
                    Log.d("FiveGServiceClient", "last 5G status:" + z2);
                    FiveGServiceClient.this.mLast5GEnabledStatus.put(i2, z);
                    if (!z && z2) {
                        FiveGServiceClient.this.onNrIconNone(i2);
                    }
                }
            }
        };
        this.mContext = context;
        this.mPackageName = context.getPackageName();
        this.mSubscriptionManager = SubscriptionManager.from(this.mContext);
        this.mRsrpThresholds = this.mContext.getResources().getIntArray(C0001R$array.op_config_5g_signal_rsrp_thresholds);
        this.mContext.getResources().getIntArray(C0001R$array.config_5g_signal_snr_thresholds);
        this.mFivebarRsrpThresholds = this.mContext.getResources().getIntArray(C0001R$array.config_5_bar_5g_signal_rsrp_thresholds);
    }

    public static FiveGServiceClient getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new FiveGServiceClient(context);
        }
        return sInstance;
    }

    public void registerCallback(KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback) {
        this.mKeyguardUpdateMonitorCallbacks.add(new WeakReference<>(keyguardUpdateMonitorCallback));
    }

    public void registerListener(int i, IFiveGStateListener iFiveGStateListener) {
        Log.d("FiveGServiceClient", "registerListener phoneId=" + i);
        this.mStatesListeners.put(i, iFiveGStateListener);
        if (!isServiceConnected()) {
            binderService();
        } else {
            initFiveGServiceState(i);
        }
        this.mRegisterListenerStatus.put(i, true);
    }

    public void unregisterListener(int i) {
        Log.d("FiveGServiceClient", "unregisterListener phoneId=" + i);
        this.mStatesListeners.remove(i);
        this.mCurrentServiceStates.remove(i);
        this.mLastServiceStates.remove(i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void binderService() {
        boolean bindService = ServiceUtil.bindService(this.mContext, this.mServiceConnection);
        Log.d("FiveGServiceClient", " bind service " + bindService);
        if (!bindService && this.mBindRetryTimes < 4 && !this.mHandler.hasMessages(1024)) {
            this.mHandler.sendEmptyMessageDelayed(1024, (long) ((this.mBindRetryTimes * 2000) + 3000));
            this.mBindRetryTimes++;
        }
    }

    public static int getNumLevels(Context context) {
        return context.getResources().getInteger(C0009R$integer.config_5g_num_signal_strength_bins);
    }

    public boolean isServiceConnected() {
        return this.mServiceConnected;
    }

    @VisibleForTesting
    public FiveGServiceState getCurrentServiceState(int i) {
        return getServiceState(i, this.mCurrentServiceStates);
    }

    private FiveGServiceState getLastServiceState(int i) {
        return getServiceState(i, this.mLastServiceStates);
    }

    private static FiveGServiceState getServiceState(int i, SparseArray<FiveGServiceState> sparseArray) {
        FiveGServiceState fiveGServiceState = sparseArray.get(i);
        if (fiveGServiceState != null) {
            return fiveGServiceState;
        }
        FiveGServiceState fiveGServiceState2 = new FiveGServiceState();
        sparseArray.put(i, fiveGServiceState2);
        return fiveGServiceState2;
    }

    public int getRsrpLevel(int i) {
        if (OpUtils.isSupportFiveBar()) {
            return getLevel(i, this.mFivebarRsrpThresholds, false);
        }
        return getLevel(i, this.mRsrpThresholds, false);
    }

    private int getLevel(int i, int[] iArr, boolean z) {
        int i2 = 1;
        int i3 = 0;
        if (iArr[iArr.length - 1] < i || i < iArr[0]) {
            i2 = 0;
        } else {
            while (true) {
                if (i3 >= iArr.length - 1) {
                    break;
                }
                if (iArr[i3] < i) {
                    int i4 = i3 + 1;
                    if (i <= iArr[i4]) {
                        i2 = i4;
                        break;
                    }
                }
                i3++;
            }
        }
        if (z) {
            i2++;
        }
        if (DEBUG) {
            Log.d("FiveGServiceClient", "value=" + i + " level=" + i2);
        }
        return i2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyListenersIfNecessary(int i) {
        FiveGServiceState currentServiceState = getCurrentServiceState(i);
        FiveGServiceState lastServiceState = getLastServiceState(i);
        if (!currentServiceState.equals(lastServiceState) || this.mRegisterListenerStatus.get(i, false)) {
            if (this.mRegisterListenerStatus.get(i, false)) {
                this.mRegisterListenerStatus.put(i, false);
                Log.d("FiveGServiceClient", "when reg listen is true, need to notify");
            }
            if (DEBUG) {
                Log.d("FiveGServiceClient", "phoneId(" + i + ") Change in state from " + lastServiceState + " \n\tto " + currentServiceState);
            }
            lastServiceState.copyFrom(currentServiceState);
            IFiveGStateListener iFiveGStateListener = this.mStatesListeners.get(i);
            if (iFiveGStateListener != null) {
                iFiveGStateListener.onStateChanged(currentServiceState);
                if (currentServiceState.mRsrp != -32768 && currentServiceState.mRsrp > -130 && currentServiceState.mRsrp < -40) {
                    if (this.mHandler.hasMessages(1027)) {
                        this.mHandler.removeMessages(1027);
                    }
                    this.mHandler.sendEmptyMessageDelayed(1027, 120000);
                }
            }
            this.mHandler.sendEmptyMessage(1026);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initFiveGServiceState() {
        Log.d("FiveGServiceClient", "initFiveGServiceState size=" + this.mStatesListeners.size());
        for (int i = 0; i < this.mStatesListeners.size(); i++) {
            initFiveGServiceState(this.mStatesListeners.keyAt(i));
        }
    }

    private void initFiveGServiceState(int i) {
        Log.d("FiveGServiceClient", "mNetworkService=" + this.mNetworkService + " mClient=" + this.mClient);
        if (this.mNetworkService != null && this.mClient != null) {
            Log.d("FiveGServiceClient", "query 5G service state for phoneId " + i);
            try {
                queryNrSignalStrength(i);
                Log.d("FiveGServiceClient", "query5gConfigInfo result:" + this.mNetworkService.query5gConfigInfo(i, this.mClient));
                Log.d("FiveGServiceClient", "queryNrIconType result:" + this.mNetworkService.queryNrIconType(i, this.mClient));
                Log.d("FiveGServiceClient", "queryNrBearerAllocation result:" + this.mNetworkService.queryNrBearerAllocation(i, this.mClient));
                Log.d("FiveGServiceClient", "query5gStatus result:" + this.mNetworkService.query5gStatus(i, this.mClient));
            } catch (DeadObjectException e) {
                Log.e("FiveGServiceClient", "initFiveGServiceState: Exception = " + e);
                Log.d("FiveGServiceClient", "try to re-binder service");
                this.mInitRetryTimes = 0;
                this.mServiceConnected = false;
                this.mNetworkService = null;
                this.mClient = null;
                binderService();
            } catch (Exception e2) {
                Log.d("FiveGServiceClient", "initFiveGServiceState: Exception = " + e2);
                if (this.mInitRetryTimes < 4 && !this.mHandler.hasMessages(1025)) {
                    this.mHandler.sendEmptyMessageDelayed(1025, (long) ((this.mInitRetryTimes * 2000) + 3000));
                    this.mInitRetryTimes++;
                }
            }
        }
    }

    public void queryNrSignalStrength(int i) {
        if (this.mNetworkService == null || this.mClient == null) {
            Log.e("FiveGServiceClient", "query queryNrSignalStrength for phoneId " + i);
            return;
        }
        Log.d("FiveGServiceClient", "query queryNrSignalStrength for phoneId " + i);
        try {
            Token queryNrSignalStrength = this.mNetworkService.queryNrSignalStrength(i, this.mClient);
            Log.d("FiveGServiceClient", "queryNrSignalStrength result:" + queryNrSignalStrength);
        } catch (Exception e) {
            Log.e("FiveGServiceClient", "queryNrSignalStrength", e);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void update5GIcon(FiveGServiceState fiveGServiceState, int i) {
        if (ProductUtils.isUsvMode() && OpUtils.isVzwSIM(i)) {
            fiveGServiceState.mNrIconType = this.mLastNrIconType.get(i, -1);
        } else if (ProductUtils.isUsVisMode()) {
            fiveGServiceState.mNrIconType = 0;
        } else if (!showFake5GIcon(i) || !this.mLast5GEnabledStatus.get(i, true)) {
            fiveGServiceState.mNrIconType = this.mLastNrIconType.get(i, -1);
        } else {
            Log.d("FiveGServiceClient", "update5GIcon: set iconType to BASIC");
            fiveGServiceState.mNrIconType = 1;
        }
        int i2 = fiveGServiceState.mNrIconType;
        fiveGServiceState.mIconGroup = getNrIconGroup(i2, i);
        Log.d("FiveGServiceClient", "update5GIcon phoneId = " + i + "iconType = " + i2 + ", iconGroup = " + fiveGServiceState.mIconGroup);
    }

    private boolean showFake5GIcon(int i) {
        FiveGServiceState currentServiceState = getCurrentServiceState(i);
        if (currentServiceState == null || this.mRsrpThresholds == null) {
            return false;
        }
        if (Build.DEBUG_ONEPLUS) {
            Log.d("FiveGServiceClient", "mBearerAllocationStatus :" + currentServiceState.mBearerAllocationStatus);
        }
        if (currentServiceState.mBearerAllocationStatus > 0) {
            return true;
        }
        return false;
    }

    private MobileSignalController.MobileIconGroup getNrIconGroup(int i, int i2) {
        MobileSignalController.MobileIconGroup mobileIconGroup = TelephonyIcons.UNKNOWN;
        if (i == 1) {
            return TelephonyIcons.FIVE_G_BASIC;
        }
        if (i == 2) {
            return TelephonyIcons.FIVE_G_UWB;
        }
        Log.d("FiveGServiceClient", "getNrIconGroup: Unknown nrIconType = " + i);
        return mobileIconGroup;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyMonitorCallback() {
        for (int i = 0; i < this.mKeyguardUpdateMonitorCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mKeyguardUpdateMonitorCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onRefreshCarrierInfo();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onNrIconNone(int i) {
        this.mCountDownTimer.cancelDelay(i, -1);
        onNrIconTypeInternal(i, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onNrIconTypeInternal(int i, int i2) {
        FiveGServiceState currentServiceState = getCurrentServiceState(i);
        this.mLastNrIconType.put(i, i2);
        currentServiceState.mNrIconType = i2;
        update5GIcon(currentServiceState, i);
        notifyListenersIfNecessary(i);
    }

    public void queryNrIconType() {
        for (int i = 0; i < this.mStatesListeners.size(); i++) {
            int keyAt = this.mStatesListeners.keyAt(i);
            try {
                if (this.mNetworkService != null) {
                    this.mScreenOnQueryToken.put(keyAt, this.mNetworkService.queryNrIconType(keyAt, this.mClient).get());
                    Log.d("FiveGServiceClient", "queryNrIconType: result:" + this.mScreenOnQueryToken + ", phoneId = " + keyAt);
                }
            } catch (Exception e) {
                Log.e("FiveGServiceClient", "queryNrIconType: exception = " + e);
            }
        }
    }
}
