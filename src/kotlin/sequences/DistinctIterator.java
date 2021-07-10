package kotlin.sequences;

import java.util.HashSet;
import java.util.Iterator;
import kotlin.collections.AbstractIterator;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: Sequences.kt */
final class DistinctIterator<T, K> extends AbstractIterator<T> {
    private final Function1<T, K> keySelector;
    private final HashSet<K> observed = new HashSet<>();
    private final Iterator<T> source;

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: java.util.Iterator<? extends T> */
    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: kotlin.jvm.functions.Function1<? super T, ? extends K> */
    /* JADX WARN: Multi-variable type inference failed */
    public DistinctIterator(@NotNull Iterator<? extends T> it, @NotNull Function1<? super T, ? extends K> function1) {
        Intrinsics.checkParameterIsNotNull(it, "source");
        Intrinsics.checkParameterIsNotNull(function1, "keySelector");
        this.source = it;
        this.keySelector = function1;
    }

    /* access modifiers changed from: protected */
    @Override // kotlin.collections.AbstractIterator
    public void computeNext() {
        while (this.source.hasNext()) {
            T next = this.source.next();
            if (this.observed.add(this.keySelector.invoke(next))) {
                setNext(next);
                return;
            }
        }
        done();
    }
}
