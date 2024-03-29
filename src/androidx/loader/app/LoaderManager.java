package androidx.loader.app;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelStoreOwner;
import java.io.FileDescriptor;
import java.io.PrintWriter;
public abstract class LoaderManager {
    @Deprecated
    public abstract void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    public abstract void markForRedelivery();

    public static <T extends LifecycleOwner & ViewModelStoreOwner> LoaderManager getInstance(T t) {
        return new LoaderManagerImpl(t, t.getViewModelStore());
    }
}
