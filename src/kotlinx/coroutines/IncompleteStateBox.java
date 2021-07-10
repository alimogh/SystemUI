package kotlinx.coroutines;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: JobSupport.kt */
final class IncompleteStateBox {
    public IncompleteStateBox(@NotNull Incomplete incomplete) {
        Intrinsics.checkParameterIsNotNull(incomplete, "state");
    }
}
