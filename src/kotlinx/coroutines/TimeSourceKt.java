package kotlinx.coroutines;

import org.jetbrains.annotations.Nullable;
/* compiled from: TimeSource.kt */
public final class TimeSourceKt {
    @Nullable
    private static TimeSource timeSource;

    @Nullable
    public static final TimeSource getTimeSource() {
        return timeSource;
    }
}
