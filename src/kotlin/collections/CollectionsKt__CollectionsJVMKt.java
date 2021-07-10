package kotlin.collections;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: CollectionsJVM.kt */
/* access modifiers changed from: package-private */
public class CollectionsKt__CollectionsJVMKt {
    @NotNull
    public static <T> List<T> listOf(T t) {
        List<T> singletonList = Collections.singletonList(t);
        Intrinsics.checkExpressionValueIsNotNull(singletonList, "java.util.Collections.singletonList(element)");
        return singletonList;
    }

    @NotNull
    public static final <T> Object[] copyToArrayOfAny(@NotNull T[] tArr, boolean z) {
        Intrinsics.checkParameterIsNotNull(tArr, "$this$copyToArrayOfAny");
        if (z && Intrinsics.areEqual(tArr.getClass(), Object[].class)) {
            return tArr;
        }
        Object[] copyOf = Arrays.copyOf(tArr, tArr.length, Object[].class);
        Intrinsics.checkExpressionValueIsNotNull(copyOf, "java.util.Arrays.copyOf(… Array<Any?>::class.java)");
        return copyOf;
    }
}
