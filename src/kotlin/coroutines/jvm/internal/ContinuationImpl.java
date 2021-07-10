package kotlin.coroutines.jvm.internal;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.ContinuationInterceptor;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: ContinuationImpl.kt */
public abstract class ContinuationImpl extends BaseContinuationImpl {
    private final CoroutineContext _context;
    private transient Continuation<Object> intercepted;

    public ContinuationImpl(@Nullable Continuation<Object> continuation, @Nullable CoroutineContext coroutineContext) {
        super(continuation);
        this._context = coroutineContext;
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public ContinuationImpl(@Nullable Continuation<Object> continuation) {
        this(continuation, continuation != null ? continuation.getContext() : null);
    }

    @Override // kotlin.coroutines.Continuation
    @NotNull
    public CoroutineContext getContext() {
        CoroutineContext coroutineContext = this._context;
        if (coroutineContext != null) {
            return coroutineContext;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    @NotNull
    public final Continuation<Object> intercepted() {
        Continuation<Object> continuation = this.intercepted;
        if (continuation == null) {
            ContinuationInterceptor continuationInterceptor = (ContinuationInterceptor) getContext().get(ContinuationInterceptor.Key);
            if (continuationInterceptor == null || (continuation = continuationInterceptor.interceptContinuation(this)) == null) {
                continuation = this;
            }
            this.intercepted = continuation;
        }
        return continuation;
    }

    /* access modifiers changed from: protected */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public void releaseIntercepted() {
        Continuation<?> continuation = this.intercepted;
        if (!(continuation == null || continuation == this)) {
            CoroutineContext.Element element = getContext().get(ContinuationInterceptor.Key);
            if (element != null) {
                ((ContinuationInterceptor) element).releaseInterceptedContinuation(continuation);
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }
        this.intercepted = CompletedContinuation.INSTANCE;
    }
}
