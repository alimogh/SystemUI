package kotlinx.coroutines;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: CancellableContinuationImpl.kt */
final class CompletedWithCancellation {
    @NotNull
    public final Function1<Throwable, Unit> onCancellation;
    @Nullable
    public final Object result;

    @NotNull
    public String toString() {
        return "CompletedWithCancellation[" + this.result + ']';
    }
}
