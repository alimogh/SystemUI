package kotlinx.coroutines;

import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: JobSupport.kt */
public final class ChildHandleNode extends JobCancellingNode<JobSupport> implements ChildHandle {
    @NotNull
    public final ChildJob childJob;

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Unit invoke(Throwable th) {
        invoke(th);
        return Unit.INSTANCE;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ChildHandleNode(@NotNull JobSupport jobSupport, @NotNull ChildJob childJob) {
        super(jobSupport);
        Intrinsics.checkParameterIsNotNull(jobSupport, "parent");
        Intrinsics.checkParameterIsNotNull(childJob, "childJob");
        this.childJob = childJob;
    }

    @Override // kotlinx.coroutines.CompletionHandlerBase
    public void invoke(@Nullable Throwable th) {
        this.childJob.parentCancelled((ParentJob) this.job);
    }

    @Override // kotlinx.coroutines.ChildHandle
    public boolean childCancelled(@NotNull Throwable th) {
        Intrinsics.checkParameterIsNotNull(th, "cause");
        return ((JobSupport) this.job).childCancelled(th);
    }

    @Override // kotlinx.coroutines.internal.LockFreeLinkedListNode
    @NotNull
    public String toString() {
        return "ChildHandle[" + this.childJob + ']';
    }
}
