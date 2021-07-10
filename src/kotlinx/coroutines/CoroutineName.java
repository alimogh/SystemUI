package kotlinx.coroutines;

import kotlin.coroutines.AbstractCoroutineContextElement;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: CoroutineName.kt */
public final class CoroutineName extends AbstractCoroutineContextElement {
    public static final Key Key = new Key(null);
    @NotNull
    private final String name;

    public boolean equals(@Nullable Object obj) {
        if (this != obj) {
            return (obj instanceof CoroutineName) && Intrinsics.areEqual(this.name, ((CoroutineName) obj).name);
        }
        return true;
    }

    public int hashCode() {
        String str = this.name;
        if (str != null) {
            return str.hashCode();
        }
        return 0;
    }

    @NotNull
    public final String getName() {
        return this.name;
    }

    /* compiled from: CoroutineName.kt */
    public static final class Key implements CoroutineContext.Key<CoroutineName> {
        private Key() {
        }

        public /* synthetic */ Key(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    @NotNull
    public String toString() {
        return "CoroutineName(" + this.name + ')';
    }
}
