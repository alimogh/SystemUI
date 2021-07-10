package kotlin.coroutines;

import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;
/* compiled from: CoroutineContext.kt */
/* access modifiers changed from: package-private */
public final class CoroutineContext$plus$1 extends Lambda implements Function2<CoroutineContext, CoroutineContext.Element, CoroutineContext> {
    public static final CoroutineContext$plus$1 INSTANCE = new CoroutineContext$plus$1();

    CoroutineContext$plus$1() {
        super(2);
    }

    @NotNull
    public final CoroutineContext invoke(@NotNull CoroutineContext coroutineContext, @NotNull CoroutineContext.Element element) {
        Intrinsics.checkParameterIsNotNull(coroutineContext, "acc");
        Intrinsics.checkParameterIsNotNull(element, "element");
        CoroutineContext minusKey = coroutineContext.minusKey(element.getKey());
        if (minusKey == EmptyCoroutineContext.INSTANCE) {
            return element;
        }
        ContinuationInterceptor continuationInterceptor = (ContinuationInterceptor) minusKey.get(ContinuationInterceptor.Key);
        if (continuationInterceptor == null) {
            return new CombinedContext(minusKey, element);
        }
        CoroutineContext minusKey2 = minusKey.minusKey(ContinuationInterceptor.Key);
        if (minusKey2 == EmptyCoroutineContext.INSTANCE) {
            return new CombinedContext(element, continuationInterceptor);
        }
        return new CombinedContext(new CombinedContext(minusKey2, element), continuationInterceptor);
    }
}
