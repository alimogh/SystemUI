package kotlinx.coroutines;

import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: CancellableContinuation.kt */
final class DisposeOnCancel extends CancelHandler {
    private final DisposableHandle handle;

    public DisposeOnCancel(@NotNull DisposableHandle disposableHandle) {
        Intrinsics.checkParameterIsNotNull(disposableHandle, "handle");
        this.handle = disposableHandle;
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Unit invoke(Throwable th) {
        invoke(th);
        return Unit.INSTANCE;
    }

    @Override // kotlinx.coroutines.CancelHandlerBase
    public void invoke(@Nullable Throwable th) {
        this.handle.dispose();
    }

    @NotNull
    public String toString() {
        return "DisposeOnCancel[" + this.handle + ']';
    }
}
