package kotlin.coroutines.intrinsics;

import kotlin.ResultKt;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.coroutines.jvm.internal.BaseContinuationImpl;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugProbesKt;
import kotlin.coroutines.jvm.internal.RestrictedContinuationImpl;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.TypeIntrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: IntrinsicsJvm.kt */
/* access modifiers changed from: package-private */
public class IntrinsicsKt__IntrinsicsJvmKt {
    @NotNull
    public static <R, T> Continuation<Unit> createCoroutineUnintercepted(@NotNull Function2<? super R, ? super Continuation<? super T>, ? extends Object> function2, R r, @NotNull Continuation<? super T> continuation) {
        Intrinsics.checkParameterIsNotNull(function2, "$this$createCoroutineUnintercepted");
        Intrinsics.checkParameterIsNotNull(continuation, "completion");
        DebugProbesKt.probeCoroutineCreated(continuation);
        if (function2 instanceof BaseContinuationImpl) {
            return ((BaseContinuationImpl) function2).create(r, continuation);
        }
        CoroutineContext context = continuation.getContext();
        if (context == EmptyCoroutineContext.INSTANCE) {
            if (continuation != null) {
                return new RestrictedContinuationImpl(continuation, continuation, function2, r) { // from class: kotlin.coroutines.intrinsics.IntrinsicsKt__IntrinsicsJvmKt$createCoroutineUnintercepted$$inlined$createCoroutineFromSuspendFunction$IntrinsicsKt__IntrinsicsJvmKt$3
                    final /* synthetic */ Continuation $completion;
                    final /* synthetic */ Object $receiver$inlined;
                    final /* synthetic */ Function2 $this_createCoroutineUnintercepted$inlined;
                    private int label;

                    {
                        this.$completion = r1;
                        this.$this_createCoroutineUnintercepted$inlined = r3;
                        this.$receiver$inlined = r4;
                    }

                    /* access modifiers changed from: protected */
                    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
                    @Nullable
                    public Object invokeSuspend(@NotNull Object obj) {
                        int i = this.label;
                        if (i == 0) {
                            this.label = 1;
                            ResultKt.throwOnFailure(obj);
                            Function2 function22 = this.$this_createCoroutineUnintercepted$inlined;
                            if (function22 != null) {
                                TypeIntrinsics.beforeCheckcastToFunctionOfArity(function22, 2);
                                return function22.invoke(this.$receiver$inlined, this);
                            }
                            throw new TypeCastException("null cannot be cast to non-null type (R, kotlin.coroutines.Continuation<T>) -> kotlin.Any?");
                        } else if (i == 1) {
                            this.label = 2;
                            ResultKt.throwOnFailure(obj);
                            return obj;
                        } else {
                            throw new IllegalStateException("This coroutine had already completed".toString());
                        }
                    }
                };
            }
            throw new TypeCastException("null cannot be cast to non-null type kotlin.coroutines.Continuation<kotlin.Any?>");
        } else if (continuation != null) {
            return new ContinuationImpl(continuation, context, continuation, context, function2, r) { // from class: kotlin.coroutines.intrinsics.IntrinsicsKt__IntrinsicsJvmKt$createCoroutineUnintercepted$$inlined$createCoroutineFromSuspendFunction$IntrinsicsKt__IntrinsicsJvmKt$4
                final /* synthetic */ Continuation $completion;
                final /* synthetic */ CoroutineContext $context;
                final /* synthetic */ Object $receiver$inlined;
                final /* synthetic */ Function2 $this_createCoroutineUnintercepted$inlined;
                private int label;

                {
                    this.$completion = r1;
                    this.$context = r2;
                    this.$this_createCoroutineUnintercepted$inlined = r5;
                    this.$receiver$inlined = r6;
                }

                /* access modifiers changed from: protected */
                @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
                @Nullable
                public Object invokeSuspend(@NotNull Object obj) {
                    int i = this.label;
                    if (i == 0) {
                        this.label = 1;
                        ResultKt.throwOnFailure(obj);
                        Function2 function22 = this.$this_createCoroutineUnintercepted$inlined;
                        if (function22 != null) {
                            TypeIntrinsics.beforeCheckcastToFunctionOfArity(function22, 2);
                            return function22.invoke(this.$receiver$inlined, this);
                        }
                        throw new TypeCastException("null cannot be cast to non-null type (R, kotlin.coroutines.Continuation<T>) -> kotlin.Any?");
                    } else if (i == 1) {
                        this.label = 2;
                        ResultKt.throwOnFailure(obj);
                        return obj;
                    } else {
                        throw new IllegalStateException("This coroutine had already completed".toString());
                    }
                }
            };
        } else {
            throw new TypeCastException("null cannot be cast to non-null type kotlin.coroutines.Continuation<kotlin.Any?>");
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: kotlin.coroutines.Continuation<? super T> */
    /* JADX WARN: Multi-variable type inference failed */
    @NotNull
    public static <T> Continuation<T> intercepted(@NotNull Continuation<? super T> continuation) {
        Continuation<T> continuation2;
        Intrinsics.checkParameterIsNotNull(continuation, "$this$intercepted");
        ContinuationImpl continuationImpl = !(continuation instanceof ContinuationImpl) ? null : continuation;
        return (continuationImpl == null || (continuation2 = (Continuation<T>) continuationImpl.intercepted()) == null) ? continuation : continuation2;
    }
}
