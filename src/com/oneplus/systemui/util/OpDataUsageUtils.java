package com.oneplus.systemui.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.TextView;
import com.android.keyguard.CarrierTextController;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.google.android.collect.Maps;
import java.util.HashMap;
import java.util.Map;
public class OpDataUsageUtils {
    private static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    private static final String TAG = "OpDataUsageUtils";
    private final Handler mBgHandler;
    private CharSequence mCarrierText;
    private final TextView mCarrierTextView;
    private final Context mContext;
    private int mCurrentDataSim;
    private boolean mExpanded = false;
    private DataUsageObserver mObserver;
    private CharSequence mOriginalCarrierText;
    private boolean mShowUsage;
    private TelephonyManager mTelephonyManager;
    private final Handler mUiHandler;

    public OpDataUsageUtils(Context context, TextView textView) {
        this.mContext = context;
        this.mCarrierTextView = textView;
        this.mUiHandler = (Handler) Dependency.get(Dependency.MAIN_HANDLER);
        this.mBgHandler = new Handler((Looper) Dependency.get(Dependency.BG_LOOPER));
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mObserver = new DataUsageObserver(this.mBgHandler);
    }

    public void setListening(boolean z) {
        if (z) {
            this.mObserver.startObserving();
            refreshUI();
            return;
        }
        this.mObserver.stopObserving();
    }

    public void setExpanded(boolean z) {
        if (this.mExpanded != z) {
            this.mExpanded = z;
            refreshUI();
        }
    }

    public void setCarrierText(CarrierTextController.CarrierTextCallbackInfo carrierTextCallbackInfo) {
        int currentTrafficRunningSlotId = getCurrentTrafficRunningSlotId();
        this.mCurrentDataSim = currentTrafficRunningSlotId;
        this.mOriginalCarrierText = carrierTextCallbackInfo.carrierText;
        CharSequence[] charSequenceArr = carrierTextCallbackInfo.listOfCarriers;
        if (charSequenceArr == null || charSequenceArr.length == 0 || currentTrafficRunningSlotId < 0 || carrierTextCallbackInfo.airplaneMode) {
            String str = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("slot id ");
            sb.append(this.mCurrentDataSim);
            sb.append(", carriersSize=");
            CharSequence[] charSequenceArr2 = carrierTextCallbackInfo.listOfCarriers;
            sb.append(charSequenceArr2 != null ? Integer.valueOf(charSequenceArr2.length) : "null");
            sb.append(", airplaneMode=");
            sb.append(carrierTextCallbackInfo.airplaneMode);
            Log.d(str, sb.toString());
            this.mShowUsage = false;
            refreshUI();
            return;
        }
        try {
            if (charSequenceArr.length == 1) {
                this.mCarrierText = charSequenceArr[0];
            } else {
                this.mCarrierText = charSequenceArr[currentTrafficRunningSlotId];
            }
            this.mShowUsage = true;
        } catch (Exception e) {
            Log.d(TAG, "setCarrierText: exception caught.", e);
            this.mCarrierText = carrierTextCallbackInfo.carrierText;
            this.mShowUsage = false;
        }
        refreshUI();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshUI() {
        if (this.mShowUsage && this.mExpanded) {
            this.mBgHandler.post(new Runnable() { // from class: com.oneplus.systemui.util.-$$Lambda$OpDataUsageUtils$uOJ2urMJPCqOdTJ9ZHr5Kqo-F68
                @Override // java.lang.Runnable
                public final void run() {
                    OpDataUsageUtils.this.lambda$refreshUI$1$OpDataUsageUtils();
                }
            });
        } else if (!this.mCarrierTextView.getText().equals(this.mOriginalCarrierText)) {
            this.mUiHandler.post(new Runnable() { // from class: com.oneplus.systemui.util.-$$Lambda$OpDataUsageUtils$xFu2vh2aMOGKRmS4bykjBlCqxaU
                @Override // java.lang.Runnable
                public final void run() {
                    OpDataUsageUtils.this.lambda$refreshUI$2$OpDataUsageUtils();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$refreshUI$1 */
    public /* synthetic */ void lambda$refreshUI$1$OpDataUsageUtils() {
        String dataUsageString = getDataUsageString();
        if (TextUtils.isEmpty(dataUsageString)) {
            if (!TextUtils.isEmpty(this.mCarrierText)) {
                String str = TAG;
                Log.d(str, "override to usage: " + ((Object) this.mCarrierText));
                dataUsageString = this.mCarrierText.toString();
            } else if (!TextUtils.isEmpty(this.mOriginalCarrierText)) {
                String str2 = TAG;
                Log.d(str2, "override to default: " + ((Object) this.mOriginalCarrierText));
                dataUsageString = this.mOriginalCarrierText.toString();
            }
        }
        if (!dataUsageString.equals(this.mCarrierTextView.getText())) {
            this.mUiHandler.post(new Runnable(dataUsageString) { // from class: com.oneplus.systemui.util.-$$Lambda$OpDataUsageUtils$hZyDzNsnrzo6snzxfW_684QJdYE
                public final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    OpDataUsageUtils.this.lambda$refreshUI$0$OpDataUsageUtils(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$refreshUI$0 */
    public /* synthetic */ void lambda$refreshUI$0$OpDataUsageUtils(String str) {
        this.mCarrierTextView.setText(str);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$refreshUI$2 */
    public /* synthetic */ void lambda$refreshUI$2$OpDataUsageUtils() {
        this.mCarrierTextView.setText(this.mOriginalCarrierText);
    }

    private String getDataUsageString() {
        int currentTrafficRunningSlotId = getCurrentTrafficRunningSlotId();
        this.mCurrentDataSim = currentTrafficRunningSlotId;
        if (!isSlotSimInserted(currentTrafficRunningSlotId)) {
            String str = TAG;
            Log.d(str, "getDataUsage: sim " + this.mCurrentDataSim + " not inserted");
            return "";
        }
        Map<String, Object> oneplusDataUsage = getOneplusDataUsage(this.mContext, this.mCurrentDataSim);
        if (oneplusDataUsage == null || oneplusDataUsage.isEmpty()) {
            String str2 = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("getDataUsage: data is ");
            sb.append(oneplusDataUsage == null ? "null" : "empty");
            Log.d(str2, sb.toString());
            return "";
        }
        int intValue = ((Integer) oneplusDataUsage.get("oneplus_datausage_error_code")).intValue();
        if (intValue != 0) {
            String str3 = TAG;
            Log.e(str3, "getDataUsage: slotId=" + this.mCurrentDataSim + ", errorCode=" + intValue);
            return "";
        }
        long longValue = ((Long) oneplusDataUsage.get("oneplus_datausage_used")).longValue();
        Formatter.BytesResult formatBytes = Formatter.formatBytes(this.mContext.getResources(), longValue, 10);
        String format = String.format(this.mContext.getResources().getString(C0015R$string.op_qs_data_usage_used), this.mCarrierText, formatBytes.value, formatBytes.units);
        if (DEBUG) {
            String str4 = TAG;
            Log.d(str4, "getDataUsage: slotId=" + this.mCurrentDataSim + ", usage=" + longValue + ", result=" + format);
        }
        return format;
    }

    private static int getCurrentTrafficRunningSlotId() {
        return findSlotIdBySubId(SubscriptionManager.getDefaultDataSubscriptionId());
    }

    private static int findSlotIdBySubId(int i) {
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        for (int i2 = 0; i2 < phoneCount; i2++) {
            int[] subId = SubscriptionManager.getSubId(i2);
            if (subId != null && subId.length > 0 && i == subId[0]) {
                return i2;
            }
        }
        return 0;
    }

    private boolean isSlotSimInserted(int i) {
        int simState;
        return i >= 0 && i <= 1 && 1 != (simState = this.mTelephonyManager.getSimState(i)) && simState != 0;
    }

    private static Map<String, Object> getOneplusDataUsage(Context context, int i) {
        Bundle bundle = new Bundle();
        bundle.putInt("oneplus_datausage_slotid", i);
        try {
            Bundle call = context.getContentResolver().call(Uri.parse("content://com.oneplus.security.database.SafeProvider"), "method_query_oneplus_datausage", (String) null, bundle);
            if (call != null) {
                int i2 = call.getInt("oneplus_datausage_error_code");
                int i3 = call.getInt("oneplus_datausage_accountday");
                long j = call.getLong("oneplus_datausage_time_start");
                long j2 = call.getLong("oneplus_datausage_time_end");
                long j3 = call.getLong("oneplus_datausage_total");
                long j4 = call.getLong("oneplus_datausage_used");
                boolean z = call.getBoolean("oneplus_datausage_warn_state");
                long j5 = call.getLong("oneplus_datausage_warn_value");
                HashMap newHashMap = Maps.newHashMap();
                newHashMap.put("oneplus_datausage_error_code", Integer.valueOf(i2));
                newHashMap.put("oneplus_datausage_accountday", Integer.valueOf(i3));
                newHashMap.put("oneplus_datausage_total", Long.valueOf(j3));
                newHashMap.put("oneplus_datausage_used", Long.valueOf(j4));
                newHashMap.put("oneplus_datausage_time_start", Long.valueOf(j));
                newHashMap.put("oneplus_datausage_time_end", Long.valueOf(j2));
                newHashMap.put("oneplus_datausage_warn_state", Boolean.valueOf(z));
                newHashMap.put("oneplus_datausage_warn_value", Long.valueOf(j5));
                return newHashMap;
            }
        } catch (Exception e) {
            Log.d(TAG, "getOneplusDataUsage error", e);
        }
        return null;
    }

    /* access modifiers changed from: private */
    public class DataUsageObserver extends ContentObserver {
        private DataUsageObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            onChange(z, null);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            if (!z) {
                Log.d(OpDataUsageUtils.TAG, "Usage update");
                OpDataUsageUtils.this.refreshUI();
            }
        }

        public void startObserving() {
            ContentResolver contentResolver = OpDataUsageUtils.this.mContext.getContentResolver();
            try {
                contentResolver.unregisterContentObserver(this);
                contentResolver.registerContentObserver(Uri.parse("content://com.oneplus.security.database.SafeProvider"), false, this, -1);
            } catch (Exception e) {
                Log.d(OpDataUsageUtils.TAG, "startObserving. exception caught.", e);
            }
        }

        public void stopObserving() {
            try {
                OpDataUsageUtils.this.mContext.getContentResolver().unregisterContentObserver(this);
            } catch (Exception e) {
                Log.d(OpDataUsageUtils.TAG, "stopObserving. exception caught.", e);
            }
        }
    }
}
