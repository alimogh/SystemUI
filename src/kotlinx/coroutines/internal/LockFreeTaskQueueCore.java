package kotlinx.coroutines.internal;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.DebugKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: LockFreeTaskQueue.kt */
public final class LockFreeTaskQueueCore<E> {
    public static final Companion Companion = new Companion(null);
    @NotNull
    public static final Symbol REMOVE_FROZEN = new Symbol("REMOVE_FROZEN");
    private static final AtomicReferenceFieldUpdater _next$FU = AtomicReferenceFieldUpdater.newUpdater(LockFreeTaskQueueCore.class, Object.class, "_next");
    public static final /* synthetic */ AtomicLongFieldUpdater _state$FU$internal = AtomicLongFieldUpdater.newUpdater(LockFreeTaskQueueCore.class, "_state$internal");
    private volatile Object _next = null;
    public volatile /* synthetic */ long _state$internal = 0;
    public /* synthetic */ AtomicReferenceArray array$internal = new AtomicReferenceArray(this.capacity);
    private final int capacity;
    private final int mask;
    private final boolean singleConsumer;

    public LockFreeTaskQueueCore(int i, boolean z) {
        this.capacity = i;
        this.singleConsumer = z;
        boolean z2 = true;
        this.mask = i - 1;
        if (this.mask <= 1073741823) {
            if (!((this.mask & this.capacity) != 0 ? false : z2)) {
                throw new IllegalStateException("Check failed.".toString());
            }
            return;
        }
        throw new IllegalStateException("Check failed.".toString());
    }

    public final boolean isEmpty() {
        long j = this._state$internal;
        return ((int) ((1073741823 & j) >> 0)) == ((int) ((j & 1152921503533105152L) >> 30));
    }

    public final int getSize() {
        long j = this._state$internal;
        return 1073741823 & (((int) ((j & 1152921503533105152L) >> 30)) - ((int) ((1073741823 & j) >> 0)));
    }

    private final LockFreeTaskQueueCore<E> fillPlaceholder(int i, E e) {
        Object obj = this.array$internal.get(this.mask & i);
        if (!(obj instanceof Placeholder) || ((Placeholder) obj).index != i) {
            return null;
        }
        this.array$internal.set(i & this.mask, e);
        return this;
    }

    @NotNull
    public final LockFreeTaskQueueCore<E> next() {
        return allocateOrGetNextCopy(markFrozen());
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v3, resolved type: java.util.concurrent.atomic.AtomicReferenceArray */
    /* JADX WARN: Multi-variable type inference failed */
    private final LockFreeTaskQueueCore<E> allocateNextCopy(long j) {
        LockFreeTaskQueueCore<E> lockFreeTaskQueueCore = new LockFreeTaskQueueCore<>(this.capacity * 2, this.singleConsumer);
        int i = (int) ((1073741823 & j) >> 0);
        int i2 = (int) ((1152921503533105152L & j) >> 30);
        while (true) {
            int i3 = this.mask;
            if ((i & i3) != (i2 & i3)) {
                Object obj = this.array$internal.get(i3 & i);
                if (obj == null) {
                    obj = new Placeholder(i);
                }
                lockFreeTaskQueueCore.array$internal.set(lockFreeTaskQueueCore.mask & i, obj);
                i++;
            } else {
                lockFreeTaskQueueCore._state$internal = Companion.wo(j, 1152921504606846976L);
                return lockFreeTaskQueueCore;
            }
        }
    }

    /* compiled from: LockFreeTaskQueue.kt */
    public static final class Placeholder {
        public final int index;

        public Placeholder(int i) {
            this.index = i;
        }
    }

    /* compiled from: LockFreeTaskQueue.kt */
    public static final class Companion {
        public final int addFailReason(long j) {
            return (j & 2305843009213693952L) != 0 ? 2 : 1;
        }

        public final long wo(long j, long j2) {
            return j & (~j2);
        }

        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final long updateHead(long j, int i) {
            return wo(j, 1073741823) | (((long) i) << 0);
        }

        public final long updateTail(long j, int i) {
            return wo(j, 1152921503533105152L) | (((long) i) << 30);
        }
    }

    public final boolean close() {
        long j;
        do {
            j = this._state$internal;
            if ((j & 2305843009213693952L) != 0) {
                return true;
            }
            if ((1152921504606846976L & j) != 0) {
                return false;
            }
        } while (!_state$FU$internal.compareAndSet(this, j, j | 2305843009213693952L));
        return true;
    }

    public final int addLast(@NotNull E e) {
        Companion companion = Companion;
        Intrinsics.checkParameterIsNotNull(e, "element");
        while (true) {
            long j = this._state$internal;
            if ((3458764513820540928L & j) != 0) {
                return companion.addFailReason(j);
            }
            int i = (int) ((1073741823 & j) >> 0);
            int i2 = (int) ((1152921503533105152L & j) >> 30);
            int i3 = this.mask;
            if (((i2 + 2) & i3) == (i & i3)) {
                return 1;
            }
            if (!this.singleConsumer && this.array$internal.get(i2 & i3) != null) {
                int i4 = this.capacity;
                if (i4 < 1024 || ((i2 - i) & 1073741823) > (i4 >> 1)) {
                    break;
                }
            } else if (_state$FU$internal.compareAndSet(this, j, companion.updateTail(j, (i2 + 1) & 1073741823))) {
                this.array$internal.set(i2 & i3, e);
                while ((this._state$internal & 1152921504606846976L) != 0 && (this = this.next().fillPlaceholder(i2, e)) != null) {
                }
                return 0;
            }
        }
        return 1;
    }

    @Nullable
    public final Object removeFirstOrNull() {
        Object obj;
        while (true) {
            long j = this._state$internal;
            if ((1152921504606846976L & j) == 0) {
                int i = (int) ((1073741823 & j) >> 0);
                if ((((int) ((1152921503533105152L & j) >> 30)) & this.mask) != (this.mask & i)) {
                    obj = this.array$internal.get(this.mask & i);
                    if (obj != null) {
                        if (!(obj instanceof Placeholder)) {
                            int i2 = (i + 1) & 1073741823;
                            if (!_state$FU$internal.compareAndSet(this, j, Companion.updateHead(j, i2))) {
                                if (this.singleConsumer) {
                                    do {
                                        this = this.removeSlowPath(i, i2);
                                    } while (this != null);
                                    break;
                                }
                            } else {
                                this.array$internal.set(this.mask & i, null);
                                break;
                            }
                        } else {
                            return null;
                        }
                    } else if (this.singleConsumer) {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return REMOVE_FROZEN;
            }
        }
        return obj;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final LockFreeTaskQueueCore<E> removeSlowPath(int i, int i2) {
        long j;
        int i3;
        do {
            j = this._state$internal;
            boolean z = false;
            i3 = (int) ((1073741823 & j) >> 0);
            if (DebugKt.getASSERTIONS_ENABLED()) {
                if (i3 == i) {
                    z = true;
                }
                if (!z) {
                    throw new AssertionError();
                }
            }
            if ((1152921504606846976L & j) != 0) {
                return next();
            }
        } while (!_state$FU$internal.compareAndSet(this, j, Companion.updateHead(j, i2)));
        this.array$internal.set(this.mask & i3, null);
        return null;
    }

    private final long markFrozen() {
        long j;
        long j2;
        do {
            j = this._state$internal;
            if ((j & 1152921504606846976L) != 0) {
                return j;
            }
            j2 = j | 1152921504606846976L;
        } while (!_state$FU$internal.compareAndSet(this, j, j2));
        return j2;
    }

    private final LockFreeTaskQueueCore<E> allocateOrGetNextCopy(long j) {
        while (true) {
            LockFreeTaskQueueCore<E> lockFreeTaskQueueCore = (LockFreeTaskQueueCore) this._next;
            if (lockFreeTaskQueueCore != null) {
                return lockFreeTaskQueueCore;
            }
            _next$FU.compareAndSet(this, null, allocateNextCopy(j));
        }
    }
}
