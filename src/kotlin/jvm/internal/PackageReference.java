package kotlin.jvm.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: PackageReference.kt */
public final class PackageReference implements ClassBasedDeclarationContainer {
    @NotNull
    private final Class<?> jClass;

    public PackageReference(@NotNull Class<?> cls, @NotNull String str) {
        Intrinsics.checkParameterIsNotNull(cls, "jClass");
        Intrinsics.checkParameterIsNotNull(str, "moduleName");
        this.jClass = cls;
    }

    @Override // kotlin.jvm.internal.ClassBasedDeclarationContainer
    @NotNull
    public Class<?> getJClass() {
        return this.jClass;
    }

    public boolean equals(@Nullable Object obj) {
        return (obj instanceof PackageReference) && Intrinsics.areEqual(getJClass(), ((PackageReference) obj).getJClass());
    }

    public int hashCode() {
        return getJClass().hashCode();
    }

    @NotNull
    public String toString() {
        return getJClass().toString() + " (Kotlin reflection is not available)";
    }
}
