package com.oneplus.aod.utils.bitmoji.download.item;

import java.util.HashMap;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
/* compiled from: LocalPack.kt */
public final class LocalPack extends Pack {
    @NotNull
    private static final String[] STICKER_BATTERY_LOW = {"a5dd74df-da36-4095-9bd0-ad786f85f237"};
    @NotNull
    private static final String[] STICKER_CHARGING = {"2b617cc0-2f81-4699-a793-5bb32e1098cc"};

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public LocalPack(@NotNull String str, boolean z) {
        super(str, z);
        Intrinsics.checkParameterIsNotNull(str, "id");
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public LocalPack(@NotNull JSONObject jSONObject) {
        super(jSONObject);
        Intrinsics.checkParameterIsNotNull(jSONObject, "json");
    }

    @NotNull
    public final HashMap<String, Sticker> getLocalStickers() {
        String[] strArr;
        if (this.mId.equals("battery_low")) {
            strArr = STICKER_BATTERY_LOW;
        } else {
            strArr = this.mId.equals("charging") ? STICKER_CHARGING : null;
        }
        HashMap<String, Sticker> hashMap = new HashMap<>();
        boolean z = true;
        if (strArr != null) {
            if (!(strArr.length == 0)) {
                z = false;
            }
        }
        if (!z) {
            for (String str : strArr) {
                Sticker createByName = Sticker.createByName(this.mId, str);
                hashMap.put(createByName.getName(), createByName);
            }
        }
        return hashMap;
    }
}
