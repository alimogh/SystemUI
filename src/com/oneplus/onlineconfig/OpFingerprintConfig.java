package com.oneplus.onlineconfig;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.os.BackgroundThread;
import com.oneplus.config.ConfigObserver;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class OpFingerprintConfig {
    private ArrayList<String> mAppUnsupportAccelerateList = new ArrayList<>();

    static {
        SystemProperties.get("persist.white_list.package.verify", "com.oneplus.systemui.test");
    }

    public OpFingerprintConfig(Context context) {
        if (context.getPackageManager().hasSystemFeature("android.hardware.fingerprint")) {
            initUnsupportAccelerateList();
            try {
                new ConfigObserver(context.getApplicationContext(), BackgroundThread.getHandler(), new OnlineConfigUpdater(), "fingerprintConfig").register();
                Log.i("KeyguardFingerprintConfig", "Register online config observer");
            } catch (SecurityException e) {
                Log.w("KeyguardFingerprintConfig", "Register online config fail, " + e);
            }
        }
    }

    private void initUnsupportAccelerateList() {
        Log.v("KeyguardFingerprintConfig", "initUnsupportAccelerateList");
        this.mAppUnsupportAccelerateList.clear();
        this.mAppUnsupportAccelerateList.add("com.android.chrome");
        this.mAppUnsupportAccelerateList.add("com.autonavi.minimap");
        this.mAppUnsupportAccelerateList.add("com.shuqiyuedu823.google");
        this.mAppUnsupportAccelerateList.add("com.amazon.avod.thirdpartyclient");
        this.mAppUnsupportAccelerateList.add("com.google.android.apps.maps");
        this.mAppUnsupportAccelerateList.add("com.tencent.qqlive");
        this.mAppUnsupportAccelerateList.add("com.youku.phone");
        this.mAppUnsupportAccelerateList.add("com.qiyi.video");
        this.mAppUnsupportAccelerateList.add("com.google.android.calendar");
        this.mAppUnsupportAccelerateList.add("com.tencent.mm");
        this.mAppUnsupportAccelerateList.add("com.sdu.didi.psnger");
        this.mAppUnsupportAccelerateList.add("com.oppo.im");
        this.mAppUnsupportAccelerateList.add("com.oneplus.calculator");
        this.mAppUnsupportAccelerateList.add("com.nearme.gamecenter");
        this.mAppUnsupportAccelerateList.add("com.snapchat.android");
        this.mAppUnsupportAccelerateList.add("com.baidu.netdisk");
        this.mAppUnsupportAccelerateList.add("com.lingdian.activity");
    }

    private class OnlineConfigUpdater implements ConfigObserver.ConfigUpdater {
        private OnlineConfigUpdater() {
        }

        public void updateConfig(JSONArray jSONArray) {
            Log.v("KeyguardFingerprintConfig", "Receive online config update");
            OpFingerprintConfig.this.resolveConfigFromJSON(jSONArray);
        }
    }

    public void resolveConfigFromJSON(JSONArray jSONArray) {
        if (jSONArray == null) {
            Log.w("KeyguardFingerprintConfig", "[OnlineConfig] config is null!");
            return;
        }
        for (int i = 0; i < jSONArray.length(); i++) {
            try {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                if (jSONObject.getString("name").equals("unsupportAccelerateList")) {
                    handleUnsupportAccelerateList(jSONObject);
                }
            } catch (JSONException e) {
                Log.w("KeyguardFingerprintConfig", "[OnlineConfig] Failed to process onlineconfig! \n" + e);
                return;
            } catch (Exception e2) {
                Log.e("KeyguardFingerprintConfig", "getWhiteList error. " + e2);
                return;
            }
        }
    }

    private void handleUnsupportAccelerateList(JSONObject jSONObject) {
        Log.v("KeyguardFingerprintConfig", "handleUnsupportAccelerateList");
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(this.mAppUnsupportAccelerateList);
        try {
            JSONArray jSONArray = jSONObject.getJSONArray("value");
            synchronized (this.mAppUnsupportAccelerateList) {
                this.mAppUnsupportAccelerateList.clear();
                for (int i = 0; i < jSONArray.length(); i++) {
                    this.mAppUnsupportAccelerateList.add(jSONArray.getString(i));
                }
            }
            Log.d("KeyguardFingerprintConfig", "[OnlineConfig] Fp unsuppor acceleratet list changed");
        } catch (JSONException e) {
            Log.w("KeyguardFingerprintConfig", "[OnlineConfig] Failed to process onlineconfig! \n" + e);
            this.mAppUnsupportAccelerateList.clear();
            this.mAppUnsupportAccelerateList.addAll(arrayList);
        }
    }

    public boolean isAppSupportAccelerate(String str) {
        Log.i("KeyguardFingerprintConfig", " isAppSupportAccelerate:" + str);
        if (SystemProperties.getBoolean("fingerprint.unsupportaccelete.test", false) || this.mAppUnsupportAccelerateList.contains(str)) {
            return false;
        }
        return true;
    }

    public ArrayList getAppUnsupportAccelerateList() {
        return this.mAppUnsupportAccelerateList;
    }
}
