package kotlinx.coroutines;

import org.jetbrains.annotations.NotNull;
/* compiled from: CancellableContinuationImpl.kt */
final class Active implements NotCompleted {
    public static final Active INSTANCE = new Active();

    @NotNull
    public String toString() {
        return "Active";
    }

    private Active() {
    }
}
