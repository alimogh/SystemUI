package kotlin;

import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: LazyJVM.kt */
/* access modifiers changed from: package-private */
public class LazyKt__LazyJVMKt {
    @NotNull
    public static <T> Lazy<T> lazy(@NotNull Function0<? extends T> function0) {
        Intrinsics.checkParameterIsNotNull(function0, "initializer");
        return new SynchronizedLazyImpl(function0, null, 2, null);
    }
}
