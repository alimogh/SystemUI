package kotlinx.coroutines.android;

import android.os.Build;
import android.support.annotation.Keep;
import java.lang.Thread;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.coroutines.AbstractCoroutineContextElement;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.PropertyReference1Impl;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KProperty;
import kotlinx.coroutines.CoroutineExceptionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: AndroidExceptionPreHandler.kt */
@Keep
public final class AndroidExceptionPreHandler extends AbstractCoroutineContextElement implements CoroutineExceptionHandler, Function0<Method> {
    static final /* synthetic */ KProperty[] $$delegatedProperties;
    private final Lazy preHandler$delegate = LazyKt.lazy(this);

    static {
        PropertyReference1Impl propertyReference1Impl = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(AndroidExceptionPreHandler.class), "preHandler", "getPreHandler()Ljava/lang/reflect/Method;");
        Reflection.property1(propertyReference1Impl);
        $$delegatedProperties = new KProperty[]{propertyReference1Impl};
    }

    private final Method getPreHandler() {
        Lazy lazy = this.preHandler$delegate;
        KProperty kProperty = $$delegatedProperties[0];
        return (Method) lazy.getValue();
    }

    public AndroidExceptionPreHandler() {
        super(CoroutineExceptionHandler.Key);
    }

    @Override // kotlin.jvm.functions.Function0
    @Nullable
    public Method invoke() {
        boolean z = false;
        try {
            Method declaredMethod = Thread.class.getDeclaredMethod("getUncaughtExceptionPreHandler", new Class[0]);
            Intrinsics.checkExpressionValueIsNotNull(declaredMethod, "it");
            if (Modifier.isPublic(declaredMethod.getModifiers()) && Modifier.isStatic(declaredMethod.getModifiers())) {
                z = true;
            }
            if (z) {
                return declaredMethod;
            }
            return null;
        } catch (Throwable unused) {
            return null;
        }
    }

    @Override // kotlinx.coroutines.CoroutineExceptionHandler
    public void handleException(@NotNull CoroutineContext coroutineContext, @NotNull Throwable th) {
        Intrinsics.checkParameterIsNotNull(coroutineContext, "context");
        Intrinsics.checkParameterIsNotNull(th, "exception");
        Thread currentThread = Thread.currentThread();
        if (Build.VERSION.SDK_INT >= 28) {
            Intrinsics.checkExpressionValueIsNotNull(currentThread, "thread");
            currentThread.getUncaughtExceptionHandler().uncaughtException(currentThread, th);
            return;
        }
        Method preHandler = getPreHandler();
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = null;
        Object invoke = preHandler != null ? preHandler.invoke(null, new Object[0]) : null;
        if (invoke instanceof Thread.UncaughtExceptionHandler) {
            uncaughtExceptionHandler = invoke;
        }
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler2 = uncaughtExceptionHandler;
        if (uncaughtExceptionHandler2 != null) {
            uncaughtExceptionHandler2.uncaughtException(currentThread, th);
        }
    }
}
