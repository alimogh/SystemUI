package kotlin.collections;

import java.util.Collection;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: Collections.kt */
/* access modifiers changed from: package-private */
public class CollectionsKt__CollectionsKt extends CollectionsKt__CollectionsJVMKt {
    @NotNull
    public static final <T> Collection<T> asCollection(@NotNull T[] tArr) {
        Intrinsics.checkParameterIsNotNull(tArr, "$this$asCollection");
        return new ArrayAsCollection(tArr, false);
    }

    @NotNull
    public static <T> List<T> emptyList() {
        return EmptyList.INSTANCE;
    }

    @NotNull
    public static <T> List<T> listOf(@NotNull T... tArr) {
        Intrinsics.checkParameterIsNotNull(tArr, "elements");
        return tArr.length > 0 ? ArraysKt___ArraysJvmKt.asList(tArr) : emptyList();
    }

    public static <T> int getLastIndex(@NotNull List<? extends T> list) {
        Intrinsics.checkParameterIsNotNull(list, "$this$lastIndex");
        return list.size() - 1;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: java.util.List<? extends T> */
    /* JADX WARN: Multi-variable type inference failed */
    @NotNull
    public static <T> List<T> optimizeReadOnlyList(@NotNull List<? extends T> list) {
        Intrinsics.checkParameterIsNotNull(list, "$this$optimizeReadOnlyList");
        int size = list.size();
        if (size == 0) {
            return emptyList();
        }
        if (size != 1) {
            return list;
        }
        return CollectionsKt__CollectionsJVMKt.listOf(list.get(0));
    }

    public static void throwIndexOverflow() {
        throw new ArithmeticException("Index overflow has happened.");
    }
}
