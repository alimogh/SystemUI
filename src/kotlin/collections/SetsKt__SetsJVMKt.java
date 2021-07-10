package kotlin.collections;

import java.util.Collections;
import java.util.Set;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: SetsJVM.kt */
/* access modifiers changed from: package-private */
public class SetsKt__SetsJVMKt {
    @NotNull
    public static final <T> Set<T> setOf(T t) {
        Set<T> singleton = Collections.singleton(t);
        Intrinsics.checkExpressionValueIsNotNull(singleton, "java.util.Collections.singleton(element)");
        return singleton;
    }
}
