package kotlin.sequences;

import java.util.Iterator;
import kotlin.collections.ArraysKt___ArraysKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: Sequences.kt */
/* access modifiers changed from: package-private */
public class SequencesKt__SequencesKt extends SequencesKt__SequencesJVMKt {
    @NotNull
    public static <T> Sequence<T> asSequence(@NotNull Iterator<? extends T> it) {
        Intrinsics.checkParameterIsNotNull(it, "$this$asSequence");
        return constrainOnce(new Sequence<T>(it) { // from class: kotlin.sequences.SequencesKt__SequencesKt$asSequence$$inlined$Sequence$1
            final /* synthetic */ Iterator $this_asSequence$inlined;

            {
                this.$this_asSequence$inlined = r1;
            }

            @Override // kotlin.sequences.Sequence
            @NotNull
            public Iterator<T> iterator() {
                return this.$this_asSequence$inlined;
            }
        });
    }

    @NotNull
    public static <T> Sequence<T> sequenceOf(@NotNull T... tArr) {
        Intrinsics.checkParameterIsNotNull(tArr, "elements");
        return tArr.length == 0 ? emptySequence() : ArraysKt___ArraysKt.asSequence(tArr);
    }

    @NotNull
    public static <T> Sequence<T> emptySequence() {
        return EmptySequence.INSTANCE;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: kotlin.sequences.Sequence<? extends T> */
    /* JADX WARN: Multi-variable type inference failed */
    @NotNull
    public static final <T> Sequence<T> constrainOnce(@NotNull Sequence<? extends T> sequence) {
        Intrinsics.checkParameterIsNotNull(sequence, "$this$constrainOnce");
        return sequence instanceof ConstrainedOnceSequence ? sequence : new ConstrainedOnceSequence(sequence);
    }
}
