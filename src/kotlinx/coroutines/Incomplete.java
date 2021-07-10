package kotlinx.coroutines;

import org.jetbrains.annotations.Nullable;
/* compiled from: JobSupport.kt */
public interface Incomplete {
    @Nullable
    NodeList getList();

    boolean isActive();
}
