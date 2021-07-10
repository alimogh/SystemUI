package kotlin.sequences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import kotlin.TypeCastException;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt__MutableCollectionsJVMKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt__StringBuilderKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: _Sequences.kt */
public class SequencesKt___SequencesKt extends SequencesKt___SequencesJvmKt {
    @NotNull
    public static <T> Sequence<T> filter(@NotNull Sequence<? extends T> sequence, @NotNull Function1<? super T, Boolean> function1) {
        Intrinsics.checkParameterIsNotNull(sequence, "$this$filter");
        Intrinsics.checkParameterIsNotNull(function1, "predicate");
        return new FilteringSequence(sequence, true, function1);
    }

    @NotNull
    public static <T> Sequence<T> filterNot(@NotNull Sequence<? extends T> sequence, @NotNull Function1<? super T, Boolean> function1) {
        Intrinsics.checkParameterIsNotNull(sequence, "$this$filterNot");
        Intrinsics.checkParameterIsNotNull(function1, "predicate");
        return new FilteringSequence(sequence, false, function1);
    }

    @NotNull
    public static <T> Sequence<T> filterNotNull(@NotNull Sequence<? extends T> sequence) {
        Intrinsics.checkParameterIsNotNull(sequence, "$this$filterNotNull");
        Sequence<T> sequence2 = filterNot(sequence, SequencesKt___SequencesKt$filterNotNull$1.INSTANCE);
        if (sequence2 != null) {
            return sequence2;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.sequences.Sequence<T>");
    }

    @NotNull
    public static <T> Sequence<T> take(@NotNull Sequence<? extends T> sequence, int i) {
        Intrinsics.checkParameterIsNotNull(sequence, "$this$take");
        if (!(i >= 0)) {
            throw new IllegalArgumentException(("Requested element count " + i + " is less than zero.").toString());
        } else if (i == 0) {
            return SequencesKt__SequencesKt.emptySequence();
        } else {
            if (sequence instanceof DropTakeSequence) {
                return ((DropTakeSequence) sequence).take(i);
            }
            return new TakeSequence(sequence, i);
        }
    }

    @NotNull
    public static <T> Sequence<T> sortedWith(@NotNull Sequence<? extends T> sequence, @NotNull Comparator<? super T> comparator) {
        Intrinsics.checkParameterIsNotNull(sequence, "$this$sortedWith");
        Intrinsics.checkParameterIsNotNull(comparator, "comparator");
        return new Sequence<T>(sequence, comparator) { // from class: kotlin.sequences.SequencesKt___SequencesKt$sortedWith$1
            final /* synthetic */ Comparator $comparator;
            final /* synthetic */ Sequence $this_sortedWith;

            {
                this.$this_sortedWith = r1;
                this.$comparator = r2;
            }

            @Override // kotlin.sequences.Sequence
            @NotNull
            public Iterator<T> iterator() {
                List mutableList = SequencesKt___SequencesKt.toMutableList(this.$this_sortedWith);
                CollectionsKt__MutableCollectionsJVMKt.sortWith(mutableList, this.$comparator);
                return mutableList.iterator();
            }
        };
    }

    @NotNull
    public static final <T, C extends Collection<? super T>> C toCollection(@NotNull Sequence<? extends T> sequence, @NotNull C c) {
        Intrinsics.checkParameterIsNotNull(sequence, "$this$toCollection");
        Intrinsics.checkParameterIsNotNull(c, "destination");
        Iterator<? extends T> it = sequence.iterator();
        while (it.hasNext()) {
            c.add(it.next());
        }
        return c;
    }

    @NotNull
    public static <T> List<T> toList(@NotNull Sequence<? extends T> sequence) {
        Intrinsics.checkParameterIsNotNull(sequence, "$this$toList");
        return CollectionsKt__CollectionsKt.optimizeReadOnlyList(toMutableList(sequence));
    }

    @NotNull
    public static final <T> List<T> toMutableList(@NotNull Sequence<? extends T> sequence) {
        Intrinsics.checkParameterIsNotNull(sequence, "$this$toMutableList");
        ArrayList arrayList = new ArrayList();
        toCollection(sequence, arrayList);
        return arrayList;
    }

    @NotNull
    public static <T, R> Sequence<R> flatMap(@NotNull Sequence<? extends T> sequence, @NotNull Function1<? super T, ? extends Sequence<? extends R>> function1) {
        Intrinsics.checkParameterIsNotNull(sequence, "$this$flatMap");
        Intrinsics.checkParameterIsNotNull(function1, "transform");
        return new FlatteningSequence(sequence, function1, SequencesKt___SequencesKt$flatMap$1.INSTANCE);
    }

    @NotNull
    public static <T, R> Sequence<R> map(@NotNull Sequence<? extends T> sequence, @NotNull Function1<? super T, ? extends R> function1) {
        Intrinsics.checkParameterIsNotNull(sequence, "$this$map");
        Intrinsics.checkParameterIsNotNull(function1, "transform");
        return new TransformingSequence(sequence, function1);
    }

    @NotNull
    public static <T, R> Sequence<R> mapNotNull(@NotNull Sequence<? extends T> sequence, @NotNull Function1<? super T, ? extends R> function1) {
        Intrinsics.checkParameterIsNotNull(sequence, "$this$mapNotNull");
        Intrinsics.checkParameterIsNotNull(function1, "transform");
        return filterNotNull(new TransformingSequence(sequence, function1));
    }

    @NotNull
    public static <T> Sequence<T> distinct(@NotNull Sequence<? extends T> sequence) {
        Intrinsics.checkParameterIsNotNull(sequence, "$this$distinct");
        return distinctBy(sequence, SequencesKt___SequencesKt$distinct$1.INSTANCE);
    }

    @NotNull
    public static final <T, K> Sequence<T> distinctBy(@NotNull Sequence<? extends T> sequence, @NotNull Function1<? super T, ? extends K> function1) {
        Intrinsics.checkParameterIsNotNull(sequence, "$this$distinctBy");
        Intrinsics.checkParameterIsNotNull(function1, "selector");
        return new DistinctSequence(sequence, function1);
    }

    @NotNull
    public static final <T, A extends Appendable> A joinTo(@NotNull Sequence<? extends T> sequence, @NotNull A a, @NotNull CharSequence charSequence, @NotNull CharSequence charSequence2, @NotNull CharSequence charSequence3, int i, @NotNull CharSequence charSequence4, @Nullable Function1<? super T, ? extends CharSequence> function1) {
        Intrinsics.checkParameterIsNotNull(sequence, "$this$joinTo");
        Intrinsics.checkParameterIsNotNull(a, "buffer");
        Intrinsics.checkParameterIsNotNull(charSequence, "separator");
        Intrinsics.checkParameterIsNotNull(charSequence2, "prefix");
        Intrinsics.checkParameterIsNotNull(charSequence3, "postfix");
        Intrinsics.checkParameterIsNotNull(charSequence4, "truncated");
        a.append(charSequence2);
        int i2 = 0;
        for (Object obj : sequence) {
            i2++;
            if (i2 > 1) {
                a.append(charSequence);
            }
            if (i >= 0 && i2 > i) {
                break;
            }
            StringsKt__StringBuilderKt.appendElement(a, obj, function1);
        }
        if (i >= 0 && i2 > i) {
            a.append(charSequence4);
        }
        a.append(charSequence3);
        return a;
    }

    public static /* synthetic */ String joinToString$default(Sequence sequence, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, int i, CharSequence charSequence4, Function1 function1, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            charSequence = ", ";
        }
        CharSequence charSequence5 = "";
        CharSequence charSequence6 = (i2 & 2) != 0 ? charSequence5 : charSequence2;
        if ((i2 & 4) == 0) {
            charSequence5 = charSequence3;
        }
        if ((i2 & 8) != 0) {
            i = -1;
        }
        if ((i2 & 16) != 0) {
            charSequence4 = "...";
        }
        if ((i2 & 32) != 0) {
            function1 = null;
        }
        return joinToString(sequence, charSequence, charSequence6, charSequence5, i, charSequence4, function1);
    }

    @NotNull
    public static final <T> String joinToString(@NotNull Sequence<? extends T> sequence, @NotNull CharSequence charSequence, @NotNull CharSequence charSequence2, @NotNull CharSequence charSequence3, int i, @NotNull CharSequence charSequence4, @Nullable Function1<? super T, ? extends CharSequence> function1) {
        Intrinsics.checkParameterIsNotNull(sequence, "$this$joinToString");
        Intrinsics.checkParameterIsNotNull(charSequence, "separator");
        Intrinsics.checkParameterIsNotNull(charSequence2, "prefix");
        Intrinsics.checkParameterIsNotNull(charSequence3, "postfix");
        Intrinsics.checkParameterIsNotNull(charSequence4, "truncated");
        StringBuilder sb = new StringBuilder();
        joinTo(sequence, sb, charSequence, charSequence2, charSequence3, i, charSequence4, function1);
        String sb2 = sb.toString();
        Intrinsics.checkExpressionValueIsNotNull(sb2, "joinTo(StringBuilder(), …ed, transform).toString()");
        return sb2;
    }

    @NotNull
    public static <T> Iterable<T> asIterable(@NotNull Sequence<? extends T> sequence) {
        Intrinsics.checkParameterIsNotNull(sequence, "$this$asIterable");
        return new Object(sequence) { // from class: kotlin.sequences.SequencesKt___SequencesKt$asIterable$$inlined$Iterable$1
            final /* synthetic */ Sequence $this_asIterable$inlined;

            {
                this.$this_asIterable$inlined = r1;
            }

            @Override // java.lang.Iterable
            @NotNull
            public Iterator<T> iterator() {
                return this.$this_asIterable$inlined.iterator();
            }
        };
    }
}
