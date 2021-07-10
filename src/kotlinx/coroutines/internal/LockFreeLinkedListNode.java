package kotlinx.coroutines.internal;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.DebugKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: LockFreeLinkedList.kt */
public class LockFreeLinkedListNode {
    static final AtomicReferenceFieldUpdater _next$FU = AtomicReferenceFieldUpdater.newUpdater(LockFreeLinkedListNode.class, Object.class, "_next");
    static final AtomicReferenceFieldUpdater _prev$FU = AtomicReferenceFieldUpdater.newUpdater(LockFreeLinkedListNode.class, Object.class, "_prev");
    private static final AtomicReferenceFieldUpdater _removedRef$FU = AtomicReferenceFieldUpdater.newUpdater(LockFreeLinkedListNode.class, Object.class, "_removedRef");
    volatile Object _next = this;
    volatile Object _prev = this;
    private volatile Object _removedRef = null;

    private final Removed removed() {
        Removed removed = (Removed) this._removedRef;
        if (removed != null) {
            return removed;
        }
        Removed removed2 = new Removed(this);
        _removedRef$FU.lazySet(this, removed2);
        return removed2;
    }

    /* compiled from: LockFreeLinkedList.kt */
    public static abstract class CondAddOp extends AtomicOp<LockFreeLinkedListNode> {
        @NotNull
        public final LockFreeLinkedListNode newNode;
        @Nullable
        public LockFreeLinkedListNode oldNext;

        public CondAddOp(@NotNull LockFreeLinkedListNode lockFreeLinkedListNode) {
            Intrinsics.checkParameterIsNotNull(lockFreeLinkedListNode, "newNode");
            this.newNode = lockFreeLinkedListNode;
        }

        public void complete(@NotNull LockFreeLinkedListNode lockFreeLinkedListNode, @Nullable Object obj) {
            Intrinsics.checkParameterIsNotNull(lockFreeLinkedListNode, "affected");
            boolean z = obj == null;
            LockFreeLinkedListNode lockFreeLinkedListNode2 = z ? this.newNode : this.oldNext;
            if (lockFreeLinkedListNode2 != null && LockFreeLinkedListNode._next$FU.compareAndSet(lockFreeLinkedListNode, this, lockFreeLinkedListNode2) && z) {
                LockFreeLinkedListNode lockFreeLinkedListNode3 = this.newNode;
                LockFreeLinkedListNode lockFreeLinkedListNode4 = this.oldNext;
                if (lockFreeLinkedListNode4 != null) {
                    lockFreeLinkedListNode3.finishAdd(lockFreeLinkedListNode4);
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            }
        }
    }

    public final boolean isRemoved() {
        return getNext() instanceof Removed;
    }

    @NotNull
    public final LockFreeLinkedListNode getNextNode() {
        return LockFreeLinkedListKt.unwrap(getNext());
    }

    @NotNull
    public final LockFreeLinkedListNode getPrevNode() {
        return LockFreeLinkedListKt.unwrap(getPrev());
    }

    public final boolean addOneIfEmpty(@NotNull LockFreeLinkedListNode lockFreeLinkedListNode) {
        Intrinsics.checkParameterIsNotNull(lockFreeLinkedListNode, "node");
        _prev$FU.lazySet(lockFreeLinkedListNode, this);
        _next$FU.lazySet(lockFreeLinkedListNode, this);
        while (getNext() == this) {
            if (_next$FU.compareAndSet(this, this, lockFreeLinkedListNode)) {
                lockFreeLinkedListNode.finishAdd(this);
                return true;
            }
        }
        return false;
    }

    public final int tryCondAddNext(@NotNull LockFreeLinkedListNode lockFreeLinkedListNode, @NotNull LockFreeLinkedListNode lockFreeLinkedListNode2, @NotNull CondAddOp condAddOp) {
        Intrinsics.checkParameterIsNotNull(lockFreeLinkedListNode, "node");
        Intrinsics.checkParameterIsNotNull(lockFreeLinkedListNode2, "next");
        Intrinsics.checkParameterIsNotNull(condAddOp, "condAdd");
        _prev$FU.lazySet(lockFreeLinkedListNode, this);
        _next$FU.lazySet(lockFreeLinkedListNode, lockFreeLinkedListNode2);
        condAddOp.oldNext = lockFreeLinkedListNode2;
        if (!_next$FU.compareAndSet(this, lockFreeLinkedListNode2, condAddOp)) {
            return 0;
        }
        return condAddOp.perform(this) == null ? 1 : 2;
    }

    public boolean remove() {
        Object next;
        LockFreeLinkedListNode lockFreeLinkedListNode;
        do {
            next = getNext();
            if ((next instanceof Removed) || next == this) {
                return false;
            }
            if (next != null) {
                lockFreeLinkedListNode = (LockFreeLinkedListNode) next;
            } else {
                throw new TypeCastException("null cannot be cast to non-null type kotlinx.coroutines.internal.Node /* = kotlinx.coroutines.internal.LockFreeLinkedListNode */");
            }
        } while (!_next$FU.compareAndSet(this, next, lockFreeLinkedListNode.removed()));
        finishRemove(lockFreeLinkedListNode);
        return true;
    }

    private final void finishRemove(LockFreeLinkedListNode lockFreeLinkedListNode) {
        helpDelete();
        lockFreeLinkedListNode.correctPrev(LockFreeLinkedListKt.unwrap(this._prev), null);
    }

    private final LockFreeLinkedListNode findHead() {
        LockFreeLinkedListNode lockFreeLinkedListNode = this;
        while (!(lockFreeLinkedListNode instanceof LockFreeLinkedListHead)) {
            lockFreeLinkedListNode = lockFreeLinkedListNode.getNextNode();
            if (DebugKt.getASSERTIONS_ENABLED()) {
                if (!(lockFreeLinkedListNode != this)) {
                    throw new AssertionError();
                }
            }
        }
        return lockFreeLinkedListNode;
    }

    public final void helpDelete() {
        Object next;
        LockFreeLinkedListNode markPrev = markPrev();
        Object obj = this._next;
        if (obj != null) {
            LockFreeLinkedListNode lockFreeLinkedListNode = ((Removed) obj).ref;
            while (true) {
                LockFreeLinkedListNode lockFreeLinkedListNode2 = null;
                while (true) {
                    Object next2 = lockFreeLinkedListNode.getNext();
                    if (next2 instanceof Removed) {
                        lockFreeLinkedListNode.markPrev();
                        lockFreeLinkedListNode = ((Removed) next2).ref;
                    } else {
                        next = markPrev.getNext();
                        if (next instanceof Removed) {
                            if (lockFreeLinkedListNode2 != null) {
                                break;
                            }
                            markPrev = LockFreeLinkedListKt.unwrap(markPrev._prev);
                        } else if (next != this) {
                            if (next != null) {
                                LockFreeLinkedListNode lockFreeLinkedListNode3 = (LockFreeLinkedListNode) next;
                                if (lockFreeLinkedListNode3 != lockFreeLinkedListNode) {
                                    lockFreeLinkedListNode2 = markPrev;
                                    markPrev = lockFreeLinkedListNode3;
                                } else {
                                    return;
                                }
                            } else {
                                throw new TypeCastException("null cannot be cast to non-null type kotlinx.coroutines.internal.Node /* = kotlinx.coroutines.internal.LockFreeLinkedListNode */");
                            }
                        } else if (_next$FU.compareAndSet(markPrev, this, lockFreeLinkedListNode)) {
                            return;
                        }
                    }
                }
                markPrev.markPrev();
                _next$FU.compareAndSet(lockFreeLinkedListNode2, markPrev, ((Removed) next).ref);
                markPrev = lockFreeLinkedListNode2;
            }
        } else {
            throw new TypeCastException("null cannot be cast to non-null type kotlinx.coroutines.internal.Removed");
        }
    }

    private final LockFreeLinkedListNode correctPrev(LockFreeLinkedListNode lockFreeLinkedListNode, OpDescriptor opDescriptor) {
        Object obj;
        while (true) {
            LockFreeLinkedListNode lockFreeLinkedListNode2 = null;
            while (true) {
                obj = lockFreeLinkedListNode._next;
                if (obj == opDescriptor) {
                    return lockFreeLinkedListNode;
                }
                if (obj instanceof OpDescriptor) {
                    ((OpDescriptor) obj).perform(lockFreeLinkedListNode);
                } else if (!(obj instanceof Removed)) {
                    Object obj2 = this._prev;
                    if (obj2 instanceof Removed) {
                        return null;
                    }
                    if (obj != this) {
                        if (obj != null) {
                            lockFreeLinkedListNode2 = lockFreeLinkedListNode;
                            lockFreeLinkedListNode = (LockFreeLinkedListNode) obj;
                        } else {
                            throw new TypeCastException("null cannot be cast to non-null type kotlinx.coroutines.internal.Node /* = kotlinx.coroutines.internal.LockFreeLinkedListNode */");
                        }
                    } else if (obj2 == lockFreeLinkedListNode) {
                        return null;
                    } else {
                        if (_prev$FU.compareAndSet(this, obj2, lockFreeLinkedListNode) && !(lockFreeLinkedListNode._prev instanceof Removed)) {
                            return null;
                        }
                    }
                } else if (lockFreeLinkedListNode2 != null) {
                    break;
                } else {
                    lockFreeLinkedListNode = LockFreeLinkedListKt.unwrap(lockFreeLinkedListNode._prev);
                }
            }
            lockFreeLinkedListNode.markPrev();
            _next$FU.compareAndSet(lockFreeLinkedListNode2, lockFreeLinkedListNode, ((Removed) obj).ref);
            lockFreeLinkedListNode = lockFreeLinkedListNode2;
        }
    }

    @NotNull
    public String toString() {
        return getClass().getSimpleName() + '@' + Integer.toHexString(System.identityHashCode(this));
    }

    @NotNull
    public final Object getNext() {
        while (true) {
            Object obj = this._next;
            if (!(obj instanceof OpDescriptor)) {
                return obj;
            }
            ((OpDescriptor) obj).perform(this);
        }
    }

    @NotNull
    public final Object getPrev() {
        while (true) {
            Object obj = this._prev;
            if (obj instanceof Removed) {
                return obj;
            }
            if (obj != null) {
                LockFreeLinkedListNode lockFreeLinkedListNode = (LockFreeLinkedListNode) obj;
                if (lockFreeLinkedListNode.getNext() == this) {
                    return obj;
                }
                correctPrev(lockFreeLinkedListNode, null);
            } else {
                throw new TypeCastException("null cannot be cast to non-null type kotlinx.coroutines.internal.Node /* = kotlinx.coroutines.internal.LockFreeLinkedListNode */");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void finishAdd(LockFreeLinkedListNode lockFreeLinkedListNode) {
        Object obj;
        do {
            obj = lockFreeLinkedListNode._prev;
            if ((obj instanceof Removed) || getNext() != lockFreeLinkedListNode) {
                return;
            }
        } while (!_prev$FU.compareAndSet(lockFreeLinkedListNode, obj, this));
        if (!(getNext() instanceof Removed)) {
            return;
        }
        if (obj != null) {
            lockFreeLinkedListNode.correctPrev((LockFreeLinkedListNode) obj, null);
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlinx.coroutines.internal.Node /* = kotlinx.coroutines.internal.LockFreeLinkedListNode */");
    }

    private final LockFreeLinkedListNode markPrev() {
        Object obj;
        LockFreeLinkedListNode lockFreeLinkedListNode;
        do {
            obj = this._prev;
            if (obj instanceof Removed) {
                return ((Removed) obj).ref;
            }
            if (obj == this) {
                lockFreeLinkedListNode = findHead();
            } else if (obj != null) {
                lockFreeLinkedListNode = (LockFreeLinkedListNode) obj;
            } else {
                throw new TypeCastException("null cannot be cast to non-null type kotlinx.coroutines.internal.Node /* = kotlinx.coroutines.internal.LockFreeLinkedListNode */");
            }
        } while (!_prev$FU.compareAndSet(this, obj, lockFreeLinkedListNode.removed()));
        return (LockFreeLinkedListNode) obj;
    }
}
