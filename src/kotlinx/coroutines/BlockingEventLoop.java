package kotlinx.coroutines;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: EventLoop.kt */
public final class BlockingEventLoop extends EventLoopImplBase {
    @NotNull
    private final Thread thread;

    /* access modifiers changed from: protected */
    @Override // kotlinx.coroutines.EventLoopImplPlatform
    @NotNull
    public Thread getThread() {
        return this.thread;
    }

    public BlockingEventLoop(@NotNull Thread thread) {
        Intrinsics.checkParameterIsNotNull(thread, "thread");
        this.thread = thread;
    }
}
