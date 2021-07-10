package com.oneplus.onlineconfig;

import android.content.Context;
import android.util.Log;
import com.android.internal.os.BackgroundThread;
import com.oneplus.config.ConfigObserver;
import java.util.ArrayList;
import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class OpSystemUIGestureOnlineConfig {
    private static OpSystemUIGestureOnlineConfig sInstance;
    private boolean mInit = false;
    private ArrayList<String> mNonBlockBackGestureList = new ArrayList<>(Arrays.asList("com.jingdong.app.mall", "com.bilibili.app", "com.bilibili.app.in", "tv.danmaku.bili", "com.tencent.tmgp.sgame"));
    private ArrayList<String> mPhotoEditorList = new ArrayList<>(Arrays.asList("com.tencent.mm.plugin.recordvideo.activity.MMRecordUI"));
    private ArrayList<String> mUseNativeOpaqueColorList = new ArrayList<>(Arrays.asList("com.ss.android.ugc.aweme", "com.ss.android.ugc.trill", "com.ss.android.article.news"));

    public static synchronized OpSystemUIGestureOnlineConfig getInstance() {
        OpSystemUIGestureOnlineConfig opSystemUIGestureOnlineConfig;
        synchronized (OpSystemUIGestureOnlineConfig.class) {
            if (sInstance == null) {
                sInstance = new OpSystemUIGestureOnlineConfig();
            }
            opSystemUIGestureOnlineConfig = sInstance;
        }
        return opSystemUIGestureOnlineConfig;
    }

    private OpSystemUIGestureOnlineConfig() {
    }

    public void init(Context context) {
        synchronized (OpSystemUIGestureOnlineConfig.class) {
            if (!this.mInit) {
                Log.d("OpGestureOnlineConfig", "init");
                this.mInit = true;
                try {
                    new ConfigObserver(context.getApplicationContext(), BackgroundThread.getHandler(), new OnlineConfigUpdater(), "opSystemUIGestureOnlineConfig").register();
                    Log.i("OpGestureOnlineConfig", "Register online config observer");
                } catch (SecurityException e) {
                    Log.w("OpGestureOnlineConfig", "Register online config fail, " + e);
                    this.mInit = false;
                }
            } else {
                Log.w("OpGestureOnlineConfig", "already init.");
            }
        }
    }

    /* access modifiers changed from: private */
    public class OnlineConfigUpdater implements ConfigObserver.ConfigUpdater {
        private OnlineConfigUpdater() {
        }

        public void updateConfig(JSONArray jSONArray) {
            Log.v("OpGestureOnlineConfig", "Receive online config update");
            OpSystemUIGestureOnlineConfig.this.resolveConfigFromJSON(jSONArray);
        }
    }

    public void resolveConfigFromJSON(JSONArray jSONArray) {
        if (jSONArray == null) {
            Log.w("OpGestureOnlineConfig", "[OnlineConfig] config is null!");
            return;
        }
        for (int i = 0; i < jSONArray.length(); i++) {
            try {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                String string = jSONObject.getString("name");
                ArrayList arrayList = new ArrayList();
                JSONArray jSONArray2 = jSONObject.getJSONArray("value");
                for (int i2 = 0; i2 < jSONArray2.length(); i2++) {
                    arrayList.add(jSONArray2.getString(i2));
                }
                if (string.equals("photoEditorList")) {
                    synchronized (this.mPhotoEditorList) {
                        this.mPhotoEditorList.clear();
                        this.mPhotoEditorList.addAll(arrayList);
                    }
                    Log.d("OpGestureOnlineConfig", "[OnlineConfig] photo editor list changed");
                } else if (string.equals("nonBlockBackGestureList")) {
                    synchronized (this.mNonBlockBackGestureList) {
                        this.mNonBlockBackGestureList.clear();
                        this.mNonBlockBackGestureList.addAll(arrayList);
                    }
                    Log.d("OpGestureOnlineConfig", "[OnlineConfig] non block back gesture list changed");
                } else if (string.equals("nativeOpaqueColorList")) {
                    synchronized (this.mUseNativeOpaqueColorList) {
                        this.mUseNativeOpaqueColorList.clear();
                        this.mUseNativeOpaqueColorList.addAll(arrayList);
                    }
                    Log.d("OpGestureOnlineConfig", "[OnlineConfig] native opaque color list changed");
                } else {
                    continue;
                }
            } catch (JSONException e) {
                Log.w("OpGestureOnlineConfig", "[OnlineConfig] Failed to process onlineconfig! \n" + e);
                return;
            } catch (Exception e2) {
                Log.e("OpGestureOnlineConfig", "[OnlineConfig] Get gesture online config error. " + e2);
                return;
            }
        }
    }

    public boolean isInPhotoEditorList(String str) {
        return this.mPhotoEditorList.contains(str);
    }

    public boolean isInNonBlockBackGestureList(String str) {
        return this.mNonBlockBackGestureList.contains(str);
    }

    public boolean isUseNativeOpaqueColor(String str) {
        boolean contains;
        synchronized (this.mUseNativeOpaqueColorList) {
            contains = this.mUseNativeOpaqueColorList.contains(str);
        }
        return contains;
    }
}
