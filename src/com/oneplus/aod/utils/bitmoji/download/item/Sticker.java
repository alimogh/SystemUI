package com.oneplus.aod.utils.bitmoji.download.item;

import android.database.Cursor;
import android.net.Uri;
import com.oneplus.aod.utils.bitmoji.OpBitmojiHelper;
import org.json.JSONException;
import org.json.JSONObject;
public class Sticker {
    private String mName;
    private String mPackId;
    private Uri mUri;

    private Sticker(String str, JSONObject jSONObject) {
        Uri parse = Uri.parse(jSONObject.optString("uri"));
        this.mUri = parse;
        this.mPackId = str;
        this.mName = parse.getLastPathSegment();
    }

    private Sticker(String str, Cursor cursor) {
        Uri build = Uri.parse(cursor.getString(cursor.getColumnIndexOrThrow("uri"))).buildUpon().appendQueryParameter("image_format", "webp").appendQueryParameter("size", "normal").build();
        this.mUri = build;
        this.mPackId = str;
        this.mName = build.getLastPathSegment();
    }

    private Sticker(String str, String str2) {
        Uri build = OpBitmojiHelper.getInstance().getStickerUriByName(str2).buildUpon().appendQueryParameter("image_format", "webp").appendQueryParameter("size", "normal").build();
        this.mUri = build;
        this.mPackId = str;
        this.mName = build.getLastPathSegment();
    }

    public Uri getUri() {
        return this.mUri;
    }

    public String getName() {
        return this.mName;
    }

    public boolean isDownloaded() {
        return OpBitmojiHelper.getInstance().getImagePath(this.mPackId, this.mName).exists();
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("uri", this.mUri.toString());
        return jSONObject;
    }

    public static Sticker createFromCursor(String str, Cursor cursor) {
        return new Sticker(str, cursor);
    }

    public static Sticker createFromJson(String str, JSONObject jSONObject) {
        return new Sticker(str, jSONObject);
    }

    public static Sticker createByName(String str, String str2) {
        return new Sticker(str, str2);
    }
}
