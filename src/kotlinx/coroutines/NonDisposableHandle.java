package kotlinx.coroutines;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: Job.kt */
public final class NonDisposableHandle implements DisposableHandle, ChildHandle {
    public static final NonDisposableHandle INSTANCE = new NonDisposableHandle();

    @Override // kotlinx.coroutines.ChildHandle
    public boolean childCancelled(@NotNull Throwable th) {
        Intrinsics.checkParameterIsNotNull(th, "cause");
        return false;
    }

    @Override // kotlinx.coroutines.DisposableHandle
    public void dispose() {
    }

    @NotNull
    public String toString() {
        return "NonDisposableHandle";
    }

    private NonDisposableHandle() {
    }
}
