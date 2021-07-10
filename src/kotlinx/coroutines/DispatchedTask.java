package kotlinx.coroutines;

import java.util.concurrent.CancellationException;
import kotlin.ExceptionsKt__ExceptionsKt;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.internal.StackTraceRecoveryKt;
import kotlinx.coroutines.internal.ThreadContextKt;
import kotlinx.coroutines.scheduling.Task;
import kotlinx.coroutines.scheduling.TaskContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: Dispatched.kt */
public abstract class DispatchedTask<T> extends Task {
    public int resumeMode;

    public void cancelResult$kotlinx_coroutines_core(@Nullable Object obj, @NotNull Throwable th) {
        Intrinsics.checkParameterIsNotNull(th, "cause");
    }

    @NotNull
    public abstract Continuation<T> getDelegate$kotlinx_coroutines_core();

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    public <T> T getSuccessfulResult$kotlinx_coroutines_core(@Nullable Object obj) {
        return obj;
    }

    @Nullable
    public abstract Object takeState$kotlinx_coroutines_core();

    public final void handleFatalException$kotlinx_coroutines_core(@Nullable Throwable th, @Nullable Throwable th2) {
        if (th != null || th2 != null) {
            if (!(th == null || th2 == null)) {
                ExceptionsKt__ExceptionsKt.addSuppressed(th, th2);
            }
            if (th == null) {
                th = th2;
            }
            String str = "Fatal exception in coroutines machinery for " + this + ". Please read KDoc to 'handleFatalException' method and report this incident to maintainers";
            if (th != null) {
                CoroutineExceptionHandlerKt.handleCoroutineException(getDelegate$kotlinx_coroutines_core().getContext(), new CoroutinesInternalError(str, th));
                return;
            }
            Intrinsics.throwNpe();
            throw null;
        }
    }

    public DispatchedTask(int i) {
        this.resumeMode = i;
    }

    @Nullable
    public final Throwable getExceptionalResult$kotlinx_coroutines_core(@Nullable Object obj) {
        if (!(obj instanceof CompletedExceptionally)) {
            obj = null;
        }
        CompletedExceptionally completedExceptionally = (CompletedExceptionally) obj;
        if (completedExceptionally != null) {
            return completedExceptionally.cause;
        }
        return null;
    }

    @Override // java.lang.Runnable
    public final void run() {
        Object obj;
        Object obj2;
        TaskContext taskContext = this.taskContext;
        try {
            Continuation<T> delegate$kotlinx_coroutines_core = getDelegate$kotlinx_coroutines_core();
            if (delegate$kotlinx_coroutines_core != null) {
                DispatchedContinuation dispatchedContinuation = (DispatchedContinuation) delegate$kotlinx_coroutines_core;
                Continuation<T> continuation = dispatchedContinuation.continuation;
                CoroutineContext context = continuation.getContext();
                Object takeState$kotlinx_coroutines_core = takeState$kotlinx_coroutines_core();
                Object updateThreadContext = ThreadContextKt.updateThreadContext(context, dispatchedContinuation.countOrElement);
                try {
                    Throwable exceptionalResult$kotlinx_coroutines_core = getExceptionalResult$kotlinx_coroutines_core(takeState$kotlinx_coroutines_core);
                    Job job = ResumeModeKt.isCancellableMode(this.resumeMode) ? (Job) context.get(Job.Key) : null;
                    if (exceptionalResult$kotlinx_coroutines_core == null && job != null && !job.isActive()) {
                        CancellationException cancellationException = job.getCancellationException();
                        cancelResult$kotlinx_coroutines_core(takeState$kotlinx_coroutines_core, cancellationException);
                        Result.Companion companion = Result.Companion;
                        Object createFailure = ResultKt.createFailure(StackTraceRecoveryKt.recoverStackTrace(cancellationException, continuation));
                        Result.m31constructorimpl(createFailure);
                        continuation.resumeWith(createFailure);
                    } else if (exceptionalResult$kotlinx_coroutines_core != null) {
                        Result.Companion companion2 = Result.Companion;
                        Object createFailure2 = ResultKt.createFailure(StackTraceRecoveryKt.recoverStackTrace(exceptionalResult$kotlinx_coroutines_core, continuation));
                        Result.m31constructorimpl(createFailure2);
                        continuation.resumeWith(createFailure2);
                    } else {
                        T successfulResult$kotlinx_coroutines_core = getSuccessfulResult$kotlinx_coroutines_core(takeState$kotlinx_coroutines_core);
                        Result.Companion companion3 = Result.Companion;
                        Result.m31constructorimpl(successfulResult$kotlinx_coroutines_core);
                        continuation.resumeWith(successfulResult$kotlinx_coroutines_core);
                    }
                    Unit unit = Unit.INSTANCE;
                    try {
                        Result.Companion companion4 = Result.Companion;
                        taskContext.afterTask();
                        obj2 = Unit.INSTANCE;
                        Result.m31constructorimpl(obj2);
                    } catch (Throwable th) {
                        Result.Companion companion5 = Result.Companion;
                        obj2 = ResultKt.createFailure(th);
                        Result.m31constructorimpl(obj2);
                    }
                    handleFatalException$kotlinx_coroutines_core(null, Result.m33exceptionOrNullimpl(obj2));
                    return;
                } finally {
                    ThreadContextKt.restoreThreadContext(context, updateThreadContext);
                }
            } else {
                throw new TypeCastException("null cannot be cast to non-null type kotlinx.coroutines.DispatchedContinuation<T>");
            }
        } catch (Throwable th2) {
            Result.Companion companion6 = Result.Companion;
            obj = ResultKt.createFailure(th2);
            Result.m31constructorimpl(obj);
        }
        handleFatalException$kotlinx_coroutines_core(th, Result.m33exceptionOrNullimpl(obj));
    }
}
