package kotlinx.coroutines.internal;

import java.io.BufferedReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import kotlin.TypeCastException;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt__MutableCollectionsKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt__StringsKt;
import org.jetbrains.annotations.NotNull;
/* compiled from: FastServiceLoader.kt */
public final class FastServiceLoader {
    public static final FastServiceLoader INSTANCE = new FastServiceLoader();

    private FastServiceLoader() {
    }

    @NotNull
    public final <S> List<S> load$kotlinx_coroutines_core(@NotNull Class<S> cls, @NotNull ClassLoader classLoader) {
        Intrinsics.checkParameterIsNotNull(cls, "service");
        Intrinsics.checkParameterIsNotNull(classLoader, "loader");
        try {
            return loadProviders$kotlinx_coroutines_core(cls, classLoader);
        } catch (Throwable unused) {
            ServiceLoader load = ServiceLoader.load(cls, classLoader);
            Intrinsics.checkExpressionValueIsNotNull(load, "ServiceLoader.load(service, loader)");
            return CollectionsKt___CollectionsKt.toList(load);
        }
    }

    @NotNull
    public final <S> List<S> loadProviders$kotlinx_coroutines_core(@NotNull Class<S> cls, @NotNull ClassLoader classLoader) {
        FastServiceLoader fastServiceLoader = INSTANCE;
        Intrinsics.checkParameterIsNotNull(cls, "service");
        Intrinsics.checkParameterIsNotNull(classLoader, "loader");
        Enumeration<URL> resources = classLoader.getResources("META-INF/services/" + cls.getName());
        Intrinsics.checkExpressionValueIsNotNull(resources, "urls");
        ArrayList<URL> list = Collections.list(resources);
        Intrinsics.checkExpressionValueIsNotNull(list, "java.util.Collections.list(this)");
        ArrayList arrayList = new ArrayList();
        for (URL url : list) {
            Intrinsics.checkExpressionValueIsNotNull(url, "it");
            boolean unused = CollectionsKt__MutableCollectionsKt.addAll(arrayList, fastServiceLoader.parse(url));
        }
        Set<String> set = CollectionsKt___CollectionsKt.toSet(arrayList);
        if (!set.isEmpty()) {
            ArrayList arrayList2 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(set, 10));
            for (String str : set) {
                arrayList2.add(fastServiceLoader.getProviderInstance(str, classLoader, cls));
            }
            return arrayList2;
        }
        throw new IllegalArgumentException("No providers were loaded with FastServiceLoader".toString());
    }

    private final <S> S getProviderInstance(String str, ClassLoader classLoader, Class<S> cls) {
        Class<?> cls2 = Class.forName(str, false, classLoader);
        if (cls.isAssignableFrom(cls2)) {
            return cls.cast(cls2.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]));
        }
        throw new IllegalArgumentException(("Expected service of class " + cls + ", but found " + cls2).toString());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0051, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0052, code lost:
        kotlin.io.CloseableKt.closeFinally(r6, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0055, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0058, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005c, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005d, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x005e, code lost:
        kotlin.ExceptionsKt__ExceptionsKt.addSuppressed(r5, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0061, code lost:
        throw r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x007a, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x007b, code lost:
        kotlin.io.CloseableKt.closeFinally(r0, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x007e, code lost:
        throw r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final java.util.List<java.lang.String> parse(java.net.URL r6) {
        /*
            r5 = this;
            kotlinx.coroutines.internal.FastServiceLoader r5 = kotlinx.coroutines.internal.FastServiceLoader.INSTANCE
            java.lang.String r0 = r6.toString()
            java.lang.String r1 = "url.toString()"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r0, r1)
            java.lang.String r1 = "jar"
            r2 = 0
            r3 = 2
            r4 = 0
            boolean r1 = kotlin.text.StringsKt.startsWith$default(r0, r1, r2, r3, r4)
            if (r1 == 0) goto L_0x0062
            java.lang.String r6 = "jar:file:"
            java.lang.String r6 = kotlin.text.StringsKt.substringAfter$default(r0, r6, r4, r3, r4)
            r1 = 33
            java.lang.String r6 = kotlin.text.StringsKt.substringBefore$default(r6, r1, r4, r3, r4)
            java.lang.String r1 = "!/"
            java.lang.String r0 = kotlin.text.StringsKt.substringAfter$default(r0, r1, r4, r3, r4)
            java.util.jar.JarFile r1 = new java.util.jar.JarFile
            r1.<init>(r6, r2)
            java.io.BufferedReader r6 = new java.io.BufferedReader     // Catch:{ all -> 0x0056 }
            java.io.InputStreamReader r2 = new java.io.InputStreamReader     // Catch:{ all -> 0x0056 }
            java.util.zip.ZipEntry r3 = new java.util.zip.ZipEntry     // Catch:{ all -> 0x0056 }
            r3.<init>(r0)     // Catch:{ all -> 0x0056 }
            java.io.InputStream r0 = r1.getInputStream(r3)     // Catch:{ all -> 0x0056 }
            java.lang.String r3 = "UTF-8"
            r2.<init>(r0, r3)     // Catch:{ all -> 0x0056 }
            r6.<init>(r2)     // Catch:{ all -> 0x0056 }
            java.util.List r5 = r5.parseFile(r6)     // Catch:{ all -> 0x004f }
            kotlin.io.CloseableKt.closeFinally(r6, r4)
            r1.close()     // Catch:{ all -> 0x004d }
            return r5
        L_0x004d:
            r5 = move-exception
            throw r5
        L_0x004f:
            r5 = move-exception
            throw r5     // Catch:{ all -> 0x0051 }
        L_0x0051:
            r0 = move-exception
            kotlin.io.CloseableKt.closeFinally(r6, r5)
            throw r0
        L_0x0056:
            r5 = move-exception
            throw r5     // Catch:{ all -> 0x0058 }
        L_0x0058:
            r6 = move-exception
            r1.close()     // Catch:{ all -> 0x005d }
            throw r6
        L_0x005d:
            r6 = move-exception
            kotlin.ExceptionsKt.addSuppressed(r5, r6)
            throw r5
        L_0x0062:
            java.io.BufferedReader r0 = new java.io.BufferedReader
            java.io.InputStreamReader r1 = new java.io.InputStreamReader
            java.io.InputStream r6 = r6.openStream()
            r1.<init>(r6)
            r0.<init>(r1)
            java.util.List r5 = r5.parseFile(r0)     // Catch:{ all -> 0x0078 }
            kotlin.io.CloseableKt.closeFinally(r0, r4)
            return r5
        L_0x0078:
            r5 = move-exception
            throw r5     // Catch:{ all -> 0x007a }
        L_0x007a:
            r6 = move-exception
            kotlin.io.CloseableKt.closeFinally(r0, r5)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: kotlinx.coroutines.internal.FastServiceLoader.parse(java.net.URL):java.util.List");
    }

    private final List<String> parseFile(BufferedReader bufferedReader) {
        boolean z;
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        while (true) {
            String readLine = bufferedReader.readLine();
            if (readLine == null) {
                return CollectionsKt___CollectionsKt.toList(linkedHashSet);
            }
            String str = StringsKt__StringsKt.substringBefore$default(readLine, "#", (String) null, 2, (Object) null);
            if (str != null) {
                String obj = StringsKt__StringsKt.trim(str).toString();
                boolean z2 = false;
                int i = 0;
                while (true) {
                    if (i >= obj.length()) {
                        z = true;
                        break;
                    }
                    char charAt = obj.charAt(i);
                    if (!(charAt == '.' || Character.isJavaIdentifierPart(charAt))) {
                        z = false;
                        break;
                    }
                    i++;
                }
                if (z) {
                    if (obj.length() > 0) {
                        z2 = true;
                    }
                    if (z2) {
                        linkedHashSet.add(obj);
                    }
                } else {
                    throw new IllegalArgumentException(("Illegal service provider class name: " + obj).toString());
                }
            } else {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.CharSequence");
            }
        }
    }
}
