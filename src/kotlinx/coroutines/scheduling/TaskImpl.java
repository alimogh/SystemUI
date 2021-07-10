package kotlinx.coroutines.scheduling;

import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.DebugStringsKt;
import org.jetbrains.annotations.NotNull;
/* compiled from: Tasks.kt */
public final class TaskImpl extends Task {
    @NotNull
    public final Runnable block;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public TaskImpl(@NotNull Runnable runnable, long j, @NotNull TaskContext taskContext) {
        super(j, taskContext);
        Intrinsics.checkParameterIsNotNull(runnable, "block");
        Intrinsics.checkParameterIsNotNull(taskContext, "taskContext");
        this.block = runnable;
    }

    @Override // java.lang.Runnable
    public void run() {
        try {
            this.block.run();
        } finally {
            this.taskContext.afterTask();
        }
    }

    @Override // java.lang.Object
    @NotNull
    public String toString() {
        return "Task[" + DebugStringsKt.getClassSimpleName(this.block) + '@' + DebugStringsKt.getHexAddress(this.block) + ", " + this.submissionTime + ", " + this.taskContext + ']';
    }
}
