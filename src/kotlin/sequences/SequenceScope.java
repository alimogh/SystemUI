package kotlin.sequences;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: SequenceBuilder.kt */
public abstract class SequenceScope<T> {
    @Nullable
    public abstract Object yield(T t, @NotNull Continuation<? super Unit> continuation);
}
