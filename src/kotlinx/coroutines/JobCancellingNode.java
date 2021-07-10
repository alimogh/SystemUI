package kotlinx.coroutines;

import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.Job;
import org.jetbrains.annotations.NotNull;
/* compiled from: JobSupport.kt */
public abstract class JobCancellingNode<J extends Job> extends JobNode<J> {
    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public JobCancellingNode(@NotNull J j) {
        super(j);
        Intrinsics.checkParameterIsNotNull(j, "job");
    }
}
