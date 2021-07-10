package kotlin.sequences;

import java.util.Iterator;
import kotlin.collections.EmptyIterator;
import org.jetbrains.annotations.NotNull;
/* compiled from: Sequences.kt */
/* access modifiers changed from: package-private */
public final class EmptySequence implements Sequence, DropTakeSequence {
    public static final EmptySequence INSTANCE = new EmptySequence();

    private EmptySequence() {
    }

    @Override // kotlin.sequences.Sequence
    @NotNull
    public Iterator iterator() {
        return EmptyIterator.INSTANCE;
    }

    @Override // kotlin.sequences.DropTakeSequence
    @NotNull
    public EmptySequence take(int i) {
        return INSTANCE;
    }
}
