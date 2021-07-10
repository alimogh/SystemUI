package com.android.keyguard;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.keyguard.CarrierTextController;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.statusbar.policy.FiveGServiceClient;
import com.oneplus.keyguard.OpCarrierTextController;
import com.oneplus.util.OpUtils;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
public class CarrierTextController extends OpCarrierTextController {
    private static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    private int mActiveMobileDataSubscription;
    private final Handler mBgHandler;
    protected final KeyguardUpdateMonitorCallback mCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.keyguard.CarrierTextController.2
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onRefreshCarrierInfo() {
            if (CarrierTextController.DEBUG) {
                Log.d("CarrierTextController", "onRefreshCarrierInfo(), mTelephonyCapable: " + Boolean.toString(CarrierTextController.this.mTelephonyCapable));
            }
            CarrierTextController.this.updateCarrierText();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onTelephonyCapable(boolean z) {
            if (CarrierTextController.DEBUG) {
                Log.d("CarrierTextController", "onTelephonyCapable() mTelephonyCapable: " + Boolean.toString(z));
            }
            CarrierTextController.this.mTelephonyCapable = z;
            CarrierTextController.this.updateCarrierText();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onSimStateChanged(int i, int i2, int i3) {
            if (i2 < 0 || i2 >= CarrierTextController.this.mSimSlotsNumber) {
                Log.d("CarrierTextController", "onSimStateChanged() - slotId invalid: " + i2 + " mTelephonyCapable: " + Boolean.toString(CarrierTextController.this.mTelephonyCapable));
                return;
            }
            if (CarrierTextController.DEBUG) {
                Log.d("CarrierTextController", "onSimStateChanged: " + CarrierTextController.this.getStatusForIccState(i3));
            }
            if (CarrierTextController.this.getStatusForIccState(i3) == StatusMode.SimIoError) {
                CarrierTextController.this.mSimErrorState[i2] = true;
                CarrierTextController.this.updateCarrierText();
            } else if (CarrierTextController.this.mSimErrorState[i2]) {
                CarrierTextController.this.mSimErrorState[i2] = false;
                CarrierTextController.this.updateCarrierText();
            }
        }
    };
    private CarrierTextCallback mCarrierTextCallback;
    private Context mContext;
    private FiveGServiceClient mFiveGServiceClient;
    protected KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final Handler mMainHandler;
    private final AtomicBoolean mNetworkSupported = new AtomicBoolean();
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() { // from class: com.android.keyguard.CarrierTextController.3
        @Override // android.telephony.PhoneStateListener
        public void onActiveDataSubscriptionIdChanged(int i) {
            CarrierTextController.this.mActiveMobileDataSubscription = i;
            if (CarrierTextController.this.mNetworkSupported.get() && CarrierTextController.this.mCarrierTextCallback != null) {
                CarrierTextController.this.updateCarrierText();
            }
        }
    };
    private CharSequence mSeparator;
    private boolean mShowAirplaneMode;
    private boolean mShowMissingSim;
    private boolean[] mSimErrorState;
    private final int mSimSlotsNumber;
    private boolean mTelephonyCapable;
    private WakefulnessLifecycle mWakefulnessLifecycle;
    private final WakefulnessLifecycle.Observer mWakefulnessObserver = new WakefulnessLifecycle.Observer() { // from class: com.android.keyguard.CarrierTextController.1
        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onFinishedWakingUp() {
            CarrierTextCallback carrierTextCallback = CarrierTextController.this.mCarrierTextCallback;
            if (carrierTextCallback != null) {
                carrierTextCallback.finishedWakingUp();
            }
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onStartedGoingToSleep() {
            CarrierTextCallback carrierTextCallback = CarrierTextController.this.mCarrierTextCallback;
            if (carrierTextCallback != null) {
                carrierTextCallback.startedGoingToSleep();
            }
        }
    };
    private WifiManager mWifiManager;

    public interface CarrierTextCallback {
        default void finishedWakingUp() {
        }

        default void startedGoingToSleep() {
        }

        default void updateCarrierInfo(CarrierTextCallbackInfo carrierTextCallbackInfo) {
        }
    }

    public enum StatusMode {
        Normal,
        NetworkLocked,
        SimMissing,
        SimMissingLocked,
        SimPukLocked,
        SimLocked,
        SimPermDisabled,
        SimNotReady,
        SimIoError,
        SimUnknown
    }

    public CarrierTextController(Context context, CharSequence charSequence, boolean z, boolean z2) {
        super(context, charSequence, z, z2);
        this.mContext = context;
        getTelephonyManager().isVoiceCapable();
        this.mShowAirplaneMode = z;
        this.mShowMissingSim = z2;
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mSeparator = charSequence;
        this.mWakefulnessLifecycle = (WakefulnessLifecycle) Dependency.get(WakefulnessLifecycle.class);
        int simCount = OpUtils.getSimCount();
        this.mSimSlotsNumber = simCount;
        this.mSimErrorState = new boolean[simCount];
        this.mMainHandler = (Handler) Dependency.get(Dependency.MAIN_HANDLER);
        this.mBgHandler = new Handler((Looper) Dependency.get(Dependency.BG_LOOPER));
        this.mKeyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mBgHandler.post(new Runnable() { // from class: com.android.keyguard.-$$Lambda$CarrierTextController$LDahO04Jhi3ephJWS_by3x0LKJY
            @Override // java.lang.Runnable
            public final void run() {
                CarrierTextController.this.lambda$new$0$CarrierTextController();
            }
        });
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$new$0 */
    private /* synthetic */ void lambda$new$0$CarrierTextController() {
        boolean isNetworkSupported = ConnectivityManager.from(this.mContext).isNetworkSupported(0);
        if (isNetworkSupported && this.mNetworkSupported.compareAndSet(false, isNetworkSupported)) {
            lambda$setListening$4(this.mCarrierTextCallback);
        }
    }

    private TelephonyManager getTelephonyManager() {
        return (TelephonyManager) this.mContext.getSystemService("phone");
    }

    private CharSequence updateCarrierTextWithSimIoError(CharSequence charSequence, CharSequence[] charSequenceArr, int[] iArr, boolean z) {
        CharSequence carrierTextForSimState = getCarrierTextForSimState(8, "");
        for (int i = 0; i < getTelephonyManager().getActiveModemCount(); i++) {
            if (this.mSimErrorState[i]) {
                if (z) {
                    return concatenate(carrierTextForSimState, getContext().getText(17040098), this.mSeparator);
                }
                if (iArr[i] != -1) {
                    int i2 = iArr[i];
                    charSequenceArr[i2] = concatenate(carrierTextForSimState, charSequenceArr[i2], this.mSeparator);
                } else {
                    charSequence = concatenate(charSequence, carrierTextForSimState, this.mSeparator);
                }
            }
        }
        return charSequence;
    }

    /* access modifiers changed from: public */
    /* renamed from: handleSetListening */
    private void lambda$setListening$4(CarrierTextCallback carrierTextCallback) {
        TelephonyManager telephonyManager = getTelephonyManager();
        if (carrierTextCallback != null) {
            this.mCarrierTextCallback = carrierTextCallback;
            if (this.mNetworkSupported.get()) {
                this.mMainHandler.post(new Runnable() { // from class: com.android.keyguard.-$$Lambda$CarrierTextController$Q9wwkxQ080xCMh0FoQlLdgAfhSI
                    @Override // java.lang.Runnable
                    public final void run() {
                        CarrierTextController.this.lambda$handleSetListening$1$CarrierTextController();
                    }
                });
                this.mWakefulnessLifecycle.addObserver(this.mWakefulnessObserver);
                telephonyManager.listen(this.mPhoneStateListener, 4194304);
                return;
            }
            this.mMainHandler.post(new Runnable() { // from class: com.android.keyguard.-$$Lambda$CarrierTextController$oi57qYsuU97hQX_k3GdwhheueLc
                @Override // java.lang.Runnable
                public final void run() {
                    CarrierTextController.lambda$handleSetListening$2(CarrierTextController.CarrierTextCallback.this);
                }
            });
            return;
        }
        this.mCarrierTextCallback = null;
        this.mMainHandler.post(new Runnable() { // from class: com.android.keyguard.-$$Lambda$CarrierTextController$DU9JkyLlu7BQSyhrdUqIiohEMiE
            @Override // java.lang.Runnable
            public final void run() {
                CarrierTextController.this.lambda$handleSetListening$3$CarrierTextController();
            }
        });
        this.mWakefulnessLifecycle.removeObserver(this.mWakefulnessObserver);
        telephonyManager.listen(this.mPhoneStateListener, 0);
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$handleSetListening$1 */
    private /* synthetic */ void lambda$handleSetListening$1$CarrierTextController() {
        this.mKeyguardUpdateMonitor.registerCallback(this.mCallback);
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$handleSetListening$3 */
    private /* synthetic */ void lambda$handleSetListening$3$CarrierTextController() {
        this.mKeyguardUpdateMonitor.removeCallback(this.mCallback);
    }

    public void setListening(CarrierTextCallback carrierTextCallback) {
        this.mBgHandler.post(new Runnable(carrierTextCallback) { // from class: com.android.keyguard.-$$Lambda$CarrierTextController$C6go0e-jA3BYgQhMyia20ELR8OQ
            public final /* synthetic */ CarrierTextController.CarrierTextCallback f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                CarrierTextController.this.lambda$setListening$4$CarrierTextController(this.f$1);
            }
        });
    }

    public List<SubscriptionInfo> getSubscriptionInfo() {
        return this.mKeyguardUpdateMonitor.getFilteredSubscriptionInfo(false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:60:0x01b7  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateCarrierText() {
        /*
        // Method dump skipped, instructions count: 481
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.CarrierTextController.updateCarrierText():void");
    }

    public void postToCallback(CarrierTextCallbackInfo carrierTextCallbackInfo) {
        CarrierTextCallback carrierTextCallback = this.mCarrierTextCallback;
        if (carrierTextCallback != null) {
            this.mMainHandler.post(new Runnable(carrierTextCallbackInfo) { // from class: com.android.keyguard.-$$Lambda$CarrierTextController$5nEYjT7ZyAro48Qi-bsR2yyNFwM
                public final /* synthetic */ CarrierTextController.CarrierTextCallbackInfo f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    CarrierTextController.lambda$postToCallback$5(CarrierTextController.CarrierTextCallback.this, this.f$1);
                }
            });
        }
    }

    private Context getContext() {
        return this.mContext;
    }

    private String getMissingSimMessage() {
        return (!this.mShowMissingSim || !this.mTelephonyCapable) ? "" : getContext().getString(C0015R$string.keyguard_missing_sim_message_short);
    }

    private String getAirplaneModeMessage() {
        return this.mShowAirplaneMode ? getContext().getString(C0015R$string.airplane_mode) : "";
    }

    /* renamed from: com.android.keyguard.CarrierTextController$4 */
    public static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode;

        static {
            int[] iArr = new int[StatusMode.values().length];
            $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode = iArr;
            try {
                iArr[StatusMode.Normal.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode[StatusMode.SimNotReady.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode[StatusMode.NetworkLocked.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode[StatusMode.SimMissing.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode[StatusMode.SimPermDisabled.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode[StatusMode.SimMissingLocked.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode[StatusMode.SimLocked.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode[StatusMode.SimPukLocked.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode[StatusMode.SimIoError.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$com$android$keyguard$CarrierTextController$StatusMode[StatusMode.SimUnknown.ordinal()] = 10;
            } catch (NoSuchFieldError unused10) {
            }
        }
    }

    private CharSequence getCarrierTextForSimState(int i, CharSequence charSequence) {
        switch (AnonymousClass4.$SwitchMap$com$android$keyguard$CarrierTextController$StatusMode[getStatusForIccState(i).ordinal()]) {
            case 1:
                return charSequence;
            case 2:
                return "";
            case 3:
                return makeCarrierStringOnEmergencyCapable(this.mContext.getText(C0015R$string.keyguard_network_locked_message), charSequence);
            case 4:
            case 6:
            case 10:
            default:
                return null;
            case 5:
                return makeCarrierStringOnEmergencyCapable(getContext().getText(C0015R$string.keyguard_permanent_disabled_sim_message_short), charSequence);
            case 7:
                return makeCarrierStringOnLocked(getContext().getText(C0015R$string.keyguard_sim_locked_message), charSequence);
            case 8:
                return makeCarrierStringOnLocked(getContext().getText(C0015R$string.keyguard_sim_puk_locked_message), charSequence);
            case 9:
                return makeCarrierStringOnEmergencyCapable(getContext().getText(C0015R$string.keyguard_sim_error_message_short), charSequence);
        }
    }

    private CharSequence makeCarrierStringOnEmergencyCapable(CharSequence charSequence, CharSequence charSequence2) {
        return opMakeCarrierStringOnEmergencyCapable(charSequence, charSequence2);
    }

    private CharSequence makeCarrierStringOnLocked(CharSequence charSequence, CharSequence charSequence2) {
        boolean z = !TextUtils.isEmpty(charSequence);
        boolean z2 = !TextUtils.isEmpty(charSequence2);
        if (z && z2) {
            return this.mContext.getString(C0015R$string.keyguard_carrier_name_with_sim_locked_template, charSequence2, charSequence);
        }
        if (z) {
            return charSequence;
        }
        return z2 ? charSequence2 : "";
    }

    private StatusMode getStatusForIccState(int i) {
        boolean z = true;
        if (this.mKeyguardUpdateMonitor.isDeviceProvisioned() || !(i == 1 || i == 7)) {
            z = false;
        }
        if (z) {
            i = 4;
        }
        switch (i) {
            case 0:
                return StatusMode.SimUnknown;
            case 1:
                return StatusMode.SimMissing;
            case 2:
                return StatusMode.SimLocked;
            case 3:
                return StatusMode.SimPukLocked;
            case 4:
                return StatusMode.SimMissingLocked;
            case 5:
                return StatusMode.Normal;
            case 6:
                return StatusMode.SimNotReady;
            case 7:
                return StatusMode.SimPermDisabled;
            case 8:
                return StatusMode.SimIoError;
            default:
                return StatusMode.SimUnknown;
        }
    }

    private static CharSequence concatenate(CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3) {
        return OpCarrierTextController.opConcatenate(charSequence, charSequence2, charSequence3);
    }

    private static CharSequence joinNotEmpty(CharSequence charSequence, CharSequence[] charSequenceArr) {
        int length = charSequenceArr.length;
        if (length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (!TextUtils.isEmpty(charSequenceArr[i])) {
                if (!TextUtils.isEmpty(sb)) {
                    sb.append(charSequence);
                }
                sb.append(charSequenceArr[i]);
            }
        }
        return sb.toString();
    }

    public static class Builder {
        public Builder(Context context, Resources resources) {
            resources.getString(17040424);
        }
    }

    public static final class CarrierTextCallbackInfo {
        public boolean airplaneMode;
        public final CharSequence carrierText;
        public final CharSequence[] listOfCarriers;

        public CarrierTextCallbackInfo(CharSequence charSequence, CharSequence[] charSequenceArr, boolean z, int[] iArr) {
            this(charSequence, charSequenceArr, z, iArr, false);
        }

        public CarrierTextCallbackInfo(CharSequence charSequence, CharSequence[] charSequenceArr, boolean z, int[] iArr, boolean z2) {
            this.carrierText = charSequence;
            this.listOfCarriers = charSequenceArr;
            this.airplaneMode = z2;
        }
    }

    private String getCustomizeCarrierName(CharSequence charSequence, SubscriptionInfo subscriptionInfo) {
        int networkType = getNetworkType(subscriptionInfo.getSubscriptionId());
        networkTypeToString(networkType);
        get5GNetworkClass(subscriptionInfo, networkType);
        return opGetCustomizeCarrierName(charSequence, subscriptionInfo);
    }

    private int getNetworkType(int i) {
        ServiceState serviceState = this.mKeyguardUpdateMonitor.mServiceStates.get(Integer.valueOf(i));
        if (serviceState == null || (serviceState.getDataRegState() != 0 && serviceState.getVoiceRegState() != 0)) {
            return 0;
        }
        int dataNetworkType = serviceState.getDataNetworkType();
        return dataNetworkType == 0 ? serviceState.getVoiceNetworkType() : dataNetworkType;
    }

    private String networkTypeToString(int i) {
        int i2 = C0015R$string.config_rat_unknown;
        long bitMaskForNetworkType = TelephonyManager.getBitMaskForNetworkType(i);
        if ((32843 & bitMaskForNetworkType) != 0) {
            i2 = C0015R$string.config_rat_2g;
        } else if ((93108 & bitMaskForNetworkType) != 0) {
            i2 = C0015R$string.config_rat_3g;
        } else if ((bitMaskForNetworkType & 397312) != 0) {
            i2 = C0015R$string.config_rat_4g;
        }
        return getContext().getResources().getString(i2);
    }

    private String get5GNetworkClass(SubscriptionInfo subscriptionInfo, int i) {
        if (i == 20) {
            return this.mContext.getResources().getString(C0015R$string.data_connection_5g);
        }
        int simSlotIndex = subscriptionInfo.getSimSlotIndex();
        int subscriptionId = subscriptionInfo.getSubscriptionId();
        if (this.mFiveGServiceClient == null) {
            FiveGServiceClient instance = FiveGServiceClient.getInstance(this.mContext);
            this.mFiveGServiceClient = instance;
            instance.registerCallback(this.mCallback);
        }
        if (!this.mFiveGServiceClient.getCurrentServiceState(simSlotIndex).isNrIconTypeValid() || !isDataRegisteredOnLte(subscriptionId)) {
            return null;
        }
        return this.mContext.getResources().getString(C0015R$string.data_connection_5g);
    }

    private boolean isDataRegisteredOnLte(int i) {
        int dataNetworkType = ((TelephonyManager) this.mContext.getSystemService("phone")).getDataNetworkType(i);
        return dataNetworkType == 13 || dataNetworkType == 19;
    }
}
