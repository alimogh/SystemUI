package kotlinx.coroutines.scheduling;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.ExecutorCoroutineDispatcher;
import org.jetbrains.annotations.NotNull;
/* compiled from: Dispatcher.kt */
/* access modifiers changed from: package-private */
public final class LimitingDispatcher extends ExecutorCoroutineDispatcher implements TaskContext, Executor {
    private static final AtomicIntegerFieldUpdater inFlightTasks$FU = AtomicIntegerFieldUpdater.newUpdater(LimitingDispatcher.class, "inFlightTasks");
    @NotNull
    private final ExperimentalCoroutineDispatcher dispatcher;
    private volatile int inFlightTasks = 0;
    private final int parallelism;
    private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();
    @NotNull
    private final TaskMode taskMode;

    @Override // kotlinx.coroutines.scheduling.TaskContext
    @NotNull
    public TaskMode getTaskMode() {
        return this.taskMode;
    }

    public LimitingDispatcher(@NotNull ExperimentalCoroutineDispatcher experimentalCoroutineDispatcher, int i, @NotNull TaskMode taskMode) {
        Intrinsics.checkParameterIsNotNull(experimentalCoroutineDispatcher, "dispatcher");
        Intrinsics.checkParameterIsNotNull(taskMode, "taskMode");
        this.dispatcher = experimentalCoroutineDispatcher;
        this.parallelism = i;
        this.taskMode = taskMode;
    }

    @Override // java.util.concurrent.Executor
    public void execute(@NotNull Runnable runnable) {
        Intrinsics.checkParameterIsNotNull(runnable, "command");
        dispatch(runnable, false);
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        throw new IllegalStateException("Close cannot be invoked on LimitingBlockingDispatcher".toString());
    }

    @Override // kotlinx.coroutines.CoroutineDispatcher
    public void dispatch(@NotNull CoroutineContext coroutineContext, @NotNull Runnable runnable) {
        Intrinsics.checkParameterIsNotNull(coroutineContext, "context");
        Intrinsics.checkParameterIsNotNull(runnable, "block");
        dispatch(runnable, false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x0010  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void dispatch(java.lang.Runnable r3, boolean r4) {
        /*
            r2 = this;
        L_0x0000:
            java.util.concurrent.atomic.AtomicIntegerFieldUpdater r0 = kotlinx.coroutines.scheduling.LimitingDispatcher.inFlightTasks$FU
            int r0 = r0.incrementAndGet(r2)
            int r1 = r2.parallelism
            if (r0 > r1) goto L_0x0010
            kotlinx.coroutines.scheduling.ExperimentalCoroutineDispatcher r0 = r2.dispatcher
            r0.dispatchWithContext$kotlinx_coroutines_core(r3, r2, r4)
            return
        L_0x0010:
            java.util.concurrent.ConcurrentLinkedQueue<java.lang.Runnable> r0 = r2.queue
            r0.add(r3)
            java.util.concurrent.atomic.AtomicIntegerFieldUpdater r3 = kotlinx.coroutines.scheduling.LimitingDispatcher.inFlightTasks$FU
            int r3 = r3.decrementAndGet(r2)
            int r0 = r2.parallelism
            if (r3 < r0) goto L_0x0020
            return
        L_0x0020:
            java.util.concurrent.ConcurrentLinkedQueue<java.lang.Runnable> r3 = r2.queue
            java.lang.Object r3 = r3.poll()
            java.lang.Runnable r3 = (java.lang.Runnable) r3
            if (r3 == 0) goto L_0x002b
            goto L_0x0000
        L_0x002b:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: kotlinx.coroutines.scheduling.LimitingDispatcher.dispatch(java.lang.Runnable, boolean):void");
    }

    @Override // kotlinx.coroutines.CoroutineDispatcher, java.lang.Object
    @NotNull
    public String toString() {
        return super.toString() + "[dispatcher = " + this.dispatcher + ']';
    }

    @Override // kotlinx.coroutines.scheduling.TaskContext
    public void afterTask() {
        Runnable poll = this.queue.poll();
        if (poll != null) {
            this.dispatcher.dispatchWithContext$kotlinx_coroutines_core(poll, this, true);
            return;
        }
        inFlightTasks$FU.decrementAndGet(this);
        Runnable poll2 = this.queue.poll();
        if (poll2 != null) {
            dispatch(poll2, true);
        }
    }
}
