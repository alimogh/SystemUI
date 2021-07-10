package kotlinx.coroutines;

import kotlin.Result;
import kotlin.ResultKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: CompletedExceptionally.kt */
public final class CompletedExceptionallyKt {
    @Nullable
    public static final <T> Object toState(@NotNull Object obj) {
        if (Result.m36isSuccessimpl(obj)) {
            ResultKt.throwOnFailure(obj);
            return obj;
        }
        Throwable r4 = Result.m33exceptionOrNullimpl(obj);
        if (r4 != null) {
            return new CompletedExceptionally(r4, false, 2, null);
        }
        Intrinsics.throwNpe();
        throw null;
    }
}
