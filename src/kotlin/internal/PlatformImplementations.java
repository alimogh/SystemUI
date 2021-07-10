package kotlin.internal;

import java.lang.reflect.Method;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: PlatformImplementations.kt */
public class PlatformImplementations {

    /* compiled from: PlatformImplementations.kt */
    /* access modifiers changed from: private */
    public static final class ReflectAddSuppressedMethod {
        @Nullable
        public static final Method method;

        /* JADX WARNING: Removed duplicated region for block: B:13:0x0042 A[EDGE_INSN: B:13:0x0042->B:11:0x0042 ?: BREAK  , SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:9:0x003e A[LOOP:0: B:1:0x000e->B:9:0x003e, LOOP_END] */
        static {
            /*
                java.lang.Class<java.lang.Throwable> r0 = java.lang.Throwable.class
                java.lang.reflect.Method[] r1 = r0.getMethods()
                java.lang.String r2 = "throwableClass.methods"
                kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r1, r2)
                int r2 = r1.length
                r3 = 0
                r4 = r3
            L_0x000e:
                if (r4 >= r2) goto L_0x0041
                r5 = r1[r4]
                java.lang.String r6 = "it"
                kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r5, r6)
                java.lang.String r6 = r5.getName()
                java.lang.String r7 = "addSuppressed"
                boolean r6 = kotlin.jvm.internal.Intrinsics.areEqual(r6, r7)
                if (r6 == 0) goto L_0x003a
                java.lang.Class[] r6 = r5.getParameterTypes()
                java.lang.String r7 = "it.parameterTypes"
                kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r6, r7)
                java.lang.Object r6 = kotlin.collections.ArraysKt.singleOrNull(r6)
                java.lang.Class r6 = (java.lang.Class) r6
                boolean r6 = kotlin.jvm.internal.Intrinsics.areEqual(r6, r0)
                if (r6 == 0) goto L_0x003a
                r6 = 1
                goto L_0x003b
            L_0x003a:
                r6 = r3
            L_0x003b:
                if (r6 == 0) goto L_0x003e
                goto L_0x0042
            L_0x003e:
                int r4 = r4 + 1
                goto L_0x000e
            L_0x0041:
                r5 = 0
            L_0x0042:
                kotlin.internal.PlatformImplementations.ReflectAddSuppressedMethod.method = r5
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: kotlin.internal.PlatformImplementations.ReflectAddSuppressedMethod.<clinit>():void");
        }
    }

    public void addSuppressed(@NotNull Throwable th, @NotNull Throwable th2) {
        Intrinsics.checkParameterIsNotNull(th, "cause");
        Intrinsics.checkParameterIsNotNull(th2, "exception");
        Method method = ReflectAddSuppressedMethod.method;
        if (method != null) {
            method.invoke(th, th2);
        }
    }
}
