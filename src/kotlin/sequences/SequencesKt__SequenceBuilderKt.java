package kotlin.sequences;

import java.util.Iterator;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt__IntrinsicsJvmKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: SequenceBuilder.kt */
/* access modifiers changed from: package-private */
public class SequencesKt__SequenceBuilderKt {
    @NotNull
    public static <T> Sequence<T> sequence(@NotNull Function2<? super SequenceScope<? super T>, ? super Continuation<? super Unit>, ? extends Object> function2) {
        Intrinsics.checkParameterIsNotNull(function2, "block");
        return new Sequence<T>(function2) { // from class: kotlin.sequences.SequencesKt__SequenceBuilderKt$sequence$$inlined$Sequence$1
            final /* synthetic */ Function2 $block$inlined;

            {
                this.$block$inlined = r1;
            }

            @Override // kotlin.sequences.Sequence
            @NotNull
            public Iterator<T> iterator() {
                return SequencesKt__SequenceBuilderKt.iterator(this.$block$inlined);
            }
        };
    }

    @NotNull
    public static final <T> Iterator<T> iterator(@NotNull Function2<? super SequenceScope<? super T>, ? super Continuation<? super Unit>, ? extends Object> function2) {
        Intrinsics.checkParameterIsNotNull(function2, "block");
        SequenceBuilderIterator sequenceBuilderIterator = new SequenceBuilderIterator();
        sequenceBuilderIterator.setNextStep(IntrinsicsKt__IntrinsicsJvmKt.createCoroutineUnintercepted(function2, sequenceBuilderIterator, sequenceBuilderIterator));
        return sequenceBuilderIterator;
    }
}
