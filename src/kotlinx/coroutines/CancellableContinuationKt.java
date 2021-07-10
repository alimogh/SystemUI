package kotlinx.coroutines;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: CancellableContinuation.kt */
public final class CancellableContinuationKt {
    public static final void disposeOnCancellation(@NotNull CancellableContinuation<?> cancellableContinuation, @NotNull DisposableHandle disposableHandle) {
        Intrinsics.checkParameterIsNotNull(cancellableContinuation, "$this$disposeOnCancellation");
        Intrinsics.checkParameterIsNotNull(disposableHandle, "handle");
        cancellableContinuation.invokeOnCancellation(new DisposeOnCancel(disposableHandle));
    }
}
