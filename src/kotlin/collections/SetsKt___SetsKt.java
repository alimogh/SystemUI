package kotlin.collections;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: _Sets.kt */
/* access modifiers changed from: package-private */
public class SetsKt___SetsKt extends SetsKt__SetsKt {
    @NotNull
    public static <T> Set<T> minus(@NotNull Set<? extends T> set, @NotNull Iterable<? extends T> iterable) {
        Intrinsics.checkParameterIsNotNull(set, "$this$minus");
        Intrinsics.checkParameterIsNotNull(iterable, "elements");
        Collection<?> convertToSetForSetOperationWith = CollectionsKt__IterablesKt.convertToSetForSetOperationWith(iterable, set);
        if (convertToSetForSetOperationWith.isEmpty()) {
            return CollectionsKt___CollectionsKt.toSet(set);
        }
        if (convertToSetForSetOperationWith instanceof Set) {
            LinkedHashSet linkedHashSet = new LinkedHashSet();
            for (T t : set) {
                if (!convertToSetForSetOperationWith.contains(t)) {
                    linkedHashSet.add(t);
                }
            }
            return linkedHashSet;
        }
        LinkedHashSet linkedHashSet2 = new LinkedHashSet(set);
        linkedHashSet2.removeAll(convertToSetForSetOperationWith);
        return linkedHashSet2;
    }
}
