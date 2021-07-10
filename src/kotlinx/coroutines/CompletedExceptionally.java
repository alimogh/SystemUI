package kotlinx.coroutines;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: CompletedExceptionally.kt */
public class CompletedExceptionally {
    private static final AtomicIntegerFieldUpdater _handled$FU = AtomicIntegerFieldUpdater.newUpdater(CompletedExceptionally.class, "_handled");
    private volatile int _handled;
    @NotNull
    public final Throwable cause;

    public CompletedExceptionally(@NotNull Throwable th, boolean z) {
        Intrinsics.checkParameterIsNotNull(th, "cause");
        this.cause = th;
        this._handled = z ? 1 : 0;
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ CompletedExceptionally(Throwable th, boolean z, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this(th, (i & 2) != 0 ? false : z);
    }

    /* JADX WARN: Type inference failed for: r0v1, types: [boolean, int] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final boolean getHandled() {
        /*
            r0 = this;
            int r0 = r0._handled
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: kotlinx.coroutines.CompletedExceptionally.getHandled():boolean");
    }

    public final boolean makeHandled() {
        return _handled$FU.compareAndSet(this, 0, 1);
    }

    @NotNull
    public String toString() {
        return DebugStringsKt.getClassSimpleName(this) + '[' + this.cause + ']';
    }
}
