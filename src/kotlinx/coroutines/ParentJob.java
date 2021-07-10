package kotlinx.coroutines;

import java.util.concurrent.CancellationException;
import org.jetbrains.annotations.NotNull;
/* compiled from: Job.kt */
public interface ParentJob extends Job {
    @NotNull
    CancellationException getChildJobCancellationCause();
}
