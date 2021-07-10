package kotlinx.coroutines.internal;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: Concurrent.kt */
public final class ConcurrentKt {
    @NotNull
    public static final <E> Set<E> identitySet(int i) {
        Set<E> newSetFromMap = Collections.newSetFromMap(new IdentityHashMap(i));
        Intrinsics.checkExpressionValueIsNotNull(newSetFromMap, "Collections.newSetFromMa…ityHashMap(expectedSize))");
        return newSetFromMap;
    }

    static {
        try {
            ScheduledThreadPoolExecutor.class.getMethod("setRemoveOnCancelPolicy", Boolean.TYPE);
        } catch (Throwable unused) {
        }
    }
}
