package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthNr;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.OpPhoneStateListener;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyDisplayInfo;
import android.telephony.TelephonyManager;
import android.telephony.ims.ImsMmTelManager;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.feature.MmTelFeature;
import android.text.TextUtils;
import android.util.Log;
import androidx.constraintlayout.widget.R$styleable;
import com.android.ims.FeatureConnector;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.PhoneConstants;
import com.android.settingslib.Utils;
import com.android.settingslib.graph.SignalDrawable;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.statusbar.policy.FiveGServiceClient;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.android.systemui.statusbar.policy.SignalController;
import com.android.systemui.util.ProductUtils;
import com.oneplus.signal.OpSignalIcons;
import com.oneplus.util.OpUtils;
import java.io.PrintWriter;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
public class MobileSignalController extends SignalController<MobileState, MobileIconGroup> {
    private static final boolean[] LTE_DEFAULT_STATUS = {false, false, false, false, false, false};
    private static String[] SHOW_LTE_OPERATORS = {"310004", "310005", "310012", "311480", "311481-9", "310026", "310160", "310170", "310200", "310210", "310220", "310230", "310240", "310250", "310260", "310270", "310280", "310290", "310310", "310330", "310490", "310580", "310660", "310800", "310090", "310150", "310380", "310410", "310560", "310680", "310980", "310990", "310120", "316010", "310020", "23203", "23207", "26002", "26201", "23001", "26006"};
    private int MAX_NOTIFYLISTENER_INTERVAL = 100;
    private int mCallState = 0;
    private ImsMmTelManager.CapabilityCallback mCapabilityCallback = new ImsMmTelManager.CapabilityCallback() { // from class: com.android.systemui.statusbar.policy.MobileSignalController.4
        @Override // android.telephony.ims.ImsMmTelManager.CapabilityCallback
        public void onCapabilitiesStatusChanged(MmTelFeature.MmTelCapabilities mmTelCapabilities) {
            if (!MobileSignalController.this.mImsListening) {
                String str = MobileSignalController.this.mTag;
                Log.d(str, MobileSignalController.this.mCapabilityCallback + " Ims is not listening");
                return;
            }
            ((MobileState) MobileSignalController.this.mCurrentState).voiceCapable = mmTelCapabilities.isCapable(1);
            ((MobileState) MobileSignalController.this.mCurrentState).videoCapable = mmTelCapabilities.isCapable(2);
            String str2 = MobileSignalController.this.mTag;
            Log.d(str2, "onCapabilitiesStatusChanged isVoiceCapable=" + ((MobileState) MobileSignalController.this.mCurrentState).voiceCapable + " isVideoCapable=" + ((MobileState) MobileSignalController.this.mCurrentState).videoCapable);
            MobileSignalController.this.notifyListenersIfNecessary();
        }
    };
    private CellSignalStrengthNr mCellSignalStrengthNr;
    private FiveGServiceClient mClient;
    private NetworkControllerImpl.Config mConfig;
    private int mDataState = 0;
    private MobileIconGroup mDefaultIcons;
    private final NetworkControllerImpl.SubscriptionDefaults mDefaults;
    private FeatureConnector<ImsManager> mFeatureConnector;
    @VisibleForTesting
    FiveGServiceClient.FiveGServiceState mFiveGState;
    @VisibleForTesting
    FiveGStateListener mFiveGStateListener;
    private Handler mHandler;
    private boolean mImsListening = false;
    private ImsManager mImsManager;
    private final ImsMmTelManager.RegistrationCallback mImsRegistrationCallback = new ImsMmTelManager.RegistrationCallback() { // from class: com.android.systemui.statusbar.policy.MobileSignalController.5
        public void onRegistered(int i) {
            String str = MobileSignalController.this.mTag;
            Log.d(str, "onRegistered imsTransportType=" + i);
            if (!MobileSignalController.this.mImsListening) {
                String str2 = MobileSignalController.this.mTag;
                Log.d(str2, MobileSignalController.this.mImsRegistrationCallback + " Ims is not listening");
                return;
            }
            MobileSignalController mobileSignalController = MobileSignalController.this;
            ((MobileState) mobileSignalController.mCurrentState).imsRegistered = true;
            mobileSignalController.notifyListenersIfNecessary();
        }

        public void onRegistering(int i) {
            String str = MobileSignalController.this.mTag;
            Log.d(str, "onRegistering imsTransportType=" + i);
            if (!MobileSignalController.this.mImsListening) {
                String str2 = MobileSignalController.this.mTag;
                Log.d(str2, MobileSignalController.this.mImsRegistrationCallback + " Ims is not listening");
                return;
            }
            MobileSignalController mobileSignalController = MobileSignalController.this;
            ((MobileState) mobileSignalController.mCurrentState).imsRegistered = false;
            mobileSignalController.notifyListenersIfNecessary();
        }

        public void onUnregistered(ImsReasonInfo imsReasonInfo) {
            String str = MobileSignalController.this.mTag;
            Log.d(str, "onDeregistered imsReasonInfo=" + imsReasonInfo);
            if (!MobileSignalController.this.mImsListening) {
                String str2 = MobileSignalController.this.mTag;
                Log.d(str2, MobileSignalController.this.mImsRegistrationCallback + " Ims is not listening");
                return;
            }
            MobileSignalController mobileSignalController = MobileSignalController.this;
            ((MobileState) mobileSignalController.mCurrentState).imsRegistered = false;
            mobileSignalController.notifyListenersIfNecessary();
        }
    };
    @VisibleForTesting
    boolean mInflateSignalStrengths = false;
    private boolean[] mLTEStatus = LTE_DEFAULT_STATUS;
    private long mLastUpdateActivityTime = 0;
    private long mLastUpdateSignalStrengthTime = 0;
    private long mLastUpdateTime = 0;
    private boolean mListening = false;
    private PhoneConstants.DataState mMMSDataState = PhoneConstants.DataState.DISCONNECTED;
    private String mMccmnc = "";
    private final String mNetworkNameDefault;
    private final String mNetworkNameSeparator;
    final Map<String, MobileIconGroup> mNetworkToIconLookup;
    BroadcastReceiver mOPMoblileReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.policy.MobileSignalController.7
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            SubscriptionInfo subscriptionInfo;
            String action = intent.getAction();
            boolean z = false;
            if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                if (MobileSignalController.this.mSubscriptionInfo != null) {
                    int intExtra = intent.getIntExtra("phone", 0);
                    int intExtra2 = intent.getIntExtra("subscription", -1);
                    String stringExtra = intent.getStringExtra("ss");
                    if (SignalController.DEBUG) {
                        String str = MobileSignalController.this.mTag;
                        Log.v(str, "onSIMstateChange state: " + stringExtra + " slotId: " + intExtra + " subId " + intExtra2 + " getSimSlotIndex: " + MobileSignalController.this.getSimSlotIndex());
                    }
                    if (MobileSignalController.this.getSimSlotIndex() == intExtra || MobileSignalController.this.mSubscriptionInfo.getSubscriptionId() == intExtra2) {
                        MobileSignalController mobileSignalController = MobileSignalController.this;
                        ((MobileState) mobileSignalController.mCurrentState).simstate = stringExtra;
                        mobileSignalController.customizeIconsMap();
                        MobileSignalController.this.updateTelephony();
                    }
                }
            } else if ("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED".equals(action)) {
                MobileSignalController mobileSignalController2 = MobileSignalController.this;
                MobileState mobileState = (MobileState) mobileSignalController2.mCurrentState;
                if (mobileSignalController2.getDefaultDataSubId() == MobileSignalController.this.getSubId()) {
                    z = true;
                }
                mobileState.isDefaultDataSubId = z;
                MobileSignalController.this.updateTelephony();
            } else if ("com.oem.intent.action.UST_5GX_ICON".equals(action)) {
                int intExtra3 = intent.getIntExtra("subid", -1);
                boolean booleanExtra = intent.getBooleanExtra("is_show_5gxicon", false);
                if (SignalController.DEBUG) {
                    String str2 = MobileSignalController.this.mTag;
                    Log.d(str2, "onReceive= subId: " + intExtra3 + ", show: " + booleanExtra + ", curState: " + MobileSignalController.this.mShowNrX);
                }
                if (intExtra3 != -1 && (subscriptionInfo = MobileSignalController.this.mSubscriptionInfo) != null && intExtra3 == subscriptionInfo.getSubscriptionId() && MobileSignalController.this.mShowNrX != booleanExtra) {
                    MobileSignalController.this.mShowNrX = booleanExtra;
                    MobileSignalController.this.updateTelephony();
                }
            }
        }
    };
    private final ContentObserver mObserver;
    final OpPhoneStateListener mOpPhoneStateListener;
    private final TelephonyManager mPhone;
    @VisibleForTesting
    final PhoneStateListener mPhoneStateListener;
    private ServiceState mServiceState;
    private boolean mShowNrX = false;
    private SignalStrength mSignalStrength;
    final SubscriptionInfo mSubscriptionInfo;
    private TelephonyDisplayInfo mTelephonyDisplayInfo = new TelephonyDisplayInfo(0, 0);
    private final BroadcastReceiver mVolteSwitchObserver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.policy.MobileSignalController.6
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String str = MobileSignalController.this.mTag;
            Log.d(str, "action=" + intent.getAction());
            if (MobileSignalController.this.mConfig.showVolteIcon) {
                MobileSignalController.this.notifyListeners();
            }
        }
    };
    private int mWwanAccessNetworkType = 0;

    private boolean isVolteSwitchOn() {
        return false;
    }

    private void updateInflateSignalStrength() {
    }

    public MobileSignalController(Context context, NetworkControllerImpl.Config config, boolean z, TelephonyManager telephonyManager, CallbackHandler callbackHandler, NetworkControllerImpl networkControllerImpl, SubscriptionInfo subscriptionInfo, NetworkControllerImpl.SubscriptionDefaults subscriptionDefaults, Looper looper) {
        super("MobileSignalController(" + subscriptionInfo.getSubscriptionId() + ")", context, 0, callbackHandler, networkControllerImpl);
        String str;
        String str2 = this.mTag;
        Log.d(str2, "init() info.getSubscriptionId(): " + subscriptionInfo.getSubscriptionId());
        this.mNetworkToIconLookup = new HashMap();
        this.mConfig = config;
        this.mPhone = telephonyManager;
        this.mDefaults = subscriptionDefaults;
        this.mSubscriptionInfo = subscriptionInfo;
        this.mFiveGStateListener = new FiveGStateListener();
        this.mFiveGState = new FiveGServiceClient.FiveGServiceState();
        this.mPhoneStateListener = new MobilePhoneStateListener(new Executor(new Handler(looper)) { // from class: com.android.systemui.statusbar.policy.-$$Lambda$LfzJt661qZfn2w-6SYHFbD3aMy0
            public final /* synthetic */ Handler f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.concurrent.Executor
            public final void execute(Runnable runnable) {
                this.f$0.post(runnable);
            }
        });
        this.mOpPhoneStateListener = new OpMobilePhoneStateListener(new Executor(new Handler(looper)) { // from class: com.android.systemui.statusbar.policy.-$$Lambda$LfzJt661qZfn2w-6SYHFbD3aMy0
            public final /* synthetic */ Handler f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.concurrent.Executor
            public final void execute(Runnable runnable) {
                this.f$0.post(runnable);
            }
        });
        this.mNetworkNameSeparator = getTextIfExists(C0015R$string.status_bar_network_name_separator).toString();
        this.mNetworkNameDefault = getTextIfExists(17040457).toString();
        this.mHandler = new Handler(looper) { // from class: com.android.systemui.statusbar.policy.MobileSignalController.1
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                int i = message.what;
                if (i != 1000) {
                    switch (i) {
                        case R$styleable.Constraint_layout_goneMarginRight /* 101 */:
                            MobileSignalController.this.updateTelephony();
                            return;
                        case R$styleable.Constraint_layout_goneMarginStart /* 102 */:
                            MobileSignalController.this.mHandler.removeMessages(R$styleable.Constraint_layout_goneMarginStart);
                            MobileSignalController.this.mLastUpdateActivityTime = System.currentTimeMillis();
                            if (SignalController.DEBUG) {
                                String str3 = MobileSignalController.this.mTag;
                                Log.d(str3, "update activity activityIn=" + ((MobileState) MobileSignalController.this.mCurrentState).activityIn + " activityOut=" + ((MobileState) MobileSignalController.this.mCurrentState).activityOut);
                            }
                            MobileSignalController.this.notifyListenersIfNecessary();
                            return;
                        case R$styleable.Constraint_layout_goneMarginTop /* 103 */:
                            if (SignalController.DEBUG) {
                                Log.d(MobileSignalController.this.mTag, "delay update signal strength for VONR");
                            }
                            MobileSignalController.this.mHandler.removeMessages(R$styleable.Constraint_layout_goneMarginTop);
                            MobileSignalController.this.mPhoneStateListener.onSignalStrengthsChanged((SignalStrength) message.obj);
                            return;
                        default:
                            return;
                    }
                } else {
                    MobileSignalController.this.mLastUpdateTime = System.currentTimeMillis();
                    MobileSignalController.this.notifyListenersIfNecessary();
                    if (SignalController.DEBUG) {
                        Log.i(MobileSignalController.this.mTag, "notifyIfNecessary");
                    }
                }
            }
        };
        customizeIconsMap();
        if (subscriptionInfo.getCarrierName() != null) {
            str = subscriptionInfo.getCarrierName().toString();
        } else {
            str = this.mNetworkNameDefault;
        }
        T t = this.mLastState;
        T t2 = this.mCurrentState;
        ((MobileState) t2).networkName = str;
        ((MobileState) t).networkName = str;
        ((MobileState) t2).networkNameData = str;
        ((MobileState) t).networkNameData = str;
        ((MobileState) t2).enabled = z;
        ((MobileState) t).enabled = z;
        MobileIconGroup mobileIconGroup = this.mDefaultIcons;
        ((MobileState) t2).iconGroup = mobileIconGroup;
        ((MobileState) t).iconGroup = mobileIconGroup;
        updateDataSim();
        FiveGServiceClient.getNumLevels(this.mContext);
        this.mObserver = new ContentObserver(new Handler(looper)) { // from class: com.android.systemui.statusbar.policy.MobileSignalController.2
            @Override // android.database.ContentObserver
            public void onChange(boolean z2) {
                MobileSignalController.this.updateTelephony();
            }
        };
    }

    public void setConfiguration(NetworkControllerImpl.Config config) {
        this.mConfig = config;
        updateInflateSignalStrength();
        customizeIconsMap();
        updateTelephony();
    }

    public void setAirplaneMode(boolean z) {
        ((MobileState) this.mCurrentState).airplaneMode = z;
        mayNotifyListeners();
    }

    public void setUserSetupComplete(boolean z) {
        ((MobileState) this.mCurrentState).userSetup = z;
        mayNotifyListeners();
    }

    @Override // com.android.systemui.statusbar.policy.SignalController
    public void updateConnectivity(BitSet bitSet, BitSet bitSet2) {
        boolean z = bitSet2.get(this.mTransportType);
        ((MobileState) this.mCurrentState).isDefault = bitSet.get(this.mTransportType);
        int i = 1;
        ((MobileState) this.mCurrentState).isWifiConnected = bitSet.get(1);
        ((MobileState) this.mCurrentState).isDefaultDataSubId = getDefaultDataSubId() == getSubId();
        if (OpUtils.isGlobalROM(this.mContext)) {
            T t = this.mCurrentState;
            if (((MobileState) t).isDefaultDataSubId) {
                MobileState mobileState = (MobileState) t;
                if (!z && ((MobileState) t).isDefault) {
                    i = 0;
                }
                mobileState.inetCondition = i;
                mayNotifyListeners();
            }
        }
        ((MobileState) this.mCurrentState).inetCondition = 1;
        mayNotifyListeners();
    }

    public void setCarrierNetworkChangeMode(boolean z) {
        ((MobileState) this.mCurrentState).carrierNetworkChangeMode = z;
        updateTelephony();
    }

    public void registerListener() {
        try {
            this.mPhone.listen(this.mPhoneStateListener, 5308897);
            this.mPhone.listen(this.mOpPhoneStateListener, 1);
        } catch (Exception e) {
            String str = this.mTag;
            Log.d(str, "registerListener exception : " + e.toString());
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        intentFilter.addAction("com.oem.intent.action.UST_5GX_ICON");
        this.mContext.registerReceiver(this.mOPMoblileReceiver, intentFilter, null, this.mHandler);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mobile_data"), true, this.mObserver);
        ContentResolver contentResolver = this.mContext.getContentResolver();
        contentResolver.registerContentObserver(Settings.Global.getUriFor("mobile_data" + this.mSubscriptionInfo.getSubscriptionId()), true, this.mObserver);
        this.mListening = true;
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mobile_data"), true, this.mObserver);
        ContentResolver contentResolver2 = this.mContext.getContentResolver();
        contentResolver2.registerContentObserver(Settings.Global.getUriFor("mobile_data" + this.mSubscriptionInfo.getSubscriptionId()), true, this.mObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("data_roaming"), true, this.mObserver);
        ContentResolver contentResolver3 = this.mContext.getContentResolver();
        contentResolver3.registerContentObserver(Settings.Global.getUriFor("data_roaming" + this.mSubscriptionInfo.getSubscriptionId()), true, this.mObserver);
        this.mContext.registerReceiver(this.mVolteSwitchObserver, new IntentFilter("org.codeaurora.intent.action.ACTION_ENHANCE_4G_SWITCH"));
        final int simSlotIndex = this.mSubscriptionInfo.getSimSlotIndex();
        FeatureConnector<ImsManager> featureConnector = new FeatureConnector<>(this.mContext, simSlotIndex, new FeatureConnector.Listener<ImsManager>() { // from class: com.android.systemui.statusbar.policy.MobileSignalController.3
            public ImsManager getFeatureManager() {
                return ImsManager.getInstance(MobileSignalController.this.mContext, simSlotIndex);
            }

            public void connectionReady(ImsManager imsManager) throws ImsException {
                Log.d(MobileSignalController.this.mTag, "ImsManager: connection ready.");
                MobileSignalController.this.mImsManager = imsManager;
                MobileSignalController.this.setListeners();
            }

            public void connectionUnavailable() {
                Log.d(MobileSignalController.this.mTag, "ImsManager: connection unavailable.");
                MobileSignalController.this.removeListeners();
            }
        }, "?");
        this.mFeatureConnector = featureConnector;
        featureConnector.connect();
    }

    public void unregisterListener() {
        String str = this.mTag;
        StringBuilder sb = new StringBuilder();
        sb.append("unregisterListener mListening: ");
        sb.append(this.mListening);
        sb.append(" FeatureConnector exist: ");
        sb.append(this.mFeatureConnector != null);
        Log.d(str, sb.toString());
        if (this.mListening) {
            this.mPhone.listen(this.mPhoneStateListener, 0);
            this.mPhone.listen(this.mOpPhoneStateListener, 0);
            this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
            this.mContext.unregisterReceiver(this.mVolteSwitchObserver);
            FeatureConnector<ImsManager> featureConnector = this.mFeatureConnector;
            if (featureConnector != null) {
                featureConnector.disconnect();
                removeListeners();
            }
            this.mFeatureConnector = null;
            this.mContext.unregisterReceiver(this.mOPMoblileReceiver);
            this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
            cleanLTEStatus();
            NetworkControllerImpl networkControllerImpl = this.mNetworkController;
            if (networkControllerImpl != null) {
                networkControllerImpl.onLTEStatusUpdate();
            } else {
                Log.w(this.mTag, "unregisterListener mNetworkController is null");
            }
        }
    }

    private void mapIconSets() {
        opMapIconSets();
    }

    private String getIconKey() {
        if (this.mTelephonyDisplayInfo.getOverrideNetworkType() == 0) {
            return toIconKey(this.mTelephonyDisplayInfo.getNetworkType());
        }
        return toDisplayIconKey(this.mTelephonyDisplayInfo.getOverrideNetworkType());
    }

    private String toIconKey(int i) {
        return Integer.toString(i);
    }

    private String toDisplayIconKey(int i) {
        if (i == 1) {
            return toIconKey(13) + "_CA";
        } else if (i == 2) {
            return toIconKey(13) + "_CA_Plus";
        } else if (i != 3) {
            return i != 4 ? "unsupported" : "5G_Plus";
        } else {
            return "5G";
        }
    }

    private void opMapIconSets() {
        this.mNetworkToIconLookup.clear();
        this.mNetworkToIconLookup.put(toIconKey(5), TelephonyIcons.THREE_G);
        this.mNetworkToIconLookup.put(toIconKey(6), TelephonyIcons.THREE_G);
        this.mNetworkToIconLookup.put(toIconKey(12), TelephonyIcons.THREE_G);
        this.mNetworkToIconLookup.put(toIconKey(14), TelephonyIcons.THREE_G);
        this.mNetworkToIconLookup.put(toIconKey(3), TelephonyIcons.THREE_G);
        this.mNetworkToIconLookup.put(toIconKey(17), TelephonyIcons.THREE_G);
        this.mNetworkToIconLookup.put(toIconKey(20), TelephonyIcons.FIVE_G_SA);
        if (!this.mConfig.showAtLeast3G) {
            this.mNetworkToIconLookup.put(toIconKey(0), TelephonyIcons.UNKNOWN);
            this.mNetworkToIconLookup.put(toIconKey(2), TelephonyIcons.E);
            this.mNetworkToIconLookup.put(toIconKey(4), TelephonyIcons.ONE_X);
            this.mNetworkToIconLookup.put(toIconKey(7), TelephonyIcons.ONE_X);
            this.mDefaultIcons = TelephonyIcons.G;
        } else {
            this.mNetworkToIconLookup.put(toIconKey(0), TelephonyIcons.THREE_G);
            this.mNetworkToIconLookup.put(toIconKey(2), TelephonyIcons.THREE_G);
            this.mNetworkToIconLookup.put(toIconKey(4), TelephonyIcons.THREE_G);
            this.mNetworkToIconLookup.put(toIconKey(7), TelephonyIcons.THREE_G);
            this.mDefaultIcons = TelephonyIcons.THREE_G;
        }
        MobileIconGroup mobileIconGroup = TelephonyIcons.H;
        MobileIconGroup mobileIconGroup2 = TelephonyIcons.H_PLUS;
        this.mNetworkToIconLookup.put(toIconKey(8), mobileIconGroup);
        this.mNetworkToIconLookup.put(toIconKey(9), mobileIconGroup);
        this.mNetworkToIconLookup.put(toIconKey(10), mobileIconGroup2);
        this.mNetworkToIconLookup.put(toIconKey(15), mobileIconGroup2);
        this.mNetworkToIconLookup.put(toIconKey(13), TelephonyIcons.FOUR_G);
        this.mNetworkToIconLookup.put(toIconKey(19), TelephonyIcons.FOUR_G_PLUS);
        this.mNetworkToIconLookup.put(toIconKey(18), TelephonyIcons.WFC);
        this.mNetworkToIconLookup.put(toDisplayIconKey(1), TelephonyIcons.FOUR_G_PLUS);
        this.mNetworkToIconLookup.put(toDisplayIconKey(2), TelephonyIcons.LTE_CA_5G_E);
        this.mNetworkToIconLookup.put(toDisplayIconKey(3), TelephonyIcons.NR_5G);
        this.mNetworkToIconLookup.put(toDisplayIconKey(4), TelephonyIcons.NR_5G_PLUS);
    }

    private int getNumLevels() {
        if (this.mInflateSignalStrengths) {
            return SignalStrength.NUM_SIGNAL_STRENGTH_BINS + 1;
        }
        return SignalStrength.NUM_SIGNAL_STRENGTH_BINS;
    }

    @Override // com.android.systemui.statusbar.policy.SignalController
    public int getCurrentIconId() {
        if (((MobileState) this.mCurrentState).iconGroup == TelephonyIcons.CARRIER_NETWORK_CHANGE) {
            return SignalDrawable.getCarrierChangeState(getNumLevels());
        }
        if (isVirtualSim()) {
            return TelephonyIcons.getOneplusVirtualSimSignalIconId(((MobileState) this.mCurrentState).level);
        }
        if (((MobileState) this.mCurrentState).roaming && !showStacked() && !OpUtils.isRemoveRoamingIcon()) {
            return TelephonyIcons.getOneplusRoamingSignalIconId(((MobileState) this.mCurrentState).level);
        }
        T t = this.mCurrentState;
        if (((MobileState) t).connected) {
            int signalLevel = is5GConnected() ? this.mFiveGState.getSignalLevel() : ((MobileState) this.mCurrentState).level;
            if (is5GConnected() && this.mFiveGState.getRsrp() == -32768) {
                signalLevel = ((MobileState) this.mCurrentState).level;
            }
            if (this.mInflateSignalStrengths) {
                signalLevel++;
            }
            if (SignalController.DEBUG) {
                String str = this.mTag;
                Log.d(str, "getCurrentIconId level " + signalLevel + " 5GState level: " + this.mFiveGState.getSignalLevel() + " Current level: " + ((MobileState) this.mCurrentState).level);
            }
            if (this.mConfig.readIconsFromXml) {
                return getIcons().mSingleSignalIcon;
            }
            return TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH[((MobileState) this.mCurrentState).inetCondition][signalLevel];
        } else if (((MobileState) t).enabled) {
            return TelephonyIcons.TELEPHONY_NO_NETWORK;
        } else {
            return 0;
        }
    }

    @Override // com.android.systemui.statusbar.policy.SignalController
    public int getQsCurrentIconId() {
        return getCurrentIconId();
    }

    private int getVolteResId() {
        int voiceNetworkType = getVoiceNetworkType();
        T t = this.mCurrentState;
        if ((((MobileState) t).voiceCapable || ((MobileState) t).videoCapable) && ((MobileState) this.mCurrentState).imsRegistered) {
            return C0006R$drawable.ic_volte;
        }
        if ((this.mTelephonyDisplayInfo.getNetworkType() == 13 || this.mTelephonyDisplayInfo.getNetworkType() == 19) && voiceNetworkType == 0) {
            return C0006R$drawable.ic_volte_no_voice;
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setListeners() {
        ImsManager imsManager = this.mImsManager;
        if (imsManager == null) {
            Log.e(this.mTag, "setListeners mImsManager is null");
            return;
        }
        try {
            this.mImsListening = true;
            imsManager.addCapabilitiesCallback(this.mCapabilityCallback);
            this.mImsManager.addRegistrationCallback(this.mImsRegistrationCallback);
            String str = this.mTag;
            Log.d(str, "slot:" + getSimSlotIndex() + "addCapabilitiesCallback " + this.mCapabilityCallback + " into " + this.mImsManager);
            String str2 = this.mTag;
            Log.d(str2, "slot:" + getSimSlotIndex() + "addRegistrationCallback " + this.mImsRegistrationCallback + " into " + this.mImsManager);
        } catch (ImsException unused) {
            Log.d(this.mTag, "unable to addCapabilitiesCallback callback.");
        }
        queryImsState();
    }

    private void queryImsState() {
        TelephonyManager createForSubscriptionId = this.mPhone.createForSubscriptionId(this.mSubscriptionInfo.getSubscriptionId());
        ((MobileState) this.mCurrentState).voiceCapable = createForSubscriptionId.isVolteAvailable();
        ((MobileState) this.mCurrentState).videoCapable = createForSubscriptionId.isVideoTelephonyAvailable();
        ((MobileState) this.mCurrentState).imsRegistered = this.mPhone.isImsRegistered(this.mSubscriptionInfo.getSubscriptionId());
        if (SignalController.DEBUG) {
            String str = this.mTag;
            Log.d(str, "queryImsState tm=" + createForSubscriptionId + " phone=" + this.mPhone + " voiceCapable=" + ((MobileState) this.mCurrentState).voiceCapable + " videoCapable=" + ((MobileState) this.mCurrentState).videoCapable + " imsResitered=" + ((MobileState) this.mCurrentState).imsRegistered);
        }
        notifyListenersIfNecessary();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeListeners() {
        ImsManager imsManager = this.mImsManager;
        if (imsManager == null) {
            String str = this.mTag;
            Log.e(str, "slot:" + getSimSlotIndex() + " removeListeners mImsManager is null");
            return;
        }
        try {
            this.mImsListening = false;
            imsManager.removeCapabilitiesCallback(this.mCapabilityCallback);
            this.mImsManager.removeRegistrationListener(this.mImsRegistrationCallback);
            String str2 = this.mTag;
            Log.d(str2, "slot:" + getSimSlotIndex() + " removeCapabilitiesCallback " + this.mCapabilityCallback + " from " + this.mImsManager);
            String str3 = this.mTag;
            Log.d(str3, "slot:" + getSimSlotIndex() + " removeRegistrationCallback " + this.mImsRegistrationCallback + " from " + this.mImsManager);
        } catch (ImsException unused) {
            Log.d(this.mTag, "unable to remove callback.");
        }
    }

    private void mayNotifyListeners() {
        long currentTimeMillis = System.currentTimeMillis() - this.mLastUpdateTime;
        Message message = new Message();
        message.what = 1000;
        this.mHandler.removeMessages(1000);
        if (currentTimeMillis < ((long) this.MAX_NOTIFYLISTENER_INTERVAL)) {
            this.mHandler.sendMessageDelayed(message, 50);
        } else {
            this.mHandler.sendMessage(message);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:119:0x01ba  */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x01fb  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x0173  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x017d  */
    @Override // com.android.systemui.statusbar.policy.SignalController
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyListeners(com.android.systemui.statusbar.policy.NetworkController.SignalCallback r26) {
        /*
        // Method dump skipped, instructions count: 846
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.MobileSignalController.notifyListeners(com.android.systemui.statusbar.policy.NetworkController$SignalCallback):void");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.policy.SignalController
    public MobileState cleanState() {
        return new MobileState();
    }

    private boolean isCdma() {
        SignalStrength signalStrength = this.mSignalStrength;
        return signalStrength != null && !signalStrength.isGsm();
    }

    public boolean isEmergencyOnly() {
        ServiceState serviceState = this.mServiceState;
        return serviceState != null && serviceState.isEmergencyOnly();
    }

    private boolean isRoaming() {
        if (isCarrierNetworkChangeActive() || this.mServiceState == null) {
            return false;
        }
        if (SignalController.DEBUG) {
            String str = this.mTag;
            StringBuilder sb = new StringBuilder();
            sb.append(" isRoaming iconMode:");
            sb.append(this.mServiceState.getCdmaEriIconMode());
            sb.append(" EriIconIndex:");
            sb.append(this.mServiceState.getCdmaEriIconIndex());
            sb.append(" isRoaming:");
            ServiceState serviceState = this.mServiceState;
            sb.append(serviceState != null && serviceState.getRoaming());
            Log.d(str, sb.toString());
        }
        if (OpUtils.isUSS()) {
            ServiceState serviceState2 = this.mServiceState;
            if (serviceState2 == null || !serviceState2.getRoaming()) {
                return false;
            }
            return true;
        } else if (!isCdma() || this.mServiceState == null) {
            ServiceState serviceState3 = this.mServiceState;
            if (serviceState3 == null || !serviceState3.getRoaming()) {
                return false;
            }
            return true;
        } else {
            int eriIconMode = this.mPhone.getCdmaEriInformation().getEriIconMode();
            if (this.mPhone.getCdmaEriInformation().getEriIconIndex() == 1) {
                return false;
            }
            if ((eriIconMode == 0 || eriIconMode == 1) && isInService(this.mServiceState)) {
                return true;
            }
            return false;
        }
    }

    private boolean isCarrierNetworkChangeActive() {
        return ((MobileState) this.mCurrentState).carrierNetworkChangeMode;
    }

    public void handleBroadcast(Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.telephony.action.SERVICE_PROVIDERS_UPDATED")) {
            updateNetworkName(intent.getBooleanExtra("android.telephony.extra.SHOW_SPN", false), intent.getStringExtra("android.telephony.extra.SPN"), intent.getStringExtra("android.telephony.extra.DATA_SPN"), intent.getBooleanExtra("android.telephony.extra.SHOW_PLMN", false), intent.getStringExtra("android.telephony.extra.PLMN"));
            mayNotifyListeners();
        } else if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
            updateDataSim();
            mayNotifyListeners();
        } else if (action.equals("android.intent.action.ANY_DATA_STATE")) {
            String stringExtra = intent.getStringExtra("apnType");
            String stringExtra2 = intent.getStringExtra("state");
            if ("mms".equals(stringExtra)) {
                if (SignalController.DEBUG) {
                    String str = this.mTag;
                    Log.d(str, "handleBroadcast MMS connection state=" + stringExtra2);
                }
                this.mMMSDataState = PhoneConstants.DataState.valueOf(stringExtra2);
                updateTelephony();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDataSim() {
        int activeDataSubId = this.mDefaults.getActiveDataSubId();
        boolean z = true;
        if (SubscriptionManager.isValidSubscriptionId(activeDataSubId)) {
            MobileState mobileState = (MobileState) this.mCurrentState;
            if (activeDataSubId != this.mSubscriptionInfo.getSubscriptionId()) {
                z = false;
            }
            mobileState.dataSim = z;
            return;
        }
        ((MobileState) this.mCurrentState).dataSim = true;
    }

    /* access modifiers changed from: package-private */
    public void updateNetworkName(boolean z, String str, String str2, boolean z2, String str3) {
        if (SignalController.CHATTY) {
            Log.d("CarrierLabel", "updateNetworkName showSpn=" + z + " spn=" + str + " dataSpn=" + str2 + " showPlmn=" + z2 + " plmn=" + str3);
        }
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        if (z2 && str3 != null) {
            sb.append(str3);
            sb2.append(str3);
        }
        if (z && str != null) {
            if (sb.length() != 0) {
                sb.append("(");
                sb.append(str);
                sb.append(")");
            } else {
                sb.append(str);
            }
            if (sb.length() != 0) {
                sb.append(this.mNetworkNameSeparator);
            }
            sb.append(str);
        }
        if (sb.length() != 0) {
            ((MobileState) this.mCurrentState).networkName = sb.toString();
        } else {
            ((MobileState) this.mCurrentState).networkName = this.mNetworkNameDefault;
        }
        if (z && str2 != null) {
            if (sb2.length() != 0) {
                sb2.append("(");
                sb2.append(str2);
                sb2.append(")");
            } else {
                sb2.append(str2);
            }
            if (sb2.length() != 0) {
                sb2.append(this.mNetworkNameSeparator);
            }
            sb2.append(str2);
        }
        if (sb2.length() != 0) {
            ((MobileState) this.mCurrentState).networkNameData = sb2.toString();
            return;
        }
        ((MobileState) this.mCurrentState).networkNameData = this.mNetworkNameDefault;
    }

    private final int getCdmaLevel() {
        List cellSignalStrengths = this.mSignalStrength.getCellSignalStrengths(CellSignalStrengthCdma.class);
        if (!cellSignalStrengths.isEmpty()) {
            return ((CellSignalStrengthCdma) cellSignalStrengths.get(0)).getLevel();
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void updateTelephony() {
        ServiceState serviceState;
        ServiceState serviceState2;
        int voiceNetworkType;
        if (SignalController.DEBUG) {
            Log.d(this.mTag, "updateTelephonySignalStrength: hasService=" + isInService(this.mServiceState) + " ss=" + this.mSignalStrength + " displayInfo=" + this.mTelephonyDisplayInfo);
        }
        checkDefaultData();
        boolean z = true;
        ((MobileState) this.mCurrentState).connected = isInService(this.mServiceState) && this.mSignalStrength != null;
        if (((MobileState) this.mCurrentState).connected) {
            if (this.mSignalStrength.isGsm() || !this.mConfig.alwaysShowCdmaRssi) {
                ((MobileState) this.mCurrentState).level = showStacked() ? this.mSignalStrength.getSmoothSignalLevelAll()[1] : this.mSignalStrength.getSmoothSignalLevel();
                if (this.mConfig.showRsrpSignalLevelforLTE) {
                    if (SignalController.DEBUG) {
                        Log.d(this.mTag, "updateTelephony CS:" + this.mServiceState.getVoiceNetworkType() + "/" + TelephonyManager.getNetworkTypeName(this.mServiceState.getVoiceNetworkType()) + ", PS:" + this.mServiceState.getDataNetworkType() + "/" + TelephonyManager.getNetworkTypeName(this.mServiceState.getDataNetworkType()));
                    }
                    int dataNetworkType = this.mServiceState.getDataNetworkType();
                    if (dataNetworkType == 13 || dataNetworkType == 19) {
                        ((MobileState) this.mCurrentState).level = getAlternateLteLevel(this.mSignalStrength);
                    } else if (dataNetworkType == 0 && ((voiceNetworkType = this.mServiceState.getVoiceNetworkType()) == 13 || voiceNetworkType == 19)) {
                        ((MobileState) this.mCurrentState).level = getAlternateLteLevel(this.mSignalStrength);
                    }
                }
            } else {
                ((MobileState) this.mCurrentState).level = getCdmaLevel();
            }
        }
        if (!OpUtils.isSupportFiveBar()) {
            T t = this.mCurrentState;
            MobileState mobileState = (MobileState) t;
            int i = 4;
            if (((MobileState) t).level <= 4) {
                i = ((MobileState) t).level;
            }
            mobileState.level = i;
        }
        String iconKey = getIconKey();
        if (SignalController.DEBUG) {
            Log.d(this.mTag, "updateTelephony iconKey " + iconKey);
        }
        if (this.mNetworkToIconLookup.get(iconKey) == null) {
            ((MobileState) this.mCurrentState).iconGroup = this.mDefaultIcons;
        } else if (showStacked()) {
            ((MobileState) this.mCurrentState).iconGroup = new MobileIconGroup(this.mNetworkToIconLookup.get(iconKey), getDataNetworkType(), getVoiceNetworkType(), getVoiceSignalLevel(), ((MobileState) this.mCurrentState).level, isRoaming(), showLTE());
            if (SignalController.DEBUG) {
                Log.d(this.mTag, " showStacked dataType:" + iconKey + " getCurrentPhoneType:" + TelephonyManager.getDefault().getCurrentPhoneType(this.mSubscriptionInfo.getSubscriptionId()) + " SubscriptionId:" + this.mSubscriptionInfo.getSubscriptionId());
            }
        } else {
            ((MobileState) this.mCurrentState).iconGroup = this.mNetworkToIconLookup.get(iconKey);
        }
        if (this.mTelephonyDisplayInfo.getNetworkType() == 20) {
            if (this.mFiveGState.isNrIconTypeValid()) {
                ((MobileState) this.mCurrentState).iconGroup = this.mFiveGState.getIconGroup();
                if (SignalController.DEBUG) {
                    Log.d(this.mTag, "get 5G SA icon from side-car");
                }
            }
            int nrLevel = getNrLevel();
            if (nrLevel > this.mFiveGState.getSignalLevel()) {
                ((MobileState) this.mCurrentState).level = nrLevel;
                if (SignalController.DEBUG) {
                    Log.d(this.mTag, "get 5G SA sinal strength from AOSP");
                }
            } else {
                updateLevelFromFiveGState();
                if (SignalController.DEBUG) {
                    Log.d(this.mTag, "get 5G SA sinal strength from side-car");
                }
            }
        }
        if (isSideCarValid()) {
            ((MobileState) this.mCurrentState).iconGroup = this.mFiveGState.getIconGroup();
            updateLevelFromFiveGState();
        } else if (!showStacked()) {
            ((MobileState) this.mCurrentState).iconGroup = getNetworkTypeIconGroup();
        }
        T t2 = this.mCurrentState;
        MobileState mobileState2 = (MobileState) t2;
        if (!((MobileState) t2).connected || !(this.mDataState == 2 || this.mMMSDataState == PhoneConstants.DataState.CONNECTED)) {
            z = false;
        }
        mobileState2.dataConnected = z;
        boolean isRoaming = isRoaming();
        T t3 = this.mCurrentState;
        if (((MobileState) t3).roaming != isRoaming) {
            ((MobileState) t3).roaming = isRoaming;
            customizeIconsMap();
        }
        if (SignalController.DEBUG) {
            Log.d(this.mTag, "updateTelephony, isDataDisabled():" + isDataDisabled());
        }
        if (isCarrierNetworkChangeActive()) {
            ((MobileState) this.mCurrentState).iconGroup = TelephonyIcons.CARRIER_NETWORK_CHANGE;
        } else if (isDataDisabled() && !this.mConfig.alwaysShowDataRatIcon && !OpUtils.isUSS()) {
            if (this.mSubscriptionInfo.getSubscriptionId() != this.mDefaults.getDefaultDataSubId()) {
                ((MobileState) this.mCurrentState).iconGroup = TelephonyIcons.NOT_DEFAULT_DATA;
            } else {
                ((MobileState) this.mCurrentState).iconGroup = TelephonyIcons.DATA_DISABLED;
            }
        }
        boolean isEmergencyOnly = isEmergencyOnly();
        T t4 = this.mCurrentState;
        if (isEmergencyOnly != ((MobileState) t4).isEmergency) {
            ((MobileState) t4).isEmergency = isEmergencyOnly();
            this.mNetworkController.recalculateEmergency();
        }
        if (((MobileState) this.mCurrentState).networkName.equals(this.mNetworkNameDefault) && (serviceState2 = this.mServiceState) != null && !TextUtils.isEmpty(serviceState2.getOperatorAlphaShort())) {
            ((MobileState) this.mCurrentState).networkName = this.mServiceState.getOperatorAlphaShort();
        }
        if (((MobileState) this.mCurrentState).networkNameData.equals(this.mNetworkNameDefault) && (serviceState = this.mServiceState) != null && ((MobileState) this.mCurrentState).dataSim && !TextUtils.isEmpty(serviceState.getOperatorAlphaShort())) {
            ((MobileState) this.mCurrentState).networkNameData = this.mServiceState.getOperatorAlphaShort();
        }
        if (this.mConfig.alwaysShowNetworkTypeIcon) {
            if (isSideCarValid()) {
                ((MobileState) this.mCurrentState).iconGroup = this.mFiveGState.getIconGroup();
            } else if (!showStacked()) {
                String iconKey2 = toIconKey(0);
                if (((MobileState) this.mCurrentState).connected) {
                    if (isDataNetworkTypeAvailable()) {
                        iconKey2 = getIconKey();
                    } else {
                        iconKey2 = toIconKey(getVoiceNetworkType());
                    }
                }
                ((MobileState) this.mCurrentState).iconGroup = this.mNetworkToIconLookup.getOrDefault(iconKey2, this.mDefaultIcons);
            }
            if (SignalController.DEBUG) {
                Log.d(this.mTag, "updateTelephony, alwaysShowNetworkTypeIcon iconGroup:" + ((MobileState) this.mCurrentState).iconGroup);
            }
        }
        ((MobileState) this.mCurrentState).mobileDataEnabled = this.mPhone.isDataEnabled();
        ((MobileState) this.mCurrentState).roamingDataEnabled = this.mPhone.isDataRoamingEnabled();
        mayNotifyListeners();
    }

    private boolean isInService(ServiceState serviceState) {
        if (OpUtils.isUST()) {
            return isCellularInService(serviceState);
        }
        return Utils.isInService(serviceState);
    }

    private boolean isCellularInService(ServiceState serviceState) {
        int ustCombinedServiceState;
        return (serviceState == null || (ustCombinedServiceState = getUstCombinedServiceState(serviceState)) == 3 || ustCombinedServiceState == 1 || ustCombinedServiceState == 2) ? false : true;
    }

    private int getUstCombinedServiceState(ServiceState serviceState) {
        if (serviceState == null) {
            return 1;
        }
        int state = serviceState.getState();
        int dataRegState = serviceState.getDataRegState();
        if ((state == 1 || state == 2) && dataRegState == 0) {
            return 0;
        }
        return state;
    }

    private void updateLevelFromFiveGState() {
        FiveGServiceClient.FiveGServiceState fiveGServiceState = this.mFiveGState;
        if (!(fiveGServiceState == null || fiveGServiceState.getRsrp() == -32768 || this.mFiveGState.getSignalLevel() <= ((MobileState) this.mCurrentState).level)) {
            if (SignalController.DEBUG) {
                String str = this.mTag;
                Log.d(str, "updateLevelFromFiveGState 5g level: " + this.mFiveGState.getSignalLevel() + " current level: " + ((MobileState) this.mCurrentState).level);
            }
            ((MobileState) this.mCurrentState).level = this.mFiveGState.getSignalLevel();
        }
        if (this.mShowNrX) {
            ((MobileState) this.mCurrentState).iconGroup = TelephonyIcons.NR_5G_X;
        }
    }

    private void checkDefaultData() {
        T t = this.mCurrentState;
        if (((MobileState) t).iconGroup != TelephonyIcons.NOT_DEFAULT_DATA) {
            ((MobileState) t).defaultDataOff = false;
            return;
        }
        ((MobileState) t).defaultDataOff = this.mNetworkController.isDataControllerDisabled();
    }

    /* access modifiers changed from: package-private */
    public void onMobileDataChanged() {
        checkDefaultData();
        notifyListenersIfNecessary();
    }

    /* access modifiers changed from: package-private */
    public boolean isDataDisabled() {
        return !this.mPhone.getDataEnabled(this.mSubscriptionInfo.getSubscriptionId()) || (OpUtils.isUST() && !isInService(this.mServiceState));
    }

    private boolean isDataNetworkTypeAvailable() {
        if (this.mTelephonyDisplayInfo.getNetworkType() == 0) {
            return false;
        }
        int dataNetworkType = getDataNetworkType();
        int voiceNetworkType = getVoiceNetworkType();
        if ((dataNetworkType == 6 || dataNetworkType == 12 || dataNetworkType == 14 || dataNetworkType == 13 || dataNetworkType == 19) && ((voiceNetworkType == 16 || voiceNetworkType == 7 || voiceNetworkType == 4) && !isCallIdle())) {
            return false;
        }
        if (OpUtils.isGlobalROM(this.mContext) || dataNetworkType != 18 || !this.mConfig.alwaysShowNetworkTypeIcon) {
            return true;
        }
        return false;
    }

    private boolean isCallIdle() {
        return this.mCallState == 0;
    }

    private int getVoiceNetworkType() {
        ServiceState serviceState = this.mServiceState;
        if (serviceState != null) {
            return serviceState.getVoiceNetworkType();
        }
        return 0;
    }

    private int getDataNetworkType() {
        ServiceState serviceState = this.mServiceState;
        if (serviceState != null) {
            return serviceState.getDataNetworkType();
        }
        return 0;
    }

    private int getAlternateLteLevel(SignalStrength signalStrength) {
        int lteDbm = signalStrength.getLteDbm();
        if (lteDbm == Integer.MAX_VALUE) {
            int level = signalStrength.getLevel();
            if (SignalController.DEBUG) {
                String str = this.mTag;
                Log.d(str, "getAlternateLteLevel lteRsrp:INVALID  signalStrengthLevel = " + level);
            }
            return level;
        }
        int i = 0;
        if (lteDbm <= -44) {
            if (lteDbm >= -97) {
                i = 4;
            } else if (lteDbm >= -105) {
                i = 3;
            } else if (lteDbm >= -113) {
                i = 2;
            } else if (lteDbm >= -120) {
                i = 1;
            }
        }
        if (SignalController.DEBUG) {
            String str2 = this.mTag;
            Log.d(str2, "getAlternateLteLevel lteRsrp:" + lteDbm + " rsrpLevel = " + i);
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setActivity(int i) {
        boolean z = false;
        ((MobileState) this.mCurrentState).activityIn = i == 3 || i == 1;
        MobileState mobileState = (MobileState) this.mCurrentState;
        if (i == 3 || i == 2) {
            z = true;
        }
        mobileState.activityOut = z;
        long currentTimeMillis = System.currentTimeMillis() - this.mLastUpdateActivityTime;
        Message message = new Message();
        message.what = R$styleable.Constraint_layout_goneMarginStart;
        this.mHandler.removeMessages(R$styleable.Constraint_layout_goneMarginStart);
        if (currentTimeMillis < 500) {
            this.mHandler.sendMessageDelayed(message, 500 - currentTimeMillis);
        } else {
            this.mHandler.sendMessage(message);
        }
    }

    public void registerFiveGStateListener(FiveGServiceClient fiveGServiceClient) {
        fiveGServiceClient.registerListener(this.mSubscriptionInfo.getSimSlotIndex(), this.mFiveGStateListener);
        this.mClient = fiveGServiceClient;
    }

    public void unregisterFiveGStateListener(FiveGServiceClient fiveGServiceClient) {
        fiveGServiceClient.unregisterListener(this.mSubscriptionInfo.getSimSlotIndex());
    }

    private boolean isDataRegisteredOnLte() {
        int dataNetworkType = getDataNetworkType();
        return dataNetworkType == 13 || dataNetworkType == 19;
    }

    private boolean isDataRegisteredOnLteNr() {
        int dataNetworkType = getDataNetworkType();
        return dataNetworkType == 13 || dataNetworkType == 19 || dataNetworkType == 20;
    }

    private boolean is5GConnected() {
        FiveGServiceClient.FiveGServiceState fiveGServiceState = this.mFiveGState;
        if (fiveGServiceState == null) {
            Log.d(this.mTag, "mFiveGState is null");
            return false;
        } else if (fiveGServiceState.isConnectedOnSaMode() || (this.mFiveGState.isConnectedOnNsaMode() && isDataRegisteredOnLteNr())) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isSideCarValid() {
        if (SignalController.DEBUG) {
            String str = this.mTag;
            Log.d(str, "isSideCarSaValid " + isSideCarSaValid() + " isSideCarNsaValid " + isSideCarNsaValid());
        }
        return isSideCarSaValid() || isSideCarNsaValid();
    }

    private boolean isSideCarSaValid() {
        if (this.mFiveGState.getNrConfigType() != 1 || !this.mFiveGState.isNrIconTypeValid()) {
            return false;
        }
        return true;
    }

    private boolean isSideCarNsaValid() {
        return this.mFiveGState.isNrIconTypeValid() && isDataRegisteredOnLte();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isCellSignalStrengthNrValid() {
        CellSignalStrengthNr cellSignalStrengthNr = this.mCellSignalStrengthNr;
        return cellSignalStrengthNr != null && cellSignalStrengthNr.isValid();
    }

    private int getNrLevel() {
        CellSignalStrengthNr cellSignalStrengthNr = this.mCellSignalStrengthNr;
        if (cellSignalStrengthNr != null) {
            return cellSignalStrengthNr.getLevel();
        }
        return 0;
    }

    private MobileIconGroup getNetworkTypeIconGroup() {
        String str;
        int overrideNetworkType = this.mTelephonyDisplayInfo.getOverrideNetworkType();
        if (overrideNetworkType == 0 || overrideNetworkType == 4 || overrideNetworkType == 3) {
            int networkType = this.mTelephonyDisplayInfo.getNetworkType();
            if (networkType == 0) {
                networkType = getVoiceNetworkType();
            }
            str = toIconKey(networkType);
        } else {
            str = toDisplayIconKey(overrideNetworkType);
        }
        return this.mNetworkToIconLookup.getOrDefault(str, this.mDefaultIcons);
    }

    private boolean showDataRatIcon() {
        T t = this.mCurrentState;
        return ((MobileState) t).mobileDataEnabled && (((MobileState) t).roamingDataEnabled || !((MobileState) t).roaming);
    }

    private int getEnhancementDataRatIcon() {
        MobileIconGroup mobileIconGroup;
        if (!showDataRatIcon()) {
            return 0;
        }
        if (isSideCarValid()) {
            mobileIconGroup = this.mFiveGState.getIconGroup();
        } else {
            mobileIconGroup = getNetworkTypeIconGroup();
        }
        return mobileIconGroup.mDataType;
    }

    private boolean isVowifiAvailable() {
        T t = this.mCurrentState;
        return ((MobileState) t).voiceCapable && ((MobileState) t).imsRegistered && getDataNetworkType() == 18;
    }

    private MobileIconGroup getVowifiIconGroup() {
        if (isVowifiAvailable() && !isCallIdle()) {
            return TelephonyIcons.VOWIFI_CALLING;
        }
        if (isVowifiAvailable()) {
            return TelephonyIcons.VOWIFI;
        }
        return null;
    }

    @Override // com.android.systemui.statusbar.policy.SignalController
    public void dump(PrintWriter printWriter) {
        super.dump(printWriter);
        printWriter.println("  mSubscription=" + this.mSubscriptionInfo + ",");
        printWriter.println("  mServiceState=" + this.mServiceState + ",");
        printWriter.println("  mSignalStrength=" + this.mSignalStrength + ",");
        printWriter.println("  mTelephonyDisplayInfo=" + this.mTelephonyDisplayInfo + ",");
        printWriter.println("  mDataState=" + this.mDataState + ",");
        printWriter.println("  mInflateSignalStrengths=" + this.mInflateSignalStrengths + ",");
        printWriter.println("  isDataDisabled=" + isDataDisabled() + ",");
        printWriter.println("  mFiveGState=" + this.mFiveGState + ",");
    }

    class OpMobilePhoneStateListener extends OpPhoneStateListener {
        public OpMobilePhoneStateListener(Executor executor) {
            super(executor);
        }

        public void onImsCapabilityStatusChange(boolean[] zArr) {
            StringBuffer stringBuffer = new StringBuffer();
            boolean z = false;
            for (int i = 0; i < zArr.length; i++) {
                if (MobileSignalController.this.mLTEStatus[i] != zArr[i]) {
                    stringBuffer.append(String.valueOf(zArr[i]).toUpperCase());
                    z = true;
                } else {
                    stringBuffer.append(String.valueOf(zArr[i]).toLowerCase());
                }
                stringBuffer.append(",");
            }
            MobileSignalController.this.mLTEStatus = zArr;
            if (z || SignalController.DEBUG) {
                Log.i(MobileSignalController.this.mTag, "onImsCapabilityStatusChange:" + stringBuffer.toString());
            }
            NetworkControllerImpl networkControllerImpl = MobileSignalController.this.mNetworkController;
            if (networkControllerImpl != null && z) {
                networkControllerImpl.onLTEStatusUpdate();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldDelaySignalStrengthsUpdateforVoNR(SignalStrength signalStrength) {
        this.mHandler.removeMessages(R$styleable.Constraint_layout_goneMarginTop);
        long j = 0;
        boolean z = false;
        if (signalStrength == null || signalStrength.getLevel() != 0 || !is5GConnected() || !isInService(this.mServiceState) || ((MobileState) this.mCurrentState).simstate == "ABSENT") {
            this.mLastUpdateSignalStrengthTime = 0;
        } else {
            long currentTimeMillis = System.currentTimeMillis();
            long j2 = this.mLastUpdateSignalStrengthTime;
            long j3 = currentTimeMillis - j2;
            if (j2 == 0) {
                this.mLastUpdateSignalStrengthTime = currentTimeMillis;
                z = true;
                j = 1000;
            } else if (j3 < 1000) {
                z = true;
                j = j3;
            }
            if (z) {
                Message message = new Message();
                message.what = R$styleable.Constraint_layout_goneMarginTop;
                message.obj = signalStrength;
                this.mHandler.sendMessageDelayed(message, j);
                Log.d(this.mTag, "onSignalStrengthsChanged delay " + j + "ms  update for VONR");
            }
        }
        return z;
    }

    class MobilePhoneStateListener extends PhoneStateListener {
        public MobilePhoneStateListener(Executor executor) {
            super(executor);
        }

        @Override // android.telephony.PhoneStateListener
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            if (MobileSignalController.this.mSignalStrength != null) {
                int i = signalStrength.getSmoothSignalLevelAll()[0];
                int i2 = signalStrength.getSmoothSignalLevelAll()[1];
                int smoothSignalLevel = signalStrength.getSmoothSignalLevel();
                int i3 = MobileSignalController.this.mSignalStrength.getSmoothSignalLevelAll()[0];
                int i4 = MobileSignalController.this.mSignalStrength.getSmoothSignalLevelAll()[1];
                int smoothSignalLevel2 = MobileSignalController.this.mSignalStrength.getSmoothSignalLevel();
                if (!(i == i3 && i2 == i4 && smoothSignalLevel == smoothSignalLevel2)) {
                    String str = MobileSignalController.this.mTag;
                    Log.d(str, "onSignalStrengthsChanged signalStrength=" + signalStrength + " level=" + smoothSignalLevel + " voicelevel=" + i + " datalevel=" + i2);
                }
            }
            if (!OpUtils.isUST() || !MobileSignalController.this.shouldDelaySignalStrengthsUpdateforVoNR(signalStrength)) {
                MobileSignalController.this.mSignalStrength = signalStrength;
                updateCellSignalStrengthNr(signalStrength);
                MobileSignalController.this.updateTelephony();
            }
        }

        private void updateCellSignalStrengthNr(SignalStrength signalStrength) {
            if (signalStrength != null) {
                List cellSignalStrengths = MobileSignalController.this.mSignalStrength.getCellSignalStrengths(CellSignalStrengthNr.class);
                if (cellSignalStrengths == null || cellSignalStrengths.size() <= 0) {
                    MobileSignalController.this.mCellSignalStrengthNr = null;
                } else {
                    MobileSignalController.this.mCellSignalStrengthNr = (CellSignalStrengthNr) cellSignalStrengths.get(0);
                }
            } else {
                MobileSignalController.this.mCellSignalStrengthNr = null;
            }
            if (MobileSignalController.this.mTelephonyDisplayInfo.getNetworkType() == 20 && !MobileSignalController.this.isCellSignalStrengthNrValid() && MobileSignalController.this.mClient != null) {
                MobileSignalController.this.mClient.queryNrSignalStrength(MobileSignalController.this.mSubscriptionInfo.getSimSlotIndex());
            }
        }

        @Override // android.telephony.PhoneStateListener
        public void onServiceStateChanged(ServiceState serviceState) {
            if (serviceState == null) {
                Log.d(MobileSignalController.this.mTag, "onServiceStateChanged / state == null");
                return;
            }
            NetworkRegistrationInfo networkRegistrationInfo = serviceState.getNetworkRegistrationInfo(2, 1);
            MobileSignalController.this.mWwanAccessNetworkType = networkRegistrationInfo != null ? networkRegistrationInfo.getAccessNetworkTechnology() : serviceState.getDataNetworkType();
            String str = MobileSignalController.this.mTag;
            Log.d(str, "onServiceStateChanged voiceState=" + serviceState.getVoiceRegState() + " dataState=" + serviceState.getDataRegState() + " isUsingCarrierAggregation:" + serviceState.isUsingCarrierAggregation() + " getDataNetworkType:" + serviceState.getDataNetworkType() + " getVoiceNetworkType:" + serviceState.getVoiceNetworkType() + " wwanAccessNetworkType:" + MobileSignalController.this.mWwanAccessNetworkType);
            MobileSignalController.this.mServiceState = serviceState;
            MobileSignalController.this.updateTelephony();
        }

        @Override // android.telephony.PhoneStateListener
        public void onDataConnectionStateChanged(int i, int i2) {
            if (SignalController.DEBUG) {
                String str = MobileSignalController.this.mTag;
                StringBuilder sb = new StringBuilder();
                sb.append("onDataConnectionStateChanged: state=");
                sb.append(i);
                sb.append(" type=");
                sb.append(i2);
                sb.append(" isUsingCarrierAggregation:");
                sb.append(MobileSignalController.this.mServiceState != null ? MobileSignalController.this.mServiceState.isUsingCarrierAggregation() : false);
                Log.d(str, sb.toString());
            }
            MobileSignalController.this.mDataState = i;
            MobileSignalController.this.updateTelephony();
        }

        @Override // android.telephony.PhoneStateListener
        public void onDataActivity(int i) {
            if (SignalController.DEBUG) {
                String str = MobileSignalController.this.mTag;
                Log.d(str, "onDataActivity: direction=" + i);
            }
            MobileSignalController.this.setActivity(i);
        }

        public void onCarrierNetworkChange(boolean z) {
            if (SignalController.DEBUG) {
                String str = MobileSignalController.this.mTag;
                Log.d(str, "onCarrierNetworkChange: active=" + z);
            }
            MobileSignalController mobileSignalController = MobileSignalController.this;
            ((MobileState) mobileSignalController.mCurrentState).carrierNetworkChangeMode = z;
            mobileSignalController.updateTelephony();
        }

        @Override // android.telephony.PhoneStateListener
        public void onActiveDataSubscriptionIdChanged(int i) {
            if (SignalController.DEBUG) {
                String str = MobileSignalController.this.mTag;
                Log.d(str, "onActiveDataSubscriptionIdChanged: subId=" + i);
            }
            MobileSignalController.this.updateDataSim();
            MobileSignalController.this.updateTelephony();
        }

        @Override // android.telephony.PhoneStateListener
        public void onDisplayInfoChanged(TelephonyDisplayInfo telephonyDisplayInfo) {
            if (SignalController.DEBUG) {
                String str = MobileSignalController.this.mTag;
                Log.d(str, "onDisplayInfoChanged: telephonyDisplayInfo=" + telephonyDisplayInfo);
            }
            MobileSignalController.this.mTelephonyDisplayInfo = telephonyDisplayInfo;
            MobileSignalController.this.updateTelephony();
        }

        @Override // android.telephony.PhoneStateListener
        public void onCallStateChanged(int i, String str) {
            if (SignalController.DEBUG) {
                String str2 = MobileSignalController.this.mTag;
                Log.d(str2, "onCallStateChanged: state=" + i);
            }
            MobileSignalController.this.mCallState = i;
            MobileSignalController.this.updateTelephony();
        }
    }

    /* access modifiers changed from: package-private */
    public class FiveGStateListener implements FiveGServiceClient.IFiveGStateListener {
        FiveGStateListener() {
        }

        @Override // com.android.systemui.statusbar.policy.FiveGServiceClient.IFiveGStateListener
        public void onStateChanged(FiveGServiceClient.FiveGServiceState fiveGServiceState) {
            String str = MobileSignalController.this.mTag;
            Log.d(str, "onStateChanged: state=" + fiveGServiceState);
            MobileSignalController mobileSignalController = MobileSignalController.this;
            mobileSignalController.mFiveGState = fiveGServiceState;
            mobileSignalController.updateTelephony();
            MobileSignalController.this.notifyListeners();
        }
    }

    /* access modifiers changed from: package-private */
    public static class MobileIconGroup extends SignalController.IconGroup {
        final int mDataContentDescription;
        final int mDataType;
        final boolean mIsWide;
        final int mQsDataType;
        final int mSingleSignalIcon;
        final int[] mStackedDataIcon;
        final int[] mStackedVoiceIcon;

        public MobileIconGroup(String str, int[][] iArr, int[][] iArr2, int[] iArr3, int i, int i2, int i3, int i4, int i5, int i6, int i7, boolean z) {
            this(str, iArr, iArr2, iArr3, i, i2, i3, i4, i5, i6, i7, z, 0, new int[2], new int[2], 0);
        }

        public MobileIconGroup(String str, int[][] iArr, int[][] iArr2, int[] iArr3, int i, int i2, int i3, int i4, int i5, int i6, int i7, boolean z, int i8, int[] iArr4, int[] iArr5, int i9) {
            super(str, iArr, iArr2, iArr3, i, i2, i3, i4, i5);
            this.mDataContentDescription = i6;
            this.mDataType = i7;
            this.mIsWide = z;
            this.mQsDataType = i7;
            this.mSingleSignalIcon = i8;
            this.mStackedDataIcon = iArr4;
            this.mStackedVoiceIcon = iArr5;
        }

        public MobileIconGroup(MobileIconGroup mobileIconGroup, int i, int i2, int i3, int i4, boolean z, boolean z2) {
            this(mobileIconGroup.mName, mobileIconGroup.mSbIcons, mobileIconGroup.mQsIcons, mobileIconGroup.mContentDesc, mobileIconGroup.mSbNullState, mobileIconGroup.mQsNullState, mobileIconGroup.mSbDiscState, mobileIconGroup.mQsDiscState, mobileIconGroup.mDiscContentDesc, mobileIconGroup.mDataContentDescription, mobileIconGroup.mDataType, mobileIconGroup.mIsWide, 0, TelephonyIcons.getStackedDataIcon(i, i4, z2), TelephonyIcons.getStackedVoiceIcon(i2, i3, z, z2), 0);
        }
    }

    /* access modifiers changed from: package-private */
    public static class MobileState extends SignalController.State {
        boolean airplaneMode;
        boolean carrierNetworkChangeMode;
        boolean dataConnected;
        boolean dataSim;
        boolean defaultDataOff;
        boolean fakePSIcon;
        boolean imsRegistered;
        boolean isDefault;
        boolean isDefaultDataSubId;
        boolean isDemoMode;
        boolean isEmergency;
        boolean isVirtual;
        boolean isWifiConnected;
        boolean mobileDataEnabled;
        String networkName;
        String networkNameData;
        boolean roaming;
        boolean roamingDataEnabled;
        String simstate;
        boolean smartlinkEnable;
        boolean userSetup;
        boolean videoCapable;
        boolean voiceCapable;

        MobileState() {
        }

        @Override // com.android.systemui.statusbar.policy.SignalController.State
        public void copyFrom(SignalController.State state) {
            super.copyFrom(state);
            MobileState mobileState = (MobileState) state;
            this.dataSim = mobileState.dataSim;
            this.networkName = mobileState.networkName;
            this.networkNameData = mobileState.networkNameData;
            this.dataConnected = mobileState.dataConnected;
            this.isDefault = mobileState.isDefault;
            this.isEmergency = mobileState.isEmergency;
            this.airplaneMode = mobileState.airplaneMode;
            this.carrierNetworkChangeMode = mobileState.carrierNetworkChangeMode;
            this.userSetup = mobileState.userSetup;
            this.roaming = mobileState.roaming;
            this.defaultDataOff = mobileState.defaultDataOff;
            this.imsRegistered = mobileState.imsRegistered;
            this.voiceCapable = mobileState.voiceCapable;
            this.videoCapable = mobileState.videoCapable;
            this.mobileDataEnabled = mobileState.mobileDataEnabled;
            this.roamingDataEnabled = mobileState.roamingDataEnabled;
            this.simstate = mobileState.simstate;
            this.isDefaultDataSubId = mobileState.isDefaultDataSubId;
            this.isVirtual = mobileState.isVirtual;
            this.isDemoMode = mobileState.isDemoMode;
            this.isWifiConnected = mobileState.isWifiConnected;
            this.smartlinkEnable = mobileState.smartlinkEnable;
            this.fakePSIcon = mobileState.fakePSIcon;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.systemui.statusbar.policy.SignalController.State
        public void toString(StringBuilder sb) {
            super.toString(sb);
            sb.append(',');
            sb.append("dataSim=");
            sb.append(this.dataSim);
            sb.append(',');
            sb.append("networkName=");
            sb.append(this.networkName);
            sb.append(',');
            sb.append("networkNameData=");
            sb.append(this.networkNameData);
            sb.append(',');
            sb.append("dataConnected=");
            sb.append(this.dataConnected);
            sb.append(',');
            sb.append("roaming=");
            sb.append(this.roaming);
            sb.append(',');
            sb.append("isDefault=");
            sb.append(this.isDefault);
            sb.append(',');
            sb.append("isEmergency=");
            sb.append(this.isEmergency);
            sb.append(',');
            sb.append("airplaneMode=");
            sb.append(this.airplaneMode);
            sb.append(',');
            sb.append("carrierNetworkChangeMode=");
            sb.append(this.carrierNetworkChangeMode);
            sb.append(',');
            sb.append("userSetup=");
            sb.append(this.userSetup);
            sb.append(',');
            sb.append("defaultDataOff=");
            sb.append(this.defaultDataOff);
            sb.append("imsRegistered=");
            sb.append(this.imsRegistered);
            sb.append(',');
            sb.append("voiceCapable=");
            sb.append(this.voiceCapable);
            sb.append(',');
            sb.append("videoCapable=");
            sb.append(this.videoCapable);
            sb.append(',');
            sb.append("mobileDataEnabled=");
            sb.append(this.mobileDataEnabled);
            sb.append(',');
            sb.append("roamingDataEnabled=");
            sb.append(this.roamingDataEnabled);
            sb.append(',');
            sb.append("simstate=");
            sb.append(this.simstate);
            sb.append(',');
            sb.append("isDefaultDataSubId=");
            sb.append(this.isDefaultDataSubId);
            sb.append(',');
            sb.append("isVirtual");
            sb.append(this.isVirtual);
            sb.append("isDemoMode");
            sb.append(this.isDemoMode);
            sb.append("isWifiConnected");
            sb.append(this.isWifiConnected);
            sb.append("smartlinkEnable");
            sb.append(this.smartlinkEnable);
            sb.append("fakePSIcon");
            sb.append(this.fakePSIcon);
        }

        @Override // com.android.systemui.statusbar.policy.SignalController.State
        public boolean equals(Object obj) {
            if (super.equals(obj)) {
                MobileState mobileState = (MobileState) obj;
                if (Objects.equals(mobileState.networkName, this.networkName) && Objects.equals(mobileState.networkNameData, this.networkNameData) && mobileState.dataSim == this.dataSim && mobileState.dataConnected == this.dataConnected && mobileState.isEmergency == this.isEmergency && mobileState.airplaneMode == this.airplaneMode && mobileState.carrierNetworkChangeMode == this.carrierNetworkChangeMode && mobileState.userSetup == this.userSetup && mobileState.isDefault == this.isDefault && mobileState.roaming == this.roaming && mobileState.defaultDataOff == this.defaultDataOff && mobileState.imsRegistered == this.imsRegistered && mobileState.voiceCapable == this.voiceCapable && mobileState.videoCapable == this.videoCapable && mobileState.mobileDataEnabled == this.mobileDataEnabled && mobileState.roamingDataEnabled == this.roamingDataEnabled && mobileState.simstate == this.simstate && mobileState.isDefaultDataSubId == this.isDefaultDataSubId && mobileState.isVirtual == this.isVirtual && mobileState.isDemoMode == this.isDemoMode && mobileState.isWifiConnected == this.isWifiConnected && mobileState.smartlinkEnable == this.smartlinkEnable && mobileState.fakePSIcon == this.fakePSIcon) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean[] getLTEStatus() {
        return this.mLTEStatus;
    }

    private void cleanLTEStatus() {
        this.mLTEStatus = LTE_DEFAULT_STATUS;
        Log.i(this.mTag, "cleanLTEStatus");
    }

    public int getSimSlotIndex() {
        SubscriptionInfo subscriptionInfo = this.mSubscriptionInfo;
        int simSlotIndex = subscriptionInfo != null ? subscriptionInfo.getSimSlotIndex() : -1;
        if (SignalController.DEBUG) {
            String str = this.mTag;
            Log.d(str, "getSimSlotIndex, slotId: " + simSlotIndex);
        }
        return simSlotIndex;
    }

    public void setVirtualSimstate(int[] iArr) {
        int simSlotIndex = getSimSlotIndex();
        if (simSlotIndex < iArr.length && simSlotIndex >= 0) {
            ((MobileState) this.mCurrentState).isVirtual = iArr[simSlotIndex] != NetworkControllerImpl.SOFTSIM_DISABLE;
            notifyListenersIfNecessary();
        }
    }

    private boolean isVirtualSim() {
        return ((MobileState) this.mCurrentState).isVirtual && isInService(this.mServiceState) && ((MobileState) this.mCurrentState).simstate != "ABSENT";
    }

    private int getVoiceSignalLevel() {
        if (this.mSignalStrength == null) {
            return 0;
        }
        boolean showStacked = showStacked();
        SignalStrength signalStrength = this.mSignalStrength;
        int level = showStacked ? signalStrength.getSmoothSignalLevelAll()[0] : signalStrength.getLevel();
        if (OpUtils.isSupportFiveBar() || level <= 4) {
            return level;
        }
        return 4;
    }

    public int getDefaultDataSubId() {
        int defaultDataSubId = this.mDefaults.getDefaultDataSubId();
        if (!SubscriptionManager.isValidSubscriptionId(defaultDataSubId)) {
            return Integer.MAX_VALUE;
        }
        return defaultDataSubId;
    }

    public int getSubId() {
        SubscriptionInfo subscriptionInfo = this.mSubscriptionInfo;
        int subscriptionId = subscriptionInfo != null ? subscriptionInfo.getSubscriptionId() : -1;
        if (SignalController.DEBUG) {
            String str = this.mTag;
            Log.d(str, "getSubId, subId: " + subscriptionId);
        }
        return subscriptionId;
    }

    public boolean showLTE() {
        if (this.mPhone == null) {
            return false;
        }
        String str = this.mMccmnc;
        int i = 0;
        while (true) {
            String[] strArr = SHOW_LTE_OPERATORS;
            if (i >= strArr.length) {
                return false;
            }
            if (str.equals(strArr[i])) {
                return true;
            }
            i++;
        }
    }

    private boolean isBouygues() {
        if (this.mPhone != null && TextUtils.equals(this.mMccmnc, "20820")) {
            return true;
        }
        return false;
    }

    private boolean isEESim() {
        if (this.mPhone == null) {
            return false;
        }
        String str = this.mMccmnc;
        if (TextUtils.equals(str, "23430") || TextUtils.equals(str, "23433")) {
            return true;
        }
        return false;
    }

    private boolean isTurKeySim() {
        if (this.mPhone == null) {
            return false;
        }
        String str = this.mMccmnc;
        if (TextUtils.equals(str, "28601") || TextUtils.equals(str, "28602") || TextUtils.equals(str, "28603")) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void customizeIconsMap() {
        TelephonyManager telephonyManager = this.mPhone;
        if (telephonyManager != null) {
            this.mMccmnc = telephonyManager.getSimOperatorNumericForPhone(getSimSlotIndex());
            boolean z = false;
            boolean z2 = OpUtils.isUST() || OpUtils.isUSS() || ProductUtils.isUsvMode();
            if (showLTE() || isBouygues() || isEESim() || isTurKeySim()) {
                z = true;
            }
            Log.d(this.mTag, " customizeIconsMap Mccmnc =" + this.mMccmnc + " CustomizeForCarrierCard=" + z);
            mapIconSets();
            if (z) {
                if (showLTE()) {
                    this.mNetworkToIconLookup.put(toIconKey(13), TelephonyIcons.LTE);
                    this.mNetworkToIconLookup.put(toIconKey(19), TelephonyIcons.LTE_PLUS);
                    this.mNetworkToIconLookup.put(toDisplayIconKey(1), TelephonyIcons.LTE_PLUS);
                } else if (isBouygues()) {
                    this.mNetworkToIconLookup.put(toIconKey(3), TelephonyIcons.THREE_G);
                    this.mNetworkToIconLookup.put(toIconKey(1), TelephonyIcons.G);
                    this.mNetworkToIconLookup.put(toIconKey(2), TelephonyIcons.E);
                    this.mNetworkToIconLookup.put(toIconKey(9), TelephonyIcons.THREE_G_PLUS);
                    this.mNetworkToIconLookup.put(toIconKey(8), TelephonyIcons.THREE_G_PLUS);
                    this.mNetworkToIconLookup.put(toIconKey(10), TelephonyIcons.H_PLUS);
                    this.mNetworkToIconLookup.put(toIconKey(15), TelephonyIcons.H_PLUS);
                    this.mNetworkToIconLookup.put(toIconKey(13), TelephonyIcons.FOUR_G);
                    this.mNetworkToIconLookup.put(toIconKey(19), TelephonyIcons.FOUR_G_PLUS);
                    this.mNetworkToIconLookup.put(toDisplayIconKey(1), TelephonyIcons.FOUR_G_PLUS);
                } else if (isEESim()) {
                    this.mNetworkToIconLookup.put(toIconKey(19), TelephonyIcons.FOUR_G);
                    this.mNetworkToIconLookup.put(toDisplayIconKey(1), TelephonyIcons.FOUR_G);
                }
                if (isTurKeySim()) {
                    this.mNetworkToIconLookup.put(toIconKey(13), TelephonyIcons.FOUR_FIVE_G);
                    this.mNetworkToIconLookup.put(toIconKey(19), TelephonyIcons.FOUR_FIVE_G);
                }
            }
            if (z2) {
                if (OpUtils.isUST()) {
                    if (((MobileState) this.mCurrentState).roaming) {
                        this.mNetworkToIconLookup.put(toIconKey(15), TelephonyIcons.THREE_G);
                    } else {
                        this.mNetworkToIconLookup.put(toIconKey(15), TelephonyIcons.FOUR_G);
                    }
                    this.mNetworkToIconLookup.put(toIconKey(10), TelephonyIcons.THREE_G);
                    this.mNetworkToIconLookup.put(toIconKey(1), TelephonyIcons.G);
                    this.mNetworkToIconLookup.put(toIconKey(2), TelephonyIcons.TWO_G);
                }
                if (OpUtils.isSupportShow4GLTE()) {
                    this.mNetworkToIconLookup.put(toIconKey(13), TelephonyIcons.FOUR_G_LTE);
                    this.mNetworkToIconLookup.put(toIconKey(19), TelephonyIcons.FOUR_G_LTE);
                    this.mNetworkToIconLookup.put(toDisplayIconKey(1), TelephonyIcons.FOUR_G_LTE);
                }
                if (OpUtils.isUSS()) {
                    this.mNetworkToIconLookup.put(toIconKey(5), TelephonyIcons.THREE_G);
                    this.mNetworkToIconLookup.put(toIconKey(6), TelephonyIcons.THREE_G);
                    this.mNetworkToIconLookup.put(toIconKey(12), TelephonyIcons.THREE_G);
                    this.mNetworkToIconLookup.put(toIconKey(14), TelephonyIcons.THREE_G);
                    this.mNetworkToIconLookup.put(toIconKey(3), TelephonyIcons.THREE_G);
                    this.mNetworkToIconLookup.put(toIconKey(17), TelephonyIcons.THREE_G);
                    this.mNetworkToIconLookup.put(toIconKey(4), TelephonyIcons.THREE_G);
                    this.mNetworkToIconLookup.put(toIconKey(7), TelephonyIcons.THREE_G);
                    this.mNetworkToIconLookup.put(toIconKey(13), TelephonyIcons.LTE);
                    this.mNetworkToIconLookup.put(toIconKey(19), TelephonyIcons.LTE);
                    this.mNetworkToIconLookup.put(toDisplayIconKey(1), TelephonyIcons.LTE);
                }
                if (ProductUtils.isUsvMode()) {
                    this.mNetworkToIconLookup.put(toIconKey(3), TelephonyIcons.THREE_G);
                    this.mNetworkToIconLookup.put(toIconKey(5), TelephonyIcons.THREE_G);
                    this.mNetworkToIconLookup.put(toIconKey(6), TelephonyIcons.THREE_G);
                    this.mNetworkToIconLookup.put(toIconKey(8), TelephonyIcons.THREE_G);
                    this.mNetworkToIconLookup.put(toIconKey(9), TelephonyIcons.THREE_G);
                    this.mNetworkToIconLookup.put(toIconKey(10), TelephonyIcons.THREE_G);
                    this.mNetworkToIconLookup.put(toIconKey(12), TelephonyIcons.THREE_G);
                    this.mNetworkToIconLookup.put(toIconKey(14), TelephonyIcons.THREE_G);
                    this.mNetworkToIconLookup.put(toIconKey(15), TelephonyIcons.THREE_G);
                    this.mNetworkToIconLookup.put(toIconKey(17), TelephonyIcons.THREE_G);
                    this.mNetworkToIconLookup.put(toIconKey(18), TelephonyIcons.FOUR_G_LTE);
                }
                if (isTurKeySim()) {
                    this.mNetworkToIconLookup.put(toIconKey(13), TelephonyIcons.FOUR_FIVE_G);
                    this.mNetworkToIconLookup.put(toIconKey(19), TelephonyIcons.FOUR_FIVE_G);
                }
            }
        }
    }

    private boolean showStacked() {
        return getDataNetworkType() != 0 && isCdma() && !isDataDisabled() && !OpUtils.isUSS() && !ProductUtils.isUsvMode();
    }

    private boolean showIdleDataIcon() {
        return showDataIconForVzw() && !showDataConnectedIconForVzw() && !isDataDisabled();
    }

    private int getIdleDataIcon() {
        long bitMaskForNetworkType = TelephonyManager.getBitMaskForNetworkType(getDataNetworkType());
        if (is5GConnected()) {
            bitMaskForNetworkType = 524288;
        }
        String str = this.mTag;
        Log.d(str, "getIdleDataIcon: " + bitMaskForNetworkType);
        return OpSignalIcons.getIdleDataIcon(bitMaskForNetworkType);
    }

    private boolean showDataIconForVzw() {
        return isInService(this.mServiceState) && getDataNetworkType() != 0;
    }

    private boolean showDataConnectedIconForVzw() {
        if (showDataIconForVzw() && !isDataDisabled()) {
            T t = this.mCurrentState;
            if (((MobileState) t).isDefault || (((MobileState) t).isWifiConnected && ((MobileState) t).dataConnected)) {
                return true;
            }
        }
        return false;
    }

    private int getDisableDataIcon() {
        long bitMaskForNetworkType = TelephonyManager.getBitMaskForNetworkType(getDataNetworkType());
        if (is5GConnected()) {
            bitMaskForNetworkType = 524288;
        }
        String str = this.mTag;
        Log.d(str, "getDisableDataIcon: " + bitMaskForNetworkType);
        return OpSignalIcons.getDisableDataIcon(bitMaskForNetworkType);
    }

    public void setSmartlinkEnable(boolean z) {
        ((MobileState) this.mCurrentState).smartlinkEnable = z;
        if (SignalController.DEBUG) {
            String str = this.mTag;
            Log.d(str, "setSmartLinkEnable: smartlinkEnable=" + z + " type=" + getDataNetworkType());
        }
        updateTelephony();
    }

    public void setFakePSIconEnable(boolean z) {
        ((MobileState) this.mCurrentState).fakePSIcon = z;
        if (SignalController.DEBUG) {
            String str = this.mTag;
            Log.d(str, "setFakePSIconEnable: fakePSIcon=" + z + " type=" + getDataNetworkType());
        }
        updateTelephony();
    }
}
