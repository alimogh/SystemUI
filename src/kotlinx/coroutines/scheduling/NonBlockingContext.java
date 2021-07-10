package kotlinx.coroutines.scheduling;

import org.jetbrains.annotations.NotNull;
/* compiled from: Tasks.kt */
public final class NonBlockingContext implements TaskContext {
    public static final NonBlockingContext INSTANCE = new NonBlockingContext();
    @NotNull
    private static final TaskMode taskMode = TaskMode.NON_BLOCKING;

    @Override // kotlinx.coroutines.scheduling.TaskContext
    public void afterTask() {
    }

    private NonBlockingContext() {
    }

    @Override // kotlinx.coroutines.scheduling.TaskContext
    @NotNull
    public TaskMode getTaskMode() {
        return taskMode;
    }
}
