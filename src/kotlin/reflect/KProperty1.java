package kotlin.reflect;

import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
/* compiled from: KProperty.kt */
public interface KProperty1<T, R> extends KProperty<R>, Function1<T, R> {

    /* compiled from: KProperty.kt */
    public interface Getter<T, R> extends Object<R>, Function1<T, R> {
    }

    R get(T t);

    @NotNull
    Getter<T, R> getGetter();
}
