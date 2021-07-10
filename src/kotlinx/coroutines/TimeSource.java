package kotlinx.coroutines;

import org.jetbrains.annotations.NotNull;
/* compiled from: TimeSource.kt */
public interface TimeSource {
    long nanoTime();

    void parkNanos(@NotNull Object obj, long j);

    void registerTimeLoopThread();

    void trackTask();

    void unTrackTask();

    void unpark(@NotNull Thread thread);

    void unregisterTimeLoopThread();

    @NotNull
    Runnable wrapTask(@NotNull Runnable runnable);
}
