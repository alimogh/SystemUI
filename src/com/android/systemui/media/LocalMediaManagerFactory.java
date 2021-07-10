package com.android.systemui.media;

import android.content.Context;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.media.InfoMediaManager;
import com.android.settingslib.media.LocalMediaManager;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: LocalMediaManagerFactory.kt */
public final class LocalMediaManagerFactory {
    private final Context context;
    private final LocalBluetoothManager localBluetoothManager;

    public LocalMediaManagerFactory(@NotNull Context context, @Nullable LocalBluetoothManager localBluetoothManager) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.context = context;
        this.localBluetoothManager = localBluetoothManager;
    }

    @NotNull
    public final LocalMediaManager create(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "packageName");
        return new LocalMediaManager(this.context, this.localBluetoothManager, new InfoMediaManager(this.context, str, null, this.localBluetoothManager), str);
    }
}
