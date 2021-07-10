package kotlin.sequences;

import java.util.Iterator;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: Sequences.kt */
public final class TransformingSequence<T, R> implements Sequence<R> {
    private final Sequence<T> sequence;
    private final Function1<T, R> transformer;

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: kotlin.sequences.Sequence<? extends T> */
    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: kotlin.jvm.functions.Function1<? super T, ? extends R> */
    /* JADX WARN: Multi-variable type inference failed */
    public TransformingSequence(@NotNull Sequence<? extends T> sequence, @NotNull Function1<? super T, ? extends R> function1) {
        Intrinsics.checkParameterIsNotNull(sequence, "sequence");
        Intrinsics.checkParameterIsNotNull(function1, "transformer");
        this.sequence = sequence;
        this.transformer = function1;
    }

    @Override // kotlin.sequences.Sequence
    @NotNull
    public Iterator<R> iterator() {
        return new Object(this) { // from class: kotlin.sequences.TransformingSequence$iterator$1
            @NotNull
            private final Iterator<T> iterator;
            final /* synthetic */ TransformingSequence this$0;

            @Override // java.util.Iterator
            public void remove() {
                throw new UnsupportedOperationException("Operation is not supported for read-only collection");
            }

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
                this.iterator = r1.sequence.iterator();
            }

            /* JADX WARN: Type inference failed for: r1v3, types: [R, java.lang.Object] */
            /* JADX WARNING: Unknown variable types count: 1 */
            @Override // java.util.Iterator
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public R next() {
                /*
                    r1 = this;
                    kotlin.sequences.TransformingSequence r0 = r1.this$0
                    kotlin.jvm.functions.Function1 r0 = kotlin.sequences.TransformingSequence.access$getTransformer$p(r0)
                    java.util.Iterator<T> r1 = r1.iterator
                    java.lang.Object r1 = r1.next()
                    java.lang.Object r1 = r0.invoke(r1)
                    return r1
                */
                throw new UnsupportedOperationException("Method not decompiled: kotlin.sequences.TransformingSequence$iterator$1.next():java.lang.Object");
            }

            @Override // java.util.Iterator
            public boolean hasNext() {
                return this.iterator.hasNext();
            }
        };
    }
}
