package kotlin.jvm;

import org.jetbrains.annotations.Nullable;
/* compiled from: KotlinReflectionNotSupportedError.kt */
public class KotlinReflectionNotSupportedError extends Error {
    public KotlinReflectionNotSupportedError() {
        super("Kotlin reflection implementation is not found at runtime. Make sure you have kotlin-reflect.jar in the classpath");
    }

    public KotlinReflectionNotSupportedError(@Nullable String str) {
        super(str);
    }
}
