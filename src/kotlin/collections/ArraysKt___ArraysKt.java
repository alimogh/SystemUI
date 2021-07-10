package kotlin.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import kotlin.jvm.internal.ArrayIteratorKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt__SequencesKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: _Arrays.kt */
/* access modifiers changed from: package-private */
public class ArraysKt___ArraysKt extends ArraysKt___ArraysJvmKt {
    public static <T> boolean contains(@NotNull T[] tArr, T t) {
        Intrinsics.checkParameterIsNotNull(tArr, "$this$contains");
        return indexOf(tArr, t) >= 0;
    }

    public static final <T> int indexOf(@NotNull T[] tArr, T t) {
        Intrinsics.checkParameterIsNotNull(tArr, "$this$indexOf");
        int i = 0;
        if (t == null) {
            int length = tArr.length;
            while (i < length) {
                if (tArr[i] == null) {
                    return i;
                }
                i++;
            }
            return -1;
        }
        int length2 = tArr.length;
        while (i < length2) {
            if (Intrinsics.areEqual(t, tArr[i])) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public static char single(@NotNull char[] cArr) {
        Intrinsics.checkParameterIsNotNull(cArr, "$this$single");
        int length = cArr.length;
        if (length == 0) {
            throw new NoSuchElementException("Array is empty.");
        } else if (length == 1) {
            return cArr[0];
        } else {
            throw new IllegalArgumentException("Array has more than one element.");
        }
    }

    @Nullable
    public static <T> T singleOrNull(@NotNull T[] tArr) {
        Intrinsics.checkParameterIsNotNull(tArr, "$this$singleOrNull");
        if (tArr.length == 1) {
            return tArr[0];
        }
        return null;
    }

    @NotNull
    public static <T> List<T> filterNotNull(@NotNull T[] tArr) {
        Intrinsics.checkParameterIsNotNull(tArr, "$this$filterNotNull");
        ArrayList arrayList = new ArrayList();
        filterNotNullTo(tArr, arrayList);
        return arrayList;
    }

    @NotNull
    public static final <C extends Collection<? super T>, T> C filterNotNullTo(@NotNull T[] tArr, @NotNull C c) {
        Intrinsics.checkParameterIsNotNull(tArr, "$this$filterNotNullTo");
        Intrinsics.checkParameterIsNotNull(c, "destination");
        for (T t : tArr) {
            if (t != null) {
                c.add(t);
            }
        }
        return c;
    }

    @NotNull
    public static final <T> T[] sortedArrayWith(@NotNull T[] tArr, @NotNull Comparator<? super T> comparator) {
        Intrinsics.checkParameterIsNotNull(tArr, "$this$sortedArrayWith");
        Intrinsics.checkParameterIsNotNull(comparator, "comparator");
        if (tArr.length == 0) {
            return tArr;
        }
        T[] tArr2 = (T[]) Arrays.copyOf(tArr, tArr.length);
        Intrinsics.checkExpressionValueIsNotNull(tArr2, "java.util.Arrays.copyOf(this, size)");
        ArraysKt___ArraysJvmKt.sortWith(tArr2, comparator);
        return tArr2;
    }

    @NotNull
    public static <T> List<T> sortedWith(@NotNull T[] tArr, @NotNull Comparator<? super T> comparator) {
        Intrinsics.checkParameterIsNotNull(tArr, "$this$sortedWith");
        Intrinsics.checkParameterIsNotNull(comparator, "comparator");
        return ArraysKt___ArraysJvmKt.asList(sortedArrayWith(tArr, comparator));
    }

    @NotNull
    public static final <T, C extends Collection<? super T>> C toCollection(@NotNull T[] tArr, @NotNull C c) {
        Intrinsics.checkParameterIsNotNull(tArr, "$this$toCollection");
        Intrinsics.checkParameterIsNotNull(c, "destination");
        for (T t : tArr) {
            c.add(t);
        }
        return c;
    }

    @NotNull
    public static <T> List<T> toMutableList(@NotNull T[] tArr) {
        Intrinsics.checkParameterIsNotNull(tArr, "$this$toMutableList");
        return new ArrayList(CollectionsKt__CollectionsKt.asCollection(tArr));
    }

    @NotNull
    public static List<Integer> toMutableList(@NotNull int[] iArr) {
        Intrinsics.checkParameterIsNotNull(iArr, "$this$toMutableList");
        ArrayList arrayList = new ArrayList(iArr.length);
        for (int i : iArr) {
            arrayList.add(Integer.valueOf(i));
        }
        return arrayList;
    }

    @NotNull
    public static <T> Set<T> toSet(@NotNull T[] tArr) {
        Intrinsics.checkParameterIsNotNull(tArr, "$this$toSet");
        int length = tArr.length;
        if (length == 0) {
            return SetsKt__SetsKt.emptySet();
        }
        if (length == 1) {
            return SetsKt__SetsJVMKt.setOf(tArr[0]);
        }
        LinkedHashSet linkedHashSet = new LinkedHashSet(MapsKt__MapsKt.mapCapacity(tArr.length));
        toCollection(tArr, linkedHashSet);
        return linkedHashSet;
    }

    @NotNull
    public static <T> Sequence<T> asSequence(@NotNull T[] tArr) {
        Intrinsics.checkParameterIsNotNull(tArr, "$this$asSequence");
        if (tArr.length == 0) {
            return SequencesKt__SequencesKt.emptySequence();
        }
        return new Sequence<T>(tArr) { // from class: kotlin.collections.ArraysKt___ArraysKt$asSequence$$inlined$Sequence$1
            final /* synthetic */ Object[] $this_asSequence$inlined;

            {
                this.$this_asSequence$inlined = r1;
            }

            @Override // kotlin.sequences.Sequence
            @NotNull
            public Iterator<T> iterator() {
                return ArrayIteratorKt.iterator(this.$this_asSequence$inlined);
            }
        };
    }
}
