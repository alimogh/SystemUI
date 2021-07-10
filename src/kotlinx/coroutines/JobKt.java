package kotlinx.coroutines;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public final class JobKt {
    @NotNull
    public static final CompletableJob Job(@Nullable Job job) {
        return JobKt__JobKt.Job(job);
    }

    @Nullable
    public static final Object cancelAndJoin(@NotNull Job job, @NotNull Continuation<? super Unit> continuation) {
        return JobKt__JobKt.cancelAndJoin(job, continuation);
    }
}
