package kotlinx.coroutines.internal;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: SystemProps.kt */
/* access modifiers changed from: package-private */
public final /* synthetic */ class SystemPropsKt__SystemPropsKt {
    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    public static final int getAVAILABLE_PROCESSORS() {
        return AVAILABLE_PROCESSORS;
    }

    @Nullable
    public static final String systemProp(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "propertyName");
        try {
            return System.getProperty(str);
        } catch (SecurityException unused) {
            return null;
        }
    }
}
