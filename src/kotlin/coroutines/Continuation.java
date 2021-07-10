package kotlin.coroutines;

import org.jetbrains.annotations.NotNull;
/* compiled from: Continuation.kt */
public interface Continuation<T> {
    @NotNull
    CoroutineContext getContext();

    void resumeWith(@NotNull Object obj);
}
