package kotlin;

import org.jetbrains.annotations.NotNull;
/* compiled from: Tuples.kt */
public final class TuplesKt {
    @NotNull
    public static final <A, B> Pair<A, B> to(A a, B b) {
        return new Pair<>(a, b);
    }
}
