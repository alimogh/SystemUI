package kotlin.io;

import java.io.File;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: Exceptions.kt */
public final class NoSuchFileException extends FileSystemException {
    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ NoSuchFileException(File file, File file2, String str, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this(file, (i & 2) != 0 ? null : file2, (i & 4) != 0 ? null : str);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public NoSuchFileException(@NotNull File file, @Nullable File file2, @Nullable String str) {
        super(file, file2, str);
        Intrinsics.checkParameterIsNotNull(file, "file");
    }
}
