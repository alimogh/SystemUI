package kotlinx.coroutines;

import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: JobSupport.kt */
public final class ChildContinuation extends JobCancellingNode<Job> {
    @NotNull
    public final CancellableContinuationImpl<?> child;

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Unit invoke(Throwable th) {
        invoke(th);
        return Unit.INSTANCE;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ChildContinuation(@NotNull Job job, @NotNull CancellableContinuationImpl<?> cancellableContinuationImpl) {
        super(job);
        Intrinsics.checkParameterIsNotNull(job, "parent");
        Intrinsics.checkParameterIsNotNull(cancellableContinuationImpl, "child");
        this.child = cancellableContinuationImpl;
    }

    @Override // kotlinx.coroutines.CompletionHandlerBase
    public void invoke(@Nullable Throwable th) {
        CancellableContinuationImpl<?> cancellableContinuationImpl = this.child;
        cancellableContinuationImpl.cancel(cancellableContinuationImpl.getContinuationCancellationCause(this.job));
    }

    @Override // kotlinx.coroutines.internal.LockFreeLinkedListNode
    @NotNull
    public String toString() {
        return "ChildContinuation[" + this.child + ']';
    }
}
