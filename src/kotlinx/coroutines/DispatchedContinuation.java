package kotlinx.coroutines;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.jvm.internal.CoroutineStackFrame;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.internal.ThreadContextKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: Dispatched.kt */
public final class DispatchedContinuation<T> extends DispatchedTask<T> implements CoroutineStackFrame, Continuation<T> {
    @Nullable
    public Object _state = DispatchedKt.UNDEFINED;
    @Nullable
    private final CoroutineStackFrame callerFrame;
    @NotNull
    public final Continuation<T> continuation;
    @NotNull
    public final Object countOrElement;
    @NotNull
    public final CoroutineDispatcher dispatcher;

    @Override // kotlin.coroutines.Continuation
    @NotNull
    public CoroutineContext getContext() {
        return this.continuation.getContext();
    }

    @Override // kotlinx.coroutines.DispatchedTask
    @NotNull
    public Continuation<T> getDelegate$kotlinx_coroutines_core() {
        return this;
    }

    @Override // kotlin.coroutines.jvm.internal.CoroutineStackFrame
    @Nullable
    public StackTraceElement getStackTraceElement() {
        return null;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: kotlin.coroutines.Continuation<? super T> */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public DispatchedContinuation(@NotNull CoroutineDispatcher coroutineDispatcher, @NotNull Continuation<? super T> continuation) {
        super(0);
        Intrinsics.checkParameterIsNotNull(coroutineDispatcher, "dispatcher");
        Intrinsics.checkParameterIsNotNull(continuation, "continuation");
        this.dispatcher = coroutineDispatcher;
        this.continuation = continuation;
        Continuation<T> continuation2 = this.continuation;
        this.callerFrame = (CoroutineStackFrame) (!(continuation2 instanceof CoroutineStackFrame) ? null : continuation2);
        this.countOrElement = ThreadContextKt.threadContextElements(getContext());
    }

    @Override // kotlin.coroutines.jvm.internal.CoroutineStackFrame
    @Nullable
    public CoroutineStackFrame getCallerFrame() {
        return this.callerFrame;
    }

    @Override // kotlinx.coroutines.DispatchedTask
    @Nullable
    public Object takeState$kotlinx_coroutines_core() {
        Object obj = this._state;
        if (DebugKt.getASSERTIONS_ENABLED()) {
            if (!(obj != DispatchedKt.UNDEFINED)) {
                throw new AssertionError();
            }
        }
        this._state = DispatchedKt.UNDEFINED;
        return obj;
    }

    @Override // kotlin.coroutines.Continuation
    public void resumeWith(@NotNull Object obj) {
        CoroutineContext context = this.continuation.getContext();
        Object state = CompletedExceptionallyKt.toState(obj);
        if (this.dispatcher.isDispatchNeeded(context)) {
            this._state = state;
            this.resumeMode = 0;
            this.dispatcher.dispatch(context, this);
            return;
        }
        EventLoop eventLoop$kotlinx_coroutines_core = ThreadLocalEventLoop.INSTANCE.getEventLoop$kotlinx_coroutines_core();
        if (eventLoop$kotlinx_coroutines_core.isUnconfinedLoopActive()) {
            this._state = state;
            this.resumeMode = 0;
            eventLoop$kotlinx_coroutines_core.dispatchUnconfined(this);
            return;
        }
        eventLoop$kotlinx_coroutines_core.incrementUseCount(true);
        try {
            CoroutineContext context2 = getContext();
            Object updateThreadContext = ThreadContextKt.updateThreadContext(context2, this.countOrElement);
            try {
                this.continuation.resumeWith(obj);
                Unit unit = Unit.INSTANCE;
                do {
                } while (eventLoop$kotlinx_coroutines_core.processUnconfinedEvent());
            } finally {
                ThreadContextKt.restoreThreadContext(context2, updateThreadContext);
            }
        } catch (Throwable th) {
            eventLoop$kotlinx_coroutines_core.decrementUseCount(true);
            throw th;
        }
        eventLoop$kotlinx_coroutines_core.decrementUseCount(true);
    }

    public final void dispatchYield$kotlinx_coroutines_core(T t) {
        CoroutineContext context = this.continuation.getContext();
        this._state = t;
        this.resumeMode = 1;
        this.dispatcher.dispatchYield(context, this);
    }

    @Override // java.lang.Object
    @NotNull
    public String toString() {
        return "DispatchedContinuation[" + this.dispatcher + ", " + DebugStringsKt.toDebugString(this.continuation) + ']';
    }
}
