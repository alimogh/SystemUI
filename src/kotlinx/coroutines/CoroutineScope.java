package kotlinx.coroutines;

import kotlin.coroutines.CoroutineContext;
import org.jetbrains.annotations.NotNull;
/* compiled from: CoroutineScope.kt */
public interface CoroutineScope {
    @NotNull
    CoroutineContext getCoroutineContext();
}
