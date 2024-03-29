package kotlinx.coroutines.internal;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: Symbol.kt */
public final class Symbol {
    @NotNull
    private final String symbol;

    public Symbol(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "symbol");
        this.symbol = str;
    }

    @NotNull
    public String toString() {
        return this.symbol;
    }
}
