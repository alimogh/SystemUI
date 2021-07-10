package kotlinx.coroutines.internal;

import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.MainCoroutineDispatcher;
import org.jetbrains.annotations.NotNull;
/* compiled from: MainDispatchers.kt */
public final class MainDispatchersKt {
    @NotNull
    public static final MainCoroutineDispatcher tryCreateDispatcher(@NotNull MainDispatcherFactory mainDispatcherFactory, @NotNull List<? extends MainDispatcherFactory> list) {
        Intrinsics.checkParameterIsNotNull(mainDispatcherFactory, "$this$tryCreateDispatcher");
        Intrinsics.checkParameterIsNotNull(list, "factories");
        try {
            return mainDispatcherFactory.createDispatcher(list);
        } catch (Throwable th) {
            return new MissingMainCoroutineDispatcher(th, mainDispatcherFactory.hintOnError());
        }
    }
}
