package kotlinx.coroutines.intrinsics;

import kotlin.Result;
import kotlin.ResultKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.intrinsics.IntrinsicsKt__IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugProbesKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.TypeIntrinsics;
import kotlinx.coroutines.internal.ThreadContextKt;
import org.jetbrains.annotations.NotNull;
/* compiled from: Undispatched.kt */
public final class UndispatchedKt {
    /* JADX INFO: finally extract failed */
    public static final <R, T> void startCoroutineUndispatched(@NotNull Function2<? super R, ? super Continuation<? super T>, ? extends Object> function2, R r, @NotNull Continuation<? super T> continuation) {
        Intrinsics.checkParameterIsNotNull(function2, "$this$startCoroutineUndispatched");
        Intrinsics.checkParameterIsNotNull(continuation, "completion");
        DebugProbesKt.probeCoroutineCreated(continuation);
        try {
            CoroutineContext context = continuation.getContext();
            Object updateThreadContext = ThreadContextKt.updateThreadContext(context, null);
            try {
                TypeIntrinsics.beforeCheckcastToFunctionOfArity(function2, 2);
                Object invoke = function2.invoke(r, continuation);
                ThreadContextKt.restoreThreadContext(context, updateThreadContext);
                if (invoke != IntrinsicsKt__IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
                    Result.Companion companion = Result.Companion;
                    Result.m31constructorimpl(invoke);
                    continuation.resumeWith(invoke);
                }
            } catch (Throwable th) {
                ThreadContextKt.restoreThreadContext(context, updateThreadContext);
                throw th;
            }
        } catch (Throwable th2) {
            Result.Companion companion2 = Result.Companion;
            Object createFailure = ResultKt.createFailure(th2);
            Result.m31constructorimpl(createFailure);
            continuation.resumeWith(createFailure);
        }
    }
}
