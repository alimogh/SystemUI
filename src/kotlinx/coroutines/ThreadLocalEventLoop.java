package kotlinx.coroutines;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: EventLoop.common.kt */
public final class ThreadLocalEventLoop {
    public static final ThreadLocalEventLoop INSTANCE = new ThreadLocalEventLoop();
    private static final ThreadLocal<EventLoop> ref = new ThreadLocal<>();

    private ThreadLocalEventLoop() {
    }

    @NotNull
    public final EventLoop getEventLoop$kotlinx_coroutines_core() {
        EventLoop eventLoop = ref.get();
        if (eventLoop != null) {
            return eventLoop;
        }
        EventLoop createEventLoop = EventLoopKt.createEventLoop();
        ref.set(createEventLoop);
        return createEventLoop;
    }

    public final void resetEventLoop$kotlinx_coroutines_core() {
        ref.set(null);
    }

    public final void setEventLoop$kotlinx_coroutines_core(@NotNull EventLoop eventLoop) {
        Intrinsics.checkParameterIsNotNull(eventLoop, "eventLoop");
        ref.set(eventLoop);
    }
}
