package kotlin;

import kotlin.Result;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: Result.kt */
public final class ResultKt {
    @NotNull
    public static final Object createFailure(@NotNull Throwable th) {
        Intrinsics.checkParameterIsNotNull(th, "exception");
        return new Result.Failure(th);
    }

    public static final void throwOnFailure(@NotNull Object obj) {
        if (obj instanceof Result.Failure) {
            throw ((Result.Failure) obj).exception;
        }
    }
}
