package kotlin;

import kotlin.internal.PlatformImplementationsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: Exceptions.kt */
/* access modifiers changed from: package-private */
public class ExceptionsKt__ExceptionsKt {
    public static void addSuppressed(@NotNull Throwable th, @NotNull Throwable th2) {
        Intrinsics.checkParameterIsNotNull(th, "$this$addSuppressed");
        Intrinsics.checkParameterIsNotNull(th2, "exception");
        PlatformImplementationsKt.IMPLEMENTATIONS.addSuppressed(th, th2);
    }
}
