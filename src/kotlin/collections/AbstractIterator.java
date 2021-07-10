package kotlin.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;
import kotlin.jvm.internal.markers.KMappedMarker;
/* compiled from: AbstractIterator.kt */
public abstract class AbstractIterator<T> implements Iterator<T>, KMappedMarker {
    private T nextValue;
    private State state = State.NotReady;

    public final /* synthetic */ class WhenMappings {
        public static final /* synthetic */ int[] $EnumSwitchMapping$0;

        static {
            int[] iArr = new int[State.values().length];
            $EnumSwitchMapping$0 = iArr;
            iArr[State.Done.ordinal()] = 1;
            $EnumSwitchMapping$0[State.Ready.ordinal()] = 2;
        }
    }

    /* access modifiers changed from: protected */
    public abstract void computeNext();

    @Override // java.util.Iterator
    public void remove() {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    @Override // java.util.Iterator
    public boolean hasNext() {
        if (this.state != State.Failed) {
            int i = WhenMappings.$EnumSwitchMapping$0[this.state.ordinal()];
            if (i == 1) {
                return false;
            }
            if (i != 2) {
                return tryToComputeNext();
            }
            return true;
        }
        throw new IllegalArgumentException("Failed requirement.".toString());
    }

    @Override // java.util.Iterator
    public T next() {
        if (hasNext()) {
            this.state = State.NotReady;
            return this.nextValue;
        }
        throw new NoSuchElementException();
    }

    private final boolean tryToComputeNext() {
        this.state = State.Failed;
        computeNext();
        return this.state == State.Ready;
    }

    /* access modifiers changed from: protected */
    public final void setNext(T t) {
        this.nextValue = t;
        this.state = State.Ready;
    }

    /* access modifiers changed from: protected */
    public final void done() {
        this.state = State.Done;
    }
}
