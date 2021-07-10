package kotlinx.coroutines;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
/* compiled from: CancellableContinuation.kt */
public interface CancellableContinuation<T> extends Continuation<T> {
    void invokeOnCancellation(@NotNull Function1<? super Throwable, Unit> function1);
}
