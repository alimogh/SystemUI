package kotlinx.coroutines;

import kotlinx.coroutines.internal.Symbol;
import org.jetbrains.annotations.Nullable;
/* compiled from: JobSupport.kt */
public final class JobSupportKt {
    private static final Empty EMPTY_ACTIVE = new Empty(true);
    private static final Empty EMPTY_NEW = new Empty(false);
    private static final Symbol SEALED = new Symbol("SEALED");

    @Nullable
    public static final Object boxIncomplete(@Nullable Object obj) {
        return obj instanceof Incomplete ? new IncompleteStateBox((Incomplete) obj) : obj;
    }
}
