package kotlin.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;
import kotlin.TypeCastException;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.TypeIntrinsics;
import kotlin.sequences.Sequence;
import org.jetbrains.annotations.NotNull;
/* compiled from: MutableCollections.kt */
/* access modifiers changed from: package-private */
public class CollectionsKt__MutableCollectionsKt extends CollectionsKt__MutableCollectionsJVMKt {
    public static <T> boolean addAll(@NotNull Collection<? super T> collection, @NotNull Iterable<? extends T> iterable) {
        Intrinsics.checkParameterIsNotNull(collection, "$this$addAll");
        Intrinsics.checkParameterIsNotNull(iterable, "elements");
        if (iterable instanceof Collection) {
            return collection.addAll((Collection) iterable);
        }
        boolean z = false;
        Iterator<? extends T> it = iterable.iterator();
        while (it.hasNext()) {
            if (collection.add((Object) it.next())) {
                z = true;
            }
        }
        return z;
    }

    public static <T> boolean addAll(@NotNull Collection<? super T> collection, @NotNull Sequence<? extends T> sequence) {
        Intrinsics.checkParameterIsNotNull(collection, "$this$addAll");
        Intrinsics.checkParameterIsNotNull(sequence, "elements");
        Iterator<? extends T> it = sequence.iterator();
        boolean z = false;
        while (it.hasNext()) {
            if (collection.add((Object) it.next())) {
                z = true;
            }
        }
        return z;
    }

    public static <T> boolean removeAll(@NotNull Iterable<? extends T> iterable, @NotNull Function1<? super T, Boolean> function1) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$removeAll");
        Intrinsics.checkParameterIsNotNull(function1, "predicate");
        return filterInPlace$CollectionsKt__MutableCollectionsKt((Iterable) iterable, (Function1) function1, true);
    }

    private static final <T> boolean filterInPlace$CollectionsKt__MutableCollectionsKt(@NotNull Iterable<? extends T> iterable, Function1<? super T, Boolean> function1, boolean z) {
        Iterator<? extends T> it = iterable.iterator();
        boolean z2 = false;
        while (it.hasNext()) {
            if (function1.invoke((Object) it.next()).booleanValue() == z) {
                it.remove();
                z2 = true;
            }
        }
        return z2;
    }

    public static <T> boolean removeAll(@NotNull List<T> list, @NotNull Function1<? super T, Boolean> function1) {
        Intrinsics.checkParameterIsNotNull(list, "$this$removeAll");
        Intrinsics.checkParameterIsNotNull(function1, "predicate");
        return filterInPlace$CollectionsKt__MutableCollectionsKt((List) list, (Function1) function1, true);
    }

    private static final <T> boolean filterInPlace$CollectionsKt__MutableCollectionsKt(@NotNull List<T> list, Function1<? super T, Boolean> function1, boolean z) {
        int i;
        if (list instanceof RandomAccess) {
            int i2 = CollectionsKt__CollectionsKt.getLastIndex(list);
            if (i2 >= 0) {
                int i3 = 0;
                i = 0;
                while (true) {
                    T t = list.get(i3);
                    if (function1.invoke(t).booleanValue() != z) {
                        if (i != i3) {
                            list.set(i, t);
                        }
                        i++;
                    }
                    if (i3 == i2) {
                        break;
                    }
                    i3++;
                }
            } else {
                i = 0;
            }
            if (i >= list.size()) {
                return false;
            }
            int i4 = CollectionsKt__CollectionsKt.getLastIndex(list);
            if (i4 < i) {
                return true;
            }
            while (true) {
                list.remove(i4);
                if (i4 == i) {
                    return true;
                }
                i4--;
            }
        } else if (list != null) {
            return filterInPlace$CollectionsKt__MutableCollectionsKt(TypeIntrinsics.asMutableIterable(list), function1, z);
        } else {
            throw new TypeCastException("null cannot be cast to non-null type kotlin.collections.MutableIterable<T>");
        }
    }

    public static final <T> boolean removeAll(@NotNull Collection<? super T> collection, @NotNull Iterable<? extends T> iterable) {
        Intrinsics.checkParameterIsNotNull(collection, "$this$removeAll");
        Intrinsics.checkParameterIsNotNull(iterable, "elements");
        return TypeIntrinsics.asMutableCollection(collection).removeAll(CollectionsKt__IterablesKt.convertToSetForSetOperationWith(iterable, collection));
    }

    public static final <T> boolean retainAll(@NotNull Collection<? super T> collection, @NotNull Iterable<? extends T> iterable) {
        Intrinsics.checkParameterIsNotNull(collection, "$this$retainAll");
        Intrinsics.checkParameterIsNotNull(iterable, "elements");
        return TypeIntrinsics.asMutableCollection(collection).retainAll(CollectionsKt__IterablesKt.convertToSetForSetOperationWith(iterable, collection));
    }
}
