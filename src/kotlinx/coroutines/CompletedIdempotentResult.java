package kotlinx.coroutines;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: CancellableContinuationImpl.kt */
final class CompletedIdempotentResult {
    @Nullable
    public final Object result;

    @NotNull
    public String toString() {
        return "CompletedIdempotentResult[" + this.result + ']';
    }
}
