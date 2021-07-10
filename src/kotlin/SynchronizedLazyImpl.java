package kotlin;

import java.io.Serializable;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: LazyJVM.kt */
/* access modifiers changed from: package-private */
public final class SynchronizedLazyImpl<T> implements Lazy<T>, Serializable {
    private volatile Object _value;
    private Function0<? extends T> initializer;
    private final Object lock;

    public SynchronizedLazyImpl(@NotNull Function0<? extends T> function0, @Nullable Object obj) {
        Intrinsics.checkParameterIsNotNull(function0, "initializer");
        this.initializer = function0;
        this._value = UNINITIALIZED_VALUE.INSTANCE;
        this.lock = obj == null ? this : obj;
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ SynchronizedLazyImpl(Function0 function0, Object obj, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this(function0, (i & 2) != 0 ? null : obj);
    }

    @Override // kotlin.Lazy
    public T getValue() {
        T t;
        T t2 = (T) this._value;
        if (t2 != UNINITIALIZED_VALUE.INSTANCE) {
            return t2;
        }
        synchronized (this.lock) {
            t = (T) this._value;
            if (t == UNINITIALIZED_VALUE.INSTANCE) {
                Function0<? extends T> function0 = this.initializer;
                if (function0 != null) {
                    t = (T) function0.invoke();
                    this._value = t;
                    this.initializer = null;
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            }
        }
        return t;
    }

    public boolean isInitialized() {
        return this._value != UNINITIALIZED_VALUE.INSTANCE;
    }

    @Override // java.lang.Object
    @NotNull
    public String toString() {
        return isInitialized() ? String.valueOf(getValue()) : "Lazy value not initialized yet.";
    }

    private final Object writeReplace() {
        return new InitializedLazyImpl(getValue());
    }
}
