package kotlinx.coroutines.internal;

import java.util.ArrayDeque;
import kotlin.Pair;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.TuplesKt;
import kotlin.TypeCastException;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.CoroutineStackFrame;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt__StringsJVMKt;
import kotlinx.coroutines.DebugKt;
import org.jetbrains.annotations.NotNull;
/* compiled from: StackTraceRecovery.kt */
public final class StackTraceRecoveryKt {
    private static final String baseContinuationImplClassName;

    /* JADX DEBUG: Multi-variable search result rejected for r1v5, resolved type: java.lang.Object */
    /* JADX DEBUG: Multi-variable search result rejected for r2v5, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    static {
        String str;
        String str2;
        String str3 = "kotlinx.coroutines.internal.StackTraceRecoveryKt";
        String str4 = "kotlin.coroutines.jvm.internal.BaseContinuationImpl";
        try {
            Result.Companion companion = Result.Companion;
            Class<?> cls = Class.forName(str4);
            Intrinsics.checkExpressionValueIsNotNull(cls, "Class.forName(baseContinuationImplClass)");
            String canonicalName = cls.getCanonicalName();
            Result.m31constructorimpl(canonicalName);
            str = canonicalName;
        } catch (Throwable th) {
            Result.Companion companion2 = Result.Companion;
            Object createFailure = ResultKt.createFailure(th);
            Result.m31constructorimpl(createFailure);
            str = createFailure;
        }
        if (Result.m33exceptionOrNullimpl(str) == null) {
            str4 = str;
        }
        baseContinuationImplClassName = str4;
        try {
            Result.Companion companion3 = Result.Companion;
            Class<?> cls2 = Class.forName(str3);
            Intrinsics.checkExpressionValueIsNotNull(cls2, "Class.forName(stackTraceRecoveryClass)");
            String canonicalName2 = cls2.getCanonicalName();
            Result.m31constructorimpl(canonicalName2);
            str2 = canonicalName2;
        } catch (Throwable th2) {
            Result.Companion companion4 = Result.Companion;
            Object createFailure2 = ResultKt.createFailure(th2);
            Result.m31constructorimpl(createFailure2);
            str2 = createFailure2;
        }
        if (Result.m33exceptionOrNullimpl(str2) == null) {
            str3 = str2;
        }
    }

    @NotNull
    public static final <E extends Throwable> E recoverStackTrace(@NotNull E e, @NotNull Continuation<?> continuation) {
        Intrinsics.checkParameterIsNotNull(e, "exception");
        Intrinsics.checkParameterIsNotNull(continuation, "continuation");
        return (!DebugKt.getRECOVER_STACK_TRACES() || !(continuation instanceof CoroutineStackFrame)) ? e : (E) recoverFromStackFrame(e, (CoroutineStackFrame) continuation);
    }

    private static final <E extends Throwable> E recoverFromStackFrame(E e, CoroutineStackFrame coroutineStackFrame) {
        Pair causeAndStacktrace = causeAndStacktrace(e);
        Throwable th = (Throwable) causeAndStacktrace.component1();
        StackTraceElement[] stackTraceElementArr = (StackTraceElement[]) causeAndStacktrace.component2();
        E e2 = (E) ExceptionsConstuctorKt.tryCopyException(th);
        if (e2 == null) {
            return e;
        }
        ArrayDeque<StackTraceElement> createStackTrace = createStackTrace(coroutineStackFrame);
        if (createStackTrace.isEmpty()) {
            return e;
        }
        if (th != e) {
            mergeRecoveredTraces(stackTraceElementArr, createStackTrace);
        }
        createFinalException(th, e2, createStackTrace);
        return e2;
    }

    private static final <E extends Throwable> E createFinalException(E e, E e2, ArrayDeque<StackTraceElement> arrayDeque) {
        arrayDeque.addFirst(artificialFrame("Coroutine boundary"));
        StackTraceElement[] stackTrace = e.getStackTrace();
        Intrinsics.checkExpressionValueIsNotNull(stackTrace, "causeTrace");
        String str = baseContinuationImplClassName;
        Intrinsics.checkExpressionValueIsNotNull(str, "baseContinuationImplClassName");
        int frameIndex = frameIndex(stackTrace, str);
        int i = 0;
        if (frameIndex == -1) {
            Object[] array = arrayDeque.toArray(new StackTraceElement[0]);
            if (array != null) {
                e2.setStackTrace((StackTraceElement[]) array);
                return e2;
            }
            throw new TypeCastException("null cannot be cast to non-null type kotlin.Array<T>");
        }
        StackTraceElement[] stackTraceElementArr = new StackTraceElement[(arrayDeque.size() + frameIndex)];
        for (int i2 = 0; i2 < frameIndex; i2++) {
            stackTraceElementArr[i2] = stackTrace[i2];
        }
        for (StackTraceElement stackTraceElement : arrayDeque) {
            stackTraceElementArr[frameIndex + i] = stackTraceElement;
            i++;
        }
        e2.setStackTrace(stackTraceElementArr);
        return e2;
    }

    private static final <E extends Throwable> Pair<E, StackTraceElement[]> causeAndStacktrace(@NotNull E e) {
        boolean z;
        Throwable cause = e.getCause();
        if (cause == null || !Intrinsics.areEqual(cause.getClass(), e.getClass())) {
            return TuplesKt.to(e, new StackTraceElement[0]);
        }
        StackTraceElement[] stackTrace = e.getStackTrace();
        Intrinsics.checkExpressionValueIsNotNull(stackTrace, "currentTrace");
        int length = stackTrace.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                z = false;
                break;
            }
            StackTraceElement stackTraceElement = stackTrace[i];
            Intrinsics.checkExpressionValueIsNotNull(stackTraceElement, "it");
            if (isArtificial(stackTraceElement)) {
                z = true;
                break;
            }
            i++;
        }
        if (z) {
            return TuplesKt.to(cause, stackTrace);
        }
        return TuplesKt.to(e, new StackTraceElement[0]);
    }

    @NotNull
    public static final <E extends Throwable> E unwrap(@NotNull E e) {
        E e2;
        Intrinsics.checkParameterIsNotNull(e, "exception");
        if (DebugKt.getRECOVER_STACK_TRACES() && (e2 = (E) e.getCause()) != null) {
            boolean z = true;
            if (!(!Intrinsics.areEqual(e2.getClass(), e.getClass()))) {
                StackTraceElement[] stackTrace = e.getStackTrace();
                Intrinsics.checkExpressionValueIsNotNull(stackTrace, "exception.stackTrace");
                int length = stackTrace.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        z = false;
                        break;
                    }
                    StackTraceElement stackTraceElement = stackTrace[i];
                    Intrinsics.checkExpressionValueIsNotNull(stackTraceElement, "it");
                    if (isArtificial(stackTraceElement)) {
                        break;
                    }
                    i++;
                }
                if (z) {
                    return e2;
                }
            }
        }
        return e;
    }

    private static final ArrayDeque<StackTraceElement> createStackTrace(CoroutineStackFrame coroutineStackFrame) {
        ArrayDeque<StackTraceElement> arrayDeque = new ArrayDeque<>();
        StackTraceElement stackTraceElement = coroutineStackFrame.getStackTraceElement();
        if (stackTraceElement != null) {
            arrayDeque.add(stackTraceElement);
        }
        while (true) {
            if (!(coroutineStackFrame instanceof CoroutineStackFrame)) {
                coroutineStackFrame = null;
            }
            if (coroutineStackFrame == null || (coroutineStackFrame = coroutineStackFrame.getCallerFrame()) == null) {
                break;
            }
            StackTraceElement stackTraceElement2 = coroutineStackFrame.getStackTraceElement();
            if (stackTraceElement2 != null) {
                arrayDeque.add(stackTraceElement2);
            }
        }
        return arrayDeque;
    }

    @NotNull
    public static final StackTraceElement artificialFrame(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "message");
        return new StackTraceElement("\b\b\b(" + str, "\b", "\b", -1);
    }

    public static final boolean isArtificial(@NotNull StackTraceElement stackTraceElement) {
        Intrinsics.checkParameterIsNotNull(stackTraceElement, "$this$isArtificial");
        String className = stackTraceElement.getClassName();
        Intrinsics.checkExpressionValueIsNotNull(className, "className");
        return StringsKt__StringsJVMKt.startsWith$default(className, "\b\b\b", false, 2, null);
    }

    private static final boolean elementWiseEquals(@NotNull StackTraceElement stackTraceElement, StackTraceElement stackTraceElement2) {
        return stackTraceElement.getLineNumber() == stackTraceElement2.getLineNumber() && Intrinsics.areEqual(stackTraceElement.getMethodName(), stackTraceElement2.getMethodName()) && Intrinsics.areEqual(stackTraceElement.getFileName(), stackTraceElement2.getFileName()) && Intrinsics.areEqual(stackTraceElement.getClassName(), stackTraceElement2.getClassName());
    }

    private static final int frameIndex(@NotNull StackTraceElement[] stackTraceElementArr, String str) {
        int length = stackTraceElementArr.length;
        for (int i = 0; i < length; i++) {
            if (Intrinsics.areEqual(str, stackTraceElementArr[i].getClassName())) {
                return i;
            }
        }
        return -1;
    }

    private static final void mergeRecoveredTraces(StackTraceElement[] stackTraceElementArr, ArrayDeque<StackTraceElement> arrayDeque) {
        int length = stackTraceElementArr.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                i = -1;
                break;
            } else if (isArtificial(stackTraceElementArr[i])) {
                break;
            } else {
                i++;
            }
        }
        int i2 = i + 1;
        int length2 = stackTraceElementArr.length - 1;
        if (length2 >= i2) {
            while (true) {
                StackTraceElement stackTraceElement = stackTraceElementArr[length2];
                StackTraceElement last = arrayDeque.getLast();
                Intrinsics.checkExpressionValueIsNotNull(last, "result.last");
                if (elementWiseEquals(stackTraceElement, last)) {
                    arrayDeque.removeLast();
                }
                arrayDeque.addFirst(stackTraceElementArr[length2]);
                if (length2 != i2) {
                    length2--;
                } else {
                    return;
                }
            }
        }
    }
}
