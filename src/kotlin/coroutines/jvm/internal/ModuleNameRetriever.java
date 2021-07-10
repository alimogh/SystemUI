package kotlin.coroutines.jvm.internal;

import java.lang.reflect.Method;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: DebugMetadata.kt */
/* access modifiers changed from: package-private */
public final class ModuleNameRetriever {
    public static final ModuleNameRetriever INSTANCE = new ModuleNameRetriever();
    @Nullable
    public static Cache cache;
    private static final Cache notOnJava9 = new Cache(null, null, null);

    /* compiled from: DebugMetadata.kt */
    /* access modifiers changed from: private */
    public static final class Cache {
        @Nullable
        public final Method getDescriptorMethod;
        @Nullable
        public final Method getModuleMethod;
        @Nullable
        public final Method nameMethod;

        public Cache(@Nullable Method method, @Nullable Method method2, @Nullable Method method3) {
            this.getModuleMethod = method;
            this.getDescriptorMethod = method2;
            this.nameMethod = method3;
        }
    }

    private ModuleNameRetriever() {
    }

    @Nullable
    public final String getModuleName(@NotNull BaseContinuationImpl baseContinuationImpl) {
        Method method;
        Object invoke;
        Method method2;
        Object invoke2;
        Intrinsics.checkParameterIsNotNull(baseContinuationImpl, "continuation");
        Cache cache2 = cache;
        if (cache2 == null) {
            cache2 = buildCache(baseContinuationImpl);
        }
        String str = null;
        if (cache2 == notOnJava9 || (method = cache2.getModuleMethod) == null || (invoke = method.invoke(baseContinuationImpl.getClass(), new Object[0])) == null || (method2 = cache2.getDescriptorMethod) == null || (invoke2 = method2.invoke(invoke, new Object[0])) == null) {
            return null;
        }
        Method method3 = cache2.nameMethod;
        Object invoke3 = method3 != null ? method3.invoke(invoke2, new Object[0]) : null;
        if (invoke3 instanceof String) {
            str = invoke3;
        }
        return str;
    }

    private final Cache buildCache(BaseContinuationImpl baseContinuationImpl) {
        try {
            Cache cache2 = new Cache(Class.class.getDeclaredMethod("getModule", new Class[0]), baseContinuationImpl.getClass().getClassLoader().loadClass("java.lang.Module").getDeclaredMethod("getDescriptor", new Class[0]), baseContinuationImpl.getClass().getClassLoader().loadClass("java.lang.module.ModuleDescriptor").getDeclaredMethod("name", new Class[0]));
            cache = cache2;
            return cache2;
        } catch (Exception unused) {
            Cache cache3 = notOnJava9;
            cache = cache3;
            return cache3;
        }
    }
}
