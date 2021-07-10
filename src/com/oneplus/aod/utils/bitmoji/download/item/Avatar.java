package com.oneplus.aod.utils.bitmoji.download.item;

import android.content.Context;
import android.content.SharedPreferences;
import org.jetbrains.annotations.Nullable;
/* compiled from: Avatar.kt */
public final class Avatar {
    private boolean mNeedsUpdate = true;

    public final void setNeedsUpdate(boolean z) {
        this.mNeedsUpdate = z;
    }

    public final boolean isNeedsUpdate() {
        return this.mNeedsUpdate;
    }

    public final void writeToFile(@Nullable Context context) {
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor edit;
        SharedPreferences.Editor putBoolean;
        if (context != null && (sharedPreferences = context.getSharedPreferences("bitmoji_info_prefs", 0)) != null && (edit = sharedPreferences.edit()) != null && (putBoolean = edit.putBoolean("avatar", this.mNeedsUpdate)) != null) {
            putBoolean.commit();
        }
    }
}
