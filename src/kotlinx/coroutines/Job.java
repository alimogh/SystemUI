package kotlinx.coroutines;

import java.util.concurrent.CancellationException;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineExceptionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: Job.kt */
public interface Job extends CoroutineContext.Element {
    public static final Key Key = Key.$$INSTANCE;

    @NotNull
    ChildHandle attachChild(@NotNull ChildJob childJob);

    void cancel(@Nullable CancellationException cancellationException);

    @NotNull
    CancellationException getCancellationException();

    @NotNull
    DisposableHandle invokeOnCompletion(boolean z, boolean z2, @NotNull Function1<? super Throwable, Unit> function1);

    boolean isActive();

    @Nullable
    Object join(@NotNull Continuation<? super Unit> continuation);

    boolean start();

    /* compiled from: Job.kt */
    public static final class Key implements CoroutineContext.Key<Job> {
        static final /* synthetic */ Key $$INSTANCE = new Key();

        static {
            CoroutineExceptionHandler.Key key = CoroutineExceptionHandler.Key;
        }

        private Key() {
        }
    }

    /* compiled from: Job.kt */
    public static final class DefaultImpls {
        public static <R> R fold(Job job, R r, @NotNull Function2<? super R, ? super CoroutineContext.Element, ? extends R> function2) {
            Intrinsics.checkParameterIsNotNull(function2, "operation");
            return (R) CoroutineContext.Element.DefaultImpls.fold(job, r, function2);
        }

        @Nullable
        public static <E extends CoroutineContext.Element> E get(Job job, @NotNull CoroutineContext.Key<E> key) {
            Intrinsics.checkParameterIsNotNull(key, "key");
            return (E) CoroutineContext.Element.DefaultImpls.get(job, key);
        }

        @NotNull
        public static CoroutineContext minusKey(Job job, @NotNull CoroutineContext.Key<?> key) {
            Intrinsics.checkParameterIsNotNull(key, "key");
            return CoroutineContext.Element.DefaultImpls.minusKey(job, key);
        }

        @NotNull
        public static CoroutineContext plus(Job job, @NotNull CoroutineContext coroutineContext) {
            Intrinsics.checkParameterIsNotNull(coroutineContext, "context");
            return CoroutineContext.Element.DefaultImpls.plus(job, coroutineContext);
        }

        public static /* synthetic */ void cancel$default(Job job, CancellationException cancellationException, int i, Object obj) {
            if (obj == null) {
                if ((i & 1) != 0) {
                    cancellationException = null;
                }
                job.cancel(cancellationException);
                return;
            }
            throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: cancel");
        }

        public static /* synthetic */ DisposableHandle invokeOnCompletion$default(Job job, boolean z, boolean z2, Function1 function1, int i, Object obj) {
            if (obj == null) {
                if ((i & 1) != 0) {
                    z = false;
                }
                if ((i & 2) != 0) {
                    z2 = true;
                }
                return job.invokeOnCompletion(z, z2, function1);
            }
            throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: invokeOnCompletion");
        }
    }
}
