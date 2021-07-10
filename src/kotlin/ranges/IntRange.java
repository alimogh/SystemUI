package kotlin.ranges;

import kotlin.jvm.internal.DefaultConstructorMarker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: Ranges.kt */
public final class IntRange extends IntProgression {
    public static final Companion Companion = new Companion(null);
    @NotNull
    private static final IntRange EMPTY = new IntRange(1, 0);

    public IntRange(int i, int i2) {
        super(i, i2, 1);
    }

    @NotNull
    public Integer getStart() {
        return Integer.valueOf(getFirst());
    }

    @NotNull
    public Integer getEndInclusive() {
        return Integer.valueOf(getLast());
    }

    @Override // kotlin.ranges.IntProgression
    public boolean isEmpty() {
        return getFirst() > getLast();
    }

    @Override // kotlin.ranges.IntProgression, java.lang.Object
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof IntRange) {
            if (!isEmpty() || !((IntRange) obj).isEmpty()) {
                IntRange intRange = (IntRange) obj;
                if (!(getFirst() == intRange.getFirst() && getLast() == intRange.getLast())) {
                }
            }
            return true;
        }
        return false;
    }

    @Override // kotlin.ranges.IntProgression, java.lang.Object
    public int hashCode() {
        if (isEmpty()) {
            return -1;
        }
        return getLast() + (getFirst() * 31);
    }

    @Override // kotlin.ranges.IntProgression, java.lang.Object
    @NotNull
    public String toString() {
        return getFirst() + ".." + getLast();
    }

    /* compiled from: Ranges.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final IntRange getEMPTY() {
            return IntRange.EMPTY;
        }
    }
}
