package kotlin.collections;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: _ArraysJvm.kt */
public class ArraysKt___ArraysJvmKt extends ArraysKt__ArraysKt {
    @NotNull
    public static <T> List<T> asList(@NotNull T[] tArr) {
        Intrinsics.checkParameterIsNotNull(tArr, "$this$asList");
        List<T> asList = ArraysUtilJVM.asList(tArr);
        Intrinsics.checkExpressionValueIsNotNull(asList, "ArraysUtilJVM.asList(this)");
        return asList;
    }

    public static /* synthetic */ Object[] copyInto$default(Object[] objArr, Object[] objArr2, int i, int i2, int i3, int i4, Object obj) {
        if ((i4 & 2) != 0) {
            i = 0;
        }
        if ((i4 & 4) != 0) {
            i2 = 0;
        }
        if ((i4 & 8) != 0) {
            i3 = objArr.length;
        }
        copyInto(objArr, objArr2, i, i2, i3);
        return objArr2;
    }

    @NotNull
    public static final <T> T[] copyInto(@NotNull T[] tArr, @NotNull T[] tArr2, int i, int i2, int i3) {
        Intrinsics.checkParameterIsNotNull(tArr, "$this$copyInto");
        Intrinsics.checkParameterIsNotNull(tArr2, "destination");
        System.arraycopy(tArr, i2, tArr2, i, i3 - i2);
        return tArr2;
    }

    public static final <T> void sortWith(@NotNull T[] tArr, @NotNull Comparator<? super T> comparator) {
        Intrinsics.checkParameterIsNotNull(tArr, "$this$sortWith");
        Intrinsics.checkParameterIsNotNull(comparator, "comparator");
        if (tArr.length > 1) {
            Arrays.sort(tArr, comparator);
        }
    }
}
