package kotlin.coroutines.jvm.internal;

import org.jetbrains.annotations.Nullable;
/* compiled from: CoroutineStackFrame.kt */
public interface CoroutineStackFrame {
    @Nullable
    CoroutineStackFrame getCallerFrame();

    @Nullable
    StackTraceElement getStackTraceElement();
}
