package kotlin;

import java.io.Serializable;
import org.jetbrains.annotations.NotNull;
/* compiled from: Lazy.kt */
public final class InitializedLazyImpl<T> implements Lazy<T>, Serializable {
    private final T value;

    public InitializedLazyImpl(T t) {
        this.value = t;
    }

    @Override // kotlin.Lazy
    public T getValue() {
        return this.value;
    }

    @Override // java.lang.Object
    @NotNull
    public String toString() {
        return String.valueOf(getValue());
    }
}
