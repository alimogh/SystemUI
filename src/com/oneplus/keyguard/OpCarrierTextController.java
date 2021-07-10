package com.oneplus.keyguard;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.keyguard.CarrierTextController;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.C0001R$array;
import com.android.systemui.C0015R$string;
import com.android.systemui.statusbar.policy.FiveGServiceClient;
import com.oneplus.util.OpReflectionUtils;
import com.oneplus.util.OpUtils;
public class OpCarrierTextController {
    private static final int[] SHOW_ROAMING_BANNER = {3, 134, 144, 154, 179, 180, 213, 214, 215, 130, 140, 150};
    private final CarrierConfigManager mCarrierConfigManager;
    private Context mContext;
    private FiveGServiceClient mFiveGServiceClient;
    private CharSequence mSeparator;
    private final TelephonyManager mTelephonyManager;

    public OpCarrierTextController(Context context, CharSequence charSequence, boolean z, boolean z2) {
        WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
        this.mContext = context;
        this.mSeparator = charSequence;
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mCarrierConfigManager = (CarrierConfigManager) context.getSystemService("carrier_config");
    }

    /* access modifiers changed from: protected */
    public String opGetCustomizeCarrierName(CharSequence charSequence, SubscriptionInfo subscriptionInfo) {
        StringBuilder sb = new StringBuilder();
        getNetworkType(subscriptionInfo.getSubscriptionId());
        if (this.mFiveGServiceClient == null) {
            FiveGServiceClient instance = FiveGServiceClient.getInstance(this.mContext);
            this.mFiveGServiceClient = instance;
            instance.registerCallback(getKeyguardUpdateMonitorCallback());
        }
        if (!TextUtils.isEmpty(charSequence)) {
            String[] split = charSequence.toString().split(this.mSeparator.toString(), 2);
            for (int i = 0; i < split.length; i++) {
                split[i] = opGetLocalString(split[i], C0001R$array.oneplus_origin_carrier_names, C0001R$array.oneplus_locale_carrier_names);
                if (!TextUtils.isEmpty(split[i]) && (i <= 0 || !split[i].equals(split[i - 1]))) {
                    if (i > 0) {
                        sb.append(this.mSeparator);
                    }
                    sb.append(split[i]);
                }
            }
        }
        return sb.toString();
    }

    /* JADX DEBUG: TODO: convert one arg to string using `String.valueOf()`, args: [(r2v0 java.lang.CharSequence), (r4v0 java.lang.CharSequence), (r3v0 java.lang.CharSequence)] */
    protected static CharSequence opConcatenate(CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3) {
        boolean z = !TextUtils.isEmpty(charSequence);
        boolean z2 = !TextUtils.isEmpty(charSequence2);
        if (!z || !z2) {
            if (z) {
                return charSequence;
            }
            return z2 ? charSequence2 : "";
        } else if (TextUtils.isEmpty(charSequence3)) {
            return charSequence + " | " + charSequence2;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(charSequence);
            sb.append(charSequence3);
            sb.append(charSequence2);
            return sb.toString();
        }
    }

    /* access modifiers changed from: protected */
    public CharSequence opMakeCarrierStringOnEmergencyCapable(CharSequence charSequence, CharSequence charSequence2) {
        return getIsEmergencyCallCapable() ? opConcatenate(charSequence, charSequence2, " - ") : charSequence;
    }

    /* access modifiers changed from: protected */
    public String opGetLocalString(String str, int i, int i2) {
        String[] stringArray = this.mContext.getResources().getStringArray(i);
        String[] stringArray2 = this.mContext.getResources().getStringArray(i2);
        for (int i3 = 0; i3 < stringArray.length; i3++) {
            if (stringArray[i3].equalsIgnoreCase(str)) {
                if (Build.DEBUG_ONEPLUS) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("carrier\"");
                    sb.append(str);
                    sb.append("\"exist local nameId:");
                    sb.append(stringArray2[i3]);
                    sb.append(" local name:");
                    Context context = this.mContext;
                    sb.append(context.getString(context.getResources().getIdentifier(stringArray2[i3], "string", "com.android.systemui")));
                    Log.i("OpCarrierTextController", sb.toString());
                }
                Context context2 = this.mContext;
                return context2.getString(context2.getResources().getIdentifier(stringArray2[i3], "string", "com.android.systemui"));
            }
        }
        if (Build.DEBUG_ONEPLUS) {
            Log.i("OpCarrierTextController", "carrier\"" + str + "\"doesn't exist local name");
        }
        return str;
    }

    /* access modifiers changed from: protected */
    public CharSequence opGetCarrierName(int i, CharSequence charSequence) {
        ServiceState serviceState;
        if (!OpUtils.isUSS() || (serviceState = getKeyguardUpdateMonitor().opGetServiceStates().get(Integer.valueOf(i))) == null) {
            return charSequence;
        }
        if (showUSSRoamingIndicator(serviceState)) {
            String eriText = getEriText(i);
            Log.d("OpCarrierTextController", "(getEriText=" + ((Object) eriText));
            return eriText;
        } else if (showRoaming(serviceState)) {
            return this.mContext.getResources().getString(C0015R$string.sim_status_uss_network_roam);
        } else {
            return serviceState.getOperatorAlphaLong();
        }
    }

    private boolean showUSSRoamingIndicator(ServiceState serviceState) {
        boolean z;
        boolean z2;
        if (serviceState == null || this.mCarrierConfigManager == null) {
            return false;
        }
        boolean roaming = serviceState.getRoaming();
        PersistableBundle config = this.mCarrierConfigManager.getConfig();
        if (config != null) {
            String string = config.getString("carrier_eri_file_name_string");
            z = !TextUtils.equals(string, "eri.xml");
            Log.i("OpCarrierTextController", "showUSSRoamingIndicator eriFile:" + string);
        } else {
            z = false;
        }
        int cdmaRoamingIndicator = serviceState.getCdmaRoamingIndicator();
        int i = 0;
        while (true) {
            int[] iArr = SHOW_ROAMING_BANNER;
            if (i >= iArr.length) {
                z2 = false;
                break;
            } else if (cdmaRoamingIndicator == iArr[i]) {
                z2 = true;
                break;
            } else {
                i++;
            }
        }
        Log.i("OpCarrierTextController", "showUSSRoamingIndicator isRoaming:" + roaming + " isSpecifyEriInfoCard:" + z + " isShowRoamingbanner:" + z2 + " romaingIndicator:" + cdmaRoamingIndicator);
        if (!roaming || !z || !z2) {
            return false;
        }
        return true;
    }

    private String getEriText(int i) {
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager == null) {
            return "";
        }
        return telephonyManager.getCdmaEriText(i);
    }

    private boolean showRoaming(ServiceState serviceState) {
        Context context;
        if (serviceState == null || (context = this.mContext) == null || !OpUtils.isSprintMccMnc(context) || !isUssCDMA() || !serviceState.getVoiceRoaming()) {
            return false;
        }
        return true;
    }

    private boolean isUssCDMA() {
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager == null) {
            return false;
        }
        int voiceNetworkType = telephonyManager.getVoiceNetworkType();
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.d("OpCarrierTextController", "isUssCDMA getType = " + voiceNetworkType);
        }
        return voiceNetworkType == 4 || voiceNetworkType == 5 || voiceNetworkType == 6 || voiceNetworkType == 7 || voiceNetworkType == 12 || voiceNetworkType == 14;
    }

    private boolean getIsEmergencyCallCapable() {
        Object value = OpReflectionUtils.getValue(CarrierTextController.class, this, "mIsEmergencyCallCapable");
        if (value != null) {
            return ((Boolean) value).booleanValue();
        }
        return false;
    }

    private KeyguardUpdateMonitorCallback getKeyguardUpdateMonitorCallback() {
        return (KeyguardUpdateMonitorCallback) OpReflectionUtils.getValue(CarrierTextController.class, this, "mCallback");
    }

    private int getNetworkType(int i) {
        return ((Integer) OpReflectionUtils.methodInvokeWithArgs(this, OpReflectionUtils.getMethodWithParams(CarrierTextController.class, "getNetworkType", Integer.TYPE), Integer.valueOf(i))).intValue();
    }

    private KeyguardUpdateMonitor getKeyguardUpdateMonitor() {
        return (KeyguardUpdateMonitor) OpReflectionUtils.getValue(CarrierTextController.class, this, "mKeyguardUpdateMonitor");
    }
}
