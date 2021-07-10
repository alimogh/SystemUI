package kotlin.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: Iterables.kt */
/* access modifiers changed from: package-private */
public class CollectionsKt__IterablesKt extends CollectionsKt__CollectionsKt {
    public static <T> int collectionSizeOrDefault(@NotNull Iterable<? extends T> iterable, int i) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$collectionSizeOrDefault");
        return iterable instanceof Collection ? ((Collection) iterable).size() : i;
    }

    private static final <T> boolean safeToConvertToSet$CollectionsKt__IterablesKt(@NotNull Collection<? extends T> collection) {
        return collection.size() > 2 && (collection instanceof ArrayList);
    }

    @NotNull
    public static final <T> Collection<T> convertToSetForSetOperationWith(@NotNull Iterable<? extends T> iterable, @NotNull Iterable<? extends T> iterable2) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$convertToSetForSetOperationWith");
        Intrinsics.checkParameterIsNotNull(iterable2, "source");
        if (iterable instanceof Set) {
            return (Collection) iterable;
        }
        if (!(iterable instanceof Collection)) {
            return CollectionsKt___CollectionsKt.toHashSet(iterable);
        }
        if ((iterable2 instanceof Collection) && ((Collection) iterable2).size() < 2) {
            return (Collection) iterable;
        }
        Collection<T> collection = (Collection) iterable;
        if (safeToConvertToSet$CollectionsKt__IterablesKt(collection)) {
            return CollectionsKt___CollectionsKt.toHashSet(iterable);
        }
        return collection;
    }
}
