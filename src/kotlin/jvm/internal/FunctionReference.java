package kotlin.jvm.internal;

import kotlin.reflect.KCallable;
import kotlin.reflect.KFunction;
public class FunctionReference extends CallableReference implements FunctionBase, KFunction {
    private final int arity;

    public FunctionReference(int i) {
        this.arity = i;
    }

    public FunctionReference(int i, Object obj) {
        super(obj);
        this.arity = i;
    }

    @Override // kotlin.jvm.internal.FunctionBase
    public int getArity() {
        return this.arity;
    }

    /* access modifiers changed from: protected */
    @Override // kotlin.jvm.internal.CallableReference
    public KFunction getReflected() {
        return (KFunction) super.getReflected();
    }

    /* access modifiers changed from: protected */
    @Override // kotlin.jvm.internal.CallableReference
    public KCallable computeReflected() {
        Reflection.function(this);
        return this;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof FunctionReference) {
            FunctionReference functionReference = (FunctionReference) obj;
            if (getOwner() != null ? getOwner().equals(functionReference.getOwner()) : functionReference.getOwner() == null) {
                if (getName().equals(functionReference.getName()) && getSignature().equals(functionReference.getSignature()) && Intrinsics.areEqual(getBoundReceiver(), functionReference.getBoundReceiver())) {
                    return true;
                }
            }
            return false;
        } else if (obj instanceof KFunction) {
            return obj.equals(compute());
        } else {
            return false;
        }
    }

    @Override // java.lang.Object
    public int hashCode() {
        return (((getOwner() == null ? 0 : getOwner().hashCode() * 31) + getName().hashCode()) * 31) + getSignature().hashCode();
    }

    @Override // java.lang.Object
    public String toString() {
        KCallable compute = compute();
        if (compute != this) {
            return compute.toString();
        }
        if ("<init>".equals(getName())) {
            return "constructor (Kotlin reflection is not available)";
        }
        return "function " + getName() + " (Kotlin reflection is not available)";
    }
}
