package kotlinx.coroutines;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: JobSupport.kt */
final class InvokeOnCompletion extends JobNode<Job> {
    private final Function1<Throwable, Unit> handler;

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Unit invoke(Throwable th) {
        invoke(th);
        return Unit.INSTANCE;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: kotlin.jvm.functions.Function1<? super java.lang.Throwable, kotlin.Unit> */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public InvokeOnCompletion(@NotNull Job job, @NotNull Function1<? super Throwable, Unit> function1) {
        super(job);
        Intrinsics.checkParameterIsNotNull(job, "job");
        Intrinsics.checkParameterIsNotNull(function1, "handler");
        this.handler = function1;
    }

    @Override // kotlinx.coroutines.CompletionHandlerBase
    public void invoke(@Nullable Throwable th) {
        this.handler.invoke(th);
    }

    @Override // kotlinx.coroutines.internal.LockFreeLinkedListNode
    @NotNull
    public String toString() {
        return "InvokeOnCompletion[" + DebugStringsKt.getClassSimpleName(this) + '@' + DebugStringsKt.getHexAddress(this) + ']';
    }
}
