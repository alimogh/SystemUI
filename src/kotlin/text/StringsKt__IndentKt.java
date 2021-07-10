package kotlin.text;

import androidx.appcompat.R$styleable;
import java.util.ArrayList;
import java.util.List;
import kotlin.collections.CollectionsKt;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: Indent.kt */
/* access modifiers changed from: package-private */
public class StringsKt__IndentKt {
    @NotNull
    public static String trimIndent(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "$this$trimIndent");
        return replaceIndent(str, "");
    }

    @NotNull
    public static final String replaceIndent(@NotNull String str, @NotNull String str2) {
        String drop;
        Intrinsics.checkParameterIsNotNull(str, "$this$replaceIndent");
        Intrinsics.checkParameterIsNotNull(str2, "newIndent");
        List<String> lines = StringsKt__StringsKt.lines(str);
        ArrayList<String> arrayList = new ArrayList();
        for (Object obj : lines) {
            if (!StringsKt__StringsJVMKt.isBlank((String) obj)) {
                arrayList.add(obj);
            }
        }
        ArrayList arrayList2 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(arrayList, 10));
        for (String str3 : arrayList) {
            arrayList2.add(Integer.valueOf(indentWidth$StringsKt__IndentKt(str3)));
        }
        Integer num = (Integer) CollectionsKt.min(arrayList2);
        int i = 0;
        int intValue = num != null ? num.intValue() : 0;
        int length = str.length() + (str2.length() * lines.size());
        Function1<String, String> indentFunction$StringsKt__IndentKt = getIndentFunction$StringsKt__IndentKt(str2);
        int i2 = CollectionsKt__CollectionsKt.getLastIndex(lines);
        ArrayList arrayList3 = new ArrayList();
        for (Object obj2 : lines) {
            int i3 = i + 1;
            String str4 = null;
            if (i >= 0) {
                String str5 = (String) obj2;
                if ((!(i == 0 || i == i2) || !StringsKt__StringsJVMKt.isBlank(str5)) && ((drop = StringsKt___StringsKt.drop(str5, intValue)) == null || (str4 = indentFunction$StringsKt__IndentKt.invoke(drop)) == null)) {
                    str4 = str5;
                }
                if (str4 != null) {
                    arrayList3.add(str4);
                }
                i = i3;
            } else {
                CollectionsKt.throwIndexOverflow();
                throw null;
            }
        }
        StringBuilder sb = new StringBuilder(length);
        CollectionsKt___CollectionsKt.joinTo$default(arrayList3, sb, "\n", null, null, 0, null, null, R$styleable.AppCompatTheme_windowMinWidthMajor, null);
        String sb2 = sb.toString();
        Intrinsics.checkExpressionValueIsNotNull(sb2, "mapIndexedNotNull { inde…\"\\n\")\n        .toString()");
        return sb2;
    }

    private static final Function1<String, String> getIndentFunction$StringsKt__IndentKt(String str) {
        if (str.length() == 0) {
            return StringsKt__IndentKt$getIndentFunction$1.INSTANCE;
        }
        return new Function1<String, String>(str) { // from class: kotlin.text.StringsKt__IndentKt$getIndentFunction$2
            final /* synthetic */ String $indent;

            {
                this.$indent = r1;
            }

            @NotNull
            public final String invoke(@NotNull String str2) {
                Intrinsics.checkParameterIsNotNull(str2, "line");
                return this.$indent + str2;
            }
        };
    }

    private static final int indentWidth$StringsKt__IndentKt(@NotNull String str) {
        int length = str.length();
        int i = 0;
        while (true) {
            if (i >= length) {
                i = -1;
                break;
            } else if (!CharsKt__CharJVMKt.isWhitespace(str.charAt(i))) {
                break;
            } else {
                i++;
            }
        }
        return i == -1 ? str.length() : i;
    }
}
