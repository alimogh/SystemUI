package kotlin.text;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: StringsJVM.kt */
public class StringsKt__StringsJVMKt extends StringsKt__StringNumberConversionsKt {
    public static /* synthetic */ boolean startsWith$default(String str, String str2, boolean z, int i, Object obj) {
        if ((i & 2) != 0) {
            z = false;
        }
        return startsWith(str, str2, z);
    }

    public static final boolean startsWith(@NotNull String str, @NotNull String str2, boolean z) {
        Intrinsics.checkParameterIsNotNull(str, "$this$startsWith");
        Intrinsics.checkParameterIsNotNull(str2, "prefix");
        if (!z) {
            return str.startsWith(str2);
        }
        return regionMatches(str, 0, str2, 0, str2.length(), z);
    }

    public static /* synthetic */ boolean endsWith$default(String str, String str2, boolean z, int i, Object obj) {
        if ((i & 2) != 0) {
            z = false;
        }
        return endsWith(str, str2, z);
    }

    public static final boolean endsWith(@NotNull String str, @NotNull String str2, boolean z) {
        Intrinsics.checkParameterIsNotNull(str, "$this$endsWith");
        Intrinsics.checkParameterIsNotNull(str2, "suffix");
        if (!z) {
            return str.endsWith(str2);
        }
        return regionMatches(str, str.length() - str2.length(), str2, 0, str2.length(), true);
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static final boolean isBlank(@org.jetbrains.annotations.NotNull java.lang.CharSequence r4) {
        /*
            java.lang.String r0 = "$this$isBlank"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r4, r0)
            int r0 = r4.length()
            r1 = 0
            r2 = 1
            if (r0 == 0) goto L_0x003e
            kotlin.ranges.IntRange r0 = kotlin.text.StringsKt__StringsKt.getIndices(r4)
            boolean r3 = r0 instanceof java.util.Collection
            if (r3 == 0) goto L_0x0020
            r3 = r0
            java.util.Collection r3 = (java.util.Collection) r3
            boolean r3 = r3.isEmpty()
            if (r3 == 0) goto L_0x0020
        L_0x001e:
            r4 = r2
            goto L_0x003c
        L_0x0020:
            java.util.Iterator r0 = r0.iterator()
        L_0x0024:
            boolean r3 = r0.hasNext()
            if (r3 == 0) goto L_0x001e
            r3 = r0
            kotlin.collections.IntIterator r3 = (kotlin.collections.IntIterator) r3
            int r3 = r3.nextInt()
            char r3 = r4.charAt(r3)
            boolean r3 = kotlin.text.CharsKt__CharJVMKt.isWhitespace(r3)
            if (r3 != 0) goto L_0x0024
            r4 = r1
        L_0x003c:
            if (r4 == 0) goto L_0x003f
        L_0x003e:
            r1 = r2
        L_0x003f:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: kotlin.text.StringsKt__StringsJVMKt.isBlank(java.lang.CharSequence):boolean");
    }

    public static final boolean regionMatches(@NotNull String str, int i, @NotNull String str2, int i2, int i3, boolean z) {
        Intrinsics.checkParameterIsNotNull(str, "$this$regionMatches");
        Intrinsics.checkParameterIsNotNull(str2, "other");
        if (!z) {
            return str.regionMatches(i, str2, i2, i3);
        }
        return str.regionMatches(z, i, str2, i2, i3);
    }
}
