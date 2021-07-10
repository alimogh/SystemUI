package kotlinx.coroutines.internal;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: LockFreeLinkedList.kt */
/* access modifiers changed from: package-private */
public final class Removed {
    @NotNull
    public final LockFreeLinkedListNode ref;

    public Removed(@NotNull LockFreeLinkedListNode lockFreeLinkedListNode) {
        Intrinsics.checkParameterIsNotNull(lockFreeLinkedListNode, "ref");
        this.ref = lockFreeLinkedListNode;
    }

    @NotNull
    public String toString() {
        return "Removed[" + this.ref + ']';
    }
}
