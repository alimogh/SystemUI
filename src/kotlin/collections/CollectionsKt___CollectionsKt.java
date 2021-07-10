package kotlin.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Set;
import kotlin.TypeCastException;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.text.StringsKt__StringBuilderKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: _Collections.kt */
public class CollectionsKt___CollectionsKt extends CollectionsKt___CollectionsJvmKt {
    public static <T> boolean contains(@NotNull Iterable<? extends T> iterable, T t) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$contains");
        if (iterable instanceof Collection) {
            return ((Collection) iterable).contains(t);
        }
        return indexOf(iterable, t) >= 0;
    }

    public static final <T> T first(@NotNull Iterable<? extends T> iterable) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$first");
        if (iterable instanceof List) {
            return (T) first((List<? extends Object>) ((List) iterable));
        }
        Iterator<? extends T> it = iterable.iterator();
        if (it.hasNext()) {
            return (T) it.next();
        }
        throw new NoSuchElementException("Collection is empty.");
    }

    public static final <T> T first(@NotNull List<? extends T> list) {
        Intrinsics.checkParameterIsNotNull(list, "$this$first");
        if (!list.isEmpty()) {
            return (T) list.get(0);
        }
        throw new NoSuchElementException("List is empty.");
    }

    public static final <T> int indexOf(@NotNull Iterable<? extends T> iterable, T t) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$indexOf");
        if (iterable instanceof List) {
            return ((List) iterable).indexOf(t);
        }
        int i = 0;
        for (Object obj : iterable) {
            if (i < 0) {
                CollectionsKt.throwIndexOverflow();
                throw null;
            } else if (Intrinsics.areEqual(t, obj)) {
                return i;
            } else {
                i++;
            }
        }
        return -1;
    }

    public static final <T> T last(@NotNull Iterable<? extends T> iterable) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$last");
        if (iterable instanceof List) {
            return (T) last((List<? extends Object>) ((List) iterable));
        }
        Iterator<? extends T> it = iterable.iterator();
        if (it.hasNext()) {
            T t = (T) it.next();
            while (it.hasNext()) {
                t = (T) it.next();
            }
            return t;
        }
        throw new NoSuchElementException("Collection is empty.");
    }

    public static final <T> T last(@NotNull List<? extends T> list) {
        Intrinsics.checkParameterIsNotNull(list, "$this$last");
        if (!list.isEmpty()) {
            return (T) list.get(CollectionsKt__CollectionsKt.getLastIndex(list));
        }
        throw new NoSuchElementException("List is empty.");
    }

    public static <T> T single(@NotNull Iterable<? extends T> iterable) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$single");
        if (iterable instanceof List) {
            return (T) single((List<? extends Object>) ((List) iterable));
        }
        Iterator<? extends T> it = iterable.iterator();
        if (it.hasNext()) {
            T t = (T) it.next();
            if (!it.hasNext()) {
                return t;
            }
            throw new IllegalArgumentException("Collection has more than one element.");
        }
        throw new NoSuchElementException("Collection is empty.");
    }

    public static final <T> T single(@NotNull List<? extends T> list) {
        Intrinsics.checkParameterIsNotNull(list, "$this$single");
        int size = list.size();
        if (size == 0) {
            throw new NoSuchElementException("List is empty.");
        } else if (size == 1) {
            return (T) list.get(0);
        } else {
            throw new IllegalArgumentException("List has more than one element.");
        }
    }

    @NotNull
    public static <T> List<T> drop(@NotNull Iterable<? extends T> iterable, int i) {
        ArrayList arrayList;
        Intrinsics.checkParameterIsNotNull(iterable, "$this$drop");
        int i2 = 0;
        if (!(i >= 0)) {
            throw new IllegalArgumentException(("Requested element count " + i + " is less than zero.").toString());
        } else if (i == 0) {
            return toList(iterable);
        } else {
            if (iterable instanceof Collection) {
                Collection collection = (Collection) iterable;
                int size = collection.size() - i;
                if (size <= 0) {
                    return CollectionsKt__CollectionsKt.emptyList();
                }
                if (size == 1) {
                    return CollectionsKt__CollectionsJVMKt.listOf(last(iterable));
                }
                arrayList = new ArrayList(size);
                if (iterable instanceof List) {
                    if (iterable instanceof RandomAccess) {
                        int size2 = collection.size();
                        while (i < size2) {
                            arrayList.add(((List) iterable).get(i));
                            i++;
                        }
                    } else {
                        ListIterator listIterator = ((List) iterable).listIterator(i);
                        while (listIterator.hasNext()) {
                            arrayList.add(listIterator.next());
                        }
                    }
                    return arrayList;
                }
            } else {
                arrayList = new ArrayList();
            }
            for (Object obj : iterable) {
                if (i2 >= i) {
                    arrayList.add(obj);
                } else {
                    i2++;
                }
            }
            return CollectionsKt__CollectionsKt.optimizeReadOnlyList(arrayList);
        }
    }

    @NotNull
    public static <T> List<T> take(@NotNull Iterable<? extends T> iterable, int i) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$take");
        int i2 = 0;
        if (!(i >= 0)) {
            throw new IllegalArgumentException(("Requested element count " + i + " is less than zero.").toString());
        } else if (i == 0) {
            return CollectionsKt__CollectionsKt.emptyList();
        } else {
            if (iterable instanceof Collection) {
                if (i >= ((Collection) iterable).size()) {
                    return toList(iterable);
                }
                if (i == 1) {
                    return CollectionsKt__CollectionsJVMKt.listOf(first(iterable));
                }
            }
            ArrayList arrayList = new ArrayList(i);
            Iterator<? extends T> it = iterable.iterator();
            while (it.hasNext()) {
                arrayList.add(it.next());
                i2++;
                if (i2 == i) {
                    break;
                }
            }
            return CollectionsKt__CollectionsKt.optimizeReadOnlyList(arrayList);
        }
    }

    @NotNull
    public static <T> List<T> takeLast(@NotNull List<? extends T> list, int i) {
        Intrinsics.checkParameterIsNotNull(list, "$this$takeLast");
        if (!(i >= 0)) {
            throw new IllegalArgumentException(("Requested element count " + i + " is less than zero.").toString());
        } else if (i == 0) {
            return CollectionsKt__CollectionsKt.emptyList();
        } else {
            int size = list.size();
            if (i >= size) {
                return toList(list);
            }
            if (i == 1) {
                return CollectionsKt__CollectionsJVMKt.listOf(last((List<? extends Object>) list));
            }
            ArrayList arrayList = new ArrayList(i);
            if (list instanceof RandomAccess) {
                for (int i2 = size - i; i2 < size; i2++) {
                    arrayList.add(list.get(i2));
                }
            } else {
                ListIterator<? extends T> listIterator = list.listIterator(size - i);
                while (listIterator.hasNext()) {
                    arrayList.add(listIterator.next());
                }
            }
            return arrayList;
        }
    }

    @NotNull
    public static <T> List<T> reversed(@NotNull Iterable<? extends T> iterable) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$reversed");
        if ((iterable instanceof Collection) && ((Collection) iterable).size() <= 1) {
            return toList(iterable);
        }
        List<T> mutableList = toMutableList(iterable);
        CollectionsKt___CollectionsJvmKt.reverse(mutableList);
        return mutableList;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v4, resolved type: java.util.Collection */
    /* JADX WARN: Multi-variable type inference failed */
    @NotNull
    public static <T> List<T> sortedWith(@NotNull Iterable<? extends T> iterable, @NotNull Comparator<? super T> comparator) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$sortedWith");
        Intrinsics.checkParameterIsNotNull(comparator, "comparator");
        if (iterable instanceof Collection) {
            Collection collection = (Collection) iterable;
            if (collection.size() <= 1) {
                return toList(iterable);
            }
            Object[] array = collection.toArray(new Object[0]);
            if (array == null) {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.Array<T>");
            } else if (array != null) {
                ArraysKt___ArraysJvmKt.sortWith(array, comparator);
                return ArraysKt___ArraysJvmKt.asList(array);
            } else {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.Array<T>");
            }
        } else {
            List<T> mutableList = toMutableList(iterable);
            CollectionsKt__MutableCollectionsJVMKt.sortWith(mutableList, comparator);
            return mutableList;
        }
    }

    @NotNull
    public static final <T, C extends Collection<? super T>> C toCollection(@NotNull Iterable<? extends T> iterable, @NotNull C c) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$toCollection");
        Intrinsics.checkParameterIsNotNull(c, "destination");
        Iterator<? extends T> it = iterable.iterator();
        while (it.hasNext()) {
            c.add(it.next());
        }
        return c;
    }

    @NotNull
    public static final <T> HashSet<T> toHashSet(@NotNull Iterable<? extends T> iterable) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$toHashSet");
        HashSet<T> hashSet = new HashSet<>(MapsKt__MapsKt.mapCapacity(CollectionsKt__IterablesKt.collectionSizeOrDefault(iterable, 12)));
        toCollection(iterable, hashSet);
        return hashSet;
    }

    @NotNull
    public static <T> List<T> toList(@NotNull Iterable<? extends T> iterable) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$toList");
        if (!(iterable instanceof Collection)) {
            return CollectionsKt__CollectionsKt.optimizeReadOnlyList(toMutableList(iterable));
        }
        Collection collection = (Collection) iterable;
        int size = collection.size();
        if (size == 0) {
            return CollectionsKt__CollectionsKt.emptyList();
        }
        if (size != 1) {
            return toMutableList((Collection) collection);
        }
        return CollectionsKt__CollectionsJVMKt.listOf(iterable instanceof List ? ((List) iterable).get(0) : iterable.iterator().next());
    }

    @NotNull
    public static final <T> List<T> toMutableList(@NotNull Iterable<? extends T> iterable) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$toMutableList");
        if (iterable instanceof Collection) {
            return toMutableList((Collection) ((Collection) iterable));
        }
        ArrayList arrayList = new ArrayList();
        toCollection(iterable, arrayList);
        return arrayList;
    }

    @NotNull
    public static <T> List<T> toMutableList(@NotNull Collection<? extends T> collection) {
        Intrinsics.checkParameterIsNotNull(collection, "$this$toMutableList");
        return new ArrayList(collection);
    }

    @NotNull
    public static <T> Set<T> toSet(@NotNull Iterable<? extends T> iterable) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$toSet");
        if (iterable instanceof Collection) {
            Collection collection = (Collection) iterable;
            int size = collection.size();
            if (size == 0) {
                return SetsKt__SetsKt.emptySet();
            }
            if (size != 1) {
                LinkedHashSet linkedHashSet = new LinkedHashSet(MapsKt__MapsKt.mapCapacity(collection.size()));
                toCollection(iterable, linkedHashSet);
                return linkedHashSet;
            }
            return SetsKt__SetsJVMKt.setOf(iterable instanceof List ? ((List) iterable).get(0) : iterable.iterator().next());
        }
        LinkedHashSet linkedHashSet2 = new LinkedHashSet();
        toCollection(iterable, linkedHashSet2);
        return SetsKt__SetsKt.optimizeReadOnlySet(linkedHashSet2);
    }

    @NotNull
    public static <T> Set<T> intersect(@NotNull Iterable<? extends T> iterable, @NotNull Iterable<? extends T> iterable2) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$intersect");
        Intrinsics.checkParameterIsNotNull(iterable2, "other");
        Set<T> set = toMutableSet(iterable);
        CollectionsKt__MutableCollectionsKt.retainAll(set, iterable2);
        return set;
    }

    @NotNull
    public static <T> Set<T> subtract(@NotNull Iterable<? extends T> iterable, @NotNull Iterable<? extends T> iterable2) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$subtract");
        Intrinsics.checkParameterIsNotNull(iterable2, "other");
        Set<T> set = toMutableSet(iterable);
        CollectionsKt__MutableCollectionsKt.removeAll(set, iterable2);
        return set;
    }

    @NotNull
    public static <T> Set<T> toMutableSet(@NotNull Iterable<? extends T> iterable) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$toMutableSet");
        if (iterable instanceof Collection) {
            return new LinkedHashSet((Collection) iterable);
        }
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        toCollection(iterable, linkedHashSet);
        return linkedHashSet;
    }

    @NotNull
    public static <T> Set<T> union(@NotNull Iterable<? extends T> iterable, @NotNull Iterable<? extends T> iterable2) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$union");
        Intrinsics.checkParameterIsNotNull(iterable2, "other");
        Set<T> set = toMutableSet(iterable);
        boolean unused = CollectionsKt__MutableCollectionsKt.addAll(set, iterable2);
        return set;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v2, types: [java.lang.Comparable, java.lang.Object] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @org.jetbrains.annotations.Nullable
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static <T extends java.lang.Comparable<? super T>> T min(@org.jetbrains.annotations.NotNull java.lang.Iterable<? extends T> r3) {
        /*
            java.lang.String r0 = "$this$min"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r3, r0)
            java.util.Iterator r3 = r3.iterator()
            boolean r0 = r3.hasNext()
            if (r0 != 0) goto L_0x0011
            r3 = 0
            return r3
        L_0x0011:
            java.lang.Object r0 = r3.next()
            java.lang.Comparable r0 = (java.lang.Comparable) r0
        L_0x0017:
            boolean r1 = r3.hasNext()
            if (r1 == 0) goto L_0x002b
            java.lang.Object r1 = r3.next()
            java.lang.Comparable r1 = (java.lang.Comparable) r1
            int r2 = r0.compareTo(r1)
            if (r2 <= 0) goto L_0x0017
            r0 = r1
            goto L_0x0017
        L_0x002b:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: kotlin.collections.CollectionsKt___CollectionsKt.min(java.lang.Iterable):java.lang.Comparable");
    }

    @NotNull
    public static <T> List<T> minus(@NotNull Iterable<? extends T> iterable, T t) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$minus");
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(iterable, 10));
        boolean z = false;
        for (Object obj : iterable) {
            boolean z2 = true;
            if (!z && Intrinsics.areEqual(obj, t)) {
                z = true;
                z2 = false;
            }
            if (z2) {
                arrayList.add(obj);
            }
        }
        return arrayList;
    }

    @NotNull
    public static <T> List<T> plus(@NotNull Collection<? extends T> collection, T t) {
        Intrinsics.checkParameterIsNotNull(collection, "$this$plus");
        ArrayList arrayList = new ArrayList(collection.size() + 1);
        arrayList.addAll(collection);
        arrayList.add(t);
        return arrayList;
    }

    @NotNull
    public static <T> List<T> plus(@NotNull Collection<? extends T> collection, @NotNull Iterable<? extends T> iterable) {
        Intrinsics.checkParameterIsNotNull(collection, "$this$plus");
        Intrinsics.checkParameterIsNotNull(iterable, "elements");
        if (iterable instanceof Collection) {
            Collection collection2 = (Collection) iterable;
            ArrayList arrayList = new ArrayList(collection.size() + collection2.size());
            arrayList.addAll(collection);
            arrayList.addAll(collection2);
            return arrayList;
        }
        ArrayList arrayList2 = new ArrayList(collection);
        boolean unused = CollectionsKt__MutableCollectionsKt.addAll(arrayList2, iterable);
        return arrayList2;
    }

    public static /* synthetic */ Appendable joinTo$default(Iterable iterable, Appendable appendable, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, int i, CharSequence charSequence4, Function1 function1, int i2, Object obj) {
        String str = (i2 & 2) != 0 ? ", " : charSequence;
        CharSequence charSequence5 = "";
        CharSequence charSequence6 = (i2 & 4) != 0 ? charSequence5 : charSequence2;
        if ((i2 & 8) == 0) {
            charSequence5 = charSequence3;
        }
        joinTo(iterable, appendable, str, charSequence6, charSequence5, (i2 & 16) != 0 ? -1 : i, (i2 & 32) != 0 ? "..." : charSequence4, (i2 & 64) != 0 ? null : function1);
        return appendable;
    }

    @NotNull
    public static final <T, A extends Appendable> A joinTo(@NotNull Iterable<? extends T> iterable, @NotNull A a, @NotNull CharSequence charSequence, @NotNull CharSequence charSequence2, @NotNull CharSequence charSequence3, int i, @NotNull CharSequence charSequence4, @Nullable Function1<? super T, ? extends CharSequence> function1) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$joinTo");
        Intrinsics.checkParameterIsNotNull(a, "buffer");
        Intrinsics.checkParameterIsNotNull(charSequence, "separator");
        Intrinsics.checkParameterIsNotNull(charSequence2, "prefix");
        Intrinsics.checkParameterIsNotNull(charSequence3, "postfix");
        Intrinsics.checkParameterIsNotNull(charSequence4, "truncated");
        a.append(charSequence2);
        int i2 = 0;
        for (Object obj : iterable) {
            i2++;
            if (i2 > 1) {
                a.append(charSequence);
            }
            if (i >= 0 && i2 > i) {
                break;
            }
            StringsKt__StringBuilderKt.appendElement(a, obj, function1);
        }
        if (i >= 0 && i2 > i) {
            a.append(charSequence4);
        }
        a.append(charSequence3);
        return a;
    }

    public static /* synthetic */ String joinToString$default(Iterable iterable, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, int i, CharSequence charSequence4, Function1 function1, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            charSequence = ", ";
        }
        CharSequence charSequence5 = "";
        CharSequence charSequence6 = (i2 & 2) != 0 ? charSequence5 : charSequence2;
        if ((i2 & 4) == 0) {
            charSequence5 = charSequence3;
        }
        if ((i2 & 8) != 0) {
            i = -1;
        }
        if ((i2 & 16) != 0) {
            charSequence4 = "...";
        }
        if ((i2 & 32) != 0) {
            function1 = null;
        }
        return joinToString(iterable, charSequence, charSequence6, charSequence5, i, charSequence4, function1);
    }

    @NotNull
    public static final <T> String joinToString(@NotNull Iterable<? extends T> iterable, @NotNull CharSequence charSequence, @NotNull CharSequence charSequence2, @NotNull CharSequence charSequence3, int i, @NotNull CharSequence charSequence4, @Nullable Function1<? super T, ? extends CharSequence> function1) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$joinToString");
        Intrinsics.checkParameterIsNotNull(charSequence, "separator");
        Intrinsics.checkParameterIsNotNull(charSequence2, "prefix");
        Intrinsics.checkParameterIsNotNull(charSequence3, "postfix");
        Intrinsics.checkParameterIsNotNull(charSequence4, "truncated");
        StringBuilder sb = new StringBuilder();
        joinTo(iterable, sb, charSequence, charSequence2, charSequence3, i, charSequence4, function1);
        String sb2 = sb.toString();
        Intrinsics.checkExpressionValueIsNotNull(sb2, "joinTo(StringBuilder(), …ed, transform).toString()");
        return sb2;
    }

    @NotNull
    public static <T> Sequence<T> asSequence(@NotNull Iterable<? extends T> iterable) {
        Intrinsics.checkParameterIsNotNull(iterable, "$this$asSequence");
        return new Sequence<T>(iterable) { // from class: kotlin.collections.CollectionsKt___CollectionsKt$asSequence$$inlined$Sequence$1
            final /* synthetic */ Iterable $this_asSequence$inlined;

            {
                this.$this_asSequence$inlined = r1;
            }

            @Override // kotlin.sequences.Sequence
            @NotNull
            public Iterator<T> iterator() {
                return this.$this_asSequence$inlined.iterator();
            }
        };
    }
}
