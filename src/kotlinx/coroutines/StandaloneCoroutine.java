package kotlinx.coroutines;

import kotlin.Unit;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: Builders.common.kt */
/* access modifiers changed from: package-private */
public class StandaloneCoroutine extends AbstractCoroutine<Unit> {
    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public StandaloneCoroutine(@NotNull CoroutineContext coroutineContext, boolean z) {
        super(coroutineContext, z);
        Intrinsics.checkParameterIsNotNull(coroutineContext, "parentContext");
    }

    /* access modifiers changed from: protected */
    @Override // kotlinx.coroutines.JobSupport
    public boolean handleJobException(@NotNull Throwable th) {
        Intrinsics.checkParameterIsNotNull(th, "exception");
        CoroutineExceptionHandlerKt.handleCoroutineException(getContext(), th);
        return true;
    }
}
