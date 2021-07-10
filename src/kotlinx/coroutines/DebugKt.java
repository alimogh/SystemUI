package kotlinx.coroutines;

import java.util.concurrent.atomic.AtomicLong;
import org.jetbrains.annotations.NotNull;
/* compiled from: Debug.kt */
public final class DebugKt {
    private static final boolean ASSERTIONS_ENABLED = CoroutineId.class.desiredAssertionStatus();
    @NotNull
    private static final AtomicLong COROUTINE_ID = new AtomicLong(0);
    private static final boolean DEBUG;
    private static final boolean RECOVER_STACK_TRACES;

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002d, code lost:
        if (r0.equals("auto") != false) goto L_0x002f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0042, code lost:
        if (r0.equals("on") != false) goto L_0x004d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004b, code lost:
        if (r0.equals("") != false) goto L_0x004d;
     */
    static {
        /*
            java.lang.Class<kotlinx.coroutines.CoroutineId> r0 = kotlinx.coroutines.CoroutineId.class
            boolean r0 = r0.desiredAssertionStatus()
            kotlinx.coroutines.DebugKt.ASSERTIONS_ENABLED = r0
            java.lang.String r0 = "kotlinx.coroutines.debug"
            java.lang.String r0 = kotlinx.coroutines.internal.SystemPropsKt.systemProp(r0)
            r1 = 0
            r2 = 1
            if (r0 != 0) goto L_0x0013
            goto L_0x002f
        L_0x0013:
            int r3 = r0.hashCode()
            if (r3 == 0) goto L_0x0045
            r4 = 3551(0xddf, float:4.976E-42)
            if (r3 == r4) goto L_0x003c
            r4 = 109935(0x1ad6f, float:1.54052E-40)
            if (r3 == r4) goto L_0x0032
            r4 = 3005871(0x2dddaf, float:4.212122E-39)
            if (r3 != r4) goto L_0x0067
            java.lang.String r3 = "auto"
            boolean r3 = r0.equals(r3)
            if (r3 == 0) goto L_0x0067
        L_0x002f:
            boolean r0 = kotlinx.coroutines.DebugKt.ASSERTIONS_ENABLED
            goto L_0x004e
        L_0x0032:
            java.lang.String r3 = "off"
            boolean r3 = r0.equals(r3)
            if (r3 == 0) goto L_0x0067
            r0 = r1
            goto L_0x004e
        L_0x003c:
            java.lang.String r3 = "on"
            boolean r3 = r0.equals(r3)
            if (r3 == 0) goto L_0x0067
            goto L_0x004d
        L_0x0045:
            java.lang.String r3 = ""
            boolean r3 = r0.equals(r3)
            if (r3 == 0) goto L_0x0067
        L_0x004d:
            r0 = r2
        L_0x004e:
            kotlinx.coroutines.DebugKt.DEBUG = r0
            if (r0 == 0) goto L_0x005b
            java.lang.String r0 = "kotlinx.coroutines.stacktrace.recovery"
            boolean r0 = kotlinx.coroutines.internal.SystemPropsKt.systemProp(r0, r2)
            if (r0 == 0) goto L_0x005b
            r1 = r2
        L_0x005b:
            kotlinx.coroutines.DebugKt.RECOVER_STACK_TRACES = r1
            java.util.concurrent.atomic.AtomicLong r0 = new java.util.concurrent.atomic.AtomicLong
            r1 = 0
            r0.<init>(r1)
            kotlinx.coroutines.DebugKt.COROUTINE_ID = r0
            return
        L_0x0067:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "System property 'kotlinx.coroutines.debug' has unrecognized value '"
            r1.append(r2)
            r1.append(r0)
            r0 = 39
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            java.lang.IllegalStateException r1 = new java.lang.IllegalStateException
            java.lang.String r0 = r0.toString()
            r1.<init>(r0)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: kotlinx.coroutines.DebugKt.<clinit>():void");
    }

    public static final boolean getASSERTIONS_ENABLED() {
        return ASSERTIONS_ENABLED;
    }

    public static final boolean getDEBUG() {
        return DEBUG;
    }

    public static final boolean getRECOVER_STACK_TRACES() {
        return RECOVER_STACK_TRACES;
    }

    @NotNull
    public static final AtomicLong getCOROUTINE_ID() {
        return COROUTINE_ID;
    }
}
