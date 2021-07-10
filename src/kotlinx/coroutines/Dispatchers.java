package kotlinx.coroutines;

import kotlinx.coroutines.internal.MainDispatcherLoader;
import kotlinx.coroutines.scheduling.DefaultScheduler;
import org.jetbrains.annotations.NotNull;
/* compiled from: Dispatchers.kt */
public final class Dispatchers {
    @NotNull
    private static final CoroutineDispatcher Default = CoroutineContextKt.createDefaultDispatcher();
    @NotNull
    private static final CoroutineDispatcher IO = DefaultScheduler.INSTANCE.getIO();

    static {
        Unconfined unconfined = Unconfined.INSTANCE;
    }

    @NotNull
    public static final CoroutineDispatcher getDefault() {
        return Default;
    }

    @NotNull
    public static final MainCoroutineDispatcher getMain() {
        return MainDispatcherLoader.dispatcher;
    }

    @NotNull
    public static final CoroutineDispatcher getIO() {
        return IO;
    }
}
