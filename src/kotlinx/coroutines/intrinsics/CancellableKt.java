package kotlinx.coroutines.intrinsics;

import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt__IntrinsicsJvmKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.DispatchedKt;
import org.jetbrains.annotations.NotNull;
/* compiled from: Cancellable.kt */
public final class CancellableKt {
    public static final <R, T> void startCoroutineCancellable(@NotNull Function2<? super R, ? super Continuation<? super T>, ? extends Object> function2, R r, @NotNull Continuation<? super T> continuation) {
        Intrinsics.checkParameterIsNotNull(function2, "$this$startCoroutineCancellable");
        Intrinsics.checkParameterIsNotNull(continuation, "completion");
        try {
            DispatchedKt.resumeCancellable(IntrinsicsKt__IntrinsicsJvmKt.intercepted(IntrinsicsKt__IntrinsicsJvmKt.createCoroutineUnintercepted(function2, r, continuation)), Unit.INSTANCE);
        } catch (Throwable th) {
            Result.Companion companion = Result.Companion;
            Object createFailure = ResultKt.createFailure(th);
            Result.m31constructorimpl(createFailure);
            continuation.resumeWith(createFailure);
        }
    }
}
