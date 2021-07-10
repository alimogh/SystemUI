package kotlinx.coroutines.internal;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import kotlinx.coroutines.DebugKt;
import org.jetbrains.annotations.Nullable;
/* compiled from: Atomic.kt */
public abstract class AtomicOp<T> extends OpDescriptor {
    private static final AtomicReferenceFieldUpdater _consensus$FU = AtomicReferenceFieldUpdater.newUpdater(AtomicOp.class, Object.class, "_consensus");
    private volatile Object _consensus = AtomicKt.NO_DECISION;

    public abstract void complete(T t, @Nullable Object obj);

    @Nullable
    public abstract Object prepare(T t);

    public final boolean tryDecide(@Nullable Object obj) {
        if (DebugKt.getASSERTIONS_ENABLED()) {
            if (!(obj != AtomicKt.NO_DECISION)) {
                throw new AssertionError();
            }
        }
        return _consensus$FU.compareAndSet(this, AtomicKt.NO_DECISION, obj);
    }

    private final Object decide(Object obj) {
        return tryDecide(obj) ? obj : this._consensus;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // kotlinx.coroutines.internal.OpDescriptor
    @Nullable
    public final Object perform(@Nullable Object obj) {
        Object obj2 = this._consensus;
        if (obj2 == AtomicKt.NO_DECISION) {
            obj2 = decide(prepare(obj));
        }
        complete(obj, obj2);
        return obj2;
    }
}
