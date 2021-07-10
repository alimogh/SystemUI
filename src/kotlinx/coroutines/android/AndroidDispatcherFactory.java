package kotlinx.coroutines.android;

import android.os.Looper;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.internal.MainDispatcherFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: HandlerDispatcher.kt */
public final class AndroidDispatcherFactory implements MainDispatcherFactory {
    @Override // kotlinx.coroutines.internal.MainDispatcherFactory
    public int getLoadPriority() {
        return 1073741823;
    }

    @Override // kotlinx.coroutines.internal.MainDispatcherFactory
    @Nullable
    public String hintOnError() {
        return "For tests Dispatchers.setMain from kotlinx-coroutines-test module can be used";
    }

    @Override // kotlinx.coroutines.internal.MainDispatcherFactory
    @NotNull
    public HandlerContext createDispatcher(@NotNull List<? extends MainDispatcherFactory> list) {
        Intrinsics.checkParameterIsNotNull(list, "allFactories");
        Looper mainLooper = Looper.getMainLooper();
        Intrinsics.checkExpressionValueIsNotNull(mainLooper, "Looper.getMainLooper()");
        return new HandlerContext(HandlerDispatcherKt.asHandler(mainLooper, true), "Main");
    }
}
