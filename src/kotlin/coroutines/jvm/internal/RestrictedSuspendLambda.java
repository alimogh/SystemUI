package kotlin.coroutines.jvm.internal;

import kotlin.coroutines.Continuation;
import kotlin.jvm.internal.FunctionBase;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: ContinuationImpl.kt */
public abstract class RestrictedSuspendLambda extends RestrictedContinuationImpl implements FunctionBase<Object> {
    private final int arity;

    @Override // kotlin.jvm.internal.FunctionBase
    public int getArity() {
        return this.arity;
    }

    public RestrictedSuspendLambda(int i, @Nullable Continuation<Object> continuation) {
        super(continuation);
        this.arity = i;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl, java.lang.Object
    @NotNull
    public String toString() {
        if (getCompletion() != null) {
            return super.toString();
        }
        String renderLambdaToString = Reflection.renderLambdaToString(this);
        Intrinsics.checkExpressionValueIsNotNull(renderLambdaToString, "Reflection.renderLambdaToString(this)");
        return renderLambdaToString;
    }
}
