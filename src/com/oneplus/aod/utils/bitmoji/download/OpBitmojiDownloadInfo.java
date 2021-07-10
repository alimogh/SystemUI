package com.oneplus.aod.utils.bitmoji.download;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.oneplus.aod.utils.bitmoji.OpBitmojiHelper;
import com.oneplus.aod.utils.bitmoji.download.item.Avatar;
import com.oneplus.aod.utils.bitmoji.download.item.LocalPack;
import com.oneplus.aod.utils.bitmoji.download.item.Pack;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
public class OpBitmojiDownloadInfo {
    private Avatar mAvatar = new Avatar();
    private Context mContext;
    private HashMap<String, Pack> mPackMap = new HashMap<>();

    public OpBitmojiDownloadInfo(Context context) {
        this.mContext = context;
        prepare();
    }

    private void prepare() {
        Pack pack;
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpBitmojiDownloadInfo", "prepare");
        }
        boolean z = false;
        SharedPreferences sharedPreferences = this.mContext.getSharedPreferences("bitmoji_info_prefs", 0);
        Map<String, ?> all = sharedPreferences.getAll();
        ArrayList arrayList = new ArrayList();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!TextUtils.isEmpty(key)) {
                if (key.startsWith("pack_")) {
                    String substring = key.substring(5);
                    if (OpBitmojiDownloadManager.DOWNLOAD_PACK_INFO.containsKey(substring)) {
                        try {
                            JSONObject jSONObject = new JSONObject((String) value);
                            if (!"battery_low".equals(substring)) {
                                if (!"charging".equals(substring)) {
                                    pack = new Pack(jSONObject);
                                    this.mPackMap.put(substring, pack);
                                }
                            }
                            pack = new LocalPack(jSONObject);
                            this.mPackMap.put(substring, pack);
                        } catch (JSONException e) {
                            Log.e("OpBitmojiDownloadInfo", "parse pack error", e);
                        }
                    } else {
                        arrayList.add(key);
                        File packFolder = OpBitmojiHelper.getInstance().getPackFolder(substring);
                        if (packFolder.exists()) {
                            packFolder.delete();
                        }
                    }
                } else if (key.equals("avatar")) {
                    this.mAvatar.setNeedsUpdate(((Boolean) value).booleanValue());
                }
            }
        }
        if (arrayList.size() > 0) {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("OpBitmojiDownloadInfo", "prepare: remove invalid data. " + arrayList);
            }
            SharedPreferences.Editor edit = sharedPreferences.edit();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                edit.remove((String) it.next());
            }
            edit.apply();
        }
        if (this.mPackMap.size() > 0) {
            z = true;
        }
        for (String str : OpBitmojiDownloadManager.DOWNLOAD_PACK_INFO.keySet()) {
            if (!this.mPackMap.containsKey(str)) {
                if ("battery_low".equals(str) || "charging".equals(str)) {
                    this.mPackMap.put(str, new LocalPack(str, z));
                } else {
                    this.mPackMap.put(str, new Pack(str, z));
                }
            }
        }
    }

    public boolean isAvatarNeedsUpdate() {
        return !isAvatarDownloaded() || this.mAvatar.isNeedsUpdate();
    }

    public boolean isPackNeesUpdate(String str) {
        Pack pack = this.mPackMap.get(str);
        if (pack != null) {
            return pack.isNeedsUpdate();
        }
        return false;
    }

    public boolean isPackNeedsUpdateOrDownload(String str) {
        Pack pack = this.mPackMap.get(str);
        if (pack != null) {
            return pack.isNeedsUpdateOrDownload();
        }
        return false;
    }

    public void needsUpdate(boolean z) {
        this.mAvatar.setNeedsUpdate(true);
        this.mAvatar.writeToFile(this.mContext);
        if (!z) {
            for (Pack pack : this.mPackMap.values()) {
                pack.setNeedsUpdate(true);
                try {
                    pack.writeToFile(this.mContext);
                } catch (Exception e) {
                    Log.e("OpBitmojiDownloadInfo", "needsUpdate pack write to file error" + pack.getPackId(), e);
                }
            }
        }
    }

    public boolean isAvatarDownloaded() {
        return OpBitmojiHelper.getInstance().getAvatarFile().exists();
    }

    public Avatar getAvatar() {
        return this.mAvatar;
    }

    public Pack getPack(String str) {
        return this.mPackMap.get(str);
    }
}
