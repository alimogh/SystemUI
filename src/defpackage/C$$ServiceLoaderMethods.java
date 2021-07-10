package defpackage;

import java.util.Arrays;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import kotlinx.coroutines.android.AndroidDispatcherFactory;
import kotlinx.coroutines.android.AndroidExceptionPreHandler;
/* renamed from: $$ServiceLoaderMethods  reason: invalid class name and default package */
public final /* synthetic */ class C$$ServiceLoaderMethods {
    /* renamed from: $load$kotlinx$coroutines$CoroutineExceptionHandlerImplKt$$clinit$-163931$$0  reason: not valid java name */
    public static Iterator m0xadba410b() {
        try {
            return Arrays.asList(new AndroidExceptionPreHandler()).iterator();
        } catch (Throwable th) {
            throw new ServiceConfigurationError(th.getMessage(), th);
        }
    }

    /* renamed from: $load$kotlinx$coroutines$internal$MainDispatcherLoader$loadMainDispatcher$-159772$$0  reason: not valid java name */
    public static Iterator m1x990b35f8() {
        try {
            return Arrays.asList(new AndroidDispatcherFactory()).iterator();
        } catch (Throwable th) {
            throw new ServiceConfigurationError(th.getMessage(), th);
        }
    }
}
