package com.android.systemui.media;

import android.content.Context;
import com.android.systemui.util.Utils;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: MediaFeatureFlag.kt */
public final class MediaFeatureFlag {
    private final Context context;

    public MediaFeatureFlag(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.context = context;
    }

    public final boolean getEnabled() {
        return Utils.useQsMediaPlayer(this.context);
    }
}
