package kotlin.collections;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import kotlin.Pair;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import org.jetbrains.annotations.NotNull;
/* compiled from: Maps.kt */
/* access modifiers changed from: package-private */
public class MapsKt__MapsKt extends MapsKt__MapsJVMKt {
    @NotNull
    public static <K, V> Map<K, V> emptyMap() {
        EmptyMap emptyMap = EmptyMap.INSTANCE;
        if (emptyMap != null) {
            return emptyMap;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.collections.Map<K, V>");
    }

    @NotNull
    public static <K, V> Map<K, V> mapOf(@NotNull Pair<? extends K, ? extends V>... pairArr) {
        Intrinsics.checkParameterIsNotNull(pairArr, "pairs");
        if (pairArr.length <= 0) {
            return emptyMap();
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap(mapCapacity(pairArr.length));
        toMap(pairArr, linkedHashMap);
        return linkedHashMap;
    }

    public static int mapCapacity(int i) {
        if (i < 3) {
            return i + 1;
        }
        if (i < 1073741824) {
            return i + (i / 3);
        }
        return Integer.MAX_VALUE;
    }

    public static <K, V> V getValue(@NotNull Map<K, ? extends V> map, K k) {
        Intrinsics.checkParameterIsNotNull(map, "$this$getValue");
        return (V) MapsKt__MapWithDefaultKt.getOrImplicitDefaultNullable(map, k);
    }

    public static final <K, V> void putAll(@NotNull Map<? super K, ? super V> map, @NotNull Pair<? extends K, ? extends V>[] pairArr) {
        Intrinsics.checkParameterIsNotNull(map, "$this$putAll");
        Intrinsics.checkParameterIsNotNull(pairArr, "pairs");
        for (Pair<? extends K, ? extends V> pair : pairArr) {
            map.put((Object) pair.component1(), (Object) pair.component2());
        }
    }

    public static final <K, V> void putAll(@NotNull Map<? super K, ? super V> map, @NotNull Sequence<? extends Pair<? extends K, ? extends V>> sequence) {
        Intrinsics.checkParameterIsNotNull(map, "$this$putAll");
        Intrinsics.checkParameterIsNotNull(sequence, "pairs");
        Iterator<? extends Pair<? extends K, ? extends V>> it = sequence.iterator();
        while (it.hasNext()) {
            Pair pair = (Pair) it.next();
            map.put((Object) pair.component1(), (Object) pair.component2());
        }
    }

    @NotNull
    public static final <K, V, M extends Map<? super K, ? super V>> M toMap(@NotNull Pair<? extends K, ? extends V>[] pairArr, @NotNull M m) {
        Intrinsics.checkParameterIsNotNull(pairArr, "$this$toMap");
        Intrinsics.checkParameterIsNotNull(m, "destination");
        putAll(m, pairArr);
        return m;
    }

    @NotNull
    public static <K, V> Map<K, V> toMap(@NotNull Sequence<? extends Pair<? extends K, ? extends V>> sequence) {
        Intrinsics.checkParameterIsNotNull(sequence, "$this$toMap");
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        toMap(sequence, linkedHashMap);
        return optimizeReadOnlyMap(linkedHashMap);
    }

    @NotNull
    public static final <K, V, M extends Map<? super K, ? super V>> M toMap(@NotNull Sequence<? extends Pair<? extends K, ? extends V>> sequence, @NotNull M m) {
        Intrinsics.checkParameterIsNotNull(sequence, "$this$toMap");
        Intrinsics.checkParameterIsNotNull(m, "destination");
        putAll(m, sequence);
        return m;
    }

    @NotNull
    public static <K, V> Map<K, V> toMutableMap(@NotNull Map<? extends K, ? extends V> map) {
        Intrinsics.checkParameterIsNotNull(map, "$this$toMutableMap");
        return new LinkedHashMap(map);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: java.util.Map<K, ? extends V> */
    /* JADX WARN: Multi-variable type inference failed */
    @NotNull
    public static final <K, V> Map<K, V> optimizeReadOnlyMap(@NotNull Map<K, ? extends V> map) {
        Intrinsics.checkParameterIsNotNull(map, "$this$optimizeReadOnlyMap");
        int size = map.size();
        if (size == 0) {
            return emptyMap();
        }
        if (size != 1) {
            return map;
        }
        return MapsKt__MapsJVMKt.toSingletonMap(map);
    }
}
