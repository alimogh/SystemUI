package kotlinx.coroutines;

import java.util.concurrent.CancellationException;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.internal.StackTraceRecoveryKt;
import kotlinx.coroutines.internal.Symbol;
import kotlinx.coroutines.internal.ThreadContextKt;
import org.jetbrains.annotations.NotNull;
/* compiled from: Dispatched.kt */
public final class DispatchedKt {
    private static final Symbol UNDEFINED = new Symbol("UNDEFINED");

    private static final void resumeUnconfined(@NotNull DispatchedTask<?> dispatchedTask) {
        EventLoop eventLoop$kotlinx_coroutines_core = ThreadLocalEventLoop.INSTANCE.getEventLoop$kotlinx_coroutines_core();
        if (eventLoop$kotlinx_coroutines_core.isUnconfinedLoopActive()) {
            eventLoop$kotlinx_coroutines_core.dispatchUnconfined(dispatchedTask);
            return;
        }
        eventLoop$kotlinx_coroutines_core.incrementUseCount(true);
        try {
            resume(dispatchedTask, dispatchedTask.getDelegate$kotlinx_coroutines_core(), 3);
            do {
            } while (eventLoop$kotlinx_coroutines_core.processUnconfinedEvent());
        } catch (Throwable th) {
            eventLoop$kotlinx_coroutines_core.decrementUseCount(true);
            throw th;
        }
        eventLoop$kotlinx_coroutines_core.decrementUseCount(true);
    }

    public static final <T> void resumeCancellable(@NotNull Continuation<? super T> continuation, T t) {
        boolean z;
        Intrinsics.checkParameterIsNotNull(continuation, "$this$resumeCancellable");
        if (continuation instanceof DispatchedContinuation) {
            DispatchedContinuation dispatchedContinuation = (DispatchedContinuation) continuation;
            if (dispatchedContinuation.dispatcher.isDispatchNeeded(dispatchedContinuation.getContext())) {
                dispatchedContinuation._state = t;
                dispatchedContinuation.resumeMode = 1;
                dispatchedContinuation.dispatcher.dispatch(dispatchedContinuation.getContext(), dispatchedContinuation);
                return;
            }
            EventLoop eventLoop$kotlinx_coroutines_core = ThreadLocalEventLoop.INSTANCE.getEventLoop$kotlinx_coroutines_core();
            if (eventLoop$kotlinx_coroutines_core.isUnconfinedLoopActive()) {
                dispatchedContinuation._state = t;
                dispatchedContinuation.resumeMode = 1;
                eventLoop$kotlinx_coroutines_core.dispatchUnconfined(dispatchedContinuation);
                return;
            }
            eventLoop$kotlinx_coroutines_core.incrementUseCount(true);
            try {
                Job job = (Job) dispatchedContinuation.getContext().get(Job.Key);
                if (job == null || job.isActive()) {
                    z = false;
                } else {
                    CancellationException cancellationException = job.getCancellationException();
                    Result.Companion companion = Result.Companion;
                    Object createFailure = ResultKt.createFailure(cancellationException);
                    Result.m31constructorimpl(createFailure);
                    dispatchedContinuation.resumeWith(createFailure);
                    z = true;
                }
                if (!z) {
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
                }
                do {
                } while (eventLoop$kotlinx_coroutines_core.processUnconfinedEvent());
            } catch (Throwable th) {
                eventLoop$kotlinx_coroutines_core.decrementUseCount(true);
                throw th;
            }
            eventLoop$kotlinx_coroutines_core.decrementUseCount(true);
            return;
        }
        Result.Companion companion3 = Result.Companion;
        Result.m31constructorimpl(t);
        continuation.resumeWith(t);
    }

    public static final <T> void resumeCancellableWithException(@NotNull Continuation<? super T> continuation, @NotNull Throwable th) {
        Intrinsics.checkParameterIsNotNull(continuation, "$this$resumeCancellableWithException");
        Intrinsics.checkParameterIsNotNull(th, "exception");
        if (continuation instanceof DispatchedContinuation) {
            DispatchedContinuation dispatchedContinuation = (DispatchedContinuation) continuation;
            CoroutineContext context = dispatchedContinuation.continuation.getContext();
            boolean z = false;
            CompletedExceptionally completedExceptionally = new CompletedExceptionally(th, false, 2, null);
            if (dispatchedContinuation.dispatcher.isDispatchNeeded(context)) {
                dispatchedContinuation._state = new CompletedExceptionally(th, false, 2, null);
                dispatchedContinuation.resumeMode = 1;
                dispatchedContinuation.dispatcher.dispatch(context, dispatchedContinuation);
                return;
            }
            EventLoop eventLoop$kotlinx_coroutines_core = ThreadLocalEventLoop.INSTANCE.getEventLoop$kotlinx_coroutines_core();
            if (eventLoop$kotlinx_coroutines_core.isUnconfinedLoopActive()) {
                dispatchedContinuation._state = completedExceptionally;
                dispatchedContinuation.resumeMode = 1;
                eventLoop$kotlinx_coroutines_core.dispatchUnconfined(dispatchedContinuation);
                return;
            }
            eventLoop$kotlinx_coroutines_core.incrementUseCount(true);
            try {
                Job job = (Job) dispatchedContinuation.getContext().get(Job.Key);
                if (job != null && !job.isActive()) {
                    CancellationException cancellationException = job.getCancellationException();
                    Result.Companion companion = Result.Companion;
                    Object createFailure = ResultKt.createFailure(cancellationException);
                    Result.m31constructorimpl(createFailure);
                    dispatchedContinuation.resumeWith(createFailure);
                    z = true;
                }
                if (!z) {
                    CoroutineContext context2 = dispatchedContinuation.getContext();
                    Object updateThreadContext = ThreadContextKt.updateThreadContext(context2, dispatchedContinuation.countOrElement);
                    try {
                        Continuation<T> continuation2 = dispatchedContinuation.continuation;
                        Result.Companion companion2 = Result.Companion;
                        Object createFailure2 = ResultKt.createFailure(StackTraceRecoveryKt.recoverStackTrace(th, continuation2));
                        Result.m31constructorimpl(createFailure2);
                        continuation2.resumeWith(createFailure2);
                        Unit unit = Unit.INSTANCE;
                    } finally {
                        ThreadContextKt.restoreThreadContext(context2, updateThreadContext);
                    }
                }
                do {
                } while (eventLoop$kotlinx_coroutines_core.processUnconfinedEvent());
            } catch (Throwable th2) {
                eventLoop$kotlinx_coroutines_core.decrementUseCount(true);
                throw th2;
            }
            eventLoop$kotlinx_coroutines_core.decrementUseCount(true);
            return;
        }
        Result.Companion companion3 = Result.Companion;
        Object createFailure3 = ResultKt.createFailure(StackTraceRecoveryKt.recoverStackTrace(th, continuation));
        Result.m31constructorimpl(createFailure3);
        continuation.resumeWith(createFailure3);
    }

    public static final <T> void resumeDirect(@NotNull Continuation<? super T> continuation, T t) {
        Intrinsics.checkParameterIsNotNull(continuation, "$this$resumeDirect");
        if (continuation instanceof DispatchedContinuation) {
            Continuation<T> continuation2 = ((DispatchedContinuation) continuation).continuation;
            Result.Companion companion = Result.Companion;
            Result.m31constructorimpl(t);
            continuation2.resumeWith(t);
            return;
        }
        Result.Companion companion2 = Result.Companion;
        Result.m31constructorimpl(t);
        continuation.resumeWith(t);
    }

    public static final <T> void resumeDirectWithException(@NotNull Continuation<? super T> continuation, @NotNull Throwable th) {
        Intrinsics.checkParameterIsNotNull(continuation, "$this$resumeDirectWithException");
        Intrinsics.checkParameterIsNotNull(th, "exception");
        if (continuation instanceof DispatchedContinuation) {
            Continuation<T> continuation2 = ((DispatchedContinuation) continuation).continuation;
            Result.Companion companion = Result.Companion;
            Object createFailure = ResultKt.createFailure(StackTraceRecoveryKt.recoverStackTrace(th, continuation2));
            Result.m31constructorimpl(createFailure);
            continuation2.resumeWith(createFailure);
            return;
        }
        Result.Companion companion2 = Result.Companion;
        Object createFailure2 = ResultKt.createFailure(StackTraceRecoveryKt.recoverStackTrace(th, continuation));
        Result.m31constructorimpl(createFailure2);
        continuation.resumeWith(createFailure2);
    }

    public static final boolean yieldUndispatched(@NotNull DispatchedContinuation<? super Unit> dispatchedContinuation) {
        Intrinsics.checkParameterIsNotNull(dispatchedContinuation, "$this$yieldUndispatched");
        Unit unit = Unit.INSTANCE;
        EventLoop eventLoop$kotlinx_coroutines_core = ThreadLocalEventLoop.INSTANCE.getEventLoop$kotlinx_coroutines_core();
        if (eventLoop$kotlinx_coroutines_core.isUnconfinedQueueEmpty()) {
            return false;
        }
        if (eventLoop$kotlinx_coroutines_core.isUnconfinedLoopActive()) {
            dispatchedContinuation._state = unit;
            dispatchedContinuation.resumeMode = 1;
            eventLoop$kotlinx_coroutines_core.dispatchUnconfined(dispatchedContinuation);
            return true;
        }
        eventLoop$kotlinx_coroutines_core.incrementUseCount(true);
        try {
            dispatchedContinuation.run();
            do {
            } while (eventLoop$kotlinx_coroutines_core.processUnconfinedEvent());
        } catch (Throwable th) {
            eventLoop$kotlinx_coroutines_core.decrementUseCount(true);
            throw th;
        }
        eventLoop$kotlinx_coroutines_core.decrementUseCount(true);
        return false;
    }

    public static final <T> void dispatch(@NotNull DispatchedTask<? super T> dispatchedTask, int i) {
        Intrinsics.checkParameterIsNotNull(dispatchedTask, "$this$dispatch");
        Continuation<? super T> delegate$kotlinx_coroutines_core = dispatchedTask.getDelegate$kotlinx_coroutines_core();
        if (!ResumeModeKt.isDispatchedMode(i) || !(delegate$kotlinx_coroutines_core instanceof DispatchedContinuation) || ResumeModeKt.isCancellableMode(i) != ResumeModeKt.isCancellableMode(dispatchedTask.resumeMode)) {
            resume(dispatchedTask, delegate$kotlinx_coroutines_core, i);
            return;
        }
        CoroutineDispatcher coroutineDispatcher = ((DispatchedContinuation) delegate$kotlinx_coroutines_core).dispatcher;
        CoroutineContext context = delegate$kotlinx_coroutines_core.getContext();
        if (coroutineDispatcher.isDispatchNeeded(context)) {
            coroutineDispatcher.dispatch(context, dispatchedTask);
        } else {
            resumeUnconfined(dispatchedTask);
        }
    }

    public static final <T> void resume(@NotNull DispatchedTask<? super T> dispatchedTask, @NotNull Continuation<? super T> continuation, int i) {
        Intrinsics.checkParameterIsNotNull(dispatchedTask, "$this$resume");
        Intrinsics.checkParameterIsNotNull(continuation, "delegate");
        Object takeState$kotlinx_coroutines_core = dispatchedTask.takeState$kotlinx_coroutines_core();
        Throwable exceptionalResult$kotlinx_coroutines_core = dispatchedTask.getExceptionalResult$kotlinx_coroutines_core(takeState$kotlinx_coroutines_core);
        if (exceptionalResult$kotlinx_coroutines_core != null) {
            if (!(continuation instanceof DispatchedTask)) {
                exceptionalResult$kotlinx_coroutines_core = StackTraceRecoveryKt.recoverStackTrace(exceptionalResult$kotlinx_coroutines_core, continuation);
            }
            ResumeModeKt.resumeWithExceptionMode(continuation, exceptionalResult$kotlinx_coroutines_core, i);
            return;
        }
        ResumeModeKt.resumeMode(continuation, dispatchedTask.getSuccessfulResult$kotlinx_coroutines_core(takeState$kotlinx_coroutines_core), i);
    }
}
