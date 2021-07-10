package kotlin.io;

import java.io.File;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: Exceptions.kt */
public final class FileAlreadyExistsException extends FileSystemException {
    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public FileAlreadyExistsException(@NotNull File file, @Nullable File file2, @Nullable String str) {
        super(file, file2, str);
        Intrinsics.checkParameterIsNotNull(file, "file");
    }
}
