package kotlin.collections;

import java.util.Map;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import org.jetbrains.annotations.NotNull;
/* compiled from: _Maps.kt */
/* access modifiers changed from: package-private */
public class MapsKt___MapsKt extends MapsKt__MapsKt {
    @NotNull
    public static <K, V> Sequence<Map.Entry<K, V>> asSequence(@NotNull Map<? extends K, ? extends V> map) {
        Intrinsics.checkParameterIsNotNull(map, "$this$asSequence");
        return CollectionsKt___CollectionsKt.asSequence(map.entrySet());
    }
}
