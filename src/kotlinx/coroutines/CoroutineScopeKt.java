package kotlinx.coroutines;

import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.internal.ContextScope;
import org.jetbrains.annotations.NotNull;
/* compiled from: CoroutineScope.kt */
public final class CoroutineScopeKt {
    @NotNull
    public static final CoroutineScope CoroutineScope(@NotNull CoroutineContext coroutineContext) {
        Intrinsics.checkParameterIsNotNull(coroutineContext, "context");
        if (coroutineContext.get(Job.Key) == null) {
            coroutineContext = coroutineContext.plus(JobKt__JobKt.Job$default(null, 1, null));
        }
        return new ContextScope(coroutineContext);
    }
}
