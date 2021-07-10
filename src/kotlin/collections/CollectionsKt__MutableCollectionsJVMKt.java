package kotlin.collections;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: MutableCollectionsJVM.kt */
/* access modifiers changed from: package-private */
public class CollectionsKt__MutableCollectionsJVMKt extends CollectionsKt__IteratorsKt {
    public static <T> void sortWith(@NotNull List<T> list, @NotNull Comparator<? super T> comparator) {
        Intrinsics.checkParameterIsNotNull(list, "$this$sortWith");
        Intrinsics.checkParameterIsNotNull(comparator, "comparator");
        if (list.size() > 1) {
            Collections.sort(list, comparator);
        }
    }
}
