package kotlin.reflect;

import kotlin.jvm.functions.Function0;
import org.jetbrains.annotations.NotNull;
/* compiled from: KProperty.kt */
public interface KProperty0<R> extends KProperty<R>, Function0<R> {

    /* compiled from: KProperty.kt */
    public interface Getter<R> extends Object<R>, Function0<R> {
    }

    R get();

    @NotNull
    Getter<R> getGetter();
}
