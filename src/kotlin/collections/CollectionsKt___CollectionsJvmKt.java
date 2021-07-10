package kotlin.collections;

import java.util.Collections;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: _CollectionsJvm.kt */
/* access modifiers changed from: package-private */
public class CollectionsKt___CollectionsJvmKt extends CollectionsKt__ReversedViewsKt {
    public static final <T> void reverse(@NotNull List<T> list) {
        Intrinsics.checkParameterIsNotNull(list, "$this$reverse");
        Collections.reverse(list);
    }
}
