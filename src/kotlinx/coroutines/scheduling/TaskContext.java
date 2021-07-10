package kotlinx.coroutines.scheduling;

import org.jetbrains.annotations.NotNull;
/* compiled from: Tasks.kt */
public interface TaskContext {
    void afterTask();

    @NotNull
    TaskMode getTaskMode();
}
