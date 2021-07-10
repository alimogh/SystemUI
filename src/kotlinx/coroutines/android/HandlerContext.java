package kotlinx.coroutines.android;

import android.os.Handler;
import android.os.Looper;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: HandlerDispatcher.kt */
public final class HandlerContext extends HandlerDispatcher {
    private volatile HandlerContext _immediate;
    private final Handler handler;
    private final boolean invokeImmediately;
    private final String name;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    private HandlerContext(Handler handler, String str, boolean z) {
        super(null);
        HandlerContext handlerContext = null;
        this.handler = handler;
        this.name = str;
        this.invokeImmediately = z;
        this._immediate = z ? this : handlerContext;
        if (this._immediate == null) {
            this._immediate = new HandlerContext(this.handler, this.name, true);
        }
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public HandlerContext(@NotNull Handler handler, @Nullable String str) {
        this(handler, str, false);
        Intrinsics.checkParameterIsNotNull(handler, "handler");
    }

    @Override // kotlinx.coroutines.CoroutineDispatcher
    public boolean isDispatchNeeded(@NotNull CoroutineContext coroutineContext) {
        Intrinsics.checkParameterIsNotNull(coroutineContext, "context");
        return !this.invokeImmediately || (Intrinsics.areEqual(Looper.myLooper(), this.handler.getLooper()) ^ true);
    }

    @Override // kotlinx.coroutines.CoroutineDispatcher
    public void dispatch(@NotNull CoroutineContext coroutineContext, @NotNull Runnable runnable) {
        Intrinsics.checkParameterIsNotNull(coroutineContext, "context");
        Intrinsics.checkParameterIsNotNull(runnable, "block");
        this.handler.post(runnable);
    }

    @Override // kotlinx.coroutines.CoroutineDispatcher, java.lang.Object
    @NotNull
    public String toString() {
        String str = this.name;
        if (str == null) {
            String handler = this.handler.toString();
            Intrinsics.checkExpressionValueIsNotNull(handler, "handler.toString()");
            return handler;
        } else if (!this.invokeImmediately) {
            return str;
        } else {
            return this.name + " [immediate]";
        }
    }

    public boolean equals(@Nullable Object obj) {
        return (obj instanceof HandlerContext) && ((HandlerContext) obj).handler == this.handler;
    }

    public int hashCode() {
        return System.identityHashCode(this.handler);
    }
}
