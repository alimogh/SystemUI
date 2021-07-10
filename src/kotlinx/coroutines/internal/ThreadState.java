package kotlinx.coroutines.internal;

import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: ThreadContext.kt */
/* access modifiers changed from: package-private */
public final class ThreadState {
    private Object[] a;
    @NotNull
    private final CoroutineContext context;
    private int i;

    public ThreadState(@NotNull CoroutineContext coroutineContext, int i) {
        Intrinsics.checkParameterIsNotNull(coroutineContext, "context");
        this.context = coroutineContext;
        this.a = new Object[i];
    }

    @NotNull
    public final CoroutineContext getContext() {
        return this.context;
    }

    public final void append(@Nullable Object obj) {
        Object[] objArr = this.a;
        int i = this.i;
        this.i = i + 1;
        objArr[i] = obj;
    }

    @Nullable
    public final Object take() {
        Object[] objArr = this.a;
        int i = this.i;
        this.i = i + 1;
        return objArr[i];
    }

    public final void start() {
        this.i = 0;
    }
}
