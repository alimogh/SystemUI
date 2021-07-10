package kotlinx.coroutines.internal;

import java.util.List;
import kotlinx.coroutines.MainCoroutineDispatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: MainDispatcherFactory.kt */
public interface MainDispatcherFactory {
    @NotNull
    MainCoroutineDispatcher createDispatcher(@NotNull List<? extends MainDispatcherFactory> list);

    int getLoadPriority();

    @Nullable
    String hintOnError();
}
