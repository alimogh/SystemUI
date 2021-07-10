package com.android.systemui.statusbar.policy;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkScoreManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.settingslib.net.DataUsageController;
import com.android.systemui.C0003R$bool;
import com.android.systemui.C0015R$string;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.WifiSignalController;
import com.oneplus.networkspeed.NetworkSpeedController;
import com.oneplus.signal.OpSignalIcons;
import com.oneplus.util.OpUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import org.codeaurora.internal.IExtTelephony;
public class NetworkControllerImpl extends BroadcastReceiver implements NetworkController, DemoMode, DataUsageController.NetworkNameProvider, Dumpable {
    static final boolean CHATTY;
    static final boolean DEBUG;
    public static int SOFTSIM_DISABLE = 0;
    public static int SOFTSIM_ENABLE = 1;
    public static int SOFTSIM_ENABLE_PILOT = 2;
    private static final Uri SOFTSIM_URL = Uri.parse("content://com.redteamobile.provider");
    private final AccessPointControllerImpl mAccessPoints;
    private int mActiveMobileDataSubscription;
    private boolean mAirplaneMode;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final CallbackHandler mCallbackHandler;
    private final Runnable mClearForceValidated;
    private Config mConfig;
    private final BitSet mConnectedTransports;
    private final ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private List<SubscriptionInfo> mCurrentSubscriptions;
    private int mCurrentUserId;
    private final DataSaverController mDataSaverController;
    private final DataUsageController mDataUsageController;
    private MobileSignalController mDefaultSignalController;
    private boolean mDemoInetCondition;
    private boolean mDemoMode;
    private WifiSignalController.WifiState mDemoWifiState;
    private int mEmergencySource;
    @VisibleForTesting
    final EthernetSignalController mEthernetSignalController;
    private IExtTelephony mExtTelephony;
    private boolean mFackPSIcon;
    @VisibleForTesting
    FiveGServiceClient mFiveGServiceClient;
    private boolean mForceCellularValidated;
    private final boolean mHasMobileDataFeature;
    private boolean mHasNoSubs;
    private final HotspotController mHotspotController;
    private boolean mInetCondition;
    private boolean mIsEmergency;
    private NetworkController.IconState[] mLTEIconStates;
    private boolean[] mLTEstatus;
    @VisibleForTesting
    ServiceState mLastServiceState;
    @VisibleForTesting
    boolean mListening;
    private Locale mLocale;
    private final Object mLock;
    @VisibleForTesting
    final SparseArray<MobileSignalController> mMobileSignalControllers;
    private NetworkSpeedController mNetworkSpeedController;
    private boolean mOpActionTetherDialogShowing;
    private final TelephonyManager mPhone;
    private PhoneStateListener mPhoneStateListener;
    private int[] mProvisionState;
    private final Handler mReceiverHandler;
    private final Runnable mRegisterListeners;
    private final SettingObserver mSettingObserver;
    private boolean mSimDetected;
    private int[] mSimState;
    private boolean mSmartlinkEnable;
    private final SubscriptionDefaults mSubDefaults;
    private SubscriptionManager.OnSubscriptionsChangedListener mSubscriptionListener;
    private final SubscriptionManager mSubscriptionManager;
    private boolean mUserSetup;
    private final CurrentUserTracker mUserTracker;
    private final BitSet mValidatedTransports;
    private final WifiManager mWifiManager;
    @VisibleForTesting
    final WifiSignalController mWifiSignalController;
    private int[] softSimState;

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.statusbar.policy.NetworkControllerImpl$1  reason: invalid class name */
    public class AnonymousClass1 implements ConfigurationController.ConfigurationListener {
    }

    static {
        boolean z = Build.DEBUG_ONEPLUS;
        DEBUG = z;
        CHATTY = z;
    }

    public NetworkControllerImpl(Context context, Looper looper, DeviceProvisionedController deviceProvisionedController, BroadcastDispatcher broadcastDispatcher, ConnectivityManager connectivityManager, TelephonyManager telephonyManager, WifiManager wifiManager, NetworkScoreManager networkScoreManager) {
        this(context, connectivityManager, telephonyManager, wifiManager, networkScoreManager, SubscriptionManager.from(context), Config.readConfig(context), looper, new CallbackHandler(), new AccessPointControllerImpl(context), new DataUsageController(context), new SubscriptionDefaults(), deviceProvisionedController, broadcastDispatcher);
        this.mReceiverHandler.post(this.mRegisterListeners);
    }

    @VisibleForTesting
    NetworkControllerImpl(Context context, ConnectivityManager connectivityManager, TelephonyManager telephonyManager, WifiManager wifiManager, NetworkScoreManager networkScoreManager, SubscriptionManager subscriptionManager, Config config, Looper looper, CallbackHandler callbackHandler, AccessPointControllerImpl accessPointControllerImpl, DataUsageController dataUsageController, SubscriptionDefaults subscriptionDefaults, final DeviceProvisionedController deviceProvisionedController, BroadcastDispatcher broadcastDispatcher) {
        this.mLock = new Object();
        this.mActiveMobileDataSubscription = -1;
        this.mMobileSignalControllers = new SparseArray<>();
        this.mConnectedTransports = new BitSet();
        this.mValidatedTransports = new BitSet();
        this.mAirplaneMode = false;
        this.mLocale = null;
        this.mCurrentSubscriptions = new ArrayList();
        this.mOpActionTetherDialogShowing = false;
        this.mSimState = null;
        this.mClearForceValidated = new Runnable() { // from class: com.android.systemui.statusbar.policy.-$$Lambda$NetworkControllerImpl$oNWIIIg3gBRqx9jT8qywGtEkW2E
            @Override // java.lang.Runnable
            public final void run() {
                NetworkControllerImpl.this.lambda$new$0$NetworkControllerImpl();
            }
        };
        this.mRegisterListeners = new Runnable() { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.9
            @Override // java.lang.Runnable
            public void run() {
                NetworkControllerImpl.this.registerListeners();
            }
        };
        boolean[] zArr = {false, false, false, false, false, false};
        this.mLTEstatus = zArr;
        this.mLTEIconStates = new NetworkController.IconState[zArr.length];
        this.mProvisionState = new int[2];
        this.softSimState = new int[2];
        this.mSmartlinkEnable = false;
        this.mFackPSIcon = false;
        this.mSettingObserver = new SettingObserver();
        this.mContext = context;
        this.mConfig = config;
        this.mReceiverHandler = new Handler(looper);
        this.mCallbackHandler = callbackHandler;
        this.mDataSaverController = new DataSaverControllerImpl(context);
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mSubscriptionManager = subscriptionManager;
        this.mSubDefaults = subscriptionDefaults;
        this.mConnectivityManager = connectivityManager;
        this.mHasMobileDataFeature = connectivityManager.isNetworkSupported(0);
        this.mPhone = telephonyManager;
        this.mWifiManager = wifiManager;
        this.mLocale = this.mContext.getResources().getConfiguration().locale;
        this.mAccessPoints = accessPointControllerImpl;
        this.mDataUsageController = dataUsageController;
        dataUsageController.setNetworkController(this);
        this.mDataUsageController.setCallback(new DataUsageController.Callback() { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.2
            @Override // com.android.settingslib.net.DataUsageController.Callback
            public void onMobileDataEnabled(boolean z) {
                NetworkControllerImpl.this.mCallbackHandler.setMobileDataEnabled(z);
                NetworkControllerImpl.this.notifyControllersMobileDataChanged();
            }
        });
        this.mWifiSignalController = new WifiSignalController(this.mContext, this.mHasMobileDataFeature, this.mCallbackHandler, this, this.mWifiManager, this.mConnectivityManager, networkScoreManager);
        this.mEthernetSignalController = new EthernetSignalController(this.mContext, this.mCallbackHandler, this);
        updateAirplaneMode(true);
        AnonymousClass3 r1 = new CurrentUserTracker(broadcastDispatcher) { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.3
            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                NetworkControllerImpl.this.onUserSwitched(i);
            }
        };
        this.mUserTracker = r1;
        r1.startTracking();
        deviceProvisionedController.addCallback(new DeviceProvisionedController.DeviceProvisionedListener() { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.4
            @Override // com.android.systemui.statusbar.policy.DeviceProvisionedController.DeviceProvisionedListener
            public void onUserSetupChanged() {
                NetworkControllerImpl networkControllerImpl = NetworkControllerImpl.this;
                DeviceProvisionedController deviceProvisionedController2 = deviceProvisionedController;
                networkControllerImpl.setUserSetupComplete(deviceProvisionedController2.isUserSetup(deviceProvisionedController2.getCurrentUser()));
            }
        });
        this.mFiveGServiceClient = FiveGServiceClient.getInstance(context);
        this.mConnectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.5
            private Network mLastNetwork;
            private NetworkCapabilities mLastNetworkCapabilities;

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                NetworkCapabilities networkCapabilities2 = this.mLastNetworkCapabilities;
                boolean z = networkCapabilities2 != null && networkCapabilities2.hasCapability(16);
                boolean hasCapability = networkCapabilities.hasCapability(16);
                if (!network.equals(this.mLastNetwork) || !networkCapabilities.equalsTransportTypes(this.mLastNetworkCapabilities) || hasCapability != z) {
                    this.mLastNetwork = network;
                    this.mLastNetworkCapabilities = networkCapabilities;
                    NetworkControllerImpl.this.updateConnectivity();
                }
            }
        }, this.mReceiverHandler);
        Handler handler = this.mReceiverHandler;
        Objects.requireNonNull(handler);
        this.mPhoneStateListener = new PhoneStateListener(new Executor(handler) { // from class: com.android.systemui.statusbar.policy.-$$Lambda$LfzJt661qZfn2w-6SYHFbD3aMy0
            public final /* synthetic */ Handler f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.concurrent.Executor
            public final void execute(Runnable runnable) {
                this.f$0.post(runnable);
            }
        }) { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.6
            @Override // android.telephony.PhoneStateListener
            public void onActiveDataSubscriptionIdChanged(int i) {
                NetworkControllerImpl networkControllerImpl = NetworkControllerImpl.this;
                if (networkControllerImpl.keepCellularValidationBitInSwitch(networkControllerImpl.mActiveMobileDataSubscription, i)) {
                    if (NetworkControllerImpl.DEBUG) {
                        Log.d("NetworkController", ": mForceCellularValidated to true.");
                    }
                    NetworkControllerImpl.this.mForceCellularValidated = true;
                    NetworkControllerImpl.this.mReceiverHandler.removeCallbacks(NetworkControllerImpl.this.mClearForceValidated);
                    NetworkControllerImpl.this.mReceiverHandler.postDelayed(NetworkControllerImpl.this.mClearForceValidated, 2000);
                }
                NetworkControllerImpl.this.mActiveMobileDataSubscription = i;
                NetworkControllerImpl.this.doUpdateMobileControllers();
            }
        };
        initProvistionState();
        this.mHotspotController = (HotspotController) Dependency.get(HotspotController.class);
        onSmartLinkChange(true);
        onFackPSIcon(true);
        int phoneCount = this.mPhone.getPhoneCount();
        this.mSimState = new int[phoneCount];
        Log.d("NetworkController", "NetworkControllerImpl init phoneCnt = " + phoneCount);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$NetworkControllerImpl() {
        if (DEBUG) {
            Log.d("NetworkController", ": mClearForceValidated");
        }
        this.mForceCellularValidated = false;
        updateConnectivity();
    }

    /* access modifiers changed from: package-private */
    public boolean isInGroupDataSwitch(int i, int i2) {
        SubscriptionInfo activeSubscriptionInfo = this.mSubscriptionManager.getActiveSubscriptionInfo(i);
        SubscriptionInfo activeSubscriptionInfo2 = this.mSubscriptionManager.getActiveSubscriptionInfo(i2);
        return (activeSubscriptionInfo == null || activeSubscriptionInfo2 == null || activeSubscriptionInfo.getGroupUuid() == null || !activeSubscriptionInfo.getGroupUuid().equals(activeSubscriptionInfo2.getGroupUuid())) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public boolean keepCellularValidationBitInSwitch(int i, int i2) {
        if (!this.mValidatedTransports.get(0) || !isInGroupDataSwitch(i, i2)) {
            return false;
        }
        return true;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public DataSaverController getDataSaverController() {
        return this.mDataSaverController;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void registerListeners() {
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            MobileSignalController valueAt = this.mMobileSignalControllers.valueAt(i);
            valueAt.registerListener();
            valueAt.registerFiveGStateListener(this.mFiveGServiceClient);
        }
        if (this.mSubscriptionListener == null) {
            this.mSubscriptionListener = new SubListener(this, null);
        }
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mSubscriptionListener);
        this.mPhone.listen(this.mPhoneStateListener, 4194304);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        intentFilter.addAction("android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED");
        intentFilter.addAction("android.intent.action.SERVICE_STATE");
        intentFilter.addAction("android.telephony.action.SERVICE_PROVIDERS_UPDATED");
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.conn.INET_CONDITION_ACTION");
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        intentFilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        intentFilter.addAction("android.intent.action.ANY_DATA_STATE");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.setupDataError_tether");
        if (!OpUtils.isReallyHasOneSim()) {
            intentFilter.addAction("org.codeaurora.intent.action.ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED");
        }
        this.mBroadcastDispatcher.registerReceiverWithHandler(this, intentFilter, this.mReceiverHandler);
        this.mListening = true;
        this.mReceiverHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.policy.-$$Lambda$NetworkControllerImpl$kkWH_IrhHJnwynPIgTUQ4s52BA4
            @Override // java.lang.Runnable
            public final void run() {
                NetworkControllerImpl.this.updateConnectivity();
            }
        });
        Handler handler = this.mReceiverHandler;
        WifiSignalController wifiSignalController = this.mWifiSignalController;
        Objects.requireNonNull(wifiSignalController);
        handler.post(new Runnable() { // from class: com.android.systemui.statusbar.policy.-$$Lambda$k1PbTpG_zEeb84NzDtu0B5A435o
            @Override // java.lang.Runnable
            public final void run() {
                WifiSignalController.this.fetchInitialState();
            }
        });
        this.mReceiverHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.policy.-$$Lambda$NetworkControllerImpl$y01JBs_73T3u8BmqanYAWT8JvK0
            @Override // java.lang.Runnable
            public final void run() {
                NetworkControllerImpl.this.lambda$registerListeners$1$NetworkControllerImpl();
            }
        });
        this.mSettingObserver.observe();
        updateMobileControllers();
        this.mReceiverHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.policy.-$$Lambda$w0FuAr6MDKkUIV1rcwwlRClXFUs
            @Override // java.lang.Runnable
            public final void run() {
                NetworkControllerImpl.this.recalculateEmergency();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$registerListeners$1 */
    public /* synthetic */ void lambda$registerListeners$1$NetworkControllerImpl() {
        if (this.mLastServiceState == null) {
            this.mLastServiceState = this.mPhone.getServiceState();
            if (this.mMobileSignalControllers.size() == 0) {
                recalculateEmergency();
            }
        }
    }

    private void unregisterListeners() {
        this.mListening = false;
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            MobileSignalController valueAt = this.mMobileSignalControllers.valueAt(i);
            valueAt.unregisterListener();
            valueAt.unregisterFiveGStateListener(this.mFiveGServiceClient);
        }
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mSubscriptionListener);
        this.mBroadcastDispatcher.unregisterReceiver(this);
        this.mSettingObserver.unObserve();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public NetworkController.AccessPointController getAccessPointController() {
        return this.mAccessPoints;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public DataUsageController getMobileDataController() {
        return this.mDataUsageController;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public boolean hasMobileDataFeature() {
        return this.mHasMobileDataFeature;
    }

    public boolean hasVoiceCallingFeature() {
        return this.mPhone.getPhoneType() != 0;
    }

    private MobileSignalController getDataController() {
        int activeDataSubId = this.mSubDefaults.getActiveDataSubId();
        if (!SubscriptionManager.isValidSubscriptionId(activeDataSubId)) {
            if (DEBUG) {
                Log.e("NetworkController", "No data sim selected");
            }
            return this.mDefaultSignalController;
        } else if (this.mMobileSignalControllers.indexOfKey(activeDataSubId) >= 0) {
            return this.mMobileSignalControllers.get(activeDataSubId);
        } else {
            if (DEBUG) {
                Log.e("NetworkController", "Cannot find controller for data sub: " + activeDataSubId);
            }
            return this.mDefaultSignalController;
        }
    }

    @Override // com.android.settingslib.net.DataUsageController.NetworkNameProvider
    public String getMobileDataNetworkName() {
        MobileSignalController dataController = getDataController();
        return dataController != null ? dataController.getState().networkNameData : "";
    }

    /* access modifiers changed from: package-private */
    public boolean isDataControllerDisabled() {
        MobileSignalController dataController = getDataController();
        if (dataController == null) {
            return false;
        }
        return dataController.isDataDisabled();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyControllersMobileDataChanged() {
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).onMobileDataChanged();
        }
    }

    public boolean isEmergencyOnly() {
        if (this.mMobileSignalControllers.size() == 0) {
            this.mEmergencySource = 0;
            ServiceState serviceState = this.mLastServiceState;
            return serviceState != null && serviceState.isEmergencyOnly();
        }
        int defaultVoiceSubId = this.mSubDefaults.getDefaultVoiceSubId();
        if (!SubscriptionManager.isValidSubscriptionId(defaultVoiceSubId)) {
            for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
                MobileSignalController valueAt = this.mMobileSignalControllers.valueAt(i);
                if (!valueAt.getState().isEmergency) {
                    this.mEmergencySource = valueAt.mSubscriptionInfo.getSubscriptionId() + 100;
                    if (DEBUG) {
                        Log.d("NetworkController", "Found emergency " + valueAt.mTag);
                    }
                    return false;
                }
            }
        }
        if (this.mMobileSignalControllers.indexOfKey(defaultVoiceSubId) >= 0) {
            this.mEmergencySource = defaultVoiceSubId + 200;
            if (DEBUG) {
                Log.d("NetworkController", "Getting emergency from " + defaultVoiceSubId);
            }
            return this.mMobileSignalControllers.get(defaultVoiceSubId).getState().isEmergency;
        } else if (this.mMobileSignalControllers.size() == 1) {
            this.mEmergencySource = this.mMobileSignalControllers.keyAt(0) + 400;
            if (DEBUG) {
                Log.d("NetworkController", "Getting assumed emergency from " + this.mMobileSignalControllers.keyAt(0));
            }
            return this.mMobileSignalControllers.valueAt(0).getState().isEmergency;
        } else {
            if (DEBUG) {
                Log.e("NetworkController", "Cannot find controller for voice sub: " + defaultVoiceSubId);
            }
            this.mEmergencySource = defaultVoiceSubId + 300;
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void recalculateEmergency() {
        boolean isEmergencyOnly = isEmergencyOnly();
        this.mIsEmergency = isEmergencyOnly;
        this.mCallbackHandler.setEmergencyCallsOnly(isEmergencyOnly);
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public void addCallback(NetworkController.SignalCallback signalCallback) {
        signalCallback.setSubs(this.mCurrentSubscriptions);
        signalCallback.setIsAirplaneMode(new NetworkController.IconState(this.mAirplaneMode, TelephonyIcons.FLIGHT_MODE_ICON, C0015R$string.accessibility_airplane_mode, this.mContext));
        signalCallback.setNoSims(this.mHasNoSubs, this.mSimDetected);
        this.mWifiSignalController.notifyListeners(signalCallback);
        this.mEthernetSignalController.notifyListeners(signalCallback);
        int i = 0;
        for (int i2 = 0; i2 < this.mMobileSignalControllers.size(); i2++) {
            this.mMobileSignalControllers.valueAt(i2).notifyListeners(signalCallback);
        }
        this.mCallbackHandler.setListening(signalCallback, true);
        signalCallback.setLTEStatus(this.mLTEIconStates);
        while (true) {
            int[] iArr = this.mProvisionState;
            if (i < iArr.length) {
                signalCallback.setProvision(i, iArr[i]);
                i++;
            } else {
                signalCallback.setVirtualSimstate(this.softSimState);
                signalCallback.setHasAnySimReady(hasSimReady());
                return;
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public void removeCallback(NetworkController.SignalCallback signalCallback) {
        this.mCallbackHandler.setListening(signalCallback, false);
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public void setWifiEnabled(final boolean z) {
        new AsyncTask<Void, Void, Void>() { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.7
            /* access modifiers changed from: protected */
            public Void doInBackground(Void... voidArr) {
                NetworkControllerImpl.this.mWifiManager.setWifiEnabled(z);
                return null;
            }
        }.execute(new Void[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUserSwitched(int i) {
        this.mCurrentUserId = i;
        this.mAccessPoints.onUserSwitched(i);
        updateConnectivity();
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        char c;
        if (CHATTY) {
            Log.d("NetworkController", "onReceive: intent=" + intent);
        }
        String action = intent.getAction();
        switch (action.hashCode()) {
            case -2104353374:
                if (action.equals("android.intent.action.SERVICE_STATE")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -1465084191:
                if (action.equals("android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -1454123155:
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case -1172645946:
                if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -1138588223:
                if (action.equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case -1076576821:
                if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -229777127:
                if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -25388475:
                if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 623179603:
                if (action.equals("android.net.conn.INET_CONDITION_ACTION")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1267145707:
                if (action.equals("android.intent.action.setupDataError_tether")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 2052218635:
                if (action.equals("org.codeaurora.intent.action.ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
            case 1:
                updateConnectivity();
                return;
            case 2:
                refreshLocale();
                updateAirplaneMode(false);
                return;
            case 3:
                recalculateEmergency();
                return;
            case 4:
                break;
            case 5:
                if (!intent.getBooleanExtra("rebroadcastOnUnlock", false)) {
                    updateMobileControllers();
                    int intExtra = intent.getIntExtra("phone", -1);
                    int simState = this.mPhone.getSimState(intExtra);
                    if (DEBUG) {
                        Log.d("NetworkController", "ACTION_SIM_STATE_CHANGED: curSimState = " + simState);
                    }
                    String stringExtra = intent.getStringExtra("ss");
                    if ("ABSENT".equals(stringExtra) || "READY".equals(stringExtra)) {
                        initProvistionState();
                        if (this.mSimState[intExtra] != simState) {
                            OpUtils.setSimType(this.mContext, intExtra);
                        }
                    } else if ("LOADED".equals(stringExtra)) {
                        initProvistionState();
                        OpUtils.setSimType(this.mContext, intExtra);
                    }
                    this.mSimState[intExtra] = simState;
                    checkVirtualSimcard();
                    this.mCallbackHandler.setHasAnySimReady(hasSimReady());
                    return;
                }
                return;
            case 6:
                this.mLastServiceState = ServiceState.newFromBundle(intent.getExtras());
                if (this.mMobileSignalControllers.size() == 0) {
                    recalculateEmergency();
                    return;
                }
                return;
            case 7:
                this.mConfig = Config.readConfig(this.mContext);
                this.mReceiverHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.policy.-$$Lambda$ybM43k5QVX_SxWbQACu1XwL3Knk
                    @Override // java.lang.Runnable
                    public final void run() {
                        NetworkControllerImpl.this.handleConfigurationChanged();
                    }
                });
                return;
            case '\b':
                int intExtra2 = intent.getIntExtra("phone", -1);
                int intExtra3 = intent.getIntExtra("newProvisionState", 0);
                Log.v("NetworkController", "onProvisionChange provisionedState: " + intExtra3 + " slotId: " + intExtra2);
                onProvisionChange(intExtra2, intExtra3);
                return;
            case '\t':
                if (OpUtils.isUSS()) {
                    if (DEBUG) {
                        Log.i("NetworkController", "TetherError callback");
                    }
                    if (this.mHotspotController.isHotspotEnabled() && intent.getBooleanExtra("data_call_error", false)) {
                        Log.i("NetworkController", "setTetherError ");
                        this.mHotspotController.setHotspotEnabled(false);
                        opActionTetherErrorAlertDialog();
                        return;
                    }
                    return;
                }
                return;
            case '\n':
                if (DEBUG) {
                    Log.i("NetworkController", "Receive ACTION_SCREEN_ON.");
                }
                this.mFiveGServiceClient.queryNrIconType();
                return;
            default:
                int intExtra4 = intent.getIntExtra("android.telephony.extra.SUBSCRIPTION_INDEX", -1);
                if (!SubscriptionManager.isValidSubscriptionId(intExtra4)) {
                    this.mWifiSignalController.handleBroadcast(intent);
                    return;
                } else if (this.mMobileSignalControllers.indexOfKey(intExtra4) >= 0) {
                    this.mMobileSignalControllers.get(intExtra4).handleBroadcast(intent);
                    return;
                } else {
                    updateMobileControllers();
                    return;
                }
        }
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).handleBroadcast(intent);
        }
        this.mConfig = Config.readConfig(this.mContext);
        this.mReceiverHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.policy.-$$Lambda$ybM43k5QVX_SxWbQACu1XwL3Knk
            @Override // java.lang.Runnable
            public final void run() {
                NetworkControllerImpl.this.handleConfigurationChanged();
            }
        });
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void handleConfigurationChanged() {
        updateMobileControllers();
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).setConfiguration(this.mConfig);
        }
        refreshLocale();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateMobileControllers() {
        if (this.mListening) {
            doUpdateMobileControllers();
        }
    }

    private void filterMobileSubscriptionInSameGroup(List<SubscriptionInfo> list) {
        if (list.size() == 2) {
            SubscriptionInfo subscriptionInfo = list.get(0);
            SubscriptionInfo subscriptionInfo2 = list.get(1);
            if (subscriptionInfo.getGroupUuid() != null && subscriptionInfo.getGroupUuid().equals(subscriptionInfo2.getGroupUuid())) {
                if (!subscriptionInfo.isOpportunistic() && !subscriptionInfo2.isOpportunistic()) {
                    return;
                }
                if (CarrierConfigManager.getDefaultConfig().getBoolean("always_show_primary_signal_bar_in_opportunistic_network_boolean")) {
                    if (!subscriptionInfo.isOpportunistic()) {
                        subscriptionInfo = subscriptionInfo2;
                    }
                    list.remove(subscriptionInfo);
                    return;
                }
                if (subscriptionInfo.getSubscriptionId() == this.mActiveMobileDataSubscription) {
                    subscriptionInfo = subscriptionInfo2;
                }
                list.remove(subscriptionInfo);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void doUpdateMobileControllers() {
        List<SubscriptionInfo> completeActiveSubscriptionInfoList = this.mSubscriptionManager.getCompleteActiveSubscriptionInfoList();
        if (completeActiveSubscriptionInfoList == null) {
            completeActiveSubscriptionInfoList = Collections.emptyList();
        }
        filterMobileSubscriptionInSameGroup(completeActiveSubscriptionInfoList);
        if (hasCorrectMobileControllers(completeActiveSubscriptionInfoList)) {
            updateNoSims();
            return;
        }
        synchronized (this.mLock) {
            setCurrentSubscriptionsLocked(completeActiveSubscriptionInfoList);
        }
        updateNoSims();
        recalculateEmergency();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void updateNoSims() {
        boolean z = this.mHasMobileDataFeature && this.mMobileSignalControllers.size() == 0;
        boolean hasAnySim = hasAnySim();
        if (z != this.mHasNoSubs || hasAnySim != this.mSimDetected) {
            this.mHasNoSubs = z;
            this.mSimDetected = hasAnySim;
            this.mCallbackHandler.setNoSims(z, hasAnySim);
        }
    }

    private boolean hasAnySim() {
        int activeModemCount = this.mPhone.getActiveModemCount();
        for (int i = 0; i < activeModemCount; i++) {
            int simState = this.mPhone.getSimState(i);
            if (!(simState == 1 || simState == 0)) {
                return true;
            }
        }
        return false;
    }

    @GuardedBy({"mLock"})
    @VisibleForTesting
    public void setCurrentSubscriptionsLocked(List<SubscriptionInfo> list) {
        int i;
        Collections.sort(list, new Comparator<SubscriptionInfo>(this) { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.8
            public int compare(SubscriptionInfo subscriptionInfo, SubscriptionInfo subscriptionInfo2) {
                int i2;
                int i3;
                if (subscriptionInfo.getSimSlotIndex() == subscriptionInfo2.getSimSlotIndex()) {
                    i3 = subscriptionInfo.getSubscriptionId();
                    i2 = subscriptionInfo2.getSubscriptionId();
                } else {
                    i3 = subscriptionInfo.getSimSlotIndex();
                    i2 = subscriptionInfo2.getSimSlotIndex();
                }
                return i3 - i2;
            }
        });
        this.mCurrentSubscriptions = list;
        SparseArray sparseArray = new SparseArray();
        for (int i2 = 0; i2 < this.mMobileSignalControllers.size(); i2++) {
            sparseArray.put(this.mMobileSignalControllers.keyAt(i2), this.mMobileSignalControllers.valueAt(i2));
        }
        this.mMobileSignalControllers.clear();
        int size = list.size();
        int i3 = 0;
        while (i3 < size) {
            int subscriptionId = list.get(i3).getSubscriptionId();
            if (sparseArray.indexOfKey(subscriptionId) < 0 || ((MobileSignalController) sparseArray.get(subscriptionId)).getSimSlotIndex() != list.get(i3).getSimSlotIndex()) {
                i = size;
                MobileSignalController mobileSignalController = new MobileSignalController(this.mContext, this.mConfig, this.mHasMobileDataFeature, this.mPhone.createForSubscriptionId(subscriptionId), this.mCallbackHandler, this, list.get(i3), this.mSubDefaults, this.mReceiverHandler.getLooper());
                mobileSignalController.setUserSetupComplete(this.mUserSetup);
                Log.i("NetworkController", "create controller SubId:" + subscriptionId + " SlotIndex:" + mobileSignalController.getSimSlotIndex());
                this.mMobileSignalControllers.put(subscriptionId, mobileSignalController);
                if (list.get(i3).getSimSlotIndex() == 0) {
                    this.mDefaultSignalController = mobileSignalController;
                }
                if (this.mListening) {
                    mobileSignalController.registerListener();
                    mobileSignalController.registerFiveGStateListener(this.mFiveGServiceClient);
                }
            } else {
                this.mMobileSignalControllers.put(subscriptionId, (MobileSignalController) sparseArray.get(subscriptionId));
                sparseArray.remove(subscriptionId);
                i = size;
            }
            i3++;
            size = i;
        }
        if (this.mListening) {
            for (int i4 = 0; i4 < sparseArray.size(); i4++) {
                int keyAt = sparseArray.keyAt(i4);
                if (sparseArray.get(keyAt) == this.mDefaultSignalController) {
                    this.mDefaultSignalController = null;
                }
                ((MobileSignalController) sparseArray.get(keyAt)).unregisterListener();
                ((MobileSignalController) sparseArray.get(keyAt)).unregisterFiveGStateListener(this.mFiveGServiceClient);
            }
        }
        this.mCallbackHandler.setSubs(list);
        this.mCallbackHandler.setLTEStatus(this.mLTEIconStates);
        int i5 = 0;
        while (true) {
            int[] iArr = this.mProvisionState;
            if (i5 >= iArr.length) {
                break;
            }
            this.mCallbackHandler.setProvision(i5, iArr[i5]);
            i5++;
        }
        notifySetVirtualSimstate(this.softSimState);
        notifyAllListeners();
        pushConnectivityToSignals();
        updateAirplaneMode(true);
        if (OpUtils.isSupportShowDualChannel()) {
            onSmartLinkChange(true);
        }
        onFackPSIcon(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setUserSetupComplete(boolean z) {
        this.mReceiverHandler.post(new Runnable(z) { // from class: com.android.systemui.statusbar.policy.-$$Lambda$NetworkControllerImpl$Azegj2_SgwoDK-vkYhUnuXri31U
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                NetworkControllerImpl.this.lambda$setUserSetupComplete$2$NetworkControllerImpl(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: handleSetUserSetupComplete */
    public void lambda$setUserSetupComplete$2(boolean z) {
        this.mUserSetup = z;
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).setUserSetupComplete(this.mUserSetup);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean hasCorrectMobileControllers(List<SubscriptionInfo> list) {
        if (list.size() != this.mMobileSignalControllers.size()) {
            return false;
        }
        for (SubscriptionInfo subscriptionInfo : list) {
            if (this.mMobileSignalControllers.indexOfKey(subscriptionInfo.getSubscriptionId()) < 0) {
                return false;
            }
            MobileSignalController mobileSignalController = this.mMobileSignalControllers.get(subscriptionInfo.getSubscriptionId());
            if (!(mobileSignalController == null || mobileSignalController.getSimSlotIndex() == subscriptionInfo.getSimSlotIndex())) {
                Log.i("NetworkController", "hasCorrectMobileControllers SubId:" + subscriptionInfo.getSubscriptionId() + " change from:" + mobileSignalController.getSimSlotIndex() + " to:" + subscriptionInfo.getSimSlotIndex());
                return false;
            }
        }
        return true;
    }

    private void updateAirplaneMode(boolean z) {
        boolean z2 = true;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 1) {
            z2 = false;
        }
        if (DEBUG) {
            Log.d("NetworkController", "updateAirplaneMode: " + this.mAirplaneMode + " new: " + z2 + " force: " + z);
        }
        if (z2 != this.mAirplaneMode || z) {
            this.mAirplaneMode = z2;
            for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
                this.mMobileSignalControllers.valueAt(i).setAirplaneMode(this.mAirplaneMode);
            }
            notifyListeners();
        }
    }

    private void refreshLocale() {
        Locale locale = this.mContext.getResources().getConfiguration().locale;
        if (!locale.equals(this.mLocale)) {
            this.mLocale = locale;
            this.mWifiSignalController.refreshLocale();
            notifyAllListeners();
        }
    }

    private void notifyAllListeners() {
        notifyListeners();
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).notifyListeners();
        }
        this.mWifiSignalController.notifyListeners();
        this.mEthernetSignalController.notifyListeners();
    }

    private void notifyListeners() {
        this.mCallbackHandler.setIsAirplaneMode(new NetworkController.IconState(this.mAirplaneMode, TelephonyIcons.FLIGHT_MODE_ICON, C0015R$string.accessibility_airplane_mode, this.mContext));
        this.mCallbackHandler.setNoSims(this.mHasNoSubs, this.mSimDetected);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void updateConnectivity() {
        this.mConnectedTransports.clear();
        this.mValidatedTransports.clear();
        NetworkCapabilities[] defaultNetworkCapabilitiesForUser = this.mConnectivityManager.getDefaultNetworkCapabilitiesForUser(this.mCurrentUserId);
        for (NetworkCapabilities networkCapabilities : defaultNetworkCapabilitiesForUser) {
            int[] transportTypes = networkCapabilities.getTransportTypes();
            for (int i : transportTypes) {
                this.mConnectedTransports.set(i);
                if (networkCapabilities.hasCapability(16)) {
                    this.mValidatedTransports.set(i);
                }
            }
        }
        if (this.mForceCellularValidated) {
            this.mValidatedTransports.set(0);
        }
        if (CHATTY) {
            Log.d("NetworkController", "updateConnectivity: mConnectedTransports=" + this.mConnectedTransports);
            Log.d("NetworkController", "updateConnectivity: mValidatedTransports=" + this.mValidatedTransports);
        }
        this.mInetCondition = !this.mValidatedTransports.isEmpty();
        pushConnectivityToSignals();
    }

    private void pushConnectivityToSignals() {
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).updateConnectivity(this.mConnectedTransports, this.mValidatedTransports);
        }
        this.mWifiSignalController.updateConnectivity(this.mConnectedTransports, this.mValidatedTransports);
        this.mEthernetSignalController.updateConnectivity(this.mConnectedTransports, this.mValidatedTransports);
        NetworkSpeedController networkSpeedController = this.mNetworkSpeedController;
        if (networkSpeedController != null) {
            networkSpeedController.updateConnectivity(this.mConnectedTransports, this.mValidatedTransports);
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("NetworkController state:");
        printWriter.println("  - telephony ------");
        printWriter.print("  hasVoiceCallingFeature()=");
        printWriter.println(hasVoiceCallingFeature());
        printWriter.println("  mListening=" + this.mListening);
        printWriter.println("  - connectivity ------");
        printWriter.print("  mConnectedTransports=");
        printWriter.println(this.mConnectedTransports);
        printWriter.print("  mValidatedTransports=");
        printWriter.println(this.mValidatedTransports);
        printWriter.print("  mInetCondition=");
        printWriter.println(this.mInetCondition);
        printWriter.print("  mAirplaneMode=");
        printWriter.println(this.mAirplaneMode);
        printWriter.print("  mLocale=");
        printWriter.println(this.mLocale);
        printWriter.print("  mLastServiceState=");
        printWriter.println(this.mLastServiceState);
        printWriter.print("  mIsEmergency=");
        printWriter.println(this.mIsEmergency);
        printWriter.print("  mEmergencySource=");
        printWriter.println(emergencyToString(this.mEmergencySource));
        printWriter.println("  - config ------");
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).dump(printWriter);
        }
        this.mWifiSignalController.dump(printWriter);
        this.mEthernetSignalController.dump(printWriter);
        this.mAccessPoints.dump(printWriter);
    }

    private static final String emergencyToString(int i) {
        if (i > 300) {
            return "ASSUMED_VOICE_CONTROLLER(" + (i - 200) + ")";
        } else if (i > 300) {
            return "NO_SUB(" + (i - 300) + ")";
        } else if (i > 200) {
            return "VOICE_CONTROLLER(" + (i - 200) + ")";
        } else if (i <= 100) {
            return i == 0 ? "NO_CONTROLLERS" : "UNKNOWN_SOURCE";
        } else {
            return "FIRST_CONTROLLER(" + (i - 100) + ")";
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:237:0x0485, code lost:
        if (r6.equals("lte+") != false) goto L_0x0463;
     */
    /* JADX WARNING: Removed duplicated region for block: B:267:0x050d  */
    /* JADX WARNING: Removed duplicated region for block: B:274:0x0522  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0161  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x0179  */
    @Override // com.android.systemui.DemoMode
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dispatchDemoCommand(java.lang.String r30, android.os.Bundle r31) {
        /*
        // Method dump skipped, instructions count: 1383
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.NetworkControllerImpl.dispatchDemoCommand(java.lang.String, android.os.Bundle):void");
    }

    private SubscriptionInfo addSignalController(int i, int i2) {
        SubscriptionInfo subscriptionInfo = new SubscriptionInfo(i, "", i2, "", "", 0, 0, "", 0, null, null, null, "", false, null, null);
        MobileSignalController mobileSignalController = new MobileSignalController(this.mContext, this.mConfig, this.mHasMobileDataFeature, this.mPhone.createForSubscriptionId(subscriptionInfo.getSubscriptionId()), this.mCallbackHandler, this, subscriptionInfo, this.mSubDefaults, this.mReceiverHandler.getLooper());
        this.mMobileSignalControllers.put(i, mobileSignalController);
        mobileSignalController.getState().userSetup = true;
        return subscriptionInfo;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public boolean hasEmergencyCryptKeeperText() {
        return EncryptionHelper.IS_DATA_ENCRYPTED;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public boolean isRadioOn() {
        return !this.mAirplaneMode;
    }

    /* access modifiers changed from: private */
    public class SubListener extends SubscriptionManager.OnSubscriptionsChangedListener {
        private SubListener() {
        }

        /* synthetic */ SubListener(NetworkControllerImpl networkControllerImpl, AnonymousClass1 r2) {
            this();
        }

        @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
        public void onSubscriptionsChanged() {
            NetworkControllerImpl.this.updateMobileControllers();
        }
    }

    public static class SubscriptionDefaults {
        public int getDefaultVoiceSubId() {
            return SubscriptionManager.getDefaultVoiceSubscriptionId();
        }

        public int getDefaultDataSubId() {
            return SubscriptionManager.getDefaultDataSubscriptionId();
        }

        public int getActiveDataSubId() {
            return SubscriptionManager.getActiveDataSubscriptionId();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class Config {
        boolean alwaysShowCdmaRssi = false;
        boolean alwaysShowDataRatIcon = false;
        boolean alwaysShowNetworkTypeIcon = false;
        boolean enableRatIconEnhancement = false;
        boolean readIconsFromXml = false;
        boolean showAtLeast3G = false;
        boolean showRsrpSignalLevelforLTE = false;
        boolean showVolteIcon = false;
        boolean showVowifiIcon = false;

        Config() {
        }

        static Config readConfig(Context context) {
            Config config = new Config();
            Resources resources = context.getResources();
            config.showAtLeast3G = resources.getBoolean(C0003R$bool.config_showMin3G);
            config.alwaysShowCdmaRssi = resources.getBoolean(17891360);
            resources.getBoolean(C0003R$bool.config_hspa_data_distinguishable);
            config.alwaysShowNetworkTypeIcon = context.getResources().getBoolean(C0003R$bool.config_alwaysShowTypeIcon);
            config.showRsrpSignalLevelforLTE = resources.getBoolean(C0003R$bool.config_showRsrpSignalLevelforLTE);
            resources.getBoolean(C0003R$bool.config_hideNoInternetState);
            config.showVolteIcon = resources.getBoolean(C0003R$bool.config_display_volte);
            SubscriptionManager.from(context);
            PersistableBundle configForSubId = ((CarrierConfigManager) context.getSystemService("carrier_config")).getConfigForSubId(SubscriptionManager.getDefaultDataSubscriptionId());
            if (configForSubId != null) {
                config.alwaysShowDataRatIcon = configForSubId.getBoolean("always_show_data_rat_icon_bool");
                configForSubId.getBoolean("show_4g_for_lte_data_icon_bool");
                configForSubId.getBoolean("show_4g_for_3g_data_icon_bool");
                configForSubId.getBoolean("hide_lte_plus_data_icon_bool");
            }
            if (!OpUtils.isGlobalROM(context)) {
                config.alwaysShowNetworkTypeIcon = true;
            }
            config.enableRatIconEnhancement = SystemProperties.getBoolean("persist.sysui.rat_icon_enhancement", false);
            config.showVowifiIcon = resources.getBoolean(C0003R$bool.config_display_vowifi);
            return config;
        }
    }

    public void onLTEStatusUpdate() {
        if (this.mContext != null) {
            boolean[][] zArr = (boolean[][]) Array.newInstance(boolean.class, 2, 6);
            int[] iArr = {0, 0, 0, 0, 0, 0};
            for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
                MobileSignalController valueAt = this.mMobileSignalControllers.valueAt(i);
                boolean[] lTEStatus = valueAt.getLTEStatus();
                for (int i2 = 0; i2 < lTEStatus.length; i2++) {
                    if (valueAt.getSimSlotIndex() < zArr.length) {
                        zArr[valueAt.getSimSlotIndex()][i2] = lTEStatus[i2];
                    }
                }
            }
            if (zArr[0][0] && zArr[1][0]) {
                iArr[0] = 3;
            } else if (zArr[0][0] && !zArr[1][0]) {
                iArr[0] = 1;
            } else if (!zArr[0][0] && zArr[1][0]) {
                iArr[0] = 2;
            }
            if (zArr[0][2] && zArr[1][2]) {
                iArr[2] = 3;
            } else if (zArr[0][2] && !zArr[1][2]) {
                iArr[2] = 1;
            } else if (!zArr[0][2] && zArr[1][2]) {
                iArr[2] = 2;
            }
            if (OpUtils.isUSS() && iArr[2] > 0) {
                iArr[0] = 0;
            }
            StringBuffer stringBuffer = new StringBuffer();
            for (int i3 = 0; i3 < 6; i3++) {
                stringBuffer.append(String.valueOf(iArr[i3]));
            }
            NetworkController.IconState[] multiLTEIcons = getMultiLTEIcons(iArr);
            Log.i("NetworkController", " multiLTEstatus:" + ((Object) stringBuffer) + " ShowHD:" + OpUtils.isSupportShowHD());
            NetworkController.IconState[] iconStateArr = this.mLTEIconStates;
            if (!(iconStateArr[0] == multiLTEIcons[0] && iconStateArr[2] == multiLTEIcons[2])) {
                this.mLTEIconStates = multiLTEIcons;
                this.mCallbackHandler.setLTEStatus(multiLTEIcons);
            }
        }
    }

    private NetworkController.IconState[] getMultiLTEIcons(int[] iArr) {
        int[] iArr2 = new int[iArr.length];
        int length = iArr.length;
        NetworkController.IconState[] iconStateArr = new NetworkController.IconState[length];
        int i = 0;
        int i2 = 0;
        while (true) {
            int[] iArr3 = this.mProvisionState;
            if (i >= iArr3.length) {
                break;
            }
            if (iArr3[i] == 1) {
                i2++;
            }
            i++;
        }
        boolean z = i2 > 1 && this.mMobileSignalControllers.size() > 1;
        if (i2 > 0) {
            if (OpUtils.isSupportShowVoLTE(this.mContext)) {
                if (!OpUtils.isSupportShowHD()) {
                    if (OpUtils.isSupportMultiLTEstatus(this.mContext) && z && iArr[0] > 0) {
                        int i3 = iArr[0];
                        int[] iArr4 = OpSignalIcons.VOLTE_ICONS;
                        if (i3 <= iArr4.length) {
                            iArr2[0] = iArr4[iArr[0] - 1];
                        }
                    }
                    if (iArr[0] > 0) {
                        iArr2[0] = OpSignalIcons.VOLTE;
                    }
                } else if (iArr[0] == 0) {
                    iArr2[0] = OpSignalIcons.HD_UNAVAILABLE;
                } else {
                    if (OpUtils.isSupportMultiLTEstatus(this.mContext) && z && iArr[0] > 0) {
                        int i4 = iArr[0];
                        int[] iArr5 = OpSignalIcons.HD_ICONS;
                        if (i4 <= iArr5.length) {
                            iArr2[0] = iArr5[iArr[0] - 1];
                        }
                    }
                    iArr2[0] = OpSignalIcons.HD;
                }
            }
            if (OpUtils.isSupportShowVoWifi()) {
                if (OpUtils.isSupportMultiLTEstatus(this.mContext) && z && iArr[2] > 0) {
                    int i5 = iArr[2];
                    int[] iArr6 = OpSignalIcons.VOWIFI_ICONS;
                    if (i5 <= iArr6.length) {
                        iArr2[2] = iArr6[iArr[2] - 1];
                    }
                }
                if (iArr[2] > 0) {
                    iArr2[2] = OpSignalIcons.VOWIFI;
                }
            }
        }
        for (int i6 = 0; i6 < length; i6++) {
            iconStateArr[i6] = new NetworkController.IconState(iArr2[i6] > 0, iArr2[i6], null);
        }
        return iconStateArr;
    }

    private void onProvisionChange(int i, int i2) {
        if (i < this.mProvisionState.length) {
            Log.i("NetworkController", "onProvisionChange slotId:" + i + " provi:" + i2);
            this.mProvisionState[i] = i2;
            onLTEStatusUpdate();
            CallbackHandler callbackHandler = this.mCallbackHandler;
            if (callbackHandler != null) {
                callbackHandler.setProvision(i, i2);
            }
        }
    }

    private int getSlotProvisionStatus(int i, int i2) {
        if (this.mExtTelephony == null) {
            this.mExtTelephony = IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone"));
        }
        try {
            i2 = this.mExtTelephony.getCurrentUiccCardProvisioningStatus(i);
        } catch (RemoteException e) {
            this.mExtTelephony = null;
            Log.e("NetworkController", "Failed to get pref, slotId: " + i + " Exception: " + e);
        } catch (NullPointerException e2) {
            this.mExtTelephony = null;
            Log.e("NetworkController", "Failed to get pref, slotId: " + i + " Exception: " + e2);
        }
        Log.d("NetworkController", "getSlotProvisionStatus slotId: " + i + ", status = " + i2);
        return i2;
    }

    private void initProvistionState() {
        Log.i("NetworkController", "init provision");
        OpUtils.getSimCount();
        for (int i = 0; i < this.mProvisionState.length; i++) {
            onProvisionChange(i, getSlotProvisionStatus(i, 1));
        }
    }

    private void checkVirtualSimcard() {
        int simCount = this.mPhone.getSimCount();
        boolean z = false;
        for (int i = 0; i < simCount; i++) {
            boolean isSoftSIM = isSoftSIM(i);
            boolean equals = "1".equals(getVirtualPilot(this.mContext, i));
            int i2 = SOFTSIM_DISABLE;
            if (isSoftSIM && equals) {
                i2 = SOFTSIM_ENABLE_PILOT;
            } else if (isSoftSIM && !equals) {
                i2 = SOFTSIM_ENABLE;
            }
            Log.i("NetworkController", "checkVirtualSimcard slotId:" + i + " virtualSimState:" + i2);
            int[] iArr = this.softSimState;
            if (i2 != iArr[i]) {
                iArr[i] = i2;
                if (i2 == SOFTSIM_ENABLE_PILOT) {
                    this.mConnectivityManager.stopTethering(0);
                }
                z = true;
            }
        }
        if (z) {
            notifySetVirtualSimstate(this.softSimState);
        }
    }

    private boolean isSoftSIM(int i) {
        if (this.mExtTelephony == null) {
            this.mExtTelephony = IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone"));
        }
        try {
            if (this.mExtTelephony != null) {
                Bundle bundle = new Bundle();
                bundle.putInt("phone", i);
                Log.i("NetworkController", "isSoftSIM slot = " + i);
                Method declaredMethod = this.mExtTelephony.getClass().getDeclaredMethod("generalGetter", String.class, Bundle.class);
                declaredMethod.setAccessible(true);
                if (((Bundle) declaredMethod.invoke(this.mExtTelephony, "isSoftSIM", bundle)).getBoolean("isSoftSIM", false)) {
                    Log.i("NetworkController", "slot " + i + " is softsim");
                    return true;
                }
                Log.i("NetworkController", "slot " + i + " is NOT softsim");
            }
        } catch (Exception e) {
            Log.i("NetworkController", "exception : " + e);
        }
        return false;
    }

    private String getVirtualPilot(Context context, int i) {
        String str = null;
        if (context == null) {
            return null;
        }
        try {
            Cursor query = context.getContentResolver().query(SOFTSIM_URL, new String[]{"slot", "iccid", "permit_package", "forbid_package", "pilot"}, new StringBuilder("slot=\"" + i + "\"").toString(), null, "slot");
            if (query != null) {
                query.moveToFirst();
                while (!query.isAfterLast()) {
                    str = query.getString(4);
                    query.moveToNext();
                }
                query.close();
            }
        } catch (Exception e) {
            Log.d("NetworkController", "getVirtualIccid SQLiteException " + e);
        }
        Log.d("NetworkController", "Virtual sim slot:" + i + " pilot = " + str);
        return str;
    }

    private void notifySetVirtualSimstate(int[] iArr) {
        this.mCallbackHandler.setVirtualSimstate(iArr);
        for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
            this.mMobileSignalControllers.valueAt(i).setVirtualSimstate(iArr);
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController
    public void setNetworkSpeedController(NetworkSpeedController networkSpeedController) {
        this.mNetworkSpeedController = networkSpeedController;
        if (networkSpeedController != null) {
            networkSpeedController.updateConnectivity(this.mConnectedTransports, this.mValidatedTransports);
        }
    }

    private boolean hasSimReady() {
        int simCount = this.mPhone.getSimCount();
        for (int i = 0; i < simCount; i++) {
            int simState = this.mPhone.getSimState(i);
            if (simState == 5 || simState == 10) {
                return true;
            }
        }
        return false;
    }

    private void opActionTetherErrorAlertDialog() {
        if (!this.mOpActionTetherDialogShowing) {
            int i = C0015R$string.hotspot_operator_dialog_othererror_title;
            int i2 = C0015R$string.hotspot_operator_dialog_othererror_msg;
            AlertDialog create = new AlertDialog.Builder(this.mContext).setMessage(i2).setTitle(i).setCancelable(false).setPositiveButton(C0015R$string.hotspot_operator_dialog_othererror_button, new DialogInterface.OnClickListener() { // from class: com.android.systemui.statusbar.policy.NetworkControllerImpl.10
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i3) {
                    NetworkControllerImpl.this.mOpActionTetherDialogShowing = false;
                }
            }).create();
            create.getWindow().setType(2014);
            create.setOnShowListener(new DialogInterface.OnShowListener(create) { // from class: com.android.systemui.statusbar.policy.-$$Lambda$NetworkControllerImpl$SPi8Wbr19DM3C_1aQLD1glLQZXY
                public final /* synthetic */ AlertDialog f$0;

                {
                    this.f$0 = r1;
                }

                @Override // android.content.DialogInterface.OnShowListener
                public final void onShow(DialogInterface dialogInterface) {
                    this.f$0.getButton(-1).setTextColor(-65536);
                }
            });
            this.mOpActionTetherDialogShowing = true;
            create.show();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSmartLinkChange(boolean z) {
        Context context = this.mContext;
        if (context != null) {
            ContentResolver contentResolver = context.getContentResolver();
            boolean z2 = true;
            boolean z3 = Settings.System.getIntForUser(contentResolver, "oneplus_smart_link_selection", 0, KeyguardUpdateMonitor.getCurrentUser()) != 0;
            if (Settings.System.getIntForUser(contentResolver, "download_smart_link_aggregation", 0, KeyguardUpdateMonitor.getCurrentUser()) == 0) {
                z2 = false;
            }
            boolean z4 = z3 | z2;
            Log.i("NetworkController", " smartLinkStateEnable:" + z4 + " smartLinkSelection:" + z4 + " smartLinkAggregation:" + z2);
            if (z4 != this.mSmartlinkEnable || z) {
                this.mSmartlinkEnable = z4;
                for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
                    this.mMobileSignalControllers.valueAt(i).setSmartlinkEnable(this.mSmartlinkEnable);
                }
                notifyListeners();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onFackPSIcon(boolean z) {
        Context context = this.mContext;
        if (context != null) {
            boolean z2 = Settings.System.getIntForUser(context.getContentResolver(), "oneplus_fake_ps_icon", 0, KeyguardUpdateMonitor.getCurrentUser()) != 0;
            Log.i("NetworkController", "fakePSIcon:" + z2 + " mFackPSIcon:" + this.mFackPSIcon);
            if (z2 != this.mFackPSIcon || z) {
                this.mFackPSIcon = z2;
                for (int i = 0; i < this.mMobileSignalControllers.size(); i++) {
                    this.mMobileSignalControllers.valueAt(i).setFakePSIconEnable(this.mFackPSIcon);
                }
                notifyListeners();
            }
        }
    }

    /* access modifiers changed from: private */
    public final class SettingObserver extends ContentObserver {
        private final Uri FAKE_PS_ICON = Settings.System.getUriFor("oneplus_fake_ps_icon");
        private final Uri SMART_LINK_AGGREGATION = Settings.System.getUriFor("download_smart_link_aggregation");
        private final Uri SMART_LINK_SELECTION = Settings.System.getUriFor("oneplus_smart_link_selection");

        public SettingObserver() {
            super(new Handler());
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            ContentResolver contentResolver = NetworkControllerImpl.this.mContext.getContentResolver();
            if (OpUtils.isSupportShowDualChannel()) {
                contentResolver.registerContentObserver(this.SMART_LINK_SELECTION, false, this, -1);
                contentResolver.registerContentObserver(this.SMART_LINK_AGGREGATION, false, this, -1);
            }
            contentResolver.registerContentObserver(this.FAKE_PS_ICON, false, this, -1);
            update(null);
        }

        /* access modifiers changed from: package-private */
        public void unObserve() {
            NetworkControllerImpl.this.mContext.getContentResolver().unregisterContentObserver(this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            if (this.FAKE_PS_ICON.equals(uri)) {
                NetworkControllerImpl.this.onFackPSIcon(false);
            } else if (OpUtils.isSupportShowDualChannel()) {
                NetworkControllerImpl.this.onSmartLinkChange(false);
            }
        }
    }
}
