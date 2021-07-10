package kotlin;

import java.io.Serializable;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: Result.kt */
public final class Result<T> implements Serializable {
    public static final Companion Companion = new Companion(null);
    @Nullable
    private final Object value;

    @NotNull
    /* renamed from: constructor-impl  reason: not valid java name */
    public static Object m31constructorimpl(@Nullable Object obj) {
        return obj;
    }

    /* renamed from: equals-impl  reason: not valid java name */
    public static boolean m32equalsimpl(Object obj, @Nullable Object obj2) {
        return (obj2 instanceof Result) && Intrinsics.areEqual(obj, ((Result) obj2).m38unboximpl());
    }

    /* renamed from: hashCode-impl  reason: not valid java name */
    public static int m34hashCodeimpl(Object obj) {
        if (obj != null) {
            return obj.hashCode();
        }
        return 0;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        return m32equalsimpl(this.value, obj);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return m34hashCodeimpl(this.value);
    }

    @Override // java.lang.Object
    @NotNull
    public String toString() {
        return m37toStringimpl(this.value);
    }

    @Nullable
    /* renamed from: unbox-impl  reason: not valid java name */
    public final /* synthetic */ Object m38unboximpl() {
        return this.value;
    }

    /* renamed from: isSuccess-impl  reason: not valid java name */
    public static final boolean m36isSuccessimpl(Object obj) {
        return !(obj instanceof Failure);
    }

    /* renamed from: isFailure-impl  reason: not valid java name */
    public static final boolean m35isFailureimpl(Object obj) {
        return obj instanceof Failure;
    }

    @Nullable
    /* renamed from: exceptionOrNull-impl  reason: not valid java name */
    public static final Throwable m33exceptionOrNullimpl(Object obj) {
        if (obj instanceof Failure) {
            return ((Failure) obj).exception;
        }
        return null;
    }

    @NotNull
    /* renamed from: toString-impl  reason: not valid java name */
    public static String m37toStringimpl(Object obj) {
        if (obj instanceof Failure) {
            return obj.toString();
        }
        return "Success(" + obj + ')';
    }

    /* compiled from: Result.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    /* compiled from: Result.kt */
    public static final class Failure implements Serializable {
        @NotNull
        public final Throwable exception;

        public Failure(@NotNull Throwable th) {
            Intrinsics.checkParameterIsNotNull(th, "exception");
            this.exception = th;
        }

        @Override // java.lang.Object
        public boolean equals(@Nullable Object obj) {
            return (obj instanceof Failure) && Intrinsics.areEqual(this.exception, ((Failure) obj).exception);
        }

        @Override // java.lang.Object
        public int hashCode() {
            return this.exception.hashCode();
        }

        @Override // java.lang.Object
        @NotNull
        public String toString() {
            return "Failure(" + this.exception + ')';
        }
    }
}
