package kotlin.jvm.internal;

import java.io.Serializable;
import org.jetbrains.annotations.NotNull;
/* compiled from: Lambda.kt */
public abstract class Lambda<R> implements FunctionBase<R>, Serializable {
    private final int arity;

    public Lambda(int i) {
        this.arity = i;
    }

    @Override // kotlin.jvm.internal.FunctionBase
    public int getArity() {
        return this.arity;
    }

    @Override // java.lang.Object
    @NotNull
    public String toString() {
        String renderLambdaToString = Reflection.renderLambdaToString((Lambda) this);
        Intrinsics.checkExpressionValueIsNotNull(renderLambdaToString, "Reflection.renderLambdaToString(this)");
        return renderLambdaToString;
    }
}
