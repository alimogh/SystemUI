package com.android.systemui.util;

import android.content.IntentFilter;
import android.os.UserHandle;
import androidx.lifecycle.MutableLiveData;
import com.android.systemui.broadcast.BroadcastDispatcher;
import java.util.concurrent.Executor;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: RingerModeTrackerImpl.kt */
public final class RingerModeLiveData extends MutableLiveData<Integer> {
    private final BroadcastDispatcher broadcastDispatcher;
    private final Executor executor;
    private final IntentFilter filter;
    private final Function0<Integer> getter;
    private boolean initialSticky;
    private final RingerModeLiveData$receiver$1 receiver = new RingerModeLiveData$receiver$1(this);

    public RingerModeLiveData(@NotNull BroadcastDispatcher broadcastDispatcher, @NotNull Executor executor, @NotNull String str, @NotNull Function0<Integer> function0) {
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(executor, "executor");
        Intrinsics.checkParameterIsNotNull(str, "intent");
        Intrinsics.checkParameterIsNotNull(function0, "getter");
        this.broadcastDispatcher = broadcastDispatcher;
        this.executor = executor;
        this.getter = function0;
        this.filter = new IntentFilter(str);
    }

    public final boolean getInitialSticky() {
        return this.initialSticky;
    }

    @Override // androidx.lifecycle.LiveData
    @NotNull
    public Integer getValue() {
        Integer num = (Integer) super.getValue();
        return Integer.valueOf(num != null ? num.intValue() : -1);
    }

    /* access modifiers changed from: protected */
    @Override // androidx.lifecycle.LiveData
    public void onActive() {
        super.onActive();
        BroadcastDispatcher broadcastDispatcher = this.broadcastDispatcher;
        RingerModeLiveData$receiver$1 ringerModeLiveData$receiver$1 = this.receiver;
        IntentFilter intentFilter = this.filter;
        Executor executor = this.executor;
        UserHandle userHandle = UserHandle.ALL;
        Intrinsics.checkExpressionValueIsNotNull(userHandle, "UserHandle.ALL");
        broadcastDispatcher.registerReceiver(ringerModeLiveData$receiver$1, intentFilter, executor, userHandle);
        this.executor.execute(new Runnable(this) { // from class: com.android.systemui.util.RingerModeLiveData$onActive$1
            final /* synthetic */ RingerModeLiveData this$0;

            {
                this.this$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                RingerModeLiveData ringerModeLiveData = this.this$0;
                ringerModeLiveData.postValue(RingerModeLiveData.access$getGetter$p(ringerModeLiveData).invoke());
            }
        });
    }

    /* access modifiers changed from: protected */
    @Override // androidx.lifecycle.LiveData
    public void onInactive() {
        super.onInactive();
        this.broadcastDispatcher.unregisterReceiver(this.receiver);
    }
}
