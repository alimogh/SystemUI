package kotlin.text;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: StringBuilder.kt */
/* access modifiers changed from: package-private */
public class StringsKt__StringBuilderKt extends StringsKt__StringBuilderJVMKt {
    public static <T> void appendElement(@NotNull Appendable appendable, T t, @Nullable Function1<? super T, ? extends CharSequence> function1) {
        Intrinsics.checkParameterIsNotNull(appendable, "$this$appendElement");
        if (function1 != null) {
            appendable.append((CharSequence) function1.invoke(t));
            return;
        }
        if (t != null ? t instanceof CharSequence : true) {
            appendable.append(t);
        } else if (t instanceof Character) {
            appendable.append(t.charValue());
        } else {
            appendable.append(String.valueOf(t));
        }
    }
}
