package kotlin.reflect;

import org.jetbrains.annotations.NotNull;
/* compiled from: KCallable.kt */
public interface KCallable<R> {
    R call(@NotNull Object... objArr);
}
