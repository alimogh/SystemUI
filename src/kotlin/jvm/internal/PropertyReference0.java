package kotlin.jvm.internal;

import kotlin.reflect.KCallable;
import kotlin.reflect.KProperty0;
public abstract class PropertyReference0 extends PropertyReference implements KProperty0 {
    /* access modifiers changed from: protected */
    @Override // kotlin.jvm.internal.CallableReference
    public KCallable computeReflected() {
        Reflection.property0(this);
        return this;
    }

    @Override // kotlin.jvm.functions.Function0
    public Object invoke() {
        return get();
    }

    @Override // kotlin.reflect.KProperty0
    public KProperty0.Getter getGetter() {
        return ((KProperty0) getReflected()).getGetter();
    }
}
