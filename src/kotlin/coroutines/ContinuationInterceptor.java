package kotlin.coroutines;

import kotlin.TypeCastException;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: ContinuationInterceptor.kt */
public interface ContinuationInterceptor extends CoroutineContext.Element {
    public static final Key Key = Key.$$INSTANCE;

    @NotNull
    <T> Continuation<T> interceptContinuation(@NotNull Continuation<? super T> continuation);

    void releaseInterceptedContinuation(@NotNull Continuation<?> continuation);

    /* compiled from: ContinuationInterceptor.kt */
    public static final class Key implements CoroutineContext.Key<ContinuationInterceptor> {
        static final /* synthetic */ Key $$INSTANCE = new Key();

        private Key() {
        }
    }

    /* compiled from: ContinuationInterceptor.kt */
    public static final class DefaultImpls {
        public static void releaseInterceptedContinuation(ContinuationInterceptor continuationInterceptor, @NotNull Continuation<?> continuation) {
            Intrinsics.checkParameterIsNotNull(continuation, "continuation");
        }

        @Nullable
        public static <E extends CoroutineContext.Element> E get(ContinuationInterceptor continuationInterceptor, @NotNull CoroutineContext.Key<E> key) {
            Intrinsics.checkParameterIsNotNull(key, "key");
            if (key != ContinuationInterceptor.Key) {
                return null;
            }
            if (continuationInterceptor != null) {
                return continuationInterceptor;
            }
            throw new TypeCastException("null cannot be cast to non-null type E");
        }

        @NotNull
        public static CoroutineContext minusKey(ContinuationInterceptor continuationInterceptor, @NotNull CoroutineContext.Key<?> key) {
            Intrinsics.checkParameterIsNotNull(key, "key");
            return key == ContinuationInterceptor.Key ? EmptyCoroutineContext.INSTANCE : continuationInterceptor;
        }
    }
}
