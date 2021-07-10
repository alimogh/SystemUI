package kotlinx.coroutines;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: EventLoop.kt */
public final class EventLoopKt {
    @NotNull
    public static final EventLoop createEventLoop() {
        Thread currentThread = Thread.currentThread();
        Intrinsics.checkExpressionValueIsNotNull(currentThread, "Thread.currentThread()");
        return new BlockingEventLoop(currentThread);
    }
}
