package kotlin.coroutines;

import java.io.Serializable;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$IntRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: CoroutineContextImpl.kt */
public final class CombinedContext implements CoroutineContext, Serializable {
    private final CoroutineContext.Element element;
    private final CoroutineContext left;

    public CombinedContext(@NotNull CoroutineContext coroutineContext, @NotNull CoroutineContext.Element element) {
        Intrinsics.checkParameterIsNotNull(coroutineContext, "left");
        Intrinsics.checkParameterIsNotNull(element, "element");
        this.left = coroutineContext;
        this.element = element;
    }

    @Override // kotlin.coroutines.CoroutineContext
    @NotNull
    public CoroutineContext plus(@NotNull CoroutineContext coroutineContext) {
        Intrinsics.checkParameterIsNotNull(coroutineContext, "context");
        return CoroutineContext.DefaultImpls.plus(this, coroutineContext);
    }

    @Override // kotlin.coroutines.CoroutineContext
    @Nullable
    public <E extends CoroutineContext.Element> E get(@NotNull CoroutineContext.Key<E> key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        while (true) {
            E e = (E) this.element.get(key);
            if (e != null) {
                return e;
            }
            CoroutineContext coroutineContext = this.left;
            if (!(coroutineContext instanceof CombinedContext)) {
                return (E) coroutineContext.get(key);
            }
            this = (CombinedContext) coroutineContext;
        }
    }

    @Override // kotlin.coroutines.CoroutineContext
    public <R> R fold(R r, @NotNull Function2<? super R, ? super CoroutineContext.Element, ? extends R> function2) {
        Intrinsics.checkParameterIsNotNull(function2, "operation");
        return (R) function2.invoke((Object) this.left.fold(r, function2), this.element);
    }

    @Override // kotlin.coroutines.CoroutineContext
    @NotNull
    public CoroutineContext minusKey(@NotNull CoroutineContext.Key<?> key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        if (this.element.get(key) != null) {
            return this.left;
        }
        CoroutineContext minusKey = this.left.minusKey(key);
        if (minusKey == this.left) {
            return this;
        }
        if (minusKey == EmptyCoroutineContext.INSTANCE) {
            return this.element;
        }
        return new CombinedContext(minusKey, this.element);
    }

    private final int size() {
        int i = 2;
        while (true) {
            CoroutineContext coroutineContext = this.left;
            if (!(coroutineContext instanceof CombinedContext)) {
                coroutineContext = null;
            }
            this = (CombinedContext) coroutineContext;
            if (this == null) {
                return i;
            }
            i++;
        }
    }

    private final boolean contains(CoroutineContext.Element element) {
        return Intrinsics.areEqual(get(element.getKey()), element);
    }

    private final boolean containsAll(CombinedContext combinedContext) {
        while (contains(combinedContext.element)) {
            CoroutineContext coroutineContext = combinedContext.left;
            if (coroutineContext instanceof CombinedContext) {
                combinedContext = (CombinedContext) coroutineContext;
            } else if (coroutineContext != null) {
                return contains((CoroutineContext.Element) coroutineContext);
            } else {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.coroutines.CoroutineContext.Element");
            }
        }
        return false;
    }

    @Override // java.lang.Object
    public boolean equals(@Nullable Object obj) {
        if (this != obj) {
            if (obj instanceof CombinedContext) {
                CombinedContext combinedContext = (CombinedContext) obj;
                if (combinedContext.size() != size() || !combinedContext.containsAll(this)) {
                }
            }
            return false;
        }
        return true;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return this.left.hashCode() + this.element.hashCode();
    }

    @Override // java.lang.Object
    @NotNull
    public String toString() {
        return "[" + ((String) fold("", CombinedContext$toString$1.INSTANCE)) + "]";
    }

    private final Object writeReplace() {
        int size = size();
        CoroutineContext[] coroutineContextArr = new CoroutineContext[size];
        Ref$IntRef ref$IntRef = new Ref$IntRef();
        boolean z = false;
        ref$IntRef.element = 0;
        fold(Unit.INSTANCE, new Function2<Unit, CoroutineContext.Element, Unit>(coroutineContextArr, ref$IntRef) { // from class: kotlin.coroutines.CombinedContext$writeReplace$1
            final /* synthetic */ CoroutineContext[] $elements;
            final /* synthetic */ Ref$IntRef $index;

            {
                this.$elements = r1;
                this.$index = r2;
            }

            /* Return type fixed from 'java.lang.Object' to match base method */
            /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object, java.lang.Object] */
            @Override // kotlin.jvm.functions.Function2
            public /* bridge */ /* synthetic */ Unit invoke(Unit unit, CoroutineContext.Element element) {
                invoke(unit, element);
                return Unit.INSTANCE;
            }

            public final void invoke(@NotNull Unit unit, @NotNull CoroutineContext.Element element) {
                Intrinsics.checkParameterIsNotNull(unit, "<anonymous parameter 0>");
                Intrinsics.checkParameterIsNotNull(element, "element");
                CoroutineContext[] coroutineContextArr2 = this.$elements;
                Ref$IntRef ref$IntRef2 = this.$index;
                int i = ref$IntRef2.element;
                ref$IntRef2.element = i + 1;
                coroutineContextArr2[i] = element;
            }
        });
        if (ref$IntRef.element == size) {
            z = true;
        }
        if (z) {
            return new Serialized(coroutineContextArr);
        }
        throw new IllegalStateException("Check failed.".toString());
    }

    /* compiled from: CoroutineContextImpl.kt */
    private static final class Serialized implements Serializable {
        private static final long serialVersionUID = 0;
        @NotNull
        private final CoroutineContext[] elements;

        public Serialized(@NotNull CoroutineContext[] coroutineContextArr) {
            Intrinsics.checkParameterIsNotNull(coroutineContextArr, "elements");
            this.elements = coroutineContextArr;
        }

        private final Object readResolve() {
            CoroutineContext[] coroutineContextArr = this.elements;
            CoroutineContext coroutineContext = EmptyCoroutineContext.INSTANCE;
            for (CoroutineContext coroutineContext2 : coroutineContextArr) {
                coroutineContext = coroutineContext.plus(coroutineContext2);
            }
            return coroutineContext;
        }
    }
}
