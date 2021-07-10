package kotlinx.coroutines;

import org.jetbrains.annotations.NotNull;
/* compiled from: Job.kt */
public interface ChildHandle extends DisposableHandle {
    boolean childCancelled(@NotNull Throwable th);
}
