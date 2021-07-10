package kotlin.collections;

import java.util.Set;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: Sets.kt */
/* access modifiers changed from: package-private */
public class SetsKt__SetsKt extends SetsKt__SetsJVMKt {
    @NotNull
    public static <T> Set<T> emptySet() {
        return EmptySet.INSTANCE;
    }

    @NotNull
    public static <T> Set<T> setOf(@NotNull T... tArr) {
        Intrinsics.checkParameterIsNotNull(tArr, "elements");
        return tArr.length > 0 ? ArraysKt___ArraysKt.toSet(tArr) : emptySet();
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: java.util.Set<? extends T> */
    /* JADX WARN: Multi-variable type inference failed */
    @NotNull
    public static final <T> Set<T> optimizeReadOnlySet(@NotNull Set<? extends T> set) {
        Intrinsics.checkParameterIsNotNull(set, "$this$optimizeReadOnlySet");
        int size = set.size();
        if (size == 0) {
            return emptySet();
        }
        if (size != 1) {
            return set;
        }
        return SetsKt__SetsJVMKt.setOf(set.iterator().next());
    }
}
