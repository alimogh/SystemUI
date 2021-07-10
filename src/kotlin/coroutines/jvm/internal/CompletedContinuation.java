package kotlin.coroutines.jvm.internal;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import org.jetbrains.annotations.NotNull;
/* compiled from: ContinuationImpl.kt */
public final class CompletedContinuation implements Continuation<Object> {
    public static final CompletedContinuation INSTANCE = new CompletedContinuation();

    @NotNull
    public String toString() {
        return "This continuation is already complete";
    }

    private CompletedContinuation() {
    }

    @Override // kotlin.coroutines.Continuation
    @NotNull
    public CoroutineContext getContext() {
        throw new IllegalStateException("This continuation is already complete".toString());
    }

    @Override // kotlin.coroutines.Continuation
    public void resumeWith(@NotNull Object obj) {
        throw new IllegalStateException("This continuation is already complete".toString());
    }
}
