package kotlinx.coroutines;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: Exceptions.kt */
public final class CompletionHandlerException extends RuntimeException {
    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public CompletionHandlerException(@NotNull String str, @NotNull Throwable th) {
        super(str, th);
        Intrinsics.checkParameterIsNotNull(str, "message");
        Intrinsics.checkParameterIsNotNull(th, "cause");
    }
}
