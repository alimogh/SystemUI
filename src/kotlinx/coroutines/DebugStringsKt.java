package kotlinx.coroutines;

import kotlin.Result;
import kotlin.ResultKt;
import kotlin.coroutines.Continuation;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: DebugStrings.kt */
public final class DebugStringsKt {
    @NotNull
    public static final String getHexAddress(@NotNull Object obj) {
        Intrinsics.checkParameterIsNotNull(obj, "$this$hexAddress");
        String hexString = Integer.toHexString(System.identityHashCode(obj));
        Intrinsics.checkExpressionValueIsNotNull(hexString, "Integer.toHexString(System.identityHashCode(this))");
        return hexString;
    }

    @NotNull
    public static final String toDebugString(@NotNull Continuation<?> continuation) {
        Object obj;
        Intrinsics.checkParameterIsNotNull(continuation, "$this$toDebugString");
        if (continuation instanceof DispatchedContinuation) {
            return continuation.toString();
        }
        try {
            Result.Companion companion = Result.Companion;
            obj = continuation + '@' + getHexAddress(continuation);
            Result.m31constructorimpl(obj);
        } catch (Throwable th) {
            Result.Companion companion2 = Result.Companion;
            obj = ResultKt.createFailure(th);
            Result.m31constructorimpl(obj);
        }
        if (Result.m33exceptionOrNullimpl(obj) != null) {
            obj = continuation.getClass().getName() + '@' + getHexAddress(continuation);
        }
        return (String) obj;
    }

    @NotNull
    public static final String getClassSimpleName(@NotNull Object obj) {
        Intrinsics.checkParameterIsNotNull(obj, "$this$classSimpleName");
        String simpleName = obj.getClass().getSimpleName();
        Intrinsics.checkExpressionValueIsNotNull(simpleName, "this::class.java.simpleName");
        return simpleName;
    }
}
