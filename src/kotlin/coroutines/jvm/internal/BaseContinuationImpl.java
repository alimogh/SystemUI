package kotlin.coroutines.jvm.internal;

import java.io.Serializable;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt__IntrinsicsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: ContinuationImpl.kt */
public abstract class BaseContinuationImpl implements Continuation<Object>, CoroutineStackFrame, Serializable {
    @Nullable
    private final Continuation<Object> completion;

    /* access modifiers changed from: protected */
    @Nullable
    public abstract Object invokeSuspend(@NotNull Object obj);

    /* access modifiers changed from: protected */
    public void releaseIntercepted() {
    }

    public BaseContinuationImpl(@Nullable Continuation<Object> continuation) {
        this.completion = continuation;
    }

    @Nullable
    public final Continuation<Object> getCompletion() {
        return this.completion;
    }

    @Override // kotlin.coroutines.Continuation
    public final void resumeWith(@NotNull Object obj) {
        while (true) {
            DebugProbesKt.probeCoroutineResumed(this);
            Continuation<Object> continuation = this.completion;
            if (continuation != null) {
                try {
                    obj = this.invokeSuspend(obj);
                    if (obj != IntrinsicsKt__IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
                        Result.Companion companion = Result.Companion;
                        Result.m31constructorimpl(obj);
                        this.releaseIntercepted();
                        if (continuation instanceof BaseContinuationImpl) {
                            this = (BaseContinuationImpl) continuation;
                        } else {
                            continuation.resumeWith(obj);
                            return;
                        }
                    } else {
                        return;
                    }
                } catch (Throwable th) {
                    Result.Companion companion2 = Result.Companion;
                    obj = ResultKt.createFailure(th);
                    Result.m31constructorimpl(obj);
                }
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }
    }

    @NotNull
    public Continuation<Unit> create(@Nullable Object obj, @NotNull Continuation<?> continuation) {
        Intrinsics.checkParameterIsNotNull(continuation, "completion");
        throw new UnsupportedOperationException("create(Any?;Continuation) has not been overridden");
    }

    @Override // java.lang.Object
    @NotNull
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Continuation at ");
        Object stackTraceElement = getStackTraceElement();
        if (stackTraceElement == null) {
            stackTraceElement = getClass().getName();
        }
        sb.append(stackTraceElement);
        return sb.toString();
    }

    @Override // kotlin.coroutines.jvm.internal.CoroutineStackFrame
    @Nullable
    public CoroutineStackFrame getCallerFrame() {
        Continuation<Object> continuation = this.completion;
        if (!(continuation instanceof CoroutineStackFrame)) {
            continuation = null;
        }
        return (CoroutineStackFrame) continuation;
    }

    @Override // kotlin.coroutines.jvm.internal.CoroutineStackFrame
    @Nullable
    public StackTraceElement getStackTraceElement() {
        return DebugMetadataKt.getStackTraceElement(this);
    }
}
