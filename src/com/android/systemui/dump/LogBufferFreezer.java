package com.android.systemui.dump;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.util.Log;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.util.concurrent.TimeUnit;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: LogBufferFreezer.kt */
public final class LogBufferFreezer {
    private final DumpManager dumpManager;
    private final DelayableExecutor executor;
    private final long freezeDuration;
    private Runnable pendingToken;

    public LogBufferFreezer(@NotNull DumpManager dumpManager, @NotNull DelayableExecutor delayableExecutor, long j) {
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        Intrinsics.checkParameterIsNotNull(delayableExecutor, "executor");
        this.dumpManager = dumpManager;
        this.executor = delayableExecutor;
        this.freezeDuration = j;
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public LogBufferFreezer(@NotNull DumpManager dumpManager, @NotNull DelayableExecutor delayableExecutor) {
        this(dumpManager, delayableExecutor, TimeUnit.MINUTES.toMillis(5));
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        Intrinsics.checkParameterIsNotNull(delayableExecutor, "executor");
    }

    public final void attach(@NotNull BroadcastDispatcher broadcastDispatcher) {
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        LogBufferFreezer$attach$1 logBufferFreezer$attach$1 = new BroadcastReceiver(this) { // from class: com.android.systemui.dump.LogBufferFreezer$attach$1
            final /* synthetic */ LogBufferFreezer this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(@Nullable Context context, @Nullable Intent intent) {
                this.this$0.onBugreportStarted();
            }
        };
        IntentFilter intentFilter = new IntentFilter("com.android.internal.intent.action.BUGREPORT_STARTED");
        DelayableExecutor delayableExecutor = this.executor;
        UserHandle userHandle = UserHandle.ALL;
        Intrinsics.checkExpressionValueIsNotNull(userHandle, "UserHandle.ALL");
        broadcastDispatcher.registerReceiver(logBufferFreezer$attach$1, intentFilter, delayableExecutor, userHandle);
    }

    /* access modifiers changed from: private */
    public final void onBugreportStarted() {
        Runnable runnable = this.pendingToken;
        if (runnable != null) {
            runnable.run();
        }
        Log.i("LogBufferFreezer", "Freezing log buffers");
        this.dumpManager.freezeBuffers();
        this.pendingToken = this.executor.executeDelayed(new Runnable(this) { // from class: com.android.systemui.dump.LogBufferFreezer$onBugreportStarted$1
            final /* synthetic */ LogBufferFreezer this$0;

            {
                this.this$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                Log.i("LogBufferFreezer", "Unfreezing log buffers");
                this.this$0.pendingToken = null;
                this.this$0.dumpManager.unfreezeBuffers();
            }
        }, this.freezeDuration);
    }
}
