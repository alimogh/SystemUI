package kotlin.sequences;

import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
/* compiled from: Sequence.kt */
public interface Sequence<T> {
    @NotNull
    Iterator<T> iterator();
}
