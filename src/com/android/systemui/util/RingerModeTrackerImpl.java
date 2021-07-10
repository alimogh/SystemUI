package com.android.systemui.util;

import android.media.AudioManager;
import androidx.lifecycle.LiveData;
import com.android.systemui.broadcast.BroadcastDispatcher;
import java.util.concurrent.Executor;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;
/* compiled from: RingerModeTrackerImpl.kt */
public final class RingerModeTrackerImpl implements RingerModeTracker {
    @NotNull
    private final LiveData<Integer> ringerMode;
    @NotNull
    private final LiveData<Integer> ringerModeInternal;

    public RingerModeTrackerImpl(@NotNull AudioManager audioManager, @NotNull BroadcastDispatcher broadcastDispatcher, @NotNull Executor executor) {
        Intrinsics.checkParameterIsNotNull(audioManager, "audioManager");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(executor, "executor");
        this.ringerMode = new RingerModeLiveData(broadcastDispatcher, executor, "android.media.RINGER_MODE_CHANGED", new Function0<Integer>(audioManager) { // from class: com.android.systemui.util.RingerModeTrackerImpl$ringerMode$1
            @Override // kotlin.jvm.internal.CallableReference
            public final String getName() {
                return "getRingerMode";
            }

            @Override // kotlin.jvm.internal.CallableReference
            public final KDeclarationContainer getOwner() {
                return Reflection.getOrCreateKotlinClass(AudioManager.class);
            }

            @Override // kotlin.jvm.internal.CallableReference
            public final String getSignature() {
                return "getRingerMode()I";
            }

            /* Return type fixed from 'int' to match base method */
            /* JADX WARN: Type inference failed for: r0v3, types: [int, java.lang.Integer] */
            /* JADX WARNING: Unknown variable types count: 1 */
            @Override // kotlin.jvm.functions.Function0
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public final java.lang.Integer invoke() {
                /*
                    r0 = this;
                    java.lang.Object r0 = r0.receiver
                    android.media.AudioManager r0 = (android.media.AudioManager) r0
                    int r0 = r0.getRingerMode()
                    return r0
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.util.RingerModeTrackerImpl$ringerMode$1.invoke():int");
            }
        });
        this.ringerModeInternal = new RingerModeLiveData(broadcastDispatcher, executor, "android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION", new Function0<Integer>(audioManager) { // from class: com.android.systemui.util.RingerModeTrackerImpl$ringerModeInternal$1
            @Override // kotlin.jvm.internal.CallableReference
            public final String getName() {
                return "getRingerModeInternal";
            }

            @Override // kotlin.jvm.internal.CallableReference
            public final KDeclarationContainer getOwner() {
                return Reflection.getOrCreateKotlinClass(AudioManager.class);
            }

            @Override // kotlin.jvm.internal.CallableReference
            public final String getSignature() {
                return "getRingerModeInternal()I";
            }

            /* Return type fixed from 'int' to match base method */
            /* JADX WARN: Type inference failed for: r0v3, types: [int, java.lang.Integer] */
            /* JADX WARNING: Unknown variable types count: 1 */
            @Override // kotlin.jvm.functions.Function0
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public final java.lang.Integer invoke() {
                /*
                    r0 = this;
                    java.lang.Object r0 = r0.receiver
                    android.media.AudioManager r0 = (android.media.AudioManager) r0
                    int r0 = r0.getRingerModeInternal()
                    return r0
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.util.RingerModeTrackerImpl$ringerModeInternal$1.invoke():int");
            }
        });
    }

    @Override // com.android.systemui.util.RingerModeTracker
    @NotNull
    public LiveData<Integer> getRingerMode() {
        return this.ringerMode;
    }

    @Override // com.android.systemui.util.RingerModeTracker
    @NotNull
    public LiveData<Integer> getRingerModeInternal() {
        return this.ringerModeInternal;
    }
}
