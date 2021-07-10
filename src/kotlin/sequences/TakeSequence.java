package kotlin.sequences;

import java.util.Iterator;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: Sequences.kt */
public final class TakeSequence<T> implements Sequence<T>, DropTakeSequence<T> {
    private final int count;
    private final Sequence<T> sequence;

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: kotlin.sequences.Sequence<? extends T> */
    /* JADX WARN: Multi-variable type inference failed */
    public TakeSequence(@NotNull Sequence<? extends T> sequence, int i) {
        Intrinsics.checkParameterIsNotNull(sequence, "sequence");
        this.sequence = sequence;
        this.count = i;
        if (!(i >= 0)) {
            throw new IllegalArgumentException(("count must be non-negative, but was " + this.count + '.').toString());
        }
    }

    @Override // kotlin.sequences.DropTakeSequence
    @NotNull
    public Sequence<T> take(int i) {
        return i >= this.count ? this : new TakeSequence(this.sequence, i);
    }

    @Override // kotlin.sequences.Sequence
    @NotNull
    public Iterator<T> iterator() {
        return new Object(this) { // from class: kotlin.sequences.TakeSequence$iterator$1
            @NotNull
            private final Iterator<T> iterator;
            private int left;

            @Override // java.util.Iterator
            public void remove() {
                throw new UnsupportedOperationException("Operation is not supported for read-only collection");
            }

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.left = TakeSequence.access$getCount$p(r2);
                this.iterator = TakeSequence.access$getSequence$p(r2).iterator();
            }

            /* JADX WARN: Type inference failed for: r1v3, types: [T, java.lang.Object] */
            /* JADX WARNING: Unknown variable types count: 1 */
            @Override // java.util.Iterator
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public T next() {
                /*
                    r1 = this;
                    int r0 = r1.left
                    if (r0 == 0) goto L_0x000f
                    int r0 = r0 + -1
                    r1.left = r0
                    java.util.Iterator<T> r1 = r1.iterator
                    java.lang.Object r1 = r1.next()
                    return r1
                L_0x000f:
                    java.util.NoSuchElementException r1 = new java.util.NoSuchElementException
                    r1.<init>()
                    throw r1
                */
                throw new UnsupportedOperationException("Method not decompiled: kotlin.sequences.TakeSequence$iterator$1.next():java.lang.Object");
            }

            @Override // java.util.Iterator
            public boolean hasNext() {
                return this.left > 0 && this.iterator.hasNext();
            }
        };
    }
}
