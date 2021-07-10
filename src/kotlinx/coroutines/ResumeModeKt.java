package kotlinx.coroutines;

import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.internal.StackTraceRecoveryKt;
import kotlinx.coroutines.internal.ThreadContextKt;
import org.jetbrains.annotations.NotNull;
/* compiled from: ResumeMode.kt */
public final class ResumeModeKt {
    public static final boolean isCancellableMode(int i) {
        return i == 1;
    }

    public static final boolean isDispatchedMode(int i) {
        return i == 0 || i == 1;
    }

    public static final <T> void resumeMode(@NotNull Continuation<? super T> continuation, T t, int i) {
        Intrinsics.checkParameterIsNotNull(continuation, "$this$resumeMode");
        if (i == 0) {
            Result.Companion companion = Result.Companion;
            Result.m31constructorimpl(t);
            continuation.resumeWith(t);
        } else if (i == 1) {
            DispatchedKt.resumeCancellable(continuation, t);
        } else if (i == 2) {
            DispatchedKt.resumeDirect(continuation, t);
        } else if (i == 3) {
            DispatchedContinuation dispatchedContinuation = (DispatchedContinuation) continuation;
            CoroutineContext context = dispatchedContinuation.getContext();
            Object updateThreadContext = ThreadContextKt.updateThreadContext(context, dispatchedContinuation.countOrElement);
            try {
                Continuation<T> continuation2 = dispatchedContinuation.continuation;
                Result.Companion companion2 = Result.Companion;
                Result.m31constructorimpl(t);
                continuation2.resumeWith(t);
                Unit unit = Unit.INSTANCE;
            } finally {
                ThreadContextKt.restoreThreadContext(context, updateThreadContext);
            }
        } else if (i != 4) {
            throw new IllegalStateException(("Invalid mode " + i).toString());
        }
    }

    public static final <T> void resumeWithExceptionMode(@NotNull Continuation<? super T> continuation, @NotNull Throwable th, int i) {
        Intrinsics.checkParameterIsNotNull(continuation, "$this$resumeWithExceptionMode");
        Intrinsics.checkParameterIsNotNull(th, "exception");
        if (i == 0) {
            Result.Companion companion = Result.Companion;
            Object createFailure = ResultKt.createFailure(th);
            Result.m31constructorimpl(createFailure);
            continuation.resumeWith(createFailure);
        } else if (i == 1) {
            DispatchedKt.resumeCancellableWithException(continuation, th);
        } else if (i == 2) {
            DispatchedKt.resumeDirectWithException(continuation, th);
        } else if (i == 3) {
            DispatchedContinuation dispatchedContinuation = (DispatchedContinuation) continuation;
            CoroutineContext context = dispatchedContinuation.getContext();
            Object updateThreadContext = ThreadContextKt.updateThreadContext(context, dispatchedContinuation.countOrElement);
            try {
                Continuation<T> continuation2 = dispatchedContinuation.continuation;
                Result.Companion companion2 = Result.Companion;
                Object createFailure2 = ResultKt.createFailure(StackTraceRecoveryKt.recoverStackTrace(th, continuation2));
                Result.m31constructorimpl(createFailure2);
                continuation2.resumeWith(createFailure2);
                Unit unit = Unit.INSTANCE;
            } finally {
                ThreadContextKt.restoreThreadContext(context, updateThreadContext);
            }
        } else if (i != 4) {
            throw new IllegalStateException(("Invalid mode " + i).toString());
        }
    }
}
