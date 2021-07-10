package kotlinx.coroutines;

import org.jetbrains.annotations.NotNull;
/* compiled from: Job.kt */
public interface ChildJob extends Job {
    void parentCancelled(@NotNull ParentJob parentJob);
}
