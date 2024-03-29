package kotlinx.coroutines;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.Nullable;
/* compiled from: CompletionHandler.kt */
public abstract class CancelHandlerBase implements Function1<Throwable, Unit> {
    public abstract void invoke(@Nullable Throwable th);
}
