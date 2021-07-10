package com.oneplus.aod.utils.bitmoji.download.item;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import com.oneplus.aod.utils.bitmoji.OpBitmojiHelper;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class Pack {
    protected String mId;
    protected boolean mIsNew;
    protected boolean mNeedsUpdate;
    protected HashMap<String, Sticker> mStickerMap;

    public Pack(JSONObject jSONObject) throws JSONException {
        this(jSONObject.optString("packId"), false);
        JSONArray optJSONArray = jSONObject.optJSONArray("stickers");
        if (optJSONArray != null && optJSONArray.length() > 0) {
            for (int i = 0; i < optJSONArray.length(); i++) {
                Sticker createFromJson = Sticker.createFromJson(this.mId, optJSONArray.optJSONObject(i));
                if (!TextUtils.isEmpty(createFromJson.getName())) {
                    this.mStickerMap.put(createFromJson.getName(), createFromJson);
                }
            }
        }
        this.mNeedsUpdate = jSONObject.optBoolean("needsUpdate", false);
        this.mIsNew = jSONObject.optBoolean("new", false);
    }

    public Pack(String str, boolean z) {
        this.mId = str;
        this.mStickerMap = new HashMap<>();
        this.mNeedsUpdate = z;
        this.mIsNew = z;
    }

    public void checkUnmatchStickers(HashMap<String, Sticker> hashMap) {
        HashSet<String> hashSet = new HashSet();
        hashSet.addAll(this.mStickerMap.keySet());
        for (String str : hashSet) {
            if (!hashMap.containsKey(str)) {
                removeSticker(str);
            }
        }
        String[] imagesPathByPackId = OpBitmojiHelper.getInstance().getImagesPathByPackId(this.mId);
        if (imagesPathByPackId != null && imagesPathByPackId.length > 0) {
            for (String str2 : imagesPathByPackId) {
                if (!hashMap.containsKey(Uri.parse(str2).getLastPathSegment())) {
                    File file = new File(str2);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        }
        for (String str3 : hashMap.keySet()) {
            addSticker(hashMap.get(str3));
        }
    }

    public void addSticker(Sticker sticker) {
        if (sticker != null && !TextUtils.isEmpty(sticker.getName())) {
            this.mStickerMap.put(sticker.getName(), sticker);
        }
    }

    public void removeSticker(String str) {
        this.mStickerMap.remove(str);
        File imagePath = OpBitmojiHelper.getInstance().getImagePath(this.mId, str);
        if (imagePath.exists()) {
            try {
                imagePath.delete();
            } catch (Exception unused) {
            }
        }
    }

    public String getPackId() {
        return this.mId;
    }

    public Collection<Sticker> values() {
        return this.mStickerMap.values();
    }

    public void setNeedsUpdate(boolean z) {
        this.mNeedsUpdate = z;
    }

    public boolean isNew() {
        return this.mIsNew;
    }

    public void notNew() {
        this.mIsNew = false;
    }

    public boolean isNeedsUpdate() {
        return isNew() || this.mNeedsUpdate;
    }

    public boolean isNeedsUpdateOrDownload() {
        return needsDownload() || this.mNeedsUpdate;
    }

    public boolean needsDownload() {
        if (this.mStickerMap.size() == 0) {
            return true;
        }
        for (Sticker sticker : this.mStickerMap.values()) {
            if (!sticker.isDownloaded()) {
                return true;
            }
        }
        return false;
    }

    public void writeToFile(Context context) throws JSONException {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("packId", this.mId);
        JSONArray jSONArray = new JSONArray();
        for (Sticker sticker : this.mStickerMap.values()) {
            jSONArray.put(sticker.toJson());
        }
        jSONObject.put("stickers", jSONArray);
        jSONObject.put("needsUpdate", this.mNeedsUpdate);
        jSONObject.put("new", this.mIsNew);
        context.getSharedPreferences("bitmoji_info_prefs", 0).edit().putString(getPackDownloadKey(), jSONObject.toString()).commit();
    }

    private String getPackDownloadKey() {
        return "pack_" + this.mId;
    }
}
