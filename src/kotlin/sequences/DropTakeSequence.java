package kotlin.sequences;

import org.jetbrains.annotations.NotNull;
/* compiled from: Sequences.kt */
public interface DropTakeSequence<T> extends Sequence<T> {
    @NotNull
    Sequence<T> take(int i);
}
