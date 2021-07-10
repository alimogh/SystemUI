package kotlinx.coroutines;

import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.internal.ArrayQueue;
import org.jetbrains.annotations.NotNull;
/* compiled from: EventLoop.common.kt */
public abstract class EventLoop extends CoroutineDispatcher {
    private boolean shared;
    private ArrayQueue<DispatchedTask<?>> unconfinedQueue;
    private long useCount;

    private final long delta(boolean z) {
        return z ? 4294967296L : 1;
    }

    public void shutdown() {
    }

    public long getNextTime() {
        ArrayQueue<DispatchedTask<?>> arrayQueue = this.unconfinedQueue;
        if (arrayQueue == null || arrayQueue.isEmpty()) {
            return Long.MAX_VALUE;
        }
        return 0;
    }

    public final boolean processUnconfinedEvent() {
        DispatchedTask<?> removeFirstOrNull;
        ArrayQueue<DispatchedTask<?>> arrayQueue = this.unconfinedQueue;
        if (arrayQueue == null || (removeFirstOrNull = arrayQueue.removeFirstOrNull()) == null) {
            return false;
        }
        removeFirstOrNull.run();
        return true;
    }

    public final void dispatchUnconfined(@NotNull DispatchedTask<?> dispatchedTask) {
        Intrinsics.checkParameterIsNotNull(dispatchedTask, "task");
        ArrayQueue<DispatchedTask<?>> arrayQueue = this.unconfinedQueue;
        if (arrayQueue == null) {
            arrayQueue = new ArrayQueue<>();
            this.unconfinedQueue = arrayQueue;
        }
        arrayQueue.addLast(dispatchedTask);
    }

    public final boolean isUnconfinedLoopActive() {
        return this.useCount >= delta(true);
    }

    public final boolean isUnconfinedQueueEmpty() {
        ArrayQueue<DispatchedTask<?>> arrayQueue = this.unconfinedQueue;
        if (arrayQueue != null) {
            return arrayQueue.isEmpty();
        }
        return true;
    }

    public static /* synthetic */ void incrementUseCount$default(EventLoop eventLoop, boolean z, int i, Object obj) {
        if (obj == null) {
            if ((i & 1) != 0) {
                z = false;
            }
            eventLoop.incrementUseCount(z);
            return;
        }
        throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: incrementUseCount");
    }

    public final void incrementUseCount(boolean z) {
        this.useCount += delta(z);
        if (!z) {
            this.shared = true;
        }
    }

    public final void decrementUseCount(boolean z) {
        long delta = this.useCount - delta(z);
        this.useCount = delta;
        if (delta <= 0) {
            if (DebugKt.getASSERTIONS_ENABLED()) {
                if (!(this.useCount == 0)) {
                    throw new AssertionError();
                }
            }
            if (this.shared) {
                shutdown();
            }
        }
    }
}
