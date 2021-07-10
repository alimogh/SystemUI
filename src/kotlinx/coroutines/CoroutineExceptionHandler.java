package kotlinx.coroutines;

import kotlin.coroutines.CoroutineContext;
import org.jetbrains.annotations.NotNull;
/* compiled from: CoroutineExceptionHandler.kt */
public interface CoroutineExceptionHandler extends CoroutineContext.Element {
    public static final Key Key = Key.$$INSTANCE;

    void handleException(@NotNull CoroutineContext coroutineContext, @NotNull Throwable th);

    /* compiled from: CoroutineExceptionHandler.kt */
    public static final class Key implements CoroutineContext.Key<CoroutineExceptionHandler> {
        static final /* synthetic */ Key $$INSTANCE = new Key();

        private Key() {
        }
    }
}
