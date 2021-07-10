package kotlin.sequences;

import java.util.Iterator;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: Sequences.kt */
public final class FlatteningSequence<T, R, E> implements Sequence<E> {
    private final Function1<R, Iterator<E>> iterator;
    private final Sequence<T> sequence;
    private final Function1<T, R> transformer;

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: kotlin.sequences.Sequence<? extends T> */
    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: kotlin.jvm.functions.Function1<? super T, ? extends R> */
    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: kotlin.jvm.functions.Function1<? super R, ? extends java.util.Iterator<? extends E>> */
    /* JADX WARN: Multi-variable type inference failed */
    public FlatteningSequence(@NotNull Sequence<? extends T> sequence, @NotNull Function1<? super T, ? extends R> function1, @NotNull Function1<? super R, ? extends Iterator<? extends E>> function12) {
        Intrinsics.checkParameterIsNotNull(sequence, "sequence");
        Intrinsics.checkParameterIsNotNull(function1, "transformer");
        Intrinsics.checkParameterIsNotNull(function12, "iterator");
        this.sequence = sequence;
        this.transformer = function1;
        this.iterator = function12;
    }

    @Override // kotlin.sequences.Sequence
    @NotNull
    public Iterator<E> iterator() {
        return new Object(this) { // from class: kotlin.sequences.FlatteningSequence$iterator$1
            @Nullable
            private Iterator<? extends E> itemIterator;
            @NotNull
            private final Iterator<T> iterator;
            final /* synthetic */ FlatteningSequence this$0;

            @Override // java.util.Iterator
            public void remove() {
                throw new UnsupportedOperationException("Operation is not supported for read-only collection");
            }

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
                this.iterator = r1.sequence.iterator();
            }

            /* JADX WARN: Type inference failed for: r1v4, types: [E, java.lang.Object] */
            /* JADX WARNING: Unknown variable types count: 1 */
            @Override // java.util.Iterator
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public E next() {
                /*
                    r1 = this;
                    boolean r0 = r1.ensureItemIterator()
                    if (r0 == 0) goto L_0x0014
                    java.util.Iterator<? extends E> r1 = r1.itemIterator
                    if (r1 == 0) goto L_0x000f
                    java.lang.Object r1 = r1.next()
                    return r1
                L_0x000f:
                    kotlin.jvm.internal.Intrinsics.throwNpe()
                    r1 = 0
                    throw r1
                L_0x0014:
                    java.util.NoSuchElementException r1 = new java.util.NoSuchElementException
                    r1.<init>()
                    throw r1
                */
                throw new UnsupportedOperationException("Method not decompiled: kotlin.sequences.FlatteningSequence$iterator$1.next():java.lang.Object");
            }

            @Override // java.util.Iterator
            public boolean hasNext() {
                return ensureItemIterator();
            }

            private final boolean ensureItemIterator() {
                Iterator<? extends E> it = this.itemIterator;
                if (it != 0 && !it.hasNext()) {
                    this.itemIterator = null;
                }
                while (true) {
                    if (this.itemIterator == null) {
                        if (this.iterator.hasNext()) {
                            Iterator<? extends E> it2 = (Iterator) this.this$0.iterator.invoke(this.this$0.transformer.invoke(this.iterator.next()));
                            if (it2.hasNext()) {
                                this.itemIterator = it2;
                                break;
                            }
                        } else {
                            return false;
                        }
                    } else {
                        break;
                    }
                }
                return true;
            }
        };
    }
}
