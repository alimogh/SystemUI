package kotlin.coroutines;

import kotlin.Result;
import kotlin.Unit;
import kotlin.coroutines.intrinsics.IntrinsicsKt__IntrinsicsJvmKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: Continuation.kt */
public final class ContinuationKt {
    public static final <R, T> void startCoroutine(@NotNull Function2<? super R, ? super Continuation<? super T>, ? extends Object> function2, R r, @NotNull Continuation<? super T> continuation) {
        Intrinsics.checkParameterIsNotNull(function2, "$this$startCoroutine");
        Intrinsics.checkParameterIsNotNull(continuation, "completion");
        Continuation continuation2 = IntrinsicsKt__IntrinsicsJvmKt.intercepted(IntrinsicsKt__IntrinsicsJvmKt.createCoroutineUnintercepted(function2, r, continuation));
        Unit unit = Unit.INSTANCE;
        Result.Companion companion = Result.Companion;
        Result.m31constructorimpl(unit);
        continuation2.resumeWith(unit);
    }
}
