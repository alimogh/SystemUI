package kotlinx.coroutines;

import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.Job;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: JobSupport.kt */
public abstract class JobNode<J extends Job> extends CompletionHandlerBase implements DisposableHandle, Incomplete {
    @NotNull
    public final J job;

    @Override // kotlinx.coroutines.Incomplete
    @Nullable
    public NodeList getList() {
        return null;
    }

    @Override // kotlinx.coroutines.Incomplete
    public boolean isActive() {
        return true;
    }

    public JobNode(@NotNull J j) {
        Intrinsics.checkParameterIsNotNull(j, "job");
        this.job = j;
    }

    @Override // kotlinx.coroutines.DisposableHandle
    public void dispose() {
        J j = this.job;
        if (j != null) {
            ((JobSupport) j).removeNode$kotlinx_coroutines_core(this);
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlinx.coroutines.JobSupport");
    }
}
