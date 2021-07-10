package kotlinx.coroutines;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import kotlin.ExceptionsKt__ExceptionsKt;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.intrinsics.IntrinsicsKt__IntrinsicsJvmKt;
import kotlin.coroutines.intrinsics.IntrinsicsKt__IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugProbesKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.internal.ConcurrentKt;
import kotlinx.coroutines.internal.LockFreeLinkedListKt;
import kotlinx.coroutines.internal.LockFreeLinkedListNode;
import kotlinx.coroutines.internal.OpDescriptor;
import kotlinx.coroutines.internal.StackTraceRecoveryKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: JobSupport.kt */
public class JobSupport implements Job, ChildJob, ParentJob {
    private static final AtomicReferenceFieldUpdater _state$FU = AtomicReferenceFieldUpdater.newUpdater(JobSupport.class, Object.class, "_state");
    private volatile Object _state;
    @Nullable
    public volatile ChildHandle parentHandle;

    /* access modifiers changed from: protected */
    public void afterCompletionInternal(@Nullable Object obj, int i) {
    }

    public boolean getHandlesException$kotlinx_coroutines_core() {
        return true;
    }

    public boolean getOnCancelComplete$kotlinx_coroutines_core() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleJobException(@NotNull Throwable th) {
        Intrinsics.checkParameterIsNotNull(th, "exception");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isScopedCoroutine() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void onCancelling(@Nullable Throwable th) {
    }

    /* access modifiers changed from: protected */
    public void onCompletionInternal(@Nullable Object obj) {
    }

    public void onStartInternal$kotlinx_coroutines_core() {
    }

    public JobSupport(boolean z) {
        this._state = z ? JobSupportKt.EMPTY_ACTIVE : JobSupportKt.EMPTY_NEW;
    }

    @Override // kotlin.coroutines.CoroutineContext
    public <R> R fold(R r, @NotNull Function2<? super R, ? super CoroutineContext.Element, ? extends R> function2) {
        Intrinsics.checkParameterIsNotNull(function2, "operation");
        return (R) Job.DefaultImpls.fold(this, r, function2);
    }

    @Override // kotlin.coroutines.CoroutineContext.Element, kotlin.coroutines.CoroutineContext
    @Nullable
    public <E extends CoroutineContext.Element> E get(@NotNull CoroutineContext.Key<E> key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        return (E) Job.DefaultImpls.get(this, key);
    }

    @Override // kotlin.coroutines.CoroutineContext
    @NotNull
    public CoroutineContext minusKey(@NotNull CoroutineContext.Key<?> key) {
        Intrinsics.checkParameterIsNotNull(key, "key");
        return Job.DefaultImpls.minusKey(this, key);
    }

    @Override // kotlin.coroutines.CoroutineContext
    @NotNull
    public CoroutineContext plus(@NotNull CoroutineContext coroutineContext) {
        Intrinsics.checkParameterIsNotNull(coroutineContext, "context");
        return Job.DefaultImpls.plus(this, coroutineContext);
    }

    @Override // kotlin.coroutines.CoroutineContext.Element
    @NotNull
    public final CoroutineContext.Key<?> getKey() {
        return Job.Key;
    }

    private final boolean addLastAtomic(Object obj, NodeList nodeList, JobNode<?> jobNode) {
        int tryCondAddNext;
        JobSupport$addLastAtomic$$inlined$addLastIf$1 jobSupport$addLastAtomic$$inlined$addLastIf$1 = new LockFreeLinkedListNode.CondAddOp(jobNode, jobNode, this, obj) { // from class: kotlinx.coroutines.JobSupport$addLastAtomic$$inlined$addLastIf$1
            final /* synthetic */ Object $expect$inlined;
            final /* synthetic */ JobSupport this$0;

            {
                this.this$0 = r3;
                this.$expect$inlined = r4;
            }

            @Nullable
            public Object prepare(@NotNull LockFreeLinkedListNode lockFreeLinkedListNode) {
                Intrinsics.checkParameterIsNotNull(lockFreeLinkedListNode, "affected");
                if (this.this$0.getState$kotlinx_coroutines_core() == this.$expect$inlined) {
                    return null;
                }
                return LockFreeLinkedListKt.getCONDITION_FALSE();
            }
        };
        do {
            Object prev = nodeList.getPrev();
            if (prev != null) {
                tryCondAddNext = ((LockFreeLinkedListNode) prev).tryCondAddNext(jobNode, nodeList, jobSupport$addLastAtomic$$inlined$addLastIf$1);
                if (tryCondAddNext == 1) {
                    return true;
                }
            } else {
                throw new TypeCastException("null cannot be cast to non-null type kotlinx.coroutines.internal.Node /* = kotlinx.coroutines.internal.LockFreeLinkedListNode */");
            }
        } while (tryCondAddNext != 2);
        return false;
    }

    public final void initParentJobInternal$kotlinx_coroutines_core(@Nullable Job job) {
        if (DebugKt.getASSERTIONS_ENABLED()) {
            if (!(this.parentHandle == null)) {
                throw new AssertionError();
            }
        }
        if (job == null) {
            this.parentHandle = NonDisposableHandle.INSTANCE;
            return;
        }
        job.start();
        ChildHandle attachChild = job.attachChild(this);
        this.parentHandle = attachChild;
        if (isCompleted()) {
            attachChild.dispose();
            this.parentHandle = NonDisposableHandle.INSTANCE;
        }
    }

    private final boolean cancelMakeCompleting(Object obj) {
        int tryMakeCompleting;
        do {
            Object state$kotlinx_coroutines_core = getState$kotlinx_coroutines_core();
            if (!(state$kotlinx_coroutines_core instanceof Incomplete) || (((state$kotlinx_coroutines_core instanceof Finishing) && ((Finishing) state$kotlinx_coroutines_core).isCompleting) || (tryMakeCompleting = tryMakeCompleting(state$kotlinx_coroutines_core, new CompletedExceptionally(createCauseException(obj), false, 2, null), 0)) == 0)) {
                return false;
            }
            if (tryMakeCompleting == 1 || tryMakeCompleting == 2) {
                return true;
            }
        } while (tryMakeCompleting == 3);
        throw new IllegalStateException("unexpected result".toString());
    }

    private final boolean joinInternal() {
        Object state$kotlinx_coroutines_core;
        do {
            state$kotlinx_coroutines_core = getState$kotlinx_coroutines_core();
            if (!(state$kotlinx_coroutines_core instanceof Incomplete)) {
                return false;
            }
        } while (startInternal(state$kotlinx_coroutines_core) < 0);
        return true;
    }

    private final boolean makeCancelling(Object obj) {
        Throwable th = null;
        Throwable th2 = null;
        while (true) {
            Object state$kotlinx_coroutines_core = getState$kotlinx_coroutines_core();
            if (state$kotlinx_coroutines_core instanceof Finishing) {
                synchronized (state$kotlinx_coroutines_core) {
                    if (((Finishing) state$kotlinx_coroutines_core).isSealed()) {
                        return false;
                    }
                    boolean isCancelling = ((Finishing) state$kotlinx_coroutines_core).isCancelling();
                    if (obj != null || !isCancelling) {
                        if (th2 == null) {
                            th2 = createCauseException(obj);
                        }
                        ((Finishing) state$kotlinx_coroutines_core).addExceptionLocked(th2);
                    }
                    Throwable th3 = ((Finishing) state$kotlinx_coroutines_core).rootCause;
                    if (!isCancelling) {
                        th = th3;
                    }
                }
                if (th != null) {
                    notifyCancelling(((Finishing) state$kotlinx_coroutines_core).getList(), th);
                }
                return true;
            } else if (!(state$kotlinx_coroutines_core instanceof Incomplete)) {
                return false;
            } else {
                if (th2 == null) {
                    th2 = createCauseException(obj);
                }
                Incomplete incomplete = (Incomplete) state$kotlinx_coroutines_core;
                if (!incomplete.isActive()) {
                    int tryMakeCompleting = tryMakeCompleting(state$kotlinx_coroutines_core, new CompletedExceptionally(th2, false, 2, null), 0);
                    if (tryMakeCompleting == 0) {
                        throw new IllegalStateException(("Cannot happen in " + state$kotlinx_coroutines_core).toString());
                    } else if (tryMakeCompleting == 1 || tryMakeCompleting == 2) {
                        break;
                    } else if (tryMakeCompleting != 3) {
                        throw new IllegalStateException("unexpected result".toString());
                    }
                } else if (tryMakeCancelling(incomplete, th2)) {
                    return true;
                }
            }
        }
        return true;
    }

    @Override // kotlinx.coroutines.Job
    @NotNull
    public final DisposableHandle invokeOnCompletion(boolean z, boolean z2, @NotNull Function1<? super Throwable, Unit> function1) {
        Throwable th;
        Intrinsics.checkParameterIsNotNull(function1, "handler");
        Throwable th2 = null;
        JobNode<?> jobNode = null;
        while (true) {
            Object state$kotlinx_coroutines_core = getState$kotlinx_coroutines_core();
            if (state$kotlinx_coroutines_core instanceof Empty) {
                Empty empty = (Empty) state$kotlinx_coroutines_core;
                if (empty.isActive()) {
                    if (jobNode == null) {
                        jobNode = makeNode(function1, z);
                    }
                    if (_state$FU.compareAndSet(this, state$kotlinx_coroutines_core, jobNode)) {
                        return jobNode;
                    }
                } else {
                    promoteEmptyToNodeList(empty);
                }
            } else if (state$kotlinx_coroutines_core instanceof Incomplete) {
                NodeList list = ((Incomplete) state$kotlinx_coroutines_core).getList();
                if (list != null) {
                    DisposableHandle disposableHandle = NonDisposableHandle.INSTANCE;
                    if (!z || !(state$kotlinx_coroutines_core instanceof Finishing)) {
                        th = null;
                    } else {
                        synchronized (state$kotlinx_coroutines_core) {
                            th = ((Finishing) state$kotlinx_coroutines_core).rootCause;
                            if (th == null || ((function1 instanceof ChildHandleNode) && !((Finishing) state$kotlinx_coroutines_core).isCompleting)) {
                                if (jobNode == null) {
                                    jobNode = makeNode(function1, z);
                                }
                                if (addLastAtomic(state$kotlinx_coroutines_core, list, jobNode)) {
                                    if (th == null) {
                                        return jobNode;
                                    }
                                    disposableHandle = jobNode;
                                }
                            }
                            Unit unit = Unit.INSTANCE;
                        }
                    }
                    if (th != null) {
                        if (z2) {
                            function1.invoke(th);
                        }
                        return disposableHandle;
                    }
                    if (jobNode == null) {
                        jobNode = makeNode(function1, z);
                    }
                    if (addLastAtomic(state$kotlinx_coroutines_core, list, jobNode)) {
                        return jobNode;
                    }
                } else if (state$kotlinx_coroutines_core != null) {
                    promoteSingleToNodeList((JobNode) state$kotlinx_coroutines_core);
                } else {
                    throw new TypeCastException("null cannot be cast to non-null type kotlinx.coroutines.JobNode<*>");
                }
            } else {
                if (z2) {
                    if (!(state$kotlinx_coroutines_core instanceof CompletedExceptionally)) {
                        state$kotlinx_coroutines_core = null;
                    }
                    CompletedExceptionally completedExceptionally = (CompletedExceptionally) state$kotlinx_coroutines_core;
                    if (completedExceptionally != null) {
                        th2 = completedExceptionally.cause;
                    }
                    function1.invoke(th2);
                }
                return NonDisposableHandle.INSTANCE;
            }
        }
    }

    public final boolean makeCompletingOnce$kotlinx_coroutines_core(@Nullable Object obj, int i) {
        int tryMakeCompleting;
        do {
            tryMakeCompleting = tryMakeCompleting(getState$kotlinx_coroutines_core(), obj, i);
            if (tryMakeCompleting == 0) {
                throw new IllegalStateException("Job " + this + " is already complete or completing, but is being completed with " + obj, getExceptionOrNull(obj));
            } else if (tryMakeCompleting == 1) {
                return true;
            } else {
                if (tryMakeCompleting == 2) {
                    return false;
                }
            }
        } while (tryMakeCompleting == 3);
        throw new IllegalStateException("unexpected result".toString());
    }

    public final void removeNode$kotlinx_coroutines_core(@NotNull JobNode<?> jobNode) {
        Object state$kotlinx_coroutines_core;
        Intrinsics.checkParameterIsNotNull(jobNode, "node");
        do {
            state$kotlinx_coroutines_core = getState$kotlinx_coroutines_core();
            if (state$kotlinx_coroutines_core instanceof JobNode) {
                if (state$kotlinx_coroutines_core != jobNode) {
                    return;
                }
            } else if ((state$kotlinx_coroutines_core instanceof Incomplete) && ((Incomplete) state$kotlinx_coroutines_core).getList() != null) {
                jobNode.remove();
                return;
            } else {
                return;
            }
        } while (!_state$FU.compareAndSet(this, state$kotlinx_coroutines_core, JobSupportKt.EMPTY_ACTIVE));
    }

    @Override // kotlinx.coroutines.Job
    public final boolean start() {
        int startInternal;
        do {
            startInternal = startInternal(getState$kotlinx_coroutines_core());
            if (startInternal == 0) {
                return false;
            }
        } while (startInternal != 1);
        return true;
    }

    @Override // kotlinx.coroutines.Job
    public boolean isActive() {
        Object state$kotlinx_coroutines_core = getState$kotlinx_coroutines_core();
        return (state$kotlinx_coroutines_core instanceof Incomplete) && ((Incomplete) state$kotlinx_coroutines_core).isActive();
    }

    public final boolean isCompleted() {
        return !(getState$kotlinx_coroutines_core() instanceof Incomplete);
    }

    /* access modifiers changed from: package-private */
    @Nullable
    public final /* synthetic */ Object joinSuspend(@NotNull Continuation<? super Unit> continuation) {
        CancellableContinuationImpl cancellableContinuationImpl = new CancellableContinuationImpl(IntrinsicsKt__IntrinsicsJvmKt.intercepted(continuation), 1);
        CancellableContinuationKt.disposeOnCancellation(cancellableContinuationImpl, invokeOnCompletion(new ResumeOnCompletion(this, cancellableContinuationImpl)));
        Object result = cancellableContinuationImpl.getResult();
        if (result == IntrinsicsKt__IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
            DebugProbesKt.probeCoroutineSuspended(continuation);
        }
        return result;
    }

    private final boolean tryFinalizeFinishingState(Finishing finishing, Object obj, int i) {
        boolean isCancelling;
        Throwable finalRootCause;
        boolean z = false;
        if (!(getState$kotlinx_coroutines_core() == finishing)) {
            throw new IllegalArgumentException("Failed requirement.".toString());
        } else if (!(!finishing.isSealed())) {
            throw new IllegalArgumentException("Failed requirement.".toString());
        } else if (finishing.isCompleting) {
            CompletedExceptionally completedExceptionally = (CompletedExceptionally) (!(obj instanceof CompletedExceptionally) ? null : obj);
            Throwable th = completedExceptionally != null ? completedExceptionally.cause : null;
            synchronized (finishing) {
                isCancelling = finishing.isCancelling();
                List<Throwable> sealLocked = finishing.sealLocked(th);
                finalRootCause = getFinalRootCause(finishing, sealLocked);
                if (finalRootCause != null) {
                    addSuppressedExceptions(finalRootCause, sealLocked);
                }
            }
            if (!(finalRootCause == null || finalRootCause == th)) {
                obj = new CompletedExceptionally(finalRootCause, false, 2, null);
            }
            if (finalRootCause != null) {
                if (cancelParent(finalRootCause) || handleJobException(finalRootCause)) {
                    z = true;
                }
                if (z) {
                    if (obj != null) {
                        ((CompletedExceptionally) obj).makeHandled();
                    } else {
                        throw new TypeCastException("null cannot be cast to non-null type kotlinx.coroutines.CompletedExceptionally");
                    }
                }
            }
            if (!isCancelling) {
                onCancelling(finalRootCause);
            }
            onCompletionInternal(obj);
            if (_state$FU.compareAndSet(this, finishing, JobSupportKt.boxIncomplete(obj))) {
                completeStateFinalization(finishing, obj, i);
                return true;
            }
            throw new IllegalArgumentException(("Unexpected state: " + this._state + ", expected: " + finishing + ", update: " + obj).toString());
        } else {
            throw new IllegalArgumentException("Failed requirement.".toString());
        }
    }

    private final Throwable getFinalRootCause(Finishing finishing, List<? extends Throwable> list) {
        Object obj = null;
        if (!list.isEmpty()) {
            Iterator<T> it = list.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Object next = it.next();
                if (!(((Throwable) next) instanceof CancellationException)) {
                    obj = next;
                    break;
                }
            }
            Throwable th = (Throwable) obj;
            return th != null ? th : (Throwable) list.get(0);
        } else if (finishing.isCancelling()) {
            return createJobCancellationException();
        } else {
            return null;
        }
    }

    private final void addSuppressedExceptions(Throwable th, List<? extends Throwable> list) {
        if (list.size() > 1) {
            Set identitySet = ConcurrentKt.identitySet(list.size());
            Throwable unwrap = StackTraceRecoveryKt.unwrap(th);
            for (Throwable th2 : list) {
                Throwable unwrap2 = StackTraceRecoveryKt.unwrap(th2);
                if (unwrap2 != th && unwrap2 != unwrap && !(unwrap2 instanceof CancellationException) && identitySet.add(unwrap2)) {
                    ExceptionsKt__ExceptionsKt.addSuppressed(th, unwrap2);
                }
            }
        }
    }

    private final boolean tryFinalizeSimpleState(Incomplete incomplete, Object obj, int i) {
        if (DebugKt.getASSERTIONS_ENABLED()) {
            if (!((incomplete instanceof Empty) || (incomplete instanceof JobNode))) {
                throw new AssertionError();
            }
        }
        if (DebugKt.getASSERTIONS_ENABLED() && !(!(obj instanceof CompletedExceptionally))) {
            throw new AssertionError();
        } else if (!_state$FU.compareAndSet(this, incomplete, JobSupportKt.boxIncomplete(obj))) {
            return false;
        } else {
            onCancelling(null);
            onCompletionInternal(obj);
            completeStateFinalization(incomplete, obj, i);
            return true;
        }
    }

    private final void completeStateFinalization(Incomplete incomplete, Object obj, int i) {
        ChildHandle childHandle = this.parentHandle;
        if (childHandle != null) {
            childHandle.dispose();
            this.parentHandle = NonDisposableHandle.INSTANCE;
        }
        Throwable th = null;
        CompletedExceptionally completedExceptionally = (CompletedExceptionally) (!(obj instanceof CompletedExceptionally) ? null : obj);
        if (completedExceptionally != null) {
            th = completedExceptionally.cause;
        }
        if (incomplete instanceof JobNode) {
            try {
                ((JobNode) incomplete).invoke(th);
            } catch (Throwable th2) {
                handleOnCompletionException$kotlinx_coroutines_core(new CompletionHandlerException("Exception in completion handler " + incomplete + " for " + this, th2));
            }
        } else {
            NodeList list = incomplete.getList();
            if (list != null) {
                notifyCompletion(list, th);
            }
        }
        afterCompletionInternal(obj, i);
    }

    private final void notifyCancelling(NodeList nodeList, Throwable th) {
        onCancelling(th);
        Object next = nodeList.getNext();
        if (next != null) {
            CompletionHandlerException completionHandlerException = null;
            for (LockFreeLinkedListNode lockFreeLinkedListNode = (LockFreeLinkedListNode) next; !Intrinsics.areEqual(lockFreeLinkedListNode, nodeList); lockFreeLinkedListNode = lockFreeLinkedListNode.getNextNode()) {
                if (lockFreeLinkedListNode instanceof JobCancellingNode) {
                    JobNode jobNode = (JobNode) lockFreeLinkedListNode;
                    try {
                        jobNode.invoke(th);
                    } catch (Throwable th2) {
                        if (completionHandlerException != null) {
                            ExceptionsKt__ExceptionsKt.addSuppressed(completionHandlerException, th2);
                            if (completionHandlerException != null) {
                            }
                        }
                        completionHandlerException = new CompletionHandlerException("Exception in completion handler " + jobNode + " for " + this, th2);
                        Unit unit = Unit.INSTANCE;
                    }
                }
            }
            if (completionHandlerException != null) {
                handleOnCompletionException$kotlinx_coroutines_core(completionHandlerException);
            }
            cancelParent(th);
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlinx.coroutines.internal.Node /* = kotlinx.coroutines.internal.LockFreeLinkedListNode */");
    }

    private final boolean cancelParent(Throwable th) {
        if (isScopedCoroutine()) {
            return true;
        }
        boolean z = th instanceof CancellationException;
        ChildHandle childHandle = this.parentHandle;
        if (childHandle == null || childHandle == NonDisposableHandle.INSTANCE) {
            return z;
        }
        if (childHandle.childCancelled(th) || z) {
            return true;
        }
        return false;
    }

    private final int startInternal(Object obj) {
        if (obj instanceof Empty) {
            if (((Empty) obj).isActive()) {
                return 0;
            }
            if (!_state$FU.compareAndSet(this, obj, JobSupportKt.EMPTY_ACTIVE)) {
                return -1;
            }
            onStartInternal$kotlinx_coroutines_core();
            return 1;
        } else if (!(obj instanceof InactiveNodeList)) {
            return 0;
        } else {
            if (!_state$FU.compareAndSet(this, obj, ((InactiveNodeList) obj).getList())) {
                return -1;
            }
            onStartInternal$kotlinx_coroutines_core();
            return 1;
        }
    }

    @Override // kotlinx.coroutines.Job
    @NotNull
    public final CancellationException getCancellationException() {
        Object state$kotlinx_coroutines_core = getState$kotlinx_coroutines_core();
        if (state$kotlinx_coroutines_core instanceof Finishing) {
            Throwable th = ((Finishing) state$kotlinx_coroutines_core).rootCause;
            if (th != null) {
                CancellationException cancellationException = toCancellationException(th, DebugStringsKt.getClassSimpleName(this) + " is cancelling");
                if (cancellationException != null) {
                    return cancellationException;
                }
            }
            throw new IllegalStateException(("Job is still new or active: " + this).toString());
        } else if (state$kotlinx_coroutines_core instanceof Incomplete) {
            throw new IllegalStateException(("Job is still new or active: " + this).toString());
        } else if (state$kotlinx_coroutines_core instanceof CompletedExceptionally) {
            return toCancellationException$default(this, ((CompletedExceptionally) state$kotlinx_coroutines_core).cause, null, 1, null);
        } else {
            return new JobCancellationException(DebugStringsKt.getClassSimpleName(this) + " has completed normally", null, this);
        }
    }

    public static /* synthetic */ CancellationException toCancellationException$default(JobSupport jobSupport, Throwable th, String str, int i, Object obj) {
        if (obj == null) {
            if ((i & 1) != 0) {
                str = null;
            }
            return jobSupport.toCancellationException(th, str);
        }
        throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: toCancellationException");
    }

    /* access modifiers changed from: protected */
    @NotNull
    public final CancellationException toCancellationException(@NotNull Throwable th, @Nullable String str) {
        Intrinsics.checkParameterIsNotNull(th, "$this$toCancellationException");
        CancellationException cancellationException = (CancellationException) (!(th instanceof CancellationException) ? null : th);
        if (cancellationException == null) {
            if (str == null) {
                str = DebugStringsKt.getClassSimpleName(th) + " was cancelled";
            }
            cancellationException = new JobCancellationException(str, th, this);
        }
        return cancellationException;
    }

    @NotNull
    public final DisposableHandle invokeOnCompletion(@NotNull Function1<? super Throwable, Unit> function1) {
        Intrinsics.checkParameterIsNotNull(function1, "handler");
        return invokeOnCompletion(false, true, function1);
    }

    private final JobNode<?> makeNode(Function1<? super Throwable, Unit> function1, boolean z) {
        boolean z2 = true;
        JobCancellingNode jobCancellingNode = null;
        if (z) {
            if (function1 instanceof JobCancellingNode) {
                jobCancellingNode = function1;
            }
            JobCancellingNode jobCancellingNode2 = jobCancellingNode;
            if (jobCancellingNode2 != null) {
                if (jobCancellingNode2.job != this) {
                    z2 = false;
                }
                if (!z2) {
                    throw new IllegalArgumentException("Failed requirement.".toString());
                } else if (jobCancellingNode2 != null) {
                    return jobCancellingNode2;
                }
            }
            return new InvokeOnCancelling(this, function1);
        }
        if (function1 instanceof JobNode) {
            jobCancellingNode = function1;
        }
        JobNode<?> jobNode = jobCancellingNode;
        if (jobNode != null) {
            if (jobNode.job != this || (jobNode instanceof JobCancellingNode)) {
                z2 = false;
            }
            if (!z2) {
                throw new IllegalArgumentException("Failed requirement.".toString());
            } else if (jobNode != null) {
                return jobNode;
            }
        }
        return new InvokeOnCompletion(this, function1);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v2, types: [kotlinx.coroutines.InactiveNodeList] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void promoteEmptyToNodeList(kotlinx.coroutines.Empty r3) {
        /*
            r2 = this;
            kotlinx.coroutines.NodeList r0 = new kotlinx.coroutines.NodeList
            r0.<init>()
            boolean r1 = r3.isActive()
            if (r1 == 0) goto L_0x000c
            goto L_0x0012
        L_0x000c:
            kotlinx.coroutines.InactiveNodeList r1 = new kotlinx.coroutines.InactiveNodeList
            r1.<init>(r0)
            r0 = r1
        L_0x0012:
            java.util.concurrent.atomic.AtomicReferenceFieldUpdater r1 = kotlinx.coroutines.JobSupport._state$FU
            r1.compareAndSet(r2, r3, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: kotlinx.coroutines.JobSupport.promoteEmptyToNodeList(kotlinx.coroutines.Empty):void");
    }

    private final void promoteSingleToNodeList(JobNode<?> jobNode) {
        jobNode.addOneIfEmpty(new NodeList());
        _state$FU.compareAndSet(this, jobNode, jobNode.getNextNode());
    }

    @Override // kotlinx.coroutines.Job
    @Nullable
    public final Object join(@NotNull Continuation<? super Unit> continuation) {
        if (joinInternal()) {
            return joinSuspend(continuation);
        }
        YieldKt.checkCompletion(continuation.getContext());
        return Unit.INSTANCE;
    }

    @Override // kotlinx.coroutines.Job
    public void cancel(@Nullable CancellationException cancellationException) {
        cancelInternal(cancellationException);
    }

    public boolean cancelInternal(@Nullable Throwable th) {
        return cancelImpl$kotlinx_coroutines_core(th) && getHandlesException$kotlinx_coroutines_core();
    }

    @Override // kotlinx.coroutines.ChildJob
    public final void parentCancelled(@NotNull ParentJob parentJob) {
        Intrinsics.checkParameterIsNotNull(parentJob, "parentJob");
        cancelImpl$kotlinx_coroutines_core(parentJob);
    }

    public boolean childCancelled(@NotNull Throwable th) {
        Intrinsics.checkParameterIsNotNull(th, "cause");
        if (th instanceof CancellationException) {
            return true;
        }
        if (!cancelImpl$kotlinx_coroutines_core(th) || !getHandlesException$kotlinx_coroutines_core()) {
            return false;
        }
        return true;
    }

    public final boolean cancelImpl$kotlinx_coroutines_core(@Nullable Object obj) {
        if (!getOnCancelComplete$kotlinx_coroutines_core() || !cancelMakeCompleting(obj)) {
            return makeCancelling(obj);
        }
        return true;
    }

    private final void notifyCompletion(@NotNull NodeList nodeList, Throwable th) {
        Object next = nodeList.getNext();
        if (next != null) {
            CompletionHandlerException completionHandlerException = null;
            for (LockFreeLinkedListNode lockFreeLinkedListNode = (LockFreeLinkedListNode) next; !Intrinsics.areEqual(lockFreeLinkedListNode, nodeList); lockFreeLinkedListNode = lockFreeLinkedListNode.getNextNode()) {
                if (lockFreeLinkedListNode instanceof JobNode) {
                    JobNode jobNode = (JobNode) lockFreeLinkedListNode;
                    try {
                        jobNode.invoke(th);
                    } catch (Throwable th2) {
                        if (completionHandlerException != null) {
                            ExceptionsKt__ExceptionsKt.addSuppressed(completionHandlerException, th2);
                            if (completionHandlerException != null) {
                            }
                        }
                        completionHandlerException = new CompletionHandlerException("Exception in completion handler " + jobNode + " for " + this, th2);
                        Unit unit = Unit.INSTANCE;
                    }
                }
            }
            if (completionHandlerException != null) {
                handleOnCompletionException$kotlinx_coroutines_core(completionHandlerException);
                return;
            }
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlinx.coroutines.internal.Node /* = kotlinx.coroutines.internal.LockFreeLinkedListNode */");
    }

    private final JobCancellationException createJobCancellationException() {
        return new JobCancellationException("Job was cancelled", null, this);
    }

    @Override // kotlinx.coroutines.ParentJob
    @NotNull
    public CancellationException getChildJobCancellationCause() {
        Throwable th;
        Object state$kotlinx_coroutines_core = getState$kotlinx_coroutines_core();
        CancellationException cancellationException = null;
        if (state$kotlinx_coroutines_core instanceof Finishing) {
            th = ((Finishing) state$kotlinx_coroutines_core).rootCause;
        } else if (state$kotlinx_coroutines_core instanceof CompletedExceptionally) {
            th = ((CompletedExceptionally) state$kotlinx_coroutines_core).cause;
        } else if (!(state$kotlinx_coroutines_core instanceof Incomplete)) {
            th = null;
        } else {
            throw new IllegalStateException(("Cannot be cancelling child in this state: " + state$kotlinx_coroutines_core).toString());
        }
        if (th instanceof CancellationException) {
            cancellationException = th;
        }
        CancellationException cancellationException2 = cancellationException;
        if (cancellationException2 != null) {
            return cancellationException2;
        }
        return new JobCancellationException("Parent job is " + stateString(state$kotlinx_coroutines_core), th, this);
    }

    private final Throwable createCauseException(Object obj) {
        if (obj != null ? obj instanceof Throwable : true) {
            return obj != null ? (Throwable) obj : createJobCancellationException();
        }
        if (obj != null) {
            return ((ParentJob) obj).getChildJobCancellationCause();
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlinx.coroutines.ParentJob");
    }

    private final NodeList getOrPromoteCancellingList(Incomplete incomplete) {
        NodeList list = incomplete.getList();
        if (list != null) {
            return list;
        }
        if (incomplete instanceof Empty) {
            return new NodeList();
        }
        if (incomplete instanceof JobNode) {
            promoteSingleToNodeList((JobNode) incomplete);
            return null;
        }
        throw new IllegalStateException(("State should have list: " + incomplete).toString());
    }

    private final boolean tryMakeCancelling(Incomplete incomplete, Throwable th) {
        if (DebugKt.getASSERTIONS_ENABLED() && !(!(incomplete instanceof Finishing))) {
            throw new AssertionError();
        } else if (!DebugKt.getASSERTIONS_ENABLED() || incomplete.isActive()) {
            NodeList orPromoteCancellingList = getOrPromoteCancellingList(incomplete);
            if (orPromoteCancellingList == null) {
                return false;
            }
            if (!_state$FU.compareAndSet(this, incomplete, new Finishing(orPromoteCancellingList, false, th))) {
                return false;
            }
            notifyCancelling(orPromoteCancellingList, th);
            return true;
        } else {
            throw new AssertionError();
        }
    }

    private final int tryMakeCompleting(Object obj, Object obj2, int i) {
        if (!(obj instanceof Incomplete)) {
            return 0;
        }
        if (((obj instanceof Empty) || (obj instanceof JobNode)) && !(obj instanceof ChildHandleNode) && !(obj2 instanceof CompletedExceptionally)) {
            return !tryFinalizeSimpleState((Incomplete) obj, obj2, i) ? 3 : 1;
        }
        return tryMakeCompletingSlowPath((Incomplete) obj, obj2, i);
    }

    private final int tryMakeCompletingSlowPath(Incomplete incomplete, Object obj, int i) {
        NodeList orPromoteCancellingList = getOrPromoteCancellingList(incomplete);
        if (orPromoteCancellingList == null) {
            return 3;
        }
        Throwable th = null;
        Finishing finishing = (Finishing) (!(incomplete instanceof Finishing) ? null : incomplete);
        if (finishing == null) {
            finishing = new Finishing(orPromoteCancellingList, false, null);
        }
        synchronized (finishing) {
            if (finishing.isCompleting) {
                return 0;
            }
            finishing.isCompleting = true;
            if (finishing != incomplete && !_state$FU.compareAndSet(this, incomplete, finishing)) {
                return 3;
            }
            if (!finishing.isSealed()) {
                boolean isCancelling = finishing.isCancelling();
                CompletedExceptionally completedExceptionally = (CompletedExceptionally) (!(obj instanceof CompletedExceptionally) ? null : obj);
                if (completedExceptionally != null) {
                    finishing.addExceptionLocked(completedExceptionally.cause);
                }
                Throwable th2 = finishing.rootCause;
                if (!isCancelling) {
                    th = th2;
                }
                Unit unit = Unit.INSTANCE;
            } else {
                throw new IllegalArgumentException("Failed requirement.".toString());
            }
        }
        if (th != null) {
            notifyCancelling(orPromoteCancellingList, th);
        }
        ChildHandleNode firstChild = firstChild(incomplete);
        if (firstChild != null && tryWaitForChild(finishing, firstChild, obj)) {
            return 2;
        }
        if (tryFinalizeFinishingState(finishing, obj, i)) {
            return 1;
        }
        return 3;
    }

    private final Throwable getExceptionOrNull(@Nullable Object obj) {
        if (!(obj instanceof CompletedExceptionally)) {
            obj = null;
        }
        CompletedExceptionally completedExceptionally = (CompletedExceptionally) obj;
        if (completedExceptionally != null) {
            return completedExceptionally.cause;
        }
        return null;
    }

    private final ChildHandleNode firstChild(Incomplete incomplete) {
        ChildHandleNode childHandleNode = (ChildHandleNode) (!(incomplete instanceof ChildHandleNode) ? null : incomplete);
        if (childHandleNode != null) {
            return childHandleNode;
        }
        NodeList list = incomplete.getList();
        if (list != null) {
            return nextChild(list);
        }
        return null;
    }

    private final boolean tryWaitForChild(Finishing finishing, ChildHandleNode childHandleNode, Object obj) {
        while (Job.DefaultImpls.invokeOnCompletion$default(childHandleNode.childJob, false, false, new ChildCompletion(this, finishing, childHandleNode, obj), 1, null) == NonDisposableHandle.INSTANCE) {
            childHandleNode = nextChild(childHandleNode);
            if (childHandleNode == null) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void continueCompleting(Finishing finishing, ChildHandleNode childHandleNode, Object obj) {
        if (getState$kotlinx_coroutines_core() == finishing) {
            ChildHandleNode nextChild = nextChild(childHandleNode);
            if ((nextChild == null || !tryWaitForChild(finishing, nextChild, obj)) && tryFinalizeFinishingState(finishing, obj, 0)) {
            }
            return;
        }
        throw new IllegalArgumentException("Failed requirement.".toString());
    }

    private final ChildHandleNode nextChild(@NotNull LockFreeLinkedListNode lockFreeLinkedListNode) {
        while (lockFreeLinkedListNode.isRemoved()) {
            lockFreeLinkedListNode = lockFreeLinkedListNode.getPrevNode();
        }
        while (true) {
            lockFreeLinkedListNode = lockFreeLinkedListNode.getNextNode();
            if (!lockFreeLinkedListNode.isRemoved()) {
                if (lockFreeLinkedListNode instanceof ChildHandleNode) {
                    return (ChildHandleNode) lockFreeLinkedListNode;
                }
                if (lockFreeLinkedListNode instanceof NodeList) {
                    return null;
                }
            }
        }
    }

    @Override // kotlinx.coroutines.Job
    @NotNull
    public final ChildHandle attachChild(@NotNull ChildJob childJob) {
        Intrinsics.checkParameterIsNotNull(childJob, "child");
        DisposableHandle invokeOnCompletion$default = Job.DefaultImpls.invokeOnCompletion$default(this, true, false, new ChildHandleNode(this, childJob), 2, null);
        if (invokeOnCompletion$default != null) {
            return (ChildHandle) invokeOnCompletion$default;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlinx.coroutines.ChildHandle");
    }

    public void handleOnCompletionException$kotlinx_coroutines_core(@NotNull Throwable th) {
        Intrinsics.checkParameterIsNotNull(th, "exception");
        throw th;
    }

    @NotNull
    public String toString() {
        return toDebugString() + '@' + DebugStringsKt.getHexAddress(this);
    }

    @NotNull
    public final String toDebugString() {
        return nameString$kotlinx_coroutines_core() + '{' + stateString(getState$kotlinx_coroutines_core()) + '}';
    }

    @NotNull
    public String nameString$kotlinx_coroutines_core() {
        return DebugStringsKt.getClassSimpleName(this);
    }

    private final String stateString(Object obj) {
        if (obj instanceof Finishing) {
            Finishing finishing = (Finishing) obj;
            if (finishing.isCancelling()) {
                return "Cancelling";
            }
            if (finishing.isCompleting) {
                return "Completing";
            }
            return "Active";
        } else if (!(obj instanceof Incomplete)) {
            return obj instanceof CompletedExceptionally ? "Cancelled" : "Completed";
        } else {
            if (((Incomplete) obj).isActive()) {
                return "Active";
            }
            return "New";
        }
    }

    /* compiled from: JobSupport.kt */
    /* access modifiers changed from: private */
    public static final class Finishing implements Incomplete {
        private volatile Object _exceptionsHolder;
        public volatile boolean isCompleting;
        @NotNull
        private final NodeList list;
        @Nullable
        public volatile Throwable rootCause;

        @Override // kotlinx.coroutines.Incomplete
        @NotNull
        public NodeList getList() {
            return this.list;
        }

        public Finishing(@NotNull NodeList nodeList, boolean z, @Nullable Throwable th) {
            Intrinsics.checkParameterIsNotNull(nodeList, "list");
            this.list = nodeList;
            this.isCompleting = z;
            this.rootCause = th;
        }

        public final boolean isSealed() {
            return this._exceptionsHolder == JobSupportKt.SEALED;
        }

        public final boolean isCancelling() {
            return this.rootCause != null;
        }

        @Override // kotlinx.coroutines.Incomplete
        public boolean isActive() {
            return this.rootCause == null;
        }

        /* JADX DEBUG: Multi-variable search result rejected for r3v1, resolved type: java.lang.StringBuilder */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r0v0, types: [java.lang.Object] */
        /* JADX WARNING: Unknown variable types count: 1 */
        @org.jetbrains.annotations.NotNull
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public final java.util.List<java.lang.Throwable> sealLocked(@org.jetbrains.annotations.Nullable java.lang.Throwable r4) {
            /*
                r3 = this;
                java.lang.Object r0 = r3._exceptionsHolder
                if (r0 != 0) goto L_0x0009
                java.util.ArrayList r0 = r3.allocateList()
                goto L_0x001c
            L_0x0009:
                boolean r1 = r0 instanceof java.lang.Throwable
                if (r1 == 0) goto L_0x0016
                java.util.ArrayList r1 = r3.allocateList()
                r1.add(r0)
                r0 = r1
                goto L_0x001c
            L_0x0016:
                boolean r1 = r0 instanceof java.util.ArrayList
                if (r1 == 0) goto L_0x0038
                java.util.ArrayList r0 = (java.util.ArrayList) r0
            L_0x001c:
                java.lang.Throwable r1 = r3.rootCause
                if (r1 == 0) goto L_0x0024
                r2 = 0
                r0.add(r2, r1)
            L_0x0024:
                if (r4 == 0) goto L_0x0031
                boolean r1 = kotlin.jvm.internal.Intrinsics.areEqual(r4, r1)
                r1 = r1 ^ 1
                if (r1 == 0) goto L_0x0031
                r0.add(r4)
            L_0x0031:
                kotlinx.coroutines.internal.Symbol r4 = kotlinx.coroutines.JobSupportKt.access$getSEALED$p()
                r3._exceptionsHolder = r4
                return r0
            L_0x0038:
                java.lang.StringBuilder r3 = new java.lang.StringBuilder
                r3.<init>()
                java.lang.String r4 = "State is "
                r3.append(r4)
                r3.append(r0)
                java.lang.String r3 = r3.toString()
                java.lang.IllegalStateException r4 = new java.lang.IllegalStateException
                java.lang.String r3 = r3.toString()
                r4.<init>(r3)
                throw r4
            */
            throw new UnsupportedOperationException("Method not decompiled: kotlinx.coroutines.JobSupport.Finishing.sealLocked(java.lang.Throwable):java.util.List");
        }

        /* JADX DEBUG: Multi-variable search result rejected for r2v2, resolved type: java.lang.StringBuilder */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r0v2, types: [java.lang.Object] */
        /* JADX WARNING: Unknown variable types count: 1 */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public final void addExceptionLocked(@org.jetbrains.annotations.NotNull java.lang.Throwable r3) {
            /*
                r2 = this;
                java.lang.String r0 = "exception"
                kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r3, r0)
                java.lang.Throwable r0 = r2.rootCause
                if (r0 != 0) goto L_0x000c
                r2.rootCause = r3
                return
            L_0x000c:
                if (r3 != r0) goto L_0x000f
                return
            L_0x000f:
                java.lang.Object r0 = r2._exceptionsHolder
                if (r0 != 0) goto L_0x0016
                r2._exceptionsHolder = r3
                goto L_0x0033
            L_0x0016:
                boolean r1 = r0 instanceof java.lang.Throwable
                if (r1 == 0) goto L_0x002a
                if (r3 != r0) goto L_0x001d
                return
            L_0x001d:
                java.util.ArrayList r1 = r2.allocateList()
                r1.add(r0)
                r1.add(r3)
                r2._exceptionsHolder = r1
                goto L_0x0033
            L_0x002a:
                boolean r2 = r0 instanceof java.util.ArrayList
                if (r2 == 0) goto L_0x0034
                java.util.ArrayList r0 = (java.util.ArrayList) r0
                r0.add(r3)
            L_0x0033:
                return
            L_0x0034:
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                java.lang.String r3 = "State is "
                r2.append(r3)
                r2.append(r0)
                java.lang.String r2 = r2.toString()
                java.lang.IllegalStateException r3 = new java.lang.IllegalStateException
                java.lang.String r2 = r2.toString()
                r3.<init>(r2)
                throw r3
            */
            throw new UnsupportedOperationException("Method not decompiled: kotlinx.coroutines.JobSupport.Finishing.addExceptionLocked(java.lang.Throwable):void");
        }

        private final ArrayList<Throwable> allocateList() {
            return new ArrayList<>(4);
        }

        @NotNull
        public String toString() {
            return "Finishing[cancelling=" + isCancelling() + ", completing=" + this.isCompleting + ", rootCause=" + this.rootCause + ", exceptions=" + this._exceptionsHolder + ", list=" + getList() + ']';
        }
    }

    /* compiled from: JobSupport.kt */
    /* access modifiers changed from: private */
    public static final class ChildCompletion extends JobNode<Job> {
        private final ChildHandleNode child;
        private final JobSupport parent;
        private final Object proposedUpdate;
        private final Finishing state;

        /* Return type fixed from 'java.lang.Object' to match base method */
        /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
        @Override // kotlin.jvm.functions.Function1
        public /* bridge */ /* synthetic */ Unit invoke(Throwable th) {
            invoke(th);
            return Unit.INSTANCE;
        }

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public ChildCompletion(@NotNull JobSupport jobSupport, @NotNull Finishing finishing, @NotNull ChildHandleNode childHandleNode, @Nullable Object obj) {
            super(childHandleNode.childJob);
            Intrinsics.checkParameterIsNotNull(jobSupport, "parent");
            Intrinsics.checkParameterIsNotNull(finishing, "state");
            Intrinsics.checkParameterIsNotNull(childHandleNode, "child");
            this.parent = jobSupport;
            this.state = finishing;
            this.child = childHandleNode;
            this.proposedUpdate = obj;
        }

        @Override // kotlinx.coroutines.CompletionHandlerBase
        public void invoke(@Nullable Throwable th) {
            this.parent.continueCompleting(this.state, this.child, this.proposedUpdate);
        }

        @Override // kotlinx.coroutines.internal.LockFreeLinkedListNode
        @NotNull
        public String toString() {
            return "ChildCompletion[" + this.child + ", " + this.proposedUpdate + ']';
        }
    }

    @Nullable
    public final Object getState$kotlinx_coroutines_core() {
        while (true) {
            Object obj = this._state;
            if (!(obj instanceof OpDescriptor)) {
                return obj;
            }
            ((OpDescriptor) obj).perform(this);
        }
    }
}
