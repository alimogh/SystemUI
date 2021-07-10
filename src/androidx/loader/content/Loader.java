package androidx.loader.content;

import java.io.FileDescriptor;
import java.io.PrintWriter;
public class Loader<D> {

    public interface OnLoadCompleteListener<D> {
    }

    public abstract void abandon();

    public abstract boolean cancelLoad();

    public abstract String dataToString(D d);

    @Deprecated
    public abstract void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    public abstract void reset();

    public final void startLoading() {
        throw null;
    }

    public abstract void stopLoading();

    public abstract void unregisterListener(OnLoadCompleteListener<D> onLoadCompleteListener);
}
