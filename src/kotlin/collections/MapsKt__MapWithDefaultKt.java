package kotlin.collections;

import java.util.Map;
import java.util.NoSuchElementException;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: MapWithDefault.kt */
/* access modifiers changed from: package-private */
public class MapsKt__MapWithDefaultKt {
    public static final <K, V> V getOrImplicitDefaultNullable(@NotNull Map<K, ? extends V> map, K k) {
        Intrinsics.checkParameterIsNotNull(map, "$this$getOrImplicitDefault");
        if (map instanceof MapWithDefault) {
            return (V) ((MapWithDefault) map).getOrImplicitDefault(k);
        }
        V v = (V) map.get(k);
        if (v != null || map.containsKey(k)) {
            return v;
        }
        throw new NoSuchElementException("Key " + ((Object) k) + " is missing in the map.");
    }

    @NotNull
    public static <K, V> Map<K, V> withDefault(@NotNull Map<K, ? extends V> map, @NotNull Function1<? super K, ? extends V> function1) {
        Intrinsics.checkParameterIsNotNull(map, "$this$withDefault");
        Intrinsics.checkParameterIsNotNull(function1, "defaultValue");
        if (map instanceof MapWithDefault) {
            return withDefault(((MapWithDefault) map).getMap(), function1);
        }
        return new MapWithDefaultImpl(map, function1);
    }
}
