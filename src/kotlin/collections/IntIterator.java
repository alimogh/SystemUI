package kotlin.collections;

import java.util.Iterator;
import kotlin.jvm.internal.markers.KMappedMarker;
import org.jetbrains.annotations.NotNull;
/* compiled from: Iterators.kt */
public abstract class IntIterator implements Iterator<Integer>, KMappedMarker {
    public abstract int nextInt();

    @Override // java.util.Iterator
    public void remove() {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    @Override // java.util.Iterator
    @NotNull
    public final Integer next() {
        return Integer.valueOf(nextInt());
    }
}
