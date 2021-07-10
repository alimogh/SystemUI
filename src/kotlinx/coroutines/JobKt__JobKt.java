package kotlinx.coroutines;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.Job;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: Job.kt */
public final /* synthetic */ class JobKt__JobKt {
    @NotNull
    public static final CompletableJob Job(@Nullable Job job) {
        return new JobImpl(job);
    }

    public static /* synthetic */ CompletableJob Job$default(Job job, int i, Object obj) {
        if ((i & 1) != 0) {
            job = null;
        }
        return JobKt.Job(job);
    }

    @Nullable
    public static final Object cancelAndJoin(@NotNull Job job, @NotNull Continuation<? super Unit> continuation) {
        Job.DefaultImpls.cancel$default(job, null, 1, null);
        return job.join(continuation);
    }
}
