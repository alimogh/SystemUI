package kotlinx.coroutines;

import kotlin.Result;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: JobSupport.kt */
final class ResumeOnCompletion extends JobNode<Job> {
    private final Continuation<Unit> continuation;

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Unit invoke(Throwable th) {
        invoke(th);
        return Unit.INSTANCE;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: kotlin.coroutines.Continuation<? super kotlin.Unit> */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ResumeOnCompletion(@NotNull Job job, @NotNull Continuation<? super Unit> continuation) {
        super(job);
        Intrinsics.checkParameterIsNotNull(job, "job");
        Intrinsics.checkParameterIsNotNull(continuation, "continuation");
        this.continuation = continuation;
    }

    @Override // kotlinx.coroutines.CompletionHandlerBase
    public void invoke(@Nullable Throwable th) {
        Continuation<Unit> continuation = this.continuation;
        Unit unit = Unit.INSTANCE;
        Result.Companion companion = Result.Companion;
        Result.m31constructorimpl(unit);
        continuation.resumeWith(unit);
    }

    @Override // kotlinx.coroutines.internal.LockFreeLinkedListNode
    @NotNull
    public String toString() {
        return "ResumeOnCompletion[" + this.continuation + ']';
    }
}
